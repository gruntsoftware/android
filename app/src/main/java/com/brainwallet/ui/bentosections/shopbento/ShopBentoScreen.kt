package com.brainwallet.ui.bentosections.shopbento

import androidx.compose.foundation.Image
import androidx.compose.runtime.getValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.constants.bentoCornerRadius
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.balanceGameBentoSurface
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ShopBentoScreen(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    viewModel: ShopBentoViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    ShopBentoScreen(
        state = state,
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
private fun ShopBentoScreen(
    state: ShopBentoState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    viewModel: ShopBentoViewModel = koinViewModel()
) {
    val storeBackground = R.drawable.store_05
    val logotypeBlack = R.drawable.logotype_bitrefill_blk
    val logotypeWhite = R.drawable.logotype_bitrefill_wht

    val logotypeBitrefill = if (state.darkMode) logotypeWhite else logotypeBlack
    val shopBentoState by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .balanceGameBentoSurface(isDarkMode = state.darkMode)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(storeBackground),
            contentDescription = "store_background",
            modifier = Modifier.fillMaxSize()
                .clip(RoundedCornerShape(bentoCornerRadius)),
            contentScale = ContentScale.Crop,
            alpha = if (state.darkMode) 1f else 0.0f
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.weight(0.2f))

                Text(
                    text = stringResource(R.string.shop_tagline),
                    color = if (state.darkMode) Color.White else Color.Black,
                    modifier = Modifier
                        .padding(12.dp),
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Start,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.4f),
                            offset = Offset(x = 3f, y = 3f),
                            blurRadius = 3f
                        )
                    ),
                )
                Spacer(modifier = Modifier.weight(0.2f))
                Image(
                    painter = painterResource(logotypeBitrefill),
                    contentDescription = "shop_logotype",
                    modifier = Modifier.fillMaxWidth(0.8f)
                        .padding(12.dp),
                    contentScale = ContentScale.Fit,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(bentoCornerRadius))

            ) {
                GiftCardsComposable(
                    imageURLOne = shopBentoState.cardImageURL1,
                    imageURLTwo = shopBentoState.cardImageURL2,
                    imageURLThree = shopBentoState.cardImageURL3,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview
@Composable
private fun ShopBentoScreenPreview() {
    Box(modifier = Modifier) {
        ShopBentoScreen()
    }
}
