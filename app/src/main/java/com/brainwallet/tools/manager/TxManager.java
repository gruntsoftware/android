package com.brainwallet.tools.manager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.WorkerThread;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brainwallet.tools.adapter.TransactionListAdapter;
import com.brainwallet.tools.animation.BRAnimator;
import com.brainwallet.tools.listeners.RecyclerItemClickListener;
import com.brainwallet.tools.threads.BRExecutor;
import com.brainwallet.presenter.activities.BreadActivity;
import com.brainwallet.presenter.entities.TxItem;
import com.brainwallet.wallet.BRPeerManager;
import com.brainwallet.wallet.BRWalletManager;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

public class TxManager {

    private static TxManager instance;
    private RecyclerView txList;
    public TransactionListAdapter adapter;
    public PromptManager.PromptItem currentPrompt;
    public PromptManager.PromptInfo promptInfo;
    public TransactionListAdapter.SyncingProgressViewHolder syncingProgressViewHolder;

    public static TxManager getInstance() {
        if (instance == null) instance = new TxManager();
        return instance;
    }

    public void init(final BreadActivity app, RecyclerView recyclerView) {
        txList = recyclerView;
        txList.setLayoutManager(new CustomLinearLayoutManager(app));
        txList.addOnItemTouchListener(new RecyclerItemClickListener(app,
                txList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, float x, float y) {
                if (currentPrompt == null || position > 0)
                    BRAnimator.showTransactionPager(app, adapter.getItems(), currentPrompt == null ? position : position - 1);
                else {
                    //clicked on the  x (close)
                    if (x > view.getWidth() - 100 && y < 100) {
                        view.animate().setDuration(150).translationX(BreadActivity.screenParametersPoint.x).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                hidePrompt(app, null);
                            }
                        });

                    } else { //clicked on the prompt
                        if (currentPrompt != PromptManager.PromptItem.SYNCING) {
                            PromptManager.PromptInfo info = PromptManager.getInstance().promptInfo(app, currentPrompt);
                            if (info != null)
                                info.listener.onClick(view);
                            currentPrompt = null;
                        }
                    }
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
        adapter = new TransactionListAdapter(app, null);
        txList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        setupSwipe(app);
    }

    private TxManager() {
    }

    public void onResume(final Activity app) {
        crashIfNotMain();
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                final double progress = BRPeerManager.syncProgress(BRSharedPrefs.getStartHeight(app));
                BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (progress > 0 && progress < 1) {
                            currentPrompt = PromptManager.PromptItem.SYNCING;
                            updateCard(app);
                        } else {
                            showNextPrompt(app);
                        }
                    }
                });
            }
        });
    }

    void showPrompt(Activity app, PromptManager.PromptItem item) {
        crashIfNotMain();
        if (item == null) throw new RuntimeException("can't be null");
        if (currentPrompt != PromptManager.PromptItem.SYNCING) {
            currentPrompt = item;
        }
        updateCard(app);
    }

    void hidePrompt(final Activity app, final PromptManager.PromptItem item) {
        crashIfNotMain();
        currentPrompt = null;
        if (txList.getAdapter() != null)
            txList.getAdapter().notifyItemRemoved(0);
        if (item == PromptManager.PromptItem.SYNCING) {
            showNextPrompt(app);
            updateCard(app);
        }
    }

    private void showNextPrompt(Activity app) {
        crashIfNotMain();
        PromptManager.PromptItem toShow = PromptManager.getInstance().nextPrompt(app);
        if (toShow != null) {
            Timber.d("timber: showNextPrompt: %s", toShow);
            currentPrompt = toShow;
            promptInfo = PromptManager.getInstance().promptInfo(app, currentPrompt);
            updateCard(app);
        } else {
            Timber.d("timber: showNextPrompt: nothing to show");
        }
    }

    @WorkerThread
    public synchronized void updateTxList(final Context app) {
        long start = System.currentTimeMillis();
        final TxItem[] arr = BRWalletManager.getInstance().getTransactions();
        final List<TxItem> items = arr == null ? null : new LinkedList<>(Arrays.asList(arr));

        long took = (System.currentTimeMillis() - start);
        if (took > 500)
            Timber.d("timber: updateTxList: took: %s", took);
        if (adapter != null) {
            ((Activity) app).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.setItems(items);
                    txList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    Timber.d("timber: updateTxList: %s", currentPrompt);
                }
            });
        }
    }

    private void updateCard(final Context app) {
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                updateTxList(app);
            }
        });
    }

    private void setupSwipe(final Activity app) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                hidePrompt(app, null);
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (!(viewHolder instanceof TransactionListAdapter.PromptHolder)) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(txList);
    }


    private static class CustomLinearLayoutManager extends LinearLayoutManager {

        CustomLinearLayoutManager(Context context) {
            super(context);
        }

        /**
         * Disable predictive animations. There is a bug in RecyclerView which causes views that
         * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
         * adapter size has decreased since the ViewHolder was recycled.
         */
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public CustomLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }
    }

    private void crashIfNotMain() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalAccessError("Can only call from main thread");
        }
    }

}
