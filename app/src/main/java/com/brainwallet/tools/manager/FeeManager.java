package com.brainwallet.tools.manager;

import android.content.Context;

import androidx.annotation.StringDef;

import com.brainwallet.data.repository.LtcRepository;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.data.model.Fee;

import org.koin.java.KoinJavaComponent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//we are still using this, maybe in the future will deprecate?
@Deprecated
public final class FeeManager {


    private static final FeeManager instance;

    private String feeType;
    public Fee currentFeeOptions;

    public static FeeManager getInstance() {
        return instance;
    }

    static {
        instance = new FeeManager();
        instance.initWithDefaultValues();
    }

    private void initWithDefaultValues() {
        currentFeeOptions = Fee.getDefault();
        feeType = LUXURY;
    }

    private FeeManager() {
    }

    public void setFeeType(@FeeType String feeType) {
        this.feeType = feeType;
    }

    public void resetFeeType() {
        this.feeType = LUXURY;
    }

    public boolean isLuxuryFee() {
        return feeType.equals(LUXURY);
    }

    public static final String LUXURY = "luxury";//top
    public static final String REGULAR = "regular";//medium
    public static final String ECONOMY = "economy";//low

    public void setFees(long luxuryFee, long regularFee, long economyFee) {
        currentFeeOptions = new Fee(luxuryFee, regularFee, economyFee);
    }

    public static void updateFeePerKb(Context app) {
        LtcRepository ltcRepository = KoinJavaComponent.get(LtcRepository.class);
        BRExecutor.getInstance().<Fee>executeSuspend(
                (coroutineScope, continuation) -> ltcRepository.fetchFeePerKb(continuation)
        ).whenComplete((fee, throwable) -> {

            //legacy logic
            FeeManager.getInstance().setFees(fee.luxury, fee.regular, fee.economy);
            BRSharedPrefs.putFeeTime(app, System.currentTimeMillis()); //store the time of the last successful fee fetch
        });
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({LUXURY, REGULAR, ECONOMY})
    public @interface FeeType {
    }
}
