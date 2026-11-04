# SafeScreen — প্রজেক্ট প্রগ্রেস ও সম্পূর্ণ হ্যান্ডওভার রিপোর্ট (Project Progress & Master Handover Report)

> ## ⚠️ MANDATORY INSTRUCTION FOR ALL FUTURE CODING AGENTS & DEVELOPERS ⚠️
> **অত্যন্ত গুরুত্বপূর্ণ বাধ্যতামূলক নির্দেশনা (যেকোনো ডিভাইসে কাজ শুরুর পূর্বে অবশ্যই পড়ুন):**  
> আপনি যদি বাসা, অফিস বা অন্য যেকোনো ডিভাইসে অন্য কোনো এআই এজেন্ট (Antigravity, Cursor, Claude Dev, Copilot, ইত্যাদি) বা মানব ডেভেলপার হিসেবে কাজ শুরু করেন, তবে কোডিং শুরুর পূর্বে নিচের নির্দেশনাগুলো অক্ষরে অক্ষরে অনুসরণ করুন:
>
> 1. **বাধ্যতামূলক কনটেক্সট লোডিং (Mandatory Reading Order):**
>    - [**`PROJECT_PROGRESS.md`**](./PROJECT_PROGRESS.md) (এই ফাইল — বর্তমান অবস্থা, সমস্ত সমস্যার সমাধান ও পরবর্তী হ্যান্ডওভার রোডম্যাপ)
>    - [**`DESIGN.md`**](./DESIGN.md) (সাইবার-ডার্ক গ্লাস মরফিজম ডিজাইন সিস্টেম, নিয়ন কালার প্যালেট, প্যাডিং ও কম্পোনেন্ট স্ট্যান্ডার্ড)
>    - [**`Screen Workflow/MASTER_UI_PROMPTS.md`**](./Screen%20Workflow/MASTER_UI_PROMPTS.md) (১৬টি স্ক্রিনের সুনির্দিষ্ট প্রম্পট বুক ও আর্কিটেকচারাল রিকোয়ারমেন্টস)
>    - [**`PROJECT_CONTEXT_BN.md`**](./PROJECT_CONTEXT_BN.md) (সেফস্ক্রিন প্রজেক্টের ফিলোসফি, ১০০% অন-ডিভাইস এআই গার্ডিয়ান ও প্রাইভেসি স্ট্যান্ডার্ড)
>    - [**`DEVELOPMENT_ROADMAP.md`**](./DEVELOPMENT_ROADMAP.md) (১০টি সুস্পষ্ট ফেজের সম্পূর্ণ রোডম্যাপ)
>    - [**`ARCHITECTURE.md`**](./ARCHITECTURE.md) (Clean Architecture: Data, Domain, Presentation, Services, DI)
>
> 2. **ধাপে ধাপে বাস্তবায়ন ও স্পষ্ট অনুমতি নীতি (Strict Step-by-Step Policy):**  
>    কোনো অবস্থাতেই একসাথে একাধিক স্ক্রিন বা একাধিক মডিউলের কোড লিখবেন না। প্রতিটি Prompt সম্পূর্ণ শেষ করে, `./gradlew assembleDebug` দিয়ে বিল্ড ও এমুলেটরে লাইভ টেস্ট করে, ব্যবহারকারীকে সাইড-বাই-সাইড রিপোর্ট দেখিয়ে তাঁর কাছ থেকে স্পষ্ট **"Go ahead"** (অনুমতি) পাওয়ার পরেই কেবল পরবর্তী Prompt শুরু করবেন।
>
> 3. **সেশন শেষে বাধ্যতামূলক হ্যান্ডওভার আপডেট:**  
>    প্রতিটি কাজের সেশন শেষ করার পর অবশ্যই এই `PROJECT_PROGRESS.md` আপডেট করতে হবে এবং গিটহাবে পুশ করতে হবে।

---

## 📌 ১. প্রজেক্টের বর্তমান ওভারভিউ ও লাইভ স্ট্যাটাস (Executive Snapshot)

