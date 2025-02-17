package com.brainwallet.presenter.activities.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.brainwallet.R;
import com.brainwallet.navigation.LegacyNavigation;
import com.brainwallet.navigation.Route;
import com.brainwallet.presenter.activities.util.BRActivity;
import com.brainwallet.tools.animation.BRAnimator;


public class WipeActivity extends BRActivity {
    private Button nextButton;
    private ImageButton close;
    public static boolean appVisible = false;
    private static WipeActivity app;

    public static WipeActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);

        nextButton = (Button) findViewById(R.id.send_button);
        close = (ImageButton) findViewById(R.id.close_button);

        ImageButton faq = (ImageButton) findViewById(R.id.faq_button);
        //TODO: all views are using the layout of this button. Views should be refactored without it
        // Hiding until layouts are built.

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                LegacyNavigation.openComposeScreen(
                        WipeActivity.this,
                        new Route.InputWords(Route.InputWords.Source.SETTING_WIPE)
                );
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                if (!WipeActivity.this.isDestroyed()) finish();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                onBackPressed();
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_up, R.anim.exit_to_bottom);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
