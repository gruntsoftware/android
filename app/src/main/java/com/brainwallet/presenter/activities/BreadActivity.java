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
import android.telephony.TelephonyManager;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
import com.brainwallet.presenter.fragments.BuyTabFragment;
import com.brainwallet.presenter.fragments.FragmentMoonpay;
import com.brainwallet.presenter.history.HistoryFragment;
import com.brainwallet.tools.animation.BRAnimator;
import com.brainwallet.tools.animation.TextSizeTransition;
import com.brainwallet.tools.manager.AnalyticsManager;
import com.brainwallet.tools.manager.BRSharedPrefs;
import com.brainwallet.tools.manager.InternetManager;
import com.brainwallet.tools.manager.SyncManager;
import com.brainwallet.tools.security.BitcoinUrlHandler;
import com.brainwallet.tools.security.PostAuth;
import com.brainwallet.tools.sqlite.TransactionDataSource;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.tools.util.BRConstants;
import com.brainwallet.tools.util.BRCurrency;
import com.brainwallet.tools.util.BRExchange;
import com.brainwallet.tools.util.ExtensionKt;
import com.brainwallet.tools.util.Utils;
import com.brainwallet.ui.BrainwalletActivity;
import com.brainwallet.ui.screens.home.SettingsViewModel;
import com.brainwallet.ui.screens.home.composable.HomeSettingDrawerComposeView;
import com.brainwallet.ui.screens.home.receive.ReceiveDialogFragment;
import com.brainwallet.ui.screens.home.receive.ReceiveDialogKt;
import com.brainwallet.util.PermissionUtil;
import com.brainwallet.wallet.BRPeerManager;
import com.brainwallet.wallet.BRWalletManager;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import java.math.BigDecimal;

import timber.log.Timber;

