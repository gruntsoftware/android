package com.brainwallet.tools.animation;

import static androidx.databinding.DataBindingUtil.setContentView;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.compose.ui.platform.ComposeView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.brainwallet.navigation.LegacyNavigation;
import com.brainwallet.presenter.fragments.FragmentMoonpay;
import com.brainwallet.tools.manager.AnalyticsManager;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.tools.util.BRConstants;
import com.brainwallet.R;
import com.brainwallet.presenter.activities.BreadActivity;
import com.brainwallet.presenter.activities.camera.ScanQRActivity;
import com.brainwallet.presenter.customviews.BRDialogView;
import com.brainwallet.presenter.entities.TxItem;
import com.brainwallet.presenter.fragments.FragmentBalanceSeedReminder;
import com.brainwallet.presenter.fragments.FragmentMenu;
import com.brainwallet.presenter.fragments.FragmentReceive;
import com.brainwallet.presenter.fragments.FragmentSend;
import com.brainwallet.presenter.fragments.FragmentSignal;
import com.brainwallet.presenter.fragments.FragmentTransactionDetails;
import com.brainwallet.presenter.interfaces.BROnSignalCompletion;

import java.util.List;

import timber.log.Timber;

public class BRAnimator {
    private static boolean clickAllowed = true;
    public static int SLIDE_ANIMATION_DURATION = 300;
    public static void showBreadSignal(@NonNull FragmentActivity activity,
                                       @NonNull String title,
                                       @NonNull String iconDescription,
                                       int drawableId,
                                       @Nullable BROnSignalCompletion completion) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return; // Avoid crashes if the activity is not in a valid state
        }

        FragmentSignal fragmentSignal = new FragmentSignal();
        Bundle bundle = new Bundle();
        bundle.putString(FragmentSignal.TITLE, title);
        bundle.putString(FragmentSignal.ICON_DESCRIPTION, iconDescription);
        bundle.putInt(FragmentSignal.RES_ID, drawableId);
        fragmentSignal.setCompletion(completion);
        fragmentSignal.setArguments(bundle);

        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        if (fragmentManager.isStateSaved()) {
            return;
        }

        androidx.fragment.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.animator.from_bottom, R.animator.to_bottom,
                R.animator.from_bottom, R.animator.to_bottom);
        transaction.add(android.R.id.content, fragmentSignal, FragmentSignal.class.getName());
        transaction.addToBackStack(null);

        try {
            transaction.commit();
        } catch (IllegalStateException e) {
            transaction.commitAllowingStateLoss();
            AnalyticsManager.logCustomEvent(BRConstants._20200112_ERR);
        }
    }

    public static void showBalanceSeedFragment(@NonNull FragmentActivity app) {
       Timber.d("timber: fetched info");

         androidx.fragment.app.FragmentManager fragmentManager = app.getSupportFragmentManager();
        FragmentBalanceSeedReminder fragmentBalanceSeedReminder = (FragmentBalanceSeedReminder) fragmentManager.findFragmentByTag(FragmentBalanceSeedReminder.class.getName());
        if (fragmentBalanceSeedReminder != null) {
            fragmentBalanceSeedReminder.fetchSeedPhrase();
            Timber.d("timber: fetched seed phrase");
            return;
        }

       try {
           fragmentBalanceSeedReminder = new FragmentBalanceSeedReminder();
           fragmentManager.beginTransaction()
                   .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                   .add(android.R.id.content, fragmentBalanceSeedReminder, FragmentBalanceSeedReminder.class.getName())
                   .addToBackStack(FragmentBalanceSeedReminder.class.getName()).commit();
       } finally {
       }
    }
    public static void showSendFragment(FragmentActivity app, final String bitcoinUrl) {
        if (app == null) {
            Timber.i("timber: showSendFragment: app is null");
            return;
        }
        androidx.fragment.app.FragmentManager fragmentManager = app.getSupportFragmentManager();
        FragmentSend fragmentSend = (FragmentSend) fragmentManager.findFragmentByTag(FragmentSend.class.getName());
        if (fragmentSend != null && fragmentSend.isAdded()) {
            fragmentSend.setUrl(bitcoinUrl);
            return;
        }
        try {
            fragmentSend = new FragmentSend();
            if (bitcoinUrl != null && !bitcoinUrl.isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putString("url", bitcoinUrl);
                fragmentSend.setArguments(bundle);
            }
            fragmentManager.beginTransaction()
                    .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                    .add(android.R.id.content, fragmentSend, FragmentSend.class.getName())
                    .addToBackStack(FragmentSend.class.getName()).commit();
        } finally {
        }
    }

    public static void showTransactionPager(FragmentActivity app, List<TxItem> items, int position) {
        if (app == null) {
            Timber.i("timber: showSendFragment: app is null");
            return;
        }
        FragmentTransactionDetails fragmentTransactionDetails = (FragmentTransactionDetails) app
                .getSupportFragmentManager()
                .findFragmentByTag(FragmentTransactionDetails.class.getName());
        if (fragmentTransactionDetails != null && fragmentTransactionDetails.isAdded()) {
            fragmentTransactionDetails.setItems(items);
            Timber.i("timber: showTransactionPager: Already showing");
            return;
        }
        fragmentTransactionDetails = new FragmentTransactionDetails();
        fragmentTransactionDetails.setItems(items);
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        fragmentTransactionDetails.setArguments(bundle);

        app.getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                .add(android.R.id.content, fragmentTransactionDetails, FragmentTransactionDetails.class.getName())
                .addToBackStack(FragmentTransactionDetails.class.getName()).commit();

    }

    public static void openScanner(Activity app, int requestID) {
        try {
            if (app == null) return;

            // Check if the camera permission is granted
            if (ContextCompat.checkSelfPermission(app,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(app,
                        Manifest.permission.CAMERA)) {
                    BRDialog.showCustomDialog(app, app.getString(R.string.Send_cameraUnavailabeTitle_android), app.getString(R.string.Send_cameraUnavailabeMessage_android), app.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismiss();
                        }
                    }, null, null, 0);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(app,
                            new String[]{Manifest.permission.CAMERA},
                            BRConstants.CAMERA_REQUEST_ID);
                }
            } else {
                // Permission is granted, open camera
                Intent intent = new Intent(app, ScanQRActivity.class);
                app.startActivityForResult(intent, requestID);
                app.overridePendingTransition(R.anim.fade_up, R.anim.fade_down);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public static LayoutTransition getDefaultTransition() {
        LayoutTransition itemLayoutTransition = new LayoutTransition();
        itemLayoutTransition.setStartDelay(LayoutTransition.APPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.DISAPPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.CHANGING, 0);
        itemLayoutTransition.setDuration(100);
        itemLayoutTransition.setInterpolator(LayoutTransition.CHANGING, new OvershootInterpolator(2f));
        Animator scaleUp = ObjectAnimator.ofPropertyValuesHolder((Object) null, PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 1), PropertyValuesHolder.ofFloat(View.SCALE_Y, 0, 1));
        scaleUp.setDuration(50);
        scaleUp.setStartDelay(50);
        Animator scaleDown = ObjectAnimator.ofPropertyValuesHolder((Object) null, PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 1), PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0));
        scaleDown.setDuration(2);
        itemLayoutTransition.setAnimator(LayoutTransition.APPEARING, scaleUp);
        itemLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, null);
        itemLayoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        return itemLayoutTransition;
    }

    //isReceive tells the Animator that the Receive fragment is requested, not My Address
    public static void showReceiveFragment(Activity app, boolean isReceive) {
        if (app == null) {
            Timber.i("timber: showReceiveFragment: app is null");
            return;
        }
        FragmentReceive fragmentReceive = (FragmentReceive) app.getFragmentManager().findFragmentByTag(FragmentReceive.class.getName());
        if (fragmentReceive != null && fragmentReceive.isAdded())
            return;
        fragmentReceive = new FragmentReceive();
        Bundle args = new Bundle();
        args.putBoolean("receive", isReceive);
        fragmentReceive.setArguments(args);

        app.getFragmentManager().beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                .add(android.R.id.content, fragmentReceive, FragmentReceive.class.getName())
                .addToBackStack(FragmentReceive.class.getName()).commit();

    }

    public static void showMoonpayFragment(FragmentActivity app) {

        if (app == null) {
            Timber.i("timber: showSendFragment: app is null");
            return;
        }
        androidx.fragment.app.FragmentManager fragmentManager = app.getSupportFragmentManager();
        FragmentMoonpay fragmentMoonpay = (FragmentMoonpay) fragmentManager.findFragmentByTag(FragmentMoonpay.class.getName());
        if (fragmentMoonpay != null && fragmentMoonpay.isAdded()) {
            return;
        }
        try {
            fragmentMoonpay = new FragmentMoonpay();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                    .add(android.R.id.content, fragmentMoonpay, FragmentMoonpay.class.getName())
                    .addToBackStack(FragmentMoonpay.class.getName()).commit();
        } finally {
        }
    }

    public static boolean isClickAllowed() {
        if (clickAllowed) {
            clickAllowed = false;
            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Timber.e(e);
                    }
                    clickAllowed = true;
                }
            });
            return true;
        } else return false;
    }

    public static void killAllFragments(FragmentActivity app) {
        //DEV: Needs refactor
        if (app != null && !app.isDestroyed() && !app.getSupportFragmentManager().isStateSaved()) {
            app.getSupportFragmentManager().popBackStack();
        }
    }

    public static void startBreadIfNotStarted(Activity app) {
        if (!(app instanceof BreadActivity))
            startBreadActivity(app, false);
    }

    /**
     * wrap using [com.brainwallet.navigation.LegacyNavigation.startBreadActivity]
     */
    public static void startBreadActivity(Activity from, boolean auth) {
        LegacyNavigation.startBreadActivity(from, auth);
    }

    public static void animateSignalSlide(ViewGroup signalLayout, final boolean reverse, @Nullable final OnSlideAnimationEnd listener) {
        float translationY = signalLayout.getTranslationY();
        float signalHeight = signalLayout.getHeight();
        signalLayout.setTranslationY(reverse ? translationY : translationY + signalHeight);

        signalLayout.animate().translationY(reverse ? BreadActivity.screenParametersPoint.y : translationY).setDuration(SLIDE_ANIMATION_DURATION)
                .setInterpolator(reverse ? new DecelerateInterpolator() : new OvershootInterpolator(0.7f))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (listener != null)
                            listener.onAnimationEnd();
                    }
                });

    }

    public static void animateBackgroundDim(final ViewGroup backgroundLayout, boolean reverse) {
        int transColor = reverse ? R.color.midnight : android.R.color.transparent;
        int blackTransColor = reverse ? android.R.color.transparent : R.color.midnight;

        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(transColor, blackTransColor);
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                backgroundLayout.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });

        anim.setDuration(SLIDE_ANIMATION_DURATION);
        anim.start();
    }

    public interface OnSlideAnimationEnd {
        void onAnimationEnd();
    }
}
