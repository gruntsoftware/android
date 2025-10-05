package com.brainwallet.initializer

import android.content.Context
import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.domain.MessagingTopicUseCase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import ltd.grunt.brainwallet.core.presentation.KoinInitializer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AppInitializerTest {

    @MockK
    lateinit var messagingTopicHandler: MessagingTopicUseCase

    @MockK
    lateinit var remoteConfigSource: RemoteConfigSource

    @RelaxedMockK
    lateinit var context: Context

    @MockK
    private lateinit var components: AppInitializer.Companion.Components
    private lateinit var initializer: AppInitializer

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every {
            components.messagingTopicHandler
        } returns messagingTopicHandler
        every {
            components.remoteConfigSource
        } returns remoteConfigSource
        initializer = AppInitializer(components)
    }

    @Test
    fun `should initialize remoteConfigSource and messagingTopicHandler`() {
        initializer.create(context)

        verify { components.loadKoinModules() }
        verify { remoteConfigSource.initialize() }
        verify { messagingTopicHandler.initialize() }
    }

    @Test
    fun `dependencies should include KoinInitializer`() {
        val deps = initializer.dependencies()
        assert(deps.contains(KoinInitializer::class.java))
    }
}
