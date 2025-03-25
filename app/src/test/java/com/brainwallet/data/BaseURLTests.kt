package com.brainwallet.data

import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.tools.manager.APIManager
import com.brainwallet.tools.util.BRConstants
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BaseURLTests {

    private val remoteConfigSource: RemoteConfigSource = mockk()
    private lateinit var apiManager: APIManager

    @Before
    fun setUp() {
        apiManager = spyk(APIManager(remoteConfigSource), recordPrivateCalls = true)
    }

    @Test
    fun `invoke getPRODBaseURL with KEY_API_BASEURL_PROD_NEW_ENABLED true, then should return new getPRODBaseURL`() {
        every { remoteConfigSource.getBoolean(RemoteConfigSource.KEY_API_BASEURL_PROD_NEW_ENABLED) } returns true
        val actual = apiManager.getPRODBaseURL()
        assertEquals(BRConstants.BW_API_PROD_HOST, actual)
    }

    @Test
    fun `invoke getDEVBaseURL with KEY_API_BASEURL_DEV_NEW_ENABLED true, then should return new getDEVBaseURL`() {
        every { remoteConfigSource.getBoolean(RemoteConfigSource.KEY_API_BASEURL_DEV_NEW_ENABLED) } returns true
        val actual = apiManager.getDEVBaseURL()
        assertEquals(BRConstants.BW_API_DEV_HOST, actual)
    }
}