* **সর্বশেষ আপডেটের তারিখ ও সময়:** ০৪ নভেম্বর ২০২৬
* **বর্তমান পর্যায়:** **Phase 2B — UI Workflow Implementation (Module 01 থেকে Module 07 Prompt 1 পর্যন্ত ১০০% সফলভাবে সম্পন্ন ও এমুলেটরে লাইভ ভেরিফাইড ✅)**
* **পরবর্তী তাৎক্ষণিক কাজ (Immediate Next Task):** **Module 07: Prompt 2 (`FocusInterventionOverlay.kt` — Fullscreen Focus Intervention & Doomscroll Breaker Overlay)**
* **গিট রিপোজিটরি:** `https://github.com/saidur1004/SafeScreen.git` (মেইন ব্রাঞ্চ: `main`)
* **টেকনিক্যাল টুলচেইন:** Kotlin 2.0.21, Gradle 8.14, AGP 8.7.3, Jetpack Compose BOM 2024.10.01, Dagger-Hilt 2.52, Room 2.6.1, Java 21, Min SDK 29 (Android 10), Target SDK 35 (Android 15)
* **টেস্ট ডিভাইস:** Android Emulator (`emulator-5554`, Google APIs ARM64/x86_64, API 34/35)

---

## 📊 ২. সামগ্রিক মডিউল ও ফিচার ভেরিফিকেশন ম্যাট্রিক্স (Verified Status Matrix)

| মডিউল / ক্যাটাগরি | ফিচার / স্ক্রিন | স্ট্যাটাস | ফাইল রেফারেন্স ও প্রমাণ |
| :--- | :--- | :---: | :--- |
| **Toolchain & Build** | Gradle 8.14, Kotlin 2.0.21, AGP 8.7.3, Hilt 2.52, Java 21 | ✅ Verified | `./gradlew assembleDebug` সফল (০ এরর, ০ ওয়ার্নিং) |
| **Core Architecture** | 3-Layer Clean Architecture (data, domain, presentation, services, di) | ✅ Verified | `app/src/main/java/com/safescreen/app/` |
| **UI Design System** | Cyber-Dark Theme, Tokens, Glassmorphism, Material 3, Typography | ✅ Verified | `DESIGN.md`, `Color.kt`, `Theme.kt`, `Typography.kt` |
| **Global Navigation** | 4-Tab Floating Bottom Navigation Bar (`Home`, `Stats`, `Focus`, `Settings`) | ✅ Verified | `FloatingBottomNavigationBar.kt` & `AppNavHost.kt` |
| **Module 01: Onboarding** | `SplashScreen.kt` (৩ডি বেভেল্ড শিল্ড ও পালস) & `OnboardingScreen.kt` (৩টি পিলার্স) | ✅ 100% Polished | `presentation/screens/splash/`, `.../onboarding/` |
| **Module 02: Permissions** | `PermissionHubScreen.kt` (পারমিশন কার্ডস) & `BatteryOptimizationDialog.kt` (গোল্ডেন শিট) | ✅ 100% Polished | `presentation/screens/permission/` |
| **Module 03: Dashboard** | `HomeScreen.kt` (৩ডি সেন্ট্রাল শিল্ড বাটন, লাইভ মেট্রিক্স) & `BlurStylePreviewSheet.kt` (স্প্লিট ভিউ) | ✅ 100% Polished | `presentation/screens/home/` |
| **Module 04: Wellbeing** | `ReelsControlScreen.kt` (টাইম লিমিট স্লাইডার্স) & `FocusScheduleScreen.kt` (শিডিউল ও ডে পিলস) | ✅ 100% Polished | `presentation/screens/focus/` |
| **Module 05: Analytics** | `StatsScreen.kt` (ডুয়াল-বার চার্ট, ডোনাট ব্রেকডাউন) & `DetailedActivityLogScreen.kt` (টাইমলাইন) | ✅ 100% Polished | `presentation/screens/stats/` |
| **Module 06: Settings** | `SettingsScreen.kt` (এআই সেনসিটিভিটি, এফপিএস পিলস, সিকিউরিটি টগলস, ক্যাশ ক্লিয়ার) | ✅ 100% Polished | `presentation/screens/settings/SettingsScreen.kt` |
| **Module 06: Whitelist** | `AppWhitelistScreen.kt` (সার্চ, ক্যাটাগরি চিপস, ব্যাংকিং প্রি-এক্সিম্পশন, স্টিকি সামারি) | ✅ 100% Polished | `presentation/screens/settings/AppWhitelistScreen.kt` |
| **Module 06: Security** | `SecurityLockScreen.kt` (৩ডি প্যাডলক, ৪-ডট পিন প্যাড, এরর শেক, বায়োমেট্রিক আনলক) | ✅ 100% Polished | `presentation/screens/settings/SecurityLockScreen.kt` |
| **Module 07: Live Blur** | `LiveBlurOverlayView.kt` (`WindowManager` Overlay, 3D Emblem, Breathing Aura, 5s Peek) | ✅ 100% Polished | `services/overlay/LiveBlurOverlayView.kt` |
| **Module 07: Focus Overlay** | `FocusInterventionOverlay.kt` (Screen 15 — Fullscreen Doomscroll Lockout Overlay) | ⏳ **Next Up (Prompt 2)** | `services/overlay/FocusInterventionOverlay.kt` |
| **Module 08: Fallbacks** | `SafeScreenStates` (Clean, Paused, AI Core Error States) | ⏳ Planned (Phase 2B) | `presentation/components/states/` |
| **Phase 3: Persistence** | Room Database (ThreatLogEntity, WhitelistEntity) & DataStore Preferences | ⏳ Planned (Phase 3) | `AppDatabase.kt`, `StatDao.kt` |
| **Phase 4: OS Services** | MediaProjection, AudioRecord, AccessibilityService, Foreground Notification | ⏳ Planned (Phase 4) | `services/ShieldProtectionService.kt` |
| **Phase 5: On-Device AI** | INT8 Quantized TFLite NSFW Filter & YAMNet Audio Classifier Integration | ⏳ Planned (Phase 5) | `domain/ml/TFLiteClassifier.kt` |

