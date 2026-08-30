package com.brainwallet.tools.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.UserNotAuthenticatedException
import android.util.Base64
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.util.cryptography.KeyStoreManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.platform.entities.WalletInfo
import com.platform.tools.KVStoreManager
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class BWAIKeyStoreTests {

    // ─────────────────────────────────────────────────────────────────────────
    // Collaborators
    // ─────────────────────────────────────────────────────────────────────────

    private val mockKeyStoreManager: KeyStoreManager = mockk()
    private val mockContext: Context = mockk(relaxed = true)
    private val mockSharedPrefs: SharedPreferences = mockk(relaxed = true)
    private val mockEditor: SharedPreferences.Editor = mockk(relaxed = true)
    private val mockCrashlytics: FirebaseCrashlytics = mockk(relaxed = true)

    // ─────────────────────────────────────────────────────────────────────────
    // Setup / teardown
    // ─────────────────────────────────────────────────────────────────────────
    @Before
    fun setUp() {
        // Provide KeyStoreManager through Koin so KoinJavaComponent.get() resolves it
        startKoin {
            modules(
                module {
                    single<KeyStoreManager> { mockKeyStoreManager }
                }
            )
        }

        // Firebase must not crash in unit-test JVM runs
        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns mockCrashlytics

        // android.util.Base64 is an Android framework class — "Method not mocked" in
        // plain JVM tests. Stub both directions to delegate to java.util.Base64 so
        // storeEncryptedData / retrieveEncryptedData produce real encodings.
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), any()) } answers {
            java.util.Base64.getEncoder().encodeToString(firstArg<ByteArray>())
        }
        every { Base64.decode(any<String>(), any()) } answers {
            java.util.Base64.getDecoder().decode(firstArg<String>())
        }

        // Shared-preference plumbing used by storeEncryptedData / destroyEncryptedData
        every {
            mockContext.getSharedPreferences(BRKeyStore.KEY_STORE_PREFS_NAME, Context.MODE_PRIVATE)
        } returns mockSharedPrefs
        every { mockSharedPrefs.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor
        every { mockEditor.apply() } just Runs

        // Ensure auth-loop flag is reset before every test
        PostAuth.isStuckWithAuthLoop = false
    }

    @After
    fun tearDown() {
        stopKoin()
        unmockkStatic(FirebaseCrashlytics::class)
        unmockkStatic(Base64::class)
        PostAuth.isStuckWithAuthLoop = false
        // Reset private static flag so a failed showLoopBugMessage in one test
        // does not cause subsequent auth-loop tests to silently skip the body.
        val field = BRKeyStore::class.java.getDeclaredField("bugMessageShowing")
        field.isAccessible = true
        field.setBoolean(null, false)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // putPhrase / getPhrase
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `putPhrase returns false for null data`() {
        assertFalse(BRKeyStore.putPhrase(null, mockContext, 0))
    }

    @Test
    fun `putPhrase returns false for empty data`() {
        assertFalse(BRKeyStore.putPhrase(ByteArray(0), mockContext, 0))
    }

    // NOTE: the three auth-loop paths (putPhrase / getPhrase / putCanary when
    // PostAuth.isStuckWithAuthLoop == true) call showLoopBugMessage(), which
    // constructs a SpannableString — an Android-framework class that throws
    // "Method not mocked" in a plain JVM test runner. Those paths require a
    // Robolectric or instrumented test; they are intentionally omitted here.
    //
    // What we CAN assert in a unit test: when the loop flag is set and
    // showLoopBugMessage() short-circuits itself (bugMessageShowing = true),
    // the UserNotAuthenticatedException is still thrown.
    @Test(expected = UserNotAuthenticatedException::class)
    fun `putPhrase throws UserNotAuthenticatedException when auth loop is active and bugMessage already showing`() {
        // Pre-set the private flag so showLoopBugMessage() returns immediately
        // (before it tries to build a SpannableString), then still throws.
        val field = BRKeyStore::class.java.getDeclaredField("bugMessageShowing")
        field.isAccessible = true
        field.setBoolean(null, true)

        PostAuth.isStuckWithAuthLoop = true
        BRKeyStore.putPhrase("phrase".toByteArray(), mockContext, 0)
    }

    @Test(expected = UserNotAuthenticatedException::class)
    fun `getPhrase throws UserNotAuthenticatedException when auth loop is active and bugMessage already showing`() {
        val field = BRKeyStore::class.java.getDeclaredField("bugMessageShowing")
        field.isAccessible = true
        field.setBoolean(null, true)

        PostAuth.isStuckWithAuthLoop = true
        BRKeyStore.getPhrase(mockContext, 0)
    }

    @Test(expected = UserNotAuthenticatedException::class)
    fun `putCanary throws UserNotAuthenticatedException when auth loop is active and bugMessage already showing`() {
        val field = BRKeyStore::class.java.getDeclaredField("bugMessageShowing")
        field.isAccessible = true
        field.setBoolean(null, true)

        PostAuth.isStuckWithAuthLoop = true
        BRKeyStore.putCanary("canary", mockContext, 0)
    }

    @Test
    fun `putPhrase returns true when KeyStoreManager setDataBlocking succeeds`() {
        val phrase = "correct horse battery staple".toByteArray()
        every { mockKeyStoreManager.setDataBlocking(any(), phrase) } returns true

        assertTrue(BRKeyStore.putPhrase(phrase, mockContext, 0))
    }

    @Test
    fun `putPhrase returns false when KeyStoreManager setDataBlocking fails`() {
        val phrase = "correct horse battery staple".toByteArray()
        every { mockKeyStoreManager.setDataBlocking(any(), phrase) } returns false

        assertFalse(BRKeyStore.putPhrase(phrase, mockContext, 0))
    }

    @Test
    fun `getPhrase returns bytes from KeyStoreManager`() {
        val phrase = "correct horse battery staple".toByteArray()
        every { mockKeyStoreManager.getDataBlocking(any()) } returns phrase

        assertArrayEquals(phrase, BRKeyStore.getPhrase(mockContext, 0))
    }

    @Test
    fun `getPhrase returns null when KeyStoreManager has no data`() {
        every { mockKeyStoreManager.getDataBlocking(any()) } returns null

        assertNull(BRKeyStore.getPhrase(mockContext, 0))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // putCanary / getCanary
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `putCanary returns false for null string`() {
        assertFalse(BRKeyStore.putCanary(null, mockContext, 0))
    }

    @Test
    fun `putCanary returns false for empty string`() {
        assertFalse(BRKeyStore.putCanary("", mockContext, 0))
    }

    @Test
    fun `putCanary returns true when KeyStoreManager succeeds`() {
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns true

        assertTrue(BRKeyStore.putCanary("canary-value", mockContext, 0))
    }

    @Test
    fun `putCanary returns false when KeyStoreManager fails`() {
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns false

        assertFalse(BRKeyStore.putCanary("canary-value", mockContext, 0))
    }

    @Test
    fun `getCanary decodes UTF-8 bytes back into the original string`() {
        val expected = "canary-value"
        every { mockKeyStoreManager.getDataBlocking(any()) } returns expected.toByteArray(Charsets.UTF_8)

        assertEquals(expected, BRKeyStore.getCanary(mockContext, 0))
    }

    @Test
    fun `getCanary returns null when KeyStoreManager has no data`() {
        every { mockKeyStoreManager.getDataBlocking(any()) } returns null

        assertNull(BRKeyStore.getCanary(mockContext, 0))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // putMasterPublicKey
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `putMasterPublicKey returns false for null key`() {
        assertFalse(BRKeyStore.putMasterPublicKey(null, mockContext))
    }

    @Test
    fun `putMasterPublicKey returns false for empty key`() {
        assertFalse(BRKeyStore.putMasterPublicKey(ByteArray(0), mockContext))
    }

    @Test
    fun `putMasterPublicKey returns false when setDataBlocking returns false`() {
        val key = "masterkey".toByteArray()
        every { mockKeyStoreManager.setDataBlocking(any(), key) } returns false

        assertFalse(BRKeyStore.putMasterPublicKey(key, mockContext))
        verify { mockCrashlytics.recordException(any()) }
    }

    @Test
    fun `putMasterPublicKey returns false when readback verification mismatches`() {
        val key = "masterkey".toByteArray()
        every { mockKeyStoreManager.setDataBlocking(any(), key) } returns true
        every { mockKeyStoreManager.getDataBlocking(any()) } returns "differentkey".toByteArray()

        assertFalse(BRKeyStore.putMasterPublicKey(key, mockContext))
        verify { mockCrashlytics.recordException(any()) }
    }

    @Test
    fun `putMasterPublicKey returns true when write and readback both succeed`() {
        val key = "masterkey".toByteArray()
        every { mockKeyStoreManager.setDataBlocking(any(), key) } returns true
        every { mockKeyStoreManager.getDataBlocking(any()) } returns key

        assertTrue(BRKeyStore.putMasterPublicKey(key, mockContext))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // putMasterPublicKeyWithRetry
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `putMasterPublicKeyWithRetry returns false after all 3 attempts fail`() {
        val key = "masterkey".toByteArray()
        every { mockKeyStoreManager.setDataBlocking(any(), key) } returns false

        assertFalse(BRKeyStore.putMasterPublicKeyWithRetry(key, mockContext))
        // Each attempt calls setDataBlocking once; 3 attempts total
        verify(exactly = 3) { mockKeyStoreManager.setDataBlocking(any(), key) }
        verify { mockCrashlytics.recordException(any()) }
    }

    @Test
    fun `putMasterPublicKeyWithRetry returns true when second attempt succeeds`() {
        val key = "masterkey".toByteArray()
        // First attempt: set fails → short-circuits. Second attempt: set succeeds + readback matches.
        every { mockKeyStoreManager.setDataBlocking(any(), key) } returnsMany listOf(false, true)
        every { mockKeyStoreManager.getDataBlocking(any()) } returns key

        assertTrue(BRKeyStore.putMasterPublicKeyWithRetry(key, mockContext))
        verify(exactly = 2) { mockKeyStoreManager.setDataBlocking(any(), key) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getMasterPublicKey
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `getMasterPublicKey returns bytes from KeyStoreManager`() {
        val key = "masterkey".toByteArray()
        every { mockKeyStoreManager.getDataBlocking(any()) } returns key

        assertArrayEquals(key, BRKeyStore.getMasterPublicKey(mockContext))
    }

    @Test
    fun `getMasterPublicKey returns null when no data is stored`() {
        every { mockKeyStoreManager.getDataBlocking(any()) } returns null

        assertNull(BRKeyStore.getMasterPublicKey(mockContext))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // putAuthKey / getAuthKey
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `putAuthKey returns false for null key`() {
        assertFalse(BRKeyStore.putAuthKey(null, mockContext))
    }

    @Test
    fun `putAuthKey returns false for empty key`() {
        assertFalse(BRKeyStore.putAuthKey(ByteArray(0), mockContext))
    }

    @Test
    fun `putAuthKey returns true when KeyStoreManager succeeds`() {
        val key = "authkey".toByteArray()
        every { mockKeyStoreManager.setDataBlocking(any(), key) } returns true

        assertTrue(BRKeyStore.putAuthKey(key, mockContext))
    }

    @Test
    fun `getAuthKey returns bytes from KeyStoreManager`() {
        val key = "authkey".toByteArray()
        every { mockKeyStoreManager.getDataBlocking(any()) } returns key

        assertArrayEquals(key, BRKeyStore.getAuthKey(mockContext))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // putToken / getToken
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `putToken returns false for null token`() {
        assertFalse(BRKeyStore.putToken(null, mockContext))
    }

    @Test
    fun `putToken returns true when KeyStoreManager succeeds`() {
        val token = "api-token".toByteArray()
        every { mockKeyStoreManager.setDataBlocking(any(), token) } returns true

        assertTrue(BRKeyStore.putToken(token, mockContext))
    }

    @Test
    fun `getToken returns bytes from KeyStoreManager`() {
        val token = "api-token".toByteArray()
        every { mockKeyStoreManager.getDataBlocking(any()) } returns token

        assertArrayEquals(token, BRKeyStore.getToken(mockContext))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // putPinCode / getPinCode
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `putPinCode stores pin via KeyStoreManager`() {
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns true

        assertTrue(BRKeyStore.putPinCode("123456", mockContext))
    }

    @Test
    fun `getPinCode returns a valid 6-digit pin`() {
        every { mockKeyStoreManager.getDataBlocking(any()) } returns "123456".toByteArray()

        assertEquals("123456", BRKeyStore.getPinCode(mockContext))
    }

    @Test
    fun `getPinCode returns a valid 4-digit pin`() {
        every { mockKeyStoreManager.getDataBlocking(any()) } returns "1234".toByteArray()

        assertEquals("1234", BRKeyStore.getPinCode(mockContext))
    }

    @Test
    fun `getPinCode returns empty string and resets when stored pin is non-numeric`() {
        every { mockKeyStoreManager.getDataBlocking(any()) } returns "abcdef".toByteArray()
        // getPinCode calls putPinCode, putFailCount and putFailTimeStamp during reset
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns true

        assertEquals("", BRKeyStore.getPinCode(mockContext))
    }

    @Test
    fun `getPinCode returns empty string and resets when pin length is not 4 or 6`() {
        every { mockKeyStoreManager.getDataBlocking(any()) } returns "12345".toByteArray() // 5 digits
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns true

        assertEquals("", BRKeyStore.getPinCode(mockContext))
    }

    @Test
    fun `getPinCode returns empty string when no data is stored`() {
        // null result → pinCode = "" → Integer.parseInt("") throws → triggers reset path
        every { mockKeyStoreManager.getDataBlocking(any()) } returns null
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns true

        assertEquals("", BRKeyStore.getPinCode(mockContext))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // putFailCount / getFailCount
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `putFailCount stores count when below threshold`() {
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns true

        assertTrue(BRKeyStore.putFailCount(2, mockContext))
    }

    @Test
    fun `putFailCount also persists timestamp when count reaches 3`() {
        mockkStatic(BRSharedPrefs::class)
        every { BRSharedPrefs.getSecureTime(mockContext) } returns 1_700_000_000L
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns true

        BRKeyStore.putFailCount(3, mockContext)

        // Reaching 3 failures must capture a timestamp via BRSharedPrefs
        verify { BRSharedPrefs.getSecureTime(mockContext) }

        unmockkStatic(BRSharedPrefs::class)
    }

    @Test
    fun `putFailCount also persists timestamp when count exceeds 3`() {
        mockkStatic(BRSharedPrefs::class)
        every { BRSharedPrefs.getSecureTime(mockContext) } returns 1_700_000_000L
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns true

        BRKeyStore.putFailCount(10, mockContext)

        verify { BRSharedPrefs.getSecureTime(mockContext) }

        unmockkStatic(BRSharedPrefs::class)
    }

    @Test
    fun `getFailCount returns 0 when no data is stored`() {
        every { mockKeyStoreManager.getDataBlocking(any()) } returns null

        assertEquals(0, BRKeyStore.getFailCount(mockContext))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // putSpendLimit / getSpendLimit
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `putSpendLimit stores limit via KeyStoreManager`() {
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns true

        assertTrue(BRKeyStore.putSpendLimit(5_000_000L, mockContext))
    }

    @Test
    fun `getSpendLimit returns 0 when no data is stored`() {
        every { mockKeyStoreManager.getDataBlocking(any()) } returns null

        assertEquals(0L, BRKeyStore.getSpendLimit(mockContext))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // putFailTimeStamp / getFailTimeStamp
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `putFailTimeStamp stores timestamp via KeyStoreManager`() {
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns true

        assertTrue(BRKeyStore.putFailTimeStamp(1_700_000_000L, mockContext))
    }

    @Test
    fun `getFailTimeStamp returns 0 when no data is stored`() {
        every { mockKeyStoreManager.getDataBlocking(any()) } returns null

        assertEquals(0L, BRKeyStore.getFailTimeStamp(mockContext))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // putTotalLimit / getTotalLimit
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `putTotalLimit stores limit via KeyStoreManager`() {
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns true

        assertTrue(BRKeyStore.putTotalLimit(10_000_000L, mockContext))
    }

    @Test
    fun `getTotalLimit returns 0 when no data is stored`() {
        every { mockKeyStoreManager.getDataBlocking(any()) } returns null

        assertEquals(0L, BRKeyStore.getTotalLimit(mockContext))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // putLastPinUsedTime / getLastPinUsedTime
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `putLastPinUsedTime stores time via KeyStoreManager`() {
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns true

        assertTrue(BRKeyStore.putLastPinUsedTime(System.currentTimeMillis(), mockContext))
    }

    @Test
    fun `getLastPinUsedTime returns 0 when no data is stored`() {
        every { mockKeyStoreManager.getDataBlocking(any()) } returns null

        assertEquals(0L, BRKeyStore.getLastPinUsedTime(mockContext))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getWalletCreationTime – KVStore fallback
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `getWalletCreationTime returns 0 when keystore and KVStore are both empty`() {
        mockkStatic(KVStoreManager::class)
        val mockKVStore = mockk<KVStoreManager>()
        every { KVStoreManager.getInstance() } returns mockKVStore
        every { mockKVStore.getWalletInfo(mockContext) } returns null
        every { mockKeyStoreManager.getDataBlocking(any()) } returns null

        assertEquals(0, BRKeyStore.getWalletCreationTime(mockContext))

        unmockkStatic(KVStoreManager::class)
    }

    @Test
    fun `getWalletCreationTime falls back to KVStore creation date when keystore is empty`() {
        mockkStatic(KVStoreManager::class)
        val mockKVStore = mockk<KVStoreManager>()
        val walletInfo = WalletInfo().apply { creationDate = 1_680_000_000 }
        every { KVStoreManager.getInstance() } returns mockKVStore
        every { mockKVStore.getWalletInfo(mockContext) } returns walletInfo
        every { mockKeyStoreManager.getDataBlocking(any()) } returns null
        // putWalletCreationTime triggered by the fallback path
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns true

        assertEquals(1_680_000_000, BRKeyStore.getWalletCreationTime(mockContext))

        unmockkStatic(KVStoreManager::class)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // storeEncryptedData / retrieveEncryptedData / destroyEncryptedData
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `storeEncryptedData writes Base64-encoded bytes to SharedPreferences`() {
        val data = "sensitive".toByteArray()

        BRKeyStore.storeEncryptedData(mockContext, data, "key1")

        verify { mockEditor.putString("key1", any()) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `retrieveEncryptedData returns null when key is absent`() {
        every { mockSharedPrefs.getString("missing", null) } returns null

        assertNull(BRKeyStore.retrieveEncryptedData(mockContext, "missing"))
    }

    @Test
    fun `retrieveEncryptedData round-trips bytes stored by storeEncryptedData`() {
        val original = "round-trip data".toByteArray()
        // Use java.util.Base64 directly — the android.util.Base64 stub above delegates
        // to it, so this value matches exactly what storeEncryptedData will produce.
        val base64 = java.util.Base64.getEncoder().encodeToString(original)
        every { mockSharedPrefs.getString("myKey", null) } returns base64

        assertArrayEquals(original, BRKeyStore.retrieveEncryptedData(mockContext, "myKey"))
    }

    @Test
    fun `destroyEncryptedData removes the key from SharedPreferences`() {
        BRKeyStore.destroyEncryptedData(mockContext, "myKey")

        verify { mockEditor.remove("myKey") }
        verify { mockEditor.apply() }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // aliasObjectMap completeness
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `aliasObjectMap contains every expected alias constant`() {
        val expected = listOf(
            BRKeyStore.PHRASE_ALIAS,
            BRKeyStore.CANARY_ALIAS,
            BRKeyStore.PUB_KEY_ALIAS,
            BRKeyStore.WALLET_CREATION_TIME_ALIAS,
            BRKeyStore.PASS_CODE_ALIAS,
            BRKeyStore.FAIL_COUNT_ALIAS,
            BRKeyStore.SPEND_LIMIT_ALIAS,
            BRKeyStore.TOTAL_LIMIT_ALIAS,
            BRKeyStore.FAIL_TIMESTAMP_ALIAS,
            BRKeyStore.AUTH_KEY_ALIAS,
            BRKeyStore.TOKEN_ALIAS,
            BRKeyStore.PASS_TIME_ALIAS,
        )
        expected.forEach { alias ->
            assertTrue("aliasObjectMap is missing: $alias", BRKeyStore.aliasObjectMap.containsKey(alias))
        }
    }

    @Test
    fun `aliasObjectMap entries have self-consistent alias names`() {
        BRKeyStore.aliasObjectMap.forEach { (key, obj) ->
            assertEquals("alias mismatch for map key '$key'", key, obj.alias)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // showAuthenticationScreen – alias guard
    // ─────────────────────────────────────────────────────────────────────────

    @Test(expected = IllegalArgumentException::class)
    fun `showAuthenticationScreen throws for pubKey alias`() {
        BRKeyStore.showAuthenticationScreen(mockContext, 0, BRKeyStore.PUB_KEY_ALIAS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `showAuthenticationScreen throws for failCount alias`() {
        BRKeyStore.showAuthenticationScreen(mockContext, 0, BRKeyStore.FAIL_COUNT_ALIAS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `showAuthenticationScreen throws for token alias`() {
        BRKeyStore.showAuthenticationScreen(mockContext, 0, BRKeyStore.TOKEN_ALIAS)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getFilePath
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `getFilePath joins filesDir absolutePath with the given filename`() {
        val mockFile = mockk<java.io.File>()
        every { mockFile.absolutePath } returns "/data/data/com.brainwallet/files"
        every { mockContext.filesDir } returns mockFile

        val result = BRKeyStore.getFilePath("my_phrase", mockContext)

        assertEquals(
            "/data/data/com.brainwallet/files${java.io.File.separator}my_phrase",
            result,
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // writeBytesToFile / readBytesFromFile  (pure I/O – no Android deps)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `writeBytesToFile returns true and readBytesFromFile recovers identical bytes`() {
        val tmp = createTempFile(suffix = ".bin").also { it.deleteOnExit() }
        val data = "hello keystore".toByteArray()

        assertTrue(BRKeyStore.writeBytesToFile(tmp.absolutePath, data))
        assertArrayEquals(data, BRKeyStore.readBytesFromFile(tmp.absolutePath))
    }

    @Test
    fun `writeBytesToFile returns false for a path that cannot be created`() {
        assertFalse(
            BRKeyStore.writeBytesToFile(
                "/this/path/does/not/exist/file.bin",
                "data".toByteArray(),
            ),
        )
    }

    @Test
    fun `readBytesFromFile returns null for a non-existent path`() {
        assertNull(BRKeyStore.readBytesFromFile("/no/such/file.bin"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // putTrustedNodeIPAddress / getTrustedNodeIPAddress
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `putTrustedNodeIPAddress returns false for null address`() {
        assertFalse(BRKeyStore.putTrustedNodeIPAddress(null, mockContext, 0))
    }

    @Test
    fun `putTrustedNodeIPAddress returns false for empty address`() {
        assertFalse(BRKeyStore.putTrustedNodeIPAddress("", mockContext, 0))
    }

    @Test
    fun `putTrustedNodeIPAddress returns true when KeyStoreManager succeeds`() {
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns true

        assertTrue(BRKeyStore.putTrustedNodeIPAddress("165.227.48.221:9333", mockContext, 0))
    }

    @Test
    fun `putTrustedNodeIPAddress returns false when KeyStoreManager fails`() {
        every { mockKeyStoreManager.setDataBlocking(any(), any()) } returns false

        assertFalse(BRKeyStore.putTrustedNodeIPAddress("165.227.48.221:9333", mockContext, 0))
    }

    @Test
    fun `getTrustedNodeIPAddress decodes UTF-8 bytes back into the original address`() {
        val expected = "165.227.48.221:9333"
        every { mockKeyStoreManager.getDataBlocking(any()) } returns expected.toByteArray(Charsets.UTF_8)

        assertEquals(expected, BRKeyStore.getTrustedNodeIPAddress(mockContext, 0))
    }

    @Test
    fun `getTrustedNodeIPAddress returns null when no data is stored`() {
        every { mockKeyStoreManager.getDataBlocking(any()) } returns null

        assertNull(BRKeyStore.getTrustedNodeIPAddress(mockContext, 0))
    }
}
