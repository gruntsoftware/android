package com.brainwallet.presenter.history

import com.brainwallet.domain.ExportedTransactionsMapper
import com.brainwallet.presenter.entities.TxItem
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import com.grunt.brainwallet.iap.presentation.model.ExportedTransaction
import org.junit.Test
import org.orbitmvi.orbit.test.test

class HistoryFragmentViewModelTest {

    private val mapper: ExportedTransactionsMapper = mockk()

    @Test
    fun `when onEvent with txItems then state contains mapped ExportedTransactions`() = runTest {
        val viewModel = HistoryFragmentViewModel(mapper)
        val txItem1 = mockk<TxItem>(relaxed = true)
        val txItem2 = mockk<TxItem>(relaxed = true)

        val exported1 = mockk<ExportedTransaction>()
        val exported2 = mockk<ExportedTransaction>()

        every { mapper(listOf(txItem1)) } returns persistentListOf(exported1)
        every { mapper(listOf(txItem1, txItem2)) } returns persistentListOf(exported1, exported2)

        viewModel.test(this, initialState = persistentListOf()) {
            viewModel.updateTx(listOf(txItem1))
            expectState {
                persistentListOf(exported1)
            }
            viewModel.updateTx(listOf(txItem1, txItem2))
            expectState {
                persistentListOf(exported1, exported2)
            }
        }
    }
}
