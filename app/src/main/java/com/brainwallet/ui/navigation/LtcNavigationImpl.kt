package com.brainwallet.ui.navigation

import android.content.Context
import android.content.Intent
import com.brainwallet.ltc.presentation.navigation.LtcNavigation
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.grunt.brainwallet.games.GamesActivity
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
class LtcNavigationImpl(
    @InjectedParam private val context: Context,
    @InjectedParam private val onNavigate: OnNavigate
) : LtcNavigation {
    override fun navigateToBuyLiteCoinScreen() {
        onNavigate.invoke(UiEffect.Navigate(Route.BuyLitecoin))
    }

    override fun navigateToGames() {
        context.startActivity(Intent(context, GamesActivity::class.java))
    }
}
