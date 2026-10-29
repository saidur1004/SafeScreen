# SafeScreen — ফিচার মেট্রিক্স ও বাস্তবায়ন স্ট্যাটাস (Feature Matrix)

নিচে SafeScreen অ্যাপ্লিকেশনের সমস্ত ফিচারের তালিকা, টেকনিক্যাল নির্ভরতা, প্ল্যাটফর্ম সক্ষমতা ও বর্তমান বাস্তবায়ন অবস্থা লিপিবদ্ধ করা হলো।

---

| ফিচার ক্যাটাগরি | ফিচারের নাম | প্ল্যাটফর্ম | নির্ভরতা (Tech Dependencies) | জটিলতা | রিস্ক ফ্যাক্টর | বর্তমান স্ট্যাটাস |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Visual Shield** | রিয়েল-টাইম স্ক্রিন ব্লার | Android | MediaProjection, TFLite, WindowManager Overlay | High | FLAG_SECURE ব্ল্যাক-আউট, ফ্রেম ড্রপ | Architecture Ready |
| **Visual Shield** | ওয়েব ইমেজ/ভিডিও ফিল্টারিং | Chrome Ext | DOM MutationObserver, CSS Filter, Canvas | Medium | ডাইনামিক SPA কন্টেন্ট লোডিং | Planned (Phase 8) |
| **Audio Protection** | অন-ডিভাইস মিউজিক ডিটেকশন | Android | AudioPlaybackCapture, YAMNet/TFLite, AudioRecord | High | থার্ড পার্টি অ্যাপ অডিও শেয়ারিং রেস্ট্রিকশন | Configured in Manifest |
| **Audio Protection** | ব্রাউজার অডিও অটো-মিউট | Chrome Ext | Web Audio API, HTMLMediaElement.muted | Low | অটোপ্লে পলিসি | Planned (Phase 8) |
| **Reels & Shorts** | রিলস/শর্টস ডিটেকশন | Android | AccessibilityService, UI Node Analysis | High | সোশ্যাল অ্যাপের ক্লাসনেম/আইডি পরিবর্তন | Researched (Doc Ready) |
| **Reels & Shorts** | দৈনিক সময়সীমা ও হস্তক্ষেপ | Android | AccessibilityService, UsageStatsManager | Medium | ব্যবহারকারীর অসন্তোষ / ফ্লিকারিং | Planned (Phase 5) |
| **Reels & Shorts** | ওয়েব শর্টস হাইড করা | Chrome Ext | CSS Injection, URL Matching, DOM Removal | Low | ইউটিউব/ইনস্টাগ্রাম লেআউট পরিবর্তন | Planned (Phase 8) |
| **Usage Limits** | দৈনিক স্ক্রিনটাইম ও সেশন সীমা | Android | AlarmManager, Foreground Service, DataStore | Medium | ব্যাটারি অপটিমাইজেশন কিলিং | Planned (Phase 6) |
| **App Whitelist** | অ্যাপ বা ওয়েবসাইট ছাড় দেওয়া | Android / Ext | PackageManager, Room Database, Host Matching | Low | কোনো রিস্ক নেই | Boilerplate Ready |
| **Security** | PIN ও বায়োমেট্রিক লক | Android | Androidx BiometricPrompt, EncryptedSharedPreferences | Medium | হার্ডওয়্যার নির্ভরতা | Planned (Phase 7) |
| **Analytics** | অফলাইন পরিসংখ্যান ও গ্রাফ | Android | Room Database, Coroutines Flow, Compose Canvas | Medium | ডেটাবেস সাইজ বৃদ্ধি | Architecture Ready |

---

## 🔍 জটিলতা ও ঝুঁকি স্কেল (Complexity & Risk Rating)
- **High Complexity:** ওএস লেভেলের স্পর্শকাতর এপিআই (MediaProjection, AudioPlaybackCapture, Accessibility) ও রিয়েল-টাইম নিউরাল নেটওয়ার্ক ব্যবহারের কারণে ব্যাটারি ও লেটেন্সি ব্যালেন্স করা অত্যন্ত চ্যালেঞ্জিং।
- **Medium Complexity:** ডেটাবেস সিঙ্কিং, নোটিফিকেশন সিস্টেম, ব্যাকগ্রাউন্ড প্রসেস দীর্ঘস্থায়ী রাখা।
- **Low Complexity:** UI উপাদান, স্থানীয় সেটিংস ও হোয়াইটলিস্ট স্টোরেজ।

