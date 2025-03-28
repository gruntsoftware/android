package com.brainwallet.presenter.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.brainwallet.R;
import com.brainwallet.navigation.Route;
import com.brainwallet.presenter.activities.util.BRActivity;
import com.brainwallet.presenter.customviews.BRKeyboard;
import com.brainwallet.data.model.CurrencyEntity;
import com.brainwallet.tools.animation.BRAnimator;
import com.brainwallet.tools.animation.SpringAnimator;
import com.brainwallet.tools.manager.AnalyticsManager;
import com.brainwallet.tools.manager.BRSharedPrefs;
import com.brainwallet.tools.security.AuthManager;
import com.brainwallet.tools.security.BRKeyStore;
import com.brainwallet.tools.sqlite.CurrencyDataSource;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.tools.util.BRConstants;
import com.brainwallet.tools.util.BRCurrency;
import com.brainwallet.ui.BrainwalletActivity;
import com.brainwallet.wallet.BRWalletManager;

import java.math.BigDecimal;

import timber.log.Timber;

@Deprecated
public class LoginActivity extends BRActivity {
    private BRKeyboard keyboard;
    private LinearLayout pinLayout;
    private View dot1;
    private View dot2;
    private View dot3;
    private View dot4;
    private View dot5;
    private View dot6;
    private StringBuilder pin = new StringBuilder();
    private int pinLimit = 6;
    private static LoginActivity app;

    private ImageView unlockedImage;
    private TextView unlockedText;
    private TextView enterPinLabel;
    private TextView versionText;
    private ViewGroup ltcPriceConstraintLayout;

    private TextView ltcPriceTextView;
    private TextView ltcPriceDescTextView; 

    public static boolean appVisible = false;
    private boolean inputAllowed = true;

    public static LoginActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin); 
        View parentLayout = findViewById(android.R.id.content);
        String pin = BRKeyStore.getPinCode(this);
        if (pin.isEmpty() || (pin.length() != 4 && pin.length() != 2)) {
            //using setpasscode compose
            Intent intent = BrainwalletActivity.createIntent(this, new Route.SetPasscode());
            intent.putExtra("noPin", true);
            startActivity(intent);
            if (!LoginActivity.this.isDestroyed()) finish();
            return;
        }
        if (BRKeyStore.getPinCode(this).length() == 4) pinLimit = 4;

        keyboard = findViewById(R.id.brkeyboard);
        pinLayout = findViewById(R.id.pinLayout);
        versionText = findViewById(R.id.version_text);

        ltcPriceTextView = findViewById(R.id.ltcPriceTextView);
        ltcPriceDescTextView = findViewById(R.id.ltcPriceDescTextView);

        unlockedImage = findViewById(R.id.unlocked_image);
        unlockedText = findViewById(R.id.unlocked_text);
        enterPinLabel = findViewById(R.id.enter_pin_label);
        ltcPriceConstraintLayout = findViewById(R.id.ltcPriceConstraintLayout);

        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);

        keyboard.addOnInsertListener(key -> handleClick(key));
        keyboard.setBRButtonTextColor(R.color.white);
        keyboard.setShowDot(false);

        keyboard.setCustomButtonBackgroundColor(10, getColor(android.R.color.transparent));
        keyboard.setDeleteImage(getDrawable(R.drawable.ic_delete_white));
        versionText.setText(BRConstants.APP_VERSION_NAME_CODE);

