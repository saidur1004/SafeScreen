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
* **তারিখ:** ৩১ অক্টোবর ২০২৬
* **বর্তমান পর্যায়:** **UI/UX Design-First Workflow & Prompt Engine Prepared (Stage 2A Complete ✅)**
* **চলমান কাজ:** ব্যবহারকারী ChatGPT-এর মাধ্যমে `Screen Workflow/` ফোল্ডারে ১৬টি স্ক্রিনের রেফারেন্স ডিজাইন ইমেজ জেনারেট ও সেভ করছেন।
* **পরবর্তী কোডিং পর্যায়:** Phase 2B (ইমেজগুলোর রেফারেন্স ধরে Kotlin + Jetpack Compose-এ পিক্সেল-পারফেক্ট UI স্ক্রিন তৈরি)।
* **GitHub Repository:** `https://github.com/saidur1004/SafeScreen.git`
* **বিল্ড স্ট্যাটাস:** ✅ Gradle Compile & Build Clean (`./gradlew assembleDebug` - 0 errors, 41 tasks executed)

---

## ১. আজ পর্যন্ত যা যা কাজ সফলভাবে সম্পন্ন হয়েছে (Completed Milestones)

### ক. প্রজেক্ট ইনিশিয়ালাইজেশন ও আর্কিটেকচার সেটআপ (Phase 1)
1. **প্রজেক্ট ক্রিয়েশন:** `/Users/betopia/StudioProjects/Projects_Another/SafeScreen` ডিরেক্টরিতে `com.safescreen.app` প্যাকেজ আইডিসহ সম্পূর্ণ অ্যান্ড্রয়েড Kotlin KTS প্রজেক্ট ইনিশিয়ালাইজ করা হয়েছে।
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
4. **Clean Architecture ফোল্ডার ও বয়লারপ্লেট:** `di`, `data` (local, repository, tflite), `domain` (model, repository, usecase), `presentation` (theme, components, navigation, screens), ও `services` (Foreground Service) রেডি।

---

### খ. UI/UX স্ক্রিন ডিজাইন ও প্রম্পট ইঞ্জিন প্রস্তুতকরণ (Phase 2A - ৩১ অক্টোবর ২০২৬)
ব্যবহারকারীর কৌশলগত পছন্দ অনুযায়ী (Design-First / UI-First Strategy) রুট ডিরেক্টরিতে একটি সম্পূর্ণ স্ক্রিন ডিজাইন ওয়ার্কফ্লো তৈরি করা হয়েছে:

1. **ডিরেক্টরি ও ফোল্ডার তৈরি:** `Screen Workflow/` নামে রুট ফোল্ডার তৈরি করা হয়েছে এবং এর অধীনে ৮টি ক্যাটাগরি ফোল্ডার সেটআপ করা হয়েছে:
   - `01_Onboarding/`
   - `02_Permissions/`
   - `03_Home_Dashboard/`
   - `04_Reels_And_Focus/`
   - `05_Stats_And_Analytics/`
   - `06_Settings_And_Security/`
   - `07_Overlays_And_Dialogs/`
   - `08_Error_And_Empty_States/`
2. **মাস্টার প্রম্পট বুক (`Screen Workflow/MASTER_UI_PROMPTS.md`):**
   - **ChatGPT Master Context Initialization Prompt:** এআই ডিজাইনারের জন্য পূর্ণাঙ্গ কনটেক্সট, ভিশন, কালার টোকেন (Cyber-Dark Deep Black `#07090E`, Neon Cyan `#00E5FF`, Neon Green `#00FF9D`, Neon Pink `#FF2A6D`) এবং গ্লাস মরফিজম গাইডলাইন।
   - **১৬টি স্ক্রিনের সুনির্দিষ্ট জোড়া প্রম্পট:**
     - **প্রম্পট A:** ChatGPT দিয়ে পিক্সেল-পারফেক্ট মোবাইল UI ইমেজ তৈরির জন্য প্রম্পট।
     - **প্রম্পট B:** পরবর্তীতে কোডিং এজেন্ট দ্বারা সংরক্ষিত ইমেজ দেখে Clean Architecture ও Jetpack Compose-এ ইমপ্লিমেন্ট করার নির্দেশিকা।
3. **ওয়ার্কফ্লো ট্র্যাকার (`Screen Workflow/README.md`):**
   - ফোল্ডার ম্যাপিং, Mermaid নেভিগেশন ম্যাপ এবং প্রতিটি স্ক্রিনের ইমেজ ও কোডিং স্ট্যাটাস ট্র্যাক করার চেকলিস্ট।

---

