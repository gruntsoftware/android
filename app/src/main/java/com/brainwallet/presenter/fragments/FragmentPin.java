package com.brainwallet.presenter.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.brainwallet.R;
import com.brainwallet.presenter.activities.BreadActivity;
import com.brainwallet.presenter.customviews.BRKeyboard;
import com.brainwallet.presenter.interfaces.BRAuthCompletion;
import com.brainwallet.tools.animation.DecelerateOvershootInterpolator;
import com.brainwallet.tools.animation.SpringAnimator;
import com.brainwallet.tools.security.AuthManager;
import com.brainwallet.tools.security.BRKeyStore;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.tools.util.Utils;

import timber.log.Timber;

public class FragmentPin extends Fragment {
    private BRAuthCompletion completion;

    private BRKeyboard keyboard;
    private LinearLayout pinLayout;
    private View dot1;
    private View dot2;
    private View dot3;
    private View dot4;
    private StringBuilder pin = new StringBuilder();
    private int pinLimit = 4;
    private TextView title;
    private TextView message;
    private RelativeLayout dialogLayout;
    ConstraintLayout mainLayout;
    private boolean authSucceeded;
    private String customTitle;
    private String customMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bread_pin, container, false);
        keyboard = (BRKeyboard) rootView.findViewById(R.id.brkeyboard);
        pinLayout = (LinearLayout) rootView.findViewById(R.id.pinLayout);

        if (BRKeyStore.getPinCode(getContext()).length() == 4) pinLimit = 4;

        title = (TextView) rootView.findViewById(R.id.title);
        message = (TextView) rootView.findViewById(R.id.message);
        dialogLayout = (RelativeLayout) rootView.findViewById(R.id.pin_dialog);
        mainLayout = (ConstraintLayout) rootView.findViewById(R.id.activity_pin);

        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().beginTransaction().remove(FragmentPin.this).commit();
            }
        });

        dot1 = rootView.findViewById(R.id.dot1);
        dot2 = rootView.findViewById(R.id.dot2);
        dot3 = rootView.findViewById(R.id.dot3);
        dot4 = rootView.findViewById(R.id.dot4);

        keyboard.addOnInsertListener(new BRKeyboard.OnInsertListener() {
            @Override
            public void onClick(String key) {
                handleClick(key);
            }
        });
        keyboard.setShowDot(false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        float keyboardTrY = keyboard.getTranslationY();
        Bundle bundle = getArguments();
        String titleString = bundle.getString("title");
        String messageString = bundle.getString("message");
        if (!Utils.isNullOrEmpty(titleString)) {
            customTitle = titleString;
            title.setText(customTitle);
        }
        if (!Utils.isNullOrEmpty(messageString)) {
            customMessage = messageString;
            message.setText(customMessage);
        }
        keyboard.setTranslationY(keyboardTrY + BreadActivity.screenParametersPoint.y / 3);
        keyboard.animate()
                .translationY(keyboardTrY)
                .setDuration(400)
                .setInterpolator(new DecelerateOvershootInterpolator(2.0f, 1f))
                .withLayer();
        float dialogScaleX = dialogLayout.getScaleX();
        float dialogScaleY = dialogLayout.getScaleY();
        dialogLayout.setScaleX(dialogScaleX / 2);
        dialogLayout.setScaleY(dialogScaleY / 2);
        dialogLayout.animate()
                .scaleY(dialogScaleY)
                .scaleX(dialogScaleX)
                .setInterpolator(new OvershootInterpolator(2f));

    }

    @Override
    public void onResume() {
        super.onResume();
        updateDots();
        authSucceeded = false;
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

    private void updateDots() {
        if (dot1 == null) return;
        AuthManager.getInstance().updateDots(getActivity(), pinLimit, pin.toString(), dot1, dot2, dot3, dot4, R.drawable.ic_pin_dot_gray, new AuthManager.OnPinSuccess() {
            @Override
            public void onSuccess() {
                if (AuthManager.getInstance().checkAuth(pin.toString(), getContext())) {
                    handleSuccess();
                } else {
                    handleFail();
                }
                pin = new StringBuilder();
            }
        });
    }

    private void handleSuccess() {
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Timber.e(e);
                }
                authSucceeded = true;
                completion.onComplete();
                Activity app = getActivity();
                AuthManager.getInstance().authSuccess(app);
                if (app != null)
                    app.getFragmentManager().popBackStack();
            }
        });
    }

    private void handleFail() {
        SpringAnimator.failShakeAnimation(getActivity(), pinLayout);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateDots();
            }
        }, 500);
        AuthManager.getInstance().authFail(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void setCompletion(BRAuthCompletion completion) {
        this.completion = completion;
    }
}
