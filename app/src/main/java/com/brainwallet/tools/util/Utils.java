package com.brainwallet.tools.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.brainwallet.tools.manager.AnalyticsManager;
import com.brainwallet.tools.sqlite.CurrencyDataSource;
import com.brainwallet.data.model.CurrencyEntity;
import com.brainwallet.presenter.entities.ServiceItems;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import timber.log.Timber;
public class Utils {

    public static boolean isUsingCustomInputMethod(Activity context) {
        if (context == null) return false;
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return false;
        }
        List<InputMethodInfo> mInputMethodProperties = imm.getEnabledInputMethodList();
        final int N = mInputMethodProperties.size();
        for (int i = 0; i < N; i++) {
            InputMethodInfo imi = mInputMethodProperties.get(i);
            if (imi.getId().equals(
                    Settings.Secure.getString(context.getContentResolver(),
                            Settings.Secure.DEFAULT_INPUT_METHOD))) {
                if ((imi.getServiceInfo().applicationInfo.flags &
                        ApplicationInfo.FLAG_SYSTEM) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static void printPhoneSpecs() {
        Timber.d("timber: ***************************PHONE SPECS***************************");
        Timber.d("timber: * Build.CPU_ABI: %s", Build.CPU_ABI);
        Timber.d("timber: * maxMemory:%s", Runtime.getRuntime().maxMemory());
        Timber.d("timber: ----------------------------PHONE SPECS----------------------------");
    }

    public static boolean isEmulatorOrDebug(Context app) {
        String fing = Build.FINGERPRINT;
        boolean isEmulator = false;
        if (fing != null) {
            isEmulator = fing.contains("vbox") || fing.contains("generic");
        }
        return isEmulator || (0 != (app.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    }

    public static String getFormattedDateFromLong(Context app, long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("M/d@ha", Locale.getDefault());
        boolean is24HoursFormat = false;
        if (app != null) {
            is24HoursFormat = android.text.format.DateFormat.is24HourFormat(app.getApplicationContext());
            if (is24HoursFormat) {
                formatter = new SimpleDateFormat("M/d H", Locale.getDefault());
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String result = formatter.format(calendar.getTime()).toLowerCase().replace("am", "a").replace("pm", "p");
        if (is24HoursFormat) result += "h";
        return result;
    }

    public static String formatTimeStamp(long time, String pattern) {
        return android.text.format.DateFormat.format(pattern, time).toString();
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNullOrEmpty(byte[] arr) {
        return arr == null || arr.length == 0;
    }

    public static boolean isNullOrEmpty(Collection collection) {
        return collection == null || collection.size() == 0;
    }

    public static int getPixelsFromDps(Context context, int dps) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String createBitcoinUrl(String address, long satoshiAmount, String label, String message, String rURL) {

        Uri.Builder builder = new Uri.Builder();
        builder = builder.scheme("litecoin");
        if (address != null && !address.isEmpty())
            builder = builder.appendPath(address);
        if (satoshiAmount != 0)
            builder = builder.appendQueryParameter("amount", new BigDecimal(satoshiAmount).divide(new BigDecimal(100000000), 8, BRConstants.ROUNDING_MODE).toPlainString());
        if (label != null && !label.isEmpty())
            builder = builder.appendQueryParameter("label", label);
        if (message != null && !message.isEmpty())
            builder = builder.appendQueryParameter("message", message);
        if (rURL != null && !rURL.isEmpty())
            builder = builder.appendQueryParameter("r", rURL);

        return builder.build().toString().replaceFirst("/", "");

    }

    public static void hideKeyboard(Context app) {
        if (app != null) {
            View view = ((Activity) app).getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) app.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

    }

    public static String getAgentString(Context app, String cfnetwork) {
        int versionNumber = 0;
        String deviceCode = Build.MANUFACTURER + "-|-" + Build.MODEL;


        if (app != null) {
            try {
                PackageInfo pInfo = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
                versionNumber = pInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                Timber.e(e);
            }
        }
        return String.format(Locale.ENGLISH, "%s/%d %s Android/%s Device/%s", "Brainwallet", versionNumber, cfnetwork, Build.VERSION.RELEASE, deviceCode);
    }

    public static String reverseHex(String hex) {
        if (hex == null) return null;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i <= hex.length() - 2; i = i + 2) {
            result.append(new StringBuilder(hex.substring(i, i + 2)).reverse());
        }
        return result.reverse().toString();
    }

    public static String join(String[] array, CharSequence separator) {
        if (array.length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < array.length - 1; i++) {
            stringBuilder.append(array[i]);
            stringBuilder.append(separator);
        }
        stringBuilder.append(array[array.length - 1]);
        return stringBuilder.toString();
    }
    public static String fetchServiceItem(Context app, ServiceItems name) {

        JSONObject keyObject;
        AssetManager assetManager = app.getAssets();
        try (InputStream inputStream = assetManager.open("service-data.json")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                StringBuilder sb = new StringBuilder();
                String line;
                String opsString = "";
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                keyObject = new JSONObject(sb.toString()).getJSONObject("items");

                if (name == ServiceItems.WALLETOPS) {
                   JSONArray array = new JSONArray(keyObject.get(name.getKey()).toString());
                    int randomNum = ThreadLocalRandom.current().nextInt(0, array.length() - 1);
                    return array.getString(randomNum);
                }
                else if (name == ServiceItems.OPSALL) {
                    JSONArray opsArray = new JSONArray(keyObject.get(name.getKey()).toString());

                    if (opsArray.length() > 0) {
                        for (int i=0;i<opsArray.length();i++){
                            opsString = (new StringBuilder())
                                 .append(opsString)
                                 .append(opsArray.getString(i))
                                 .append(",")
                                 .toString();
                        }
                    } else {
                        Timber.e("timber: ops element fail");
                    }
                    return opsString.replaceAll("\\s+","");
                }
                else if (name == ServiceItems.AFDEVID) {
                    return keyObject.optString(name.getKey());
                }
                else if (name == ServiceItems.CLIENTCODE) {
                    return keyObject.optString(name.getKey());
                }
                Timber.d("timber: fetchServiceItem name key found %s",name.getKey());

                return keyObject.get(name.getKey()).toString();
            } catch (IOException e) {
                e.printStackTrace();
                Timber.d("timber: fetchServiceItem IOEXception");

            } catch (JSONException e) {
                e.printStackTrace();
                Timber.d("timber: fetchServiceItem JSONException");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bundle   params = new Bundle();
        params.putString("lwa_error_message: %s Key not found", name.getKey());
        AnalyticsManager.logCustomEventWithParams(BRConstants._20200112_ERR,params);
        Timber.d("timber: fetchServiceItem lwa_error_message");
        return "";
    }
    /// Description: 1715876807
    public static long tieredOpsFee(Context app, long sendAmount) {
        if (sendAmount < 1_398_000) {
            return 69900;
        }
        else if (sendAmount < 6_991_000) {
            return 111_910;
        }
        else if (sendAmount < 27_965_000) {
            return 279_700;
        }
        else if (sendAmount < 139_820_000) {
            return 699_540;
        }
        else if (sendAmount < 279_653_600) {
            return 1_049_300;
        }
        else if (sendAmount < 699_220_000) {
            return 1_398_800;
        }
        else if (sendAmount < 1_398_440_000) {
            return 2_797_600;
        }
        else {
            return 2_797_600;
        }
    }
    private static boolean isBetween(double x, double lower, double upper) {
        return lower <= x && x <= upper;
    }
    public static Set<String> brainwalletOpsSet(Context app) {
        List<String> addressList = Collections.singletonList(Utils.fetchServiceItem(app, ServiceItems.WALLETOPS));
        return new HashSet<String>(addressList);
    }

    private class UInt64 {
        public UInt64(BigInteger bigInteger) {
        }
    }
}