## ২. রিলস ও শর্ট-ভিডিও বিষয়ে গৃহীত প্রযুক্তিগত সিদ্ধান্ত

* **৩য় পক্ষের অ্যাপে UI মোছা বা অবাস্তব কাউন্টিং বাদ:** অ্যান্ড্রয়েডের কঠোর "Sandboxing" এবং Meta/Google-এর "Dynamic Obfuscation"-এর কারণে ৩য় পক্ষের অ্যাপের ভেতর থেকে বাটন ডিলিট করা কিংবা অবফুসকেটেড অ্যাপে নিখুঁতভাবে ১০টি ভিডিও গোনা অসম্ভব ও ভঙ্গুর।
* **গৃহীত বাস্তবসম্মত সমাধান:**
  1. `UsageStatsManager` ব্যবহার করে পুরো অ্যাপের জন্য দৈনিক নির্দিষ্ট সময়সীমা নির্ধারণ (যেমন ৩০ মিনিট পর স্বয়ংক্রিয় অ্যাপ লক)।
  2. `AccessibilityService` দিয়ে ব্যবহারকারী রিলস/শর্টস সেকশনে ঢুকলে সাথে সাথে `WindowManager` এর মাধ্যমে একটি নিশ্ছিদ্র ব্লকার ওভারলে ভাসিয়ে দেওয়া (`Focus Time Active` কার্ড)।
  3. Chrome Extension-এ DOM Manipulation বহাল থাকবে।

---

## ৩. পরবর্তী এজেন্টের জন্য সুনির্দিষ্ট কাজের সিকোয়েন্স (Next Action Items for AI / Dev)

যেকোনো নতুন এজেন্ট বা ডেভেলপার এই প্রজেক্ট হাতে নেওয়ার পর নিচের সিকোয়েন্স অনুযায়ী কাজ করবেন:

### বর্তমান অবস্থান:
* ব্যবহারকারী ChatGPT-তে `Screen Workflow/MASTER_UI_PROMPTS.md` ব্যবহার করে স্ক্রিনগুলোর ইমেজ জেনারেট করছেন এবং `Screen Workflow/` এর সংশ্লিষ্ট সাবফোল্ডারে ইমেজগুলো সংরক্ষণ করছেন।

### পরবর্তী ধাপ (যখন ব্যবহারকারী কোড ইমপ্লিমেন্ট করতে বলবেন):
1. **রেফারেন্স ইমেজ যাচাই:** `Screen Workflow/` ফোল্ডারের ইমেজগুলো দেখুন এবং সংশ্লিষ্ট স্ক্রিনের জন্য `MASTER_UI_PROMPTS.md` ফাইলের **প্রম্পট B** পড়ুন।
2. **UI কোডিং (Jetpack Compose):**
   - `presentation/screens/` এর ভেতরে প্রতিটি স্ক্রিনের কম্পোজ কোড লিখুন।
   - **State Hoisting** বজায় রাখুন (`UiState` ডেটা ক্লাস ও ইভেন্ট ল্যাম্বডা ব্যবহার করুন)।
   - প্রতিটি স্ক্রিনে ডামি/মক ডেটা দিয়ে `@Preview` তৈরি করুন যাতে অ্যান্ড্রয়েড স্টুডিওতে সরাসরি প্রিভিউ দেখা যায়।
   - লাইট ও ডার্ক মোডের জন্য কালার টোকেন সামঞ্জস্যপূর্ণ রাখুন।
3. **ফিচার ও ব্যাকএন্ড সংযোগ (পরবর্তী ধাপসমূহ):**
   - স্ক্রিন ডিজাইন শেষ হলে Phase 3-তে TFLite মডেল ইন্টিগ্রেশন এবং Phase 4-তে MediaProjection ও AccessibilityService সংযোগ করা হবে।
4. **সেশন শেষে এই ফাইল আপডেট ও গিট পুশ:**
   - প্রতিটি কাজের শেষে `PROJECT_PROGRESS.md` আপডেট করতে হবে এবং গিট পুশ করতে হবে।

---

## ৪. গিট ও রিপোজিটোরি স্ট্যাটাস (Git Status)
* **Remote Repository URL:** `https://github.com/saidur1004/SafeScreen.git`
* **Default Branch:** `main`
* **Commit History:** সমস্ত প্রাথমিক সোর্স কোড, বিল্ড কনফিগারেশন, মাস্টার ডকুমেন্টেশন এবং স্ক্রিন ওয়ার্কফ্লো প্রম্পট ডিরেক্টরি মেইন ব্রাঞ্চে সিঙ্ক করা হয়েছে।
