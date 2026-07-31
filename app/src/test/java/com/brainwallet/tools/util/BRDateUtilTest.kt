package com.brainwallet.tools.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BRDateUtilTest {

    private fun minutesAgo(minutes: Long): Date = Date(System.currentTimeMillis() - minutes * 60 * 1000)

    private fun hoursAgo(hours: Long): Date = Date(System.currentTimeMillis() - hours * 60 * 60 * 1000)

    private fun daysAgo(days: Long): Date = Date(System.currentTimeMillis() - days * 24 * 60 * 60 * 1000)

    @Test
    fun `given date under a minute ago, then formats as zero minutes`() {
        assertEquals("0 m", BRDateUtil.getCustomSpan(Date()))
    }

    @Test
    fun `given date several minutes ago, then formats in minutes`() {
        assertEquals("15 m", BRDateUtil.getCustomSpan(minutesAgo(15)))
    }

    @Test
    fun `given date just under an hour ago, then still formats in minutes`() {
        assertEquals("59 m", BRDateUtil.getCustomSpan(minutesAgo(59)))
    }

    @Test
    fun `given date exactly one hour ago, then formats in hours`() {
        assertEquals("1 h", BRDateUtil.getCustomSpan(hoursAgo(1)))
    }

    @Test
    fun `given date just under a day ago, then still formats in hours`() {
        assertEquals("23 h", BRDateUtil.getCustomSpan(hoursAgo(23)))
    }

    @Test
    fun `given date exactly one day ago, then formats in days`() {
        assertEquals("1 d", BRDateUtil.getCustomSpan(daysAgo(1)))
    }

    @Test
    fun `given date just under a week ago, then still formats in days`() {
        assertEquals("6 d", BRDateUtil.getCustomSpan(daysAgo(6)))
    }

    @Test
    fun `given date a week or more ago, then formats as month and day`() {
        val date = daysAgo(7)
        val expected = SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)

        assertEquals(expected, BRDateUtil.getCustomSpan(date))
    }
}
