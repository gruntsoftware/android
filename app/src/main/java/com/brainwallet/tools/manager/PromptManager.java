package com.brainwallet.tools.manager;

import static com.brainwallet.tools.manager.PromptManager.PromptItem.RECOMMEND_RESCAN;
import static com.brainwallet.tools.manager.PromptManager.PromptItem.UPGRADE_PIN;
import static com.brainwallet.tools.util.BRConstants.BW_PIN_LENGTH;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.brainwallet.presenter.activities.settings.SyncBlockchainActivity;
import com.brainwallet.tools.security.BRKeyStore;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.R;
import com.brainwallet.presenter.activities.UpdatePinActivity;
import com.brainwallet.wallet.BRPeerManager;

public class PromptManager {

    private PromptManager() {
    }

    private static PromptManager instance;

    public static PromptManager getInstance() {
        if (instance == null) instance = new PromptManager();
        return instance;
    }

    public enum PromptItem {
        SYNCING,
        FINGER_PRINT,
        UPGRADE_PIN,
        RECOMMEND_RESCAN,
        NO_PASSCODE
    }

    public class PromptInfo {
        public String title;
        public String description;
        public View.OnClickListener listener;

        public PromptInfo(String title, String description, View.OnClickListener listener) {
            assert (title != null);
            assert (description != null);
            assert (listener != null);
            this.title = title;
            this.description = description;
            this.listener = listener;
        }
    }

    public boolean shouldPrompt(Context app, PromptItem item) {
        assert (app != null);
        switch (item) {
            case UPGRADE_PIN:
                return BRKeyStore.getPinCode(app).length() != BW_PIN_LENGTH;
            case RECOMMEND_RESCAN:
                return BRSharedPrefs.getScanRecommended(app);
        }
        return false;
    }

    public PromptItem nextPrompt(Context app) {
        if (shouldPrompt(app, RECOMMEND_RESCAN)) return RECOMMEND_RESCAN;
        if (shouldPrompt(app, UPGRADE_PIN)) return UPGRADE_PIN;
        return null;
    }

    public PromptInfo promptInfo(final Activity app, PromptItem item) {
        switch (item) {
            case UPGRADE_PIN:
                return new PromptInfo(app.getString(R.string.Prompts_UpgradePin_title), app.getString(R.string.Prompts_UpgradePin_body), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(app, UpdatePinActivity.class);
                        app.startActivity(intent);
                        app.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                    }
                });
            case RECOMMEND_RESCAN:
                return new PromptInfo(app.getString(R.string.Prompts_RecommendRescan_title), app.getString(R.string.Prompts_RecommendRescan_body), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                            @Override
                            public void run() {
                                BRSharedPrefs.putStartHeight(app, 0);
                                BRPeerManager.getInstance().rescan();
                                BRSharedPrefs.putScanRecommended(app, false);
                                BRSharedPrefs.putAllowSpend(app, false);
                            }
                        });
                    }
                });

        }
        return null;
    }

    /**
     * touchIdPrompt - Shown to the user to enable biometric authentication for purchases under a certain amount.
     * paperKeyPrompt - Shown to the user if they have not yet written down their paper key. This is a persistent prompt and shows up until the user has gone through the paper key flow.
     * upgradePinPrompt - Shown to recommend to the user they should upgrade their PIN from 4 digits to 6. Only shown once. If the user dismisses do not show again.
     * recommendRescanPrompt - Shown when the user should rescan the blockchain
     * noPasscodePrompt - Shown when the user does not have a passcode set up for their device.
     * shareDataPrompt - Shown when asking the user if they wish to share anonymous data. Lowest priority prompt. Only show once and if they dismiss do not show again.
     */
    public String getPromptName(PromptItem prompt) {
        switch (prompt) {
            case UPGRADE_PIN:
                return "upgradePinPrompt";
            case RECOMMEND_RESCAN:
                return "recommendRescanPrompt";
            case NO_PASSCODE:
                return "noPasscodePrompt";

        }
        return null;
    }

}