// DEV:  Keeping for reference when REFACTORING
//        findViewById(R.id.scanQRCodeImgBut).setOnClickListener(v -> {
//            if (!BRAnimator.isClickAllowed()) return;
//            try {
//                // Check if the camera permission is granted
//                if (ContextCompat.checkSelfPermission(app,
//                        Manifest.permission.CAMERA)
//                        != PackageManager.PERMISSION_GRANTED) {
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(app,
//                            Manifest.permission.CAMERA)) {
//                        BRDialog.showCustomDialog(app, getString(R.string.Send_cameraUnavailabeTitle_android),
//                                getString(R.string.Send_cameraUnavailabeMessage_android), getString(R.string.AccessibilityLabels_close), null, brDialogView -> brDialogView.dismiss(), null, null, 0);
//                    } else {
//                        ActivityCompat.requestPermissions(app,
//                                new String[]{Manifest.permission.CAMERA},
//                                BRConstants.CAMERA_REQUEST_ID);
//                    }
//                } else {
//                    Intent intent = new Intent(app, ScanQRActivity.class);
//                    app.startActivityForResult(intent, SCANNER_REQUEST);
//                    app.overridePendingTransition(R.anim.fade_up, 0);
//                }
//            } catch (Exception e) {
//                Timber.e(e);
//            }
//        });

        setCurrentLtcPrice();
    }

    private void setCurrentLtcPrice() {
        String iso = BRSharedPrefs.getIsoSymbol(this);

        String formattedCurrency = null;
        CurrencyEntity currency = CurrencyDataSource.getInstance(this).getCurrencyByIso(iso);
        if (currency != null) {
            final BigDecimal roundedPriceAmount = new BigDecimal(currency.rate).multiply(new BigDecimal(100))
                    .divide(new BigDecimal(100), 2, BRConstants.ROUNDING_MODE);
            formattedCurrency = BRCurrency.getFormattedCurrencyString(this, iso, roundedPriceAmount);
        } else {
            Timber.w("The currency related to %s is NULL", iso);
        }

        if (formattedCurrency != null) {
            ltcPriceTextView.setText(getString(R.string.Login_ltcPrice, formattedCurrency));
            ltcPriceDescTextView.setText(getString(R.string.Login_currentLtcPrice, iso));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDots();

        appVisible = true;
        app = this;
        inputAllowed = true;
        if (!BRWalletManager.getInstance().isCreated()) {
            BRExecutor.getInstance().forBackgroundTasks().execute(() -> BRWalletManager.getInstance().initWallet(LoginActivity.this));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    private void handleClick(String key) {
        if (!inputAllowed) {
            Timber.d("timber: handleClick: input not allowed");
            return;
        }
        if (key == null) {
            Timber.d("timber: handleClick: key is null! ");
            return;
        }

        if (key.isEmpty()) {
            handleDeleteClick();
        } else if (Character.isDigit(key.charAt(0))) {
            handleDigitClick(Integer.parseInt(key.substring(0, 1)));
        } else {
            Timber.d("timber: handleClick: oops: %s", key);
        }
    }


    private void handleDigitClick(Integer dig) {
        if (pin.length() < pinLimit)
            pin.append(dig);
        updateDots();
    }

    private void handleDeleteClick() {
        if (pin.length() > 0)
            pin.deleteCharAt(pin.length() - 1);
        updateDots();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
        } else {
            finishAffinity();
        }
    }

    private void unlockWallet() {
        pin = new StringBuilder();
        ltcPriceConstraintLayout.animate().translationY(-600).setInterpolator(new AccelerateInterpolator());
        pinLayout.animate().translationY(-2000).setInterpolator(new AccelerateInterpolator());
        enterPinLabel.animate().translationY(-1800).setInterpolator(new AccelerateInterpolator());
        keyboard.animate().translationY(2000).setInterpolator(new AccelerateInterpolator());
        unlockedImage.animate().alpha(1f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(LoginActivity.this, BreadActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_up, R.anim.fade_down);
                    if (!LoginActivity.this.isDestroyed()) {
                        LoginActivity.this.finish();

                    }
                }, 400);
            }
        });
        unlockedText.animate().alpha(1f);
    }

    private void showFailedToUnlock() {
        SpringAnimator.failShakeAnimation(LoginActivity.this, pinLayout);
        pin = new StringBuilder();
        new Handler().postDelayed(() -> {
            inputAllowed = true;
            updateDots();
        }, 1000);
    }

    private void updateDots() {
        AuthManager.getInstance().updateDots(this, pinLimit, pin.toString(), dot1, dot2, dot3, dot4, R.drawable.ic_pin_dot_white,
                () -> {
                    inputAllowed = false;
                    if (AuthManager.getInstance().checkAuth(pin.toString(), LoginActivity.this)) {
                        AuthManager.getInstance().authSuccess(LoginActivity.this);
                        unlockWallet();
                        AnalyticsManager.logCustomEvent(BRConstants._20200217_DUWB);
                        AnalyticsManager.logCustomEvent(BRConstants._20200217_DUWB);

                    } else {
                        AuthManager.getInstance().authFail(LoginActivity.this);
                        showFailedToUnlock();
                    }
                });
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
                    Timber.i("timber: onRequestPermissionsResult: permission isn't granted for: %s", requestCode);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
