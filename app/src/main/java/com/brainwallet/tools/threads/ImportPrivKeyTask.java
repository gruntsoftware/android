package com.brainwallet.tools.threads;

import android.app.Activity;
import android.os.AsyncTask;

import com.brainwallet.tools.animation.BRDialog;
import com.brainwallet.tools.util.BRCurrency;
import com.brainwallet.tools.util.BRExchange;
import com.brainwallet.BuildConfig;
import com.brainwallet.R;
import com.brainwallet.presenter.customviews.BRDialogView;
import com.brainwallet.presenter.entities.ImportPrivKeyEntity;
import com.brainwallet.wallet.BRWalletManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import timber.log.Timber;

public class ImportPrivKeyTask extends AsyncTask<String, String, String> {
    public static final String TAG = ImportPrivKeyTask.class.getName();
    public static String UNSPENT_URL;
    private Activity app;
    private String key;
    private ImportPrivKeyEntity importPrivKeyEntity;

    public ImportPrivKeyTask(Activity activity) {
        app = activity;
        UNSPENT_URL = "https://blockchair.com/litecoin/transaction/";
    }

    @Override
    protected String doInBackground(String... params) {
        if (params.length == 0) return null;
        key = params[0];
        if (key == null || key.isEmpty() || app == null) return null;
        String tmpAddrs = BRWalletManager.getInstance().getAddressFromPrivKey(key);
        String url = UNSPENT_URL + tmpAddrs + "/utxo";
        importPrivKeyEntity = createTx(url);
        if (importPrivKeyEntity == null) {
            app.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BRDialog.showCustomDialog(app, app.getString(R.string.JailbreakWarnings_title),
                            app.getString(R.string.Import_Error_empty), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismissWithAnimation();
                                }
                            }, null, null, 0);
                }
            });
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        if (importPrivKeyEntity == null) {
            return;
        }

//        String iso = BRSharedPrefs.getIsoSymbol(app);

        String sentBits = BRCurrency.getFormattedCurrencyString(app, "LTC", BRExchange.getAmountFromLitoshis(app, "LTC", new BigDecimal(importPrivKeyEntity.getAmount())));
//        String sentExchange = BRCurrency.getFormattedCurrencyString(app, iso, BRExchange.getAmountFromLitoshis(app, iso, new BigDecimal(importPrivKeyEntity.getAmount())));

        String feeBits = BRCurrency.getFormattedCurrencyString(app, "LTC", BRExchange.getAmountFromLitoshis(app, "LTC", new BigDecimal(importPrivKeyEntity.getFee())));
//        String feeExchange = BRCurrency.getFormattedCurrencyString(app, iso, BRExchange.getAmountFromLitoshis(app, iso, new BigDecimal(importPrivKeyEntity.getFee())));

        if (app == null || importPrivKeyEntity == null) return;
        String message = String.format(app.getString(R.string.Import_confirm), sentBits, feeBits);
        String posButton = String.format("%s (%s)", sentBits, feeBits);
        BRDialog.showCustomDialog(app, "", message, posButton, app.getString(R.string.Button_cancel), new BRDialogView.BROnClickListener() {
            @Override
            public void onClick(BRDialogView brDialogView) {
                BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        boolean result = BRWalletManager.getInstance().confirmKeySweep(importPrivKeyEntity.getTx(), key);
                        if (!result) {
                            app.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    BRDialog.showCustomDialog(app, app.getString(R.string.JailbreakWarnings_title),
                                            app.getString(R.string.Import_Error_notValid), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                                                @Override
                                                public void onClick(BRDialogView brDialogView) {
                                                    brDialogView.dismissWithAnimation();
                                                }
                                            }, null, null, 0);
                                }
                            });

                        }
                    }
                });

                brDialogView.dismissWithAnimation();

            }
        }, new BRDialogView.BROnClickListener() {
            @Override
            public void onClick(BRDialogView brDialogView) {
                brDialogView.dismissWithAnimation();
            }
        }, null, 0);
        super.onPostExecute(s);
    }

    public static ImportPrivKeyEntity createTx(String url) {
        if (url == null || url.isEmpty()) return null;
        String jsonString = callURL(url);
        if (jsonString == null || jsonString.isEmpty()) return null;
        ImportPrivKeyEntity result = null;
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
            int length = jsonArray.length();
            if (length > 0)
                BRWalletManager.getInstance().createInputArray();

            for (int i = 0; i < length; i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String txid = obj.getString("txid");
                int vout = obj.getInt("vout");
                String scriptPubKey = obj.getString("scriptPubKey");
                long amount = obj.getLong("satoshis");
                byte[] txidBytes = hexStringToByteArray(txid);
                byte[] scriptPubKeyBytes = hexStringToByteArray(scriptPubKey);
                BRWalletManager.getInstance().addInputToPrivKeyTx(txidBytes, vout, scriptPubKeyBytes, amount);
            }

            result = BRWalletManager.getInstance().getPrivKeyObject();

        } catch (JSONException e) {
            Timber.e(e);
        }
        return result;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static String callURL(String myURL) {
//        System.out.println("Requested URL_EA:" + myURL);
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn = null;
        InputStreamReader in = null;
        try {
            URL url = new URL(myURL);
            urlConn = url.openConnection();
            if (urlConn != null)
                urlConn.setReadTimeout(60 * 1000);
            if (urlConn != null && urlConn.getInputStream() != null) {
                in = new InputStreamReader(urlConn.getInputStream(),
                        Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(in);

                int cp;
                while ((cp = bufferedReader.read()) != -1) {
                    sb.append((char) cp);
                }
                bufferedReader.close();
            }
            assert in != null;
            in.close();
        } catch (Exception e) {
            return null;
        }

        return sb.toString();
    }
}
