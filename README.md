# SafeScreen 🛡️ — Privacy-First Digital Wellbeing & Shield

> **বর্তমান স্ট্যাটাস:** **v1.0.0-beta (Development Complete & Project Frozen) ✅**  
> **SafeScreen** হলো একটি আধুনিক, ১০০% অফলাইন ও গোপনীয়তা-বান্ধব (Privacy-First) ডিজিটাল ওয়েলবিয়িং ও প্রোটেকশন সিস্টেম। এটি ডিভাইসের ভেতরেই (100% On-Device) ডুয়াল মেশিন লার্নিং ইঞ্জিন (TensorFlow Lite & YAMNet) ব্যবহার করে রিয়েল-টাইমে অনাকাঙ্ক্ষিত/প্রাপ্তবয়স্ক (NSFW) ফ্রেম শনাক্ত করে ফ্রস্টেড গ্লাস ব্লার করে, ক্ষতিকর অডিও স্বয়ংক্রিয়ভাবে মিউট করে এবং Reels/Shorts-এর মতো ইনফিনিট স্ক্রলিং প্ল্যাটফর্মে আসক্তি নিয়ন্ত্রণ করে।

---

## 🌟 মূল বৈশিষ্ট্যসমূহ (Key Highlights)

- 👁️ **Visual Shield (On-Device Vision AI):** `MediaProjection` ও TFLite কম্পিউটার ভিশন মডেলের সাহায্যে স্ক্রিনের ফ্রেম যাচাই করে তাৎক্ষণিক ফ্রস্টেড গ্লাস ব্লার ওভারলে প্রয়োগ (Zero GC Pressure, NNAPI Hardware Acceleration)।
- 🔇 **Smart Audio Guard (YAMNet Audio AI):** `AudioPlaybackCapture` API ব্যবহার করে ডিভাইসের ইন্টারনাল অডিও এনালাইসিস এবং এক্সপ্লিসিট অডিও/সাউন্ডট্র্যাক শনাক্ত করে স্বয়ংক্রিয় অডিও মিউট।
- ⏳ **Reels & Shorts Guardian:** Facebook Reels, Instagram Reels, YouTube Shorts এবং TikTok-এর অবিরাম স্ক্রলিং ট্র্যাকিং এবং পূর্ণাঙ্গ ফোকাস ইন্টারভেনশন লকআউট।
- 🔒 **Absolute Privacy (Zero Cloud, Zero Analytics):** কোনো ছবি, ভিডিও, ফ্রেম বা অডিও ক্লাউডে পাঠানো হয় না। কোনো ইন্টারনেট ট্র্যাকার বা থার্ড-পার্টি অ্যানালিটিক্স নেই।
- 📊 **Local Insights (Room SQLite Database):** কতগুলো ফ্রেম ব্লার করা হয়েছে বা কত মিনিট বাঁচানো হয়েছে তার অফলাইন পরিসংখ্যান ও ডুয়াল-বার ট্রেন্ড চার্টস।
- 🛡️ **Security Lock:** PIN ও বায়োমেট্রিক অথেন্টিকেশন দিয়ে সেটিংস পরিবর্তন ও আনইনস্টল সুরক্ষা।

---

## 🔬 কোর আর্কিটেকচার ও এআই ইঞ্জিন সামারি (Developer Guide)

ভবিষ্যতে যেকোনো ডেভেলপার যাতে সহজে কোডবেস বুঝতে পারেন, তার সারসংক্ষেপ:

1. **Dual-Engine AI Coordinator (`AiShieldCoordinator.kt`):**
   - **TFLite NSFW Vision Classifier:** MobileNetV2 আর্কিটেকচারের অন-ডিভাইস কোয়ান্টাইজড মডেল (`nsfw.tflite`, ২২ MB)। এটি `DirectByteBuffer` (`1 × 224 × 224 × 3 × 4` বাইট) ব্যবহার করে পার-ফ্রেম কোনো নতুন অবজেক্ট তৈরি না করে NNAPI হার্ডওয়্যার এক্সিলারেশনে চলে।
   - **YAMNet Audio Classifier:** গুগলের অফিসিয়াল MediaPipe YAMNet মডেল (`yamnet.tflite`, ৩.৯ MB), যা ১৬kHz মনো অডিও স্ট্রিমের ১৫,৬০০ স্যাম্পল নিয়ে ৫২১টি AudioSet ক্লাসে ভোকাল ও অডিও সিগনেচার শনাক্ত করে।
   - **Zero-GC Decimation (`MediaProjectionCaptureEngine.kt`):** Sparse Delta Check মেকানিজম ব্যবহার করে স্ক্রিনের আনচেঞ্জড পিক্সেল বাইপাস করে মেমোরি ব্যান্ডউইথ ১০.৫ MB/s থেকে ১.১৩ MB/s-এ নামিয়ে আনা হয়েছে।
   - **Circular RingBuffer (`AudioPlaybackCaptureEngine.kt`):** অডিও ফ্রেম সংগ্রহের জন্য ফিক্সড-সাইজ সার্কুলার রিংবাফার ব্যবহৃত হয়েছে, যাতে দীর্ঘসময় চললেও কোনো মেমোরি লিক না হয়।

