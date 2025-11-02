package com.brainwallet.tutorial.presentation.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.design.presentation.component.effect.CardOpacityContainer
import com.brainwallet.design.presentation.component.widget.GridChip
import com.brainwallet.design.presentation.component.widget.PaginationDot
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class TutorialData(
    val title: String,
    val body: String
) {
    companion object {
        val default = persistentListOf(
            TutorialData(
                "Only you know the meaning!",
                "Remember your seed phrase and train yourself"
            )
        )
    }
}

@Composable
fun TutorialGrid(
    modifier: Modifier = Modifier,
    tutorials: PersistentList<TutorialData> = TutorialData.default,
) {
    val pagerState = rememberPagerState(pageCount = { tutorials.size })

    CardOpacityContainer(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            GridChip("TUTORIALS", modifier = Modifier.padding(bottom = 3.dp))
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val currentTutorial = tutorials[page]

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentTutorial.title,
                        style = BrainwalletTheme.typography.headlineMedium.copy(
                            color = BrainwalletTheme.colors.content,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentTutorial.body,
                        style = BrainwalletTheme.typography.bodyMedium.copy(
                            color = BrainwalletTheme.colors.content.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tutorials.forEachIndexed { index, _ ->
                    PaginationDot(
                        isActive = index == pagerState.currentPage,
                        modifier = Modifier.padding(horizontal = 3.dp)
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Preview
@Composable
private fun TutorialGridPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        TutorialGrid()
    }
}
