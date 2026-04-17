package com.brainwallet.ui.bentosections.favouritesbento

import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brainwallet.ui.screens.main.MainViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.constants.favouriteSize
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.mainBentoSurface
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavouritesBentoScreen(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    viewModel: MainViewModel = koinViewModel()
) {
    var resizedAsOfFontSize by remember { mutableStateOf(12.sp) }
    val context = LocalContext.current
    val appSetting by viewModel.appSetting.collectAsStateWithLifecycle()
    val isDarkMode = appSetting.isDarkMode
    val favouriteArray = intArrayOf(1, 2, 3, 4)
    val opacityFactor = 0.3f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mainBentoSurface(isDarkMode)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(com.brainwallet.R.string.favourites_label).uppercase(),
                color = if (isDarkMode) Color.White else Color.Black,
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp
                ),
                modifier = Modifier
                    .padding(start = 1.dp, end = 1.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.padding(top = 4.dp)) {
                Box {
                    Column(
                        modifier = Modifier
                            .size(favouriteSize)
                            .offset(x = 0.dp)
                            .background(
                                color = DesignTheme.colors.affirm.copy(alpha = opacityFactor),
                                shape = CircleShape
                            )
                    ) {
                    }
                    Column(
                        modifier = Modifier
                            .size(favouriteSize)
                            .offset(x = 25.dp)
                            .background(
                                color = DesignTheme.colors.info.copy(alpha = opacityFactor),
                                shape = CircleShape
                            )

                    ) {
                    }
                    Column(
                        modifier = Modifier
                            .size(favouriteSize)
                            .offset(x = 50.dp)
                            .background(
                                color = DesignTheme.colors.warn.copy(alpha = opacityFactor),
                                shape = CircleShape
                            )

                    ) {
                    }
                    Column(
                        modifier = Modifier
                            .size(favouriteSize)
                            .offset(x = 75.dp)
                            .background(
                                color = DesignTheme.colors.error.copy(opacityFactor),
                                shape = CircleShape
                            ),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Plus",
                            tint = if (isDarkMode) {
                                Color.White.copy(0.1f)
                            } else {
                                Color.Black.copy(0.1f)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@PreviewLightDark
@Composable
private fun FavouritesBentoScreenPreview() {
    Box(modifier = Modifier.height(120.dp)) {
        FavouritesBentoScreen()
    }
}
