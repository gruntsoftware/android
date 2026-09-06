package com.brainwallet.wallet;

import static com.brainwallet.data.source.RemoteConfigSource.KEY_FEATURE_SELECTED_PEERS_ENABLED;

import android.content.Context;
import android.security.keystore.UserNotAuthenticatedException;

import com.brainwallet.BrainwalletApp;
import com.brainwallet.data.model.LtcStats;
import com.brainwallet.data.repository.SelectedPeersRepository;
import com.brainwallet.data.source.RemoteConfigSource;
import com.brainwallet.presenter.entities.BlockEntity;
import com.brainwallet.presenter.entities.PeerEntity;
import com.brainwallet.tools.manager.BRSharedPrefs;
import com.brainwallet.tools.manager.sync.SyncThreadManager;
import com.brainwallet.tools.security.BRKeyStore;
import com.brainwallet.tools.sqlite.MerkleBlockDataSource;
import com.brainwallet.tools.sqlite.PeerDataSource;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.tools.util.TrustedNode;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.koin.java.KoinJavaComponent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import kotlin.Suppress;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.future.FutureKt;
import timber.log.Timber;

public final class BRPeerManager {

    private static final List<OnTxStatusUpdate> statusUpdateListeners = new ArrayList<>();
    private static OnSyncSucceeded onSyncFinished;

    static long syncStartDate = new Date().getTime();
    static long syncCompletedDate = new Date().getTime();

    private BRPeerManager() {
    }

    // Initialization-on-demand holder: the JVM guarantees InstanceHolder is only
    // classloaded (and INSTANCE only constructed) on the first call to getInstance(),
    // and that this happens exactly once even under concurrent callers - no explicit
    // synchronization needed, unlike the previous unsynchronized lazy-init.
    private static final class InstanceHolder {
        static final BRPeerManager INSTANCE = new BRPeerManager();
    }

