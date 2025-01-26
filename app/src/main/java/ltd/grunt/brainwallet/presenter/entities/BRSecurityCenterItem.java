package ltd.grunt.brainwallet.presenter.entities;

import android.view.View;

public class BRSecurityCenterItem {

    public String title;
    public String text;
    public int checkMarkResId;
    public View.OnClickListener listener;

    public BRSecurityCenterItem(String title, String text, int checkMarkResId, View.OnClickListener listener) {
        this.title = title;
        this.text = text;
        this.checkMarkResId = checkMarkResId;
        this.listener = listener;
    }

}