package com.brainwallet.presenter.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.brainwallet.databinding.FragmentHistoryBinding
import com.brainwallet.presenter.activities.BreadActivity
import com.brainwallet.presenter.base.BaseFragment
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.manager.BRSharedPrefs.OnIsoChangedListener
import com.brainwallet.tools.manager.TxManager
import com.brainwallet.tools.sqlite.TransactionDataSource.OnTxAddedListener
import com.brainwallet.tools.threads.BRExecutor
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.wallet.BRPeerManager
import com.brainwallet.wallet.BRPeerManager.OnTxStatusUpdate
import com.brainwallet.wallet.BRWalletManager
import com.brainwallet.wallet.BRWalletManager.OnBalanceChanged
import timber.log.Timber

class HistoryFragment :
    BaseFragment<HistoryPresenter>(),
    OnBalanceChanged,
    OnTxStatusUpdate,
    OnIsoChangedListener,
    OnTxAddedListener,
    HistoryView {
    lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TxManager.getInstance().init(requireActivity() as BreadActivity, binding.recyclerView)
    }

    private fun addObservers() {
        BRWalletManager.getInstance().addBalanceChangedListener(this)
        BRPeerManager.getInstance().addStatusUpdateListener(this)
        BRSharedPrefs.addIsoChangedListener(this)
    }

    private fun removeObservers() {
        BRWalletManager.getInstance().removeListener(this)
        BRPeerManager.getInstance().removeListener(this)
        BRSharedPrefs.removeListener(this)
    }
    private fun registerAnalyticsError(errorString: String) {
        val params = Bundle()
        params.putString("lwa_error_message", errorString);
        AnalyticsManager.logCustomEventWithParams(BRConstants._20200112_ERR, params)
        Timber.d("History Fragment: RegisterError : %s", errorString)
    }
    override fun onResume() {
        super.onResume()
        addObservers()

        if (this.activity == null) {
            registerAnalyticsError("null_in_history_fragment_on_resume")
        }
        else {
            TxManager.getInstance().onResume(this.activity)
        }
    }

    override fun onPause() {
        super.onPause()
        removeObservers()
    }

    override fun onBalanceChanged(balance: Long) {
        updateUI()
    }

    override fun onStatusUpdate() {
        BRExecutor.getInstance().forBackgroundTasks().execute {
            if (this.activity == null) {
                registerAnalyticsError("null_in_history_fragment_on_status_update")
            }
            else {
                TxManager.getInstance().updateTxList(this.activity)
            }
        }
    }

    override fun onIsoChanged(iso: String) {
        updateUI()
    }

    override fun onTxAdded() {
        BRExecutor.getInstance().forBackgroundTasks().execute {
            if (this.activity == null) {
                registerAnalyticsError("null_in_history_fragment_on_tx_added")
            }
            else {
                TxManager.getInstance().updateTxList(this.activity)
            }
        }
    }
    private fun updateUI() {
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute {
            if (this.activity == null) {
                registerAnalyticsError("null_in_history_fragment_update_ui")
            }
            else {
                Thread.currentThread().name = Thread.currentThread().name + "HistoryFragment:updateUI"
                TxManager.getInstance().updateTxList(this.activity)
            }
        }
    }

    override fun initPresenter() = HistoryPresenter(this)
}
