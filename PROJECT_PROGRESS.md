# SafeScreen — প্রজেক্ট প্রগ্রেস ও হ্যান্ডওভার রিপোর্ট (Project Progress)

> ## ⚠️ MANDATORY INSTRUCTION FOR ANY CODING AGENT / DEVELOPER ⚠️
> **অত্যন্ত গুরুত্বপূর্ণ নির্দেশনা:**  
> যেকোনো ডিভাইসে যেকোনো এআই কোডিং এজেন্ট (Antigravity, Cursor, Claude Dev, Copilot, ইত্যাদি) বা মানব ডেভেলপার এই প্রজেক্টে কাজ শুরু করলে:
> 1. প্রথমে এই `PROJECT_PROGRESS.md` এবং `PROJECT_CONTEXT_BN.md` ফাইল দুটি শুরু থেকে শেষ পর্যন্ত মনোযোগ দিয়ে পড়ুন।
> 2. কোডবেসের পূর্ববর্তী সিদ্ধান্ত, আর্কিটেকচারাল প্যাটার্ন ও সীমাবদ্ধতা মেনে কাজ করুন।
> 3. **প্রতিটি কাজের সেশন শেষ করার সাথে সাথে অবশ্যই এই `PROJECT_PROGRESS.md` ফাইলটি আপডেট করতে হবে।** 
> 4. আপডেটের মধ্যে থাকবে: আজ কোন কোন টাস্ক সম্পন্ন হয়েছে, কী কী সিদ্ধান্ত নেওয়া হয়েছে, এবং পরবর্তী এজেন্টকে ঠিক কোন ফাইল বা টাস্ক থেকে শুরু করতে হবে তার স্পষ্ট সিকোয়েন্স।  
> এর উদ্দেশ্য হলো যেকোনো ডিভাইসে যেকোনো সময়ে অন্য কোনো ডেভেলপার বা এজেন্ট এসে যেন এক মুহূর্তের মধ্যেও প্রজেক্টের ধারাবাহিকতা বজায় রেখে কাজ চালিয়ে যেতে পারে।

---

## 📅 সর্বশেষ আপডেট তথ্য
* **তারিখ:** ২৯ অক্টোবর ২০২৬
* **বর্তমান ফেজ:** Phase 1 Complete (Environment, Architecture, Boilerplate, Documentation & Git Sync Done)
* **পরবর্তী ফেজ:** Phase 2 (On-Device AI Engine & TFLite Model Integration)
* **GitHub Repository:** `https://github.com/saidur1004/SafeScreen.git`
* **বিল্ড স্ট্যাটাস:** ✅ Gradle Compile & Build Successful (`./gradlew assembleDebug` - 0 errors, 41 tasks executed)

---

## ১. আজ পর্যন্ত যা যা কাজ সফলভাবে সম্পন্ন হয়েছে (Completed Milestones)

### ক. প্রজেক্ট ইনিশিয়ালাইজেশন ও কারিগরি এনভায়রনমেন্ট
1. **প্রজেক্ট লোকেশন ও প্যাকেজ:** `/Users/betopia/StudioProjects/Projects_Another/SafeScreen` ডিরেক্টরিতে `com.safescreen.app` প্যাকেজ আইডিসহ একটি আধুনিক অ্যান্ড্রয়েড Kotlin KTS প্রজেক্ট ইনিশিয়ালাইজ করা হয়েছে।
2. **টুলচেন ও কম্প্যাটিবিলিটি:**
   - Min SDK: `29` (Android 10 - `AudioPlaybackCapture` সাপোর্টের জন্য বাধ্যতামূলক)
   - Compile SDK & Target SDK: `35` (Android 15)
   - Gradle Wrapper: `8.14` (Java 17 কম্প্যাটিবল)
   - Kotlin: `2.0.21`
   - Android Gradle Plugin: `8.7.3`
   - KSP (Kotlin Symbol Processing): `2.0.21-1.0.28`
3. **ডিপেন্ডেন্সি সেটআপ (`gradle/libs.versions.toml` ও `app/build.gradle.kts`):**
   - Jetpack Compose BOM 2024.10.01 + Material 3 + Extended Icons
   - Navigation Compose `2.8.5`
   - Dagger Hilt `2.52` (KSP ইন্টিগ্রেশন) + `androidx.hilt:hilt-navigation-compose:1.2.0`
   - Kotlin Coroutines & StateFlow `1.9.0`
   - Room Database `2.6.1` (KSP কম্পাইলার)
   - TensorFlow Lite & TFLite Support `2.16.1`
   - CameraX `1.4.1`
