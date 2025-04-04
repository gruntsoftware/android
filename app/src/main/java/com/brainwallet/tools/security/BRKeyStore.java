package com.brainwallet.tools.security;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.view.View;

import com.brainwallet.R;
import com.brainwallet.exceptions.BRKeystoreErrorException;
import com.brainwallet.presenter.customviews.BRDialogView;
import com.brainwallet.tools.animation.BRDialog;
import com.brainwallet.tools.manager.BRSharedPrefs;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.tools.util.BytesUtil;
import com.brainwallet.tools.util.TypesConverter;
import com.brainwallet.tools.util.Utils;
import com.brainwallet.util.cryptography.KeyStoreManager;
import com.brainwallet.wallet.BRWalletManager;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.platform.entities.WalletInfo;
import com.platform.tools.KVStoreManager;

import org.koin.java.KoinJavaComponent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

import timber.log.Timber;

//TODO: [yuana] please migrate the caller using [KeyStoreManager]
@Deprecated
public class BRKeyStore {

    public static final String KEY_STORE_PREFS_NAME = "keyStorePrefs";
    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    public static final String CIPHER_ALGORITHM = "AES/CBC/PKCS7Padding";
    public static final String PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7;
    public static final String BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC;

    public static final String NEW_CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    public static final String NEW_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE;
    public static final String NEW_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM;

    public static Map<String, AliasObject> aliasObjectMap;

    private static final String PHRASE_IV = "ivphrase";
    private static final String CANARY_IV = "ivcanary";
    private static final String PUB_KEY_IV = "ivpubkey";
    private static final String WALLET_CREATION_TIME_IV = "ivtime";
    private static final String PASS_CODE_IV = "ivpasscode";
    private static final String FAIL_COUNT_IV = "ivfailcount";
    private static final String SPENT_LIMIT_IV = "ivspendlimit";
    private static final String TOTAL_LIMIT_IV = "ivtotallimit";
    private static final String FAIL_TIMESTAMP_IV = "ivfailtimestamp";
    private static final String AUTH_KEY_IV = "ivauthkey";
    private static final String TOKEN_IV = "ivtoken";
    private static final String PASS_TIME_IV = "passtimetoken";

    public static final String PHRASE_ALIAS = "phrase";
    public static final String CANARY_ALIAS = "canary";
    public static final String PUB_KEY_ALIAS = "pubKey";
    public static final String WALLET_CREATION_TIME_ALIAS = "creationTime";
    public static final String PASS_CODE_ALIAS = "passCode";
    public static final String FAIL_COUNT_ALIAS = "failCount";
    public static final String SPEND_LIMIT_ALIAS = "spendlimit";
    public static final String TOTAL_LIMIT_ALIAS = "totallimit";
    public static final String FAIL_TIMESTAMP_ALIAS = "failTimeStamp";
    public static final String AUTH_KEY_ALIAS = "authKey";
    public static final String TOKEN_ALIAS = "token";
    public static final String PASS_TIME_ALIAS = "passTime";

    private static final String PHRASE_FILENAME = "my_phrase";
    private static final String CANARY_FILENAME = "my_canary";
    private static final String PUB_KEY_FILENAME = "my_pub_key";
    private static final String WALLET_CREATION_TIME_FILENAME = "my_creation_time";
    private static final String PASS_CODE_FILENAME = "my_pass_code";
    private static final String FAIL_COUNT_FILENAME = "my_fail_count";
    private static final String SPEND_LIMIT_FILENAME = "my_spend_limit";
    private static final String TOTAL_LIMIT_FILENAME = "my_total_limit";
    private static final String FAIL_TIMESTAMP_FILENAME = "my_fail_timestamp";
    private static final String AUTH_KEY_FILENAME = "my_auth_key";
    private static final String TOKEN_FILENAME = "my_token";
    private static final String PASS_TIME_FILENAME = "my_pass_time";

    private static boolean bugMessageShowing;

    public static final int AUTH_DURATION_SEC = 300;
    private static final ReentrantLock lock = new ReentrantLock();

