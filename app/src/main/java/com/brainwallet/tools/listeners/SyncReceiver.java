package com.brainwallet.tools.listeners;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.brainwallet.tools.util.BRConstants;
import com.brainwallet.wallet.BRWalletManager;

public class SyncReceiver extends IntentService {
    public final String TAG = SyncReceiver.class.getSimpleName();
    public final static String SYNC_RECEIVER = "SYNC_RECEIVER";
    public static Context app;

    //    private Calendar c = Calendar.getInstance();
    static {
        System.loadLibrary(BRConstants.NATIVE_LIB_NAME);
    }

    public SyncReceiver() {
        super("SyncReceiver");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        if (workIntent != null)
            if (SYNC_RECEIVER.equals(workIntent.getAction())) {
                app = getApplication();
//                BRToast.showCustomToast(getApplication(), "Starting background syncing...", BreadActivity.screenParametersPoint.y / 2, Toast.LENGTH_LONG, 0);
                BRWalletManager.getInstance().initWallet(app);
            }
    }
}