4. **বিল্ড টেস্ট:** `./gradlew assembleDebug` কমান্ড রান করে ভ্যালিডেট করা হয়েছে। কোনো সিনট্যাক্স, ডিক্লারেশন বা ডিপেন্ডেন্সি কনফ্লিক্ট নেই।

---

### খ. Clean Architecture ফোল্ডার স্ট্রাকচার ও বয়লারপ্লেট
`app/src/main/java/com/safescreen/app/` এর অধীনে প্রতিটি লেয়ার সুবিন্যস্তভাবে সাজানো হয়েছে:
* `/di`:
  - `DatabaseModule.kt`: Room DB (`AppDatabase`), `StatDao`, ও `StatRepository` প্রোভাইডার।
  - `AppModule.kt`: `@IoDispatcher`, `@MainDispatcher`, এবং `TFLiteClassifier` সিঙ্গলটন প্রোভাইডার।
* `/data`:
  - `local/entity/StatEntity.kt`: ব্লকিং ইভেন্ট ট্র্যাকিংয়ের জন্য Room Entity।
  - `local/dao/StatDao.kt`: অফলাইন ডেটা কুয়েরি ও হিস্ট্রি রিঅ্যাক্টিভ ফ্লো (`Flow<List<StatEntity>>`)।
  - `local/AppDatabase.kt`: ডেটাবেস স্কিমা ও মাইগ্রেশন কনফিগারেশন।
  - `repository/StatRepositoryImpl.kt`: ডেটাবেস ও ডোমেইনের মাঝে রিপোজিটরি ইমপ্লিমেন্টেশন।
  - `tflite/TFLiteClassifier.kt`: TFLite মডেল ইন্টারপ্রেটার ও ফ্রেম ক্লাসিফিকেশন র‍্যাপার।
* `/domain`:
  - `model/ShieldState.kt`: `Inactive`, `Active`, `ThreatDetected(message)`, `Paused` স্টেট।
  - `model/ProtectionStat.kt`: `ProtectionType` (NSFW, Music, Reels, AppBlocked)।
  - `repository/StatRepository.kt`: রিপোজিটরি ইন্টারফেস কন্ট্রাক্ট।
  - `usecase/StartShieldUseCase.kt`: ফোরগ্রাউন্ড সার্ভিস চালুর ইউজকেস।
  - `usecase/StopShieldUseCase.kt`: সার্ভিস বন্ধের ইউজকেস।
  - `usecase/GetStatsUseCase.kt`: আজকের ও সর্বমোট ব্লকিং ডেটা পাওয়ার ইউজকেস।
* `/presentation`:
  - `theme/`: আধুনিক সাইবার-ডার্ক নিয়ন থিম (Deep Black `#07090E`, Neon Cyan `#00E5FF`, Neon Green `#00FF9D`, Neon Pink `#FF2A6D`) এবং গ্লাস মরফিক এফেক্ট।
  - `components/`: `ShieldButton.kt` (পালসিং অ্যানিমেটেড নিয়ন শিল্ড সুইচ), `GlassmorphicCard.kt`।
  - `navigation/`: `Screen.kt` ও `AppNavHost.kt` (৬টি স্ক্রিনের নেভিগেশন রাউট)।
  - `screens/`:
    1. `splash/SplashScreen.kt`: ব্র্যান্ড অ্যানিমেশন ও স্টার্টআপ।
    2. `onboarding/OnboardingScreen.kt`: জিরো-ক্লাউড ও ১০০% প্রাইভেসি ভ্যালু প্রপোজিশন।
    3. `permission/PermissionHubScreen.kt`: Overlay, Screen Capture, Audio Capture, ও Notification পারমিশন গাইড।
    4. `home/HomeScreen.kt`: মূল ড্যাশবোর্ড, পালসিং শিল্ড বাটন, লাইভ মেট্রিক্স কার্ড।
    5. `stats/StatsScreen.kt`: অফলাইন সেভ হওয়া পরিসংখ্যান ও প্রটেকশন চার্ট।
    6. `settings/SettingsScreen.kt`: NSFW সেনসিটিভিটি, অটো-মিউট, রিলস লিমিট ও অ্যাপ হোয়াইটলিস্ট।
