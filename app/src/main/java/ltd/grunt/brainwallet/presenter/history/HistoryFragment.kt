package ltd.grunt.brainwallet.presenter.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ltd.grunt.brainwallet.databinding.FragmentHistoryBinding
import ltd.grunt.brainwallet.presenter.activities.BreadActivity
import ltd.grunt.brainwallet.presenter.base.BaseFragment
import ltd.grunt.brainwallet.tools.manager.AnalyticsManager
import ltd.grunt.brainwallet.tools.manager.BRSharedPrefs
import ltd.grunt.brainwallet.tools.manager.BRSharedPrefs.OnIsoChangedListener
import ltd.grunt.brainwallet.tools.manager.TxManager
import ltd.grunt.brainwallet.tools.sqlite.TransactionDataSource.OnTxAddedListener
import ltd.grunt.brainwallet.tools.threads.BRExecutor
import ltd.grunt.brainwallet.tools.util.BRConstants
import ltd.grunt.brainwallet.wallet.BRPeerManager
import ltd.grunt.brainwallet.wallet.BRPeerManager.OnTxStatusUpdate
import ltd.grunt.brainwallet.wallet.BRWalletManager
import ltd.grunt.brainwallet.wallet.BRWalletManager.OnBalanceChanged
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
