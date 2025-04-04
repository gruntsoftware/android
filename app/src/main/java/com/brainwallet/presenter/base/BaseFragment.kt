package com.brainwallet.presenter.base

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import timber.log.Timber


abstract class BaseFragment<P : BasePresenter<BaseView>> : Fragment() {
    lateinit var presenter: P

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = initPresenter()
    }

    override fun onDetach() {
        super.onDetach()
        if (this::presenter.isInitialized) {
            presenter.detach()
        } else {
            Timber.w("presenter is not yet initialized")
        }
    }

    fun showError(error: String) {
        AlertDialog.Builder(requireContext()).setMessage(error).setPositiveButton(
            android.R.string.ok,
            null,
        ).show()
    }

    fun showError(errorId: Int) {
        showError(getString(errorId))
    }

    abstract fun initPresenter(): P
}
