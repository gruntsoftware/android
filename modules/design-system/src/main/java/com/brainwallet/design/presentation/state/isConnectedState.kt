package com.brainwallet.design.presentation.state

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberIsConnectedState(): IsConnectedState {
    val context = LocalContext.current
    val connectivityManager = remember(context) {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    val state = remember {
        IsConnectedState(connectivityManager)
    }
    DisposableEffect(connectivityManager) {
        state.observe()
        onDispose {
            state.dispose()
        }
    }
    return state
}

@Stable
class IsConnectedState(
    private val connectivityManager: ConnectivityManager
) {
    var isConnected by mutableStateOf(checkInitialConnection())
        private set

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            isConnected = true
        }

        override fun onLost(network: Network) {
            isConnected = false
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val hasInternet = networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
            val isValidated = networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            )
            isConnected = hasInternet && isValidated
        }
    }

    fun observe() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun dispose() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun checkInitialConnection(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
