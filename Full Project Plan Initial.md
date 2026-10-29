# **SafeScreen**

## **Master Project Context & Development Blueprint**

**প্রজেক্টের নাম:** SafeScreen  
**ট্যাগলাইন:** Your Digital Purity & Focus Guard  
**প্রজেক্টের ধরন:** Privacy-First Digital Wellbeing, Content Filtering & Screen-Time Management  
**প্রাথমিক প্ল্যাটফর্ম:** Android Application  
**দ্বিতীয় প্ল্যাটফর্ম:** Google Chrome Extension  
**সম্ভাব্য ভবিষ্যৎ প্ল্যাটফর্ম:** অন্যান্য Chromium-ভিত্তিক ব্রাউজার এবং প্রয়োজন অনুযায়ী অন্যান্য প্ল্যাটফর্ম  
**প্রধান নীতি:** Privacy First, Offline AI, User-Controlled Protection, Digital Wellbeing

---

# **১. প্রজেক্টের পরিচিতি**

SafeScreen একটি আধুনিক, প্রাইভেসি-কেন্দ্রিক ডিজিটাল ওয়েলবিং অ্যাপ্লিকেশন। এর উদ্দেশ্য হলো ব্যবহারকারীদের অনাকাঙ্ক্ষিত ভিজ্যুয়াল কনটেন্ট, বিভ্রান্তিকর শর্ট-ভিডিও ফিড এবং অতিরিক্ত স্ক্রলিং থেকে নিজেদের রক্ষা করতে সাহায্য করা।

প্রজেক্টটি বিশেষভাবে এমন ব্যবহারকারীদের কথা বিবেচনা করে তৈরি করা হবে, যারা ব্যক্তিগত, ধর্মীয়, পারিবারিক বা মনোযোগ-সংক্রান্ত কারণে কিছু ধরনের ছবি, ভিডিও, অডিও কিংবা সোশ্যাল মিডিয়ার কনটেন্ট এড়িয়ে চলতে চান।

SafeScreen কোনো নির্দিষ্ট ধর্ম বা ব্যবহারকারী গোষ্ঠীর জন্য সীমাবদ্ধ থাকবে না। এটি হবে একটি সাধারণ-purpose ডিজিটাল ওয়েলবিং টুল, যেখানে ব্যবহারকারী নিজের পছন্দ ও প্রয়োজন অনুযায়ী সুরক্ষার নিয়ম নির্ধারণ করতে পারবেন।

অ্যাপটির মূল ধারণা তিনটি প্রধান সমস্যার সমাধান করা:

১. অনাকাঙ্ক্ষিত ছবি বা ভিডিও দেখা থেকে নিজেকে রক্ষা করা।  
২. অনাকাঙ্ক্ষিত অডিও এবং অন্যান্য ডিজিটাল বিভ্রান্তি নিয়ন্ত্রণ করা।  
৩. Reels, Shorts ও TikTok-এর মতো শর্ট-ভিডিও ফিডে অতিরিক্ত সময় ব্যয় করা কমানো।

এই তিনটি সমস্যাকে একটি সমন্বিত প্রোডাক্টের মধ্যে আনা হবে, তবে প্রতিটির জন্য আলাদা প্রযুক্তি, সেটিংস এবং বাস্তবায়ন পরিকল্পনা থাকবে।

## **১.১ প্রজেক্টের ভিশন**

একটি এমন ব্যক্তিগত ডিজিটাল সুরক্ষা ব্যবস্থা তৈরি করা, যা ব্যবহারকারীর ডিভাইসেই যতটা সম্ভব কাজ সম্পন্ন করবে, ব্যক্তিগত তথ্যের গোপনীয়তা বজায় রাখবে এবং ব্যবহারকারীকে নিজের ডিজিটাল অভ্যাসের ওপর অধিক নিয়ন্ত্রণ দেবে।

## **১.২ মূল নীতিমালা**

* Privacy by Design  
* Offline-First AI Processing  
* Minimal Permissions  
* Transparent User Consent  
* Modular and Maintainable Architecture  
* Battery and Performance Optimization  
* Configurable Content Protection  
* Digital Wellbeing Without Unnecessary Surveillance  
* Accessibility and Usability  
* Honest Communication About Technical Limitations

---

# **২. প্রধান সমস্যা ও সমাধান**

## **২.১ Visual Content Protection**

কোনো ওয়েবসাইট, ছবি, ভিডিও বা সোশ্যাল মিডিয়া ফিডে ব্যবহারকারীর নির্বাচিত নিয়মের সঙ্গে অসামঞ্জস্যপূর্ণ কনটেন্ট দেখা গেলে সেটি শনাক্ত করে ব্লার বা আড়াল করা।

সম্ভাব্য ব্যবহার:

* অনাকাঙ্ক্ষিত নগ্নতা বা যৌনভাবে স্পষ্ট কনটেন্ট ফিল্টার করা।  
* ব্যবহারকারীর নির্ধারিত ভিজ্যুয়াল কনটেন্ট ক্যাটাগরি অনুযায়ী ব্লার করা।  
* ওয়েবপেজে ছবির পাশাপাশি ভিডিওর দৃশ্যও পরীক্ষা করা।  
* ভুল শনাক্তকরণ কমাতে Confidence Threshold ব্যবহার করা।  
* ব্যবহারকারীকে নিরাপদ ও স্বচ্ছভাবে ব্লার করা কনটেন্ট প্রকাশ করার নিয়ন্ত্রণ দেওয়া।

**গুরুত্বপূর্ণ:** কোনো AI মডেল সব ধরনের অনাকাঙ্ক্ষিত ছবি বা ভিডিও নিখুঁতভাবে শনাক্ত করতে পারবে না। তাই False Positive, False Negative এবং Processing Delay বিবেচনায় রেখে সিস্টেম তৈরি করতে হবে।

## **২.২ Audio Protection**

ব্যবহারকারী অনাকাঙ্ক্ষিত অডিও থেকে নিজেকে দূরে রাখতে চাইলে নির্বাচিত অ্যাপ বা সমর্থিত পরিবেশে অডিও শ্রেণিবিন্যাসের মাধ্যমে সতর্কতা, মিউট বা অন্যান্য নিয়ন্ত্রণের ব্যবস্থা করা।

সম্ভাব্য অডিও ক্যাটাগরি:

* Music  
* Speech  
* Environmental Sound  
* User-defined sound categories

