package com.brainwallet.presenter.activities.util;

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Looper;

import com.brainwallet.R;
import com.brainwallet.presenter.activities.DisabledActivity;
import com.brainwallet.presenter.activities.SetPinActivity;
import com.brainwallet.presenter.activities.InputWordsActivity;


import java.util.List;

import timber.log.Timber;

public class ActivityUTILS {

    //return true if the app does need to show the disabled wallet screen
    public static boolean isAppSafe(Activity app) {
        return app instanceof SetPinActivity || app instanceof InputWordsActivity;
    }

    public static void showWalletDisabled(Activity app) {
        Intent intent = new Intent(app, DisabledActivity.class);
        app.startActivity(intent);
        app.overridePendingTransition(R.anim.fade_up, R.anim.fade_down);
        Timber.d("timber: showWalletDisabled: %s", app.getClass().getName());
    }

    public static boolean isLast(Activity app) {
        ActivityManager mngr = (ActivityManager) app.getSystemService(ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> taskList = mngr.getRunningTasks(10);

        if (taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(app.getClass().getName())) {
            return true;
        }
        return false;
    }

    public static boolean isMainThread() {
        boolean isMain = Looper.myLooper() == Looper.getMainLooper();
        if (isMain) {
            Timber.d("timber: IS MAIN UI THREAD!");
        }
        return isMain;
    }
}
