package com.brainwallet.currency

import android.app.Activity
import android.content.Context
import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.presenter.activities.util.ActivityUTILS
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.tools.util.Utils
import com.platform.APIClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test

//TODO: need update this test after refactor
class BackupRateFetchTests {
//    private val remoteConfigSource: RemoteConfigSource = mockk()
//    private lateinit var apiManager: BRApiManager
//
//    @Before
//    fun setUp() {
//        apiManager = spyk(BRApiManager(remoteConfigSource), recordPrivateCalls = true)
//    }
//
//    @Test
//    fun `invoke backupFetchRates, should return success with parsed JSONArray`() {
//        val activity: Activity = mockk(relaxed = true)
//        val responseString = """
//            [
//                {
//                  "code" : "CZK",
//                  "price" : "Kč3065.541255",
//                  "name" : "Czech Republic Koruna",
//                  "n" : 3065.541255
//                },
//                {
//                  "code" : "KRW",
//                  "price" : "₩183797.935875",
//                  "name" : "South Korean Won",
//                  "n" : 183797.935875
//                },
//                {
//                  "code" : "BYN",
//                  "price" : "Br418.909259625",
//                  "n" : 418.909259625
//                },
//                {
//                  "code" : "CNY",
//                  "price" : "CN¥927.7974375",
//                  "name" : "Chinese Yuan",
//                  "n" : 927.7974375
//                }
//            ]
//        """.trimIndent()
//        mockkStatic(ActivityUTILS::class)
//        mockkObject(APIClient.getInstance(activity))
//        every {
//            remoteConfigSource.getBoolean(RemoteConfigSource.KEY_API_BASEURL_PROD_NEW_ENABLED)
//        } returns false
//        every {
//            apiManager invoke "createGETRequestURL" withArguments (listOf(
//                activity as Context,
//                BRConstants.BW_API_DEV_HOST
//            ))
//        } returns responseString
//        every { ActivityUTILS.isMainThread() } returns false
//        every { APIClient.getInstance(activity).getCurrentLocale(activity) } returns "en"
//
//        val request = Request.Builder()
//            .url(BRConstants.BW_API_DEV_HOST)
//            .header("Content-Type", "application/json")
//            .header("Accept", "application/json")
//            .header("User-agent", Utils.getAgentString(activity, "android/HttpURLConnection"))
//            .get().build()
//        every {
//            APIClient.getInstance(activity).sendRequest(request, false, 0)
//        } returns Response.Builder()
//            .request(request)
//            .protocol(Protocol.HTTP_1_1)
//            .code(200)
//            .message("OK")
//            .body(responseString.toResponseBody())
//            .build()
//
//    }
}