package com.brainwallet.data.repository
import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.presenter.entities.ShopCard
import com.brainwallet.presenter.entities.ShopConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber

@Serializable
data class ShopProxy(
    @SerialName("widget_url") val widget: String = "",
    @SerialName("cards") val shopCards: List<ShopCard> = emptyList()
)

interface ShopProxyRepository {
    val shopProxy: StateFlow<List<ShopProxy>>
    suspend fun refresh()
}

class ShopProxyRepositoryImpl(
    private val remoteConfigSource: RemoteConfigSource,
    private val json: Json,
) : ShopProxyRepository {

    private val _shopProxy = MutableStateFlow<List<ShopProxy>>(emptyList())
    override val shopProxy: StateFlow<List<ShopProxy>> = _shopProxy.asStateFlow()

    override suspend fun refresh() {
        runCatching { remoteConfigSource.fetchAndActivate() }
            .onFailure { Timber.e(it, "RemoteConfig fetch failed") }

        val rawJson = remoteConfigSource.getString(RemoteConfigSource.PATH_SHOP_CONTENT)
        Timber.d("ShopProxy rawJson: '$rawJson'")

        runCatching {
            val shopConfig = json.decodeFromString<ShopConfig>(rawJson)
            Timber.d("ShopConfig parsed: ${shopConfig.shopData}")
            _shopProxy.value = listOf(shopConfig.shopData).map {
                ShopProxy(
                    widget = it.widgetUrl,
                    shopCards = it.cards
                )
            }
        }.onFailure { Timber.e(it, "RemoteConfig parse failed") }
    }
}
