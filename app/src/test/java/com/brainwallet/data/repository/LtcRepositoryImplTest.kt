package com.brainwallet.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.brainwallet.data.source.PeerManagerSource
import com.brainwallet.data.source.RemoteApiSource
import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.data.source.response.GetMoonpaySignUrlResponse
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.tools.util.Utils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

class LtcRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var remoteApiSource: RemoteApiSource
    private lateinit var remoteConfigSource: RemoteConfigSource
    private lateinit var currencyDataSource: CurrencyDataSource
    private lateinit var peerManagerSource: PeerManagerSource
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var repository: LtcRepositoryImpl

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        remoteApiSource = mockk(relaxed = true)
        remoteConfigSource = mockk(relaxed = true)
        currencyDataSource = mockk(relaxed = true)
        peerManagerSource = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)
        okHttpClient = mockk()

        every { currencyDataSource.getAllCurrencies(any()) } returns emptyList()

        mockkStatic(Utils::class)
        every { Utils.getEncryptedAgentString(any()) } returns "fake-agent-string"

        repository = LtcRepositoryImpl(
            context = context,
            remoteApiSource = remoteApiSource,
            remoteConfigSource = remoteConfigSource,
            currencyDataSource = currencyDataSource,
            peerManagerSource = peerManagerSource,
            // cancelled scope: the sync-loop launched in init{} must not run during these tests
            repositoryScope = CoroutineScope(Job().apply { cancel() }),
            sharedPreferences = sharedPreferences,
            okHttpClient = okHttpClient,
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun stubResponse(isSuccessful: Boolean, body: String?) {
        val call = mockk<Call>()
        val response = mockk<Response>(relaxed = true)
        every { okHttpClient.newCall(any()) } returns call
        every { call.execute() } returns response
        every { response.isSuccessful } returns isSuccessful
        if (body != null) {
            val responseBody = mockk<ResponseBody>()
            every { responseBody.string() } returns body
            every { response.body } returns responseBody
        } else {
            every { response.body } returns null
        }
    }

    // ── fetchUserIpAddress ────────────────────────────────────────────────

    @Test
    fun `fetchUserIpAddress returns trimmed ip on success`() = runBlocking {
        stubResponse(isSuccessful = true, body = "203.0.113.5\n")

        val result = repository.fetchUserIpAddress()

        assertEquals("203.0.113.5", result)
    }

    @Test
    fun `fetchUserIpAddress returns empty string when response unsuccessful`() = runBlocking {
        stubResponse(isSuccessful = false, body = "error")

        val result = repository.fetchUserIpAddress()

        assertEquals("", result)
    }

    @Test
    fun `fetchUserIpAddress returns empty string when body is null`() = runBlocking {
        stubResponse(isSuccessful = true, body = null)

        val result = repository.fetchUserIpAddress()

        assertEquals("", result)
    }

    @Test
    fun `fetchUserIpAddress returns empty string when call throws`() = runBlocking {
        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } returns call
        every { call.execute() } throws IOException("network down")

        val result = repository.fetchUserIpAddress()

        assertEquals("", result)
    }

    // ── fetchMoonpaySignedUrl ────────────────────────────────────────────────

    // fetchMoonpaySignedUrl pipes the backend's signedUrl through android.net.Uri
    // (via the toUri()/buildUpon() KTX chain), which isn't mockable on the plain
    // android.jar stub used by JVM unit tests — stub it out so the tests can focus
    // on the params passed upstream, which is the thing under test here.
    private fun stubUriConstruction(returnedUrlString: String) {
        mockkStatic(Uri::class)
        val mockUri = mockk<Uri>(relaxed = true)
        val mockBuilder = mockk<Uri.Builder>(relaxed = true)
        every { Uri.parse(any()) } returns mockUri
        every { mockUri.buildUpon() } returns mockBuilder
        every { mockBuilder.build() } returns mockUri
        every { mockUri.toString() } returns returnedUrlString
    }

    @Test
    fun `fetchMoonpaySignedUrl includes a freshly fetched ipAddress in the request params`() = runBlocking {
        stubUriConstruction("https://buy.moonpay.com/signed")
        stubResponse(isSuccessful = true, body = "198.51.100.7")

        val paramsSlot = slot<Map<String, String>>()
        coEvery {
            remoteApiSource.getMoonpaySignedUrl(capture(paramsSlot))
        } returns GetMoonpaySignUrlResponse(signedUrl = "https://buy.moonpay.com/signed")

        repository.fetchMoonpaySignedUrl(mapOf("walletAddress" to "LTC_FAKE_ADDRESS"))

        assertEquals("198.51.100.7", paramsSlot.captured["ipAddress"])
    }

    @Test
    fun `fetchMoonpaySignedUrl still succeeds with empty ipAddress when ip lookup fails`() = runBlocking {
        stubUriConstruction("https://buy.moonpay.com/signed")
        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } returns call
        every { call.execute() } throws IOException("network down")

        val paramsSlot = slot<Map<String, String>>()
        coEvery {
            remoteApiSource.getMoonpaySignedUrl(capture(paramsSlot))
        } returns GetMoonpaySignUrlResponse(signedUrl = "https://buy.moonpay.com/signed")

        val result = repository.fetchMoonpaySignedUrl(mapOf("walletAddress" to "LTC_FAKE_ADDRESS"))

        assertEquals("", paramsSlot.captured["ipAddress"])
        assertEquals("https://buy.moonpay.com/signed", result)
    }

    @Test
    fun `fetchMoonpaySignedUrl does not let a caller-supplied ipAddress override the fresh lookup`() = runBlocking {
        stubUriConstruction("https://buy.moonpay.com/signed")
        stubResponse(isSuccessful = true, body = "198.51.100.7")

        val paramsSlot = slot<Map<String, String>>()
        coEvery {
            remoteApiSource.getMoonpaySignedUrl(capture(paramsSlot))
        } returns GetMoonpaySignUrlResponse(signedUrl = "https://buy.moonpay.com/signed")

        repository.fetchMoonpaySignedUrl(mapOf("walletAddress" to "LTC_FAKE_ADDRESS", "ipAddress" to "stale-value"))

        assertEquals("198.51.100.7", paramsSlot.captured["ipAddress"])
    }
}