* `/services`:
  - `ShieldProtectionService.kt`: `foregroundServiceType="mediaProjection"` ভিত্তিক ফোরগ্রাউন্ড সার্ভিস যাতে ওএস ব্যাকগ্রাউন্ডে সার্ভিস কিল না করে।
* রুট ক্লাসসমূহ:
  - `SafeScreenApp.kt`: `@HiltAndroidApp` ক্লাস।
  - `MainActivity.kt`: `@AndroidEntryPoint` এবং `AppNavHost` লোডার।
* `AndroidManifest.xml`:
  - সমস্ত প্রয়োজনীয় পারমিশন (`FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PROJECTION`, `RECORD_AUDIO`, `SYSTEM_ALERT_WINDOW`, `POST_NOTIFICATIONS`, `CAMERA`, `RECEIVE_BOOT_COMPLETED`) এবং সার্ভিস ডিফিনিশন সংযুক্ত।

---

### গ. পূর্ণাঙ্গ প্রজেক্ট ডকুমেন্টেশন সিস্টেম
রুট ডিরেক্টরি ও `docs/research/` এ মোট ১০টি মাস্টার ডকুমেন্ট তৈরি ও আপডেট করা হয়েছে:
1. `README.md` — প্রজেক্ট ওভারভিউ, টেক স্ট্যাক, ফোল্ডার স্ট্রাকচার ও বিল্ড রান গাইড।
2. `Full Project Plan Initial.md` — প্রজেক্টের সম্পূর্ণ বিশদ মাস্টার প্ল্যান (রিলস সেকশন বাস্তবসম্মত বিকল্প দিয়ে আপডেটকৃত)।
3. `PROJECT_CONTEXT_BN.md` — ভিশন, মূল দর্শন, ৩টি প্রধান স্তম্ভ এবং টেকনিক্যাল বাউন্ডারি।
4. `ARCHITECTURE.md` — Clean Architecture লেয়ারসমূহ, ডেটাফ্লো এবং Mermaid ডায়গ্রাম।
5. `DEVELOPMENT_ROADMAP.md` — ৯টি ফেজের রোডম্যাপ (Phase 1 সম্পন্ন, Phase 2 পরবর্তী)।
6. `FEATURE_MATRIX.md` — সমস্ত ফিচারের স্ট্যাটাস, নির্ভরতা ও জটিলতা মেট্রিক্স।
7. `TECHNICAL_FEASIBILITY.md` — Android ওএস সীমাবদ্ধতা (`FLAG_SECURE`, Audio Capture, ব্যাটারি বাজেট)।
8. `PRIVACY_POLICY.md` — Zero-Cloud Policy এবং অন-ডিভাইস ডেটা প্রক্রিয়াকরণের শতভাগ গ্যারান্টি।
9. `TESTING_STRATEGY.md` — ইউনিট, ইন্টিগ্রেশন, ইনফারেন্স লেটেন্সি বেঞ্চমার্ক ও ব্যাটারি প্রোফাইলিং গাইড।
10. `docs/research/REELS_CONTROL_RESEARCH_BN.md` — রিলস নিয়ন্ত্রণে Android Sandboxing সীমাবদ্ধতা বনাম `UsageStatsManager` ও `AccessibilityService Overlay` কৌশল।

---

## ২. রিলস ও শর্ট-ভিডিও বিষয়ে গুরুত্বপূর্ণ প্রযুক্তিগত সিদ্ধান্ত

* **পূর্বের অবাস্তব চিন্তা বাদ দেওয়া হয়েছে:** ৩য় পক্ষের অ্যাপে (Facebook, Instagram, YouTube) CSS-এর মতো DOM মুছে ফেলা বা ১০টি রিলস নিখুঁতভাবে গুনে লক করার ধারণাটি Android OS-এর "Sandboxing" এবং "Dynamic Obfuscation"-এর কারণে প্রায় অসম্ভব ও চরম ভঙ্গুর।
* **গৃহীত বাস্তবসম্মত বিকল্প সমাধান:**
  1. **App-Level Time Limit (`UsageStatsManager`):** নির্দিষ্ট অ্যাপে দৈনিক মোট ব্যবহারের সময়সীমা (যেমন সর্বোচ্চ ৩০ মিনিট) নির্ধারণ করে সময় শেষ হলে অ্যাপ ব্লক করা।
  2. **Accessibility Blocker / Overlay (`AccessibilityService` + `WindowManager`):** ব্যবহারকারী যখন রিলস/শর্টস সেকশনে প্রবেশ করবেন, তখন `AccessibilityService` টেক্সট/নোড শনাক্ত করে তাৎক্ষণিকভাবে স্ক্রিনের ওপর একটি `TYPE_APPLICATION_OVERLAY` ব্লকার কার্ড (যেমন: *"Focus Time Active / Reels Blocked"*) ভাসিয়ে দেবে।
  3. **Chrome Extension-এ কোনো পরিবর্তন নেই:** ব্রাউজারে DOM Manipulation দিয়ে রিলস সরাসরি হাইড বা রিমুভ করা ১০০% সম্ভব, তাই সেখানে প্রাথমিক পরিকল্পনাটিই বহাল থাকবে।

