package com.brainwallet.ui.bento

import android.content.Context
import org.koin.core.annotation.Factory

@Factory
class BentoNavigation {
    fun navigateToBento(context: Context, vararg flags: Int) {
        BentoActivity.start(context, *flags)
    }
}