2. **Single Source of Truth (SSOT) Persistence:**
   - **Room Database (`AppDatabase.kt`):** `ThreatLogDao` (থ্রেট অডিট লগ), `WhitelistDao` (ব্যাংকিং ও প্রাইভেট অ্যাপ এক্সিম্পশন), এবং `FocusScheduleDao` (ফোকাস রুটিন শিডিউল)।
   - **Jetpack DataStore Preferences:** শিল্ড স্টেটাস, এআই সেনসিটিভিটি, ব্লার স্টাইল, এবং SHA-256 হ্যাশড সিকিউরিটি পিন ধারণ করে।

3. **System Overlays & Lifecycle Owners:**
   - `LiveBlurOverlayManager` এবং `FocusInterventionOverlayManager` অ্যান্ড্রয়েডের `TYPE_APPLICATION_OVERLAY` উইন্ডোতে স্বাধীন ComposeView রেন্ডার করে।
   - এতে ২-মিনিটের এমার্জেন্সি পাস কুলডাউন এবং সেফ-রিমুভাল ট্রাই-ক্যাচ নিশ্চিত করা আছে।

---

## 🛠️ টেকনোলজি স্ট্যাক (Tech Stack)

- **Language:** Kotlin 2.0.21 (JVM 21 / 17 Compatibility)
- **UI Framework:** Jetpack Compose & Material 3 (Custom Cyber-Dark Neon Design System)
- **Architecture:** Clean Architecture + MVVM + MVI UI Events
- **Dependency Injection:** Dagger Hilt 2.52 (KSP)
- **Database:** Room Database 2.6.1 with Coroutines Flow
- **Preferences:** Jetpack DataStore Preferences 1.1.1
- **Machine Learning Engine:** TensorFlow Lite 2.16.1 & TFLite Support Library
- **OS APIs:** `MediaProjection`, `AudioPlaybackCapture`, `AccessibilityService`, `WindowManager` Overlay, Foreground Service with `mediaProjection` type.
- **Min SDK:** 29 (Android 10) | **Target SDK:** 35 (Android 15)

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

# প্রোজেক্ট ক্লিন
./gradlew clean

# ফাইনাল প্রোডাকশন-রেডি রিলিজ APK বিল্ড (R8 Minified)
./gradlew assembleRelease

# কানেক্টেড ডিভাইসে সরাসরি রিলিজ ইনস্টল
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## 📱 বাস্তব ফোনে বেটা টেস্টিং নির্দেশিকা (Beta Testing Guide)

১. **APK ফাইল ট্রান্সফার:** `app/build/outputs/apk/release/app-release.apk` ফাইলটি ইউএসবি বা গুগল ড্রাইভের মাধ্যমে আপনার অ্যান্ড্রয়েড ফোনে নিন।
২. **ইনস্টল ও পারমিশন প্রদান:** অ্যাপটি ওপেন করে Permission Hub থেকে Accessibility Service, Display over other apps, এবং Notification পারমিশনগুলো এলাউ করুন।
৩. **শিল্ড সক্রিয়করণ:** হোম স্ক্রিনের সেন্ট্রাল ৩ডি শিল্ড বাটনে ট্যাপ করুন এবং অ্যান্ড্রয়েড সিস্টেমের "Start Now" ডায়ালগ নিশ্চিত করুন।
৪. **টেস্টিং:** ব্রাউজার বা সোশ্যাল মিডিয়ায় টেস্ট কন্টেন্ট প্লে করুন — অন-ডিভাইস এআই রিয়েল-টাইমে ক্ষতিকর ভিজ্যুয়াল ব্লার করবে এবং এক্সপ্লিসিট অডিও মিউট করবে। রিলস বা শর্টস ওপেন করলে দৈনিক নির্ধারিত সময়শেষে স্বয়ংক্রিয়ভাবে ফুলস্ক্রিন ফোকাস ইন্টারভেনশন লকআউট ট্রিগার হবে।

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

