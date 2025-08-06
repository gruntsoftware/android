package com.brainwallet.ui.composable

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.brainwallet.ui.theme.BrainwalletTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameContainerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gameContainer_initialState_showsPlaceholder() {
        composeTestRule.setContent {
            BrainwalletTheme {
                GameContainer()
            }
        }

        composeTestRule.onNodeWithTag("game_container").assertIsDisplayed()
        composeTestRule.onNodeWithTag("placeholder_content").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unity Game Container").assertIsDisplayed()
        composeTestRule.onNodeWithTag("load_game_button").assertIsDisplayed()
    }

    @Test
    fun gameContainer_loadingState_showsLoadingIndicator() {
        val loadingState = GameContainerState(isLoading = true)

        composeTestRule.setContent {
            BrainwalletTheme {
                GameContainer(gameState = loadingState)
            }
        }

        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithTag("loading_text").assertIsDisplayed()
        composeTestRule.onNodeWithText("Loading Unity Game...").assertIsDisplayed()
    }

    @Test
    fun gameContainer_errorState_showsErrorMessage() {
        val errorState = GameContainerState(
            errorMessage = "Failed to load Unity game"
        )

        composeTestRule.setContent {
            BrainwalletTheme {
                GameContainer(gameState = errorState)
            }
        }

        composeTestRule.onNodeWithTag("error_container").assertIsDisplayed()
        composeTestRule.onNodeWithTag("error_message").assertTextContains("Failed to load Unity game")
        composeTestRule.onNodeWithTag("retry_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dismiss_button").assertIsDisplayed()
    }

    @Test
    fun gameContainer_loadedState_showsUnityContent() {
        val loadedState = GameContainerState(
            isUnityLoaded = true,
            gameTitle = "Test Game"
        )

        composeTestRule.setContent {
            BrainwalletTheme {
                GameContainer(gameState = loadedState)
            }
        }

        composeTestRule.onNodeWithTag("unity_game_content").assertIsDisplayed()
        composeTestRule.onNodeWithTag("unity_placeholder").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Game").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pause_resume_button").assertIsDisplayed()
    }

    @Test
    fun gameContainer_pausedState_showsPauseOverlay() {
        val pausedState = GameContainerState(
            isUnityLoaded = true,
            isGamePaused = true,
            gameTitle = "Test Game"
        )

        composeTestRule.setContent {
            BrainwalletTheme {
                GameContainer(gameState = pausedState)
            }
        }

        composeTestRule.onNodeWithTag("pause_overlay").assertIsDisplayed()
        composeTestRule.onNodeWithText("Game Paused").assertIsDisplayed()
        composeTestRule.onNodeWithTag("resume_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("restart_button").assertIsDisplayed()
    }

    @Test
    fun gameContainer_loadGameButton_triggersLoadAction() {
        var actionTriggered: GameContainerAction? = null

        composeTestRule.setContent {
            BrainwalletTheme {
                GameContainer(
                    onAction = { action -> actionTriggered = action }
                )
            }
        }

        composeTestRule.onNodeWithTag("load_game_button").performClick()

        assert(actionTriggered is GameContainerAction.LoadUnity)
    }

    @Test
    fun gameContainer_pauseButton_triggersPauseAction() {
        var actionTriggered: GameContainerAction? = null
        val loadedState = GameContainerState(isUnityLoaded = true)

        composeTestRule.setContent {
            BrainwalletTheme {
                GameContainer(
                    gameState = loadedState,
                    onAction = { action -> actionTriggered = action }
                )
            }
        }

        composeTestRule.onNodeWithTag("pause_resume_button").performClick()

        assert(actionTriggered is GameContainerAction.PauseGame)
    }

    @Test
    fun gameContainer_resumeButton_triggersResumeAction() {
        var actionTriggered: GameContainerAction? = null
        val pausedState = GameContainerState(
            isUnityLoaded = true,
            isGamePaused = true
        )

        composeTestRule.setContent {
            BrainwalletTheme {
                GameContainer(
                    gameState = pausedState,
                    onAction = { action -> actionTriggered = action }
                )
            }
        }

        composeTestRule.onNodeWithTag("pause_resume_button").performClick()

        assert(actionTriggered is GameContainerAction.ResumeGame)
    }

    @Test
    fun gameContainer_retryButton_triggersLoadAction() {
        var actionTriggered: GameContainerAction? = null
        val errorState = GameContainerState(
            errorMessage = "Test error"
        )

        composeTestRule.setContent {
            BrainwalletTheme {
                GameContainer(
                    gameState = errorState,
                    onAction = { action -> actionTriggered = action }
                )
            }
        }

        composeTestRule.onNodeWithTag("retry_button").performClick()

        assert(actionTriggered is GameContainerAction.LoadUnity)
    }

    @Test
    fun gameContainer_dismissButton_triggersDismissAction() {
        var actionTriggered: GameContainerAction? = null
        val errorState = GameContainerState(
            errorMessage = "Test error"
        )

        composeTestRule.setContent {
            BrainwalletTheme {
                GameContainer(
                    gameState = errorState,
                    onAction = { action -> actionTriggered = action }
                )
            }
        }

        composeTestRule.onNodeWithTag("dismiss_button").performClick()

        assert(actionTriggered is GameContainerAction.DismissError)
    }

    @Test
    fun gameContainer_restartButton_triggersRestartAction() {
        var actionTriggered: GameContainerAction? = null
        val pausedState = GameContainerState(
            isUnityLoaded = true,
            isGamePaused = true
        )

        composeTestRule.setContent {
            BrainwalletTheme {
                GameContainer(
                    gameState = pausedState,
                    onAction = { action -> actionTriggered = action }
                )
            }
        }

        composeTestRule.onNodeWithTag("restart_button").performClick()

        assert(actionTriggered is GameContainerAction.RestartGame)
    }
}