---

## 🛠️ ৩. মডিউল অনুযায়ী এখন পর্যন্ত যা যা কাজ সম্পন্ন করা হয়েছে (Complete Details)

### ১. গ্লোবাল বটম নেভিগেশন আর্কিটেকচার (`FloatingBottomNavigationBar.kt`)
- **সমস্যা যা ছিল:** পূর্বে ফ্লোটিং বটম নেভিগেশন বারটি শুধু `HomeScreen.kt`-এর ভেতর প্রাইভেট কম্পোনেন্ট হিসেবে ছিল। ফলে ইউজার যখন Stats, Focus বা Settings স্ক্রিনে যেত, বটম বারটি গায়েব হয়ে যেত।
- **সমাধান:** `FloatingBottomNavigationBar.kt` নামে একটি সম্পূর্ণ স্বাধীন, পুনর্ব্যবহারযোগ্য কম্পোনেন্ট তৈরি করে `AppNavHost.kt`-এ সংযুক্ত করা হয়েছে।
- **৪টি মূল ট্যাব (Persistent 4 Main Tabs):** `Home`, `Stats`, `FocusSchedule` এবং `Settings` স্ক্রিনের মধ্যে সুইচ করার সময় বটম বারটি স্থায়ীভাবে স্ক্রিনের নিচে ফ্লোট করে এবং সিলেক্টেড আইটেমের নিচে গ্লোয়িং নিয়ন হ্যালো ও অ্যাক্টিভ পিল মসৃণভাবে অ্যানিমেট হয়।
- **ইনার স্ক্রিনে অটো-হাইড (Smart Auto-Hiding):** যেকোনো সাব-স্ক্রিনে (`DetailedActivityLog`, `ReelsControl`, `AppWhitelist`, `SecurityLock`, `Splash`, `Onboarding`, `PermissionHub`) প্রবেশ করলে মসৃণ অ্যানিমেশনের মাধ্যমে বটম বারটি নিচে নেমে লুকিয়ে যায় এবং ব্যাক বাটন চাপলে আবার স্বয়ংক্রিয়ভাবে ফিরে আসে।

### ২. মডিউল ০১: অনবোর্ডিং ফ্লো (`SplashScreen` & `OnboardingScreen`)
- **SplashScreen:** সেন্ট্রাল ৩ডি বেভেল্ড শিল্ড, সাইবার নিয়ন গ্লো, রিয়েলিস্টিক এআই লোডার ও ডিপ ব্ল্যাক অ্যাম্বিয়েন্ট সারফেস।
- **OnboardingScreen:** ৩টি প্রধান ভ্যালু প্রপোজিশন ফ্রস্টেড গ্লাস কার্ডস (`Visual Shield`, `Smart Audio Guard`, `Doomscroll Breaker`), ১০০% অফলাইন এআই প্রাইভেসি ব্যাজ ও নিয়ন সায়ান "Get Started" বাটন।

