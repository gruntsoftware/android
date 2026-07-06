package com.brainwallet.ui.screens.youremojis

import kotlinx.collections.immutable.ImmutableList

sealed class YourEmojisEvent {
    data class OnSavedItClick(val emojis: ImmutableList<String>) : YourEmojisEvent()
}
