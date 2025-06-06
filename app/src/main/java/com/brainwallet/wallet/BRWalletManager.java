package com.brainwallet.wallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.NetworkOnMainThreadException;
import android.os.SystemClock;
import android.security.keystore.UserNotAuthenticatedException;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.WorkerThread;
import androidx.fragment.app.FragmentActivity;

import com.brainwallet.BrainwalletApp;
import com.brainwallet.R;
import com.brainwallet.presenter.activities.BreadActivity;
import com.brainwallet.presenter.activities.util.ActivityUTILS;
import com.brainwallet.presenter.customviews.BRDialogView;
import com.brainwallet.presenter.customviews.BRToast;
import com.brainwallet.presenter.entities.BRMerkleBlockEntity;
import com.brainwallet.presenter.entities.BRPeerEntity;
import com.brainwallet.presenter.entities.BRTransactionEntity;
import com.brainwallet.presenter.entities.ImportPrivKeyEntity;
import com.brainwallet.presenter.entities.TxItem;
import com.brainwallet.presenter.interfaces.BROnSignalCompletion;
import com.brainwallet.tools.animation.BRAnimator;
import com.brainwallet.tools.animation.BRDialog;
import com.brainwallet.tools.animation.SpringAnimator;
import com.brainwallet.tools.manager.AnalyticsManager;
import com.brainwallet.tools.manager.BRNotificationManager;
import com.brainwallet.tools.manager.BRSharedPrefs;
import com.brainwallet.tools.manager.FeeManager;
import com.brainwallet.tools.security.BRKeyStore;
import com.brainwallet.tools.sqlite.MerkleBlockDataSource;
import com.brainwallet.tools.sqlite.PeerDataSource;
import com.brainwallet.tools.sqlite.TransactionDataSource;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.tools.threads.ImportPrivKeyTask;
import com.brainwallet.tools.util.BRConstants;
import com.brainwallet.tools.util.BRCurrency;
import com.brainwallet.tools.util.BRExchange;
import com.brainwallet.tools.util.Bip39Reader;
import com.brainwallet.tools.util.TypesConverter;
import com.brainwallet.tools.util.Utils;
import com.platform.entities.WalletInfo;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class BRWalletManager {
    private static BRWalletManager instance;
    public List<OnBalanceChanged> balanceListeners = new ArrayList<>();
    private boolean itInitiatingWallet;

    public void setBalance(final Context context, long balance) {
        if (context == null) {
            Timber.i("timber: setBalance: FAILED TO SET THE BALANCE NULL context");
            return;
        }
        BRSharedPrefs.putCatchedBalance(context, balance);
        refreshAddress(context);

        for (OnBalanceChanged listener : balanceListeners) {
            if (listener != null) listener.onBalanceChanged(balance);
        }
    }

    public void refreshBalance(Activity app) {
        long nativeBalance = nativeBalance();
        if (nativeBalance != -1) {
            setBalance(app, nativeBalance);
        } else {
            Timber.i("timber: UpdateUI, nativeBalance is -1 meaning _wallet was null!");
        }
    }

    public long getBalance(Context context) {
        return BRSharedPrefs.getCatchedBalance(context);
    }

    private BRWalletManager() {
    }

    public static BRWalletManager getInstance() {
        if (instance == null) {
            instance = new BRWalletManager();
        }
        return instance;
    }

    public synchronized boolean generateRandomSeed(final Context ctx) {
        SecureRandom sr = new SecureRandom();
        final String[] words;
        List<String> list;
        String languageCode = "en";
        list = Bip39Reader.bip39List(ctx, "en");
        words = list.toArray(new String[list.size()]);
        final byte[] randomSeed = sr.generateSeed(16);
        if (words.length != 2048) {
            IllegalArgumentException ex = new IllegalArgumentException("the list is wrong, size: " + words.length);
            Timber.e(ex);
            throw ex;
        }
        if (randomSeed.length != 16)
            throw new NullPointerException("failed to create the seed, seed length is not 128: " + randomSeed.length);
        byte[] strPhrase = encodeSeed(randomSeed, words);
        if (strPhrase == null || strPhrase.length == 0) {
            NullPointerException ex = new NullPointerException("failed to encodeSeed");
            Timber.e(ex);
            throw ex;
        }
        String[] splitPhrase = new String(strPhrase).split(" ");
        if (splitPhrase.length != 12) {
            NullPointerException ex = new NullPointerException("phrase does not have 12 words:" + splitPhrase.length + ", lang: " + languageCode);
            Timber.e(ex);
            throw ex;
        }

        boolean success;
        try {
            success = BRKeyStore.putPhrase(strPhrase, ctx, BRConstants.PUT_PHRASE_NEW_WALLET_REQUEST_CODE);
        } catch (UserNotAuthenticatedException e) {
            return false;
        }
        if (!success) return false;
        byte[] phrase;
        try {
            phrase = BRKeyStore.getPhrase(ctx, 0);
        } catch (UserNotAuthenticatedException e) {
            throw new RuntimeException("Failed to retrieve the phrase even though at this point the system auth was asked for sure.");
        }
        if (Utils.isNullOrEmpty(phrase)) throw new NullPointerException("phrase is null!!");
        byte[] nulTermPhrase = TypesConverter.getNullTerminatedPhrase(phrase);
        if (nulTermPhrase == null || nulTermPhrase.length == 0)
            throw new RuntimeException("nulTermPhrase is null");
        byte[] seed = getSeedFromPhrase(nulTermPhrase);
        if (seed == null || seed.length == 0) throw new RuntimeException("seed is null");
        byte[] authKey = getAuthPrivKeyForAPI(seed);
        if (authKey == null || authKey.length == 0) {
            IllegalArgumentException ex = new IllegalArgumentException("authKey is invalid");
            Timber.e(ex);
            throw ex;
        }
        BRKeyStore.putAuthKey(authKey, ctx);
        int walletCreationTime = (int) (System.currentTimeMillis() / 1000);
        BRKeyStore.putWalletCreationTime(walletCreationTime, ctx);
        final WalletInfo info = new WalletInfo();
        info.creationDate = walletCreationTime;

        byte[] strBytes = TypesConverter.getNullTerminatedPhrase(strPhrase);
        byte[] pubKey = BRWalletManager.getInstance().getMasterPubKey(strBytes);
        BRKeyStore.putMasterPublicKey(pubKey, ctx);

        return true;
    }

    public boolean wipeKeyStore(Context context) {
        Timber.d("timber: wipeKeyStore");
        return BRKeyStore.resetWalletKeyStore(context);
    }

    /**
     * true if keystore is available and we know that no wallet exists on it
     */
    public boolean noWallet(Context ctx) {
        byte[] pubkey = BRKeyStore.getMasterPublicKey(ctx);

        if (pubkey == null || pubkey.length == 0) {
            byte[] phrase;
            try {
                phrase = BRKeyStore.getPhrase(ctx, 0);
                //if not authenticated, an error will be thrown and returned false, so no worry about mistakenly removing the wallet
                if (phrase == null || phrase.length == 0) {
                    return true;
                }
            } catch (UserNotAuthenticatedException e) {
                return false;
            }
        }
        return false;
    }

    public boolean noWalletForPlatform(Context ctx) {
        byte[] pubkey = BRKeyStore.getMasterPublicKey(ctx);
        return pubkey == null || pubkey.length == 0;
    }

    /**
     * true if device passcode is enabled
     */
    public boolean isPasscodeEnabled(Context ctx) {
        KeyguardManager keyguardManager = (KeyguardManager) ctx.getSystemService(Activity.KEYGUARD_SERVICE);
        return keyguardManager.isKeyguardSecure();
    }

    public boolean isNetworkAvailable(Context ctx) {
        if (ctx == null) return false;
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();

    }

    public static boolean refreshAddress(Context ctx) {
        String address = getReceiveAddress();
        if (Utils.isNullOrEmpty(address)) {
            Timber.d("timber: refreshAddress: WARNING, retrieved address:%s", address);
            return false;
        }
        BRSharedPrefs.putReceiveAddress(ctx, address);
        return true;
    }

    public void wipeWalletButKeystore(final Context ctx) {
        Timber.d("timber: wipeWalletButKeystore");
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                Timber.d("timber: Running peerManagerFreeEverything");
                BRSharedPrefs.clearAllPrefs(ctx);
                BRPeerManager.getInstance().peerManagerFreeEverything();
                walletFreeEverything();
                TransactionDataSource.getInstance(ctx).deleteAllTransactions();
                MerkleBlockDataSource.getInstance(ctx).deleteAllBlocks();
                PeerDataSource.getInstance(ctx).deleteAllPeers();
            }
        });
    }

    public void wipeAll(Context app) {
        wipeKeyStore(app);
        wipeWalletButKeystore(app);
    }

    public boolean confirmSweep(final Context ctx, final String privKey) {
        if (ctx == null) return false;
        if (isValidBitcoinBIP38Key(privKey)) {
            Timber.d("timber: isValidBitcoinBIP38Key true");
            ((Activity) ctx).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
//                    builder.setTitle("password protected key");

                    final View input = ((Activity) ctx).getLayoutInflater().inflate(R.layout.view_bip38password_dialog, null);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    builder.setView(input);

                    final EditText editText = (EditText) input.findViewById(R.id.bip38password_edittext);

                    (new Handler()).postDelayed(new Runnable() {
                        public void run() {
                            editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                            editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));

                        }
                    }, 100);

                    // Set up the buttons
                    builder.setPositiveButton(ctx.getString(R.string.Button_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (ctx != null)
                                ((Activity) ctx).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        BRToast.showCustomToast(ctx, ctx.getString(R.string.Import_checking), 500, Toast.LENGTH_LONG, R.drawable.toast_layout_blue);
                                    }
                                });
                            if (editText == null) {
                                Timber.d("timber: onClick: edit text is null!");
                                return;
                            }

                            final String pass = editText.getText().toString();
                            Timber.d("timber: onClick: before");
                            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                                @Override
                                public void run() {
                                    String decryptedKey = decryptBip38Key(privKey, pass);
                                    Timber.d("timber: onClick: after");

                                    if (decryptedKey.equals("")) {
                                        SpringAnimator.springView(input);
                                        confirmSweep(ctx, privKey);
                                    } else {
                                        confirmSweep(ctx, decryptedKey);
                                    }
                                }
                            });

                        }
                    });
                    builder.setNegativeButton(ctx.getString(R.string.Button_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            });
            return true;
        } else if (isValidBitcoinPrivateKey(privKey)) {
            Timber.d("timber: isValidBitcoinPrivateKey true");
            new ImportPrivKeyTask(((Activity) ctx)).execute(privKey);
            return true;
        } else {
            Timber.d("timber: confirmSweep: !isValidBitcoinPrivateKey && !isValidBitcoinBIP38Key");
            return false;
        }
    }


    /**
     * Wallet callbacks
     */
    public static void publishCallback(final String message, final int error, byte[] txHash) {
        Timber.d("timber: publishCallback: " + message + ", err:" + error + ", txHash: " + Arrays.toString(txHash));

        Bundle params = new Bundle();
        params.putString("function", "BRWalletManager.publishCallback");
        params.putString("message", message);
        params.putInt("error", error);
        params.putString("txHash", Arrays.toString(txHash));
        AnalyticsManager.logCustomEventWithParams(BRConstants._20250517_WCINFO, params);

        final Context app = BrainwalletApp.getBreadContext();
        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (app instanceof FragmentActivity)
                            BRAnimator.showBreadSignal((FragmentActivity) app, error == 0 ? app.getString(R.string.Alerts_sendSuccess) : app.getString(R.string.Alert_error),
                                    error == 0 ? app.getString(R.string.Alerts_sendSuccessSubheader) : message, error == 0 ? R.drawable.ic_check_mark_white : R.drawable.ic_error_outline_black_24dp, new BROnSignalCompletion() {
                                        @Override
                                        public void onComplete() {
                                            /**
                                             * no-op, since the fragment already closed, please check [FragmentSignal]
                                             */
                                        }
                                    });
                    }
                }, 500);
            }
        });

    }

    public static void onBalanceChanged(final long balance) {
        Timber.d("timber: onBalanceChanged:  " + balance);
        Context app = BrainwalletApp.getBreadContext();
        BRWalletManager.getInstance().setBalance(app, balance);

    }

    public static void onTxAdded(byte[] tx, int blockHeight, long timestamp, final long amount, String hash) {

        // DEV Uncomment to see values
        // Timber.d("timber: onTxAdded: tx.length: %d, blockHeight: %d, timestamp: %d, amount: %d, hash: %s", tx.length, blockHeight, timestamp, amount, hash));

        final Context ctx = BrainwalletApp.getBreadContext();
        if (amount > 0) {
            BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                @Override
                public void run() {
                    String am = BRCurrency.getFormattedCurrencyString(ctx, "LTC", BRExchange.getLitecoinForLitoshis(ctx, new BigDecimal(amount)));
                    String amCur = BRCurrency.getFormattedCurrencyString(ctx, BRSharedPrefs.getIsoSymbol(ctx), BRExchange.getAmountFromLitoshis(ctx, BRSharedPrefs.getIsoSymbol(ctx), new BigDecimal(amount)));
                    String formatted = String.format("%s (%s)", am, amCur);
                    String strToShow = String.format(ctx.getString(R.string.TransactionDetails_received), formatted);
                    showToastWithMessage(ctx, strToShow);
                }
            });
        }
        if (ctx != null)
            TransactionDataSource.getInstance(ctx).putTransaction(new BRTransactionEntity(tx, blockHeight, timestamp, hash));
        else
            Timber.i("timber: onTxAdded: ctx is null!");
    }

    private static void showToastWithMessage(Context ctx, final String message) {
        if (ctx == null) ctx = BrainwalletApp.getBreadContext();
        if (ctx != null) {
            final Context finalCtx = ctx;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!BRToast.isToastShown()) {
                        BRToast.showCustomToast(finalCtx, message,
                                BrainwalletApp.DISPLAY_HEIGHT_PX / 2, Toast.LENGTH_LONG, R.drawable.toast_layout_black);
                        AudioManager audioManager = (AudioManager) finalCtx.getSystemService(Context.AUDIO_SERVICE);
                        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                            final MediaPlayer mp = MediaPlayer.create(finalCtx, R.raw.coinflip);
                            if (mp != null) try {
                                mp.start();
                            } catch (IllegalArgumentException ex) {
                                Timber.e(ex, "run: ");
                            }
                        }

                        if (!BreadActivity.appVisible && BRSharedPrefs.getShowNotification(finalCtx))
                            BRNotificationManager.sendNotification(finalCtx, R.drawable.notification_icon, finalCtx.getString(R.string.app_name), message, 1);
                    }
                }
            }, 1000);


        } else {
            Timber.i("timber: showToastWithMessage: failed, ctx is null");
        }
    }

    public static void onTxUpdated(String hash, int blockHeight, int timeStamp) {
        // DEV Uncomment to see values
        // Timber.d("timber: onTxUpdated: " + String.format("hash: %s, blockHeight: %d, timestamp: %d", hash, blockHeight, timeStamp));
        Context ctx;
        ctx = BrainwalletApp.getBreadContext();
        if (ctx != null) {
            TransactionDataSource.getInstance(ctx).updateTxBlockHeight(hash, blockHeight, timeStamp);

        } else {
            Timber.i("timber: onTxUpdated: Failed, ctx is null");
        }
    }

    public static void onTxDeleted(String hash, int notifyUser, final int recommendRescan) {
        // DEV Uncomment to see values
        // Timber.d("timber: onTxDeleted: " + String.format("hash: %s, notifyUser: %d, recommendRescan: %d", hash, notifyUser, recommendRescan));
        final Context ctx = BrainwalletApp.getBreadContext();
        if (ctx != null) {
            BRSharedPrefs.putScanRecommended(ctx, true);
            TransactionDataSource.getInstance(ctx).deleteTxByHash(hash);
        } else {
            Timber.i("timber: onTxDeleted: Failed! ctx is null");
        }
    }

    public void startTheWalletIfExists(final Activity app) {
        final BRWalletManager m = BRWalletManager.getInstance();
        if (!m.isPasscodeEnabled(app)) {
            //Device passcode/password should be enabled for the app to work
            BRDialog.showCustomDialog(app, app.getString(R.string.JailbreakWarnings_title), app.getString(R.string.Prompts_NoScreenLock_body_android),
                    app.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            app.finish();
                        }
                    }, null, new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            app.finish();
                        }
                    }, 0);
        } else {
            if (!m.noWallet(app) && BRSharedPrefs.getPhraseWroteDown(app)) {
                BRAnimator.startBreadActivity(app, true);
            }
            //else just sit in the intro screen

        }
    }

    @WorkerThread
    public void initWallet(final Context ctx) {
        if (ActivityUTILS.isMainThread()) throw new NetworkOnMainThreadException();
        if (itInitiatingWallet) {
            AnalyticsManager.logCustomEvent(BRConstants._20200111_WNI);
            return;
        }

        itInitiatingWallet = true;

        try {
            Timber.d("timber: initWallet:%s", Thread.currentThread().getName());
            if (ctx == null) {
                Timber.i("timber: initWallet: ctx is null");
                return;
            }
            BRWalletManager m = BRWalletManager.getInstance();
            final BRPeerManager pm = BRPeerManager.getInstance();
            double fpRate = BRSharedPrefs.getFalsePositivesRate(ctx);

            Timber.d("timber: Showing seed fragment");

            if (!m.isCreated()) {
                List<BRTransactionEntity> transactions = TransactionDataSource.getInstance(ctx).getAllTransactions();
                Timber.d("timber: All transactions : %d", transactions.size());

                int transactionsCount = transactions.size();
                if (transactionsCount > 0) {
                    m.createTxArrayWithCount(transactionsCount);
                    for (BRTransactionEntity entity : transactions) {
                        m.putTransaction(entity.getBuff(), entity.getBlockheight(), entity.getTimestamp());
                    }
                }

                byte[] pubkeyEncoded = BRKeyStore.getMasterPublicKey(ctx);
                if (Utils.isNullOrEmpty(pubkeyEncoded)) {
                    Timber.i("timber: initWallet: pubkey is missing");
                    return;
                }
                //Save the first address for future check
                m.createWallet(transactionsCount, pubkeyEncoded);
                String firstAddress = BRWalletManager.getFirstAddress(pubkeyEncoded);
                BRSharedPrefs.putFirstAddress(ctx, firstAddress);
                //set fee here
                BRWalletManager.getInstance().setFeePerKb(FeeManager.getInstance().getCurrentFeeValue());
            }

            if (!pm.isCreated()) {
                List<BRMerkleBlockEntity> blocks = MerkleBlockDataSource.getInstance(ctx).getAllMerkleBlocks();
                List<BRPeerEntity> peers = PeerDataSource.getInstance(ctx).getAllPeers();
                final int blocksCount = blocks.size();
                final int peersCount = peers.size();
                if (blocksCount > 0) {
                    pm.createBlockArrayWithCount(blocksCount);
                    for (BRMerkleBlockEntity entity : blocks) {
                        pm.putBlock(entity.getBuff(), entity.getBlockHeight());
                    }
                }
                if (peersCount > 0) {
                    pm.createPeerArrayWithCount(peersCount);
                    for (BRPeerEntity entity : peers) {
                        pm.putPeer(entity.getAddress(), entity.getPort(), entity.getTimeStamp());
                    }
                }
                Timber.d("timber: blocksCount before connecting: %s", blocksCount);
                Timber.d("timber: peersCount before connecting: %s", peersCount);

                int walletTime = BRKeyStore.getWalletCreationTime(ctx);

                Timber.d("timber: initWallet: walletTime: %s user preferred fpRate: %f", walletTime, fpRate);
                pm.create(walletTime, blocksCount, peersCount, fpRate);
                BRPeerManager.getInstance().updateFixedPeer(ctx);
            }

            pm.wrapConnectV2();
            if (BRSharedPrefs.getStartHeight(ctx) == 0) {
                BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        BRSharedPrefs.putStartHeight(ctx, BRPeerManager.getCurrentBlockHeight());
                    }
                });
            }


        } finally {
            itInitiatingWallet = false;
        }
    }

    public void addBalanceChangedListener(OnBalanceChanged listener) {
        if (balanceListeners.contains(listener)) return;
        balanceListeners.add(listener);
    }

    public void removeListener(OnBalanceChanged listener) {
        balanceListeners.remove(listener);
    }

    public String getSeedPhrase(Context context) {
        byte[] phraseBytes;
        try {
            phraseBytes = BRKeyStore.getPhrase(context, BRConstants.PUT_PHRASE_NEW_WALLET_REQUEST_CODE);
        } catch (UserNotAuthenticatedException e) {
            phraseBytes = new byte[0];
        }
        return new String(phraseBytes);
    }

    public interface OnBalanceChanged {
        void onBalanceChanged(long balance);
    }

    public void setFeePerKb(long fee) {
        setFeePerKb(fee, false);
    }

    private native byte[] encodeSeed(byte[] seed, String[] wordList);

    public native void createWallet(int transactionCount, byte[] pubkey);

    public native void putTransaction(byte[] transaction, long blockHeight, long timeStamp);

    public native void createTxArrayWithCount(int count);

    public native byte[] getMasterPubKey(byte[] normalizedString);

    public static native String getReceiveAddress();

    public native TxItem[] getTransactions();

    public static native boolean validateAddress(String address);

    public native boolean addressContainedInWallet(String address);

    public native boolean addressIsUsed(String address);

    public native int feeForTransaction(String addressHolder, long amountHolder);

    public native int feeForTransactionAmount(long amountHolder);

    public native long getMinOutputAmount();

    public native long getMaxOutputAmount();

    public native boolean isCreated();

    public native byte[] tryTransaction(String addressHolder, long amountHolder);

    public native byte[] tryTransactionWithOps(String sendAddress, long sendAmount, String opsAddress, long opsFeeAmount);

    // returns the given amount (amount is in satoshis) in local currency units (i.e. pennies, pence)
    // price is local currency units per bitcoin
    public native long localAmount(long amount, double price);

    // returns the given local currency amount in satoshis
    // price is local currency units (i.e. pennies, pence) per bitcoin
    public native long bitcoinAmount(long localAmount, double price);

    public native void walletFreeEverything();

    public native boolean validateRecoveryPhrase(String[] words, String phrase);

    public native static String getFirstAddress(byte[] mpk);

    public native byte[] publishSerializedTransaction(byte[] serializedTransaction, byte[] phrase);

    public native long getTotalSent();

    public native long setFeePerKb(long fee, boolean ignore);

    public native boolean isValidBitcoinPrivateKey(String key);

    public native boolean isValidBitcoinBIP38Key(String key);

    public native String getAddressFromPrivKey(String key);

    public native void createInputArray();

    public native void addInputToPrivKeyTx(byte[] hash, int vout, byte[] script, long amount);

    public native boolean confirmKeySweep(byte[] tx, String key);

    public native ImportPrivKeyEntity getPrivKeyObject();

    public native String decryptBip38Key(String privKey, String pass);

    public native String reverseTxHash(String txHash);

    public native String txHashToHex(byte[] txHash);

    public native long nativeBalance();

    public native long defaultFee();

    public native long maxFee();

    public native int getTxCount();

    public native long getMinOutputAmountRequested();

    public static native byte[] getAuthPrivKeyForAPI(byte[] seed);

    public static native String getAuthPublicKeyForAPI(byte[] privKey);

    public static native byte[] getSeedFromPhrase(byte[] phrase);

    public static native boolean isTestNet();

    public static native byte[] sweepBCash(byte[] pubKey, String address, byte[] phrase);

    public static native long getBCashBalance(byte[] pubKey);

    public static native int getTxSize(byte[] serializedTx);


}