### ৩. মডিউল ০২: পারমিশন ম্যানেজমেন্ট (`PermissionHubScreen` & `BatteryOptimizationDialog`)
- **PermissionHubScreen:** সিস্টেম অ্যালার্ট উইন্ডো, মিডিয়া প্রোজেকশন, অডিও রেকর্ড, অ্যাক্সেসিবিলিটি সার্ভিস ও ফোরগ্রাউন্ড নোটিফিকেশনের জন্য ৫টি প্রিমিয়াম ফ্রস্টেড পারমিশন কার্ড। গ্রান্টেড হলে তাৎক্ষণিক নিয়ন গ্রিন ব্যাজ ও ভাইব্রেন্ট অ্যানিমেশন।
- **BatteryOptimizationDialog:** ফ্রস্টেড বটম শিট, অরবিটাল গোল্ডেন রিংস সহ ৩ডি গ্লোয়িং ব্যাটারি আইকন, "Allow Unrestricted" সলিড সায়ান বাটন ও "Later" বাটন।

### ৪. মডিউল ০৩: কোর হোম ও ড্যাশবোর্ড (`HomeScreen` & `BlurStylePreviewSheet`)
- **HomeScreen:** ফ্ল্যাগশিপ রিয়েল-টাইম প্রোটেকশন ড্যাশবোর্ড, ৩ডি ইন্টারঅ্যাক্টিভ শিল্ড অ্যাক্টিভেশন বাটন (`ARMED` / `DISARMED`), পালসিং স্টেটাস ব্যাজ, এআই ফ্রেম রেট ইন্ডিকেটর (৫ FPS) এবং লাইভ থ্রেট কাউন্টার্স।
- **BlurStylePreviewSheet:** শিল্ড ব্লার কাস্টমাইজেশন বটম শিট, ৩টি স্টাইল সিলেক্টর (`Frosted Glass`, `Pixelated Censor`, `Opaque Dark Mask`), ইন্টেনসিটি স্লাইডার ও লাইভ স্প্লিট ইন্টারঅ্যাক্টিভ বিফোর/আফটার প্রিভিউ।

### ৫. মডিউল ০৪: রিলস ও ডিজিটাল ওয়েলবিয়িং (`ReelsControlScreen` & `FocusScheduleScreen`)
- **ReelsControlScreen:** ৩ডি আওয়ারগ্লাস হিরো, কাস্টম নিয়ন সায়ান টাইম লিমিট স্লাইডার্স (ডেইলি কোটা ও সেশন লিমিট), শর্ট-ভিডিও প্ল্যাটফর্ম টগলস (YouTube Shorts, Instagram Reels, TikTok) এবং কাস্টম অ্যাপ লিমিট বটম শিট।
- **FocusScheduleScreen:** ফোকাস শিডিউল ক্যালেন্ডার, ডে সিলেক্টর পিলস (Mon-Sun), ডেইলি রুটিন কার্ডস (Deep Work, Sleep Mode) এবং নতুন শিডিউল যোগ করার ফ্রস্টেড মডাল।

### ৬. মডিউল ০৫: অ্যানালিটিক্স ও অ্যাক্টিভিটি হিস্ট্রি (`StatsScreen` & `DetailedActivityLogScreen`)
- **StatsScreen:** কাস্টম ক্যানভাস দিয়ে আঁকা ডুয়াল-বার ট্রেন্ড চার্ট (ব্লকড ফ্রেম বনাম মিউটেড অডিও), ডোনাট ক্যাটাগরি ব্রেকডাউন চার্ট, অফলাইন SQLite প্রাইভেসি ব্যানার এবং টপ ইন্টারসেপ্টেড অ্যাপস র‍্যাঙ্কিং।
- **DetailedActivityLogScreen:** ডেট-গ্রুপড টাইমলাইন ("Today", "Yesterday"), ক্যাটাগরি ফিল্টার চিপস (`All`, `Visual`, `Audio`, `Reels`), ইভেন্ট কার্ডস (নিয়ন পিঙ্ক/সায়ান/পার্পল অরা), ক্লিয়ার কনফার্মেশন অ্যালার্ট ডায়ালগ এবং স্ক্রিন ১৬-এর "All Clean" বায়ো-গ্রিন এম্পটি স্টেট।

