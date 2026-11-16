package com.brainwallet.initializer

import android.content.Context
import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.domain.MessagingTopicUseCase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import com.grunt.brainwallet.core.presentation.KoinInitializer
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
        // Both initialize() calls run on background threads (`thread { }` in AppInitializer.create),
        // so wait for them instead of asserting immediately — the immediate check raced on slow CI.
        verify(timeout = THREAD_TIMEOUT_MS) { remoteConfigSource.initialize() }
        verify(timeout = THREAD_TIMEOUT_MS) { messagingTopicHandler.initialize() }
    }

    @Test
    fun `dependencies should include KoinInitializer`() {
        val deps = initializer.dependencies()
        assert(deps.contains(KoinInitializer::class.java))
    }

    private companion object {
        const val THREAD_TIMEOUT_MS = 5_000L
    }
}
