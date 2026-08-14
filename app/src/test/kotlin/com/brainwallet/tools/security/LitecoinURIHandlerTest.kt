package com.brainwallet.tools.security

import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.brainwallet.R
import com.brainwallet.navigation.LegacyNavigation
import com.brainwallet.navigation.Route
import com.brainwallet.presenter.customviews.BRDialogView
import com.brainwallet.tools.animation.BRDialog
import com.brainwallet.tools.manager.BRClipboardManager
import com.brainwallet.util.EventBus
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * BRWalletManager's address/key checks are native methods, which mockk cannot intercept
 * on a mocked BRWalletManager instance in a plain JVM unit test - there's no bytecode body
 * to override (see LitecoinURIHandler.AddressResolver's javadoc). These tests supply a
 * simple fake implementing that seam instead.
 */
class LitecoinURIHandlerTest {

    private class FakeAddressResolver(
        var validAddresses: Set<String> = emptySet(),
        var bip38Keys: Set<String> = emptySet(),
        var privateKeys: Set<String> = emptySet(),
        var sweepConfirmed: Boolean = false,
        var walletFullySynced: Boolean = true
    ) : LitecoinURIHandler.AddressResolver {
        override fun validateAddress(address: String) = address in validAddresses
        override fun isValidBitcoinBIP38Key(key: String) = key in bip38Keys
        override fun isValidBitcoinPrivateKey(key: String) = key in privateKeys
        override fun confirmSweep(ctx: Context, privKey: String) = sweepConfirmed
        override fun isWalletFullySynced() = walletFullySynced
    }

    private lateinit var resolver: FakeAddressResolver

    @Before
    fun setUp() {
        // Default: treat "LQRScannedAddr" as the one valid address unless a test says otherwise.
        resolver = FakeAddressResolver(validAddresses = setOf("LQRScannedAddr"))

        mockkStatic(BRDialog::class)
        every {
            BRDialog.showCustomDialog(
                any<Context>(),
                any<String>(),
                any<String>(),
                any<String>(),
                any<String>(),
                any<BRDialogView.BROnClickListener>(),
                any<BRDialogView.BROnClickListener>(),
                any<DialogInterface.OnDismissListener>(),
                any<Int>()
            )
        } just Runs

        mockkObject(EventBus)
        every { EventBus.postQRCodeScanned(any()) } returns Unit

        // openComposeScreen is @JvmStatic, which generates a real static forwarding
        // method that mockkObject's instance-based interception doesn't cover -
        // mockkStatic is what's needed to intercept that.
        mockkStatic(LegacyNavigation::class)
        every { LegacyNavigation.openComposeScreen(any<Context>(), any<Route>()) } returns mockk(relaxed = true)

        mockkStatic(BRClipboardManager::class)
        every { BRClipboardManager.putClipboard(any(), any()) } just Runs

        mockkStatic(Toast::class)
        every { Toast.makeText(any<Context>(), any<Int>(), any<Int>()) } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ── getRequestFromString ─────────────────────────────────────────────────

    @Test
    fun `getRequestFromString returns null for null input`() {
        assertNull(LitecoinURIHandler.getRequestFromString(null, resolver))
    }

    @Test
    fun `getRequestFromString returns null for empty input`() {
        assertNull(LitecoinURIHandler.getRequestFromString("", resolver))
    }

    @Test
    fun `getRequestFromString extracts a bare valid address with no litecoin colon prefix`() {
        val result = LitecoinURIHandler.getRequestFromString("LQRScannedAddr", resolver)

        assertNotNull(result)
        assertEquals("LQRScannedAddr", result!!.address)
        assertNull(result.amount)
    }

    @Test
    fun `getRequestFromString extracts the address from a litecoin colon URI`() {
        val result = LitecoinURIHandler.getRequestFromString("litecoin:LQRScannedAddr", resolver)

        assertEquals("LQRScannedAddr", result!!.address)
    }

    @Test
    fun `getRequestFromString leaves address null when validateAddress rejects it`() {
        resolver.validAddresses = emptySet()

        val result = LitecoinURIHandler.getRequestFromString("litecoin:notAnAddress", resolver)

        assertNotNull(result)
        assertNull(result!!.address)
    }

    @Test
    fun `getRequestFromString converts the amount param to litoshis`() {
        val result = LitecoinURIHandler.getRequestFromString("litecoin:LQRScannedAddr?amount=1", resolver)

        val expected = BigDecimal("1").multiply(BigDecimal("100000000")).toString()
        assertEquals(expected, result!!.amount)
    }

    @Test
    fun `getRequestFromString converts a fractional amount to litoshis`() {
        val result = LitecoinURIHandler.getRequestFromString("litecoin:LQRScannedAddr?amount=0.5", resolver)

        val expected = BigDecimal("0.5").multiply(BigDecimal("100000000")).toString()
        assertEquals(expected, result!!.amount)
    }

    @Test
    fun `getRequestFromString leaves amount null when it is not a number`() {
        val result = LitecoinURIHandler.getRequestFromString("litecoin:LQRScannedAddr?amount=notANumber", resolver)

        assertNotNull(result)
        assertNull(result!!.amount)
    }

    @Test
    fun `getRequestFromString parses label and message params`() {
        val result = LitecoinURIHandler.getRequestFromString(
            "litecoin:LQRScannedAddr?label=Alice&message=Thanks",
            resolver
        )

        assertEquals("Alice", result!!.label)
        assertEquals("Thanks", result.message)
    }

    @Test
    fun `getRequestFromString parses the req param without confusing it for r`() {
        val result = LitecoinURIHandler.getRequestFromString(
            "litecoin:LQRScannedAddr?req=https://example.com/pay",
            resolver
        )

        assertEquals("https://example.com/pay", result!!.req)
        assertNull("req= must not also populate r", result.r)
    }

    @Test
    fun `getRequestFromString parses a bare r param as a payment request`() {
        val result = LitecoinURIHandler.getRequestFromString("litecoin:?r=https://example.com/pay", resolver)

        assertEquals("https://example.com/pay", result!!.r)
    }

    @Test
    fun `getRequestFromString ignores query pairs without an equals sign`() {
        val result = LitecoinURIHandler.getRequestFromString("litecoin:LQRScannedAddr?justAFlag", resolver)

        assertNotNull(result)
        assertEquals("LQRScannedAddr", result!!.address)
        assertNull(result.amount)
        assertNull(result.label)
    }

    @Test
    fun `getRequestFromString returns null for a malformed URI`() {
        // An invalid percent-encoding sequence makes URI#create throw IllegalArgumentException.
        val result = LitecoinURIHandler.getRequestFromString("litecoin:bad%zzhost", resolver)

        assertNull(result)
    }

    // ── isValidLitecoinURI ───────────────────────────────────────────────────

    @Test
    fun `isValidLitecoinURI is true for a request with a valid address`() {
        assertTrue(LitecoinURIHandler.isValidLitecoinURI("litecoin:LQRScannedAddr", resolver))
    }

    @Test
    fun `isValidLitecoinURI is true for a request with an r param`() {
        assertTrue(LitecoinURIHandler.isValidLitecoinURI("litecoin:?r=https://example.com/pay", resolver))
    }

    @Test
    fun `isValidLitecoinURI is true for a BIP38 key even without a parsable address`() {
        resolver.validAddresses = emptySet()
        resolver.bip38Keys = setOf("someKey")

        assertTrue(LitecoinURIHandler.isValidLitecoinURI("someKey", resolver))
    }

    @Test
    fun `isValidLitecoinURI is true for a raw private key even without a parsable address`() {
        resolver.validAddresses = emptySet()
        resolver.privateKeys = setOf("someKey")

        assertTrue(LitecoinURIHandler.isValidLitecoinURI("someKey", resolver))
    }

    @Test
    fun `isValidLitecoinURI is false when nothing matches`() {
        resolver.validAddresses = emptySet()

        assertFalse(LitecoinURIHandler.isValidLitecoinURI("garbage", resolver))
    }

    // ── isValidLitecoinUrl ───────────────────────────────────────────────────

    @Test
    fun `isValidLitecoinUrl is true when the raw url string is itself a valid address`() {
        resolver.validAddresses = setOf("LQRScannedAddr")

        assertTrue(LitecoinURIHandler.isValidLitecoinUrl("LQRScannedAddr", resolver))
    }

    @Test
    fun `isValidLitecoinUrl is false when the raw url string does not validate as an address`() {
        // The URI is valid (it has an address param) but the *raw* url string passed to
        // validateAddress is the full litecoin: URI, not the extracted address - only the
        // bare address is in validAddresses here, so this is expected to fail.
        resolver.validAddresses = setOf("LQRScannedAddr")

        assertFalse(LitecoinURIHandler.isValidLitecoinUrl("litecoin:LQRScannedAddr", resolver))
    }

    @Test
    fun `isValidLitecoinUrl is false for a garbage url`() {
        resolver.validAddresses = emptySet()

        assertFalse(LitecoinURIHandler.isValidLitecoinUrl("garbage", resolver))
    }

    // ── processRequest ───────────────────────────────────────────────────────

    private fun mockActivity(): FragmentActivity {
        val activity = mockk<FragmentActivity>(relaxed = true)
        every { activity.runOnUiThread(any()) } answers { firstArg<Runnable>().run() }
        return activity
    }

    private fun verifyDialogShown(activity: FragmentActivity) {
        verify(exactly = 1) {
            BRDialog.showCustomDialog(
                activity,
                any<String>(),
                any<String>(),
                any<String>(),
                any<String>(),
                any<BRDialogView.BROnClickListener>(),
                any<BRDialogView.BROnClickListener>(),
                any<DialogInterface.OnDismissListener>(),
                any<Int>()
            )
        }
    }

    @Test
    fun `processRequest returns false for a null url`() {
        assertFalse(LitecoinURIHandler.processRequest(mockActivity(), null, resolver))
    }

    @Test
    fun `processRequest returns true immediately when confirmSweep handles it`() {
        val activity = mockActivity()
        resolver.sweepConfirmed = true

        assertTrue(LitecoinURIHandler.processRequest(activity, "someSweepKey", resolver))

        verify(exactly = 0) { EventBus.postQRCodeScanned(any()) }
        verify(exactly = 0) { LegacyNavigation.openComposeScreen(any(), any()) }
    }

    @Test
    fun `processRequest returns false and shows a dialog for a malformed request`() {
        val activity = mockActivity()

        val result = LitecoinURIHandler.processRequest(activity, "litecoin:bad%zzhost", resolver)

        assertFalse(result)
        verifyDialogShown(activity)
    }

    @Test
    fun `processRequest returns false and shows a dialog when neither address nor r is present`() {
        val activity = mockActivity()
        resolver.validAddresses = emptySet()

        val result = LitecoinURIHandler.processRequest(activity, "litecoin:?label=noAddress", resolver)

        assertFalse(result)
        verifyDialogShown(activity)
    }

    @Test
    fun `processRequest pastes the scanned address and deep links to Send when there is no amount`() {
        val activity = mockActivity()

        val result = LitecoinURIHandler.processRequest(activity, "litecoin:LQRScannedAddr", resolver)

        assertTrue(result)
        verify(exactly = 1) { EventBus.postQRCodeScanned("LQRScannedAddr") }

        val routeSlot = slot<Route>()
        verify(exactly = 1) { LegacyNavigation.openComposeScreen(activity, capture(routeSlot)) }
        assertEquals(Route.Send("LQRScannedAddr"), routeSlot.captured)
    }

    @Test
    fun `processRequest copies the address and toasts instead of navigating when the wallet is not fully synced`() {
        val activity = mockActivity()
        resolver.walletFullySynced = false

        val result = LitecoinURIHandler.processRequest(activity, "litecoin:LQRScannedAddr", resolver)

        assertTrue(result)
        verify(exactly = 1) { BRClipboardManager.putClipboard(activity, "LQRScannedAddr") }
        verify(exactly = 1) {
            Toast.makeText(activity, R.string.Send_qrAddressCopiedWhileSyncing, Toast.LENGTH_LONG)
        }
        verify(exactly = 0) { EventBus.postQRCodeScanned(any()) }
        verify(exactly = 0) { LegacyNavigation.openComposeScreen(any(), any()) }
    }

    @Test
    fun `processRequest does not copy to clipboard when the wallet is fully synced`() {
        val activity = mockActivity()
        resolver.walletFullySynced = true

        LitecoinURIHandler.processRequest(activity, "litecoin:LQRScannedAddr", resolver)

        verify(exactly = 0) { BRClipboardManager.putClipboard(any(), any()) }
    }

    @Test
    fun `processRequest does not paste the address or navigate when an amount is present`() {
        // Pinning existing behavior: a BIP21 URI with a non-zero amount is currently a
        // silent no-op beyond returning true - see tryLitecoinURL's amount guard.
        val activity = mockActivity()

        val result = LitecoinURIHandler.processRequest(activity, "litecoin:LQRScannedAddr?amount=1", resolver)

        assertTrue(result)
        verify(exactly = 0) { EventBus.postQRCodeScanned(any()) }
        verify(exactly = 0) { LegacyNavigation.openComposeScreen(any(), any()) }
    }

    @Test
    fun `processRequest returns true for a payment-protocol r param request`() {
        val activity = mockActivity()

        val result = LitecoinURIHandler.processRequest(activity, "litecoin:?r=https://example.com/pay", resolver)

        assertTrue(result)
        verify(exactly = 0) { EventBus.postQRCodeScanned(any()) }
        verify(exactly = 0) { LegacyNavigation.openComposeScreen(any(), any()) }
    }
}