### ৭. মডিউল ০৬: সেটিংস, হোয়াইটলিস্ট ও সিকিউরিটি লক (Module 06 Complete)
- **SettingsScreen.kt:**
  - *AI Detection Engine:* মাইক্রোচিপ আইকন, সেনসিটিভিটি লেভেল ৩-পিল সিলেক্টর (`[Strict] [Balanced] [Relaxed]`) এবং পারফরম্যান্স প্রোফাইল ৩-পিল সিলেক্টর (`[Battery Saver (3 FPS)] [Balanced (5 FPS)] [High Precision (8 FPS)]`)।
  - *Security & Protection:* "PIN & Biometric Lock" এবং "Prevent Uninstallation" ট্যাকটাইল `NeonSwitch` টগলস।
  - *App Exclusions:* "Manage App Whitelist (5 apps excluded)" গ্লাস কার্ড উইথ শেভরন।
  - *Data & Storage:* "Clear Offline Database Cache" ক্যাপসুল বাটন ও সাইবার-ডার্ক ডায়ালগ।
  - *About:* v1.0.0, নিয়ন গ্রিন অ্যাক্টিভ স্ট্যাটাস, প্রাইভেসি পলিসি ও ওপেন সোর্স লাইসেন্স।
- **AppWhitelistScreen.kt:**
  - *Top Bar & Search:* ব্যাক বাটন, ২২sp "Exempted Apps" টাইটেল, সার্চ বার উইথ ক্লিয়ার 'X'।
  - *Category Filter Chips:* `[All Apps] [Banking & Finance] [Communication] [Entertainment] [Productivity]` হরাইজন্টাল স্ক্রোল পিলস।
  - *Banking App Emphasis:* ব্যাংকিং ও ফিন্যান্স অ্যাপসমূহ (PayPal, Bank of America, Google Pay, Chase, Revolut) ডিফল্টভাবেই `[ 🔒 BANKING ]` গোল্ডেন ব্যাজ সহ হোয়াইটলিস্টেড (Switch ON) থাকে।
  - *Sticky Bottom Summary Pill:* স্ক্রিনের নিচে পিন করা গ্লোয়িং পিল ("X of 13 Apps Exempted", `ACTIVE` ব্যাজ)।
- **SecurityLockScreen.kt:**
  - *Center Hero:* ৭৬dp ৩ডি গ্লোয়িং শিল্ড প্যাডলক এম্বলম উইথ সাইবার নিয়ন সায়ান ও অরা।
  - *PIN Dots:* ৪টি সাইবার সার্কেল ডট—সংখ্যা টাইপ করলে নিয়ন সায়ান/গ্রিনে ফিল হয়, ভুল পিনে নিয়ন রেড আলো ধারণ করে।
  - *Shake Error Animation:* ভুল পিন দিলে ডটগুলোর ওপর হরিজন্টাল শেক অ্যানিমেশন প্লে হয় এবং এরর মেসেজ প্রদর্শিত হয়।
  - *Custom 3x4 Dial Pad:* ৭২dp সার্কুলার গ্লাস কিপ্যাড বাটনস (লেটার সাবটাইটেলস সহ), বায়োমেট্রিক ফিঙ্গারপ্রিন্ট বাটন এবং ব্যাকস্পেস বাটন। ডিফল্ট টেস্টিং পিন হলো `1234`।

---

## 🔍 ৪. কাজ করতে গিয়ে যেসব সমস্যা দেখা দিয়েছিল এবং যেভাবে সমাধান করা হয়েছে (Problems & Solutions)

যাতে পরবর্তীতে অন্য ডিভাইসে অন্য কোনো ডেভেলপার বা এজেন্ট একই সমস্যায় না পড়ে, তার বিস্তারিত বিবরণ:

1. **সমস্যা ১: প্রাথমিক UI খুব সাধারণ ও ফ্ল্যাট হয়ে যাওয়া**
   - *কারণ:* মডিউল ০১ ও ০২ এর শুরুতে সাধারণ ডার্ক গ্রে কার্ড ও ফ্ল্যাট কালার ব্যবহার করায় `DESIGN.md`-এর Cyber-Dark Glassmorphic প্রিমিয়াম ফিল আসছিল না।
   - *সমাধান:* ব্যবহারকারী স্পষ্টভাবে "Option A: Strict Visual Audit & Refactoring" বেছে নেন। আমরা `GlassmorphicCard.kt`-কে মাল্টি-স্টপ ট্রান্সলুসেন্ট গ্রেডিয়েন্ট এবং ১dp লুমিনাস বর্ডার দিয়ে আপগ্রেড করি, `CyberBackgroundWaves.kt` ব্যাকগ্রাউন্ড যুক্ত করি এবং প্রতিটি বাটনে নিয়ন ড্রপ-শ্যাডো ও অরা ইফেক্ট ইমপ্লিমেন্ট করি।

