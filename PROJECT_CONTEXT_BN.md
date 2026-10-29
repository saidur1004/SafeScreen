# SafeScreen — Master Project Context & Blueprint

**প্রজেক্টের নাম:** SafeScreen  
**ট্যাগলাইন:** Your Digital Purity & Focus Guard  
**প্রজেক্টের ধরন:** Privacy-First Digital Wellbeing, Content Filtering & Screen-Time Management  
**প্রাথমিক প্ল্যাটফর্ম:** Android Application (Kotlin, Jetpack Compose, Clean Architecture)  
**দ্বিতীয় প্ল্যাটফর্ম:** Google Chrome Extension (Manifest V3, DOM MutationObserver)  
**প্রধান দর্শন:** Privacy First, Zero Cloud, 100% Offline AI, Digital Wellbeing  

---

## ১. প্রজেক্টের পরিচিতি ও মিশন

**SafeScreen** হলো একটি আধুনিক, গোপনীয়তা-বান্ধব (Privacy-First) ডিজিটাল ওয়েলবিয়িং প্ল্যাটফর্ম। এর উদ্দেশ্য হলো ব্যবহারকারীদের অনাকাঙ্ক্ষিত ভিজ্যুয়াল কনটেন্ট, বিভ্রান্তিকর শর্ট-ভিডিও ফিড এবং অতিরিক্ত স্ক্রলিং আসক্তি থেকে সুরক্ষিত রাখা। 

ব্যক্তিগত, পারিবারিক, নৈতিক বা মনোযোগ-সংক্রান্ত কারণে ব্যবহারকারী যাতে তার ডিজিটাল অভিজ্ঞতাকে নিজের নিয়ন্ত্রণে রাখতে পারেন—সেই উদ্দেশ্যে এটি তৈরি। কোনো প্রকার ক্লাউড সার্ভার, এক্সটার্নাল ট্র্যাকার বা ডেটা হার্ভেস্টিং ছাড়া সম্পূর্ণ অন-ডিভাইস প্রসেসিংয়ের মাধ্যমে SafeScreen ব্যবহারকারীর পূর্ণ গোপনীয়তা নিশ্চিত করে।

### ১.১ প্রধান তিনটি স্তম্ভ (Core Pillars)
১. **Visual Content Protection:** অনাকাঙ্ক্ষিত নগ্ন বা প্রাপ্তবয়স্ক (NSFW) ছবি ও ভিডিও রিয়েল-টাইমে শনাক্ত করে ব্লার করা।  
২. **Audio Protection:** ব্যাকগ্রাউন্ড মিউজিক বা নির্বাচিত সাউন্ড ট্র্যাক অন-ডিভাইস AI দিয়ে শনাক্ত করে স্বয়ংক্রিয়ভাবে অডিও মিউট/নিয়ন্ত্রণ করা।  
৩. **Reels & Digital Wellbeing:** Facebook Reels, Instagram Reels, YouTube Shorts ও TikTok-এর মতো ইনফিনিট স্ক্রলিং প্ল্যাটফর্মে আসক্তি প্রতিরোধ করা।

---

## ২. মূল নীতিমালা (Core Architectural Principles)

* **Privacy by Design:** কোনো স্ক্রিনশট, অডিও ক্লিপ, ফ্রেম বা ব্যক্তিগত ডেটা ক্লাউডে পাঠানো হবে না। কোনো ইন্টারনেট পারমিশনের ওপর ব্যাকগ্রাউন্ড সার্ভিস নির্ভরশীল নয়।
* **100% Offline-First AI:** সমস্ত কম্পিউটার ভিশন ও অডিও ক্লাসিফিকেশন প্রসেসিং ডিভাইসের ভেতরে TensorFlow Lite (কোয়ান্টাইজড INT8) মডেলে সম্পন্ন হবে।
* **Minimal Permissions & Transparency:** ব্যবহারকারীর কাছ থেকে শুধু অত্যাবশ্যকীয় ওএস পারমিশন নেওয়া হবে এবং প্রতিটির কারণ স্বচ্ছভাবে ব্যাখ্যা করা থাকবে।
* **Clean Architecture & Separation of Concerns:** কোডবেস সবসময় Data, Domain এবং Presentation স্তরে বিভক্ত থাকবে।
* **Battery & Performance Budget:** রিয়েল-টাইম ফ্রেম স্যাম্পলিং ৩-৫ FPS এ সীমাবদ্ধ রেখে ব্যাটারি ড্রেন প্রতি ঘণ্টায় সর্বোচ্চ ৩-৪% এর মধ্যে রাখা হবে।
* **Honest Communication About Technical Limitations:** কোনো অতিরঞ্জিত বা অবাস্তব দাবি করা হবে না। ওএস সীমাবদ্ধতা ব্যবহারকারীর কাছে খোলামেলা তুলে ধরা হবে।

---

## ৩. প্রধান ফিচার মডিউলসমূহ

