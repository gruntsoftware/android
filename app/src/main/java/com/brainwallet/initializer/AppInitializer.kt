package com.brainwallet.initializer

import android.content.Context
import androidx.startup.Initializer
import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.di.AppModule
import com.brainwallet.di.remoteConfigModule
import com.brainwallet.domain.MessagingTopicUseCase
import com.grunt.brainwallet.core.presentation.KoinInitializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.ksp.generated.module
import kotlin.concurrent.thread
import com.brainwallet.gameinterface.gameModule

class AppInitializer(
    private val componentProvider: Components = object : Components {
        override val messagingTopicHandler: MessagingTopicUseCase by inject()
        override val remoteConfigSource: RemoteConfigSource by inject()
        override fun loadKoinModules() {
            loadKoinModules(
                listOf(
                    AppModule.dataModule,
                    AppModule.module,
                    remoteConfigModule,
                    gameModule
                )
            )
        }
    }
) : Initializer<Unit>, KoinComponent {

    override fun create(context: Context) {
        componentProvider.run {
            loadKoinModules()
            thread { remoteConfigSource.initialize() }
            thread { messagingTopicHandler.initialize() }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return listOf(KoinInitializer::class.java)
    }

    companion object {
        interface Components : KoinComponent {
            val messagingTopicHandler: MessagingTopicUseCase
            val remoteConfigSource: RemoteConfigSource
            fun loadKoinModules()
        }
    }
}
