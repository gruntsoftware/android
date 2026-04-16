package com.brainwallet.di

import com.brainwallet.constants.BWConstants
import com.brainwallet.data.source.RemoteApiSource
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertFalse

class AppModuleTest {

    private lateinit var json: Json
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setUp() {
        json = AppModule.json
        okHttpClient = OkHttpClient.Builder().build()
    }

    // ── json ───────────────────────────────────────────────────────────────

    @Test
    fun `json ignores unknown keys`() {
        val config = AppModule.json.configuration
        assertTrue(config.ignoreUnknownKeys)
    }

    @Test
    fun `json has explicitNulls disabled`() {
        val config = AppModule.json.configuration
        assertFalse(config.explicitNulls)
    }

    @Test
    fun `json has prettyPrint enabled`() {
        val config = AppModule.json.configuration
        assertTrue(config.prettyPrint)
    }

    @Test
    fun `json deserializes unknown keys without throwing`() {
        @kotlinx.serialization.Serializable
        data class Simple(val name: String)

        val result = AppModule.json.decodeFromString<Simple>(
            """{"name":"test","unknownField":"ignored"}"""
        )

        assertEquals("test", result.name)
    }

    @Test
    fun `json serializes without explicit nulls`() {
        @kotlinx.serialization.Serializable
        data class WithNullable(val name: String, val value: String? = null)

        val result = AppModule.json.encodeToString(
            WithNullable.serializer(),
            WithNullable(name = "test")
        )

        assertFalse(result.contains("value"))
    }

    // ── provideRetrofit ────────────────────────────────────────────────────

    @Test
    fun `provideRetrofit returns non-null Retrofit instance`() {
        val retrofit = AppModule.provideRetrofit(json, okHttpClient)
        assertNotNull(retrofit)
    }

    @Test
    fun `provideRetrofit uses prod base url by default`() {
        val retrofit = AppModule.provideRetrofit(json, okHttpClient)
        assertEquals(BWConstants.BW_API_PROD_HOST + "/", retrofit.baseUrl().toString())
    }

    @Test
    fun `provideRetrofit uses custom base url when provided`() {
        val customUrl = "https://staging.api.brainwallet.com/"
        val retrofit = AppModule.provideRetrofit(json, okHttpClient, customUrl)
        assertEquals(customUrl, retrofit.baseUrl().toString())
    }

    @Test
    fun `provideRetrofit creates RemoteApiSource via provideApi`() {
        val retrofit = AppModule.provideRetrofit(json, okHttpClient)
        val api = AppModule.provideApi<RemoteApiSource>(retrofit)
        assertNotNull(api)
    }

    @Test
    fun `provideApi returns instance of requested type`() {
        val retrofit = AppModule.provideRetrofit(json, okHttpClient)
        val api = AppModule.provideApi<RemoteApiSource>(retrofit)
        assertTrue(api is RemoteApiSource)
    }

    @Test
    fun `provideRetrofit with different clients produces independent instances`() {
        val client1 = OkHttpClient.Builder().build()
        val client2 = OkHttpClient.Builder().build()

        val retrofit1 = AppModule.provideRetrofit(json, client1)
        val retrofit2 = AppModule.provideRetrofit(json, client2)

        assertNotNull(retrofit1)
        assertNotNull(retrofit2)
        assertTrue(retrofit1 !== retrofit2)
    }
}
