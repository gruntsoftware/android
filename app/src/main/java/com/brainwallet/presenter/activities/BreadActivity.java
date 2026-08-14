package com.brainwallet.presenter.activities;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.transition.ChangeBounds;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.brainwallet.R;
import com.brainwallet.navigation.LegacyNavigation;
import com.brainwallet.navigation.Route;
import com.brainwallet.presenter.activities.settings.SyncBlockchainActivity;
import com.brainwallet.presenter.activities.util.BRActivity;
import com.brainwallet.presenter.customviews.BRNotificationBar;
import com.brainwallet.presenter.history.HistoryFragment;
import com.brainwallet.tools.animation.TextSizeTransition;
import com.brainwallet.tools.manager.BRSharedPrefs;
import com.brainwallet.tools.manager.InternetManager;
import com.brainwallet.tools.manager.sync.SyncManager;
import com.brainwallet.tools.security.BitcoinUrlHandler;
import com.brainwallet.tools.security.PostAuth;
import com.brainwallet.tools.sqlite.TransactionDataSource;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.tools.util.BRCurrency;
import com.brainwallet.tools.util.BRExchange;
import com.brainwallet.tools.util.ExtensionKt;
import com.brainwallet.tools.util.Utils;
import com.brainwallet.ui.BrainwalletActivity;
import com.brainwallet.ui.screens.settings.SettingsViewModel;
import com.brainwallet.ui.screens.settings.settingsrows.HomeSettingDrawerComposeView;
import com.brainwallet.ui.bentosections.buyreceivebento.receive.ReceiveDialogFragment;
import com.brainwallet.util.PermissionUtil;
import com.brainwallet.wallet.BRPeerManager;
import com.brainwallet.wallet.BRWalletManager;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import java.math.BigDecimal;

import timber.log.Timber;

public class BreadActivity extends BRActivity implements BRWalletManager.OnBalanceChanged,
        TransactionDataSource.OnTxAddedListener, InternetManager.ConnectionReceiverListener {

    public static final Point screenParametersPoint = new Point();

    private InternetManager mConnectionReceiver;
    private TextView equals;
    private TextView balanceTxtV;

    public static boolean appVisible = false;
    public ViewFlipper barFlipper;
    private ConstraintLayout toolBarConstraintLayout;
    private boolean uiIsDone;

    private static BreadActivity app;

    private Handler mHandler = new Handler();
    private NavigationView navigationDrawer;
    private HomeSettingDrawerComposeView homeSettingDrawerComposeView;

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
            BitcoinUrlHandler.processRequest(this, str);
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
            BRExecutor.getInstance().forBackgroundTasks().execute(() -> BRWalletManager.getInstance().initWallet(BreadActivity.this));
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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mConnectionReceiver);
    }

    @Override
    public void onBalanceChanged(final long balance) {

    }

    @Override
    public void onTxAdded() {
        BRWalletManager.getInstance().refreshBalance(BreadActivity.this);
    }

    @Override
    public void onConnectionChanged(boolean isConnected) {

        Context thisContext = BreadActivity.this;
        Context app = getApplicationContext();

    }
}
