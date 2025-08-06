package com.brainwallet.presenter.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.brainwallet.R
import com.brainwallet.ui.composable.GameContainerComposeView
import com.brainwallet.ui.composable.GameContainerState

class GameFragment : Fragment() {
    
    private var gameContainerView: GameContainerComposeView? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        gameContainerView = view.findViewById(R.id.gameContainerComposeView)
        setupGameContainer()
    }
    
    private fun setupGameContainer() {
        gameContainerView?.apply {
            setGameTitle("Brainwallet Unity Game")
            updateGameState(
                GameContainerState(
                    gameTitle = "Brainwallet Unity Game"
                )
            )
        }
    }
    
    fun loadGame() {
        gameContainerView?.loadUnityGame()
    }
    
    fun pauseGame() {
        gameContainerView?.pauseGame()
    }
    
    fun resumeGame() {
        gameContainerView?.resumeGame()
    }
    
    fun restartGame() {
        gameContainerView?.restartGame()
    }
    
    fun isGameLoaded(): Boolean {
        return gameContainerView?.isGameLoaded() ?: false
    }
    
    fun isGamePaused(): Boolean {
        return gameContainerView?.isGamePaused() ?: false
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        gameContainerView = null
    }
    
    companion object {
        fun newInstance(): GameFragment {
            return GameFragment()
        }
    }
}