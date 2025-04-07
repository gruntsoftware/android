package com.brainwallet.presenter.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.brainwallet.R;
import com.brainwallet.presenter.interfaces.BROnSignalCompletion;

import timber.log.Timber;

public class FragmentSignal extends DialogFragment {
    public static final String TITLE = "title";
    public static final String ICON_DESCRIPTION = "iconDescription";
    public static final String RES_ID = "resId";
    public TextView mTitle;
    public ImageView mIcon;
    private BROnSignalCompletion completion;
    private LinearLayout signalLayout;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable popBackStackRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded()) {
                dismiss();
                handler.postDelayed(completionRunnable, 300);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_signal, container, false);
        mTitle = (TextView) rootView.findViewById(R.id.title);
        mIcon = (ImageView) rootView.findViewById(R.id.qr_image);
        signalLayout = (LinearLayout) rootView.findViewById(R.id.signal_layout);
        signalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do nothing, in order to prevent click through
            }
        });

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            String title = bundle.getString(TITLE, "");
            int resId = bundle.getInt(RES_ID, 0);
            mTitle.setText(title);
            mIcon.setImageResource(resId);
        }

        return rootView;
    }

    public void setCompletion(BROnSignalCompletion completion) {
        this.completion = completion;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handler.postDelayed(popBackStackRunnable, 1000);
    }

    private final Runnable completionRunnable = new Runnable() {
        @Override
        public void run() {
            if (completion != null) {
                completion.onComplete();
                completion = null;
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove callbacks to prevent execution after the fragment is destroyed
        handler.removeCallbacks(popBackStackRunnable);
        handler.removeCallbacks(completionRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}