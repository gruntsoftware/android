package com.brainwallet.util

import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test

class VersionCodeProviderTest {

    @Test
    fun `Given a version code, When getVersionCode called, Then return that version code`() {
        val versionCodeGetter: () -> Int = mockk()
        every { versionCodeGetter.invoke() } returns 123
        val provider = VersionCodeProvider(versionCodeGetter = versionCodeGetter)

        val result = provider.getVersionCode()
        assertEquals(123, result)
    }

    @Test
    fun `Given a version name, When getVersionName called, Then return that version name`() {
        val versionNameGetter: () -> String = mockk()
        every { versionNameGetter.invoke() } returns "1.2.3"
        val provider = VersionCodeProvider(versionNameGetter = versionNameGetter)

        val result = provider.getVersionName()
        assertEquals("1.2.3", result)
    }

    @Test
    fun `Given version code and name, When getFormatted called, Then return formatted string`() {
        val versionCodeGetter: () -> Int = mockk()
        val versionNameGetter: () -> String = mockk()
        every { versionCodeGetter.invoke() } returns 456
        every { versionNameGetter.invoke() } returns "2.0.0"
        val provider = VersionCodeProvider(
            versionCodeGetter = versionCodeGetter,
            versionNameGetter = versionNameGetter
        )

        val result = provider.getFormatted()
        assertEquals("2.0.0 (456)", result)
    }
}
