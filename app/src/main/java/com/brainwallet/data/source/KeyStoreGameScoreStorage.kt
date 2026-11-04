package com.brainwallet.data.source

import android.app.Application
import com.brainwallet.constants.BWConstants
import com.brainwallet.data.repository.GameScoreStorage
import com.brainwallet.tools.security.BRKeyStore

/**
 * Keeps game history in the Android Keystore-backed [BRKeyStore], the counterpart of the iOS
 * "brainwallet.gamescores" Keychain item. It is wiped with the rest of the keystore on wallet wipe.
 */
class KeyStoreGameScoreStorage(
    private val app: Application,
) : GameScoreStorage {

    override fun read(): String? =
        BRKeyStore.getGameScores(app, BWConstants.GAME_SCORES_REQUEST_CODE)

    override fun write(scoresJson: String) {
        BRKeyStore.putGameScores(scoresJson, app, BWConstants.GAME_SCORES_REQUEST_CODE)
    }
}
