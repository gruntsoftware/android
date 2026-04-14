package com.brainwallet.ui.screens.settings
import com.brainwallet.tools.manager.FeeManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
class SettingsStateTest {

    @Test
    fun `darkMode is true by default`() {
        assertTrue(SettingsState().darkMode)
    }

    @Test
    fun `lastSyncMetadata is null by default`() {
        // null = no sync has occurred yet
        assertNull(SettingsState().lastSyncMetadata)
    }

    @Test
    fun `selectedCurrency default rate is sentinel value indicating unfetched`() {
        assertEquals(-1f, SettingsState().selectedCurrency.rate, 0.0001f)
    }

    @Test
    fun `selectedFeeType defaults to luxury`() {
        assertEquals(FeeManager.LUXURY, SettingsState().selectedFeeType)
    }

    @Test
    fun `fee type constants are all distinct`() {
        val types = listOf(FeeManager.LUXURY, FeeManager.REGULAR, FeeManager.ECONOMY)
        assertEquals(types.size, types.toSet().size)
    }
}
