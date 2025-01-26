package ltd.grunt.brainwallet.presenter.entities;

import android.view.View;

public class BRMenuItem {

    public String text;
    public int resId;
    public View.OnClickListener listener;

    public BRMenuItem(String text, int resId, View.OnClickListener listener) {
        this.text = text;
        this.resId = resId;
        this.listener = listener;
    }

}