package com.brainwallet.gameinterface

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.dsl.koinApplication

class GameKoinModuleTest {

    @Test
    fun `gameModule provides a GameSlot instance`() {
        val koin = koinApplication { modules(gameModule) }.koin

        val gameSlot = koin.get<GameSlot>()

        assertNotNull(gameSlot)
        assertTrue(gameSlot is GdxGameSlot)
    }

    @Test
    fun `gameModule provides GameSlot as a singleton`() {
        val koin = koinApplication { modules(gameModule) }.koin

        val first = koin.get<GameSlot>()
        val second = koin.get<GameSlot>()

        assertSame(first, second)
    }
}
