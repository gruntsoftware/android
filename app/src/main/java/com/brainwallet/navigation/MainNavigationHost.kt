package com.brainwallet.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.brainwallet.constants.BWConstants
import com.brainwallet.ui.bentosections.buyreceivebento.receive.ReceiveDialog
import com.brainwallet.ui.screens.buyreceive.BuyReceiveScreen
import com.brainwallet.ui.screens.gamehub.GameHubScreen
import com.brainwallet.ui.screens.home.MainScreen
import com.brainwallet.ui.screens.main.WebModalScreen
import com.brainwallet.ui.screens.emojis.EmojiPagerScreen
import com.brainwallet.ui.screens.emojis.YourEmojisScreen
import com.brainwallet.ui.screens.ready.ReadyScreen
import com.brainwallet.ui.screens.restore.RestoreScreen
import com.brainwallet.ui.screens.send.SendScreen
import com.brainwallet.ui.screens.setpasscode.SetPasscodeScreen
import com.brainwallet.ui.screens.topup.TopUpScreen
import com.brainwallet.ui.screens.unlock.UnLockScreen
import com.brainwallet.ui.screens.welcome.WelcomeScreen
import com.brainwallet.ui.screens.yourseedproveit.YourSeedProveItScreen
import com.brainwallet.ui.screens.yourseedwords.YourSeedWordsScreen
import kotlinx.collections.immutable.toImmutableList

/**
 * Main Navigation Host for compose
 *
 * @param startDestination from [Route], e.g. we are opening compose screen from old activity
 * @param onFinish if the navController didn't have back stack then trigger this to close the activity,
 * maybe we have back stack activity from the old
 *
 */
// / Route.Welcome,
@Composable
fun MainNavigationHost(
    onFinish: () -> Unit,
    startDestination: Any = Route.Welcome,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination // since we still have multiple activity, we just start from the param
    ) {
        mainNavGraph(
            onNavigate = {
                if (it.isBack() && navController.previousBackStackEntry == null) {
                    onFinish()
                    return@mainNavGraph
                }

                if (it.isBack()) {
                    navController.navigateUp()
                    return@mainNavGraph
                }

                navController.navigate(route = it.destinationRoute!!) {
                    if (it.forcePopBackStack) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                    it.navOptionsBuilder?.invoke(this)
                }
            }
        )
    }
}

/**
 * navigation graph
 */
fun NavGraphBuilder.mainNavGraph(
    onNavigate: OnNavigate
) {
    composable<Route.Welcome> {
        WelcomeScreen(onNavigate = onNavigate)
    }
    composable<Route.Ready> {
        ReadyScreen(onNavigate = onNavigate)
    }
    composable<Route.TopUp> { navBackStackEntry ->
        val route: Route.TopUp = navBackStackEntry.toRoute()
        TopUpScreen(onNavigate = onNavigate)
    }
    composable<Route.SetPasscode> { navBackStackEntry ->
        val route: Route.SetPasscode = navBackStackEntry.toRoute()
        SetPasscodeScreen(
            onNavigate = onNavigate,
            passcode = route.passcode,
        )
    }
    composable<Route.Restore> { navBackStackEntry ->
        val route: Route.Restore = navBackStackEntry.toRoute()
        RestoreScreen(
            onNavigate = onNavigate,
            source = route.source,
        )
    }
    composable<Route.YourSeedWords> { navBackStackEntry ->
        val route: Route.YourSeedWords = navBackStackEntry.toRoute()
        YourSeedWordsScreen(
            onNavigate = onNavigate,
            seedWords = route.seedWords,
        )
    }

    composable<Route.YourEmojis> { navBackStackEntry ->
        val route: Route.YourEmojis = navBackStackEntry.toRoute()
        YourEmojisScreen(
            onNavigate = onNavigate,
            emojis = route.emojis.toImmutableList(),
        )
    }

    composable<Route.YourSeedProveIt> { navBackStackEntry ->
        val route: Route.YourSeedProveIt = navBackStackEntry.toRoute()
        YourSeedProveItScreen(
            onNavigate = onNavigate,
            seedWords = route.seedWords
        )
    }

    composable<Route.Main> { navBackStackEntry ->
        val route: Route.Main = navBackStackEntry.toRoute()
        MainScreen(onNavigate = onNavigate)
    }

    composable<Route.UnLock> { navBackStackEntry ->
        val route: Route.UnLock = navBackStackEntry.toRoute()
        UnLockScreen(onNavigate = onNavigate, isUpdatePin = route.isUpdatePin)
    }

    composable<Route.BuyReceive> { navBackStackEntry ->
        val route: Route.BuyReceive = navBackStackEntry.toRoute()
        ReceiveDialog(onDismissRequest = {})
    }

    composable<Route.Send> { navBackStackEntry ->
        val route: Route.Send = navBackStackEntry.toRoute()
        SendScreen(onNavigate = onNavigate, address = route.address)
    }

    composable<Route.GameHub> { navBackStackEntry ->
        val route: Route.GameHub = navBackStackEntry.toRoute()
        GameHubScreen(onNavigate = onNavigate)
    }
    composable<Route.MoonPayBuy> { navBackStackEntry ->
        val route: Route.MoonPayBuy = navBackStackEntry.toRoute()
        BuyReceiveScreen(onNavigate = onNavigate)
    }

    composable<Route.LinktreeWeb> { navBackStackEntry ->
        val route: Route.LinktreeWeb = navBackStackEntry.toRoute()
        WebModalScreen(
            onNavigate = onNavigate,
            url = BWConstants.LINKTREE_URL
        )
    }

    composable<Route.BitrefillWeb> { navBackStackEntry ->
        val route: Route.BitrefillWeb = navBackStackEntry.toRoute()
        WebModalScreen(
            onNavigate = onNavigate,
            url = route.url
        )
    }

    composable<Route.EmojiPickerPager>(
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(350)
            )
        },
    ) { navBackStackEntry ->
        val route: Route.EmojiPickerPager = navBackStackEntry.toRoute()
        EmojiPagerScreen(onNavigate = onNavigate)
    }
}
