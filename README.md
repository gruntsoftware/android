# Brainwallet: Android

**Brainwallet** is a free, open-source, self-custodial [Litecoin](https://litecoin.org) wallet for Android. Your seed phrase and keys stay on your device — Brainwallet never has custody of your funds.

### CircleCI status  
[![Release](https://img.shields.io/github/v/release/gruntsoftware/android?style=plastic)](https://github.com/gruntsoftware/android/releases)
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/gruntsoftware/android/tree/main.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/gruntsoftware/android/tree/main)
[![Tests](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/grunt-claude-bot/ae4830cb1b5da4611598d20a517fd933/raw/tests-badge.json)](https://dl.circleci.com/status-badge/redirect/gh/gruntsoftware/android/tree/develop)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Play Store
[![Get it on Google Play](images/google-play-badge-en.png)](https://play.google.com/store/apps/details?id=ltd.grunt.brainwallet)

## Important Links & Download
- **iOS Repo**: [gruntsoftware/ios](https://github.com/gruntsoftware/ios)
- **Website**: [brainwallet.co](https://brainwallet.co)
- **Support**: [brainwallet.co/support](https://www.brainwallet.co/support)

## Why Brainwallet

**Standalone, not a client of our servers.** Brainwallet connects directly to the Litecoin peer-to-peer network using [SPV](https://github.com/bitcoin/bips/blob/master/bip-0037.mediawiki) (simplified payment verification) — checking balances and broadcasting transactions doesn't depend on any Brainwallet-run backend. An already-installed copy keeps working even if Brainwallet the company disappeared.

**Deterministic recovery.** Brainwallet is a [BIP32](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki) hierarchical-deterministic wallet — one seed phrase (paper key) recovers your full balance and transaction history on any device, forever.

**Keys never leave your device.** Private keys are generated and held in the Android Keystore, not in Brainwallet's infrastructure — we never have custody of your funds and no backend path to recover them for you.

**Built on open standards**, not a proprietary protocol:
- [SPV](https://github.com/bitcoin/bips/blob/master/bip-0037.mediawiki) for fast sync without running a full node
- [BIP32](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki) deterministic wallets
- [BIP38](https://github.com/bitcoin/bips/blob/master/bip-0038.mediawiki) import of password-protected paper wallets
- [BIP70](https://github.com/bitcoin/bips/blob/master/bip-0070.mediawiki) payment protocol support

## Features

- Self-custodial Litecoin wallet — your seed phrase never leaves your device
- Send/receive LTC with real-time fee estimation and fiat conversion
- Games to help you memorize your seed phrase (Check out Fallinmoji!)
- PIN app lock
- Buy/sell LTC and gift cards via in-app widgets
- Modern Jetpack Compose UI, with a legacy Java/UIKit-era codebase still being progressively migrated

## Auditing code

### Prerequisites
- Android Studio (current stable) with SDK 36 installed, NDK `25.1.8937393`, CMake `3.22.1`
- `minSdk 29`, `targetSdk 35`

## Architecture

- **Language/UI**: Kotlin, Jetpack Compose (legacy screens still in Java/`View`-based UIKit-era code under `presenter/`, being migrated incrementally)
- **DI**: [Koin](https://insert-koin.io/)
- **Concurrency**: Kotlin Coroutines & `StateFlow`
- **Wallet core**: native C/C++ (`app/src/main/jni/core`), elliptic curve crypto via [`secp256k1`](https://github.com/bitcoin-core/secp256k1)
- **Modules**: `app` (main app), `bw-gdlib` (mini-games, LibGDX, composite build), `modules/private-general-purpose` (in-app purchases + general-purpose features), `android-build-logic` (shared Gradle convention plugins)

## Testing

Unit tests live under `app/src/test/{java,kotlin}`. Run them with `./gradlew test`, or a specific variant with `./gradlew testBrainwalletDebugUnitTest`. CI runs on CircleCI (`.circleci/config.yml`).

## Security

Found a security vulnerability? Please **do not** open a public issue — see [SECURITY.md](SECURITY.md) for how to report it privately.


## License

Brainwallet Android is released under the [MIT License](LICENSE).

---

## Release Notes

For the full, up-to-date changelog see [GitHub Releases](https://github.com/gruntsoftware/android/releases) and the [compare view](https://github.com/gruntsoftware/android/compare). Highlights from recent versions:

---

### **v4.11.0**  [PR [#259](https://github.com/gruntsoftware/android/pull/259)]
---
#### 🎮 Fallinmoji v1.6.0 — Ready, Set, GO!
Tapping **Play** (or **Play Again** from the end screen) now runs a short intro sequence before the round begins instead of dropping straight into gameplay:
- **Ready dialog** — shows the round's 3 target emojis on star icons alongside the bonus-scoring rules for ~4.8s
- **3-2-1-GO! countdown** — a rewired full-screen countdown over a looping 6-frame ray-burst flipbook and center star accent, replacing the old libGDX `ParticleEffect`-driven burst for more reliable screen coverage and consistent timing
- Main menu button artwork polished (Play / How-To buttons)

#### 🧪 Test Coverage
Added 48 new unit tests across 7 new test classes, closing coverage gaps on previously-untested pure-logic utilities and the `gameinterface` package:
- `Base58Test`, `TypesConverterTest`, `BytesUtilTest`, `BRCompressorTest`, `BRDateUtilTest` — encode/decode round-trips, gzip/bz2 compression round-trips, and relative-time formatting edge cases
- `GameKoinModuleTest` — verifies the `gameModule` Koin DI wiring resolves `GameSlot` to `GdxGameSlot` as a singleton
- `GdxGameViewTest` — covers `GameExitData` JSON parsing (defaults, unknown-key tolerance) and the social-share dispatch logic in `handleGameExit` (Twitter/Instagram screenshot sharing, missing/empty screenshots, malformed JSON, unrecognized networks)

#### 🔧 Chores
- Added `skills-lock.json` to track installed Claude Code skills (`aso`, `mobile-android-design`)
- `.gitignore`: also exclude `CLAUDE.local.md`
- Version bumped: **v4.10.6 (202506345) → v4.11.0 (202506346)**

**Full Changelog**: https://github.com/gruntsoftware/android/compare/v4.10.6...v4.11.0

---

### **v4.10.6**  [PR [#253](https://github.com/gruntsoftware/android/pull/253)]
---
#### 🐛 Crash Fixes
- **`UnsatisfiedLinkError` on `core-lib` load** — `BRActivity`/`SyncReceiver` loaded the native wallet library via unguarded `System.loadLibrary()` static initializers, crashing every screen on launch if the OS failed to extract the `.so` (a common install/update failure mode on some OEMs). Native lib loading is now centralized in `BrainwalletApp.onCreate()` via [ReLinker](https://github.com/KeepSafe/ReLinker), which falls back to manual extraction from the APK when the system loader fails.
- **Ghost launcher icon crash** — the LibGDX game module's `AndroidLauncher` was accidentally exported with its own `MAIN`/`LAUNCHER` intent-filter, producing a second home-screen icon that crashed with `UnsatisfiedLinkError: libgdx.so not found` when tapped (it bypassed the app's real startup path). Fixed by removing the exported intent-filter.
- **`BreadActivity` NPE fix (#248)** — removed dead bottom-nav/toolbar code left over from an earlier refactor that deleted the view-binding logic but not the code still referencing those views, guaranteeing a `NullPointerException` on every launch.

#### 🧪 Test Reliability
- Fixed a flaky `BWSender` reentrancy test that relied on `Thread.sleep()` racing real-time against a `StandardTestDispatcher` — replaced with deterministic `CountDownLatch` synchronization.
- Eliminated a flaky `ShopBentoViewModelTest` (#250).
- Removed a stale `modules/bw-gdlib` gitlink that was breaking CI (#249).

#### 📚 Docs
- Real `SECURITY.md` with an actual vulnerability disclosure process, replacing GitHub's unfilled default template.
- Restructured `README.md` for external visitors/contributors (#251).

**Full Changelog**: https://github.com/gruntsoftware/android/compare/v4.10.4...v4.10.6

---

### **v4.10.4**  [PR [#236](https://github.com/gruntsoftware/android/pull/236)]
---
A feature-rich release centered on the Game Hub / emoji mini-game experience and an MVI refactor of the main screens:

- **New screens**: emoji pager/picker (`EmojiPagerScreen`, `PickEmojisScreen`, `YourEmojisScreen`, `HowToSetEmojisScreen`) and Game Hub bento screens (`GameHubBentoScreen`, `GameHubBentoPagerScreen`)
- **MVI refactor**: `MainScreen`, `SettingsScreen`, and `GameHub` all restructured into Event/State/ViewModel patterns (`MainScreenEvent`/`MainScreenState`/`MainViewModel`, `SettingsEvent`/`SettingsState`/`SettingsViewModel`, `GameHubEvent`/`GameHubState`/`GameHubViewModel`)
- **In-app review**: Google Play in-app review integration (`InAppReviewService`)
- Design system, font (`Bolden Van`, `Lilita One`, `Open Sauce One`), and localization updates alongside the above

**Full Changelog**: https://github.com/gruntsoftware/android/compare/v4.9.3...v4.10.4

---

### **v4.9.4 & v4.9.3**  [PRs [#236](https://github.com/gruntsoftware/android/pull/236), [#222](https://github.com/gruntsoftware/android/pull/222)–[#229](https://github.com/gruntsoftware/android/pull/229)]
---
- Fixed a crash in the send flow (`sendModelOnEvent`)
- Fixed the LTC/fiat picker layout
- Refactored analytics and reduced sync rate polling frequency (techdebt)
- Switched release tagging from release branches to tags going forward

**Full Changelog**: https://github.com/gruntsoftware/android/compare/v4.9.1...v4.9.4

---

### **v4.9.1**  [PR [#211](https://github.com/gruntsoftware/android/pull/211)]
---
- Fixed `BreadActivity.initializeViews` crash
- Added `WalletManager`, made `validateAddress` an instance method
- Expanded Send flow test coverage for better stability

---

### **v4.9.0**  [PR [#201](https://github.com/gruntsoftware/android/pull/201)]
---
#### ✨ New Features

##### 📋 Copy Transaction Details to Clipboard
Transaction details can now be copied directly to the clipboard from the transaction detail view. A new `BRClipboardManager.putDetailsClipboard()` method handles the copy action with full analytics tracking, making it easy to share or reference transaction information outside the app.

##### 📊 Analytics Tracking for Key Interactions
Firebase analytics events are now fired for core user interactions — fiat/LTC toggle, balance visibility toggle, and transaction detail copy — giving the team better insight into how users engage with the wallet's main screens.

---

#### 🎨 UI / Design System

##### 🧱 Unified `bentoSurface` Design System
A new `bentoSurface()` modifier in `BentoModifiers.kt` consolidates gradient and border styling across the entire bento UI. Previously hardcoded values like `1.dp` borders are now driven by shared constants (`bentoBorderWidth`, `bentoCornerRadius`, `bentoSpacer`, `transactionActionHt`, `transactionDetailHt`), making the visual system consistent and easy to maintain going forward.

##### 🃏 Bento Section Refactors
- **GameHub** — migrated from `Card` to `Box` with `bentoSurface()`, with a `clickable` modifier added
- **Favourites** — refactored to `bentoSurface()` with improved `Spacer` layout
- **LTC Picker** — label repositioned to top, spacing and item heights adjusted
- **Transactions** — `TransactionFilter`, `TransactionsBentoScreen`, `ConfirmationStatus`, `CopyTransactionWidget`, `ExportTransactionsWidget`, `NoTxRow`, `TransactionDetail`, `TransactionFilterWidget`, `TransactionRow` all updated to unified styling
- **Tutorials & Bottom Nav Bar** — updated to use unified theme system

---

#### 🔧 Technical Changes
- `MainScreen`, `MainScreenEvent`, and `MainViewModel` updated for new event handling and state management
- `NoWifiBalanceAlertScreen` and `ReceiveDialog` border widths replaced with `bentoBorderWidth` constant
- `strings.xml` updated with new/updated string resources
- `gradle/libs.versions.toml` and `app/build.gradle.kts` dependencies updated
- `detekt-app-baseline.xml` baseline updated
- Version bumped: **v4.8.4 (202506314) → v4.9.0 (202506315)**
- Release/v4.7.2 202506296 by @kcw-grunt in https://github.com/gruntsoftware/android/pull/134
- Release/v4.8.0 by @kcw-grunt in https://github.com/gruntsoftware/android/pull/145

**Full Changelog**: https://github.com/gruntsoftware/android/compare/v4.7.2...v4.9.0

---

### **v4.8.3**
---
#### ✨ New UI — Favourites & Tutorials Bento Sections
**PR [#191](https://github.com/gruntsoftware/android/pull/191)**
The main screen now features two new purpose-built bento panels replacing the generic placeholder container. The **Favourites** panel displays a set of coloured circular indicators using the app's design theme colours (affirm, info, warn, error) with full dark/light mode support. The **Tutorials** panel shows a "Coming Soon" placeholder consistent with the broader bento design language. The old `HomeBentoContainer` has been removed. String resources for both sections have been translated across 19 locales including Arabic, Chinese (Traditional & Simplified), French, German, Farsi, Hindi, Indonesian, Italian, Japanese, Korean, Polish, Punjabi, Brazilian Portuguese, Russian, Spanish, Swedish, Turkish, and Ukrainian.

#### 🔧 Send Screen Overhaul & Financial Precision
**PR [#182](https://github.com/gruntsoftware/android/pull/182)**
The send transaction flow has been fully redesigned as a multi-step paged screen (Pre-send → Confirm → Authenticate). All monetary values have been migrated from `Float` to `BigDecimal` with explicit rounding modes, eliminating floating-point precision issues in fiat/LTC conversions. Transaction fees are now calculated dynamically using `getFeePerKb()` rather than a static default. The pre-send composable has been broken into focused sub-components (`PreSendAddressRow`, `PreSendAmountRow`, `PreSendMemoRow`), and passcode UI components have been extracted into a dedicated package. QR code scanning is now wired into the send flow via an `EventBus` event. A `BWSender` class handles transaction preparation with proper error handling and activity context management.

#### 🧪 Test Coverage Expanded to 309 Tests
**PR [#182](https://github.com/gruntsoftware/android/pull/182)**
Unit test coverage has been significantly expanded alongside the send screen work, bringing the total suite to **309 tests**. New test classes include `AppModuleTest`, `SendStateTest`, and `BrainwalletAppTest`, covering the Koin DI module wiring, send state logic, and core app initialisation paths.

---

### **v3.9.0 – v3.9.1**
---
- Update README for improved description by @kcw-grunt in #78
- Beta Release [ 🚀 ] Merge Develop into Main by @kcw-grunt in #81
- Current fiat preference from Settings needs to be reset if set in the TickerBento
- Localizations are covered to 100%
- Mini game FALLINMOJI is present in the Welcome and Game Hub
- Theme applied from Settings/Lock Screen/Main screen made consistent
- Fonts made consistent across the app
- Mini game sounds set to a nominal level
- Layout fixes for iPhone 8 – iPhone 17 Pro Max on the Welcome Screen
- Support.brainwallet.co link fixed

---

### **v3.6.0**
---
- 🚀 [Release v3.5.0] merged into Main by @kcw-grunt in #51
- Full Changelog: v3.4.2...v3.6.0
- Switched to `bundle exec fastlane single_unit_test_all`
- Downgraded Firebase to 11.12.0
- CI config polish
- 🦾 Chore/migrate ready onboarding
- Fix/login view crash
- 🧰 Fix: removed thread blocking in the lock screen transaction loading
- Epic/settings migration (#50)
- Chore/refactor Firebase analytics
- Chore/activate test coverage

---

### **v3.3.1**
---
- Added locale filter
- UI improvements
- Various fixes