অডিও শনাক্তকরণ এবং অডিও বন্ধ করার সক্ষমতা দুটি আলাদা বিষয় হিসেবে বিবেচনা করতে হবে।

কোনো অডিওতে মিউজিক শনাক্ত হওয়া মানেই Android-এ অন্য অ্যাপের সাউন্ড নির্ভরযোগ্যভাবে বন্ধ করা সম্ভব হবে, এমন নয়।

## **২.৩ Reels & Short-Video Control**

ব্যবহারকারী Facebook Reels, Instagram Reels, YouTube Shorts এবং TikTok-এর মতো প্ল্যাটফর্মে কতক্ষণ বা কতবার শর্ট-ভিডিও দেখবেন, তা নিয়ন্ত্রণ করতে পারবেন।

সম্ভাব্য নিয়ন্ত্রণ:

* নির্দিষ্ট প্ল্যাটফর্মের শর্ট-ভিডিও ফিড লুকানো।  
* নির্বাচিত ফিডে প্রবেশ সীমিত করা।  
* দৈনিক সময়সীমা নির্ধারণ করা।  
* নির্দিষ্ট সময়ের মধ্যে দেখার অনুমোদিত সময় নিয়ন্ত্রণ করা।  
* দৈনিক ভিডিও বা সেশন সীমা নির্ধারণ করা।  
* সীমা শেষ হলে সতর্কতা বা ব্লক স্ক্রিন দেখানো।  
* ব্যবহারকারীর ডিজিটাল অভ্যাসের সারাংশ দেখানো।

এই মডিউলের পূর্ণ প্রযুক্তিগত পরিকল্পনা আলাদা Reels Research Document-এ নির্ধারিত হবে।

---

# **৩. প্রোডাক্ট আর্কিটেকচার: দুটি প্রধান প্ল্যাটফর্ম**

SafeScreen-এর Android App এবং Chrome Extension একই প্রোডাক্টের অংশ হলেও তাদের প্রযুক্তিগত বাস্তবায়ন আলাদা হবে।

## **৩.১ Android Application**

Android অ্যাপ হবে মোবাইল ডিভাইসের ডিজিটাল ওয়েলবিং ও সুরক্ষা নিয়ন্ত্রণের কেন্দ্র।

প্রধান দায়িত্ব:

* ব্যবহারকারীকে প্রোটেকশন সেটআপ করতে সাহায্য করা।  
* AI-ভিত্তিক কনটেন্ট শনাক্তকরণ পরিচালনা করা, যেখানে অনুমোদিত ও প্রযুক্তিগতভাবে সম্ভব।  
* সমর্থিত ক্ষেত্রে ব্লার বা ওভারলে প্রদর্শন করা।  
* অডিও নিয়ন্ত্রণের পরীক্ষামূলক সুবিধা প্রদান করা।  
* অ্যাপভিত্তিক নিয়ন্ত্রণ ও হোয়াইটলিস্ট পরিচালনা করা।  
* Reels ও Shorts-সংক্রান্ত ডিজিটাল ওয়েলবিং সেটিংস পরিচালনা করা।  
* স্থানীয় ডেটাবেসে সেটিংস ও পরিসংখ্যান সংরক্ষণ করা।

## **৩.২ Google Chrome Extension**

Chrome Extension হবে ব্রাউজারের ভেতরে ওয়েব কনটেন্ট নিয়ন্ত্রণের প্রধান মাধ্যম।

প্রধান দায়িত্ব:

* ওয়েবপেজে ছবি শনাক্ত ও ব্লার করা।  
* সমর্থিত ভিডিওর দৃশ্য পরীক্ষা করা।  
* নির্দিষ্ট ওয়েবসাইটে কনটেন্ট ফিল্টার করা।  
* Reels ও Shorts ফিড লুকানো বা সীমিত করা।  
* নির্বাচিত ওয়েবসাইটে সময়সীমা ও ব্যবহার-নিয়ম প্রয়োগ করা।  
* লোকাল স্টোরেজে সেটিংস সংরক্ষণ করা।  
* প্রয়োজন অনুযায়ী Android অ্যাপের সঙ্গে ভবিষ্যৎ সমন্বয়ের ব্যবস্থা রাখা।

Chrome Extension-এ DOM পর্যবেক্ষণ, CSS পরিবর্তন এবং JavaScript-এর মাধ্যমে কনটেন্ট নিয়ন্ত্রণ করা যায়। তবে প্রতিটি ওয়েবসাইটের কাঠামো আলাদা হওয়ায় আলাদা প্ল্যাটফর্ম অ্যাডাপ্টার প্রয়োজন হতে পারে।

## **৩.৩ দুই প্ল্যাটফর্মের মধ্যে সমন্বয়**

প্রাথমিক সংস্করণে Android অ্যাপ ও Chrome Extension স্বাধীনভাবে কাজ করবে।

প্রথম সংস্করণে কোনো বাধ্যতামূলক ক্লাউড সার্ভার বা অ্যাকাউন্ট সিস্টেম প্রয়োজন হবে না।

ভবিষ্যতে ব্যবহারকারী চাইলে সেটিংস সিঙ্ক্রোনাইজেশনের ব্যবস্থা বিবেচনা করা যেতে পারে। তবে সেটি আলাদা ঐচ্ছিক ফিচার হবে এবং ডিফল্টভাবে ব্যক্তিগত তথ্য বাইরে পাঠানো হবে না।

---

# **৪. Visual Content Protection Module**

## **৪.১ উদ্দেশ্য**

AI মডেলের সাহায্যে নির্বাচিত ভিজ্যুয়াল কনটেন্ট ক্যাটাগরি শনাক্ত করা এবং সম্ভব হলে সংশ্লিষ্ট কনটেন্টের ওপর ব্লার বা আড়াল প্রয়োগ করা।

## **৪.২ সম্ভাব্য AI প্রযুক্তি**

প্রাথমিক গবেষণায় বিবেচনা করা যেতে পারে:

* TensorFlow Lite অথবা সমর্থিত LiteRT Runtime  
* NSFWJS-জাতীয় ব্রাউজারভিত্তিক ইমেজ ক্লাসিফিকেশন প্রযুক্তি  
* NudeNet-জাতীয় NSFW Detection প্রযুক্তি  
* উপযুক্ত Open-Source Image Classification Models  
* প্রয়োজন অনুযায়ী Object Detection বা Region Detection Models

কোনো নির্দিষ্ট মডেল বেছে নেওয়ার আগে লাইসেন্স, মডেলের আকার, ইনফারেন্সের গতি, RAM ব্যবহার, নির্ভুলতা এবং মোবাইল/ব্রাউজার সামঞ্জস্য পরীক্ষা করতে হবে।

