package com.brainwallet.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.brainwallet.ui.screens.inputwords.InputWordsScreen
import com.brainwallet.ui.screens.ready.ReadyScreen
import com.brainwallet.ui.screens.setpasscode.SetPasscodeScreen
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
@Composable
fun MainNavHost(
    startDestination: Any = Route.Welcome,
    onFinish: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination //since we still have multiple activity, we just start from the param
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
    composable<Route.SetPasscode> { navBackStackEntry ->
        val route: Route.SetPasscode = navBackStackEntry.toRoute()
        SetPasscodeScreen(onNavigate = onNavigate, passcode = route.passcode)
    }
    composable<Route.InputWords> { navBackStackEntry ->
        val route: Route.InputWords = navBackStackEntry.toRoute()
        InputWordsScreen(
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
    /**
     * for now, still using old activity & fragment [com.brainwallet.presenter.activities.BreadActivity]
     */
//    composable<Route.Home> {
//        HomeScreen(onNavigate = onNavigate)
//    }

    composable<Route.UnLock> {
        UnLockScreen(onNavigate = onNavigate)
    }

    //todo add more composable screens
}