public class BreadActivity extends BRActivity implements BRWalletManager.OnBalanceChanged, BRSharedPrefs.OnIsoChangedListener,
        TransactionDataSource.OnTxAddedListener, InternetManager.ConnectionReceiverListener {

    public static final Point screenParametersPoint = new Point();
    private static final float PRIMARY_TEXT_SIZE = 24f;
    private static final float SECONDARY_TEXT_SIZE = 12.8f;
    private int mSelectedBottomNavItem = -1;

    private InternetManager mConnectionReceiver;
    private Button primaryPrice;
    private Button secondaryPrice;
    private TextView equals;
    private ImageButton menuBut;
    private TextView balanceTxtV;

    public static boolean appVisible = false;
    public ViewFlipper barFlipper;
    private ConstraintLayout toolBarConstraintLayout;
    private boolean uiIsDone;

    private static BreadActivity app;
    private BottomNavigationView bottomNav;

    private Handler mHandler = new Handler();
    private NavigationView navigationDrawer;
    private DrawerLayout drawerLayout;
    private HomeSettingDrawerComposeView homeSettingDrawerComposeView;

    public static BreadActivity getApp() {
        return app;
    }

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(app, R.string.permission_notification_granted, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bread);
        AnalyticsManager.logCustomEvent(BRConstants._HOME_OPEN);

        app = this;
        getWindowManager().getDefaultDisplay().getSize(screenParametersPoint);

        initializeViews();
        setPriceTags(BRSharedPrefs.getPreferredLTC(BreadActivity.this), false);
        setListeners();
        setUpBarFlipper();
        checkTransactionDatabase();

        primaryPrice.setTextSize(PRIMARY_TEXT_SIZE);
        secondaryPrice.setTextSize(SECONDARY_TEXT_SIZE);

        finishActivities(SetPinActivity.introSetPitActivity, ReEnterPinActivity.reEnterPinActivity);

        onConnectionChanged(InternetManager.getInstance().isConnected(this));

        updateUI();
        bottomNav.setSelectedItemId(R.id.nav_history);

        setupNotificationPermission();
        showInAppReviewDialogIfNeeded();
    }

    private void setupNotificationPermission() {
        //https://developer.android.com/develop/ui/views/notifications/notification-permission
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (!PermissionUtil.hasPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_info)
                        .setMessage(R.string.please_grant_notification_permission)
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            PermissionUtil.requestPermission(requestNotificationPermissionLauncher, Manifest.permission.POST_NOTIFICATIONS);
                        })
                        .show();
            } else {
                PermissionUtil.requestPermission(requestNotificationPermissionLauncher, Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void finishActivities(BRActivity... activities) {
        for (BRActivity activity : activities) {
            if (activity != null) activity.finish();
        }
    }

    private void showInAppReviewDialogIfNeeded() {
        if (!BRSharedPrefs.isInAppReviewDone(this) && BRSharedPrefs.getSendTransactionCount(this) > 2) {
            ReviewManager manager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                AnalyticsManager.logCustomEvent(BRConstants._20241006_DRR);
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    Task<Void> flow = manager.launchReviewFlow(BreadActivity.this, reviewInfo);
                    flow.addOnCompleteListener(task1 -> {
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                        Timber.i("timber: In-app LaunchReviewFlow completed successful (%s)", task1.isSuccessful());
                        if (task1.isSuccessful()) {
                            BRSharedPrefs.inAppReviewDone(BreadActivity.this);
                            AnalyticsManager.logCustomEvent(BRConstants._20241006_UCR);
                        }
                    });
                } else {
                    Timber.e(task.getException(), "In-app request review flow failed");
                }
            });
        }
    }

    private void addObservers() {
        BRWalletManager.getInstance().addBalanceChangedListener(this);
        BRSharedPrefs.addIsoChangedListener(this);
    }

    private void removeObservers() {
        BRWalletManager.getInstance().removeListener(this);
        BRSharedPrefs.removeListener(this);
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

    private void setListeners() {
        bottomNav.setOnNavigationItemSelectedListener(item -> handleNavigationItemSelected(item.getItemId()));

        primaryPrice.setOnClickListener(v -> swap());
        secondaryPrice.setOnClickListener(v -> swap());
        menuBut.setOnClickListener(v -> {
            if (BRAnimator.isClickAllowed()) {
                drawerLayout.open();
            }
        });
    }

    public boolean handleNavigationItemSelected(int menuItemId) {
        if (mSelectedBottomNavItem == menuItemId) return true;
        mSelectedBottomNavItem = menuItemId;

        //TODO: revisit
        // we are using compose, that's why commented, will remove it after fully migrated to compose
        if (menuItemId == R.id.nav_history) {
            ExtensionKt.replaceFragment(BreadActivity.this, new HistoryFragment(), false, R.id.fragment_container);
        } else if (menuItemId == R.id.nav_send) {
            if (BRAnimator.isClickAllowed()) {
                BRAnimator.showSendFragment(BreadActivity.this, null);
            }
            mSelectedBottomNavItem = 0;
        } else if (menuItemId == R.id.nav_receive) {
            if (BRAnimator.isClickAllowed()) {
//                BRAnimator.showReceiveFragment(BreadActivity.this, true);
                //todo
                ReceiveDialogFragment.show(getSupportFragmentManager());
            }
            mSelectedBottomNavItem = 0;
        }
//        else if (menuItemId == R.id.nav_buy) {
//            if (BRAnimator.isClickAllowed()) {
////                BRAnimator.showMoonpayFragment(BreadActivity.this);
//            }
//
//
//
//
//            mSelectedBottomNavItem = 0;
//        }
        return true;
    }

    private void swap() {
        if (!BRAnimator.isClickAllowed()) return;
        boolean b = !BRSharedPrefs.getPreferredLTC(this);
        setPriceTags(b, true);
        BRSharedPrefs.putPreferredLTC(this, b);
        BRSharedPrefs.notifyIsoChanged("");
    }

    private void setPriceTags(boolean ltcPreferred, boolean animate) {
        ConstraintSet set = new ConstraintSet();
        set.clone(toolBarConstraintLayout);

        if (animate) {
            TransitionSet textSizeTransition = new TransitionSet()
                    .setOrdering(TransitionSet.ORDERING_TOGETHER)
                    .addTransition(new TextSizeTransition())
                    .addTransition(new ChangeBounds());

            TransitionSet transition = new TransitionSet()
                    .setOrdering(TransitionSet.ORDERING_SEQUENTIAL)
                    .addTransition(new Fade(Fade.OUT))
                    .addTransition(textSizeTransition)
                    .addTransition(new Fade(Fade.IN));
            TransitionManager.beginDelayedTransition(toolBarConstraintLayout, transition);
        }

        primaryPrice.setTextSize(ltcPreferred ? PRIMARY_TEXT_SIZE : SECONDARY_TEXT_SIZE);
        secondaryPrice.setTextSize(ltcPreferred ? SECONDARY_TEXT_SIZE : PRIMARY_TEXT_SIZE);

        int[] ids = {primaryPrice.getId(), secondaryPrice.getId(), equals.getId()};
        // Clear views constraints
        for (int id : ids) {
            set.clear(id);
            set.constrainWidth(id, ConstraintSet.WRAP_CONTENT);
            set.constrainHeight(id, ConstraintSet.WRAP_CONTENT);
        }

        int dp16 = Utils.getPixelsFromDps(this, 16);
        int dp8 = Utils.getPixelsFromDps(this, 4);

        int leftId = ltcPreferred ? primaryPrice.getId() : secondaryPrice.getId();
        int rightId = ltcPreferred ? secondaryPrice.getId() : primaryPrice.getId();

        int[] chainViews = {leftId, equals.getId(), rightId};

        set.connect(leftId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dp16);
        set.connect(leftId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(leftId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, dp16);
        set.setVerticalBias(leftId, 1.0f);

        set.connect(rightId, ConstraintSet.BASELINE, leftId, ConstraintSet.BASELINE);
        set.connect(equals.getId(), ConstraintSet.BASELINE, leftId, ConstraintSet.BASELINE);

        set.connect(equals.getId(), ConstraintSet.START, leftId, ConstraintSet.END, dp8);
        set.connect(equals.getId(), ConstraintSet.END, rightId, ConstraintSet.START, dp8);

        set.createHorizontalChain(leftId, ConstraintSet.LEFT, equals.getId(), ConstraintSet.RIGHT, chainViews, null, ConstraintSet.CHAIN_PACKED);

        // Apply the changes
        set.applyTo(toolBarConstraintLayout);

        mHandler.postDelayed(() -> updateUI(), toolBarConstraintLayout.getLayoutTransition().getDuration(LayoutTransition.CHANGING));
    }

    private void checkTransactionDatabase() {

    }

    private void setUpBarFlipper() {
        barFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.flipper_enter));
        barFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.flipper_exit));
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

        setupNetworking();

        if (!BRWalletManager.getInstance().isCreated()) {
            BRExecutor.getInstance().forBackgroundTasks().execute(() -> BRWalletManager.getInstance().initWallet(BreadActivity.this));
        }
        mHandler.postDelayed(() -> updateUI(), 1000);

        BRWalletManager.getInstance().refreshBalance(this);
    }

    private void setupNetworking() {
        if (mConnectionReceiver == null) mConnectionReceiver = InternetManager.getInstance();
        IntentFilter mNetworkStateFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectionReceiver, mNetworkStateFilter);
        InternetManager.addConnectionListener(this);
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

    private void initializeViews() {
        menuBut = findViewById(R.id.menuBut);

        navigationDrawer = findViewById(R.id.navigationDrawer);
        drawerLayout = findViewById(R.id.drawerLayout);
        homeSettingDrawerComposeView = findViewById(R.id.homeDrawerComposeView);
        homeSettingDrawerComposeView.observeBus(message -> {
            drawerLayout.close();
            if (SettingsViewModel.LEGACY_EFFECT_ON_LOCK.equals(message.getMessage())) {
                LegacyNavigation.startBreadActivity(this, true);
            } else if (SettingsViewModel.LEGACY_EFFECT_ON_TOGGLE_DARK_MODE.equals(message.getMessage())) {
                LegacyNavigation.restartBreadActivity(this);
            } else if (SettingsViewModel.LEGACY_EFFECT_ON_SEC_UPDATE_PIN.equals(message.getMessage())) {
                Intent intent = BrainwalletActivity.createIntent(this, new Route.UnLock(true));
                intent.putExtra("noPin", true);
                startActivity(intent);
            } else if (SettingsViewModel.LEGACY_EFFECT_ON_SEED_PHRASE.equals(message.getMessage())) {
                PostAuth.getInstance().onPhraseCheckAuth(this, true);
            } else if (SettingsViewModel.LEGACY_EFFECT_ON_SHARE_ANALYTICS_DATA_TOGGLE.equals(message.getMessage())) {
                boolean currentShareAnalyticsDataEnabled = BRSharedPrefs.getShareData(this);
                BRSharedPrefs.putShareData(this, !currentShareAnalyticsDataEnabled);
            } else if (SettingsViewModel.LEGACY_EFFECT_ON_SYNC.equals(message.getMessage())) {
                Intent intent = new Intent(this, SyncBlockchainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
            return null;
        }); //since we are still using this BreadActivity, need to observe EventBus e.g. lock from [HomeSettingDrawerSheet]

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.getMenu().clear();
        bottomNav.inflateMenu(R.menu.bottom_nav_menu);
        balanceTxtV = findViewById(R.id.balanceTxtV);

        primaryPrice = findViewById(R.id.primary_price);
        secondaryPrice = findViewById(R.id.secondary_price);
        equals = findViewById(R.id.equals);
        toolBarConstraintLayout = findViewById(R.id.bread_toolbar);

        barFlipper = findViewById(R.id.tool_bar_flipper);

        final ViewTreeObserver observer = primaryPrice.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (observer.isAlive()) {
                    observer.removeOnGlobalLayoutListener(this);
                }
                if (uiIsDone) return;
                uiIsDone = true;
                setPriceTags(BRSharedPrefs.getPreferredLTC(BreadActivity.this), false);
            }
        });

        balanceTxtV.append(":");
    }

    @Override
    public void onBalanceChanged(final long balance) {
        updateUI();
    }

    public void updateUI() {
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(() -> {
            Thread.currentThread().setName(Thread.currentThread().getName() + ":updateUI");
            //sleep a little in order to make sure all the commits are finished (like SharePreferences commits)
            String iso = BRSharedPrefs.getIsoSymbol(BreadActivity.this);

            //current amount in litoshis
            final BigDecimal amount = new BigDecimal(BRSharedPrefs.getCatchedBalance(BreadActivity.this));

            //amount in LTC units
            BigDecimal btcAmount = BRExchange.getLitecoinForLitoshis(BreadActivity.this, amount);
            final String formattedBTCAmount = BRCurrency.getFormattedCurrencyString(BreadActivity.this, "LTC", btcAmount);

            final BigDecimal curAmount = BRExchange.getAmountFromLitoshis(BreadActivity.this, iso, amount);
            final String formattedCurAmount = BRCurrency.getFormattedCurrencyString(BreadActivity.this, iso, curAmount);
            runOnUiThread(() -> {
                primaryPrice.setText(formattedBTCAmount);
                secondaryPrice.setText(String.format("%s", formattedCurAmount));
            });
        });
    }

    @Override
    public void onIsoChanged(String iso) {
        updateUI();
    }

    @Override
    public void onTxAdded() {
        BRWalletManager.getInstance().refreshBalance(BreadActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case BRConstants.CAMERA_REQUEST_ID: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    BRAnimator.openScanner(this, BRConstants.SCANNER_REQUEST);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnectionChanged(boolean isConnected) {

        Context thisContext = BreadActivity.this;
        Context app = getApplicationContext();
        if (isConnected) {
            if (barFlipper != null) {
                if (barFlipper.getDisplayedChild() == 1) {
                    removeNotificationBar();
                }
            }
            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(() -> {
                final double progress = BRPeerManager.syncProgress(BRSharedPrefs.getStartHeight(thisContext));
                if (progress > 0 && progress < 1) {
                    SyncManager.getInstance().startSyncingProgressThread(app);
                }
            });
        } else {
            if (barFlipper != null) {
                addNotificationBar();
            }
            SyncManager.getInstance().stopSyncingProgressThread(app);
        }
    }

    public void removeNotificationBar() {
        if (barFlipper.getChildCount() == 1) return;
        barFlipper.removeViewAt(1);
        barFlipper.setDisplayedChild(0);
    }

    public void addNotificationBar() {
        if (barFlipper.getChildCount() == 2) return;
        BRNotificationBar view = new BRNotificationBar(this);
        barFlipper.addView(view);
        barFlipper.setDisplayedChild(1);
    }
}