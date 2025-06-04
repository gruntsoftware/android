package com.brainwallet.ui.screens.inputwords

import android.content.Context
import com.brainwallet.BrainwalletApp
import com.brainwallet.navigation.Route
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.security.SmartValidator
import com.brainwallet.tools.util.Bip39Reader
import com.brainwallet.util.EventBus
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class InputWordsViewModelTest {

    private lateinit var viewModel: InputWordsViewModel
    private val context: Context = mockk(relaxed = true)
    private val mockBip39Words = listOf(
        "apple",
        "banana",
        "cherry",
        "dog",
        "elephant",
        "frog",
        "grape",
        "house",
        "ice",
        "jungle",
        "kite",
        "lemon"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // Mock Bip39Reader
        mockkStatic(Bip39Reader::class)
        every { Bip39Reader.bip39List(any(), any()) } returns mockBip39Words
        every { Bip39Reader.cleanWord(any()) } answers { firstArg<String>() }

        // Mock BrainwalletApp
        mockkObject(BrainwalletApp)
        every { BrainwalletApp.breadContext } returns context

        // Mock SmartValidator
        mockkStatic(SmartValidator::class)
        every { SmartValidator.cleanPaperKey(any(), any()) } answers { secondArg<String>() }
        every { SmartValidator.isPaperKeyValid(any(), any()) } returns true
        every { SmartValidator.isPaperKeyCorrect(any(), any()) } returns true

        // Mock BRWalletManager
//        mockkObject(BRWalletManager.getInstance())
//        val walletManager = mockk<BRWalletManager> {
//            every { wipeAll(any()) } just Runs
//        }
//        every { BRWalletManager.getInstance() } returns walletManager

        // Mock BRSharedPrefs
        mockkStatic(BRSharedPrefs::class)
        every { BRSharedPrefs.putAllowSpend(any(), any()) } just Runs
        every { BRSharedPrefs.putStartHeight(any(), any()) } just Runs

        // Mock PostAuth
//        mockkObject(PostAuth.getInstance())
//        val postAuth = mockk<PostAuth> {
//            every { setPhraseForKeyStore(any()) } just Runs
//        }
//        every { PostAuth.getInstance() } returns postAuth

        // Mock EventBus
        mockkObject(EventBus)
        coEvery { EventBus.emit(any()) } just Runs

        viewModel = InputWordsViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `init should load bip39Words`() {
        // Then
        assertEquals(mockBip39Words, viewModel.state.value.bip39Words)
    }

    @Test
    fun `test OnSeedWordItemChange updates seedWords map with cleaned word`() {
        // When
        viewModel.onEvent(InputWordsEvent.OnSeedWordItemChange(0, "apple"))

        // Then
        assertEquals("apple", viewModel.state.value.seedWords[0])
    }

    @Test
    fun `test OnSeedWordItemChange updates suggestions based on input`() {
        // When
        viewModel.onEvent(InputWordsEvent.OnSeedWordItemChange(0, "a"))

        // Then
        val suggestions = viewModel.state.value.suggestionsSeedWords
        assertTrue(suggestions.contains("apple"))
        assertEquals(1, suggestions.size)
    }

    @Test
    fun `test OnClearSeedWords clears all seed words`() {
        // Given
        mockBip39Words.forEachIndexed { index, word ->
            viewModel.onEvent(InputWordsEvent.OnSeedWordItemChange(index, word))
        }
        assertEquals(12, viewModel.state.value.seedWords.size)

        // When
        viewModel.onEvent(InputWordsEvent.OnClearSeedWords)

        // Then
        assertTrue(viewModel.state.value.seedWords.all { it.value.isEmpty() })
        assertEquals(mockBip39Words, viewModel.state.value.bip39Words)
    }

    @Test
    fun `test OnLoad sets source correctly`() {
        // Given
        val source = Route.InputWords.Source.RESET_PIN

        // When
        viewModel.onEvent(InputWordsEvent.OnLoad(source))

        // Then
        assertEquals(source, viewModel.state.value.source)
    }

    @Test
    fun `test OnRestoreClick from welcome with invalid paper key shows error`() = runTest {
        // Given
        viewModel.onEvent(InputWordsEvent.OnLoad())
        viewModel.onEvent(InputWordsEvent.OnSeedWordItemChange(0, "apple"))

        // Mock validation to fail
        every { SmartValidator.isPaperKeyValid(any(), any()) } returns false

        // When
        viewModel.onEvent(InputWordsEvent.OnRestoreClick(context))

        // Then
        val eventSlot = slot<EventBus.Event>()
        coVerify { EventBus.emit(capture(eventSlot)) }

        val capturedEvent = eventSlot.captured
        assertTrue(capturedEvent is EventBus.Event.Message)
        assertEquals(
            InputWordsViewModel.LEGACY_DIALOG_INVALID,
            (capturedEvent as EventBus.Event.Message).message
        )
    }

    @Test
    fun `test OnRestoreClick from non-welcome with incorrect paper key shows error`() = runTest {
        // Given
        viewModel.onEvent(InputWordsEvent.OnLoad(Route.InputWords.Source.RESET_PIN))
        viewModel.onEvent(InputWordsEvent.OnSeedWordItemChange(0, "apple"))

        // Mock validation to fail
        every { SmartValidator.isPaperKeyCorrect(any(), any()) } returns false

        // When
        viewModel.onEvent(InputWordsEvent.OnRestoreClick(context))

        // Then
        val eventSlot = slot<EventBus.Event>()
        coVerify { EventBus.emit(capture(eventSlot)) }

        val capturedEvent = eventSlot.captured
        assertTrue(capturedEvent is EventBus.Event.Message)
        assertEquals(
            InputWordsViewModel.LEGACY_DIALOG_INVALID,
            (capturedEvent as EventBus.Event.Message).message
        )
    }

    @Test
    fun `test OnRestoreClick from RESET_PIN emits reset pin event`() = runTest {
        // Given
        viewModel.onEvent(InputWordsEvent.OnLoad(Route.InputWords.Source.RESET_PIN))
        viewModel.onEvent(InputWordsEvent.OnSeedWordItemChange(0, "apple"))

        // When
        viewModel.onEvent(InputWordsEvent.OnRestoreClick(context))

        // Then
        val eventSlot = slot<EventBus.Event>()
        coVerify { EventBus.emit(capture(eventSlot)) }

        val capturedEvent = eventSlot.captured
        assertTrue(capturedEvent is EventBus.Event.Message)
        assertEquals(
            InputWordsViewModel.LEGACY_EFFECT_RESET_PIN,
            (capturedEvent as EventBus.Event.Message).message
        )
    }

    @Test
    fun `test OnRestoreClick from SETTING_WIPE shows wipe alert`() = runTest {
        // Given
        viewModel.onEvent(InputWordsEvent.OnLoad(Route.InputWords.Source.SETTING_WIPE))
        viewModel.onEvent(InputWordsEvent.OnSeedWordItemChange(0, "apple"))

        // When
        viewModel.onEvent(InputWordsEvent.OnRestoreClick(context))

        // Then
        val eventSlot = slot<EventBus.Event>()
        coVerify { EventBus.emit(capture(eventSlot)) }

        val capturedEvent = eventSlot.captured
        assertTrue(capturedEvent is EventBus.Event.Message)
        assertEquals(
            InputWordsViewModel.LEGACY_DIALOG_WIPE_ALERT,
            (capturedEvent as EventBus.Event.Message).message
        )
    }

    @Test
    fun `test OnRestoreClick from welcome with valid paper key performs wallet restore`() =
        runTest {
            //TODO
        }

    @Test
    fun `test OnRestoreClick from non-welcome source with valid key performs wallet restore`() =
        runTest {
            //TODO
        }
}