## **৪.৩ Processing Workflow**

১. সমর্থিত উৎস থেকে একটি ছবি বা ভিডিও ফ্রেম শনাক্ত করা হবে।  
২. প্রয়োজন অনুযায়ী ছবিটি Resize ও Normalize করা হবে।  
৩. AI মডেল কনটেন্ট বিশ্লেষণ করবে।  
৪. মডেল একটি বা একাধিক শ্রেণি এবং Confidence Score দেবে।  
৫. ব্যবহারকারীর নির্বাচিত নিয়মের সঙ্গে ফলাফল তুলনা করা হবে।  
৬. নির্ধারিত Threshold অতিক্রম করলে সংশ্লিষ্ট কনটেন্ট ব্লার করা হবে।  
৭. স্থানীয়ভাবে একটি Event Record তৈরি করা যেতে পারে।  
৮. একই কনটেন্ট বারবার পরীক্ষা করা এড়াতে উপযুক্ত Caching ব্যবহার করা হবে।

## **৪.৪ Detection Policy**

সিস্টেমে অন্তত তিনটি Detection Sensitivity থাকবে:

**Low:** উচ্চ Confidence থাকলেই ব্লার করবে।

**Medium:** নির্ভুলতা ও সুরক্ষার মধ্যে ভারসাম্য রাখবে।

**High:** অপেক্ষাকৃত বেশি কনটেন্ট ব্লার করবে; ফলে সাধারণ ছবি ভুলভাবে ব্লার হওয়ার সম্ভাবনাও বাড়তে পারে।

Sensitivity শুধু একটি UI Slider হবে না। প্রতিটি মোডের জন্য পরীক্ষিত Threshold এবং আচরণ নির্ধারণ করতে হবে।

## **৪.৫ Blur ও Reveal**

সম্ভাব্য ফিচার:

* Automatic Blur  
* Full-Element Blur  
* Supported Region Blur  
* Tap-to-Reveal  
* Double-Tap-to-Reveal  
* Temporary Reveal Timer  
* Manual Re-Blur  
* False-Positive Feedback

Reveal করার সময়সীমা ডিফল্টভাবে ৫ সেকেন্ড রাখা যেতে পারে।

ব্যবহারকারী কোন ধরনের Reveal Interaction চান, তা Settings থেকে নির্ধারণ করতে পারবেন।

## **৪.৬ ভিডিও শনাক্তকরণ**

ভিডিওর প্রতিটি ফ্রেম পরীক্ষা করলে ব্যাটারি ও প্রসেসিং খরচ অনেক বেড়ে যেতে পারে।

তাই প্রথমে নির্দিষ্ট বিরতিতে ফ্রেম পরীক্ষা, দৃশ্য পরিবর্তন শনাক্তকরণ এবং প্রয়োজন অনুযায়ী Adaptive Sampling পরীক্ষা করতে হবে।

Android-এ সর্বোচ্চ ২–৩ FPS একটি প্রাথমিক পরীক্ষামূলক লক্ষ্য হতে পারে; এটি কোনো নিশ্চয়তাপ্রাপ্ত পারফরম্যান্স সীমা নয়।

ব্রাউজারে দৃশ্যমান ভিডিও, ভিডিওর অভ্যন্তরীণ ফ্রেম এবং সাধারণ DOM ইমেজের জন্য আলাদা প্রসেসিং কৌশল লাগতে পারে।

---

# **৫. Android Screen Monitoring: গুরুত্বপূর্ণ সীমাবদ্ধতা**

Android-এর MediaProjection API ব্যবহারকারীর অনুমোদনের মাধ্যমে স্ক্রিন কনটেন্ট ক্যাপচার করতে পারে।

তবে এটি কোনো অ্যাপকে সব পরিস্থিতিতে নির্বিঘ্নে অন্য অ্যাপের স্ক্রিন পড়া বা পরিবর্তন করার পূর্ণ অধিকার দেয় না।

সম্ভাব্য সমস্যা:

* ব্যবহারকারীর স্পষ্ট Screen Capture Consent প্রয়োজন।  
* স্ক্রিন ক্যাপচার চলাকালে সিস্টেমের নির্দেশনা ও স্থায়ী নোটিফিকেশন প্রয়োজন হতে পারে।  
* ব্যবহারকারী যেকোনো সময় ক্যাপচার বন্ধ করতে পারেন।  
* কিছু অ্যাপ স্ক্রিন ক্যাপচার প্রতিরোধ করতে পারে।  
* সংবেদনশীল বা সুরক্ষিত কনটেন্ট ক্যাপচারে নাও আসতে পারে।  
* ক্যাপচার করা স্ক্রিনে AI শনাক্তকরণ করতে সময় লাগতে পারে।  
* Window Overlay সব অ্যাপ বা সব পরিস্থিতিতে একইভাবে কাজ নাও করতে পারে।  
* অন্য অ্যাপের কনটেন্টের ওপর নির্ভুল ও তাৎক্ষণিক ব্লার প্রয়োগের জন্য আলাদা Prototype প্রয়োজন।

অতএব, পুরো Android সিস্টেমের সব অ্যাপে স্বয়ংক্রিয় ব্লার নির্ভরযোগ্যভাবে কাজ করবে—এমন দাবি করা যাবে না।

**প্রস্তাবিত পদ্ধতি:** প্রথমে Android-এর স্ক্রিন ক্যাপচার, অনুমোদিত প্রসেসিং এবং Overlay-এর সম্ভাব্যতা নিয়ে একটি ছোট Technical Prototype তৈরি করতে হবে। তারপর পূর্ণ অ্যাপের আর্কিটেকচার চূড়ান্ত করতে হবে।

---

# **৬. Audio Protection Module**

## **৬.১ উদ্দেশ্য**

সমর্থিত অডিও স্ট্রিম থেকে অনাকাঙ্ক্ষিত অডিও শ্রেণি শনাক্ত করা এবং ব্যবহারকারীর নির্বাচিত নিয়ম অনুযায়ী ব্যবস্থা নেওয়া।

## **৬.২ সম্ভাব্য প্রযুক্তি**

* TensorFlow Lite / LiteRT  
* YAMNet-জাতীয় Audio Event Classification Model  
* প্রয়োজন অনুযায়ী Music/Speech Classification Model  
* Android AudioPlaybackCapture API  
* Android AudioManager এবং সমর্থিত Playback Controls