2. **সমস্যা ২: ফ্লোটিং বটম বার স্ক্রিন পরিবর্তনের সাথে গায়েব হয়ে যাওয়া**
   - *কারণ:* বটম নেভিগেশন বারটি পূর্বে শুধুমাত্র `HomeScreen.kt`-এর ভেতর লোকালভাবে রেন্ডার হতো। ফলে Stats বা Focus স্ক্রিনে গেলে বটম বার আর দেখা যেত না।
   - *সমাধান:* `FloatingBottomNavigationBar.kt` আলাদা কম্পোনেন্ট হিসেবে বের করে `AppNavHost.kt`-এ রুট-লেভেলে `Box(Modifier.align(Alignment.BottomCenter))` হিসেবে স্থাপন করা হয়েছে। `currentBackStackEntryAsState()` দিয়ে ট্র্যাক করে ৪টি মূল স্ক্রিনে বারটি দৃশ্যমান রাখা হয়।

3. **সমস্যা ৩: বটম বার এবং স্ক্রিনের নিচের কন্টেন্টের ওভারল্যাপ (Padding Collision)**
   - *কারণ:* স্ক্রিনগুলো স্ক্রোল করার সময় শেষ কার্ড বা বাটনটি ফ্লোটিং বটম বারের নিচে ঢাকা পড়ে যেত।
   - *সমাধান:* ৪টি মূল স্ক্রিনের (`HomeScreen`, `StatsScreen`, `FocusScheduleScreen`, `SettingsScreen`) স্ক্রোলেবল কন্টেন্টের নিচে `Spacer(modifier = Modifier.height(105.dp..115.dp))` যুক্ত করা হয়েছে যাতে ইউজার একদম শেষ আইটেমটিও বটম বারের অনেক উপরে ফ্রিলি দেখতে ও ট্যাপ করতে পারে।

4. **সমস্যা ৪: ইনার বা সেকেন্ডারি স্ক্রিনে বটম বার দেখানোর ত্রুটি**
   - *কারণ:* `DetailedActivityLog`, `AppWhitelist`, `SecurityLock` ইত্যাদিতে বটম বার রাখা অনুচিত, কারণ এগুলো ডেডিকেটেড টাস্ক স্ক্রিন।
   - *সমাধান:* `AppNavHost.kt`-এ একটি ক্লিন প্রেডিকেট তৈরি করা হয়েছে:
     `val isMainTab = currentRoute in listOf(Screen.Home.route, Screen.Stats.route, Screen.FocusSchedule.route, Screen.Settings.route)`
     এর ফলে অন্য যেকোনো স্ক্রিনে ঢুকলে বটম বারটি অ্যানিমেশনের সাথে নিচে নেমে হাইড হয়ে যায়।

5. **সমস্যা ৫: কম্পাইল ও থিম টোকেন অমিল (Theme Token Mismatch)**
   - *কারণ:* কোডে ভুলবশত `BioGreen` বা `Color` বনাম `Brush` টাইপ মিসম্যাচ হচ্ছিল (যেমন `Modifier.background()`-এ if-else ব্রাঞ্চে দুটি আলাদা রিটার্ন টাইপ ছিল)।
   - *সমাধান:* `Color.kt`-তে রেজিস্টার্ড অফিশিয়াল কালার `NeonGreen` (`Color(0xFF00FF9D)`) ব্যবহার করা হয়েছে এবং ব্যাকগ্রাউন্ডে `SolidColor(...)` ব্যবহার করে টাইপ সেফটি নিশ্চিত করা হয়েছে।

6. **সমস্যা ৬: কাস্টম কিপ্যাড ও ডায়ালগ বাটন এমুলেটর অফসেট**
   - *কারণ:* এমুলেটরে অটোমেটেড টেস্টিংয়ের সময় স্ট্যাটাস বার অফসেটের কারণে কিছু টাচ ইভেন্ট মিস হচ্ছিল।
   - *সমাধান:* পাইথন পিআইএল এবং অ্যান্ড্রয়েড উইন্ডোডাম্প (`uiautomator dump`) দিয়ে এক্স্যাক্ট সেন্ট্রয়েড বাউন্ডিং বক্স কোঅর্ডিনেট ক্যালকুলেট করে নিখুঁতভাবে অটোমেশন ভেরিফাই করা হয়েছে।

---

## 🚀 ৫. পরবর্তী কার্যক্রম ও সিকোয়েন্স (Next Steps for the Next Agent / Device)

বাসায় গিয়ে অন্য ডিভাইসে কাজ শুরু করলে আপনার **প্রথম ও সরাসরি কাজ হবে Module 07 Prompt 2 শুরু করা**।

