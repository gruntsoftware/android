package com.brainwallet.presenter.activities;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;

import com.brainwallet.R;
import com.brainwallet.presenter.activities.util.BRActivity;
import com.brainwallet.tools.manager.InternetManager;
import com.brainwallet.tools.util.LitecoinURIHandler;
import com.brainwallet.tools.sqlite.TransactionDataSource;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.wallet.BRWalletManager;

@Deprecated
public class BreadActivity extends BRActivity implements BRWalletManager.OnBalanceChanged,
        TransactionDataSource.OnTxAddedListener, InternetManager.ConnectionReceiverListener {

    public static final Point screenParametersPoint = new Point();

    public static boolean appVisible = false;
    private static BreadActivity app;

    public static BreadActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bread);
        app = this;
        getWindowManager().getDefaultDisplay().getSize(screenParametersPoint);
        onConnectionChanged(InternetManager.getInstance().isConnected(this));
        // Handle a litecoin: deep link (e.g. a QR code scanned by another app) on a
        // cold start too, not just when this activity is already running (onNewIntent).
        setUrlHandler(getIntent());
    }

    private void addObservers() {
        BRWalletManager.getInstance().addBalanceChangedListener(this);
    }

    private void removeObservers() {
        BRWalletManager.getInstance().removeListener(this);
    }

    private void setUrlHandler(Intent intent) {
        Uri data = intent.getData();
        if (data == null) return;
        String scheme = data.getScheme();
        if (scheme != null && scheme.startsWith("litecoin")) {
            String str = intent.getDataString();
            LitecoinURIHandler.processRequest(this, str);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setUrlHandler(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        app = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
        addObservers();

        if (!BRWalletManager.getInstance().isCreated()) {
            BRExecutor.getInstance().forBackgroundTasks()
                .execute(() -> BRWalletManager.getInstance()
                    .initWallet(BreadActivity.this));
        }
        BRWalletManager.getInstance().refreshBalance(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
        removeObservers();
    }

    @Override
    public void onBalanceChanged(final long balance) {
        //no-op
    }

    @Override
    public void onTxAdded() {
        BRWalletManager.getInstance()
            .refreshBalance(BreadActivity.this);
    }

    @Override
    public void onConnectionChanged(boolean isConnected) {
        //no-op
    }
}
