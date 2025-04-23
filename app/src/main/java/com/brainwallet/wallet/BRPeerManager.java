package com.brainwallet.wallet;

import static com.brainwallet.data.source.RemoteConfigSource.KEY_FEATURE_SELECTED_PEERS_ENABLED;

import android.content.Context;

import com.brainwallet.BrainwalletApp;
import com.brainwallet.data.repository.SelectedPeersRepository;
import com.brainwallet.data.source.RemoteConfigSource;
import com.brainwallet.presenter.entities.BlockEntity;
import com.brainwallet.presenter.entities.PeerEntity;
import com.brainwallet.tools.manager.BRSharedPrefs;
import com.brainwallet.tools.manager.SyncManager;
import com.brainwallet.tools.sqlite.MerkleBlockDataSource;
import com.brainwallet.tools.sqlite.PeerDataSource;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.tools.util.TrustedNode;

import org.koin.java.KoinJavaComponent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.future.FutureKt;
import timber.log.Timber;

public class BRPeerManager {
    private static BRPeerManager instance;

    private static final List<OnTxStatusUpdate> statusUpdateListeners = new ArrayList<>();
    private static OnSyncSucceeded onSyncFinished;

    static long syncStartDate = new Date().getTime();
    static long syncCompletedDate = new Date().getTime();

    private BRPeerManager() {
    }

    public static BRPeerManager getInstance() {
        if (instance == null) {
            instance = new BRPeerManager();
        }
        return instance;
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

    public static void syncStarted() {
        Context ctx = BrainwalletApp.getBreadContext();
        int startHeight = BRSharedPrefs.getStartHeight(ctx);
        int lastHeight = BRSharedPrefs.getLastBlockHeight(ctx);
        if (startHeight > lastHeight) BRSharedPrefs.putStartHeight(ctx, lastHeight);
        SyncManager.getInstance().startSyncingProgressThread(ctx);
    }

    public static void syncSucceeded() {
        Context ctx = BrainwalletApp.getBreadContext();
        if (ctx == null) return;
        BRSharedPrefs.putLastSyncTimestamp(ctx, System.currentTimeMillis());
        SyncManager.getInstance().updateAlarms(ctx);
        BRSharedPrefs.putAllowSpend(ctx, true);
        SyncManager.getInstance().stopSyncingProgressThread(ctx);
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                BRSharedPrefs.putStartHeight(ctx, getCurrentBlockHeight());
            }
        });
        if (onSyncFinished != null) onSyncFinished.onFinished();
    }

    public static void syncFailed() {
        Context ctx = BrainwalletApp.getBreadContext();
        SyncManager.getInstance().stopSyncingProgressThread(ctx);

        if (ctx == null) return;
        Timber.d("timber: Network Not Available, showing not connected bar");

        SyncManager.getInstance().stopSyncingProgressThread(ctx);
        if (onSyncFinished != null) onSyncFinished.onFinished();
    }

    public static void txStatusUpdate() {
        Timber.d("timber: txStatusUpdate");

        for (OnTxStatusUpdate listener : statusUpdateListeners) {
            if (listener != null) listener.onStatusUpdate();
        }
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                updateLastBlockHeight(getCurrentBlockHeight());
            }
        });
    }

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

    public static boolean networkIsReachable() {
        Timber.d("timber: networkIsReachable");
        return BRWalletManager.getInstance().isNetworkAvailable(BrainwalletApp.getBreadContext());
    }

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

    public void updateFixedPeer(Context ctx) {
        String node = BRSharedPrefs.getTrustNode(ctx);
        String host = TrustedNode.getNodeHost(node);
        int port = TrustedNode.getNodePort(node);
        boolean success = setFixedPeer(host, port);
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
        void onStatusUpdate();
    }

    public interface OnSyncSucceeded {
        void onFinished();
    }

    public static void updateLastBlockHeight(int blockHeight) {
        final Context ctx = BrainwalletApp.getBreadContext();
        if (ctx == null) return;
        BRSharedPrefs.putLastBlockHeight(ctx, blockHeight);
    }

    public native String getCurrentPeerName();

    public native void create(int earliestKeyTime, int blockCount, int peerCount, double fpRate);

    public native void connect();

    public native void putPeer(byte[] peerAddress, byte[] peerPort, byte[] peerTimeStamp);

    public native void createPeerArrayWithCount(int count);

    public native void putBlock(byte[] block, int blockHeight);

    public native void createBlockArrayWithCount(int count);

    public native static double syncProgress(int startHeight);

    public native static int getCurrentBlockHeight();

    public native static int getRelayCount(byte[] hash);

    public native boolean setFixedPeer(String node, int port);

    public native static int getEstimatedBlockHeight();

    public native boolean isCreated();

    public native boolean isConnected();

    public native void peerManagerFreeEverything();

    public native long getLastBlockTimestamp();

    public native void rescan();
}