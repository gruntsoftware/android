package com.brainwallet.ui.screens.home.receive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.brainwallet.ui.theme.BrainwalletAppTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * describe [ReceiveDialogFragment] for backward compat,
 * since we are still using [com.brainwallet.presenter.activities.BreadActivity]
 */
class ReceiveDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val viewModel = koinViewModel<ReceiveDialogViewModel>()
                val appSetting by viewModel.appSetting.collectAsState()
                /**
                 * we need this theme inside this fragment,
                 * because we are still using fragment to display ReceiveDialog composable
                 * pls check BreadActivity.handleNavigationItemSelected
                 */
                BrainwalletAppTheme(appSetting = appSetting) {
                    ReceiveDialog(
                        modifier = Modifier.padding(12.dp),
                        viewModel = viewModel,
                        onDismissRequest = { dismiss() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
    }

    companion object {
        @JvmStatic
        fun show(manager: FragmentManager) {
            ReceiveDialogFragment().show(manager, "ReceiveDialogFragment")
        }
    }
}
