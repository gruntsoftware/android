package com.brainwallet.tools.util

import android.app.Activity
import android.content.Context
import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.presenter.activities.util.ActivityUTILS
import com.brainwallet.tools.manager.BRApiManager
import com.platform.APIClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verifyAll
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ProdAPIManagerTests {
    private val remoteConfigSource: RemoteConfigSource = mockk()
    private lateinit var apiManager: BRApiManager

    @Before
    fun setUp() {
        apiManager = spyk(BRApiManager(remoteConfigSource), recordPrivateCalls = true)
    }

    @Test
    fun `invoke fetchRates, should return success with parsed JSONArray`() {
        val activity: Activity = mockk(relaxed = true)

        val responseString = """
            [
                {
                    "code": "USD",
                    "n": 416.81128312406213,
                    "price": "USD416.811283124062145364",
                    "name": "US Dollar"
                },
                {
                    "code": "EUR",
                    "n": 7841.21263788453,
                    "price": "Af7841.212637884529266812",
                    "name": "Euro"
                },
                {
                    "code": "GBP",
                    "n": 10592.359754930994,
                    "price": "ALL10592.359754930995026136",
                    "name": "British Pound"
                }
            ]
        """.trimIndent()
        mockkStatic(ActivityUTILS::class)
        mockkObject(APIClient.getInstance(activity))
        every {
            remoteConfigSource.getBoolean(RemoteConfigSource.KEY_API_BASEURL_PROD_NEW_ENABLED)
        } returns false
        every {
            apiManager invoke "createGETRequestURL" withArguments (listOf(
                activity as Context,
                BRConstants.BW_API_PROD_HOST
            ))
        } returns responseString
        every { ActivityUTILS.isMainThread() } returns false
        every { APIClient.getInstance(activity).getCurrentLocale(activity) } returns "en"

        val request = Request.Builder()
            .url(BRConstants.BW_API_PROD_HOST)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("User-agent", Utils.getAgentString(activity, "android/HttpURLConnection"))
            .get().build()
        every {
            APIClient.getInstance(activity).sendRequest(request, false, 0)
        } returns Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseString.toResponseBody())
            .build()

        val result = apiManager.fetchRates(activity)
        val jsonUSD = result.getJSONObject(154)
        val jsonEUR = result.getJSONObject(49)
        val jsonGBP = result.getJSONObject(52)

        assertEquals("USD", jsonUSD.optString("code"))
        assertEquals("US Dollar", jsonUSD.optString("name"))
        assertEquals("EUR", jsonEUR.optString("code"))
        assertEquals("Euro", jsonEUR.optString("name"))
        assertEquals("GBP", jsonGBP.optString("code"))
        assertEquals("British Pound Sterling", jsonGBP.optString("name"))

        ///DEV: Very flaky test not enough time for the response
        verifyAll {
            ActivityUTILS.isMainThread()
            APIClient.getInstance(activity).getCurrentLocale(activity)
            APIClient.getInstance(activity).sendRequest(any(), any(), any())
        }

    }
}