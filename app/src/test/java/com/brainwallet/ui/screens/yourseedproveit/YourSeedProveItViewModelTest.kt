package com.brainwallet.ui.screens.yourseedproveit

import com.brainwallet.util.EventBus
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.mockkObject
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class YourSeedProveItViewModelTest {

    private lateinit var viewModel: YourSeedProveItViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockkObject(EventBus)
        viewModel = YourSeedProveItViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `test OnLoad event updates state with correct seed words and shuffled list`() {
        // Given
        val seedWords = listOf("apple", "banana", "cherry")

        // When
        viewModel.onEvent(YourSeedProveItEvent.OnLoad(seedWords))

        // Then
        val state = viewModel.state.value
        assertEquals(3, state.correctSeedWords.size)
        assertEquals(3, state.shuffledSeedWords.size)

        for (i in seedWords.indices) {
            assertEquals(seedWords[i], state.correctSeedWords[i]?.expected)
            assertTrue(state.shuffledSeedWords.contains(seedWords[i]))
        }
    }

    @Test
    fun `test OnDropSeedWordItem event updates the correct seed word item`() {
        // Given
        val seedWords = listOf("apple", "banana", "cherry")
        viewModel.onEvent(YourSeedProveItEvent.OnLoad(seedWords))

        // When
        viewModel.onEvent(YourSeedProveItEvent.OnDropSeedWordItem(0, "apple", "apple"))

        // Then
        val state = viewModel.state.value
        assertEquals("apple", state.correctSeedWords[0]?.actual)
        assertFalse(state.orderCorrected) // Not all words are placed correctly yet
    }

    @Test
    fun `test all words correctly placed sets orderCorrected to true`() {
        // Given
        val seedWords = listOf("apple", "banana", "cherry")
        viewModel.onEvent(YourSeedProveItEvent.OnLoad(seedWords))

        // When
        viewModel.onEvent(YourSeedProveItEvent.OnDropSeedWordItem(0, "apple", "apple"))
        viewModel.onEvent(YourSeedProveItEvent.OnDropSeedWordItem(1, "banana", "banana"))
        viewModel.onEvent(YourSeedProveItEvent.OnDropSeedWordItem(2, "cherry", "cherry"))

        // Then
        val state = viewModel.state.value
        assertTrue(state.orderCorrected)
    }

    @Test
    fun `test OnClear event resets actual values`() {
        // Given
        val seedWords = listOf("apple", "banana", "cherry")
        viewModel.onEvent(YourSeedProveItEvent.OnLoad(seedWords))
        viewModel.onEvent(YourSeedProveItEvent.OnDropSeedWordItem(0, "apple", "apple"))

        // When
        viewModel.onEvent(YourSeedProveItEvent.OnClear)

        // Then
        val state = viewModel.state.value
        assertEquals("", state.correctSeedWords[0]?.actual)
        assertEquals("apple", state.correctSeedWords[0]?.expected)
    }

    @Test
    fun `test OnGameAndSync emits correct event`() = runTest {
        // When
        viewModel.onEvent(YourSeedProveItEvent.OnGameAndSync)

        // Then
        val slot = slot<EventBus.Event>()
        coVerify { EventBus.emit(capture(slot)) }

        val capturedEvent = slot.captured
        assertTrue(capturedEvent is EventBus.Event.Message)
        assertEquals(
            YourSeedProveItViewModel.LEGACY_EFFECT_ON_PAPERKEY_PROVED,
            (capturedEvent as EventBus.Event.Message).message
        )
    }
}