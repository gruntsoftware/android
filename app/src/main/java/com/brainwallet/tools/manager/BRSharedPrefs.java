package com.brainwallet.tools.manager;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;

import com.brainwallet.data.model.LtcStats;
import com.brainwallet.data.repository.SettingRepository;
import com.brainwallet.constants.BWConstants;

import org.jetbrains.annotations.NotNull;
import org.koin.core.annotation.Single;
import org.koin.java.KoinJavaComponent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;
@Single
public class BRSharedPrefs {

    private static final List<OnIsoChangedListener> isoChangedListeners = new ArrayList<>();
    public static final String SEND_TRANSACTION_COUNT = "send_transaction_count";
    public static final String IN_APP_REVIEW_DONE = "in_app_review_done";
    public static final String PREFERRED_FPRATE = "preferredFalsePositiveRate";

    public interface OnIsoChangedListener {
        void onIsoChanged(String iso);
    }

    public static void addIsoChangedListener(OnIsoChangedListener listener) {
        if (isoChangedListeners.contains(listener)) return;
        isoChangedListeners.add(listener);
    }

    public static void removeListener(OnIsoChangedListener listener) {
        isoChangedListeners.remove(listener);
    }

    public static String getIsoSymbol(Context context) {
        SharedPreferences settingsToGet = KoinJavaComponent.get(SharedPreferences.class);
        String defIso;
        String defaultLanguage = Locale.getDefault().getLanguage();

        try {
            if (defaultLanguage == "ru") {
                defIso = Currency.getInstance(new Locale("ru", "RU")).getCurrencyCode();
            } else if (defaultLanguage == "en") {
                defIso = Currency.getInstance(Locale.US).getCurrencyCode();
            } else {
                defIso = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
            }
        } catch (IllegalArgumentException e) {
            Timber.e(e);
            defIso = Currency.getInstance(Locale.US).getCurrencyCode();
        }
        return settingsToGet.getString(SettingRepository.KEY_FIAT_CURRENCY_CODE, defIso); //using new shared prefs used by setting repository
    }

    public static void putIso(Context context, String code) {
        SharedPreferences settings = KoinJavaComponent.get(SharedPreferences.class);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(SettingRepository.KEY_FIAT_CURRENCY_CODE, code); //using new shared prefs used by setting repository
        editor.apply();
    }


    /// ///////////////////////////////////////////////////////////////////////////
    /// ///////////////// Active Shared Preferences ///////////////////////////////

    ////// Start Sync Timestamp ///////////
    public static void putStartSyncTimestamp(long time) {
        SharedPreferences prefs = KoinJavaComponent.get(SharedPreferences.class);
        prefs.edit().putLong("startSyncTime", time).apply();
    }
    public static long getStartSyncTimestamp() {
        SharedPreferences prefs = KoinJavaComponent.get(SharedPreferences.class);
        return prefs.getLong("startSyncTime", System.currentTimeMillis());
    }

    ////// End Sync Timestamp ///////////
    public static void putEndSyncTimestamp(long time) {
        SharedPreferences prefs = KoinJavaComponent.get(SharedPreferences.class);
        prefs.edit().putLong("endSyncTime", time).apply();
    }
    public static long getEndSyncTimestamp() {
        SharedPreferences prefs = KoinJavaComponent.get(SharedPreferences.class);
        return prefs.getLong("endSyncTime", System.currentTimeMillis());
    }

