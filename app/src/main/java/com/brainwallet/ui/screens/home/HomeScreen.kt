@file:OptIn(ExperimentalMaterial3Api::class)

package com.brainwallet.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.rememberFragmentState
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.presenter.history.HistoryFragment

//TODO: wip
@Composable
fun HomeScreen(
    onNavigate: OnNavigate
) {
    Scaffold(
        topBar = { HomeTopBar() },
        bottomBar = { HomeBottomNavBar() },
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            val fragmentState = rememberFragmentState()
            //using old fragment
            AndroidFragment<HistoryFragment>(
                fragmentState = fragmentState
            )
        }
    }
}

@Composable
fun HomeTopBar() {
    TopAppBar(
        title = {
            //
        },
        actions = {
            IconButton(onClick = {

            }) {
                Icon(Icons.Default.Menu, contentDescription = "menu")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = NavigationBarDefaults.containerColor
        )
    )
}

@Composable
fun HomeBottomNavBar() {
    NavigationBar {
        listOf("history", "send", "receive", "buy").forEach {
            NavigationBarItem(
                selected = false,
                label = {
                    Text(it)
                },
                icon = {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = it
                    )
                },
                onClick = {
                    //
                }
            )
        }
    }
}
