package com.brainwallet.presenter.activities;

import static com.brainwallet.tools.util.BRConstants.BW_PIN_LENGTH;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brainwallet.R;
import com.brainwallet.presenter.activities.util.BRActivity;
import com.brainwallet.presenter.customviews.BRKeyboard;
import com.brainwallet.presenter.interfaces.BROnSignalCompletion;
import com.brainwallet.tools.animation.BRAnimator;
import com.brainwallet.tools.animation.SpringAnimator;
import com.brainwallet.tools.security.AuthManager;
import com.brainwallet.tools.security.BRKeyStore;

import timber.log.Timber;

public class UpdatePinActivity extends BRActivity {
    private BRKeyboard keyboard;
    private View dot1;
    private View dot2;
    private View dot3;
    private View dot4;
    private StringBuilder pin = new StringBuilder();
    private int pinLimit = 4;
    //    private boolean allowInserting = true;
    private TextView title;
    private TextView description;
    int mode = ENTER_PIN;
    public static final int ENTER_PIN = 1;
    public static final int ENTER_NEW_PIN = 2;
    public static final int RE_ENTER_NEW_PIN = 3;

    private ImageButton faq;
    private LinearLayout pinLayout;
    private String curNewPin = "";
    public static boolean appVisible = false;
    private static UpdatePinActivity app;

    public static UpdatePinActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_template);

        keyboard = (BRKeyboard) findViewById(R.id.brkeyboard);
        title = (TextView) findViewById(R.id.title);
        description = (TextView) findViewById(R.id.description);
        pinLayout = (LinearLayout) findViewById(R.id.pinLayout);
        if (BRKeyStore.getPinCode(this).length() == BW_PIN_LENGTH) pinLimit = BW_PIN_LENGTH;
        setMode(ENTER_PIN);
        title.setText(getString(R.string.UpdatePin_updateTitle));
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);

        //TODO: all views are using the layout of this button. Views should be refactored without it
        // Hiding until layouts are built.
        faq = (ImageButton) findViewById(R.id.faq_button);

        keyboard.addOnInsertListener(new BRKeyboard.OnInsertListener() {
            @Override
            public void onClick(String key) {
                handleClick(key);
            }
        });
        keyboard.setShowDot(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDots();
        appVisible = true;
        app = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    private void handleClick(String key) {
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
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    private void updateDots() {

        AuthManager.getInstance().updateDots(this, pinLimit, pin.toString(), dot1, dot2, dot3, dot4, R.drawable.ic_pin_dot_gray, new AuthManager.OnPinSuccess() {
            @Override
            public void onSuccess() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        goNext();
                    }
                }, 100);
            }
        });
    }

    private void goNext() {
        switch (mode) {
            case ENTER_PIN:
                if (AuthManager.getInstance().checkAuth(pin.toString(), this)) {
                    setMode(ENTER_NEW_PIN);
                    pinLimit = 4;
                } else {
                    SpringAnimator.failShakeAnimation(this, pinLayout);
                }
                pin = new StringBuilder();
                updateDots();
                break;
            case ENTER_NEW_PIN:
                setMode(RE_ENTER_NEW_PIN);
                curNewPin = pin.toString();
                pin = new StringBuilder();
                updateDots();
                break;

            case RE_ENTER_NEW_PIN:
                if (curNewPin.equalsIgnoreCase(pin.toString())) {
                    AuthManager.getInstance().setPinCode(pin.toString(), this);
                    BRAnimator.showBreadSignal(this, getString(R.string.Alerts_pinSet), getString(R.string.UpdatePin_caption), R.drawable.ic_check_mark_white, new BROnSignalCompletion() {
                        @Override
                        public void onComplete() {
                            BRAnimator.startBreadActivity(UpdatePinActivity.this, false);
                        }
                    });
                } else {
                    SpringAnimator.failShakeAnimation(this, pinLayout);
                    setMode(ENTER_NEW_PIN);
                    pinLimit = BRKeyStore.getPinCode(this).length();
                }
                pin = new StringBuilder();
                updateDots();
                break;
        }
    }

    private void setMode(int mode) {
        String text = "";
        this.mode = mode;
        switch (mode) {
            case ENTER_PIN:
                text = getString(R.string.UpdatePin_enterCurrent);
                break;
            case ENTER_NEW_PIN:
                text = getString(R.string.UpdatePin_enterNew);
                break;
            case RE_ENTER_NEW_PIN:
                text = getString(R.string.UpdatePin_reEnterNew);
                break;
        }
        description.setText(text);
        SpringAnimator.springView(description);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
