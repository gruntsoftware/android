package ltd.grunt.brainwallet.tools.security;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;

import ltd.grunt.brainwallet.R;
import ltd.grunt.brainwallet.presenter.activities.DisabledActivity;
import ltd.grunt.brainwallet.presenter.activities.util.ActivityUTILS;
import ltd.grunt.brainwallet.tools.manager.BRSharedPrefs;
import ltd.grunt.brainwallet.tools.security.BRKeyStore;
import ltd.grunt.brainwallet.tools.threads.BRExecutor;
import ltd.grunt.brainwallet.wallet.BRWalletManager;

import timber.log.Timber;

public class AuthManager {
    private static AuthManager instance;
    private String previousTry;

    private AuthManager() {
        previousTry = "";
    }

    public static AuthManager getInstance() {
        if (instance == null)
            instance = new AuthManager();
        return instance;
    }

    public boolean checkAuth(CharSequence passSequence, Context context) {
        Timber.d("timber: checkAuth: ");
        String tempPass = passSequence.toString();
        if (!previousTry.equals(tempPass)) {
            int failCount = ltd.grunt.brainwallet.tools.security.BRKeyStore.getFailCount(context);
            ltd.grunt.brainwallet.tools.security.BRKeyStore.putFailCount(failCount + 1, context);
        }
        previousTry = tempPass;

        String pass = ltd.grunt.brainwallet.tools.security.BRKeyStore.getPinCode(context);
        boolean match = pass != null && tempPass.equals(pass);
        if (!match) {
            if (ltd.grunt.brainwallet.tools.security.BRKeyStore.getFailCount(context) >= 3) {
                setWalletDisabled((Activity) context);
            }
        }

        return match;
    }

    //when pin auth success
    public void authSuccess(final Context app) {
        //put the new total limit in 3 seconds, leave some time for the core to register any new tx
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Timber.e(e);
                }
                AuthManager.getInstance().setTotalLimit(app, BRWalletManager.getInstance().getTotalSent()
                        + ltd.grunt.brainwallet.tools.security.BRKeyStore.getSpendLimit(app));
            }
        });

        ltd.grunt.brainwallet.tools.security.BRKeyStore.putFailCount(0, app);
        ltd.grunt.brainwallet.tools.security.BRKeyStore.putLastPinUsedTime(System.currentTimeMillis(), app);
    }

    public void authFail(Context app) {
        //TODO: Check if this cruft from the BRD implementation
    }

    public boolean isWalletDisabled(Activity app) {
        int failCount = ltd.grunt.brainwallet.tools.security.BRKeyStore.getFailCount(app);
        return failCount >= 3 && disabledUntil(app) > BRSharedPrefs.getSecureTime(app);
    }

    public long disabledUntil(Activity app) {
        int failCount = ltd.grunt.brainwallet.tools.security.BRKeyStore.getFailCount(app);
        long failTimestamp = ltd.grunt.brainwallet.tools.security.BRKeyStore.getFailTimeStamp(app);
        double pow = Math.pow(6, failCount - 3) * 60;
        return (long) ((failTimestamp + pow * 1000));
    }

    public void setWalletDisabled(Activity app) {
        if (!(app instanceof DisabledActivity))
            ActivityUTILS.showWalletDisabled(app);
    }

    public void setPinCode(String pass, Activity context) {
        ltd.grunt.brainwallet.tools.security.BRKeyStore.putFailCount(0, context);
        ltd.grunt.brainwallet.tools.security.BRKeyStore.putPinCode(pass, context);
        ltd.grunt.brainwallet.tools.security.BRKeyStore.putLastPinUsedTime(System.currentTimeMillis(), context);
        setSpendingLimitIfNotSet(context);
    }

    /**
     * Returns the total current limit that cannot be surpass without a pin
     */
    public long getTotalLimit(Context activity) {
        return ltd.grunt.brainwallet.tools.security.BRKeyStore.getTotalLimit(activity);
    }

    /**
     * Sets the total current limit that cannot be surpass without a pin
     */
    public void setTotalLimit(Context activity, long limit) {
        ltd.grunt.brainwallet.tools.security.BRKeyStore.putTotalLimit(limit, activity);
    }

    private void setSpendingLimitIfNotSet(final Activity activity) {
        if (activity == null) return;
        long limit = AuthManager.getInstance().getTotalLimit(activity);
        if (limit == 0) {
            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    long totalSpent = BRWalletManager.getInstance().getTotalSent();
                    long totalLimit = totalSpent + BRKeyStore.getSpendLimit(activity);
                    setTotalLimit(activity, totalLimit);
                }
            });
        }
    }

    public void updateDots(Context context, int pinLimit, String pin, View dot1, View dot2, View dot3, View dot4, View dot5, View dot6, int emptyPinRes, final OnPinSuccess onPinSuccess) {
        if (dot1 == null || context == null) return;
        int selectedDots = pin.length();

        if (pinLimit == 6) {
            dot6.setVisibility(View.VISIBLE);
            dot1.setVisibility(View.VISIBLE);
            dot1.setBackground(context.getDrawable(selectedDots <= 0 ? emptyPinRes : R.drawable.ic_pin_dot_black));
            selectedDots--;
        } else {
            dot6.setVisibility(View.GONE);
            dot1.setVisibility(View.GONE);
        }

        dot2.setBackground(context.getDrawable(selectedDots <= 0 ? emptyPinRes : R.drawable.ic_pin_dot_black));
        selectedDots--;
        dot3.setBackground(context.getDrawable(selectedDots <= 0 ? emptyPinRes : R.drawable.ic_pin_dot_black));
        selectedDots--;
        dot4.setBackground(context.getDrawable(selectedDots <= 0 ? emptyPinRes : R.drawable.ic_pin_dot_black));
        selectedDots--;
        dot5.setBackground(context.getDrawable(selectedDots <= 0 ? emptyPinRes : R.drawable.ic_pin_dot_black));
        if (pinLimit == 6) {
            selectedDots--;
            dot6.setBackground(context.getDrawable(selectedDots <= 0 ? emptyPinRes : R.drawable.ic_pin_dot_black));
        }

        if (pin.length() == pinLimit) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onPinSuccess.onSuccess();
                }
            }, 100);
        }
    }

    public interface OnPinSuccess {
        void onSuccess();
    }
}
