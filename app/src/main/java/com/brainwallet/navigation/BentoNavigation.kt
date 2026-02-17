package com.brainwallet.navigation

import android.content.Context
import com.brainwallet.ui.BentoActivity
import org.koin.core.annotation.Factory

@Factory
class BentoNavigation {
    fun navigateToBento(context: Context, vararg flags: Int) {
        BentoActivity.start(context, *flags)
    }
}
