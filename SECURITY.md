# Security Policy

Brainwallet is a self-custodial Litecoin wallet. A vulnerability here can put
users' funds or seed phrases at direct risk, so we treat security reports
with priority and ask that they be disclosed to us privately first.

## Reporting a Vulnerability

**Please do not open a public GitHub issue for security vulnerabilities.**
Public issues are indexed and searchable, and a wallet vulnerability
disclosed that way can be exploited before we're able to ship a fix.

To report a vulnerability, contact us at [brainwallet.co/support](https://www.brainwallet.co/support)
and clearly mark your message as a **security report**.

When reporting, please include:
- A description of the vulnerability and its potential impact
- Steps to reproduce, or a proof of concept if you have one
- The affected version (or commit hash) and platform (Android/iOS)
- Any suggested severity or CVSS score, if you have one

**What to expect:**
- We aim to acknowledge new reports within a few business days.
- We'll work with you to confirm the issue, keep you updated as we
  investigate and remediate it, and let you know once a fix has shipped.
- With your permission, we're happy to credit you for the report once it's
  resolved.

## Scope

**In scope:**
- The Brainwallet Android app in this repository — key management,
  transaction signing and broadcast, wallet backup/recovery (seed phrase),
  PIN/biometric authentication, and secure local storage
  (`app/src/main/jni/core`, `app/src/main/secp/secp256k1`)
- Companion modules pulled in as git submodules: `bw-gdlib`,
  `android-private-general-purpose`, `android-build-logic`

**Out of scope:**
- Vulnerabilities in third-party dependencies with their own upstream
  disclosure process — please report to the upstream project (we'd
  appreciate a copy of the report too, but it isn't required)
- Social engineering, phishing, or attacks requiring physical access to an
  unlocked device
- Issues that only reproduce on a rooted/jailbroken device or a device
  already compromised by other malware
- Denial-of-service against our backend/infrastructure rather than the app
  itself
- Missing security best-practices with no demonstrated, concrete impact

## Supported Versions

Brainwallet ships continuously through the Play Store, and we only support
the most recently published release — please make sure you can reproduce an
issue on the latest version before reporting it.

| Version        | Supported |
| -------------- | :-------: |
| Latest release | ✅        |
| Older releases | ❌        |

## Safe Harbor

We consider security research conducted in good faith and in line with this
policy to be authorized. We will not pursue legal action against researchers
who make a genuine effort to avoid privacy violations, data destruction, and
service disruption, and who report vulnerabilities to us privately as
described above before any public disclosure.
