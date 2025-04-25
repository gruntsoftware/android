package com.brainwallet.data

import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.tools.util.BRConstants
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

//TODO: need update this test after refactor
class BaseURLTests {

//    private val remoteConfigSource: RemoteConfigSource = mockk()
//    private lateinit var apiManager: BRApiManager
//
//    @Before
//    fun setUp() {
//        apiManager = spyk(BRApiManager(remoteConfigSource), recordPrivateCalls = true)
//    }
//
//    @Test
//    fun `invoke getBaseUrlProd with KEY_API_BASEURL_PROD_NEW_ENABLED true, then should return new baseUrlProd`() {
//        every { remoteConfigSource.getBoolean(RemoteConfigSource.KEY_API_BASEURL_PROD_NEW_ENABLED) } returns true
//
//        val actual = apiManager.baseUrlProd
//
//        assertEquals(BRConstants.BW_API_PROD_HOST, actual)
//    }
}


