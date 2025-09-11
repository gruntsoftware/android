package com.brainwallet.navigation

import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brainwallet.BuildConfig
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.ui.composable.LoadingDialog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.compose.viewmodel.koinViewModel

@KoinViewModel
class MoonPayWidgetLauncherViewModel(
    private val settingRepository: SettingRepository,
    private val ltcRepository: LtcRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _result = Channel<Result<Pair<Boolean, Uri>>>()
    val result = _result.receiveAsFlow()

    fun launch(params: Map<String, String>) {
        _isLoading.update { true }
        viewModelScope.launch(ioDispatcher) {
            val isDarkMode = settingRepository.isDarkMode()
            _result.send(
                ltcRepository.runCatching {
                    val result = ltcRepository.fetchMoonpaySignedUrl(
                        params = params.toMutableMap().apply {
                            put("theme", if (isDarkMode) "dark" else "light")
                        }
                    )
                    isDarkMode to result.toUri().buildUpon()
                        .apply {
                            if (BuildConfig.DEBUG) {
                                authority("buy-sandbox.moonpay.com") // replace base url from buy.moonpay.com
                            }
                        }
                        .build()
                }
            )
            _isLoading.update { false }
        }
    }
}

@Composable
fun MoonPayWidgetLauncher(
    modifier: Modifier = Modifier,
    viewModel: MoonPayWidgetLauncherViewModel = koinViewModel(),
    onResult: () -> Unit = {}
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()

    AnimatedVisibility(isLoading, modifier = modifier) {
        LoadingDialog()
    }

    LaunchedEffect(Unit) {
        viewModel.result.collect { result ->
            result.fold(
                onSuccess = { (isDarkMode, uri) ->
                    val intent = CustomTabsIntent.Builder()
                        .setColorScheme(
                            if (isDarkMode) {
                                CustomTabsIntent.COLOR_SCHEME_DARK
                            } else {
                                CustomTabsIntent.COLOR_SCHEME_LIGHT
                            }
                        )
                        .build()
                    intent.launchUrl(context, uri)
                },
                onFailure = { e ->
                    Toast.makeText(
                        context,
                        "Failed to load: ${e.message}, please try again later",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
            onResult.invoke()
        }
    }
}
