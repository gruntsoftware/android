package com.brainwallet.ui.screens.emojis

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.domain.EmojiLevelUp
import com.brainwallet.tools.security.AuthManager
import com.brainwallet.ui.composable.PinInputComponent
import com.brainwallet.ui.composable.rememberPinInputState
import com.brainwallet.ui.screens.main.MainViewModel
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.bentoModalDarkGradient
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import kotlinx.collections.immutable.ImmutableList
import org.koin.compose.viewmodel.koinViewModel

/**
 * Shown from the Game Hub bento / Game tab once the user has 3 emojis and has played
 * [EmojiLevelUp.GAMES_TO_UNLOCK_NEXT_TRIPLET] games: PIN first, then emojis 4 - 6
 * matched to seed words 4 - 6. Mirrors iOS `SetSixthEmojiView`.
 */
@Composable
fun SetNextEmojisScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel(),
    onEmojisSaved: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    // Seed words are only fetched after the PIN is verified
    var seedWords by remember { mutableStateOf<ImmutableList<String>?>(null) }
    val words = seedWords

    if (words == null) {
        EnterPinForNextEmojis(
            modifier = modifier,
            onPinVerified = { seedWords = viewModel.walletManager.getSeedWords() },
            onDismiss = onDismiss,
        )
    } else {
        PickEmojisScreen(
            modifier = modifier,
            firstThreeWords = words,
            wordOffset = EmojiLevelUp.NEXT_TRIPLET_WORD_OFFSET,
            title = stringResource(R.string.set_next_emojis_title),
            description = stringResource(R.string.set_next_emojis_description),
            onSaveEmojis = { picked -> viewModel.saveNextEmojis(picked) },
            onEmojisPicked = { saved -> if (saved) onEmojisSaved() else onDismiss() },
        )
    }
}

@Composable
private fun EnterPinForNextEmojis(
    onPinVerified: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    // AuthManager casts to Activity when it disables the wallet after repeated failures,
    // and the sheet's LocalContext isn't guaranteed to be the Activity
    val authContext = LocalActivity.current ?: context
    val pinState = rememberPinInputState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = bentoModalDarkGradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(id = R.drawable.btn_clear),
                        contentDescription = stringResource(R.string.clear),
                        tint = Color.White
                    )
                }
            }

            Text(
                modifier = Modifier.padding(horizontal = 30.dp),
                text = stringResource(R.string.set_next_emojis_title),
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp,
                    color = Color.White
                ),
            )

            Text(
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 16.dp),
                text = stringResource(R.string.set_next_emojis_enter_pin),
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = Color.White
                ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Dark theme keeps the dots and keypad white on the purple gradient
            BrainwalletTheme(darkTheme = true) {
                PinInputComponent(
                    pinState = pinState,
                    onPinComplete = { pin ->
                        val auth = AuthManager.getInstance()
                        if (auth.checkAuth(pin, authContext)) {
                            auth.authSuccess(authContext)
                            onPinVerified()
                        } else {
                            pinState.clear()
                            Toast.makeText(context, R.string.incorrect_passcode, Toast.LENGTH_SHORT).show()
                        }
                    },
                )
            }
        }
    }
}
