package com.brainwallet.tools.manager;

import android.content.Context;

import androidx.annotation.StringDef;

import com.brainwallet.data.model.Fee;
import com.brainwallet.data.repository.LtcRepository;
import com.brainwallet.data.repository.SettingRepository;
import com.brainwallet.tools.threads.BRExecutor;

import org.koin.java.KoinJavaComponent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//we are still using this, maybe in the future will deprecate?
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

    public void setFees(Fee fee) {
        currentFeeOptions = fee;
    }

    public long getCurrentFeeValue() {
        SettingRepository settingRepository = KoinJavaComponent.get(SettingRepository.class);
        String feeType = settingRepository.getSelectedFeeType();

        switch (feeType) {
            case LUXURY:
                return currentFeeOptions.luxury;
            case REGULAR:
                return currentFeeOptions.regular;
            case ECONOMY:
                return currentFeeOptions.economy;
            default:
                return currentFeeOptions.regular; // Default to regular fee
        }
    }

    public static void updateFeePerKb(Context app) {
        LtcRepository ltcRepository = KoinJavaComponent.get(LtcRepository.class);
        BRExecutor.getInstance().<Fee>executeSuspend(
                (coroutineScope, continuation) -> ltcRepository.fetchFeePerKb(continuation)
        ).whenComplete((fee, throwable) -> {

            FeeManager.getInstance().setFees(fee);
            BRSharedPrefs.putFeeTime(app, System.currentTimeMillis()); //store the time of the last successful fee fetch
        });
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({LUXURY, REGULAR, ECONOMY})
    public @interface FeeType {
    }
}
