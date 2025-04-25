package com.brainwallet.tools.manager;

import android.content.Context;

import androidx.annotation.StringDef;

import com.brainwallet.data.repository.LtcRepository;
import com.brainwallet.presenter.entities.ServiceItems;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.tools.util.Utils;
import com.brainwallet.data.model.Fee;
import com.platform.APIClient;

import org.koin.java.KoinJavaComponent;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

//we are still using this, maybe in the future will deprecate?
public final class FeeManager {


    private static final FeeManager instance;

    private String feeType;
    public Fee currentFees;

    public static FeeManager getInstance() {
        return instance;
    }

    static {
        instance = new FeeManager();
        instance.initWithDefaultValues();
    }

    private void initWithDefaultValues() {
        currentFees = Fee.getDefault();
        feeType = REGULAR;
    }

    private FeeManager() {
    }

    public void setFeeType(@FeeType String feeType) {
        this.feeType = feeType;
    }

    public void resetFeeType() {
        this.feeType = REGULAR;
    }

    public boolean isRegularFee() {
        return feeType.equals(REGULAR);
    }

    public static final String LUXURY = "luxury";
    public static final String REGULAR = "regular";
    public static final String ECONOMY = "economy";

    public void setFees(long luxuryFee, long regularFee, long economyFee) {
        // TODO: to be implemented when feePerKB API will be ready
        currentFees = new Fee(luxuryFee, regularFee, economyFee, System.currentTimeMillis());
    }

    public static void updateFeePerKb(Context app) {

//        String jsonString = "{'fee_per_kb': 10000, 'fee_per_kb_economy': 2500, 'fee_per_kb_luxury': 66746}";
//        try {
//            JSONObject obj = new JSONObject(jsonString);
//            // TODO: Refactor when mobile-api v0.4.0 is in prod
//            long regularFee = obj.optLong("fee_per_kb");
//            long economyFee = obj.optLong("fee_per_kb_economy");
//            long luxuryFee = obj.optLong("fee_per_kb_luxury");
//            FeeManager.getInstance().setFees(luxuryFee, regularFee, economyFee);
//            BRSharedPrefs.putFeeTime(app, System.currentTimeMillis()); //store the time of the last successful fee fetch
//        } catch (JSONException e) {
//            Timber.e(new IllegalArgumentException("updateFeePerKb: FAILED: " + jsonString, e));
//        }

        LtcRepository ltcRepository = KoinJavaComponent.get(LtcRepository.class);
        BRExecutor.getInstance().<Fee>executeSuspend(
                (coroutineScope, continuation) -> ltcRepository.fetchFeePerKb(continuation)
        ).whenComplete((fee, throwable) -> {

            //legacy logic
            FeeManager.getInstance().setFees(fee.luxury, fee.regular, fee.economy);
            BRSharedPrefs.putFeeTime(app, System.currentTimeMillis()); //store the time of the last successful fee fetch
        });
    }

    // createGETRequestURL
    // Creates the params and headers to make a GET Request
    @Deprecated
    private static String createGETRequestURL(Context app, String myURL) {
        Request request = new Request.Builder()
                .url(myURL)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-agent", Utils.getAgentString(app, "android/HttpURLConnection"))
                .header("BW-client-code", Utils.fetchServiceItem(app, ServiceItems.CLIENTCODE))
                .get().build();
        String response = null;
        Response resp = APIClient.getInstance(app).sendRequest(request, false, 0);

        try {
            if (resp == null) {
                Timber.i("timber: urlGET: %s resp is null", myURL);
                return null;
            }
            response = resp.body().string();
            String strDate = resp.header("date");
            if (strDate == null) {
                Timber.i("timber: urlGET: strDate is null!");
                return response;
            }
            SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            Date date = formatter.parse(strDate);
            long timeStamp = date.getTime();
            BRSharedPrefs.putSecureTime(app, timeStamp);
        } catch (ParseException | IOException e) {
            Timber.e(e);
        } finally {
            if (resp != null) resp.close();
        }
        return response;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({LUXURY, REGULAR, ECONOMY})
    public @interface FeeType {
    }
}