    static {
        aliasObjectMap = new HashMap<>();
        aliasObjectMap.put(PHRASE_ALIAS, new AliasObject(PHRASE_ALIAS, PHRASE_FILENAME, PHRASE_IV));
        aliasObjectMap.put(CANARY_ALIAS, new AliasObject(CANARY_ALIAS, CANARY_FILENAME, CANARY_IV));
        aliasObjectMap.put(PUB_KEY_ALIAS, new AliasObject(PUB_KEY_ALIAS, PUB_KEY_FILENAME, PUB_KEY_IV));
        aliasObjectMap.put(WALLET_CREATION_TIME_ALIAS, new AliasObject(WALLET_CREATION_TIME_ALIAS, WALLET_CREATION_TIME_FILENAME, WALLET_CREATION_TIME_IV));
        aliasObjectMap.put(PASS_CODE_ALIAS, new AliasObject(PASS_CODE_ALIAS, PASS_CODE_FILENAME, PASS_CODE_IV));
        aliasObjectMap.put(FAIL_COUNT_ALIAS, new AliasObject(FAIL_COUNT_ALIAS, FAIL_COUNT_FILENAME, FAIL_COUNT_IV));
        aliasObjectMap.put(SPEND_LIMIT_ALIAS, new AliasObject(SPEND_LIMIT_ALIAS, SPEND_LIMIT_FILENAME, SPENT_LIMIT_IV));
        aliasObjectMap.put(FAIL_TIMESTAMP_ALIAS, new AliasObject(FAIL_TIMESTAMP_ALIAS, FAIL_TIMESTAMP_FILENAME, FAIL_TIMESTAMP_IV));
        aliasObjectMap.put(AUTH_KEY_ALIAS, new AliasObject(AUTH_KEY_ALIAS, AUTH_KEY_FILENAME, AUTH_KEY_IV));
        aliasObjectMap.put(TOKEN_ALIAS, new AliasObject(TOKEN_ALIAS, TOKEN_FILENAME, TOKEN_IV));
        aliasObjectMap.put(PASS_TIME_ALIAS, new AliasObject(PASS_TIME_ALIAS, PASS_TIME_FILENAME, PASS_TIME_IV));
        aliasObjectMap.put(TOTAL_LIMIT_ALIAS, new AliasObject(TOTAL_LIMIT_ALIAS, TOTAL_LIMIT_FILENAME, TOTAL_LIMIT_IV));

    }


