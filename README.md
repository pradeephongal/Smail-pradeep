# Smail 🛡️✉️
> **Smart Email Client with On-Device Naïve Bayes Spam & Phishing Defense**
> *Created by @PRADEEP*

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-4285F4.svg?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Room-SQLite-3DDC84.svg?logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📌 Overview

**Smail** is a modern, privacy-first Android email client featuring an **on-device Multinomial Naïve Bayes machine learning engine**. Designed to protect users from phishing attempts, spam campaigns, and malicious messages, Smail evaluates email text entirely offline on the client device—ensuring complete data confidentiality without sending personal communications to third-party cloud servers.

Smail blends the fluidity of **Jetpack Compose (Material 3)** with robust security controls, including **Biometric authentication**, encrypted PIN lock, custom warning alarms, and a live machine learning inspector.

---

## ✨ Key Features

### 🧠 1. Real-Time On-Device Machine Learning
- **Multinomial Naïve Bayes Classifier**: Computes spam vs. ham likelihood ratios using token frequency distributions with Laplace smoothing.
- **Live Draft Inspector**: Analyzes drafts in real-time as you compose, alerting you to suspicious trigger words and phishing risks before sending.
- **Interactive Classifier Playground**: Test arbitrary subjects and message bodies to view token contributions, confidence percentages, and class decisions.
- **Continuous On-Device Learning**: Marking emails as Spam or Not Spam dynamically adjusts word probabilities directly on the device.

### 📬 2. Complete Email Management
- **Multi-Folder Navigation**: Seamlessly navigate **Inbox**, **Sent Mail**, and the isolated **Spam Quarantine**.
- **Real-Time Outgoing Dispatch**: Send emails with live transmission feedback and immediate inbox loopback for self-addressed notes.
- **Full-Text Instant Search**: Search subject lines, senders, and email bodies instantly.
- **Detail View & Actions**: Star important messages, review Naïve Bayes diagnostic breakdowns, and restore falsely flagged emails.

### 🚨 3. Threat Interception & Alert System
- **Spam Warning Banner**: High-visibility warning banner triggered whenever incoming mail exceeds spam risk thresholds.
- **Auditory Alert System**: Synthesized audio alert notifying users when a dangerous or suspicious email is intercepted.
- **High-Priority Notifications**: System notifications for newly received messages with deep-link navigation directly into the app.

### 🔒 4. Privacy & Device Vault
- **Biometric & PIN Authentication**: Secure the application using fingerprint/face biometric authentication or a custom 4-digit PIN.
- **Zero Cloud Telemetry**: All spam classification models and message histories stay 100% stored locally within SQLite/Room.
- **Privacy Policy Transparency**: Integrated privacy dialog documenting zero-tracking and local storage guarantees.

### ⚡ 5. Live Simulation Engine
- **Preset Scenarios**: Simulate real-world scenarios including legitimate meetings, banking phishing scams, Nigerian prince lures, and delivery alerts.
- **Continuous Stream Simulator**: Toggle automated simulated traffic to test classifier throughput and quarantine reliability under load.

---

## 🏗️ Architecture & Technology Stack

```
com.example
├── MainActivity.kt               # Single-activity container, window insets, top/bottom navigation
├── data/
│   ├── AppDatabase.kt           # Room database definition with prepopulated seeds
│   ├── EmailDao.kt              # Reactive SQLite queries (Flow<List<EmailEntity>>)
│   ├── EmailEntity.kt           # Schema definition for stored emails
│   └── EmailRepository.kt       # Repository bridging Room and ML classification
├── ml/
│   └── NaiveBayesClassifier.kt  # On-device Multinomial Naïve Bayes engine
├── notifications/
│   └── NotificationHelper.kt    # System notification channels and dispatchers
├── security/
│   ├── AppLockManager.kt        # PIN storage and lock timeout manager
│   ├── BiometricAuthHelper.kt   # AndroidX BiometricPrompt integration
│   └── UserAccountManager.kt    # Local account session manager
├── ui/
│   ├── EmailViewModel.kt        # MVVM state container with StateFlow
│   ├── components/              # Reusable Compose widgets (cards, dialogs, footer)
│   ├── screens/                 # Inbox, Sent, Spam, Detail, Dashboard, Playground
│   └── theme/                   # Material Design 3 color schemes and typography
└── util/
    └── AlertSoundHelper.kt      # Synthesizer for audible spam warnings
```

### Core Technologies
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
- **Language**: Kotlin 2.0 (Coroutines, StateFlow)
- **Local Persistence**: [Android Room Database](https://developer.android.com/training/data-storage/room) with SQLite
- **Authentication**: AndroidX Biometrics (`BiometricPrompt`)
- **Testing**: Robolectric JVM unit tests and Roborazzi visual verification

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2+) or higher
- Android SDK 35 (compileSdk 35, minSdk 26)
- JDK 17 or higher

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/smail.git
   cd smail
   ```
2. Build the debug APK:
   ```bash
   gradle assembleDebug
   ```
3. Run the unit test suite:
   ```bash
   gradle :app:testDebugUnitTest
   ```

---

## 🛡️ Privacy & Security Philosophy
1. **Local Model Evaluation**: Email content never leaves the device for classification purposes.
2. **No External Trackers**: No analytics or ad SDKs are included.
3. **Hardware-Backed Protection**: Biometric unlocks make use of Android's secure hardware keystore.

---

## 👨‍💻 Author
Designed and developed by **@PRADEEP**.
