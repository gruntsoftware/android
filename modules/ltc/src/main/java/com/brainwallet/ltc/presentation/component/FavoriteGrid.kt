package com.brainwallet.ltc.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.design.component.effect.CardOpacityContainer
import com.brainwallet.design.component.widget.GridChip
import com.brainwallet.design.R as DesignR
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import com.grunt.brainwallet.core.presentation.theme.blue
import com.grunt.brainwallet.core.presentation.theme.cheddar
import com.grunt.brainwallet.core.presentation.theme.chili
import com.grunt.brainwallet.core.presentation.theme.grape
import com.grunt.brainwallet.core.presentation.theme.nearBlack
import com.grunt.brainwallet.core.presentation.theme.pesto
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun FavoriteGrid(
    modifier: Modifier = Modifier,
    favoriteContacts: PersistentList<FavoriteContactData> = defaultFavoriteContacts,
    onContactClick: (FavoriteContactData) -> Unit = {},
    onAddContactClick: () -> Unit = {}
) {
    CardOpacityContainer(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GridChip("TOP SECRET", modifier = Modifier.padding(bottom = 3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(-16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                favoriteContacts.forEach { contact ->
                    FavoriteContactAvatar(
                        contact = contact,
                        onClick = { onContactClick(contact) }
                    )
                }

                AddContactButton(
                    onClick = onAddContactClick
                )
            }
        }
    }
}

@Composable
private fun FavoriteContactAvatar(
    contact: FavoriteContactData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(contact.backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = contact.name.first().uppercase(),
            style = BrainwalletTheme.typography.labelMedium.copy(
                color = BrainwalletTheme.colors.surface,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AddContactButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(nearBlack)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = DesignR.drawable.ic_plus),
            contentDescription = "Add favorite contact",
            tint = BrainwalletTheme.colors.content.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
    }
}

data class FavoriteContactData(
    val id: String,
    val name: String,
    val backgroundColor: Color
)

private val avatarColorOptions = listOf(
    chili,
    pesto,
    grape,
    cheddar,
    blue
)

private val defaultFavoriteContacts = persistentListOf(
    FavoriteContactData(
        id = "alice",
        name = "Alice",
        backgroundColor = avatarColorOptions[0]
    ),
    FavoriteContactData(
        id = "bob",
        name = "Bob",
        backgroundColor = avatarColorOptions[1]
    ),
    FavoriteContactData(
        id = "charlie",
        name = "Charlie",
        backgroundColor = avatarColorOptions[2]
    )
)

@Composable
@PreviewLightDark
fun FavoriteGridPreview() {
    BrainwalletTheme(darkTheme = isSystemInDarkTheme()) {
        FavoriteGrid()
    }
}
