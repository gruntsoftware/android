package ltd.grunt.brainwallet.presenter.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ltd.grunt.brainwallet.R;
import ltd.grunt.brainwallet.presenter.interfaces.BROnSignalCompletion;

public class FragmentSignal extends Fragment {
    public static final String TITLE = "title";
    public static final String ICON_DESCRIPTION = "iconDescription";
    public static final String RES_ID = "resId";
    public TextView mTitle;
    //    public TextView mDescription;
    public ImageView mIcon;
    private BROnSignalCompletion completion;
    private LinearLayout signalLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.

        View rootView = inflater.inflate(R.layout.fragment_signal, container, false);
        mTitle = (TextView) rootView.findViewById(R.id.title);
//        mDescription = (TextView) rootView.findViewById(R.id.description);
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                getParentFragmentManager().popBackStack();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (completion != null) {
                            completion.onComplete();
                            completion = null;
                        }
                    }
                }, 300);
            }
        }, 1500);
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