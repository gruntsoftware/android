package com.brainwallet.ui.bento

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.SyncAnalyticsRepository
import com.brainwallet.design.presentation.component.rail.BentoRailSettings
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.ui.screens.home.SettingsEvent
import com.brainwallet.ui.screens.home.SettingsViewModel
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import com.brainwallet.util.EventBus
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.java.KoinJavaComponent.inject

@Composable
fun BentoSettingsDrawerSheet(
    modifier: Modifier = Modifier,
    syncAnalyticsRepository: SyncAnalyticsRepository = koinInject(),
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(
            SettingsEvent.OnLoad(
                shareAnalyticsDataEnabled = false, // Placeholder - would need actual implementation
                lastSyncMetadata = syncAnalyticsRepository.getLastSyncMetadata(),
            )
        )
    }

    val syncDescription = state.lastSyncMetadata?.let {
        SyncAnalyticsRepository.SyncMetadata.Formatter().format(it)
    } ?: "No sync metadata"

    ModalDrawerSheet(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        drawerContainerColor = BrainwalletTheme.colors.surface,
        drawerContentColor = BrainwalletTheme.colors.content
    ) {
        BentoRailSettings(
            modifier = Modifier
                .testTag("bentoRailSettings")
                .fillMaxSize()
                .padding(16.dp),
            shareAnalyticsEnabled = state.shareAnalyticsDataEnabled,
            selectedLanguage = state.selectedLanguage.name,
            selectedCurrency = state.selectedCurrency.code,
            selectedFeeType = state.selectedFeeType,
            syncDescription = syncDescription,
            onSecurityClick = {
                // Handle security settings navigation
            },
            onLanguageClick = {
                // Handle language settings navigation
            },
            onCurrencyClick = {
                // Handle currency settings navigation
            },
            onGamesClick = {
                // Handle games settings navigation
            },
            onBlockchainClick = {
                // Handle blockchain settings navigation
            },
            onSupportClick = {
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(context, Uri.parse(BRConstants.SUPPORT_WEB_LINK))
            },
            onSocialMediaClick = {
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(context, Uri.parse(BRConstants.LINKTREE_URL))
            },
            onLockClick = {
                viewModel.onEvent(SettingsEvent.OnToggleLock)
            }
        )
    }
}

/**
 * Backward compatibility with XML for now.
 * Will be used by [activity_bread.xml]
 */
class BentoSettingsDrawerComposeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AbstractComposeView(context, attrs) {

    private val settingsViewModel: SettingsViewModel by inject(SettingsViewModel::class.java)

    @Composable
    override fun Content() {
        val appSetting by settingsViewModel.appSetting.collectAsState(
            AppSetting()
        )
        BrainwalletAppTheme(appSetting = appSetting) {
            BentoSettingsDrawerSheet(viewModel = settingsViewModel)
        }
    }

    fun observeBus(
        onEach: (EventBus.Event.Message) -> Unit
    ) {
        EventBus.events
            .filter { it is EventBus.Event.Message }
            .map { it as EventBus.Event.Message }
            .onEach { onEach.invoke(it) }
            .launchIn(findViewTreeLifecycleOwner()!!.lifecycle.coroutineScope)
    }
}
