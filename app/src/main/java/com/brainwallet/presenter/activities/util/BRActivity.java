package com.brainwallet.presenter.activities.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.brainwallet.BrainwalletApp;
import com.brainwallet.data.repository.SettingRepository;
import com.brainwallet.di.Module;
import com.brainwallet.presenter.activities.DisabledActivity;
import com.brainwallet.presenter.activities.intro.RecoverActivity;
import com.brainwallet.presenter.activities.intro.WriteDownActivity;
import com.brainwallet.tools.animation.BRAnimator;
import com.brainwallet.tools.manager.InternetManager;
import com.brainwallet.tools.security.AuthManager;
import com.brainwallet.tools.security.BRKeyStore;
import com.brainwallet.tools.security.BitcoinUrlHandler;
import com.brainwallet.tools.security.PostAuth;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.tools.util.BRConstants;
import com.brainwallet.tools.util.ExtensionKt;
import com.brainwallet.ui.BrainwalletActivity;
import com.brainwallet.wallet.BRWalletManager;

import java.util.Locale;

import timber.log.Timber;

public class BRActivity extends AppCompatActivity {

    static {
        System.loadLibrary(BRConstants.NATIVE_LIB_NAME);
    }

    SettingRepository settingRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        settingRepository = BrainwalletApp.module.getSettingRepository();  //just inject here
        String languageCode = settingRepository.getCurrentLanguage().getCode();
        Locale.setDefault(settingRepository.getCurrentLanguage().toLocale());
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode));
        AppCompatDelegate.setDefaultNightMode(settingRepository.isDarkMode() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
    }

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(LocaleHelper.Companion.getInstance().setLocale(newBase));
//    }

    @Override
    protected void onStop() {
        super.onStop();
        BrainwalletApp.activityCounter.decrementAndGet();
        BrainwalletApp.onStop(this);
        BrainwalletApp.backgroundedTime = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        init(this);
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BRConstants.PAY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            PostAuth.getInstance().onPublishTxAuth(BRActivity.this, true);
                        }
                    });
                }
                break;
            case BRConstants.PAYMENT_PROTOCOL_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onPaymentProtocolRequest(this, true);
                }
                break;

            case BRConstants.CANARY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onCanaryCheck(this, true);
                } else {
                    finish();
                }
                break;

            case BRConstants.SHOW_PHRASE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onPhraseCheckAuth(this, true);
                }
                break;
            case BRConstants.PROVE_PHRASE_REQUEST:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onPhraseProveAuth(this, true);
                }
                break;
            case BRConstants.PUT_PHRASE_RECOVERY_WALLET_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onRecoverWalletAuth(this, true);
                } else {
                    finish();
                }
                break;

            case BRConstants.SCANNER_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String result = data.getStringExtra("result");
                            if (BitcoinUrlHandler.isBitcoinUrl(result))
                                BitcoinUrlHandler.processRequest(BRActivity.this, result);
                            else
                                Timber.i("timber: onActivityResult: not litecoin address NOR bitID");
                        }
                    }, 500);

                }
                break;

            case BRConstants.PUT_PHRASE_NEW_WALLET_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onCreateWalletAuth(this, true);
                } else {
                    Timber.d("timber: WARNING: resultCode != RESULT_OK");
                    BRWalletManager m = BRWalletManager.getInstance();
                    m.wipeWalletButKeystore(this);
                    finish();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static void init(Activity app) {
        InternetManager.getInstance();


        if (!(app instanceof RecoverActivity || app instanceof WriteDownActivity)) {
            BrainwalletApp.module.getApiManager().startTimer(app);
        }

        //show wallet locked if it is
        if (!ActivityUTILS.isAppSafe(app))
            if (AuthManager.getInstance().isWalletDisabled(app))
                AuthManager.getInstance().setWalletDisabled(app);

        BrainwalletApp.activityCounter.incrementAndGet();
        BrainwalletApp.setBreadContext(app);
        //lock wallet if 3 minutes passed (180 * 1000)
        if (BrainwalletApp.backgroundedTime != 0 && hasTimeElapsedSinceInBackground(180 * 1000) && !(app instanceof DisabledActivity)) {
            if (!BRKeyStore.getPinCode(app).isEmpty()) {
                BRAnimator.startBreadActivity(app, true);
            }
        }
        BrainwalletApp.backgroundedTime = System.currentTimeMillis();
    }

    private static boolean hasTimeElapsedSinceInBackground(long timeInMillis) {
        return System.currentTimeMillis() - BrainwalletApp.backgroundedTime >= timeInMillis;
    }
}
