package com.brainwallet.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class GameContainerState(
    val isUnityLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val gameTitle: String = "Unity Game",
    val isGamePaused: Boolean = false
)

sealed class GameContainerAction {
    object LoadUnity : GameContainerAction()
    object PauseGame : GameContainerAction()
    object ResumeGame : GameContainerAction()
    object RestartGame : GameContainerAction()
    object DismissError : GameContainerAction()
}

@Composable
fun GameContainer(
    modifier: Modifier = Modifier,
    gameState: GameContainerState = GameContainerState(),
    onAction: (GameContainerAction) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("game_container")
    ) {
        when {
            gameState.isLoading -> {
                GameLoadingContent(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            gameState.errorMessage != null -> {
                GameErrorContent(
                    errorMessage = gameState.errorMessage,
                    onRetry = { onAction(GameContainerAction.LoadUnity) },
                    onDismiss = { onAction(GameContainerAction.DismissError) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            gameState.isUnityLoaded -> {
                UnityGameContent(
                    gameTitle = gameState.gameTitle,
                    isGamePaused = gameState.isGamePaused,
                    onPause = { onAction(GameContainerAction.PauseGame) },
                    onResume = { onAction(GameContainerAction.ResumeGame) },
                    onRestart = { onAction(GameContainerAction.RestartGame) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            else -> {
                GamePlaceholderContent(
                    onLoadGame = { onAction(GameContainerAction.LoadUnity) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun GameLoadingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(48.dp)
                .testTag("loading_indicator"),
            color = Color.White,
            strokeWidth = 4.dp
        )
        
        Text(
            text = "Loading Unity Game...",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("loading_text")
        )
    }
}

@Composable
private fun GameErrorContent(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Red.copy(alpha = 0.1f))
            .border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(20.dp)
            .testTag("error_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Game Error",
            color = Color.Red,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = errorMessage,
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("error_message")
        )
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            ),
            modifier = Modifier.testTag("retry_button")
        ) {
            Text(text = "Retry")
        }
        
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            modifier = Modifier.testTag("dismiss_button")
        ) {
            Text(text = "Dismiss")
        }
    }
}

@Composable
private fun UnityGameContent(
    gameTitle: String,
    isGamePaused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.testTag("unity_game_content")) {
        UnityGamePlaceholder(
            gameTitle = gameTitle,
            modifier = Modifier.fillMaxSize()
        )
        
        if (isGamePaused) {
            GamePausedOverlay(
                onResume = onResume,
                onRestart = onRestart,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        GameControlsOverlay(
            isGamePaused = isGamePaused,
            onPause = onPause,
            onResume = onResume,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

@Composable
private fun UnityGamePlaceholder(
    gameTitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.DarkGray)
            .testTag("unity_placeholder"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = gameTitle,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Unity Game Will Load Here",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GamePausedOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.7f))
            .testTag("pause_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Game Paused",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("resume_button")
            ) {
                Text(text = "Resume")
            }
            
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("restart_button")
            ) {
                Text(text = "Restart")
            }
        }
    }
}

@Composable
private fun GameControlsOverlay(
    isGamePaused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = if (isGamePaused) onResume else onPause,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black.copy(alpha = 0.6f),
            contentColor = Color.White
        ),
        modifier = modifier
            .padding(16.dp)
            .testTag("pause_resume_button")
    ) {
        Text(text = if (isGamePaused) "Resume" else "Pause")
    }
}

@Composable
private fun GamePlaceholderContent(
    onLoadGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Gray.copy(alpha = 0.2f))
            .border(2.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(32.dp)
            .testTag("placeholder_content"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "🎮",
            fontSize = 48.sp,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Unity Game Container",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Ready to embed Unity game.\nClick below to simulate loading.",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        
        Button(
            onClick = onLoadGame,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            modifier = Modifier.testTag("load_game_button")
        ) {
            Text(text = "Load Game")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GameContainerPreview() {
    MaterialTheme {
        GameContainer()
    }
}

@Preview(showBackground = true)
@Composable
private fun GameContainerLoadingPreview() {
    MaterialTheme {
        GameContainer(
            gameState = GameContainerState(isLoading = true)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GameContainerErrorPreview() {
    MaterialTheme {
        GameContainer(
            gameState = GameContainerState(
                errorMessage = "Failed to load Unity game. Please check your connection."
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GameContainerLoadedPreview() {
    MaterialTheme {
        GameContainer(
            gameState = GameContainerState(
                isUnityLoaded = true,
                gameTitle = "Brainwallet Game"
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GameContainerPausedPreview() {
    MaterialTheme {
        GameContainer(
            gameState = GameContainerState(
                isUnityLoaded = true,
                isGamePaused = true,
                gameTitle = "Brainwallet Game"
            )
        )
    }
}