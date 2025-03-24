package com.brainwallet.presenter.activities.settings;

import static com.brainwallet.R.layout.settings_list_item;
import static com.brainwallet.R.layout.settings_list_section;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brainwallet.BrainwalletApp;
import com.brainwallet.R;
import com.brainwallet.data.repository.SettingRepository;
import com.brainwallet.navigation.LegacyNavigation;
import com.brainwallet.navigation.Route;
import com.brainwallet.presenter.activities.util.BRActivity;
import com.brainwallet.presenter.entities.BRSettingsItem;
import com.brainwallet.presenter.language.ChangeLanguageBottomSheet;
import com.brainwallet.tools.animation.BRAnimator;
import com.brainwallet.tools.manager.BRSharedPrefs;

import org.koin.java.KoinJavaComponent;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BRActivity {
    private ListView listView;
    public List<BRSettingsItem> items;
    public static boolean appVisible = false;
    private ImageButton closeButton;
    private static SettingsActivity app;

    public static SettingsActivity getApp() {
        return app;
    }

    private SettingRepository settingRepository = (SettingRepository) KoinJavaComponent.inject(SettingRepository.class).getValue();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        listView = findViewById(R.id.settings_list);

        closeButton = (ImageButton) findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> finish());
    }

    public class SettingsListAdapter extends ArrayAdapter<String> {

        private List<BRSettingsItem> items;
        private Context mContext;

        public SettingsListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<BRSettingsItem> items) {
            super(context, resource);
            this.items = items;
            this.mContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View v;
            BRSettingsItem item = items.get(position);
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();

            if (item.isSection) {
                v = inflater.inflate(settings_list_section, parent, false);
            } else {
                v = inflater.inflate(settings_list_item, parent, false);
                TextView addon = (TextView) v.findViewById(R.id.item_addon);
                addon.setText(item.addonText);
                v.setOnClickListener(item.listener);
            }

            TextView title = (TextView) v.findViewById(R.id.item_title);
            title.setText(item.title);
            return v;

        }

        @Override
        public int getCount() {
            return items == null ? 0 : items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
        if (items == null)
            items = new ArrayList<>();
        items.clear();

        populateItems();

        listView.setAdapter(new SettingsListAdapter(this, R.layout.settings_list_item, items));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_up, R.anim.exit_to_bottom);
    }

    private void populateItems() {

        /*Wallet Title*/
        items.add(new BRSettingsItem(getString(R.string.Settings_wallet), "", null, true));

        /*Show Seed Phrase*/
        items.add(new BRSettingsItem(getString(R.string.settings_show_seed), "", v -> {
            BRAnimator.showBalanceSeedFragment(this);
        }, false));

        /*Manage Title*/
        items.add(new BRSettingsItem(getString(R.string.Settings_manage), "", null, true));

        //toggle dark mode
        boolean isDarkMode = settingRepository.isDarkMode();
        items.add(new BRSettingsItem(getString(R.string.toggle_dark_mode), getString(isDarkMode ? androidx.appcompat.R.string.abc_capital_on : androidx.appcompat.R.string.abc_capital_off), v -> {
            settingRepository.toggleDarkMode(!isDarkMode);
            LegacyNavigation.openComposeScreen(SettingsActivity.this);
        }, false));

        /*Languages*/
        items.add(new BRSettingsItem(getString(R.string.Settings_languages), null, v -> {
            ChangeLanguageBottomSheet fragment = new ChangeLanguageBottomSheet();
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        }, false));

        /*Display Currency*/
        items.add(new BRSettingsItem(getString(R.string.Settings_currency), BRSharedPrefs.getIsoSymbol(this), v -> {
            Intent intent = new Intent(SettingsActivity.this, DisplayCurrencyActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }, false));

        /*Sync Blockchain*/
        items.add(new BRSettingsItem(getString(R.string.Settings_sync), "", v -> {
            Intent intent = new Intent(SettingsActivity.this, SyncBlockchainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }, false));

        /*SPACER*/
        items.add(new BRSettingsItem("", "", null, true));

        /*Share Anonymous Data*/
        items.add(new BRSettingsItem(getString(R.string.Settings_shareData), "", v -> {
            Intent intent = new Intent(SettingsActivity.this, ShareDataActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }, false));

        /*About*/
        items.add(new BRSettingsItem(getString(R.string.Settings_about), "", v -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }, false));

        /*SPACER*/
        items.add(new BRSettingsItem("", "", null, true));

        /*Advanced Settings*/
        items.add(new BRSettingsItem(getString(R.string.Settings_advancedTitle), "", v -> {
            Intent intent = new Intent(SettingsActivity.this, AdvancedActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }, false));

        /*SPACER*/
        items.add(new BRSettingsItem("", "", null, true));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }
}
