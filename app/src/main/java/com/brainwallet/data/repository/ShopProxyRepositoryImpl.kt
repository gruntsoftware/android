package com.brainwallet.data.repository
import com.brainwallet.data.source.RemoteConfigSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
@Serializable
data class ShopCard(
    @SerialName("country_code") val countryCode: String = "",
    @SerialName("country_name") val countryName: String = "",
    @SerialName("product_slug") val productSlug: String = "",
    @SerialName("product_name") val productName: String = "",
    @SerialName("product_url") val productUrl: String = "",
    @SerialName("card_image_webp") val cardImageWebp: String = ""
)

@Serializable
data class ShopProxy(
    @SerialName("widget_url") val widget: String = "",
    @SerialName("cards") val cards: List<ShopCard> = emptyList()
)
interface ShopProxyRepository {
    val shopProxy: StateFlow<List<ShopProxy>>
    suspend fun refresh()
}

class ShopProxyRepositoryImpl(
    private val remoteConfigSource: RemoteConfigSource,
    private val json: Json,
    private val scope: CoroutineScope,
) : ShopProxyRepository {

    private val _shopProxy = MutableStateFlow<List<ShopProxy>>(emptyList())
    override val shopProxy: StateFlow<List<ShopProxy>> = _shopProxy.asStateFlow()

    init {
        scope.launch { refresh() }
    }
    override suspend fun refresh() {
        runCatching { remoteConfigSource.fetchAndActivate() }
            .onFailure { Timber.e(it, "RemoteConfig fetch failed") }

        val rawJson = remoteConfigSource.getString(RemoteConfigSource.PATH_SHOP_CONTENT)
        Timber.d("ShopProxy rawJson: '$rawJson'")

        runCatching {
            val wrapper = json.decodeFromString<ShopProxyWrapper>(rawJson)
            Timber.d("ShopProxy parsed: ${wrapper.shopProxy}")
            _shopProxy.value = listOf(wrapper.shopProxy)
        }.onFailure { Timber.e(it, "RemoteConfig parse failed: ${it.message}") }
    }

    @Serializable
    private data class ShopProxyWrapper(
        @SerialName("shop_data") val shopProxy: ShopProxy = ShopProxy()
    )
}
