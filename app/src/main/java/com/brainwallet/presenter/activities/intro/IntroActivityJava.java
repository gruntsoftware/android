
package com.brainwallet.presenter.activities.intro;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brainwallet.R;
import com.brainwallet.data.model.IntroLanguageResource;
import com.brainwallet.data.model.Language;
import com.brainwallet.presenter.activities.BreadActivity;
import com.brainwallet.presenter.activities.SetPinActivity;
import com.brainwallet.presenter.activities.util.BRActivity;
import com.brainwallet.tools.adapter.CountryLanguageAdapter;
import com.brainwallet.tools.animation.BRAnimator;
import com.brainwallet.tools.security.BRKeyStore;
import com.brainwallet.tools.security.PostAuth;
import com.brainwallet.tools.security.SmartValidator;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.tools.util.BRConstants;
import com.brainwallet.tools.util.LocaleHelper;
import com.brainwallet.tools.util.Utils;
import com.brainwallet.wallet.BRWalletManager;
import com.platform.APIClient;

import java.io.Serializable;
import java.util.Objects;

import timber.log.Timber;

public class IntroActivityJava extends BRActivity implements Serializable {
    public Button newWalletButton;
    public Button recoverWalletButton;
    public static IntroActivityJava introActivityJava;
    public static boolean appVisible = false;
    private static IntroActivityJava app;
    public CountryLanguageAdapter countryLanguageAdapter;
    public RecyclerView listLangRecyclerView;
    public IntroLanguageResource introLanguageResource = new IntroLanguageResource();
    public static IntroActivityJava getApp() {
        return app;
    }

    public static final Point screenParametersPoint = new Point();
    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Log.i("Some", getPackageName());
        newWalletButton = (Button) findViewById(R.id.button_new_wallet);
        recoverWalletButton = (Button) findViewById(R.id.button_recover_wallet);
        TextView versionText = findViewById(R.id.version_text);
        listLangRecyclerView = findViewById(R.id.language_list);
        View parentLayout = findViewById(android.R.id.content);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        countryLanguageAdapter = new CountryLanguageAdapter(this, introLanguageResource.loadResources());
        listLangRecyclerView.setAdapter(countryLanguageAdapter);

        Language currentLanguage = LocaleHelper.Companion.getInstance().getCurrentLocale();
        int currentIndex = introLanguageResource.findLanguageIndex(currentLanguage);
        countryLanguageAdapter.updateCenterPosition(currentIndex);
        new Handler().post(() -> listLangRecyclerView.scrollToPosition(currentIndex));
        listLangRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    int centerPosition = (lastVisibleItemPosition - firstVisibleItemPosition) / 2 + firstVisibleItemPosition;
                    if (centerPosition != RecyclerView.NO_POSITION) {
                        countryLanguageAdapter.updateCenterPosition(centerPosition);
                        showDialogForItem(countryLanguageAdapter.selectedMessage());
                        listLangRecyclerView.smoothScrollToPosition(centerPosition);
                    }
                }
            }

        });
        listLangRecyclerView.setLayoutManager(layoutManager);

        setListeners();
        updateBundles();
        if (BRKeyStore.AUTH_DURATION_SEC != 300) {
            RuntimeException ex = new RuntimeException("onCreate: AUTH_DURATION_SEC should be 300");
            Timber.e(ex);
            throw ex;
        }
        introActivityJava = this;

        getWindowManager().getDefaultDisplay().getSize(screenParametersPoint);
        versionText.setText(BRConstants.APP_VERSION_NAME_CODE);

        if (Utils.isEmulatorOrDebug(this))
            Utils.printPhoneSpecs();

        byte[] masterPubKey = BRKeyStore.getMasterPublicKey(this);
        boolean isFirstAddressCorrect = false;
        if (masterPubKey != null && masterPubKey.length != 0) {
            Timber.d("timber: masterPubkey exists");

            isFirstAddressCorrect = SmartValidator.checkFirstAddress(this, masterPubKey);
        }
        if (!isFirstAddressCorrect) {
            Timber.d("timber: Calling wipeWalletButKeyStore");
            BRWalletManager.getInstance().wipeWalletButKeystore(this);
        }

        PostAuth.getInstance().onCanaryCheck(this, false);
    }

    private void showDialogForItem(String title) {
        Dialog dialog = new Dialog(IntroActivityJava.this);
        dialog.setContentView(R.layout.pop_up_language_intro);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.rounded_pop_up_intro);
        Button btnYes = dialog.findViewById(R.id.button_yes);
        Button btnNo = dialog.findViewById(R.id.button_no);
        TextView txtTitle = dialog.findViewById(R.id.dialog_message);
        txtTitle.setText(title);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LocaleHelper.Companion.getInstance().setLocaleIfNeeded(countryLanguageAdapter.selectedLang())) {
                    dialog.dismiss();
                    recreate();
                }else {
                    dialog.dismiss();
                }
            }
        });


        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void updateBundles() {
        BRExecutor.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("updateBundle");
                final long startTime = System.currentTimeMillis();
                APIClient apiClient = APIClient.getInstance(IntroActivityJava.this);
                long endTime = System.currentTimeMillis();
                Timber.d("timber: updateBundle DONE in %sms",endTime - startTime);


                //DEV Moved this back until after the bundle is loaded
                //STILL NOT WORKING
                // String afID = Utils.fetchPartnerKey(IntroActivity.this, PartnerNames.AFDEVID)
                // AppsFlyerLib.getInstance().init(afID, null, IntroActivity.this);
                // AppsFlyerLib.getInstance().start(IntroActivity.this);
                // boolean didVerify = verifyInstallAssets(IntroActivity.this);
            }
        });
    }

//    public static boolean verifyInstallAssets(final Context context) {
//        Timber.d("timber: verify");
//
//        String pusherStagingKey = Utils.fetchPartnerKey(context, PartnerNames.PUSHERSTAGING);
//        boolean isCanaryFilePresent = false;
//        try (Scanner scanner = new Scanner(new File("canary-file.json"))) {
//            while (scanner.hasNextLine()) {
//                String line = scanner.nextLine();
//                Timber.d("timber: canary file : %s", line);
//                isCanaryFilePresent = line.length() > 5;
//            }
//        } catch (RuntimeException | FileNotFoundException e) {
//            Timber.e(e);
//        }
//
//        return ( pusherStagingKey.contains("4cc2-94df") && isCanaryFilePresent );
//    }

    private void setListeners() {
        newWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BreadActivity bApp = BreadActivity.getApp();
                if (bApp != null) bApp.finish();
                Intent intent = new Intent(IntroActivityJava.this, SetPinActivity.class);
                startActivity(intent);
            }
        });

        recoverWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BreadActivity bApp = BreadActivity.getApp();
                if (bApp != null) bApp.finish();
                Intent intent = new Intent(IntroActivityJava.this, RecoverActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;

    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

}
