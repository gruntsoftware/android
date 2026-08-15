package com.brainwallet.tools.util;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.FragmentActivity;

import com.brainwallet.R;
import com.brainwallet.navigation.DeepLink;
import com.brainwallet.presenter.customviews.BRDialogView;
import com.brainwallet.presenter.entities.PaymentRequestWrapper;
import com.brainwallet.presenter.entities.RequestObject;
import com.brainwallet.tools.animation.BRDialog;
import com.brainwallet.tools.manager.BRClipboardManager;
import com.brainwallet.tools.threads.PaymentProtocolTask;
import com.brainwallet.util.EventBus;
import com.brainwallet.wallet.BRWalletManager;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;

import timber.log.Timber;

/**
 * Handles incoming litecoin: URIs (deep links, QR scans) - address-only requests,
 * BIP21-style amount/label/message params, and BIP70 payment-protocol requests (r=).
 *
 * The BIP70 native methods below are implemented in
 * app/src/main/jni/transition/JNIPaymentsCore.c, resolved by the JNI runtime via their exact
 * symbol name (Java_com_brainwallet_tools_security_LitecoinURIHandler_...) - keep the
 * class/method names here and the JNIEXPORT function names there in sync.
 */
public class LitecoinURIHandler {
    private static final Object lockObject = new Object();

    /**
     * The address/key checks this class needs from {@link BRWalletManager} - all four are
     * native methods, which mockk cannot intercept on a mocked BRWalletManager instance in
     * a plain JVM unit test (unlike regular methods, there's no bytecode body to override -
     * see {@link com.brainwallet.wallet.WalletManager} for the same problem solved for
     * validateAddress/isCreated elsewhere). Package-private overloads below accept this
     * seam so tests can supply a plain fake instead of a real BRWalletManager.
     */
    @VisibleForTesting
    interface AddressResolver {
        boolean validateAddress(String address);

        boolean isValidBitcoinBIP38Key(String key);

        boolean isValidBitcoinPrivateKey(String key);

        boolean confirmSweep(Context ctx, String privKey);

        static AddressResolver usingWalletManager() {
            BRWalletManager manager = BRWalletManager.getInstance();
            return new AddressResolver() {
                @Override
                public boolean validateAddress(String address) {
                    return manager.validateAddress(address);
                }

                @Override
                public boolean isValidBitcoinBIP38Key(String key) {
                    return manager.isValidBitcoinBIP38Key(key);
                }

                @Override
                public boolean isValidBitcoinPrivateKey(String key) {
                    return manager.isValidBitcoinPrivateKey(key);
                }

                @Override
                public boolean confirmSweep(Context ctx, String privKey) {
                    return manager.confirmSweep(ctx, privKey);
                }
            };
        }
    }

    /**
     * @see #processRequest(FragmentActivity, String, boolean) - defaults to treating this
     * as an external deep link (the manifest's litecoin: scheme intent-filter).
     */
    public static synchronized boolean processRequest(FragmentActivity app, String url) {
        return processRequest(app, url, true);
    }

    /**
     * @param isExternalDeepLink true for a litecoin: URI arriving from outside the app - the
     *                           recipient's camera/QR app scanning ReceiveDialog's QR code,
     *                           resolved via the manifest's litecoin: scheme intent-filter
     *                           to BreadActivity. On a resolved address this always copies it
     *                           to the clipboard and shows a toast, then opens the Unlock
     *                           screen carrying the address; once the PIN is verified *and*
     *                           the wallet is fully synced, BrainwalletActivity.onUnlock
     *                           continues on to the existing Send screen (via Compose
     *                           navigation, not a new Activity) with the address pasted in.
     *                           <p>
     *                           false for a QR code scanned in-app (e.g. tapping "Scan" on an
     *                           already-open Send screen) - the user is already authenticated
     *                           and already looking at Send, so this just pastes the address
     *                           into its recipient field instead of tearing down to a new,
     *                           disconnected BrainwalletActivity/re-auth flow.
     */
    public static synchronized boolean processRequest(FragmentActivity app, String url, boolean isExternalDeepLink) {
        return processRequest(app, url, isExternalDeepLink, AddressResolver.usingWalletManager());
    }

    @VisibleForTesting
    static synchronized boolean processRequest(FragmentActivity app, String url, AddressResolver resolver) {
        return processRequest(app, url, true, resolver);
    }

