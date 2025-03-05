package com.brainwallet.ui.screens.home.composable

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.screens.home.composable.settingsrows.SecurityRowItem
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.BrainwalletTheme

@Composable
fun HomeSettingDrawerSheet(
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.fillMaxWidth(),
        drawerContainerColor = BrainwalletTheme.colors.surface,
        drawerContentColor = BrainwalletTheme.colors.content
    ) {

        Spacer(Modifier.height(70.dp)) //70.dp is hack since wa are using xml?

        //TODO
        SecurityRowItem()


        Spacer(Modifier.height(12.dp))

    }
}

/**
 * for backward compat with XML, for now we are using XML
 * will used by [activity_bread.xml]
 */

class HomeSettingDrawerComposeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AbstractComposeView(context, attrs) {
    @Composable
    override fun Content() {
        BrainwalletAppTheme { HomeSettingDrawerSheet() }
    }

}