package com.brainwallet.design.presentation.state

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brainwallet.design.domain.DarkModeUseCase
import org.koin.compose.koinInject

@Composable
fun rememberDarkModeState(darkModeUseCase: DarkModeUseCase = koinInject()): DarkModeState {
    return DarkModeState(
        darkModeUseCase.isDarkMode(),
    ) { current ->
        darkModeUseCase.toggleDarkMode(!current)
        !current
    }
}

@Stable
class DarkModeState(
    initialIsDarkMode: Boolean = true,
    private val onToggle: (current: Boolean) -> Boolean = { !it }
) {
    var isDarkMode by mutableStateOf(initialIsDarkMode)
        private set

    fun toggle() {
        this.isDarkMode = onToggle(isDarkMode)
        Log.d("ketai", "toggle: $isDarkMode")
    }
}
