package ltd.grunt.brainwallet.presenter.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ltd.grunt.brainwallet.R;
import ltd.grunt.brainwallet.presenter.activities.DisabledActivity;
import ltd.grunt.brainwallet.presenter.activities.InputWordsActivity;
import ltd.grunt.brainwallet.presenter.activities.settings.AboutActivity;
import ltd.grunt.brainwallet.presenter.activities.settings.AdvancedActivity;
import ltd.grunt.brainwallet.presenter.activities.settings.DisplayCurrencyActivity;
import ltd.grunt.brainwallet.presenter.activities.settings.ShareDataActivity;
import ltd.grunt.brainwallet.presenter.activities.settings.SyncBlockchainActivity;
import ltd.grunt.brainwallet.presenter.activities.settings.WipeActivity;
import ltd.grunt.brainwallet.presenter.language.ChangeLanguageBottomSheet;
import ltd.grunt.brainwallet.presenter.activities.util.BRActivity;
import ltd.grunt.brainwallet.presenter.entities.BRSettingsItem;
import ltd.grunt.brainwallet.presenter.interfaces.BRAuthCompletion;
import ltd.grunt.brainwallet.tools.manager.BRSharedPrefs;
import ltd.grunt.brainwallet.tools.security.AuthManager;
import com.platform.APIClient;
import ltd.grunt.brainwallet.tools.animation.BRAnimator;

import java.util.ArrayList;
import java.util.List;

import static ltd.grunt.brainwallet.R.layout.settings_list_item;
import static ltd.grunt.brainwallet.R.layout.settings_list_section;

public class SettingsActivity extends BRActivity {
    private ListView listView;
    public List<BRSettingsItem> items;
    public static boolean appVisible = false;
    private ImageButton closeButton;
    private static SettingsActivity app;
    public static SettingsActivity getApp() {
        return app;
    }
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

        /*Wipe Start_Recover Wallet*/
        items.add(new BRSettingsItem(getString(R.string.Settings_wipe), "", v -> {
            Intent intent = new Intent(SettingsActivity.this, WipeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
        }, false));

        /*Manage Title*/
        items.add(new BRSettingsItem(getString(R.string.Settings_manage), "", null, true));

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
