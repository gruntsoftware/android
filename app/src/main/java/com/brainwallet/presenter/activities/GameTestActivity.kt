package com.brainwallet.presenter.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.brainwallet.R
import com.brainwallet.ui.composable.GameContainerComposeView

class GameTestActivity : AppCompatActivity() {
    
    private lateinit var gameContainerView: GameContainerComposeView
    private lateinit var loadGameButton: Button
    private lateinit var pauseResumeButton: Button
    private lateinit var restartButton: Button
    private lateinit var showHideButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_test)
        
        initializeViews()
        setupClickListeners()
        setupGameContainer()
    }
    
    private fun initializeViews() {
        gameContainerView = findViewById(R.id.gameContainerComposeView)
        loadGameButton = findViewById(R.id.loadGameButton)
        pauseResumeButton = findViewById(R.id.pauseResumeButton)
        restartButton = findViewById(R.id.restartButton)
        showHideButton = findViewById(R.id.showHideButton)
    }
    
    private fun setupClickListeners() {
        loadGameButton.setOnClickListener {
            gameContainerView.loadUnityGame()
            updateButtonStates()
            showToast("Loading Unity Game...")
        }
        
        pauseResumeButton.setOnClickListener {
            if (gameContainerView.isGamePaused()) {
                gameContainerView.resumeGame()
                showToast("Game Resumed")
            } else {
                gameContainerView.pauseGame()
                showToast("Game Paused")
            }
            updateButtonStates()
        }
        
        restartButton.setOnClickListener {
            gameContainerView.restartGame()
            updateButtonStates()
            showToast("Game Restarted")
        }
        
        showHideButton.setOnClickListener {
            if (gameContainerView.visibility == android.view.View.VISIBLE) {
                gameContainerView.visibility = android.view.View.GONE
                showHideButton.text = "Show Game Container"
            } else {
                gameContainerView.visibility = android.view.View.VISIBLE
                showHideButton.text = "Hide Game Container"
            }
        }
    }
    
    private fun setupGameContainer() {
        gameContainerView.setGameTitle("Brainwallet Test Game")
        updateButtonStates()
    }
    
    private fun updateButtonStates() {
        val isGameLoaded = gameContainerView.isGameLoaded()
        val isGamePaused = gameContainerView.isGamePaused()
        
        loadGameButton.isEnabled = !isGameLoaded
        pauseResumeButton.isEnabled = isGameLoaded
        restartButton.isEnabled = isGameLoaded
        
        pauseResumeButton.text = if (isGamePaused) "Resume Game" else "Pause Game"
        
        loadGameButton.text = if (isGameLoaded) "Game Loaded" else "Load Game"
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, GameTestActivity::class.java)
        }
    }
}