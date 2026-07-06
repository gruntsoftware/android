package com.brainwallet.tools.manager;

import android.content.Context;
import android.os.Bundle;

import com.brainwallet.constants.BWConstants;
import com.google.firebase.analytics.FirebaseAnalytics;

public final class AnalyticsManager {

    private static FirebaseAnalytics instance;

    private AnalyticsManager() {
        // NO-OP
    }

    public static void init(Context context) {
        instance = FirebaseAnalytics.getInstance(context);
    }

    public static Object logCustomEvent(@BWConstants.Event String customEvent) {
        instance.logEvent(customEvent, null);
        return null;
    }

    public static Object logCustomAdHocEvent(String adHocEvent, Bundle params) {
        instance.logEvent(adHocEvent, params);
        return null;
    }

    public static void logCustomEventWithParams(@BWConstants.Event String customEvent, Bundle params) {
        instance.logEvent(customEvent, params);
    }
}