    private synchronized static boolean _setData(Context context, byte[] data, String alias, String alias_file, String alias_iv,
                                                 int request_code, boolean auth_required) throws UserNotAuthenticatedException {
        validateSet(data, alias, alias_file, alias_iv, auth_required);

        try {
            lock.lock();
            KeyStoreManager keyStoreManager = KoinJavaComponent.get(KeyStoreManager.class);
            return keyStoreManager.setDataBlocking(new AliasObject(alias, alias_file, alias_iv), data);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e, "timber:_setData: showAuthenticationScreen: %s", alias);
            showAuthenticationScreen(context, request_code, alias);
            throw e;
        } catch (Exception e) {
            Timber.e(e, "timber:setData: error retrieving");
            FirebaseCrashlytics.getInstance().recordException(e);
            return false;
        } finally {
            lock.unlock();
        }

    }

    private static SecretKey createKeys(String alias, boolean auth_required) throws InvalidAlgorithmParameterException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);

        // Set the alias of the entry in Android KeyStore where the key will appear
        // and the constrains (purposes) in the constructor of the Builder
        keyGenerator.init(new KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(NEW_BLOCK_MODE)
                .setUserAuthenticationRequired(auth_required)
                .setUserAuthenticationValidityDurationSeconds(AUTH_DURATION_SEC)
                .setRandomizedEncryptionRequired(false)
                .setEncryptionPaddings(NEW_PADDING)
                .build());
        return keyGenerator.generateKey();
    }

    private synchronized static byte[] _getData(final Context context, String alias, String alias_file, String alias_iv, int request_code)
            throws UserNotAuthenticatedException {
        validateGet(alias, alias_file, alias_iv);//validate entries

        try {
            lock.lock();
            KeyStoreManager keyStoreManager = KoinJavaComponent.get(KeyStoreManager.class);
            return keyStoreManager.getDataBlocking(new AliasObject(alias, alias_file, alias_iv));
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e, "timber:_getData: showAuthenticationScreen: %s", alias);
            showAuthenticationScreen(context, request_code, alias);
            throw e;
        } catch (Exception e) {
            Timber.e(e, "timber:getData: error retrieving");
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        } finally {
            lock.unlock();
        }

    }

    private static void validateGet(String alias, String alias_file, String alias_iv) throws IllegalArgumentException {
        AliasObject obj = aliasObjectMap.get(alias);
        if (!obj.alias.equals(alias) || !obj.datafileName.equals(alias_file) || !obj.ivFileName.equals(alias_iv)) {
            String err = alias + "|" + alias_file + "|" + alias_iv + ", obj: " + obj.alias + "|" + obj.datafileName + "|" + obj.ivFileName;
            throw new IllegalArgumentException("keystore insert inconsistency in names: " + err);
        }
    }

    private static void validateSet(byte[] data, String alias, String alias_file, String alias_iv, boolean auth_required) throws IllegalArgumentException {
        if (data == null) throw new IllegalArgumentException("keystore insert data is null");
        AliasObject obj = aliasObjectMap.get(alias);
        if (!obj.alias.equals(alias) || !obj.datafileName.equals(alias_file) || !obj.ivFileName.equals(alias_iv)) {
            String err = alias + "|" + alias_file + "|" + alias_iv + ", obj: " + obj.alias + "|" + obj.datafileName + "|" + obj.ivFileName;
            throw new IllegalArgumentException("keystore insert inconsistency in names: " + err);
        }

        if (auth_required)
            if (!alias.equals(PHRASE_ALIAS) && !alias.equals(CANARY_ALIAS))
                throw new IllegalArgumentException("keystore auth_required is true but alias is: " + alias);
    }

    public static void showKeyInvalidated(final Context app) {
        BRDialog.showCustomDialog(app, app.getString(R.string.Alert_keystore_title_android), app.getString(R.string.Alert_keystore_invalidated_android), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
            @Override
            public void onClick(BRDialogView brDialogView) {
                brDialogView.dismissWithAnimation();
            }
        }, null, new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                BRWalletManager.getInstance().wipeWalletButKeystore(app);
                BRWalletManager.getInstance().wipeKeyStore(app);
                dialog.dismiss();
            }
        }, 0);
    }

    public synchronized static String getFilePath(String fileName, Context context) {
        String filesDirectory = context.getFilesDir().getAbsolutePath();
        return filesDirectory + File.separator + fileName;
    }

    public synchronized static boolean putPhrase(byte[] strToStore, Context context, int requestCode) throws UserNotAuthenticatedException {
        if (PostAuth.isStuckWithAuthLoop) {
            showLoopBugMessage(context);
            throw new UserNotAuthenticatedException();
        }
        AliasObject obj = aliasObjectMap.get(PHRASE_ALIAS);
        return !(strToStore == null || strToStore.length == 0) && _setData(context, strToStore, obj.alias, obj.datafileName, obj.ivFileName, requestCode, true);
    }

    public synchronized static byte[] getPhrase(final Context context, int requestCode) throws UserNotAuthenticatedException {
        if (PostAuth.isStuckWithAuthLoop) {
            showLoopBugMessage(context);
            throw new UserNotAuthenticatedException();
        }
        AliasObject obj = aliasObjectMap.get(PHRASE_ALIAS);
        return _getData(context, obj.alias, obj.datafileName, obj.ivFileName, requestCode);
    }

    public synchronized static boolean putCanary(String strToStore, Context context, int requestCode) throws UserNotAuthenticatedException {
        if (PostAuth.isStuckWithAuthLoop) {
            showLoopBugMessage(context);
            throw new UserNotAuthenticatedException();
        }
        if (strToStore == null || strToStore.isEmpty()) return false;
        AliasObject obj = aliasObjectMap.get(CANARY_ALIAS);
        byte[] strBytes = new byte[0];
        try {
            strBytes = strToStore.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Timber.e(e);
        }
        return strBytes.length != 0 && _setData(context, strBytes, obj.alias, obj.datafileName, obj.ivFileName, requestCode, true);
    }

    public synchronized static String getCanary(final Context context, int requestCode) throws UserNotAuthenticatedException {
        if (PostAuth.isStuckWithAuthLoop) {
            showLoopBugMessage(context);
            throw new UserNotAuthenticatedException();
        }
        AliasObject obj = aliasObjectMap.get(CANARY_ALIAS);
        byte[] data;
        data = _getData(context, obj.alias, obj.datafileName, obj.ivFileName, requestCode);
        String result = null;
        try {
            result = data == null ? null : new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Timber.e(e);
        }
        return result;
    }

    public synchronized static boolean putMasterPublicKey(byte[] masterPubKey, Context context) {
        AliasObject obj = aliasObjectMap.get(PUB_KEY_ALIAS);
        try {
            return masterPubKey != null && masterPubKey.length != 0 && _setData(context, masterPubKey, obj.alias, obj.datafileName, obj.ivFileName, 0, false);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        return false;
    }

    public synchronized static byte[] getMasterPublicKey(final Context context) {
        AliasObject obj = aliasObjectMap.get(PUB_KEY_ALIAS);
        try {
            return _getData(context, obj.alias, obj.datafileName, obj.ivFileName, 0);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        return null;
    }

    public synchronized static boolean putAuthKey(byte[] authKey, Context context) {
        AliasObject obj = aliasObjectMap.get(AUTH_KEY_ALIAS);
        try {
            return authKey != null && authKey.length != 0 && _setData(context, authKey, obj.alias, obj.datafileName, obj.ivFileName, 0, false);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        return false;
    }

    public synchronized static byte[] getAuthKey(final Context context) {
        AliasObject obj = aliasObjectMap.get(AUTH_KEY_ALIAS);
        try {
            return _getData(context, obj.alias, obj.datafileName, obj.ivFileName, 0);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        return null;
    }

    public synchronized static boolean putToken(byte[] token, Context context) {
        AliasObject obj = aliasObjectMap.get(TOKEN_ALIAS);
        try {
            return token != null && token.length != 0 && _setData(context, token, obj.alias, obj.datafileName, obj.ivFileName, 0, false);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        return false;
    }

    public synchronized static byte[] getToken(final Context context) {
        AliasObject obj = aliasObjectMap.get(TOKEN_ALIAS);
        try {
            return _getData(context, obj.alias, obj.datafileName, obj.ivFileName, 0);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        return null;
    }

    public synchronized static boolean putWalletCreationTime(int creationTime, Context context) {
        AliasObject obj = aliasObjectMap.get(WALLET_CREATION_TIME_ALIAS);
        byte[] bytesToStore = TypesConverter.intToBytes(creationTime);
        try {
            return bytesToStore.length != 0 && _setData(context, bytesToStore, obj.alias, obj.datafileName, obj.ivFileName, 0, false);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        return false;
    }

    public synchronized static int getWalletCreationTime(final Context context) {
        AliasObject obj = aliasObjectMap.get(WALLET_CREATION_TIME_ALIAS);
        byte[] result = null;
        try {
            result = _getData(context, obj.alias, obj.datafileName, obj.ivFileName, 0);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        if (Utils.isNullOrEmpty(result)) {
            //if none, try getting from KVStore
            WalletInfo info = KVStoreManager.getInstance().getWalletInfo(context);
            if (info != null) {
                int creationDate = info.creationDate;
                putWalletCreationTime(creationDate, context);
                return creationDate;
            } else
                return 0;
        } else {
            return TypesConverter.bytesToInt(result);
        }
    }

    public synchronized static boolean putPinCode(String pinCode, Context context) {
        AliasObject obj = aliasObjectMap.get(PASS_CODE_ALIAS);
        byte[] bytesToStore = pinCode.getBytes();
        try {
            return _setData(context, bytesToStore, obj.alias, obj.datafileName, obj.ivFileName, 0, false);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        return false;
    }

    public synchronized static String getPinCode(final Context context) {
        AliasObject obj = aliasObjectMap.get(PASS_CODE_ALIAS);
        byte[] result = null;
        try {
            result = _getData(context, obj.alias, obj.datafileName, obj.ivFileName, 0);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        String pinCode = result == null ? "" : new String(result);
        try {
            Integer.parseInt(pinCode);
        } catch (Exception e) {
            Timber.e(e, "timber:getPinCode: WARNING passcode isn't a number");
            pinCode = "";
            putPinCode(pinCode, context);
            putFailCount(0, context);
            putFailTimeStamp(0, context);
            return pinCode;
        }
        if (pinCode.length() != 6 && pinCode.length() != 4) {
            pinCode = "";
            putPinCode(pinCode, context);
            putFailCount(0, context);
            putFailTimeStamp(0, context);
        }
        return pinCode;
    }

    public synchronized static boolean putFailCount(int failCount, Context context) {
        AliasObject obj = aliasObjectMap.get(FAIL_COUNT_ALIAS);
        if (failCount >= 3) {
            long time = BRSharedPrefs.getSecureTime(context);
            putFailTimeStamp(time, context);
        }
        byte[] bytesToStore = TypesConverter.intToBytes(failCount);
        try {
            return bytesToStore.length != 0 && _setData(context, bytesToStore, obj.alias, obj.datafileName, obj.ivFileName, 0, false);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        return false;
    }

    public synchronized static int getFailCount(final Context context) {
        AliasObject obj = aliasObjectMap.get(FAIL_COUNT_ALIAS);
        byte[] result = null;
        try {
            result = _getData(context, obj.alias, obj.datafileName, obj.ivFileName, 0);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }

        return result != null && result.length > 0 ? TypesConverter.bytesToInt(result) : 0;
    }

    public synchronized static boolean putSpendLimit(long spendLimit, Context context) {
        AliasObject obj = aliasObjectMap.get(SPEND_LIMIT_ALIAS);
        byte[] bytesToStore = TypesConverter.long2byteArray(spendLimit);
        try {
            return bytesToStore.length != 0 && _setData(context, bytesToStore, obj.alias, obj.datafileName, obj.ivFileName, 0, false);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        return false;
    }

    public synchronized static long getSpendLimit(final Context context) {
        AliasObject obj = aliasObjectMap.get(SPEND_LIMIT_ALIAS);
        byte[] result = null;
        try {
            result = _getData(context, obj.alias, obj.datafileName, obj.ivFileName, 0);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }

        return result != null && result.length > 0 ? TypesConverter.byteArray2long(result) : 0;
    }

    public synchronized static boolean putFailTimeStamp(long spendLimit, Context context) {
        AliasObject obj = aliasObjectMap.get(FAIL_TIMESTAMP_ALIAS);
        byte[] bytesToStore = TypesConverter.long2byteArray(spendLimit);
        try {
            return bytesToStore.length != 0 && _setData(context, bytesToStore, obj.alias, obj.datafileName, obj.ivFileName, 0, false);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        return false;
    }

    public synchronized static long getFailTimeStamp(final Context context) {
        AliasObject obj = aliasObjectMap.get(FAIL_TIMESTAMP_ALIAS);
        byte[] result = null;
        try {
            result = _getData(context, obj.alias, obj.datafileName, obj.ivFileName, 0);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }

        return result != null && result.length > 0 ? TypesConverter.byteArray2long(result) : 0;
    }

    public synchronized static boolean putLastPinUsedTime(long time, Context context) {
        AliasObject obj = aliasObjectMap.get(PASS_TIME_ALIAS);
        byte[] bytesToStore = TypesConverter.long2byteArray(time);
        try {
            return bytesToStore.length != 0 && _setData(context, bytesToStore, obj.alias, obj.datafileName, obj.ivFileName, 0, false);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        return false;
    }

    // WARNING use AuthManager to get the limit
    public synchronized static boolean putTotalLimit(long totalLimit, Context context) {
        AliasObject obj = aliasObjectMap.get(TOTAL_LIMIT_ALIAS);
        byte[] bytesToStore = TypesConverter.long2byteArray(totalLimit);
        try {
            return bytesToStore.length != 0 && _setData(context, bytesToStore, obj.alias, obj.datafileName, obj.ivFileName, 0, false);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        return false;
    }


    // WARNING use AuthManager to set the limit
    public synchronized static long getTotalLimit(final Context context) {
        AliasObject obj = aliasObjectMap.get(TOTAL_LIMIT_ALIAS);
        byte[] result = new byte[0];
        try {
            result = _getData(context, obj.alias, obj.datafileName, obj.ivFileName, 0);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        return (result != null && result.length > 0) ? TypesConverter.byteArray2long(result) : 0;
    }

    public synchronized static long getLastPinUsedTime(final Context context) {
        AliasObject obj = aliasObjectMap.get(PASS_TIME_ALIAS);
        byte[] result = null;
        try {
            result = _getData(context, obj.alias, obj.datafileName, obj.ivFileName, 0);
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e);
        }
        return result != null && result.length > 0 ? TypesConverter.byteArray2long(result) : 0;
    }

    public synchronized static boolean resetWalletKeyStore(Context context) {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            int count = 0;
            while (keyStore.aliases().hasMoreElements()) {
                String alias = keyStore.aliases().nextElement();
                removeAliasAndFiles(keyStore, alias, context);
                destroyEncryptedData(context, alias);
                count++;
            }
            Timber.d("timber: resetWalletKeyStore: removed:%s", count);
        } catch (NoSuchAlgorithmException | KeyStoreException | IOException e) {
            Timber.e(e);
            return false;
        } catch (CertificateException e) {
            Timber.e(e);
        }
        return true;
    }

    public synchronized static void removeAliasAndFiles(KeyStore keyStore, String alias, Context context) {
        try {
            keyStore.deleteEntry(alias);

            AliasObject aliasObject = aliasObjectMap.get(alias);
            if (aliasObject == null) {
                Timber.w("aliasObject for alias: %s is null, skipping deletion", alias);
                return;
            }

            boolean b1 = new File(getFilePath(aliasObject.datafileName, context)).delete();
            boolean b2 = new File(getFilePath(aliasObject.ivFileName, context)).delete();
        } catch (KeyStoreException e) {
            Timber.e(e);
        }
    }

    public static void storeEncryptedData(Context ctx, byte[] data, String name) {
        SharedPreferences pref = ctx.getSharedPreferences(KEY_STORE_PREFS_NAME, Context.MODE_PRIVATE);
        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(name, base64);
        edit.apply();
    }

    public static void destroyEncryptedData(Context ctx, String name) {
        SharedPreferences pref = ctx.getSharedPreferences(KEY_STORE_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(name);
        edit.apply();
    }

    public static byte[] retrieveEncryptedData(Context ctx, String name) {
        SharedPreferences pref = ctx.getSharedPreferences(KEY_STORE_PREFS_NAME, Context.MODE_PRIVATE);
        String base64 = pref.getString(name, null);
        if (base64 == null) return null;
        return Base64.decode(base64, Base64.DEFAULT);
    }

    public synchronized static void showAuthenticationScreen(Context context, int requestCode, String alias) {
        // Create the Confirm Credentials screen. You can customize the title and description. Or
        // we will provide a generic one for you if you leave it null
        if (!alias.equalsIgnoreCase(PHRASE_ALIAS) && !alias.equalsIgnoreCase(CANARY_ALIAS)) {
            IllegalArgumentException ex = new IllegalArgumentException("requesting auth for: " + alias);
            Timber.e(ex);
            throw ex;
        }
        if (context instanceof Activity) {
            Activity app = (Activity) context;
            KeyguardManager mKeyguardManager = (KeyguardManager) app.getSystemService(Context.KEYGUARD_SERVICE);
            if (mKeyguardManager == null) {
                NullPointerException ex = new NullPointerException("KeyguardManager is null in showAuthenticationScreen");
                Timber.e(ex);
                throw ex;
            }
            String message = context.getString(R.string.UnlockScreen_touchIdPrompt_android);
            if (Utils.isEmulatorOrDebug(app)) {
                message = alias;
            }
            Intent intent = mKeyguardManager.createConfirmDeviceCredentialIntent(context.getString(R.string.UnlockScreen_touchIdTitle_android), message);

            if (Utils.isEmulatorOrDebug(context))
                intent = mKeyguardManager.createConfirmDeviceCredentialIntent(alias, context.getString(R.string.UnlockScreen_touchIdPrompt_android));
            if (intent != null) {
                app.startActivityForResult(intent, requestCode);
            } else {
                Timber.e(new RuntimeException("showAuthenticationScreen: failed to create intent for auth"));
                app.finish();
            }
        } else {
            Timber.e(new RuntimeException("showAuthenticationScreen: context is not activity!"));
        }
    }

    public static byte[] readBytesFromFile(String path) {
        byte[] bytes = null;
        FileInputStream fin;
        try {
            File file = new File(path);
            fin = new FileInputStream(file);
            bytes = BytesUtil.readBytesFromStream(fin);
        } catch (IOException e) {
            Timber.e(e);
        }
        return bytes;
    }

    //USE ONLY FOR TESTING
    public synchronized static boolean _setOldData(Context context, byte[] data, String alias, String alias_file, String alias_iv,
                                                   int request_code, boolean auth_required) throws UserNotAuthenticatedException {
        try {
            validateSet(data, alias, alias_file, alias_iv, auth_required);
        } catch (Exception e) {
            Timber.e(e, "timber:_setData: ");
        }

        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            // Create the keys if necessary
            if (!keyStore.containsAlias(alias)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);

                // Set the alias of the entry in Android KeyStore where the key will appear
                // and the constrains (purposes) in the constructor of the Builder
                keyGenerator.init(new KeyGenParameterSpec.Builder(alias,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(BLOCK_MODE)
                        .setKeySize(256)
                        .setUserAuthenticationRequired(auth_required)
                        .setUserAuthenticationValidityDurationSeconds(AUTH_DURATION_SEC)
                        .setRandomizedEncryptionRequired(true)
                        .setEncryptionPaddings(PADDING)
                        .build());
                SecretKey key = keyGenerator.generateKey();
            }

            String encryptedDataFilePath = getFilePath(alias_file, context);

            SecretKey secret = (SecretKey) keyStore.getKey(alias, null);
            if (secret == null) {
                Timber.d("timber: _setOldData: " + "secret is null on _setData: " + alias);
                return false;
            }
            Cipher inCipher = Cipher.getInstance(CIPHER_ALGORITHM);
            inCipher.init(Cipher.ENCRYPT_MODE, secret);
            byte[] iv = inCipher.getIV();
            String path = getFilePath(alias_iv, context);
            boolean success = writeBytesToFile(path, iv);
            if (!success) {
                Timber.d("timber: _setOldData: " + "failed to writeBytesToFile: " + alias);
                BRDialog.showCustomDialog(context, context.getString(R.string.Alert_keystore_title_android), "Failed to save the iv file for: " + alias, "close", null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismissWithAnimation();
                    }
                }, null, null, 0);
                keyStore.deleteEntry(alias);
                return false;
            }
            CipherOutputStream cipherOutputStream = null;
            try {
                cipherOutputStream = new CipherOutputStream(
                        new FileOutputStream(encryptedDataFilePath), inCipher);
                cipherOutputStream.write(data);
            } catch (Exception ex) {
                Timber.e(ex);
            } finally {
                if (cipherOutputStream != null) cipherOutputStream.close();
            }
            return true;
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e, "timber:_setOldData: showAuthenticationScreen: " + alias);
            showAuthenticationScreen(context, request_code, alias);
            throw e;
        } catch (Exception e) {
            Timber.e(e, "timber:_setOldData: ");
            return false;
        }
    }

    //USE ONLY FOR TESTING
    public synchronized static byte[] _getOldData(final Context context, String alias, String alias_file, String alias_iv, int request_code)
            throws UserNotAuthenticatedException {
        try {
            validateGet(alias, alias_file, alias_iv);
        } catch (Exception e) {
            Timber.e(e, "timber:_getOldData");
        }

        KeyStore keyStore;

        String encryptedDataFilePath = getFilePath(alias_file, context);
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey(alias, null);
            if (secretKey == null) {
                /* no such key, the key is just simply not there */
                boolean fileExists = new File(encryptedDataFilePath).exists();
                if (!fileExists) {
                    return null;/* file also not there, fine then */
                }
                Timber.i("timber: _getOldData: file is present but the key is gone: %s", alias);
                return null;
            }

            boolean ivExists = new File(getFilePath(alias_iv, context)).exists();
            boolean aliasExists = new File(getFilePath(alias_file, context)).exists();
            if (!ivExists || !aliasExists) {
                removeAliasAndFiles(keyStore, alias, context);
                //report it if one exists and not the other.
                if (ivExists != aliasExists) {
                    Timber.d("timber: _getOldData: " + "alias or iv isn't on the disk: " + alias + ", aliasExists:" + aliasExists);
                    return null;
                } else {
                    Timber.d("timber: _getOldData: " + "!ivExists && !aliasExists: " + alias);
                    return null;
                }
            }

            byte[] iv = readBytesFromFile(getFilePath(alias_iv, context));
            if (Utils.isNullOrEmpty(iv))
                throw new NullPointerException("iv is missing for " + alias);
            Cipher outCipher;
            outCipher = Cipher.getInstance(CIPHER_ALGORITHM);
            outCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            CipherInputStream cipherInputStream = new CipherInputStream(new FileInputStream(encryptedDataFilePath), outCipher);
            return BytesUtil.readBytesFromStream(cipherInputStream);
        } catch (InvalidKeyException e) {
            if (e instanceof UserNotAuthenticatedException) {
                /** user not authenticated, ask the system for authentication */
                Timber.e(e, "timber:_getOldData: showAuthenticationScreen: %s", alias);
                showAuthenticationScreen(context, request_code, alias);
                throw (UserNotAuthenticatedException) e;
            } else {
                Timber.e(e, "timber:_getOldData: InvalidKeyException");
                return null;
            }
        } catch (IOException | CertificateException | KeyStoreException e) {
            /** keyStore.load(null) threw the Exception, meaning the keystore is unavailable */
            Timber.e(e, "timber:_getOldData: keyStore.load(null) threw the Exception, meaning the keystore is unavailable");
            return null;
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | NoSuchPaddingException |
                 InvalidAlgorithmParameterException e) {
            /** if for any other reason the keystore fails, crash! */
            Timber.e(e, "timber:getData: error");
            return null;
        }
    }

    private static void showLoopBugMessage(final Context app) {
        if (bugMessageShowing) return;
        bugMessageShowing = true;
        Timber.d("timber: showLoopBugMessage: ");
        String mess = app.getString(R.string.ErrorMessages_loopingLockScreen_android);

        SpannableString ss = new SpannableString(mess.replace("[", "").replace("]", ""));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Timber.d("timber: onClick: clicked on span!");
                BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        BRDialog.hideDialog();
                    }
                });
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, mess.indexOf("[") - 1, mess.indexOf("]") - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        BRDialog.showCustomDialog(app, app.getString(R.string.JailbreakWarnings_title), ss, app.getString(R.string.AccessibilityLabels_close), null,
                new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        if (app instanceof Activity) ((Activity) app).finish();
                    }
                }, null, new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        bugMessageShowing = false;
                    }
                }, 0);
    }

    public static boolean writeBytesToFile(String path, byte[] data) {
        FileOutputStream fos = null;
        try {
            File file = new File(path);
            fos = new FileOutputStream(file);
            // Writes bytes from the specified byte array to this file output stream
            fos.write(data);
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while writing file " + ioe);
        } finally {
            // close the streams using close method
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ioe) {
                Timber.e(ioe, "Error while closing stream");
            }
        }
        return false;
    }

    public static class AliasObject {
        public String alias;
        public String datafileName;
        public String ivFileName;

        AliasObject(String alias, String datafileName, String ivFileName) {
            this.alias = alias;
            this.datafileName = datafileName;
            this.ivFileName = ivFileName;
        }
    }
}
