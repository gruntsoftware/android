package com.brainwallet.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.brainwallet.ui.screens.buyreceive.BuyReceiveScreen
import com.brainwallet.ui.screens.gamehub.GameHubScreen
import com.brainwallet.ui.screens.home.MainScreen
import com.brainwallet.ui.screens.main.history.HistoryScreen
import com.brainwallet.ui.screens.main.history.receive.ReceiveDialog
import com.brainwallet.ui.screens.restore.RestoreScreen
import com.brainwallet.ui.screens.ready.ReadyScreen
import com.brainwallet.ui.screens.send.SendScreen
import com.brainwallet.ui.screens.setpasscode.SetPasscodeScreen
import com.brainwallet.ui.screens.topup.TopUpScreen
import com.brainwallet.ui.screens.unlock.UnLockScreen
import com.brainwallet.ui.screens.welcome.WelcomeScreen
import com.brainwallet.ui.screens.yourseedproveit.YourSeedProveItScreen
import com.brainwallet.ui.screens.yourseedwords.YourSeedWordsScreen

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

    composable<Route.History> { navBackStackEntry ->
        val route: Route.History = navBackStackEntry.toRoute()
        HistoryScreen(onNavigate = onNavigate)
    }

    composable<Route.Send> { navBackStackEntry ->
        val route: Route.Send = navBackStackEntry.toRoute()
        SendScreen(onNavigate = onNavigate)
    }

    composable<Route.GameHub> { navBackStackEntry ->
        val route: Route.GameHub = navBackStackEntry.toRoute()
        GameHubScreen(onNavigate = onNavigate)
    }
    composable<Route.MoonPayWeb> { navBackStackEntry ->
        val route: Route.GameHub = navBackStackEntry.toRoute()
        BuyReceiveScreen(onNavigate = onNavigate)
    }
}