## **৬.৩ সম্ভাব্য Workflow**

১. অনুমোদিত অডিও উৎস নির্বাচন করা হবে।  
২. সমর্থিত হলে AudioPlaybackCapture দিয়ে অডিও নমুনা নেওয়া হবে।  
৩. অডিও বাফার নির্ধারিত আকারে প্রস্তুত করা হবে।  
৪. AI মডেল অডিও শ্রেণিবিন্যাস করবে।  
৫. ফলাফল Confidence Threshold-এর সঙ্গে তুলনা করা হবে।  
৬. ব্যবহারকারীর নিয়ম অনুযায়ী সতর্কতা বা সমর্থিত Audio Control প্রয়োগ করা হবে।  
৭. স্থানীয়ভাবে প্রয়োজনীয় Event Count সংরক্ষণ করা হবে।

## **৬.৪ গুরুত্বপূর্ণ সীমাবদ্ধতা**

AudioPlaybackCapture-এর জন্য অডিও রেকর্ডিং অনুমতি এবং MediaProjection-এর অনুমোদন প্রয়োজন হয়। পাশাপাশি অন্য অ্যাপের অডিও ক্যাপচারযোগ্য হতে হবে।

তাই অন্য অ্যাপের প্রতিটি অডিও শনাক্ত করা যাবে না।

অডিও শনাক্ত হওয়ার পর অন্য অ্যাপের Master Volume স্বয়ংক্রিয়ভাবে পরিবর্তন করার জন্য কোনো সাধারণ, নির্ভরযোগ্য ও সর্বজনীন API ধরে নেওয়া যাবে না।

প্রাথমিক সংস্করণে নিচের বিকল্পগুলো পরীক্ষা করা উচিত:

* Audio Detection Alert  
* User-Initiated Mute  
* Supported App-Specific Controls  
* Optional Audio Session Monitoring

সম্পূর্ণ স্বয়ংক্রিয় অডিও মিউটকে প্রথম সংস্করণের নিশ্চিত ফিচার হিসেবে ঘোষণা করা যাবে না।

---

# **৭. Reels & Digital Wellbeing Module (Android Limitations & Feasible Solution)**

এই মডিউল ব্যবহারকারীকে শর্ট-ভিডিও আসক্তি ও অতিরিক্ত স্ক্রলিং থেকে সুরক্ষা দিতে সাহায্য করবে।

## **৭.১ প্রাথমিক পরিকল্পনা ও সীমাবদ্ধতা**
* **প্রাথমিক পরিকল্পনা ছিল:** Android অ্যাপে Facebook, YouTube বা TikTok-এর ভেতর থেকে নির্দিষ্ট Reels বা Shorts ফিডটি CSS/DOM ম্যানিপুলেশনের মতো হাইড (Hide) বা রিমুভ করে দেওয়া অথবা ব্যবহারকারী ১০টি রিলস দেখলে স্বয়ংক্রিয়ভাবে রিলস লক করে দেওয়া।
* **বাস্তবতা (কেন এটি প্রায় অসম্ভব):** Android অপারেটিং সিস্টেমের কঠোর "Sandboxing" নীতির কারণে একটি অ্যাপ অন্য কোনো অ্যাপের ইউআই (UI) সরাসরি পরিবর্তন বা মুছে ফেলতে পারে না। ব্রাউজারের মতো এখানে কোড বা স্টাইল ইনজেক্ট করা যায় না। এছাড়া Meta বা Google তাদের অ্যাপের UI Node-গুলো প্রতিনিয়ত ডাইনামিকভাবে পরিবর্তন ও অবফুসকেট (Obfuscation) করে। তাই অন্য অ্যাপের ভেতর থেকে নির্দিষ্ট একটি বাটন বা ফিড নিখুঁতভাবে গায়েব করা কিংবা ৩য় পক্ষের অ্যাপের ভেতর নিখুঁতভাবে ১০টি ভিডিও গণনা করা অ্যান্ড্রয়েডে অবাস্তব, অনির্ভরযোগ্য এবং ভঙ্গুর।

## **৭.২ বাস্তবসম্মত বিকল্প ও কার্যকর সমাধান (App Implementation)**
UI রিমুভ করার অবাস্তব চেষ্টার পরিবর্তে, Android অ্যাপে নিচের দুটি নির্ভরযোগ্য পদ্ধতি ব্যবহার করে ব্যবহারকারীকে শর্ট-ভিডিওর আসক্তি থেকে মুক্ত করা হবে:

১. **App-Level Time Limit (UsageStatsManager):**  
পুরো অ্যাপের জন্য নির্দিষ্ট সময়সীমা নির্ধারণ করা। যেমন, ব্যবহারকারী দিনে সর্বোচ্চ ৩০ মিনিট TikTok বা Instagram ব্যবহার করতে পারবেন। `UsageStatsManager` API ব্যবহার করে এটি ওএস লেভেলে নিখুঁতভাবে ট্র্যাক করা হবে। সময়সীমা শেষ হলে অ্যাপটি ব্যবহারের ওপর পূর্ণ বা আংশিক ব্লক আরোপ করা হবে।

২. **Accessibility Blocker / Overlay (AccessibilityService + WindowManager):**  
ব্যবহারকারী যখন Facebook বা YouTube-এ থাকবেন, তখন `AccessibilityService` ব্যাকগ্রাউন্ডে স্ক্রিনের টেক্সট বা অ্যাক্সেসিবিলিটি নোড মনিটর করবে। ইউজার যদি নির্দিষ্ট "Shorts" বা "Reels" সেকশনে প্রবেশ করেন, তবে সিস্টেম তা শনাক্ত করে সাথে সাথে `System Alert Window` (`TYPE_APPLICATION_OVERLAY`) ব্যবহার করে স্ক্রিনের ওপর একটি নিশ্ছিদ্র ব্লকার বা সতর্কবার্তা (যেমন: *"Focus Time Active / Reels Blocked"*) ভাসিয়ে দেবে।  
* **ফলাফল:** রিলস স্ক্রিন গায়েব হবে না ঠিকই, কিন্তু ব্যবহারকারী রিলস দেখতেও পারবেন না।

*(নোট: Chrome Extension-এর ক্ষেত্রে DOM Manipulation ব্যবহার করে সরাসরি Reels/Shorts হাইড বা ব্লক করার প্রাথমিক পরিকল্পনাটিই বহাল থাকবে, কারণ ব্রাউজারে এটি ১০০% সম্ভব।)*

