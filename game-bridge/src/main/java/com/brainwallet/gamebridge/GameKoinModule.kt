package com.brainwallet.gamebridge

import com.brainwallet.game.contract.GameSlot
import org.koin.dsl.module


val gameModule = module {
    single<GameSlot> { GdxGameSlot() }
}
