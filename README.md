# SafeScreen 🛡️ — Privacy-First Digital Wellbeing & Shield

> **SafeScreen** হলো একটি আধুনিক, গোপনীয়তা-বান্ধব (Privacy-First) ডিজিটাল ওয়েলবিয়িং অ্যাপ। এটি ডিভাইসের ভেতরেই (100% On-Device) মেশিন লার্নিং (TensorFlow Lite) ব্যবহার করে রিয়েল-টাইমে অনাকাঙ্ক্ষিত/প্রাপ্তবয়স্ক (NSFW) ছবি ও ভিডিও শনাক্ত করে ব্লার করে, ক্ষতিকর ব্যাকগ্রাউন্ড মিউজিক স্বয়ংক্রিয়ভাবে মিউট করে এবং Reels/Shorts-এর মতো ইনফিনিট স্ক্রলিং প্ল্যাটফর্মে আসক্তি নিয়ন্ত্রণ করে।

---

## 🌟 মূল বৈশিষ্ট্যসমূহ (Key Highlights)

- 👁️ **Visual Shield (AI ব্লার প্রোটেকশন):** MediaProjection ও TFLite কম্পিউটার ভিশন মডেলের সাহায্যে স্ক্রিনের ফ্রেম যাচাই করে তাৎক্ষণিক ব্লার ওভারলে প্রয়োগ।
- 🔇 **Smart Audio Guard:** `AudioPlaybackCapture` API ব্যবহার করে ডিভাইসের ইন্টারনাল অডিও এনালাইসিস এবং মিউজিক ট্র্যাক শনাক্ত করে স্বয়ংক্রিয় অডিও মিউট।
- ⏳ **Reels & Shorts Controller:** Facebook Reels, Instagram Reels, YouTube Shorts এবং TikTok-এর অবিরাম স্ক্রলিং ট্র্যাকিং এবং দৈনিক সময়সীমা প্রয়োগ।
- 🔒 **Absolute Privacy (Zero Cloud):** কোনো ছবি, ভিডিও, ফ্রেম বা অডিও ক্লাউডে পাঠানো হয় না। কোনো ইন্টারনেট ট্র্যাকার বা থার্ড-পার্টি অ্যানালিটিক্স নেই।
- 📊 **Local Insights (Room Database):** কতগুলো ফ্রেম ব্লার করা হয়েছে বা কত সময় বাঁচানো হয়েছে তার অফলাইন পরিসংখ্যান।
- 🛡️ **Security Lock:** PIN ও বায়োমেট্রিক অথেন্টিকেশন দিয়ে সেটিংস পরিবর্তন সুরক্ষা।

---

## 🛠️ টেকনোলজি স্ট্যাক (Tech Stack)

### Android Platform
- **Language:** Kotlin 2.0.21 (JVM 17)
- **UI Framework:** Jetpack Compose & Material 3 (Custom Cyber-Dark Neon Palette)
- **Architecture:** Clean Architecture + MVVM + MVI-style UI States
- **Dependency Injection:** Dagger Hilt 2.52 (KSP)
- **Database:** Room Database 2.6.1 with Coroutines Flow
- **Machine Learning Engine:** TensorFlow Lite 2.16.1 & TFLite Support Library
- **OS APIs:**
  - `MediaProjection` (Android 10+ / API 29+)
  - `AudioPlaybackCaptureConfiguration` (API 29+)
  - `AccessibilityService` (Reels UI Node Detection)
  - `WindowManager` Overlay (`TYPE_APPLICATION_OVERLAY`)
  - Foreground Service with `mediaProjection` Type

### Chrome Extension (Phase 8 Roadmap)
- Manifest V3, WebExtensions API, DOM MutationObserver, CSS Injection, Local Storage.

---

## 📁 আর্কিটেকচার ও ফোল্ডার স্ট্রাকচার

