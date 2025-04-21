package com.brainwallet.tools.manager;

import static com.brainwallet.tools.manager.BRSharedPrefs.putSyncMetadata;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.brainwallet.tools.listeners.SyncReceiver;
import com.brainwallet.tools.util.BRConstants;
import com.brainwallet.tools.util.Utils;
import com.brainwallet.R;
import com.brainwallet.presenter.activities.BreadActivity;
import com.brainwallet.wallet.BRPeerManager;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class SyncManager {
    private static SyncManager instance;
    private static final long SYNC_PERIOD = TimeUnit.HOURS.toMillis(24);
    private static SyncProgressTask syncTask;
    public boolean running;

    public static SyncManager getInstance() {
        if (instance == null) instance = new SyncManager();
        return instance;
    }

    private SyncManager() {
    }

    public synchronized void startSyncingProgressThread(Context app) {
        try {
            if (syncTask != null) {
                if (running) {
                    updateStartSyncData(app);
                    return;
                }
                syncTask.interrupt();
                syncTask = null;
            }
            syncTask = new SyncProgressTask();
            syncTask.start();
            updateStartSyncData(app);
        } catch (IllegalThreadStateException ex) {
            Timber.e(ex);
        }
    }

    private synchronized void updateStartSyncData(Context app) {
        final double progress = BRPeerManager.syncProgress(BRSharedPrefs.getStartHeight(app));
    }

    private synchronized void markFinishedSyncData(Context app) {
        Timber.d("timber: || SYNC ELAPSE markFinish threadname:%s", Thread.currentThread().getName());
        final double progress = BRPeerManager.syncProgress(BRSharedPrefs.getStartHeight(app));
    }

    public synchronized void stopSyncingProgressThread(Context app) {

        if (app == null) {
            Timber.i("timber: || stopSyncingProgressThread: ctx is null");
            return;
        }
        try {
            if (syncTask != null) {
                syncTask.interrupt();
                syncTask = null;
                markFinishedSyncData(app);
            }
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    private void createAlarm(Context app, long time) {
        //Add another flag
        AlarmManager alarmManager = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(app, SyncReceiver.class);
        intent.setAction(SyncReceiver.SYNC_RECEIVER);//my custom string action name
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getService(app, 1001, intent, flags);
        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, time, time + TimeUnit.MINUTES.toMillis(1), pendingIntent);//first start will start asap
    }

    public synchronized void updateAlarms(Context app) {
        createAlarm(app, System.currentTimeMillis() + SYNC_PERIOD);
    }


    private class SyncProgressTask extends Thread {
        public double progressStatus = 0;
        private BreadActivity app;

        public SyncProgressTask() {
            progressStatus = 0;
        }

        @Override
        public void run() {
            if (running) return;
            try {
                app = BreadActivity.getApp();
                progressStatus = 0;
                running = true;
                long runTimeStamp = System.currentTimeMillis();
                Timber.d("timber: run: starting: %s date: %d", progressStatus, runTimeStamp);
                ///Set StartSync
                BRSharedPrefs.putStartSyncTimestamp(app, runTimeStamp);

                if (app != null) {
                    final long lastBlockTimeStamp = BRPeerManager.getInstance().getLastBlockTimestamp() * 1000;
                    app.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (TxManager.getInstance().syncingProgressViewHolder != null)
                                TxManager.getInstance().syncingProgressViewHolder.progress.setProgress((int) (progressStatus * 100));
                            if (TxManager.getInstance().syncingProgressViewHolder != null) {
                                TxManager.getInstance().syncingProgressViewHolder.date.setText(Utils.formatTimeStamp(lastBlockTimeStamp, "MMM. dd, yyyy  ha"));
                                TxManager.getInstance().syncingProgressViewHolder.label.setText(BreadActivity.getApp().getString(R.string.SyncingView_header));
                            }
                        }
                    });
                }

                while (running) {
                    if (app != null) {
                        int startHeight = BRSharedPrefs.getStartHeight(app);
                        progressStatus = BRPeerManager.syncProgress(startHeight);
                        if (progressStatus == 1) {
                            running = false;
                            /// Record sync time
                            long startTimeStamp = BRSharedPrefs.getStartSyncTimestamp(app);
                            long endSyncTimeStamp = System.currentTimeMillis();
                            BRSharedPrefs.putEndSyncTimestamp(app, endSyncTimeStamp);

                            double syncDuration = (double) (endSyncTimeStamp - startTimeStamp) / 1_000.0 / 60.0;
                            /// only update if the sync duration is longer than 2 mins
                           if (syncDuration > 2.0) {
                                putSyncMetadata(app, startTimeStamp, endSyncTimeStamp);
                           }
                            continue;
                        }
                        final long lastBlockTimeStamp = BRPeerManager.getInstance().getLastBlockTimestamp() * 1000;
                        final int currentBlockHeight = BRPeerManager.getCurrentBlockHeight();
                        app.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (TxManager.getInstance().currentPrompt != PromptManager.PromptItem.SYNCING) {
                                    Timber.d("timber: run: currentPrompt != SYNCING, showPrompt(SYNCING) ....");
                                    TxManager.getInstance().showPrompt(app, PromptManager.PromptItem.SYNCING);
                                }
                                if (TxManager.getInstance().syncingProgressViewHolder != null) {
                                    TxManager.getInstance().syncingProgressViewHolder.progress.setProgress((int) (progressStatus * 100));
                                    TxManager.getInstance().syncingProgressViewHolder.date.setText(Utils.formatTimeStamp(lastBlockTimeStamp, "MMM. dd, yyyy  ha"));
                                    String progressString = String.format("%3.2f%%", progressStatus * 100);
                                    TxManager.getInstance().syncingProgressViewHolder.label.setText(String.format("%s %s - %d",BreadActivity.getApp().getString(R.string.SyncingView_header),progressString, currentBlockHeight));
                                }
                            }
                        });

                    } else {
                        app = BreadActivity.getApp();
                    }

                    ///DEV: kcw-grunt 26-10-24
                    /// DUMB sleep was slowing sync dramatically
                    /// Why is this here?
                    /// Reduced it from 500msec to 100msec until refactor
                    /// Poor control flow, loop should continue for the next task
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Timber.e(e, "timber:run: SyncManager.run Thread.sleep was Interrupted:%s", Thread.currentThread().getName());
                    }
                }
                Timber.d("timber: run: SyncProgress task finished:%s", Thread.currentThread().getName());
            } finally {
                running = false;
                progressStatus = 0;
                if (app != null)
                    app.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                         TxManager.getInstance().hidePrompt(app, PromptManager.PromptItem.SYNCING);
                        }
                    });
            }
        }
    }
}
