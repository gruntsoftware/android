package com.brainwallet.presenter.history

import com.brainwallet.domain.ExportedTransactionsMapper
import com.brainwallet.presenter.entities.TxItem
import com.grunt.brainwallet.iap.export.transactions.presentation.model.ExportedTransaction
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryFragmentViewModelTest {

    private val mapper: ExportedTransactionsMapper = mockk()

    @Test
    fun `starts with no exported transactions`() {
        assertEquals(persistentListOf<ExportedTransaction>(), HistoryFragmentViewModel(mapper).state.value)
    }

    @Test
    fun `when OnTxUpdated then state contains the mapped ExportedTransactions`() {
        val viewModel = HistoryFragmentViewModel(mapper)
        val txItem1 = mockk<TxItem>(relaxed = true)
        val txItem2 = mockk<TxItem>(relaxed = true)
        val exported1 = mockk<ExportedTransaction>()
        val exported2 = mockk<ExportedTransaction>()
        every { mapper(listOf(txItem1)) } returns persistentListOf(exported1)
        every { mapper(listOf(txItem1, txItem2)) } returns persistentListOf(exported1, exported2)

        viewModel.onEvent(HistoryFragmentEvent.OnTxUpdated(listOf(txItem1)))
        assertEquals(persistentListOf(exported1), viewModel.state.value)

        viewModel.onEvent(HistoryFragmentEvent.OnTxUpdated(listOf(txItem1, txItem2)))
        assertEquals(persistentListOf(exported1, exported2), viewModel.state.value)
    }

    @Test
    fun `when OnTxUpdated with an empty list then state is emptied`() {
        val viewModel = HistoryFragmentViewModel(mapper)
        val txItem = mockk<TxItem>(relaxed = true)
        every { mapper(listOf(txItem)) } returns persistentListOf(mockk())
        every { mapper(emptyList()) } returns persistentListOf()

        viewModel.onEvent(HistoryFragmentEvent.OnTxUpdated(listOf(txItem)))
        viewModel.onEvent(HistoryFragmentEvent.OnTxUpdated(emptyList()))

        assertEquals(persistentListOf<ExportedTransaction>(), viewModel.state.value)
    }
}