### ১. পরবর্তী টাস্কের বিস্তারিত বিবরণ (Next Task Details):
* **পর্যায়:** **Phase 2B — Module 07: System Overlays Flow (Prompt 2)**
* **টাস্ক:** **Prompt 2 (`FocusInterventionOverlay.kt` — Fullscreen Focus Intervention & Doomscroll Breaker Overlay)**
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/services/overlay/FocusInterventionOverlay.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/07_Overlays_And_Dialogs/SafeScreen Protection and Focus .png` (ডান পাশের স্ক্রিন — Cropped ref: `focus_overlay_right_ref.jpg`)
* **প্রম্পট গাইড:** `Screen Workflow/MASTER_UI_PROMPTS.md` ফাইলের **প্রম্পট ১৫** (Screen 15: Fullscreen Focus Intervention Overlay)

### ২. কী কী তৈরি করতে হবে (Requirements for Module 07 Prompt 2):
1. **Fullscreen Lockout Scrim:**
   - অ্যান্ড্রয়েড ওএস উইন্ডো টাইপ: `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`
   - ফ্ল্যাগস: ফুলস্ক্রিন ইনপুট ইন্টারসেপশন যাতে লকআউটের সময় পেছনের রিলস/শর্টস অ্যাপে কোনো টাচ কাজ না করে।
2. **ভায়োলেশন ও মোটিভেশনাল হেডার:**
   - "Time to Breathe" / "Daily Limit Reached (30m)"
   - বড় ৩ডি আওয়ারগ্লাস বা ব্রিদিং লোটাস/শিল্ড আইকন উইথ নিয়ন পার্পল অরা গ্লো।
3. **ইন্টারভেনশন কন্ট্রোলস:**
   - "Take a 5-Min Walk" / "Close App" প্রাইমারি অ্যাকশন বাটন।
   - "Bypass with Emergency PIN" বা "Snooze (1m)" সেকেন্ডারি চয়েস।

### ৩. এর পরবর্তী ধাপসমূহ (Subsequent Roadmap):
* **Module 08:** `SafeScreenStates.kt` (Screen 16 — এম্পটি হিস্ট্রি স্টেট, শিল্ড পজড স্টেট ও এআই কোর এরর স্টেট)।
* **Phase 3:** Room Database (`AppDatabase.kt`, `ThreatLogDao.kt`, `WhitelistDao.kt`) এবং DataStore Preferences ইন্টিগ্রেশন।
* **Phase 4:** Android OS Permissions Wiring (MediaProjection, Audio, AccessibilityService, Battery Whitelist) ও ফোরগ্রাউন্ড সার্ভিস।
* **Phase 5:** On-Device AI Pipeline (TFLite NSFW Detection & YAMNet Audio Classifier) ইন্টিগ্রেশন।

---

## 💻 ৬. ডেভেলপার রান ও ভেরিফিকেশন গাইড (Developer Cheat Sheet)

অন্য ডিভাইসে অ্যাপটি ক্লোন করার পর কীভাবে রান ও টেস্ট করবেন:

```bash
# ১. রিপোজিটরি ক্লোন বা লেটেস্ট পুল
git clone https://github.com/saidur1004/SafeScreen.git
cd SafeScreen
git checkout main
git pull origin main

# ২. সম্পূর্ণ ক্লিন ও ডিবাগ বিল্ড তৈরি
./gradlew clean assembleDebug

# ৩. এমুলেটরে ইনস্টল (ডিভাইস আইডি: emulator-5554 বা adb devices থেকে প্রাপ্ত আইডি)
adb install -r app/build/outputs/apk/debug/app-debug.apk

# ৪. অ্যাপ চালু করা
adb shell am start -n com.safescreen.app/.MainActivity

