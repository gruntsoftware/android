package com.brainwallet.ui.composable

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.brainwallet.ui.theme.BrainwalletTheme

class GameContainerComposeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ComposeView(context, attrs, defStyleAttr) {

    private var gameContainerState by mutableStateOf(GameContainerState())
    
    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        
        setContent {
            BrainwalletTheme {
                GameContainer(
                    gameState = gameContainerState,
                    onAction = ::handleGameAction
                )
            }
        }
    }
    
    fun updateGameState(newState: GameContainerState) {
        gameContainerState = newState
    }
    
    fun loadUnityGame() {
        gameContainerState = gameContainerState.copy(isLoading = true)
        
        simulateUnityLoading { success ->
            if (success) {
                gameContainerState = gameContainerState.copy(
                    isLoading = false,
                    isUnityLoaded = true,
                    errorMessage = null
                )
            } else {
                gameContainerState = gameContainerState.copy(
                    isLoading = false,
                    isUnityLoaded = false,
                    errorMessage = "Failed to load Unity game. Please try again."
                )
            }
        }
    }
    
    fun pauseGame() {
        if (gameContainerState.isUnityLoaded) {
            gameContainerState = gameContainerState.copy(isGamePaused = true)
        }
    }
    
    fun resumeGame() {
        if (gameContainerState.isUnityLoaded) {
            gameContainerState = gameContainerState.copy(isGamePaused = false)
        }
    }
    
    fun restartGame() {
        if (gameContainerState.isUnityLoaded) {
            gameContainerState = gameContainerState.copy(isGamePaused = false)
        }
    }
    
    fun setGameTitle(title: String) {
        gameContainerState = gameContainerState.copy(gameTitle = title)
    }
    
    fun isGameLoaded(): Boolean = gameContainerState.isUnityLoaded
    
    fun isGamePaused(): Boolean = gameContainerState.isGamePaused
    
    fun hasError(): Boolean = gameContainerState.errorMessage != null
    
    private fun handleGameAction(action: GameContainerAction) {
        when (action) {
            is GameContainerAction.LoadUnity -> loadUnityGame()
            is GameContainerAction.PauseGame -> pauseGame()
            is GameContainerAction.ResumeGame -> resumeGame()
            is GameContainerAction.RestartGame -> restartGame()
            is GameContainerAction.DismissError -> {
                gameContainerState = gameContainerState.copy(errorMessage = null)
            }
        }
    }
    
    private fun simulateUnityLoading(onComplete: (Boolean) -> Unit) {
        postDelayed({
            val success = (0..10).random() > 2
            onComplete(success)
        }, 2000)
    }
}