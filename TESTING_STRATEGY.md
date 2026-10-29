# SafeScreen — টেস্টিং ও ভ্যালিডেশন স্ট্র্যাটেজি (Testing Strategy)

SafeScreen ব্যাকগ্রাউন্ড সার্ভিস, রিয়েল-টাইম স্ক্রিন ক্যাপচার ও মেশিন লার্নিং ইনফারেন্সের মতো জটিল উপাদান নিয়ে কাজ করে। তাই এর কার্যকারিতা, স্থায়িত্ব ও ব্যাটারি খরচ যাচাইয়ের জন্য একটি কঠোর টেস্টিং কৌশল গ্রহণ করা হয়েছে।

---

## ১. টেস্ট পিরামিড (Testing Pyramid)

```
        / \
       /   \        End-to-End / Journey Tests
      / E2E \       (MediaProjection & Real Apps, 10%)
     /-------\
    /         \     Integration & Benchmark Tests
   /Integration\    (Room DAO, TFLite Pipeline, 30%)
  /-------------\
 /               \  Unit Tests
/    Unit Tests   \ (UseCases, ViewModels, StateFlow, 60%)
-------------------
```

---

## ২. টেস্টের স্তরসমূহ (Testing Layers)

### ক. ইউনিট টেস্টিং (Unit Testing)
- **টুলস:** JUnit 4, Kotlinx-Coroutines-Test, Truth / Turbine (Flow ভ্যালিডেশনের জন্য)।
- **টার্গেট:**
  - সমস্ত UseCase লজিক (`StartShieldUseCase`, `StopShieldUseCase`, `GetStatsUseCase`)।
  - ViewModel-এর স্টেট ট্রানজিশন (`StateFlow<ShieldState>`)।
  - ডেটাবেসের কনভার্টার ও এন্টিটি ম্যাপিং।

### খ. ইন্টিগ্রেশন টেস্টিং (Integration Testing)
- **Room Database Test:**
  - `Room.inMemoryDatabaseBuilder` ব্যবহার করে বিভিন্ন কুয়েরি, ক্লিয়ার এবং স্ট্যাটাস ইনসার্ট ভ্যালিডেশন।
- **TFLite Model Inference Benchmarking:**
  - টেস্ট ইমেজ (নরমাল বনাম NSFW নমুনা) দিয়ে ক্লাসিফায়ারের অ্যাকুরেসি ও লেটেন্সি পরিমাপ (টার্গেট: <৩০ মিলিসেকেন্ড)।
  - মডেল লোডিং ও রিসোর্স আনলোডিং টেস্ট (মেমোরি লিক ছাড়া একাধিকবার স্টপ/স্টার্ট)।

### গ. পারফরম্যান্স, মেমরি ও ব্যাটারি টেস্ট (Performance & Battery Profiling)
- **টুলস:** Android Studio Profiler (CPU, Memory, Energy profiler)।
- **যাচাইয়ের ক্ষেত্রসমূহ:**
  - টানা ১ ঘণ্টা ব্যাকগ্রাউন্ডে শিল্ড সক্রিয় রেখে ব্যাটারি ব্যবহারের হার (টার্গেট: প্রতি ঘণ্টায় সর্বোচ্চ ৩-৪% ড্রপ)।
  - ফ্রেম বাফারিং ও গ্ল্যাস মরফিক ওভারলে রেন্ডারিংয়ে মেমরি ফুটপ্রিন্ট ১৫০ এমবি এর নিচে থাকা।
  - ব্যাকগ্রাউন্ডে ওএস যাতে মেমোরির অভাবে সার্ভিস কিল না করে (`START_STICKY` ও লো প্রায়োরিটি নোটিফিকেশন)।

### ঘ. ওএস পারমিশন ও এজ-কেস টেস্টিং (Permission & Edge Cases)
- ব্যবহারকারী পারমিশন ডিনাই করলে অ্যাপ যাতে ক্র্যাশ না করে গ্রেসফুলভাবে `PermissionHubScreen`-এ গাইড করা।
- Android 10 থেকে Android 15 পর্যন্ত বিভিন্ন ওএস ভার্সনে MediaProjection এবং Foreground Service আচরণের সামঞ্জস্য রক্ষা করা।
- ফোন রিবুট হলে বুট রিসিভার দিয়ে স্বয়ংক্রিয়ভাবে শিল্ড পুনরায় সচল করার সক্ষমতা যাচাই।

