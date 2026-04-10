package com.brainwallet.data.repository
import com.brainwallet.data.source.RemoteConfigSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import timber.log.Timber

data class HubContent(
    @SerialName("content_name") val contentName: String,
    @SerialName("asset_url") val assetUrl: String,
    @SerialName("title") val title: String,
    @SerialName("subtitle") val subtitle: String,
    @SerialName("id") val id: Long,
)

interface HubContentRepository {
    val hubContent: StateFlow<List<HubContent>>
    suspend fun refresh()
}

class HubContentRepositoryImpl(
    private val remoteConfigSource: RemoteConfigSource,
    private val json: Json,
) : HubContentRepository {

    private val _hubContent = MutableStateFlow<List<HubContent>>(emptyList())
    override val hubContent: StateFlow<List<HubContent>> = _hubContent.asStateFlow()

    override suspend fun refresh() {
        runCatching { remoteConfigSource.fetchAndActivate() }
            .onFailure { Timber.e(it, "RemoteConfig fetch failed") }

        val rawJson = remoteConfigSource.getString(RemoteConfigSource.KEY_FEATURE_GAMEHUB_CONTENT)
        runCatching {
            val wrapper = json.decodeFromString<HubContentWrapper>(rawJson)
            _hubContent.value = wrapper.hubContent
        }.onFailure { Timber.e(it, "RemoteConfig parse failed") }
    }

    private data class HubContentWrapper(
        @SerialName("hubcontent") val hubContent: List<HubContent> = emptyList()
    )
}
