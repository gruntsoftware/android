package com.brainwallet.ui.screens.home.receive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.ui.composable.BrainwalletButton
import com.brainwallet.ui.screens.home.SettingsViewModel
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.BrainwalletTheme
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject

//TODO: WIP here
@Composable
fun ReceiveDialog(
    onDismissRequest: () -> Unit,
    viewModel: ReceiveDialogViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(ReceiveDialogEvent.OnLoad(context))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.Receive_title).uppercase(),
                style = BrainwalletTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrainwalletTheme.colors.surface
                ),
            )
            OutlinedIconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                border = null,
                onClick = onDismissRequest
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.AccessibilityLabels_close),
                    tint = BrainwalletTheme.colors.surface
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            state.qrBitmap?.asImageBitmap()?.let { imageBitmap ->
                Image(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    bitmap = imageBitmap,
                    contentDescription = "address"
                )
            } ?: Box(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
                    .background(Color.Gray)
                    .padding(8.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = state.address,
                    style = BrainwalletTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrainwalletTheme.colors.surface
                    )
                )
                Text(
                    text = "NEW RECEIVE ADDRESS",
                    style = BrainwalletTheme.typography.bodySmall.copy(
                        color = BrainwalletTheme.colors.surface,
                    )
                )
            }
        }
        BrainwalletButton(onClick = {
            viewModel.onEvent(ReceiveDialogEvent.OnCopyClick(context))
            Toast.makeText(context, R.string.Receive_copied, Toast.LENGTH_SHORT).show()
        }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.URLHandling_copy).uppercase(),
                    fontWeight = FontWeight.Bold
                )
                Text(state.address)
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "1.29Ł",
                style = BrainwalletTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrainwalletTheme.colors.surface
                )
            )
            Text(
                text = "9 APR 2025 02:00:00",
                style = BrainwalletTheme.typography.bodySmall.copy(
                    color = BrainwalletTheme.colors.surface
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "84€   |   EUR",
                style = BrainwalletTheme.typography.titleMedium.copy(
                    color = BrainwalletTheme.colors.surface,
                )
            )
            BrainwalletButton(
                modifier = Modifier.weight(1f),
                onClick = {}
            ) {
                Text(
                    text = "BUY LTC",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * describe [ReceiveDialogFragment] for backward compat,
 * since we are still using [com.brainwallet.presenter.activities.BreadActivity]
 */
class ReceiveDialogFragment : DialogFragment() {

    private val settingsViewModel: SettingsViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val appSetting by settingsViewModel.appSetting.collectAsState(
                    AppSetting()
                )
                /**
                 * we need this theme inside this fragment,
                 * because we are still using fragment to display ReceiveDialog composable
                 * pls check BreadActivity.handleNavigationItemSelected
                 */
                BrainwalletAppTheme(appSetting = appSetting) {
                    Box(
                        modifier = Modifier
                            .padding(24.dp)
                            .background(
                                BrainwalletTheme.colors.content,
                                shape = BrainwalletTheme.shapes.extraLarge
                            )
                            .border(
                                width = 1.dp,
                                color = BrainwalletTheme.colors.surface,
                                shape = BrainwalletTheme.shapes.extraLarge
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ReceiveDialog(onDismissRequest = { dismiss() })
                    }
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
    }

    companion object {
        @JvmStatic
        fun show(manager: FragmentManager) {
            ReceiveDialogFragment().show(manager, "ReceiveDialogFragment")
        }
    }
}