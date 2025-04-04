package com.brainwallet.tools.threads;

import android.app.Activity;
import android.os.AsyncTask;

import com.brainwallet.tools.animation.BRDialog;
import com.brainwallet.tools.manager.BRSharedPrefs;
import com.brainwallet.tools.security.BitcoinUrlHandler;
import com.brainwallet.tools.security.X509CertificateValidator;
import com.brainwallet.tools.util.BRConstants;
import com.brainwallet.tools.util.BRCurrency;
import com.brainwallet.tools.util.BRExchange;
import com.brainwallet.tools.util.BytesUtil;
import com.brainwallet.tools.util.CustomLogger;
import com.brainwallet.BrainwalletApp;
import com.brainwallet.R;
import com.brainwallet.exceptions.CertificateChainNotFound;
import com.brainwallet.presenter.customviews.BRDialogView;
import com.brainwallet.presenter.entities.PaymentRequestWrapper;
import com.brainwallet.wallet.BRWalletManager;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class PaymentProtocolTask extends AsyncTask<String, String, String> {
    HttpURLConnection urlConnection;
    String certName = null;
    PaymentRequestWrapper paymentRequest = null;
    int certified = 0;
    Activity app;

    //params[0] = uri, params[1] = label
    @Override
    protected String doInBackground(String... params) {
        app = (Activity) BrainwalletApp.getBreadContext();
        InputStream in;
        try {
            Timber.d("timber: the uri: %s", params[0]);
            URL url = new URL(params[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept", "application/litecoin-paymentrequest");
            urlConnection.setConnectTimeout(3000);
            urlConnection.setReadTimeout(3000);
            urlConnection.setUseCaches(false);
            in = urlConnection.getInputStream();

            if (in == null) {
                Timber.i("timber: The inputStream is null!");
                return null;
            }
            byte[] serializedBytes = BytesUtil.readBytesFromStream(in);
            if (serializedBytes == null || serializedBytes.length == 0) {
                Timber.i("timber: serializedBytes are null!!!");
                return null;
            }

            paymentRequest = BitcoinUrlHandler.parsePaymentRequest(serializedBytes);

            if (paymentRequest == null || paymentRequest.error == PaymentRequestWrapper.INVALID_REQUEST_ERROR) {
                Timber.i("timber: paymentRequest is null!!!");
                BRDialog.showCustomDialog(app, "", app.getString(R.string.Send_remoteRequestError), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismissWithAnimation();
                    }
                }, null, null, 0);
                paymentRequest = null;
                return null;
            } else if (paymentRequest.error == PaymentRequestWrapper.INSUFFICIENT_FUNDS_ERROR) {
                Timber.i("timber: insufficient amount!!!");
                BRDialog.showCustomDialog(app, "", app.getString(R.string.Alerts_sendFailure), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismissWithAnimation();
                    }
                }, null, null, 0);
                paymentRequest = null;
                return null;
            } else if (paymentRequest.error == PaymentRequestWrapper.SIGNING_FAILED_ERROR) {
                Timber.i("timber: failed to sign tx!!! \n insufficient amount!!!");
                BRDialog.showCustomDialog(app, "", app.getString(R.string.Import_Error_signing), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismissWithAnimation();
                    }
                }, null, null, 0);
                paymentRequest = null;
                return null;
            } else if (paymentRequest.error == PaymentRequestWrapper.REQUEST_TOO_LONG_ERROR) {
                Timber.i("timber: failed to sign tx!!!");
                BRDialog.showCustomDialog(app, app.getString(R.string.PaymentProtocol_Errors_badPaymentRequest), "Too long", app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismissWithAnimation();
                    }
                }, null, null, 0);
                paymentRequest = null;
                return null;
            } else if (paymentRequest.error == PaymentRequestWrapper.AMOUNTS_ERROR) {
                Timber.i("timber: failed to sign tx!!!");
                BRDialog.showCustomDialog(app, "", app.getString(R.string.PaymentProtocol_Errors_badPaymentRequest), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismissWithAnimation();
                    }
                }, null, null, 0);
                paymentRequest = null;
                return null;
            }

            //Logging
            StringBuilder allAddresses = new StringBuilder();
            for (String s : paymentRequest.addresses) {
                allAddresses.append(s).append(", ");
                if (!BRWalletManager.validateAddress(s)) {
                    if (app != null)
                        BRDialog.showCustomDialog(app, app.getString(R.string.Alert_error), app.getString(R.string.Send_invalidAddressTitle) + ": " + s, app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismissWithAnimation();
                            }
                        }, null, null, 0);
                    paymentRequest = null;
                    return null;
                }
            }

            allAddresses.delete(allAddresses.length() - 2, allAddresses.length());

            CustomLogger.logThis("Signature", String.valueOf(paymentRequest.signature.length),
                    "pkiType", paymentRequest.pkiType, "pkiData", String.valueOf(paymentRequest.pkiData.length));
            CustomLogger.logThis("network", paymentRequest.network, "time", String.valueOf(paymentRequest.time),
                    "expires", String.valueOf(paymentRequest.expires), "memo", paymentRequest.memo,
                    "paymentURL", paymentRequest.paymentURL, "merchantDataSize",
                    String.valueOf(paymentRequest.merchantData.length), "address", allAddresses.toString(),
                    "amount", String.valueOf(paymentRequest.amount));
            //end logging
            if (paymentRequest.expires != 0 && paymentRequest.time > paymentRequest.expires) {
                Timber.i("timber: Request is expired");
                if (app != null)
                    BRDialog.showCustomDialog(app, app.getString(R.string.Alert_error), app.getString(R.string.PaymentProtocol_Errors_requestExpired), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismissWithAnimation();
                        }
                    }, null, null, 0);
                paymentRequest = null;
                return null;
            }
            List<X509Certificate> certList = X509CertificateValidator.getCertificateFromBytes(serializedBytes);
            certName = X509CertificateValidator.certificateValidation(certList, paymentRequest);
        } catch (Exception e) {
            if (e instanceof java.net.UnknownHostException) {
                if (app != null)
                    BRDialog.showCustomDialog(app, app.getString(R.string.Alert_error), app.getString(R.string.PaymentProtocol_Errors_corruptedDocument), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismissWithAnimation();
                        }
                    }, null, null, 0);
                paymentRequest = null;
            } else if (e instanceof FileNotFoundException) {
                if (app != null)
                    BRDialog.showCustomDialog(app, app.getString(R.string.Alert_error), app.getString(R.string.PaymentProtocol_Errors_badPaymentRequest), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismissWithAnimation();
                        }
                    }, null, null, 0);
                paymentRequest = null;
            } else if (e instanceof SocketTimeoutException) {
                if (app != null)
                    BRDialog.showCustomDialog(app, app.getString(R.string.Alert_error), "Connection timed-out", app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismissWithAnimation();
                        }
                    }, null, null, 0);
                paymentRequest = null;
            } else if (e instanceof CertificateChainNotFound) {
                Timber.e(e, "timber:No certificates!");
            } else {
                if (app != null)
                    BRDialog.showCustomDialog(app, app.getString(R.string.JailbreakWarnings_title), app.getString(R.string.PaymentProtocol_Errors_badPaymentRequest) + ":" + e.getMessage(), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismissWithAnimation();
                        }
                    }, null, null, 0);

                paymentRequest = null;
            }
            Timber.e(e);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
        if (paymentRequest == null) return null;
        if (!paymentRequest.pkiType.equals("none") && certName == null) {
            certified = 2;
        } else if (!paymentRequest.pkiType.equals("none") && certName != null) {
            certified = 1;
        }
        certName = extractCNFromCertName(certName);
        if (certName == null || certName.isEmpty())
            certName = params[1];
        if (certName == null || certName.isEmpty())
            certName = paymentRequest.addresses[0];
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (app == null) return;
        if (paymentRequest == null || paymentRequest.addresses == null ||
                paymentRequest.addresses.length == 0 || paymentRequest.amount == 0) {
            return;
        }
        final String certification;
        if (certified == 0) {
            certification = certName + "\n";
        } else {
            if (certName == null || certName.isEmpty()) {
                certification = "\u274C " + certName + "\n";
                BRDialog.showCustomDialog(app, app.getString(R.string.PaymentProtocol_Errors_untrustedCertificate), "", app.getString(R.string.JailbreakWarnings_ignore), app.getString(R.string.Button_cancel), new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        continueWithThePayment(app, certification);
                    }
                }, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismissWithAnimation();
                    }
                }, null, 0);
                return;
            } else {
                certification = "\uD83D\uDD12 " + certName + "\n";
            }
        }
        continueWithThePayment(app, certification);
    }

    private String extractCNFromCertName(String str) {
        if (str == null || str.length() < 4) return null;
        String cn = "CN=";
        int index = -1;
        int endIndex = -1;
        for (int i = 0; i < str.length() - 3; i++) {
            if (str.substring(i, i + 3).equalsIgnoreCase(cn)) {
                index = i + 3;
            }
            if (index != -1) {
                if (str.charAt(i) == ',') {
                    endIndex = i;
                    break;
                }
            }
        }
        String cleanCN = str.substring(index, endIndex);
        return (index != -1 && endIndex != -1) ? cleanCN : null;
    }

    private void continueWithThePayment(final Activity app, final String certification) {

        StringBuilder allAddresses = new StringBuilder();
        for (String s : paymentRequest.addresses) {
            allAddresses.append(s + ", ");
        }
        allAddresses.delete(allAddresses.length() - 2, allAddresses.length());
        if (paymentRequest.memo == null) paymentRequest.memo = "";
        final String memo = (!paymentRequest.memo.isEmpty() ? "\n" : "") + paymentRequest.memo;
        allAddresses = new StringBuilder();

        final String iso = BRSharedPrefs.getIsoSymbol(app);
        final StringBuilder finalAllAddresses = allAddresses;
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                double minOutput = BRWalletManager.getInstance().getMinOutputAmount();
                if (paymentRequest.amount < minOutput) {
                    final String bitcoinMinMessage = String.format(Locale.getDefault(), app.getString(R.string.PaymentProtocol_Errors_smallTransaction),
                            BRConstants.litecoinLowercase + new BigDecimal(minOutput).divide(new BigDecimal("100")));
                    app.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BRDialog.showCustomDialog(app, app.getString(R.string.PaymentProtocol_Errors_badPaymentRequest), bitcoinMinMessage, app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismissWithAnimation();
                                }
                            }, null, null, 0);
                        }
                    });

                    return;
                }
                final long total = paymentRequest.amount + paymentRequest.fee;

                BigDecimal bigAm = BRExchange.getAmountFromLitoshis(app, iso, new BigDecimal(paymentRequest.amount));
                BigDecimal bigFee = BRExchange.getAmountFromLitoshis(app, iso, new BigDecimal(paymentRequest.fee));
                BigDecimal bigTotal = BRExchange.getAmountFromLitoshis(app, iso, new BigDecimal(total));
                final String message = certification + memo + finalAllAddresses.toString() + "\n\n" + "amount: " + BRCurrency.getFormattedCurrencyString(app, iso, bigAm)
                        + "\nnetwork fee: +" + BRCurrency.getFormattedCurrencyString(app, iso, bigFee)
                        + "\ntotal: " + BRCurrency.getFormattedCurrencyString(app, iso, bigTotal);

                app.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /// DEV NOTES: Remove this call to auth Prompt
                        
                    }
                });
            }
        });

    }

}
