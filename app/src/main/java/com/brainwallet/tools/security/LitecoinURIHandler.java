package com.brainwallet.tools.security;

import com.brainwallet.R;
import com.brainwallet.presenter.customviews.BRDialogView;
import com.brainwallet.presenter.entities.PaymentRequestWrapper;
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

import androidx.fragment.app.FragmentActivity;

public class LitecoinURIHandler {

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
            if (BRWalletManager.validateAddress(addrs)) {
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

    public static boolean isValidLitecoinURI(String url) {
        RequestObject requestObject = getRequestFromString(url);
        // return true if the request is valid url and has param: r or param: address
        // return true if it is a valid bitcoinPrivKey
        return (requestObject != null && (requestObject.r != null || requestObject.address != null)
            || BRWalletManager.getInstance().isValidBitcoinBIP38Key(url)
            || BRWalletManager.getInstance().isValidBitcoinPrivateKey(url));
    }

    public static boolean isValidLitecoinUrl(String url) {
        if (isValidLitecoinURI(url)) {
            return BRWalletManager.validateAddress(url);
        }
        return false;
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
                    EventBus.INSTANCE.postQRCodeScanned(requestObject.address);
                }
            });
        }
        return true;
    }


}