    public static BRPeerManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * void BRPeerManagerSetCallbacks(BRPeerManager *manager, void *info,
     * void (*syncStarted)(void *info),
     * void (*syncSucceeded)(void *info),
     * void (*syncFailed)(void *info, BRPeerManagerError error),
     * void (*txStatusUpdate)(void *info),
     * void (*saveBlocks)(void *info, const BRMerkleBlock blocks[], size_t count),
     * void (*savePeers)(void *info, const BRPeer peers[], size_t count),
     * int (*networkIsReachable)(void *info))
     */
    @Suppress(names = "unused") // called via BRPeerManager callback
    public static void syncStarted() {
        Context ctx = BrainwalletApp.getBreadContext();

        LtcStats ltcStats = BRSharedPrefs.getLiveLtcStats(ctx);
        int startHeight = ltcStats.currentBlockHeight;
        int lastHeight = BRSharedPrefs.getLastBlockHeight(ctx);
        if (startHeight > lastHeight) BRSharedPrefs.putStartHeight(ctx, lastHeight);
        SyncThreadManager.getInstance().startSyncing(startHeight);
    }
    @Suppress(names = "unused") // called via BRPeerManager callback
    public static void syncSucceeded() {
        Context ctx = BrainwalletApp.getBreadContext();
        if (ctx == null) return;
        SyncThreadManager.getInstance().stopSyncing();
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                BRSharedPrefs.putStartHeight(ctx, getInstance().getCurrentBlockHeight());
            }
        });
        if (onSyncFinished != null) onSyncFinished.onFinished();
    }
    @Suppress(names = "unused") // called via BRPeerManager callback
    public static void syncFailed() {
        SyncThreadManager.getInstance().stopSyncing();
        if (onSyncFinished != null) onSyncFinished.onFinished();
    }

    @Suppress(names = "unused") // called via BRPeerManager callback
    public static void txStatusUpdate() {
        Timber.d("timber: txStatusUpdate");

        synchronized (statusUpdateListeners) {
            for (OnTxStatusUpdate listener : statusUpdateListeners) {
                if (listener != null) listener.onStatusPeerManagerUpdate();
            }
        }

        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                updateLastBlockHeight(getInstance().getCurrentBlockHeight());
            }
        });
    }
    @Suppress(names = "unused") // called via BRPeerManager callback
    public static void onIntegrityWarning(String warning) {
        Timber.e("timber: native integrity warning: %s", warning);
        FirebaseCrashlytics.getInstance().recordException(
                new RuntimeException("BRPeerManager native integrity warning: " + warning)
        );
    }
    @Suppress(names = "unused") // called via BRPeerManager callback
    public static void saveBlocks(final BlockEntity[] blockEntities, final boolean replace) {
        Timber.d("timber: saveBlocks: %s", blockEntities.length);

        final Context ctx = BrainwalletApp.getBreadContext();
        if (ctx == null) return;
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                if (replace) MerkleBlockDataSource.getInstance(ctx).deleteAllBlocks();
                MerkleBlockDataSource.getInstance(ctx).putMerkleBlocks(blockEntities);
            }
        });

    }
    @Suppress(names = "unused") // called via BRPeerManager callback
    public static void savePeers(final PeerEntity[] peerEntities, final boolean replace) {
        Timber.d("timber: savePeers: %s", peerEntities.length);
        final Context ctx = BrainwalletApp.getBreadContext();
        if (ctx == null) return;
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                if (replace) PeerDataSource.getInstance(ctx).deleteAllPeers();
                PeerDataSource.getInstance(ctx).putPeers(peerEntities);
            }
        });
    }
    @Suppress(names = "unused") // called via BRPeerManager callback
    public static boolean networkIsReachable() {
        Timber.d("timber: networkIsReachable");
        return BRWalletManager.getInstance().isNetworkAvailable(BrainwalletApp.getBreadContext());
    }
    @Suppress(names = "unused") // called via BRPeerManager callback
    public static void deleteBlocks() {
        Timber.d("timber: deleteBlocks");
        final Context ctx = BrainwalletApp.getBreadContext();
        if (ctx == null) return;
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                MerkleBlockDataSource.getInstance(ctx).deleteAllBlocks();
            }
        });
    }
    @Suppress(names = "unused") // called via BRPeerManager callback
    public static void deletePeers() {
        Timber.d("timber: deletePeers");
        final Context ctx = BrainwalletApp.getBreadContext();
        if (ctx == null) return;
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                PeerDataSource.getInstance(ctx).deleteAllPeers();
            }
        });
    }

    /**
     * The peer BRPeerManager should pin its SPV sync to, as resolved from the user's
     * preferred trusted node in the Android Keystore. An empty {@link #host} means "no
     * fixed peer" - either the user is on the default "Litecoin mainnet" sync mode, or
     * trusted-node mode is on but no address has been entered yet - so the fixed peer is
     * cleared and sync uses the random mainnet peer discovery. {@link #port} is always the
     * real port to connect on (never 0).
     */
    static final class TrustedFixedPeer {
        final String host;
        final int port;

        TrustedFixedPeer(String host, int port) {
            this.host = host;
            this.port = port;
        }

        boolean isSet() {
            return !host.isEmpty();
        }
    }

    /**
     * Resolves the peer {@link #updateFixedPeer} should pin the SPV sync to, from the
     * trusted-node values persisted in the Android Keystore:
     * <ul>
     *   <li>the sync-mode preference (BRKeyStore#getTrustedNodeSyncPreference) gates
     *       everything - when the user is on the default "Litecoin mainnet" mode (preference
     *       false) this returns an empty host so any previously-pinned peer is cleared and
     *       sync goes back to the random mainnet peer array, regardless of whether a trusted
     *       address is still stored;</li>
     *   <li>otherwise the host/port are read (they're persisted separately -
     *       BRKeyStore#putTrustedNodeIPAddress / #putTrustedNodePort - the port being just as
     *       required as the host);</li>
     *   <li>absent / unreadable host -> {@code ""} (clears any fixed peer);</li>
     *   <li>a host with a stored port of 0 ("unset", e.g. saved before the port had its own
     *       field) -> {@link TrustedNode#STANDARD_PORT}, so the port connected on is stated
     *       explicitly rather than left to the native zero-means-default fallback.</li>
     * </ul>
     * Package-private so it can be unit-tested without the native library loaded.
     */
    static TrustedFixedPeer resolveTrustedFixedPeer(Context ctx) {
        String host = "";
        int port = 0;
        try {
            if (BRKeyStore.getTrustedNodeSyncPreference(ctx, 0)) {
                String storedHost = BRKeyStore.getTrustedNodeIPAddress(ctx, 0);
                if (storedHost != null) host = storedHost;
                port = BRKeyStore.getTrustedNodePort(ctx, 0);
            }
        } catch (UserNotAuthenticatedException e) {
            Timber.e(e, "timber: resolveTrustedFixedPeer: could not read trusted node from keystore");
        }
        int effectivePort = host.isEmpty() ? port : (port > 0 ? port : TrustedNode.STANDARD_PORT);
        return new TrustedFixedPeer(host, effectivePort);
    }

    /**
     * Applies the user's current peer-sync mode to the running SPV sync. Called at launch
     * (BRWalletManager.initWallet -> pm.create -> here) and whenever the peer-sync mode or
     * trusted-node address changes in settings.
     *
     * <p>The sequence is always <b>stop -> re-resolve the fixed peer -> restart</b>, in both
     * toggle directions:
     * <ol>
     *   <li>the current sync is stopped up front ({@link SyncThreadManager#stopSyncing()}),
     *       so the switch doesn't depend only on the native disconnect side effect of
     *       {@code setFixedPeer} to end it;</li>
     *   <li>{@link #resolveTrustedFixedPeer} re-reads the mode + trusted node from the
     *       Keystore. An empty host (default "Litecoin mainnet" mode, or trusted-node mode
     *       with no address yet) hands {@code ""} to the native {@code setFixedPeer} - it
     *       disconnects, restores the full mainnet connection count and clears the peer
     *       array; a resolved trusted node is pinned as the single fixed peer;</li>
     *   <li>{@code wrapConnectV2()} reconnects, which restarts the sync against the freshly
     *       rebuilt peer array (random mainnet peers, or the pinned trusted node).</li>
     * </ol>
     */
    public void updateFixedPeer(Context ctx) {
        TrustedFixedPeer peer = resolveTrustedFixedPeer(ctx);
        // Stop first so a mode switch is an explicit stop -> re-pin -> restart. Harmless
        // no-op at launch (nothing syncing yet) and idempotent with the native
        // syncStopped callback setFixedPeer's disconnect will also fire.
        SyncThreadManager.getInstance().stopSyncing();
        String node = peer.isSet() ? TrustedNode.withPort(peer.host, peer.port) : "";
        boolean success = setFixedPeer(peer.host, peer.port);
        if (!success) {
            Timber.i("timber: updateFixedPeer: Failed to updateFixedPeer with input: %s", node);
        } else {
            Timber.d("timber: updateFixedPeer: succeeded");
        }
        wrapConnectV2();
    }

    public void networkChanged(boolean isOnline) {
        if (isOnline)
            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    wrapConnectV2();
                }
            });
    }

    //wrap logic enable/disable connect with new flow
    public void wrapConnectV2() {
//        if (featureSelectedPeersEnabled()) {
//            fetchSelectedPeers().whenComplete((strings, throwable) -> connect());
//        } else {
//            connect();
//        }
        //currently we are just using connect(), since the core using hardcoded peers
        //https://github.com/gruntsoftware/core/commit/0b7f85feac840c7667338c340c808dfccde4251a
        connect();
    }

    public static boolean featureSelectedPeersEnabled() {
        RemoteConfigSource remoteConfigSource = KoinJavaComponent.get(RemoteConfigSource.class);
        return remoteConfigSource.getBoolean(KEY_FEATURE_SELECTED_PEERS_ENABLED);
    }

    public void addStatusUpdateListener(OnTxStatusUpdate listener) {
        if (statusUpdateListeners.contains(listener)) return;
        statusUpdateListeners.add(listener);
    }

    public void removeListener(OnTxStatusUpdate listener) {
        statusUpdateListeners.remove(listener);
    }

    public CompletableFuture<Set<? extends String>> fetchSelectedPeers() {
        SelectedPeersRepository selectedPeersRepository = KoinJavaComponent.get(SelectedPeersRepository.class);

        return FutureKt.future(
                CoroutineScopeKt.CoroutineScope(EmptyCoroutineContext.INSTANCE),
                EmptyCoroutineContext.INSTANCE,
                CoroutineStart.DEFAULT,
                (coroutineScope, continuation) -> selectedPeersRepository.fetchSelectedPeers(continuation)
        );
    }

    public static Set<? extends String> fetchSelectedPeersBlocking() {
        try {
            return BRPeerManager.getInstance().fetchSelectedPeers().get();
        } catch (ExecutionException | InterruptedException e) {
            return java.util.Collections.emptySet();
        }
    }

    public static void setOnSyncFinished(OnSyncSucceeded listener) {
        onSyncFinished = listener;
    }

    public interface OnTxStatusUpdate {
        void onStatusPeerManagerUpdate();
    }

    public interface OnSyncSucceeded {
        void onFinished();
    }

    public static void updateLastBlockHeight(int blockHeight) {
        final Context ctx = BrainwalletApp.getBreadContext();
        if (ctx == null) return;
        BRSharedPrefs.putLastBlockHeight(ctx, blockHeight);
    }

    // All native methods below are instance methods on this singleton, marked synchronized:
    // the JVM acquires this instance's monitor before entering native code and holds it until
    // the native call returns. Since getInstance() always hands back the same object, this
    // serializes every caller's access to the shared native BRPeerManager* - in particular it
    // prevents connect() (which can spawn new native peer/DNS threads) from ever running
    // concurrently with peerManagerFreeEverything() (which destroys the manager's mutex and
    // frees it), which was letting a still-running background thread call pthread_mutex_lock()
    // on an already-destroyed mutex (FORTIFY abort, Crashlytics issue 85c581edcdb39b941df627e7b1324a71).
    public native synchronized String getCurrentPeerName();

    public native synchronized void create(int earliestKeyTime, int blockCount, int peerCount, double fpRate);

    public native synchronized void connect();

    public native synchronized void putPeer(byte[] peerAddress, byte[] peerPort, byte[] peerTimeStamp);

    public native synchronized void createPeerArrayWithCount(int count);

    public native synchronized void putBlock(byte[] block, int blockHeight);

    public native synchronized void createBlockArrayWithCount(int count);

    public native synchronized double syncProgress(int startHeight);

    public native synchronized int getCurrentBlockHeight();

    public native synchronized int getRelayCount(byte[] hash);

    public native synchronized boolean setFixedPeer(String node, int port);

    public native synchronized int getEstimatedBlockHeight();

    public native synchronized boolean isCreated();

    public native synchronized boolean isConnected();

    public native synchronized void peerManagerFreeEverything();

    public native synchronized long getLastBlockTimestamp();

    public native synchronized void rescan();
}