    @VisibleForTesting
    static synchronized boolean processRequest(FragmentActivity app, String url, boolean isExternalDeepLink, AddressResolver resolver) {
        if (url == null) {
            Timber.d("timber: processRequest: url is null");
            return false;
        }

        RequestObject requestObject = getRequestFromString(url, resolver);
        if (resolver.confirmSweep(app, url)) {
            return true;
        }
        if (requestObject == null) {
            if (app != null) {
                BRDialog.showCustomDialog(app, app.getString(R.string.JailbreakWarnings_title),
                        app.getString(R.string.Send_invalidAddressTitle), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismissWithAnimation();
                            }
                        }, null, null, 0);
            }
            return false;
        }
        if (requestObject.r != null) {
            return tryPaymentRequest(requestObject);
        } else if (requestObject.address != null) {
            return tryLitecoinURL(url, app, isExternalDeepLink, resolver);
        } else {
            if (app != null) {
                BRDialog.showCustomDialog(app, app.getString(R.string.JailbreakWarnings_title),
                        app.getString(R.string.Send_remoteRequestError), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismissWithAnimation();
                            }
                        }, null, null, 0);
            }
            return false;
        }
    }

    public static RequestObject getRequestFromString(String str) {
        return getRequestFromString(str, AddressResolver.usingWalletManager());
    }

    @VisibleForTesting
    static RequestObject getRequestFromString(String str, AddressResolver resolver) {
        if (str == null || str.isEmpty()) return null;
        RequestObject obj = new RequestObject();

        String tmp = str.trim().replaceAll("\n", "").replaceAll(" ", "%20");

        if (!tmp.startsWith("litecoin://")) {
            if (!tmp.startsWith("litecoin:"))
                tmp = "litecoin://".concat(tmp);
            else
                tmp = tmp.replace("litecoin:", "litecoin://");
        }
        URI uri;
        try {
            uri = URI.create(tmp);
        } catch (IllegalArgumentException ex) {
            Timber.e(ex, "getRequestFromString: ");
            return null;
        }

        String host = uri.getHost();
        if (host != null) {
            String addrs = host.trim();
            if (resolver.validateAddress(addrs)) {
                obj.address = addrs;
            }
        }
        String query = uri.getQuery();
        if (query == null) return obj;
        String[] params = query.split("&");
        for (String s : params) {
            String[] keyValue = s.split("=", 2);
            if (keyValue.length != 2)
                continue;
            if (keyValue[0].trim().equals("amount")) {
                try {
                    BigDecimal bigDecimal = new BigDecimal(keyValue[1].trim());
                    obj.amount = bigDecimal.multiply(new BigDecimal("100000000")).toString();
                } catch (NumberFormatException e) {
                    Timber.e(e);
                }
            } else if (keyValue[0].trim().equals("label")) {
                obj.label = keyValue[1].trim();
            } else if (keyValue[0].trim().equals("message")) {
                obj.message = keyValue[1].trim();
            } else if (keyValue[0].trim().startsWith("req")) {
                obj.req = keyValue[1].trim();
            } else if (keyValue[0].trim().startsWith("r")) {
                obj.r = keyValue[1].trim();
            }
        }
        return obj;
    }

    /**
     * Returns true if the request is a valid URI with an r= or address param, or a
     * valid Litecoin BIP38/private key.
     */
    public static boolean isValidLitecoinURI(String url) {
        return isValidLitecoinURI(url, AddressResolver.usingWalletManager());
    }

    @VisibleForTesting
    static boolean isValidLitecoinURI(String url, AddressResolver resolver) {
        RequestObject requestObject = getRequestFromString(url, resolver);
        return (requestObject != null && (requestObject.r != null || requestObject.address != null)
                || resolver.isValidBitcoinBIP38Key(url)
                || resolver.isValidBitcoinPrivateKey(url));
    }

    public static boolean isValidLitecoinUrl(String url) {
        return isValidLitecoinUrl(url, AddressResolver.usingWalletManager());
    }

    @VisibleForTesting
    static boolean isValidLitecoinUrl(String url, AddressResolver resolver) {
        if (isValidLitecoinURI(url, resolver)) {
            return resolver.validateAddress(url);
        }
        return false;
    }

    private static boolean tryPaymentRequest(RequestObject requestObject) {
        String theURL;
        String url = requestObject.r;
        synchronized (lockObject) {
            try {
                theURL = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Timber.e(e);
                return false;
            }
            new PaymentProtocolTask().execute(theURL, requestObject.label);
        }
        return true;
    }

    @SuppressWarnings("deprecation") // intentional EventBus#postQRCodeScanned use, see below
    private static boolean tryLitecoinURL(
            final String url,
            final FragmentActivity app,
            final boolean isExternalDeepLink,
            AddressResolver resolver
    ) {
        RequestObject requestObject = getRequestFromString(url, resolver);
        if (requestObject == null || requestObject.address == null || requestObject.address.isEmpty())
            return false;

        String amount = requestObject.amount;

        if (amount == null || amount.isEmpty() || new BigDecimal(amount).doubleValue() == 0) {
            // requestObject.address has already been through AddressResolver#validateAddress
            // (see getRequestFromString above).
            app.runOnUiThread(() -> {
                if (!isExternalDeepLink) {
                    // In-app scan (e.g. tapping "Scan" on an already-open Send screen): the
                    // user is already authenticated and already looking at Send - just hand
                    // the verified address off to whichever screen is listening (e.g.
                    // SendViewModel) instead of tearing down to a new, disconnected
                    // BrainwalletActivity/re-auth flow below. This is the one remaining
                    // legacy EventBus bridge - see EventBus#postQRCodeScanned's deprecation
                    // note; a true deep link (below) never touches EventBus.
                    EventBus.INSTANCE.postQRCodeScanned(requestObject.address);
                    return;
                }

                // External deep link (the recipient's QR code, scanned by the device
                // camera/another app, resolved via BreadActivity's litecoin: intent-filter):
                // always copy the address to the clipboard and let the user know - it's all
                // they get if the wallet isn't synced enough to send yet below.
                BRClipboardManager.putClipboard(app, requestObject.address);
                Toast.makeText(app, R.string.Send_unlockDeepLink, Toast.LENGTH_LONG).show();

                // Route through DeepLink (Compose Navigation, opening MainScreen's Send modal
                // once authenticated+synced) rather than EventBus - see DeepLink's class doc.
                DeepLink.sendToAddress(requestObject.address).open(app);
            });
        }
        return true;
    }

    public static native PaymentRequestWrapper parsePaymentRequest(byte[] req);

    public static native String parsePaymentACK(byte[] req);

    public static native byte[] getCertificatesFromPaymentRequest(byte[] req, int index);
}