## **৭.৩ ডিজিটাল ওয়েলবিয়িং ফিচারসমূহ**
* **Daily Time Limit:** নির্বাচিত প্ল্যাটফর্ম বা শর্ট-ভিডিও সেকশনে দৈনিক সময়সীমা নির্ধারণ।
* **Scheduled Mode (Focus Mode):** কাজের বা পড়াশোনার সময়ে শর্ট-ভিডিও প্ল্যাটফর্ম সম্পূর্ণ নিষিদ্ধ রাখা।
* **Break Reminder:** একটানা ১৫ মিনিট ব্যবহারের পর ব্যবহারকারীকে চোখ ও মনকে বিশ্রাম দেওয়ার নটিফিকেশন।
* **Daily Usage Summary:** দৈনিক কত সময় বাঁচানো হয়েছে তার অফলাইন অ্যানালিটিক্স।

---

# **৮. App Whitelist ও User-Controlled Rules**

ব্যবহারকারী নির্দিষ্ট অ্যাপ বা ওয়েবসাইটকে সুরক্ষা নিয়ম থেকে বাদ দিতে পারবেন।

উদাহরণ:

* Banking Apps  
* Educational Apps  
* Messaging Apps  
* Work Tools  
* User-Selected Websites

Whitelist-এর নিয়ম স্পষ্ট হতে হবে।

কোনো অ্যাপ Whitelist-এ থাকলে সেটি শুধু Visual Protection থেকে বাদ যাবে, নাকি Audio Protection এবং Reels Controls থেকেও বাদ যাবে, তা ব্যবহারকারী আলাদাভাবে নির্ধারণ করতে পারবেন।

সমর্থিত Android API ছাড়া অন্য অ্যাপের প্রতিটি আচরণ নির্ভুলভাবে শনাক্ত করার দাবি করা যাবে না।

---

# **৯. Security ও Protection Lock**

ব্যবহারকারী সুরক্ষা সেটিংস পরিবর্তনের জন্য ঐচ্ছিক PIN অথবা Biometric Authentication চালু করতে পারবেন।

সম্ভাব্য ফিচার:

* PIN Setup  
* PIN Verification  
* Biometric Unlock  
* Sensitive Settings Protection  
* Protection Disable Confirmation  
* Recovery Flow  
* Failed Attempt Handling

গুরুত্বপূর্ণ: ব্যক্তিগত ডিভাইসে PIN বা Biometric Lock দিয়ে ব্যবহারকারীকে সম্পূর্ণভাবে নিজের অ্যাপ বন্ধ করা থেকে আটকানো যাবে—এমন দাবি করা যাবে না।

SafeScreen-এর উদ্দেশ্য হবে ইচ্ছাকৃতভাবে সুরক্ষা বন্ধ করা কঠিন করা, ব্যবহারকারীর ডিভাইসের ওপর তার বৈধ নিয়ন্ত্রণ কেড়ে নেওয়া নয়।

---

# **১০. Analytics ও Local Event History**

Analytics মডিউল ব্যবহারকারীকে নিজের ডিজিটাল অভ্যাস এবং সুরক্ষা ব্যবস্থার কার্যক্রম বুঝতে সাহায্য করবে।

সম্ভাব্য তথ্য:

* আজ কতবার কনটেন্ট ব্লার হয়েছে।  
* সপ্তাহে কতবার ব্লার হয়েছে।  
* কোন মডিউল কতবার সক্রিয় হয়েছে।  
* কতবার Reels Limit অতিক্রমের চেষ্টা হয়েছে।  
* কত সময় Focus Mode চালু ছিল।  
* কতবার ভুল শনাক্তকরণের রিপোর্ট করা হয়েছে।

সব Analytics সংখ্যাকে প্রকৃত ক্ষতিকর কনটেন্টের নিখুঁত পরিমাপ হিসেবে উপস্থাপন করা যাবে না।

উদাহরণস্বরূপ, ১০০ বার ব্লার হওয়া মানে নিশ্চিতভাবে ১০০টি ক্ষতিকর কনটেন্ট শনাক্ত হয়েছে—এমন নয়। একই কনটেন্ট একাধিকবার শনাক্ত হতে পারে।

Analytics-এ Event Count, Unique Item Estimate এবং Time-Based Usage আলাদা রাখা উচিত।

---

# **১১. প্রস্তাবিত UI/UX ও স্ক্রিনসমূহ**

## **১১.১ Splash Screen**

* SafeScreen Logo  
* App Name  
* Tagline  
* সংক্ষিপ্ত অ্যানিমেশন

## **১১.২ Onboarding**

তিনটি প্রধান পৃষ্ঠা:

১. Visual Content Protection  
২. Audio & Digital Distraction Control  
৩. Reels, Shorts & Screen-Time Management

ব্যবহারকারীকে স্পষ্টভাবে জানাতে হবে যে কোন ফিচার কী ধরনের অনুমতি চায় এবং কোথায় প্রযুক্তিগত সীমাবদ্ধতা আছে।

## **১১.৩ Permission Hub**

প্রয়োজন অনুযায়ী আলাদা Permission Card:

* Screen Capture  
* Overlay  
* Audio Recording / Playback Capture  
* Accessibility Service, যদি কোনো নির্দিষ্ট ফিচারের জন্য সত্যিই প্রয়োজন হয়  
* Notifications  
* Usage Access, যদি সমর্থিত সময়-পর্যবেক্ষণ ফিচার বাস্তবায়িত হয়

সব Permission একসঙ্গে অকারণে চাওয়া যাবে না।

প্রতিটি Permission-এর উদ্দেশ্য ব্যাখ্যা করতে হবে এবং ব্যবহারকারীকে স্পষ্ট সম্মতি দিতে হবে।

## **১১.৪ Home Dashboard**

প্রধান উপাদান:

* Protection Status  
* Main Shield Control  
* Visual Protection Status  
* Audio Protection Status  
* Reels Control Status  
* Today's Activity  
* Quick Settings

শিল্ড অন বা অফ করার পাশাপাশি প্রতিটি মডিউলের অবস্থা আলাদাভাবে দেখাতে হবে।

## **১১.৫ Protection Settings**

* Visual Detection Sensitivity  
* Blur Behavior  
* Reveal Duration  
* Audio Detection  
* App Whitelist  
* Website Rules

## **১১.৬ Reels Control Screen**