# ৫. ডিফল্ট টেস্টিং ক্রেডেনশিয়ালস:
# - Security PIN: 1234
# - Default Exempted Apps: PayPal, Bank of America, Google Pay, Chase, Revolut (5 apps)
```

---

## 📂 ৭. প্রজেক্ট ফাইল স্ট্রাকচার রেফারেন্স (Key Files Map)

```text
SafeScreen/
├── app/src/main/java/com/safescreen/app/
│   ├── MainActivity.kt                         # রুট অ্যাক্টিভিটি
│   ├── SafeScreenApp.kt                        # Application ক্লাস (@HiltAndroidApp)
│   ├── di/                                     # Dagger-Hilt ডিপেনডেন্সি ইনজেকশন মডিউলস
│   ├── presentation/
│   │   ├── components/                         # পুনর্ব্যবহারযোগ্য সাইবার-ডার্ক কম্পোনেন্টস
│   │   │   ├── CyberBackgroundWaves.kt         # ডাইনামিক অ্যাম্বিয়েন্ট ব্যাকগ্রাউন্ড
│   │   │   ├── FloatingBottomNavigationBar.kt  # গ্লোবাল ৪-ট্যাব ফ্লোটিং বটম বার
│   │   │   ├── GlassmorphicCard.kt             # প্রিমিয়াম ফ্রস্টেড গ্লাস কার্ড
│   │   │   ├── NeonSwitch.kt                   # ট্যাকটাইল সাইবার নিয়ন টগল সুইচ
│   │   │   ├── ShieldButton.kt                 # ৩ডি ইন্টারঅ্যাক্টিভ শিল্ড বাটন
│   │   │   └── TimeLimitSlider.kt              # কাস্টম নিয়ন রেঞ্জ স্লাইডার
│   │   ├── navigation/
│   │   │   ├── Screen.kt                       # ১৬টি স্ক্রিনের সম্পূর্ণ সিল্ড ক্লাস রুটস
│   │   │   └── AppNavHost.kt                   # গ্লোবাল নেভিগেশন হোস্ট ও বটম বার হ্যান্ডলার
│   │   ├── screens/
│   │   │   ├── splash/SplashScreen.kt          # মডিউল ০১: স্প্ল্যাশ স্ক্রিন
│   │   │   ├── onboarding/OnboardingScreen.kt  # মডিউল ০১: অনবোর্ডিং স্ক্রিন
│   │   │   ├── permission/                     # মডিউল ০২: পারমিশন হাব ও ব্যাটারি শিট
│   │   │   ├── home/                           # মডিউল ০৩: হোম ড্যাশবোর্ড ও ব্লার শিট
│   │   │   ├── focus/                          # মডিউল ০৪: রিলস কন্ট্রোল ও ফোকাস শিডিউল
│   │   │   ├── stats/                          # মডিউল ০৫: স্ট্যাটস চার্টস ও অ্যাক্টিভিটি লগ
│   │   │   └── settings/                       # মডিউল ০৬: সেটিংস, হোয়াইটলিস্ট ও সিকিউরিটি লক
│   │   │       ├── SettingsScreen.kt           # স্ক্রিন ১১: মেইন সেটিংস ও এআই টিউন
│   │   │       ├── SettingsUiState.kt          # সেটিংস স্টেট ও ইভেন্টস
│   │   │       ├── AppWhitelistScreen.kt       # স্ক্রিন ১২: এক্সিম্পটেড অ্যাপস তালিকা ও সার্চ
│   │   │       ├── WhitelistUiState.kt         # হোয়াইটলিস্ট স্টেট ও ইভেন্টস
│   │   │       ├── SecurityLockScreen.kt       # স্ক্রিন ১৩: পিন ডায়াল প্যাড ও বায়োমেট্রিক
│   │   │       └── SecurityUiState.kt          # সিকিউরিটি স্টেট ও ইভেন্টস
│   │   └── theme/                              # থিম, টাইপোগ্রাফি ও কালার টোকেনস
│   └── services/                               # ব্যাকগ্রাউন্ড সার্ভিস ও ওভারলে ভিউজ
│       └── overlay/                            # [মডিউল ০৭ এর জন্য প্রস্তুত ডিরেক্টরি]
├── Screen Workflow/                            # ১৬টি স্ক্রিনের রেফারেন্স মকআপস ও প্রম্পট বুক
│   ├── MASTER_UI_PROMPTS.md                    # সেলফ-কন্টেইন্ড প্রম্পট বুক
│   ├── 06_Settings_And_Security/               # সেটিংস ও সিকিউরিটি রেফারেন্স
│   └── 07_Overlays_And_Dialogs/                # সিস্টেম ওভারলে রেফারেন্স মকআপস
├── DESIGN.md                                   # ডিজাইন সিস্টেম স্পেসিফিকেশন
└── PROJECT_PROGRESS.md                         # বর্তমান হ্যান্ডওভার ও প্রগ্রেস রিপোর্ট
```

---

*রিপোর্ট প্রস্তুতকারক: Antigravity AI Pair Programmer*  
*সর্বশেষ কমিট হ্যাশ:* `78a5d75` (এবং পরবর্তী সিঙ্ক)  
*রিপোজিটরি ব্রাঞ্চ:* `main` (সম্পূর্ণ ক্লিন ও পুশড)
