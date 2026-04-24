
# Brainwallet: Android
The open source code of Brainwallet Android

## CI/CD Status
**main**: [![CircleCI](https://dl.circleci.com/status-badge/img/gh/gruntsoftware/android/tree/main.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/gruntsoftware/android/tree/main)

**develop**: [![CircleCI](https://dl.circleci.com/status-badge/img/gh/gruntsoftware/android/tree/develop.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/gruntsoftware/android/tree/develop)


# 🚀 Brainwallet Android — Release Notes
### v4.9.0

### ✨ New Features

#### 📋 Copy Transaction Details to Clipboard
Transaction details can now be copied directly to the clipboard from the transaction detail view. A new `BRClipboardManager.putDetailsClipboard()` method handles the copy action with full analytics tracking, making it easy to share or reference transaction information outside the app.

#### 📊 Analytics Tracking for Key Interactions
Firebase analytics events are now fired for core user interactions — fiat/LTC toggle, balance visibility toggle, and transaction detail copy — giving the team better insight into how users engage with the wallet's main screens.

---

### 🎨 UI / Design System

#### 🧱 Unified `bentoSurface` Design System
A new `bentoSurface()` modifier in `BentoModifiers.kt` consolidates gradient and border styling across the entire bento UI. Previously hardcoded values like `1.dp` borders are now driven by shared constants (`bentoBorderWidth`, `bentoCornerRadius`, `bentoCornerRadius`, `bentoSpacer`, `transactionActionHt`, `transactionDetailHt`), making the visual system consistent and easy to maintain going forward.

#### 🃏 Bento Section Refactors
- **GameHub** — migrated from `Card` to `Box` with `bentoSurface()`, with a `clickable` modifier added
- **Favourites** — refactored to `bentoSurface()` with improved `Spacer` layout
- **LTC Picker** — label repositioned to top, spacing and item heights adjusted
- **Transactions** — `TransactionFilter`, `TransactionsBentoScreen`, `ConfirmationStatus`, `CopyTransactionWidget`, `ExportTransactionsWidget`, `NoTxRow`, `TransactionDetail`, `TransactionFilterWidget`, `TransactionRow` all updated to unified styling
- **Tutorials & Bottom Nav Bar** — updated to use unified theme system

---

### 🔧 Technical Changes
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

## ✨ New UI — Favourites & Tutorials Bento Sections
**PR [#191](https://github.com/gruntsoftware/android/pull/191)**
The main screen now features two new purpose-built bento panels replacing the generic placeholder container. The **Favourites** panel displays a set of coloured circular indicators using the app's design theme colours (affirm, info, warn, error) with full dark/light mode support. The **Tutorials** panel shows a "Coming Soon" placeholder consistent with the broader bento design language. The old `HomeBentoContainer` has been removed. String resources for both sections have been translated across 19 locales including Arabic, Chinese (Traditional & Simplified), French, German, Farsi, Hindi, Indonesian, Italian, Japanese, Korean, Polish, Punjabi, Brazilian Portuguese, Russian, Spanish, Swedish, Turkish, and Ukrainian.

## 🔧 Send Screen Overhaul & Financial Precision
**PR [#182](https://github.com/gruntsoftware/android/pull/182)**
The send transaction flow has been fully redesigned as a multi-step paged screen (Pre-send → Confirm → Authenticate). All monetary values have been migrated from `Float` to `BigDecimal` with explicit rounding modes, eliminating floating-point precision issues in fiat/LTC conversions. Transaction fees are now calculated dynamically using `getFeePerKb()` rather than a static default. The pre-send composable has been broken into focused sub-components (`PreSendAddressRow`, `PreSendAmountRow`, `PreSendMemoRow`), and passcode UI components have been extracted into a dedicated package. QR code scanning is now wired into the send flow via an `EventBus` event. A `BWSender` class handles transaction preparation with proper error handling and activity context management.

## 🧪 Test Coverage Expanded to 309 Tests
**PR [#182](https://github.com/gruntsoftware/android/pull/182)**

Unit test coverage has been significantly expanded alongside the send screen work, bringing the total suite to **309 tests**. New test classes include `AppModuleTest`, `SendStateTest`, and `BrainwalletAppTest`, covering the Koin DI module wiring, send state logic, and core app initialisation paths.



### v3.9.0 - v3.9.1 Latest
Update README for improved description by @kcw-grunt in #78
Beta Release [ 🚀 ] Merge Develop into Main by @kcw-grunt in #81

#### Fixes and Changes:
- Current fiat preference from Settings needs to be reset if set in the TickerBento
- Localizations are covered to 100%
- Mini game FALLINMOJI is present in the Welcome and Game Hub
- When setting the theme from the Settings and the Lock Screen and the Main screen is not consistently applied
- In general the fonts in the app are not consistent and need to be managed properly for consistency
- Mini game sounds set a nominal level
- Layout for iPhone 8 - iPhone 17 Pro Max is set for: Welcome Screen
- Support.brainwallet.co link is fixed

### v3.6.0 Latest

#### What's Changed
🚀[Release v3.5.0] Merge into Main by @kcw-grunt in #51
Full Changelog: v3.4.2...v3.6.0\

#### Updates:
- using bundle exec fastlane single_unit_test_all
- downgrade firebase to 11.12.0
- polsih ci config
- 🦾 Chore/migrate ready onboarding
- Fix/login view crash
- 🧰 fix: Removed the thread blocking seen in the lock screen trx. loading
- Epic/settings migration (#50)
- Chore/refactor firebase analytics
- Chore/activate test coverage

### v3.3.1
- Added locale filter
- Made improvements to UI
- Fixes