* Supported Platforms  
* Hide/Block Mode  
* Daily Time Limit  
* Session Limit  
* Daily View Limit, যদি সমর্থিত হয়  
* Scheduled Restriction  
* Strict Mode  
* Usage Summary

## **১১.৭ Analytics**

* Daily Summary  
* Weekly Chart  
* Module-Based Breakdown  
* Usage History  
* False-Positive Reports

## **১১.৮ Security Settings**

* PIN  
* Biometric Authentication  
* Protected Settings  
* Recovery Options

## **১১.৯ Privacy & Permissions**

* বর্তমানে চালু থাকা সুরক্ষা ফিচার  
* প্রয়োজনীয় অনুমতি  
* লোকাল ডেটা কী সংরক্ষণ হচ্ছে  
* ডেটা মুছে ফেলার অপশন  
* AI মডেল ও তার সংস্করণ

---

# **১২. প্রস্তাবিত প্রযুক্তিগত স্ট্যাক**

## **১২.১ Android**

* Kotlin  
* Jetpack Compose  
* Material 3  
* MVVM  
* Clean Architecture  
* Dagger-Hilt  
* Kotlin Coroutines  
* StateFlow  
* Room Database  
* DataStore  
* TensorFlow Lite / LiteRT  
* MediaProjection API  
* AudioPlaybackCapture API  
* WindowManager API  
* Android Biometric APIs

প্রতিটি লাইব্রেরির বর্তমান রক্ষণাবেক্ষণ অবস্থা, সামঞ্জস্য এবং লাইসেন্স যাচাই করে নির্দিষ্ট সংস্করণ নির্বাচন করতে হবে।

## **১২.২ Chrome Extension**

* JavaScript অথবা TypeScript  
* Chrome Extension Manifest V3  
* Content Scripts  
* Service Worker  
* CSS  
* DOM MutationObserver  
* Chrome Storage API  
* Chrome Alarms API, যেখানে উপযুক্ত  
* Browser-Compatible AI Runtime

Extension-এর জন্য Android-এর Kotlin কোড সরাসরি ব্যবহার করা যাবে না। তবে প্রয়োজন অনুযায়ী কিছু সাধারণ নিয়ম, কনফিগারেশন স্কিমা এবং মডেল-সম্পর্কিত ধারণা দুই প্ল্যাটফর্মে পুনর্ব্যবহার করা যেতে পারে।

## **১২.৩ Backend**

প্রথম সংস্করণে Backend বাধ্যতামূলক নয়।

যে ফিচারগুলো অফলাইনে চালানো সম্ভব, সেগুলো লোকালভাবেই চালাতে হবে।

ভবিষ্যতে ঐচ্ছিক Sync বা Account System যুক্ত হলে তার জন্য আলাদা Privacy Review করতে হবে।

---

# **১৩. Clean Architecture ও প্রস্তাবিত ফোল্ডার স্ট্রাকচার**

Android অ্যাপের কোডে Data, Domain এবং Presentation Layer আলাদা রাখতে হবে।

প্রস্তাবিত কাঠামো:

app/

* Main Application  
* Navigation  
* Dependency Injection  
* App Configuration

core/

* Common Utilities  
* Permissions  
* AI Runtime  
* Database  
* Security  
* Logging

feature/

* onboarding/  
* dashboard/  
* visual\_protection/  
* audio\_protection/  
* reels\_control/  
* analytics/  
* whitelist/  
* security\_settings/  
* privacy\_settings/

প্রতিটি Feature-এর প্রয়োজন অনুযায়ী:

* data/  
* domain/  
* presentation/

Data Layer:

* Repository Implementations  
* Local Data Sources  
* Database Access  
* Model Integration Adapters

Domain Layer:

* Entities  
* Repository Interfaces  
* Use Cases  
* Business Rules

Presentation Layer:

* Compose Screens  
* UI Components  
* ViewModels  
* UI State  
* UI Events

Android-এর বিশেষ সেবা, যেমন MediaProjection বা WindowManager, সরাসরি Compose UI-এর মধ্যে পরিচালনা করা যাবে না। এগুলো উপযুক্ত Platform Service এবং Lifecycle Management Layer-এ রাখতে হবে।

Chrome Extension-এর জন্য আলাদা Modular Architecture তৈরি করতে হবে।

---

# **১৪. Privacy ও Data Protection Policy**

SafeScreen-এর মূল প্রতিশ্রুতি হবে ব্যক্তিগত তথ্যের গোপনীয়তা।

## **১৪.১ Local-First Processing**

সম্ভব হলে:

* ছবি ডিভাইসেই প্রসেস হবে।  
* ভিডিও ফ্রেম ডিভাইসেই প্রসেস হবে।  
* অডিও বাফার ডিভাইসেই প্রসেস হবে।  
* AI ফলাফল লোকালভাবে ব্যবহার হবে।  
* Settings লোকাল ডেটাবেসে সংরক্ষিত হবে।  
* Analytics লোকালভাবে গণনা হবে।

## **১৪.২ Network Access**

প্রথম সংস্করণে কোনো অপ্রয়োজনীয় নেটওয়ার্ক যোগাযোগ থাকবে না।

Android অ্যাপে Internet Permission না রাখার লক্ষ্য থাকবে, যদি নির্বাচিত প্রযুক্তিগত উপাদানগুলো তা ছাড়াই কাজ করতে পারে।

Chrome Extension-এ কোনো অপ্রয়োজনীয় Remote API, Analytics Endpoint বা Third-Party Tracking থাকবে না।

তবে Android অ্যাপে Internet Permission না থাকা এবং Chrome Extension-এর কোনো নেটওয়ার্ক অ্যাক্সেস না থাকা একই বিষয় নয়।

Extension-এর ওয়েবসাইটভিত্তিক কাজের জন্য প্রয়োজনীয় Host Permission থাকতে পারে। Host Permission নিজে থেকেই ব্যক্তিগত তথ্য সার্ভারে পাঠানো বোঝায় না; তবু প্রতিটি Permission ন্যূনতম রাখতে হবে।

## **১৪.৩ Logging**

লগে কোনো ছবি, ভিডিও ফ্রেম, অডিও বাফার, ব্যক্তিগত URL বা সংবেদনশীল কনটেন্ট সংরক্ষণ করা যাবে না।

প্রয়োজন হলে শুধু প্রযুক্তিগত অবস্থা, Error Code এবং সীমিত Diagnostic তথ্য রাখা যাবে।

## **১৪.৪ Privacy Verification**

Release-এর আগে যাচাই করতে হবে:

* অ্যাপের Network Traffic  
* ব্যবহৃত লাইব্রেরির Network Behavior  
* Manifest Permissions  
* Extension Permissions  
* Local Database Content  
* Log Output  
* AI Model Loading  
* Data Deletion Behavior

কোনো সিস্টেমকে পরীক্ষা ছাড়া ১০০% নিরাপদ বা সম্পূর্ণ ঝুঁকিমুক্ত বলা যাবে না।

---

# **১৫. Performance ও Battery Optimization**

স্ক্রিন এবং অডিও প্রসেসিংয়ের জন্য নির্দিষ্ট Performance Budget তৈরি করতে হবে।

প্রাথমিক লক্ষ্য:

* UI Thread-এ AI Inference চালানো যাবে না।  
* Coroutines ও উপযুক্ত Dispatchers ব্যবহার করতে হবে।  
* অপ্রয়োজনীয় Frame Processing এড়াতে হবে।  
* একই কনটেন্ট বারবার প্রসেস করা কমাতে হবে।  
* Model Input Size যুক্তিসংগত রাখতে হবে।  
* Memory Leak পরীক্ষা করতে হবে।  
* Background Service-এর Lifecycle সঠিকভাবে পরিচালনা করতে হবে।  
* দীর্ঘ সময় ব্যবহারে Battery Drain পরিমাপ করতে হবে।  
* নিম্নক্ষমতার ডিভাইসে Performance পরীক্ষা করতে হবে।

প্রতি সেকেন্ডে ২–৩টি ফ্রেম একটি প্রাথমিক পরীক্ষামূলক লক্ষ্য। মডেল, ডিভাইস ও ব্যবহারের পরিস্থিতি অনুযায়ী প্রকৃত হার পরিবর্তিত হতে পারে।

---

# **১৬. Development Roadmap**

## **Phase 0: Feasibility Research**

কোডিং শুরু করার আগে প্রযুক্তিগত সম্ভাব্যতা যাচাই করতে হবে।

কাজ:

* Android Screen Capture Prototype  
* Overlay Prototype  
* Image Classification Benchmark  
* Video Frame Sampling Test  
* Audio Capture Prototype  
* Audio Classification Benchmark  
* Chrome Image Blur Prototype  
* Reels/Shorts DOM Filtering Prototype  
* Time Limit Enforcement Prototype  
* Privacy and Permission Audit

Deliverable:  
Feasibility Report এবং MVP Feature Matrix।

## **Phase 1: Foundation & UI**

* Android Project Setup  
* Chrome Extension Project Setup  
* Clean Architecture  
* Design System  
* Navigation  
* Splash Screen  
* Onboarding  
* Home Dashboard  
* Settings  
* Local Database  
* Mock Data

Deliverable:  
সম্পূর্ণ নেভিগেশনসহ UI Prototype।

## **Phase 2: Permission & Platform Services**

* Permission Hub  
* MediaProjection Integration  
* Foreground Service, যেখানে প্রয়োজন  
* Overlay Permission Handling  
* Audio Capture Prototype  
* Lifecycle Management  
* Error Handling

Deliverable:  
অনুমোদিত Platform API-গুলোর কার্যকর Prototype।

## **Phase 3: Visual AI**

* Model Evaluation  
* Image Classification  
* Frame Sampling  
* Confidence Threshold  
* Blur Logic  
* Reveal Interaction  
* Performance Tests

Deliverable:  
সমর্থিত পরিবেশে পরীক্ষিত Visual Protection।

## **Phase 4: Browser Extension**

* Manifest V3  
* Content Scripts  
* Image Detection  
* Blur Overlay  
* Platform-Specific Adapters  
* Local Settings  
* Reels/Shorts Controls  
* Time Limit Logic

Deliverable:  
প্রথম কার্যকর Chrome Extension।

## **Phase 5: Audio Protection**

* Audio Model Integration  
* Supported Audio Capture  
* Classification Pipeline  
* User Alerts  
* Supported Mute Controls  
* Performance and Permission Tests

Deliverable:  
যেসব পরিবেশে নির্ভরযোগ্যভাবে কাজ করে, সেগুলোর জন্য Audio Protection।

## **Phase 6: Reels & Digital Wellbeing**

* Platform Selection  
* Daily Time Limits  
* Session Limits  
* Scheduled Blocking  
* Optional Item/View Limits  
* Focus Mode  
* Usage Analytics

Deliverable:  
পরীক্ষিত Reels Control Module।

## **Phase 7: Security & Analytics**

* PIN  
* Biometric Authentication  
* Whitelist  
* Room Analytics  
* False-Positive Reporting  
* Data Deletion  
* Privacy Settings

Deliverable:  
সম্পূর্ণ Local Data Management।

## **Phase 8: Testing & Release Preparation**

* Unit Tests  
* Integration Tests  
* UI Tests  
* Device Compatibility  
* Battery Tests  
* Privacy Audit  
* Accessibility Review  
* Release Build  
* Documentation

Deliverable:  
পরীক্ষিত Release Candidate।

---

# **১৭. Testing Strategy**

## **Functional Testing**

* Permission দেওয়া ও প্রত্যাহার করা।  
* Protection Start/Stop।  
* Image Detection।  
* Blur এবং Reveal।  
* Audio Classification।  
* Whitelist।  
* Time Limit।  
* Daily Reset।  
* App Restart-এর পর Settings Restore।  
* Analytics Accuracy।

## **AI Testing**

* False Positive Rate  
* False Negative Rate  
* Confidence Calibration  
* Processing Latency  
* Model Size  
* Memory Usage  
* Low-Light বা কম-মানের ছবির প্রভাব  
* Video Scene Changes

## **Platform Testing**

* বিভিন্ন Android Version  
* বিভিন্ন Screen Size  
* বিভিন্ন Device Performance  
* Chrome-এর Supported Version  
* বিভিন্ন Website Layout  
* Single-Page Application Navigation  
* Dynamic Content Loading  
* Browser Restart  
* Offline Operation

## **Privacy Testing**

* Network Request Audit  
* Permission Review  
* Local Data Inspection  
* Logging Audit  
* Third-Party Dependency Review

---

# **১৮. MVP-এর পরিধি**

প্রথম সংস্করণে সব ফিচার একসঙ্গে তৈরি করার পরিবর্তে সবচেয়ে বাস্তবসম্মত ও পরীক্ষাযোগ্য ফিচার দিয়ে শুরু করতে হবে।

প্রস্তাবিত MVP:

১. Android অ্যাপের সম্পূর্ণ UI এবং Settings।  
২. Chrome Extension-এ নির্বাচিত ওয়েবসাইটের ছবি ব্লার।  
৩. Chrome Extension-এ YouTube Shorts ও Facebook Reels লুকানো বা সীমিত করা।  
৪. স্থানীয়ভাবে Daily Usage Limit গণনা।  
৫. Room/DataStore-ভিত্তিক Settings ও Analytics।  
৬. Android Screen Capture এবং Overlay-এর Feasibility Prototype।  
৭. Audio Classification-এর Feasibility Prototype।

Android-এর সব অ্যাপে স্বয়ংক্রিয় ব্লার এবং সব অ্যাপের অডিও বন্ধ করার মতো কঠিন ফিচারগুলো Prototype সফল হওয়ার পরই পূর্ণাঙ্গভাবে বাস্তবায়ন করতে হবে।

---

# **১৯. AI Coding Agent-এর জন্য স্থায়ী নির্দেশনা**

এই নির্দেশনাগুলো SafeScreen-এর সব Development Task-এর ক্ষেত্রে অনুসরণ করতে হবে।

## **Architecture**

* Clean Architecture বজায় রাখতে হবে।  
* Data, Domain এবং Presentation Layer আলাদা রাখতে হবে।  
* Business Logic Compose UI-এর মধ্যে রাখা যাবে না।  
* Platform Service এবং UI Lifecycle আলাদা রাখতে হবে।  
* Dependency Injection যথাযথভাবে ব্যবহার করতে হবে।  
* Shared Mutable State এবং অপ্রয়োজনীয় Global Singleton এড়াতে হবে।

## **Privacy**

* কোনো অপ্রয়োজনীয় Internet Permission যোগ করা যাবে না।  
* কোনো ছবি, ভিডিও বা অডিও বাইরে পাঠানো যাবে না।  
* অপ্রয়োজনীয় Analytics বা Tracking যোগ করা যাবে না।  
* কোনো নতুন Permission যোগ করার আগে তার প্রয়োজন ব্যাখ্যা করতে হবে।  
* সব তৃতীয়-পক্ষ লাইব্রেরির Privacy ও License যাচাই করতে হবে।

## **Performance**

* UI Thread-এ ভারী কাজ করা যাবে না।  
* AI Inference Background Dispatcher-এ চালাতে হবে।  
* Frame Sampling এবং Buffer Management কার্যকর হতে হবে।  
* Memory Leak এবং Battery Drain পরীক্ষা করতে হবে।  
* শুধু দ্রুত কাজ করার জন্য Privacy বা Stability কমানো যাবে না।

## **Code Quality**

* প্রতিটি Feature-এর আগে সংক্ষিপ্ত Implementation Plan দিতে হবে।  
* অপ্রয়োজনীয় ফাইল তৈরি করা যাবে না।  
* Error Handling থাকতে হবে।  
* Unit Tests যোগ করতে হবে।  
* পুরোনো কার্যকর ফিচার অকারণে ভেঙে ফেলা যাবে না।  
* Build এবং Tests চালিয়ে ফলাফল জানাতে হবে।  
* অসম্পূর্ণ ফিচারকে সম্পূর্ণ বলে ঘোষণা করা যাবে না।

## **Documentation**

প্রজেক্টের Root Directory-তে নিচের ডকুমেন্টগুলো বজায় রাখতে হবে:

* `README.md`  
* `PROJECT_CONTEXT_BN.md`  
* `ARCHITECTURE.md`  
* `DEVELOPMENT_ROADMAP.md`  
* `FEATURE_MATRIX.md`  
* `TECHNICAL_FEASIBILITY.md`  
* `PRIVACY_POLICY.md`  
* `TESTING_STRATEGY.md`  
* `PROJECT_PROGRESS.md`  
* `docs/research/REELS_CONTROL_RESEARCH_BN.md`

প্রতিটি গুরুত্বপূর্ণ সিদ্ধান্ত, প্রযুক্তিগত সীমাবদ্ধতা এবং সম্পন্ন কাজ নথিভুক্ত করতে হবে।

---

# **২০. Definition of Done**

কোনো ফিচারকে সম্পন্ন বলা যাবে না, যতক্ষণ না:

১. কোড বাস্তবায়ন করা হয়েছে।  
২. সংশ্লিষ্ট Build সফল হয়েছে।  
৩. প্রয়োজনীয় Test পাস করেছে।  
৪. বাস্তব ডিভাইস বা সমর্থিত পরিবেশে পরীক্ষা করা হয়েছে।  
৫. Permission ও Privacy আচরণ যাচাই করা হয়েছে।  
৬. পরিচিত সীমাবদ্ধতা নথিভুক্ত করা হয়েছে।  
৭. `PROJECT_PROGRESS.md` আপডেট করা হয়েছে।

UI-তে কোনো বাটন দেখা গেলেই সংশ্লিষ্ট ফিচার সম্পূর্ণ হয়েছে বলে গণ্য হবে না।

---

# **২১. চূড়ান্ত নির্দেশনা**

SafeScreen একটি দীর্ঘমেয়াদি, মডিউলার প্রজেক্ট। তাই একসঙ্গে সবকিছু তৈরি করার পরিবর্তে প্রথমে গবেষণা, তারপর ছোট Prototype, এরপর MVP এবং সবশেষে উন্নত ফিচার যুক্ত করতে হবে।

প্রতিটি নতুন ফিচারের ক্ষেত্রে তিনটি প্রশ্নের উত্তর দিতে হবে:

১. এটি বাস্তবে প্রযুক্তিগতভাবে সম্ভব কি?  
২. এটি ব্যবহারকারীর গোপনীয়তা ও ডিভাইসের পারফরম্যান্সের সঙ্গে সামঞ্জস্যপূর্ণ কি?  
৩. এটি পরীক্ষার মাধ্যমে নির্ভরযোগ্যভাবে কাজ করছে কি?

এই তিনটি শর্ত পূরণ না হওয়া পর্যন্ত সংশ্লিষ্ট ফিচারকে Experimental হিসেবে চিহ্নিত রাখতে হবে।

**SafeScreen-এর মূল লক্ষ্য শুধু অনাকাঙ্ক্ষিত কনটেন্ট ব্লক করা নয়; বরং ব্যবহারকারীকে নিজের ডিজিটাল পরিবেশ, মনোযোগ এবং অভ্যাসের ওপর আরও কার্যকর নিয়ন্ত্রণ দেওয়া।**

