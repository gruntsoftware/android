package com.brainwallet.navigation

import androidx.navigation.NavOptionsBuilder

typealias OnNavigate = (UiEffect.Navigate) -> Unit

sealed class UiEffect {
    data class Navigate(
        val destinationRoute: Route? = null, // If null, it acts as PopBackStack
        val forcePopBackStack: Boolean = false,
        val navOptionsBuilder: (NavOptionsBuilder.() -> Unit)? = null
    ) : UiEffect() {

        fun isBack(): Boolean = destinationRoute == null

        companion object {
            fun Back(): Navigate = Navigate()
        }
    }

    data class ShowDialog(val name: String): UiEffect()

    data class ShowMessage(
        val message: String,
        val type: Type = Type.Success,
    ) : UiEffect() {
        sealed class Type {
            object Error : Type()
            object Success : Type()
        }

        companion object {
            fun Error(message: String): ShowMessage {
                return ShowMessage(
                    type = Type.Error,
                    message = message
                )
            }
        }
    }
}