### ৩.১ Visual Content Protection Module
* **প্রযুক্তি:** `MediaProjection` API (Android 10+) + TensorFlow Lite Computer Vision Model + `WindowManager` Overlay (`TYPE_APPLICATION_OVERLAY`)।
* **কাজের ধারা:** স্ক্রিনের ডিসপ্লে ফ্রেম ব্যাকগ্রাউন্ডে ক্যাপচার করে অপ্টিমাইজড রেজোলিউশনে রিসাইজ করা হয়। TFLite মডেল যদি ফ্রেমটিতে NSFW কনটেন্ট শনাক্ত করে (কনফিডেন্স থ্রেশহোল্ড অতিক্রম করে), তবে তাৎক্ষণিকভাবে স্ক্রিনের ওপর স্মুথ ব্লার ওভারলে প্রয়োগ করা হয়। কনটেন্ট সরে গেলে ব্লার স্বয়ংক্রিয়ভাবে উঠে যায়।
* **সীমাবদ্ধতা:** ব্যাংকিং বা নেটফ্লিক্সের মতো `FLAG_SECURE` উইন্ডো ওএস লেভেলে ব্ল্যাক স্ক্রিন হিসেবে আসবে, যা অ্যান্ড্রয়েডের নিজস্ব নিরাপত্তা নীতি।

### ৩.২ Audio Protection Module
* **প্রযুক্তি:** `AudioPlaybackCaptureConfiguration` (Android 10+) + Audio TFLite Classifier (YAMNet) + `AudioManager`।
* **কাজের ধারা:** মাইক্রোফোনের বদলে ডিভাইসের ইন্টারনাল মিডিয়া অডিও স্ট্রিম ক্যাপচার করা হয়। অডিওতে মিউজিক বা নির্দিষ্ট সাউন্ড প্যাটার্ন শনাক্ত হলে `AudioManager.requestAudioFocus` অথবা অডিও স্ট্রিম মিউটের মাধ্যমে সাউন্ড সাপ্রেস করা হয়।
* **সীমাবদ্ধতা:** যেসব অ্যাপ অডিও শেয়ারিং বন্ধ রাখে (`ALLOW_CAPTURE_BY_NONE`) তাদের অডিও ক্যাপচার করা সম্ভব নয়।

---

## ৪. Reels & Digital Wellbeing Module (Android Limitations & Feasible Solution)

### ৪.১ প্রাথমিক পরিকল্পনা ও সীমাবদ্ধতা
* **প্রাথমিক পরিকল্পনা ছিল:** Android অ্যাপে Facebook, YouTube বা TikTok-এর ভেতর থেকে নির্দিষ্ট Reels বা Shorts ফিডটি CSS/DOM ম্যানিপুলেশনের মতো হাইড (Hide) বা রিমুভ করে দেওয়া অথবা ব্যবহারকারী ১০টি রিলস দেখলে স্বয়ংক্রিয়ভাবে রিলস কাউন্ট করে বন্ধ করে দেওয়া।
* **বাস্তবতা (কেন এটি প্রায় অসম্ভব):** Android অপারেটিং সিস্টেমের কঠোর **Sandboxing** নীতির কারণে একটি অ্যাপ অন্য কোনো অ্যাপের ইউআই (UI) সরাসরি পরিবর্তন বা মুছে ফেলতে পারে না। ব্রাউজারের মতো এখানে কোড বা স্টাইল ইনজেক্ট করা যায় না। এছাড়া Meta বা Google তাদের অ্যাপের UI Node-গুলো প্রতিনিয়ত ডাইনামিকভাবে পরিবর্তন ও অবফুসকেট (Obfuscation) করে। তাই অন্য অ্যাপের ভেতর থেকে নির্দিষ্ট একটি বাটন বা ফিড নিখুঁতভাবে গায়েব করা কিংবা ৩য় পক্ষের অ্যাপের ভেতর নিখুঁতভাবে ১০টি ভিডিও গণনা করা অ্যান্ড্রয়েডে অবাস্তব, অনির্ভরযোগ্য এবং ভঙ্গুর।

### ৪.২ বাস্তবসম্মত বিকল্প ও কার্যকর সমাধান (App Implementation)
UI রিমুভ করার অবাস্তব চেষ্টার পরিবর্তে, Android অ্যাপে নিচের দুটি নির্ভরযোগ্য পদ্ধতি ব্যবহার করে ব্যবহারকারীকে শর্ট-ভিডিওর আসক্তি থেকে মুক্ত করা হবে:

১. **App-Level Time Limit (`UsageStatsManager`):**  
পুরো অ্যাপের জন্য নির্দিষ্ট সময়সীমা নির্ধারণ করা। যেমন, ব্যবহারকারী দিনে সর্বোচ্চ ৩০ মিনিট TikTok বা Instagram ব্যবহার করতে পারবেন। `UsageStatsManager` API ব্যবহার করে এটি ওএস লেভেলে নিখুঁতভাবে ট্র্যাক করা হবে। সময়সীমা শেষ হলে অ্যাপটি ব্যবহারের ওপর পূর্ণ বা আংশিক ব্লক আরোপ করা হবে।