    ////// Sync Metadata ///////////
    public static void putSyncMetadata(long startSyncTime, long endSyncTime) {
        SharedPreferences prefs = KoinJavaComponent.get(SharedPreferences.class);
        double syncDuration = (double) (endSyncTime - startSyncTime) / 1_000.0 / 60.0;

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm",
                Locale.getDefault());
        Date startDate = new Date(startSyncTime);
        Date endDate = new Date(endSyncTime);
        String formattedMetadata = String.format(Locale.getDefault(),
                "Duration: %3.2f mins\nStarted: %d (%s)\nEnded: %d (%s)",
                syncDuration,
                startSyncTime, sdf.format(startDate),
                endSyncTime, sdf.format(endDate));
        prefs.edit().putString("syncMetadata", formattedMetadata).apply();
    }
    public static String getSyncMetadata(Context context) {
        SharedPreferences syncMetadata = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return syncMetadata.getString("syncMetadata", " No Sync Duration metadata");
    }


    public static boolean getPhraseWroteDown(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(BWConstants.PHRASE_WRITTEN, false);
    }

    public static void putPhraseWroteDown(Context context, boolean check) {
        SharedPreferences prefs = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(BWConstants.PHRASE_WRITTEN, check);
        editor.apply();
    }

    public static int getCurrencyListPosition(Context context) {
        SharedPreferences settings = context.getSharedPreferences(BWConstants.PREFS_NAME, 0);
        return settings.getInt(BWConstants.POSITION, 0);
    }

    public static void putCurrencyListPosition(Context context, int lastItemsPosition) {
        SharedPreferences settings = context.getSharedPreferences(BWConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(BWConstants.POSITION, lastItemsPosition);
        editor.apply();
    }

    public static String getReceiveAddress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(BWConstants.RECEIVE_ADDRESS, "");
    }

    public static void putReceiveAddress(Context ctx, String tmpAddr) {
        SharedPreferences.Editor editor = ctx.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(BWConstants.RECEIVE_ADDRESS, tmpAddr);
        editor.apply();
    }

    public static String getFirstAddress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(BWConstants.FIRST_ADDRESS, "");
    }

    public static void putFirstAddress(Context context, String firstAddress) {
        SharedPreferences prefs = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(BWConstants.FIRST_ADDRESS, firstAddress);
        editor.apply();
    }

    public static long getCachedBalance(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong("balance", 0);
    }

    public static void putCachedBalance(Context context, long amount) {
        SharedPreferences prefs = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("balance", amount);
        editor.apply();
    }

    public static long getSecureTime(Context activity) {
        SharedPreferences prefs = activity.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(BWConstants.SECURE_TIME_PREFS, System.currentTimeMillis() / 1000);
    }

    //secure time from the server
    public static void putSecureTime(Context activity, long date) {
        SharedPreferences prefs = activity.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(BWConstants.SECURE_TIME_PREFS, date);
        editor.apply();
    }

    public static long getFeeTime(Context activity) {
        SharedPreferences prefs = activity.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong("feeTime", 0);
    }

    public static void putFeeTime(Context activity, long feeTime) {
        SharedPreferences prefs = activity.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("feeTime", feeTime);
        editor.apply();
    }

    public static boolean getAllowSpend(Context activity) {
        SharedPreferences prefs = activity.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(BWConstants.ALLOW_SPEND, true);
    }

    public static void putAllowSpend(Context activity, boolean allow) {
        SharedPreferences prefs = activity.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(BWConstants.ALLOW_SPEND, allow);
        editor.apply();
    }

    //if the user prefers all in litecoin units, not other currencies
    public static boolean getPreferredLTC(Context activity) {
        SharedPreferences prefs = activity.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("priceSetToLitecoin", true);
    }

    //if the user prefers all in litecoin units, not other currencies
    public static void putPreferredLTC(Context activity, boolean b) {
        Timber.d("timber: putPreferredLTC: %s", b);
        SharedPreferences prefs = activity.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("priceSetToLitecoin", b);
        editor.apply();
    }

    //if the user prefers all in litecoin units, not other currencies
    public static boolean getUseFingerprint(Context activity) {
        SharedPreferences prefs = activity.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("useFingerprint", false);
    }

    //if the user prefers all in litecoin units, not other currencies
    public static void putUseFingerprint(Context activity, boolean use) {
        SharedPreferences prefs = activity.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("useFingerprint", use);
        editor.apply();
    }

    public static int getStartHeight(Context context) {
        SharedPreferences settingsToGet = context.getSharedPreferences(BWConstants.PREFS_NAME, 0);
        return settingsToGet.getInt(BWConstants.START_HEIGHT, 0);
    }

    public static void putStartHeight(Context context, int startHeight) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BWConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(BWConstants.START_HEIGHT, startHeight);
        editor.apply();
    }
    public static int getLastBlockHeight(Context context) {
        SharedPreferences settingsToGet = context.getSharedPreferences(BWConstants.PREFS_NAME, 0);
        return settingsToGet.getInt(BWConstants.LAST_BLOCK_HEIGHT, 0);
    }
    public static void putLiveLtcStats(Context context,
                                       int ltcBlockHeight,
                                       int mempoolTransactions,
                                       int mempoolSize,
                                       int transactionsOver24H) {
        SharedPreferences.Editor editor = context
                .getSharedPreferences(BWConstants.PREFS_NAME, 0)
                .edit();
        editor.putInt(BWConstants.LTC_STATS_BLOCK_HEIGHT, ltcBlockHeight);
        editor.putInt(BWConstants.LTC_STATS_MEMPOOL_TRANSACTIONS, mempoolTransactions);
        editor.putInt(BWConstants.LTC_STATS_MEMPOOL_SIZE, mempoolSize);
        editor.putInt(BWConstants.LTC_STATS_TRANSACTIONS_OVER_24H, transactionsOver24H);
        editor.apply();
    }
    public static LtcStats getLiveLtcStats(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BWConstants.PREFS_NAME, 0);
        return new LtcStats(
                prefs.getInt(BWConstants.LTC_STATS_BLOCK_HEIGHT, 0),
                prefs.getInt(BWConstants.LTC_STATS_MEMPOOL_TRANSACTIONS, 0),
                prefs.getInt(BWConstants.LTC_STATS_MEMPOOL_SIZE, 0),
                prefs.getInt(BWConstants.LTC_STATS_TRANSACTIONS_OVER_24H, 0)
        );
    }
    public static void putLastBlockHeight(Context context, int lastHeight) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BWConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(BWConstants.LAST_BLOCK_HEIGHT, lastHeight);
        editor.apply();
    }

    public static boolean getScanRecommended(Context context) {
        SharedPreferences settingsToGet = context.getSharedPreferences(BWConstants.PREFS_NAME, 0);
        return settingsToGet.getBoolean("scanRecommended", false);
    }

    public static void putScanRecommended(Context context, boolean recommended) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BWConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("scanRecommended", recommended);
        editor.apply();
    }

    public static int getCurrencyUnit(Context context) {
        SharedPreferences settingsToGet = context.getSharedPreferences(BWConstants.PREFS_NAME, 0);
        return settingsToGet.getInt(BWConstants.CURRENT_UNIT, BWConstants.CURRENT_UNIT_LITECOINS);
    }

    public static void putCurrencyUnit(Context context, int unit) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BWConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(BWConstants.CURRENT_UNIT, unit);
        editor.apply();
    }

    private static void setDeviceId(Context context, String uuid) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BWConstants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(BWConstants.USER_ID, uuid);
        editor.apply();
    }

    public static void clearAllPrefs(Context activity) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }

    public static boolean getShowNotification(Context context) {
        SharedPreferences settingsToGet = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return settingsToGet.getBoolean("showNotification", false);
    }

    public static void putShowNotification(Context context, boolean show) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("showNotification", show);
        editor.apply();
    }

    public static boolean getShareData(Context context) {
        SharedPreferences settingsToGet = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return settingsToGet.getBoolean("shareData", false);
    }

    public static void putShareData(Context context, boolean show) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("shareData", show);
        editor.apply();
    }

    public static boolean getShareDataDismissed(Context context) {
        SharedPreferences settingsToGet = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return settingsToGet.getBoolean("shareDataDismissed", false);
    }

    public static void putShareDataDismissed(Context context, boolean dismissed) {
        if (context == null) return;
        SharedPreferences settings = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("shareDataDismissed", dismissed);
        editor.apply();
    }

    public static String getTrustNode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("trustNode", "");
    }

    public static void putTrustNode(Context context, String trustNode) {
        SharedPreferences prefs = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("trustNode", trustNode);
        editor.apply();
    }

    public static void incrementSendTransactionCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        int currentTransactions = prefs.getInt(SEND_TRANSACTION_COUNT, 0);
        prefs.edit().putInt(SEND_TRANSACTION_COUNT, currentTransactions + 1).apply();
    }

    public static int getSendTransactionCount(Context context) {
        return context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(SEND_TRANSACTION_COUNT, 0);
    }

    public static boolean isInAppReviewDone(Context context) {
        return context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(IN_APP_REVIEW_DONE, false);
    }

    public static void inAppReviewDone(Context context) {
        context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(IN_APP_REVIEW_DONE, true).apply();
    }

    public static float getFalsePositivesRate(Context context) {
        return context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE).getFloat(PREFERRED_FPRATE, BWConstants.FALSE_POS_RATE_LOW_PRIVACY);
    }

    public static void putFalsePositivesRate(Context context, float preferredRate) {
        SharedPreferences prefs = context.getSharedPreferences(BWConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(PREFERRED_FPRATE, preferredRate);
        editor.apply();
    }
}

