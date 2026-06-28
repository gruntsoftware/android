package com.brainwallet.gameinterface
import org.koin.dsl.module

val gameModule = module {
    single<GameSlot> { GdxGameSlot() }
}