২. **Accessibility Blocker / Overlay (`AccessibilityService` + `WindowManager`):**  
ব্যবহারকারী যখন Facebook বা YouTube-এ থাকবেন, তখন `AccessibilityService` ব্যাকগ্রাউন্ডে স্ক্রিনের টেক্সট বা অ্যাক্সেসিবিলিটি নোড মনিটর করবে। ইউজার যদি নির্দিষ্ট "Shorts" বা "Reels" সেকশনে প্রবেশ করেন, তবে সিস্টেম তা শনাক্ত করে সাথে সাথে `System Alert Window` (`TYPE_APPLICATION_OVERLAY`) ব্যবহার করে স্ক্রিনের ওপর একটি নিশ্ছিদ্র ব্লকার বা সতর্কবার্তা (যেমন: *"Focus Time Active / Reels Blocked"*) ভাসিয়ে দেবে।  
* **ফলাফল:** রিলস স্ক্রিন গায়েব হবে না ঠিকই, কিন্তু ব্যবহারকারী রিলস দেখতেও পারবেন না।

*(নোট: Chrome Extension-এর ক্ষেত্রে DOM Manipulation ব্যবহার করে সরাসরি Reels/Shorts হাইড বা ব্লক করার প্রাথমিক পরিকল্পনাটিই বহাল থাকবে, কারণ ব্রাউজারে এটি ১০০% সম্ভব।)*

### ৪.৩ ডিজিটাল ওয়েলবিয়িং সেটিংস
* **Daily Time Limit:** অ্যাপ ও রিলস ফিডে দৈনিক সর্বোচ্চ সময়সীমা।
* **Focus Schedule:** পড়াশোনা বা কাজের নির্দিষ্ট সময়ে শর্ট-ভিডিও অ্যাপ সম্পূর্ণ নিস্ক্রিয় রাখা।
* **Break Reminder:** একটানা ব্যবহারের পর বিরতি নেওয়ার নির্দেশিকা।
* **Usage Summary:** কত মিনিট সময় বাঁচানো হয়েছে তার অফলাইন গ্রাফ।

---

## ৫. সাপোর্টিং মডিউলসমূহ

### ৫.১ App Whitelist
* ব্যবহারকারী নির্দিষ্ট প্রয়োজনীয় অ্যাপকে (যেমন ব্যাংকিং, গুগল মিট, ক্যালকুলেটর) প্রোটেকশন শিল্ডের আওতামুক্ত রাখতে পারবেন।

### ৫.২ Security & Lock
* সুরক্ষার সেটিংস বা শিল্ড আনআর্ম করা প্রতিরোধ করতে PIN এবং `BiometricPrompt` লক থাকবে।

### ৫.৩ Local Offline Analytics
* ফিল্টার হওয়া কন্টেন্টের সমস্ত হিস্ট্রি শুধুমাত্র ডিভাইসের লোকাল Room SQLite ডেটাবেসে এনক্রিপ্ট আকারে থাকবে। ব্যবহারকারী চাইলে যেকোনো সময় তা এক ক্লিকে মুছে দিতে পারবেন।

---

## ৬. প্রযুক্তিগত স্ট্যাক (Technology Stack)

### Android Architecture
* **Language & SDK:** Kotlin 2.0.21, Min SDK 29, Target SDK 35, Java 17
* **UI:** Jetpack Compose, Material 3, Cyber-Dark Palette (`#07090E`, Neon Cyan `#00E5FF`, Neon Green `#00FF9D`)
* **Architecture Pattern:** Clean Architecture + MVVM + MVI StateFlow
* **Dependency Injection:** Dagger Hilt 2.52 via KSP
* **Local Storage:** Room Database 2.6.1 + DataStore Preferences
* **Machine Learning:** TensorFlow Lite 2.16.1 & TFLite Support Library
* **System Services:** Foreground Service (`mediaProjection`), AccessibilityService, WindowManager Overlay

### Chrome Extension (Phase 8)
* Manifest V3, WebExtensions API, DOM MutationObserver, CSS Injection, Local Storage

---

## ৭. ডেভেলপার ও এআই এজেন্টের জন্য নির্দেশনা (Guidelines for AI Agents)
* কোড লেখার সময় সবসময় এই ফাইলটিকে কেন্দ্রীয় রেফারেন্স হিসেবে পড়তে হবে।
* কোনো ফিচার অবাস্তবভাবে দাবি করা যাবে না (যেমন ৩য় পক্ষের অ্যাপের ভেতর গিয়ে বোতাম ডিলিট করা)।
* প্রতিবার কাজ শেষে অবশ্যই `PROJECT_PROGRESS.md` আপডেট করতে হবে যাতে যেকোনো ডিভাইসে অন্য যেকোনো এজেন্ট পরবর্তী ধাপ সহজে শুরু করতে পারে।
