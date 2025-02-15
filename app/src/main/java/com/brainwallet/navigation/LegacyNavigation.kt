package com.brainwallet.navigation

import android.content.Context
import com.brainwallet.ui.BrainwalletActivity

//provide old navigation using intent activity
object LegacyNavigation {

    //todo

    //open compose from old activity
    @JvmStatic
    fun openComposeScreen(
        context: Context,
        destination: Route = Route.Welcome
    ) = BrainwalletActivity.createIntent(context, destination).also {
        context.startActivity(it)
    }

}