```
app/src/main/java/com/safescreen/app/
├── SafeScreenApp.kt                  # @HiltAndroidApp Application Class
├── MainActivity.kt                   # Single Activity, Compose Entry Point
│
├── di/                               # Dagger-Hilt DI Modules
│   ├── AppModule.kt                  # Coroutine Dispatchers, TFLite Classifier
│   └── DatabaseModule.kt             # Room DB, DAOs & Repositories
│
├── data/                             # Data Layer (Local Data Sources & Models)
│   ├── local/
│   │   ├── AppDatabase.kt            # Room DB Definition
│   │   ├── dao/StatDao.kt            # Room Data Access Object
│   │   └── entity/StatEntity.kt      # Protection Stat SQLite Entity
│   ├── repository/
│   │   └── StatRepositoryImpl.kt     # StatRepository Implementation
│   └── tflite/
│       └── TFLiteClassifier.kt       # On-Device TFLite Model Execution Wrapper
│
├── domain/                           # Domain Layer (Business Logic & UseCases)
│   ├── model/                        # Domain Models & Sealed States
│   │   ├── ProtectionStat.kt
│   │   └── ShieldState.kt
│   ├── repository/
│   │   └── StatRepository.kt         # Repository Interface Contracts
│   └── usecase/                      # Business UseCases
│       ├── StartShieldUseCase.kt
│       ├── StopShieldUseCase.kt
│       └── GetStatsUseCase.kt
│
├── presentation/                     # Presentation Layer (Jetpack Compose UI)
│   ├── theme/                        # Cyber-Dark & Neon Palette
│   │   ├── Color.kt
│   │   ├── Typography.kt
│   │   └── Theme.kt
│   ├── components/                   # Reusable Compose Widgets
│   │   ├── ShieldButton.kt           # Pulsing Neon Shield Toggle
│   │   └── GlassmorphicCard.kt       # Frosted Glass UI Container
│   ├── navigation/                   # Compose Navigation Routes & NavHost
│   │   ├── Screen.kt
│   │   └── AppNavHost.kt
│   └── screens/                      # Main Screens
│       ├── splash/SplashScreen.kt
│       ├── onboarding/OnboardingScreen.kt
│       ├── permission/PermissionHubScreen.kt
│       ├── home/HomeScreen.kt
│       ├── stats/StatsScreen.kt
│       └── settings/SettingsScreen.kt
│
└── services/                         # Android OS Services
    └── ShieldProtectionService.kt    # Foreground Service for MediaProjection & Audio
```

---

## 🚀 প্রোজেক্ট ওপেন এবং বিল্ড করার নিয়ম

### পূর্বশর্ত (Prerequisites)
1. **JDK:** OpenJDK 17 বা তার বেশি
2. **Android Studio:** Android Studio Ladybug / Meerkat বা সমমানের আধুনিক ভার্সন
3. **Android SDK:** Compile SDK 35 (Android 15), Min SDK 29 (Android 10)

### বিল্ড কমান্ডসমূহ
```bash
# প্রোজেক্ট ডিরেক্টরিতে যান
cd /Users/betopia/StudioProjects/Projects_Another/SafeScreen

# প্রোজেক্ট ক্লিন ও বিল্ড টেস্ট
./gradlew clean

# ডিবাগ APK তৈরি
./gradlew assembleDebug

# কানেক্টেড ডিভাইসে রান
./gradlew installDebug
```

---

## 📚 বিস্তারিত ডকুমেন্টেশন লিঙ্কসমূহ

প্রোজেক্টের স্থাপত্য, গবেষণা এবং নীতি সম্পর্কিত পূর্ণাঙ্গ ডকুমেন্টগুলো নিম্নে সংযুক্ত রয়েছে:
- 📖 [প্রোজেক্ট কনটেক্সট (বাংলা)](./PROJECT_CONTEXT_BN.md)
- 🏗️ [সিস্টেম আর্কিটেকচার ডিজাইন](./ARCHITECTURE.md)
- 🗺️ [ডেভেলপমেন্ট রোডম্যাপ](./DEVELOPMENT_ROADMAP.md)
- 📋 [ফিচার মেট্রিক্স ও স্ট্যাটাস](./FEATURE_MATRIX.md)
- 🔬 [টেকনিক্যাল সম্ভাব্যতা ও ওএস সীমাবদ্ধতা](./TECHNICAL_FEASIBILITY.md)
- 🔒 [প্রাইভেসি পলিসি ও ডেটা গ্যারান্টি](./PRIVACY_POLICY.md)
- 🧪 [টেস্টিং ও ভ্যালিডেশন স্ট্র্যাটেজি](./TESTING_STRATEGY.md)
- 📈 [প্রোজেক্ট প্রগ্রেস লগ](./PROJECT_PROGRESS.md)
- 🔬 [Reels/Shorts কন্ট্রোল বিষয়ক গবেষণা](./docs/research/REELS_CONTROL_RESEARCH_BN.md)

---
*SafeScreen — Defending Your Focus, Protecting Your Values.*

