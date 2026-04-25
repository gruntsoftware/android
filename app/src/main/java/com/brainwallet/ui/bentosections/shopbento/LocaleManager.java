package com.brainwallet.ui.bentosections.shopbento;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import java.util.Locale;

public class LocaleManager {

    private final Context context;

    public LocaleManager(Context context) {
        this.context = context;
    }

    public String getCountryIso() {
        Locale locale = context.getResources().getConfiguration().getLocales().get(0);
        return locale.getCountry(); // e.g. "TR", "US", "GB"
    }

    public String getLanguage() {
        Locale locale = context.getResources().getConfiguration().getLocales().get(0);
        return locale.getLanguage(); // e.g. "tr", "en"
    }
}
