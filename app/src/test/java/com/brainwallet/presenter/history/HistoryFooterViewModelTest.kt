package com.brainwallet.presenter.history

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryFooterViewModelTest {

    @Test
    fun `export sheet starts hidden`() {
        assertFalse(HistoryFooterViewModel().state.value)
    }

    @Test
    fun `OnToggleExportSheet shows then hides the export sheet`() {
        val viewModel = HistoryFooterViewModel()

        viewModel.onEvent(HistoryFooterEvent.OnToggleExportSheet)
        assertTrue(viewModel.state.value)

        viewModel.onEvent(HistoryFooterEvent.OnToggleExportSheet)
        assertFalse(viewModel.state.value)
    }
}
