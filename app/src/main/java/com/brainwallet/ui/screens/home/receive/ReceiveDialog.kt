package com.brainwallet.ui.screens.home.receive

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.qrcode.QRUtils
import com.brainwallet.ui.composable.BrainwalletButton
import com.brainwallet.ui.screens.home.SettingsViewModel
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.BrainwalletTheme
import org.koin.android.ext.android.inject

//TODO: WIP here
@Composable
fun ReceiveDialog() {

    val context = LocalContext.current
    var address by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        address = BRSharedPrefs.getReceiveAddress(context)

        qrBitmap = QRUtils.generateQR(context, "litecoin:${address}")
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.Receive_title).uppercase(),
            style = BrainwalletTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = BrainwalletTheme.colors.surface
            ),
        )
        Row(modifier = Modifier.fillMaxWidth()) {

            qrBitmap?.asImageBitmap()?.let { imageBitmap ->
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
                    .padding(8.dp)
            ) {
                Text(
                    text = address,
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
        BrainwalletButton(onClick = {}) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.URLHandling_copy).uppercase(),
                    fontWeight = FontWeight.Bold
                )
                Text(address)
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
                        ReceiveDialog()
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