# Who Checked My Phone (WCMP) 🛡️

[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blueviolet.svg)](https://developer.android.com/jetpack/compose)
[![Encryption](https://img.shields.io/badge/Encryption-AES--256--GCM-red.svg)](https://developer.android.com/reference/javax/crypto/Cipher)
[![License](https://img.shields.io/badge/License-Proprietary-lightgrey.svg)]()

**Who Checked My Phone (WCMP)** is an advanced, privacy-first security and anti-snoop suite for Android devices. WCMP protects user privacy by detecting unauthorized device handling, securing individual apps behind an instant biometric/PIN lock, and capturing tamper-evident security telemetry when unauthorized access is attempted.

---

## ✨ Features

* **🛡️ Smart App Lock**
  * Lock any installed application behind a secure PIN or Biometric prompt.
  * Real-time foreground detection powered by Android Accessibility Services and Usage Stats.
  * Low latency, seamless unlock flow returning control immediately to the protected app.

* **📷 Silent Intruder Photo Capture**
  * Captures high-resolution front-camera snapshots whenever unauthorized access or repeated failed unlock attempts occur.
  * Stored with SHA-256 verification hashes and incident telemetry.

* **🎙️ Ambient Audio Recording**
  * Records short ambient audio clips during perimeter or security breach events.
  * Encrypted at rest before being written to private storage.

* **📍 Incident Geolocation Telemetry**
  * Logs high-precision GPS coordinates when security incidents trigger outside trusted locations.

* **🔐 Encrypted Evidence Vault**
  * Hardware-backed **AES-256-GCM** authenticated encryption powered by **Android Keystore**.
  * Zero-knowledge local storage: Decryption keys remain exclusively within the device's secure hardware element.
  * Screen capture protection via `FLAG_SECURE` prevents unauthorized screenshots or screen mirroring of evidence records.

* **📊 Security Dashboard & Monitoring**
  * Live status of armed protection, recent security activity, and total encrypted evidence counters.
  * Filter evidence by Photos, Audio, and Location events with timestamped telemetry.

---

## 🏗️ Architecture & Tech Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose with Material Design 3
* **Concurrency:** Kotlin Coroutines & Asynchronous `StateFlow`
* **Local Persistence:** Room Database (SQLite) + Encrypted SharedPreferences
* **Hardware Security:** Android Keystore Provider (`AES/GCM/NoPadding`)
* **Camera Integration:** CameraX
* **Background Tasks:** Android Jetpack WorkManager

---

## 🚀 Building & Running

### Prerequisites
* Android Studio Hedgehog or newer
* Android SDK 34 / 35
* JDK 17 or Android Studio Embedded JBR

### Build from Terminal
```bash
# Clone the repository
git clone https://github.com/muhammadnuman340/wcmp.git
cd wcmp

# Build debug APK
./gradlew assembleDebug

# Install directly to a connected device
adb install -r -g app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 Documentation

Comprehensive architecture plans, security audits, and phase implementation notes are located in the [`docs/`](docs/) directory:
* [Phase Architecture & Design Notes](docs/)
* Security Hardening & Android Keystore integration guidelines

---

## 🔒 Privacy & Permissions

WCMP requires specific Android capabilities strictly for security and device protection:
* **Camera (`android.permission.CAMERA`)**: For silent intruder capture upon failed auth.
* **Microphone (`android.permission.RECORD_AUDIO`)**: For ambient incident audio recording.
* **Location (`android.permission.ACCESS_FINE_LOCATION`)**: For incident geolocation stamps.
* **Display Over Apps (`SYSTEM_ALERT_WINDOW`)**: For displaying the App Lock prompt over protected apps.
* **Accessibility (`WcmpAccessibilityService`)**: For real-time foreground app change detection.

All captured telemetry remains 100% on-device and is hardware-encrypted.