---

## ৩. পরবর্তী এজেন্টের জন্য সুনির্দিষ্ট কাজের সিকোয়েন্স (Next Action Items for AI / Dev)

যেকোনো নতুন এজেন্ট বা ডেভেলপার এই প্রজেক্ট হাতে নেওয়ার পর **Phase 2** থেকে কাজ শুরু করবেন। নিচের সিকোয়েন্সটি কঠোরভাবে অনুসরণ করুন:

### ধাপ ১: অন-ডিভাইস TFLite মডেল সংগ্রহ ও অ্যাসেটস ইন্টিগ্রেশন
* `app/src/main/assets/` ডিরেক্টরিতে হালকা ও কোয়ান্টাইজড TFLite মডেল যুক্ত করা:
  - NSFW ইমেজ ক্লাসিফায়ারের জন্য একটি কোয়ান্টাইজড MobileNetV3 / EfficientNet-Lite মডেল (`nsfw_detector.tflite`)।
  - অডিও ক্লাসিফিকেশনের জন্য YAMNet বা সমমানের লাইটওয়েট মডেল (`audio_classifier.tflite`)।
* `app/build.gradle.kts` এ `aaptOptions { noCompress "tflite" }` কনফিগারেশন যোগ করা।

### ধাপ ২: TFLite ক্লাসিফায়ার বাস্তবায়ন (`com.safescreen.app.data.tflite.TFLiteClassifier`)
* `TFLiteClassifier.kt` ফাইলে `classifyFrame(bitmap: Bitmap)` ফাংশনে আসল টেনসর ইনপুট ও আউটপুট প্রসেসিং সম্পূর্ণ করা।
* `Interpreter.Options` এ থ্রেড সংখ্যা ও NNAPI / GPU ডেলিগেট কনফিগারেশন করা।
* টেস্ট বিটম্যাপ দিয়ে ইনফারেন্স স্পিড (<৩০ মিলিসেকেন্ড) বেঞ্চমার্ক করা।

### ধাপ ৩: MediaProjection ফ্রেম ক্যাপচার পাইপলাইন তৈরি (Phase 3)
* `ShieldProtectionService.kt` এ `MediaProjectionManager`-এর ইনটেন্ট হ্যান্ডেল করে `VirtualDisplay` এবং `ImageReader` তৈরি করা।
* ফ্রেম স্যাম্পলিং রেট প্রতি সেকেন্ডে ৩ থেকে ৫ ফ্রেমের মধ্যে সীমাবদ্ধ রাখা (ব্যাটারি সংরক্ষণের জন্য)।
* ক্যাপচার করা ফ্রেম `TFLiteClassifier`-এ পাঠিয়ে NSFW ডিটেক্ট হলে `WindowManager` দিয়ে ব্লার ওভারলে ড্র করা।

### ধাপ ৪: সেশন শেষে এই ফাইল আপডেট ও গিট পুশ
* কাজ শেষ করে `PROJECT_PROGRESS.md` আপডেট করবেন এবং গিট পুশ করবেন।

---

## ৪. গিট ও রিপোজিটোরি স্ট্যাটাস (Git Status)
* **Remote Repository URL:** `https://github.com/saidur1004/SafeScreen.git`
* **Default Branch:** `main`
* **Commit History:** সমস্ত প্রাথমিক সোর্স কোড, বিল্ড কনফিগারেশন এবং পূর্ণাঙ্গ ডকুমেন্টেশন মেইন ব্রাঞ্চে কমিট ও পুশ করা হয়েছে।
