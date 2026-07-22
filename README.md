# Brainwallet: Android

**Brainwallet** is a free, open-source, self-custodial [Litecoin](https://litecoin.org) wallet for Android. Your seed phrase and keys stay on your device — Brainwallet never has custody of your funds.

[![CircleCI](https://dl.circleci.com/status-badge/img/gh/gruntsoftware/android/tree/main.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/gruntsoftware/android/tree/main)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Website](https://img.shields.io/badge/website-brainwallet.co-blue)](https://brainwallet.co)

**CI status** — [main](https://dl.circleci.com/status-badge/redirect/gh/gruntsoftware/android/tree/main) · [develop](https://dl.circleci.com/status-badge/redirect/gh/gruntsoftware/android/tree/develop)

## Download

- **Play Store**: [ltd.grunt.brainwallet](https://play.google.com/store/apps/details?id=ltd.grunt.brainwallet)
- **iOS**: [gruntsoftware/ios](https://github.com/gruntsoftware/ios)
- **Website**: [brainwallet.co](https://brainwallet.co)
- **Support**: [brainwallet.co/support](https://www.brainwallet.co/support)

## Features

- Self-custodial Litecoin wallet — your seed phrase never leaves your device
- Send/receive LTC with real-time fee estimation and fiat conversion
- PIN and biometric app lock
- Buy/sell LTC and gift cards via in-app widgets
- In-app mini-games (built on a shared LibGDX library, [`bw-gdlib`](https://github.com/gruntsoftware/bw-gdlib))
- Modern Jetpack Compose UI, with a legacy Java/UIKit-era codebase still being progressively migrated

## Getting Started

### Prerequisites
- Android Studio (current stable) with SDK 36 installed, NDK `25.1.8937393`, CMake `3.22.1`
- `minSdk 29`, `targetSdk 35`

### Clone with submodules

This repo pulls in several git submodules (`android-build-logic`, `bw-gdlib`, `modules/private-general-purpose`, native wallet code under `app/src/main/jni/core` and `app/src/main/secp/secp256k1`). Clone with:

```bash
git clone --recurse-submodules https://github.com/gruntsoftware/android.git
# or, if already cloned:
git submodule update --init --recursive
```

### Local configuration

Some values (signing keystores, Firebase config) are pulled from `local.properties`, which is gitignored and not included in this repo. On CI these are materialized from encrypted environment variables at build time (see `.circleci/config.yml`). To build locally you'll need to supply your own debug keystore and a `local.properties` with matching `DEBUG_STORE_FILE`/`DEBUG_STORE_PASSWORD`/`DEBUG_KEY_ALIAS`/`DEBUG_KEY_PASSWORD` entries, plus your own `app/google-services.json` from a Firebase project.

### Build & run

```bash
./gradlew assembleDebug      # build a debug APK
./gradlew installDebug       # install on a connected device/emulator
./gradlew test               # run unit tests
./gradlew detekt             # static analysis
```

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

## Contributing

Pull requests are welcome — please target the `develop` branch. The [PR template](.github/PULL_REQUEST_TEMPLATE.md) will guide you through what to include (platform, type of change, tests, etc.).

## License

Brainwallet Android is released under the [MIT License](LICENSE).

---

## Release Notes

For the full, up-to-date changelog see [GitHub Releases](https://github.com/gruntsoftware/android/releases) and the [compare view](https://github.com/gruntsoftware/android/compare). Highlights from recent versions:

### v4.9.0

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

### v4.8.3

#### ✨ New UI — Favourites & Tutorials Bento Sections
**PR [#191](https://github.com/gruntsoftware/android/pull/191)**
The main screen now features two new purpose-built bento panels replacing the generic placeholder container. The **Favourites** panel displays a set of coloured circular indicators using the app's design theme colours (affirm, info, warn, error) with full dark/light mode support. The **Tutorials** panel shows a "Coming Soon" placeholder consistent with the broader bento design language. The old `HomeBentoContainer` has been removed. String resources for both sections have been translated across 19 locales including Arabic, Chinese (Traditional & Simplified), French, German, Farsi, Hindi, Indonesian, Italian, Japanese, Korean, Polish, Punjabi, Brazilian Portuguese, Russian, Spanish, Swedish, Turkish, and Ukrainian.

#### 🔧 Send Screen Overhaul & Financial Precision
**PR [#182](https://github.com/gruntsoftware/android/pull/182)**
The send transaction flow has been fully redesigned as a multi-step paged screen (Pre-send → Confirm → Authenticate). All monetary values have been migrated from `Float` to `BigDecimal` with explicit rounding modes, eliminating floating-point precision issues in fiat/LTC conversions. Transaction fees are now calculated dynamically using `getFeePerKb()` rather than a static default. The pre-send composable has been broken into focused sub-components (`PreSendAddressRow`, `PreSendAmountRow`, `PreSendMemoRow`), and passcode UI components have been extracted into a dedicated package. QR code scanning is now wired into the send flow via an `EventBus` event. A `BWSender` class handles transaction preparation with proper error handling and activity context management.

#### 🧪 Test Coverage Expanded to 309 Tests
**PR [#182](https://github.com/gruntsoftware/android/pull/182)**
Unit test coverage has been significantly expanded alongside the send screen work, bringing the total suite to **309 tests**. New test classes include `AppModuleTest`, `SendStateTest`, and `BrainwalletAppTest`, covering the Koin DI module wiring, send state logic, and core app initialisation paths.

### v3.9.0 – v3.9.1
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

### v3.6.0
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

### v3.3.1
- Added locale filter
- UI improvements
- Various fixes
