package com.brainwallet.tools.security;

import androidx.fragment.app.FragmentActivity;

import com.brainwallet.R;
import com.brainwallet.navigation.LegacyNavigation;
import com.brainwallet.navigation.Route;
import com.brainwallet.presenter.customviews.BRDialogView;
import com.brainwallet.presenter.entities.RequestObject;
import com.brainwallet.tools.animation.BRDialog;
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
 * This is the Litecoin-named entry point for that logic; use it instead of
 * {@link BitcoinUrlHandler}, which now exists only to hold the payment-protocol
 * native method declarations (see that class for why).
 */
public class LitecoinURIHandler {
    private static final Object lockObject = new Object();

    public static synchronized boolean processRequest(FragmentActivity app, String url) {
        if (url == null) {
            Timber.d("timber: processRequest: url is null");
            return false;
        }

        RequestObject requestObject = getRequestFromString(url);
        if (BRWalletManager.getInstance().confirmSweep(app, url)) {
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
            return tryLitecoinURL(url, app);
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
            if (BRWalletManager.getInstance().validateAddress(addrs)) {
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
        RequestObject requestObject = getRequestFromString(url);
        return (requestObject != null && (requestObject.r != null || requestObject.address != null)
                || BRWalletManager.getInstance().isValidBitcoinBIP38Key(url)
                || BRWalletManager.getInstance().isValidBitcoinPrivateKey(url));
    }

    public static boolean isValidLitecoinUrl(String url) {
        if (isValidLitecoinURI(url)) {
            return BRWalletManager.getInstance().validateAddress(url);
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

    private static boolean tryLitecoinURL(final String url, final FragmentActivity app) {
        RequestObject requestObject = getRequestFromString(url);
        if (requestObject == null || requestObject.address == null || requestObject.address.isEmpty())
            return false;

        String amount = requestObject.amount;

        if (amount == null || amount.isEmpty() || new BigDecimal(amount).doubleValue() == 0) {
            app.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Keep posting this in case the Send screen is already open and
                    // collecting (e.g. address scanned via the in-app camera flow).
                    EventBus.INSTANCE.postQRCodeScanned(requestObject.address);
                    // Deep link straight to the Send screen with the address pasted
                    // in, so scanning this QR code from another app (camera, another
                    // wallet, etc.) reliably lands there instead of wherever the app
                    // happened to be, or nowhere at all on a cold start.
                    LegacyNavigation.openComposeScreen(app, new Route.Send(requestObject.address));
                }
            });
        }
        return true;
    }
}
