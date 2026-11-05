# SafeScreen QA ও ফিজিক্যাল ডিভাইস টেস্টিং গাইড

এই গাইডটি Android 10–15 (API 29–35) ফোন বা এমুলেটরে ফিচারগুলো যাচাই করার জন্য। পরীক্ষার সময় নিজের বা অনুমতিপ্রাপ্ত কনটেন্ট ব্যবহার করুন। AI classifier ফলাফল probabilistic; একটি নির্দিষ্ট ছবি বা অডিওতে সবসময় একই ফল হবে—এমন নিশ্চয়তা নেই।

## বর্তমান যাচাই-অবস্থা

- Debug ও Release variant compile/build করা যায়। Unit test এবং বাস্তব device permission, MediaProjection, overlay, accessibility, biometric ও AI ফলাফল আলাদাভাবে চালিয়ে যাচাই করতে হবে।
- Release signing তথ্য না থাকলে `assembleRelease` unsigned APK তৈরি করে। Unsigned APK Android-এ install হবে না।
- পুরোনো signing key material Git history-তে ছিল। Release প্রকাশের আগে key rotate করুন এবং repository history পরিষ্কার করুন। নতুন signing key নিরাপদে configure না করা পর্যন্ত production release দেবেন না।
- Reels feed detection নির্দিষ্ট supported app/feed-এর accessibility layout-এর ওপর নির্ভরশীল। Social app update হলে selector বদলাতে পারে। বর্তমানে কার্যকর intervention হচ্ছে full-screen Hard Lockout; Soft Warning/Friction Delay দেওয়া হয় না।
- Settings-এর “Prevent Uninstallation” switch preference সংরক্ষণ করে; Device Owner/MDM ছাড়া এটি Android-এ uninstall আটকায় না।

## ১. Build ও installation প্রস্তুতি

### APK তৈরি

প্রজেক্ট root-এ চালান:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
```

সাধারণ output location:

- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk` যখন signing configure করা নেই। Signing configure থাকলে ফাইলের নাম `app-release.apk` হতে পারে।

Release APK ইনস্টল করার আগে signing configure করুন। `local.properties`-এ `release.storeFile`, `release.storePassword`, `release.keyAlias`, `release.keyPassword` দিন, অথবা `SAFESCREEN_RELEASE_STORE_FILE`, `SAFESCREEN_RELEASE_STORE_PASSWORD`, `SAFESCREEN_RELEASE_KEY_ALIAS`, `SAFESCREEN_RELEASE_KEY_PASSWORD` environment variable ব্যবহার করুন। Secret কোনো tracked file-এ commit করবেন না। APK signer যাচাই করতে Android SDK-এর `apksigner verify --print-certs <apk>` চালান।

### ফোনে ইনস্টল

1. APK-টি trusted build machine/repository থেকে কপি করুন; অজানা mirror বা re-upload করা APK ব্যবহার করবেন না।
2. Android prompt এলে যে file manager/browser দিয়ে APK খুলেছেন, কেবল সেই source-এর জন্য **Install unknown apps** অনুমতি দিন। ইনস্টল শেষে অনুমতিটি বন্ধ করুন।
3. Play Protect সতর্ক করলে ইনস্টল থামিয়ে APK-এর উৎস ও signer fingerprint যাচাই করুন। নিশ্চিতভাবে নিজের team-এর build হলে Android-এর installer যদি per-install **Install anyway** বিকল্প দেয়, তখনই সেটি বিবেচনা করুন। Play Protect পুরোপুরি বন্ধ করে রাখবেন না।
4. APK unsigned হলে certificate/parse error স্বাভাবিক: private signing config দিয়ে পুনরায় Release build করুন।

### “App not installed” troubleshooting

- পুরোনো SafeScreen ইনস্টল থাকলে update-এর signing certificate একই হতে হবে। Debug ও Release key আলাদা হতে পারে; mismatch হলে Android `INSTALL_FAILED_UPDATE_INCOMPATIBLE` দেখায়। পুরোনো app uninstall করলে local PIN/preferences/log হারাতে পারে—QA data মুছে যাবে ধরে নিন।
- APK signed কি না `apksigner verify --print-certs <apk>` দিয়ে দেখুন। `INSTALL_PARSE_FAILED_NO_CERTIFICATES` হলে unsigned APK; signing configure করে build করুন।
- ফোনের Android version `minSdk 29` বা তার বেশি, পর্যাপ্ত storage, এবং device policy install আটকাচ্ছে কি না যাচাই করুন।
- USB debugging-সহ install error দেখতে:

```powershell
adb install -r app/build/outputs/apk/release/app-release.apk
```

ADB-এর পূর্ণ `INSTALL_FAILED_*` ফল দেখে কারণ আলাদা করুন। `-d` দিয়ে version downgrade জোর করে এড়িয়ে চলবেন না।

## ২. প্রথম চালু ও Permission Hub

1. App data পরিষ্কার করে SafeScreen চালু করুন। Splash-এর পর onboarding slides দেখে **Get Started** চাপুন।
2. Permission Hub-এ পাঁচটি permission card দেখুন: **Display Over Other Apps**, **Screen Capture**, **Audio Playback Capture**, **Accessibility Service**, **Foreground Notification**।
3. প্রতিটি card-এর action চাপুন; OS screen-এ permission দিন, app-এ ফিরে status **Granted** হয়েছে কি না দেখুন। Accessibility-এর জন্য Settings → Accessibility → SafeScreen → Use service চালু করে ফিরে আসুন।
4. **Screen Capture**-এ Android-এর MediaProjection consent dialog-এ শুরু অনুমোদন করুন। Cancel করলে এটি Granted হওয়া বা Shield Active দেখানো উচিত নয়।
5. **Allow/Continue** action কেবল প্রয়োজনীয় permission সব Granted হলে Home-এ নেবে। Battery optimization prompt ঐচ্ছিক; এটি screen-capture consent-এর বিকল্প নয়।

**Expected:** Permission Hub-এ status ফিরে এসে refresh হবে। Notification tray-তে Shield active না থাকলে “SafeScreen Active” notification থাকবে না। Screen Capture revoke/cancel করলে protection শুরু হবে না।

## ৩. Shield, Visual Blur ও Blur Style

1. Home dashboard-এর বড় shield button চাপুন। Screen capture consent চাইলে অনুমতি দিন।
2. Home-এর status pill ও foreground notification-এ **AI SHIELD ACTIVE** দেখুন। Notification-এর **Stop Shield** চাপলে Home disarmed হবে এবং notification সরে যাবে।
3. আবার চালু করে **Visual Shield** switch অন রাখুন। একটি আলাদা app-এ অনুমতিপ্রাপ্ত test image/video দেখান; classifier high-confidence visual threat শনাক্ত করলে Live Blur overlay দেখা উচিত। Safe content-এ overlay না আসা স্বাভাবিক।
4. Home → **Visual Shield** card / palette button থেকে blur style sheet খুলুন। Frosted/Pixelated/Opaque style, intensity বদলে **Apply** করুন; DataStore-backed selection Home-এ ফিরে বজায় আছে কি না দেখুন।
5. Settings → **Manage App Whitelist**-এ একই test app exempt করুন। ওই app foreground-এ থাকলে visual scan/blur intervention বন্ধ হওয়া উচিত। Exemption সরিয়ে পুনরায় পরীক্ষা করুন।
6. Android notification-এর Stop, OS-এর screen-capture stop, এবং process restart আলাদা করে পরীক্ষা করুন। Restart-এর পরে পুরোনো grant ব্যবহার করে service Active হওয়া উচিত নয়; Main screen থেকে fresh screen-capture consent চাইবে।

**Expected:** Service capture সফল হওয়ার পরেই Active status আসে। Capture failure/revoke-এ service ও Home inactive হয়। App restart-এ fresh system consent প্রয়োজন।

## ৪. Audio Playback Capture ও Mute

1. Permission Hub-এ **Audio Playback Capture**/`RECORD_AUDIO` অনুমতি দিন। Home-এ Shield চালু করুন এবং **Audio Mute** switch অন রাখুন।
2. এমন media app ব্যবহার করুন যার audio capture policy playback capture অনুমোদন করে। অনুমতিপ্রাপ্ত, classifier-এর জন্য স্পষ্ট test audio চালান; অন্য app-এর audio policy capture আটকালে সেটিকে test failure ধরে নেবেন না।
3. Classifier audio threat শনাক্ত করলে Home telemetry/notification পরিবর্তন এবং threat log দেখুন। App music stream mute করলে ফোনের media volume indicator/state পর্যবেক্ষণ করুন।
4. Safe audio, whitelist করা app, Audio Mute switch off, এবং Stop Shield—প্রতিটি অবস্থায় unmute/স্বাভাবিক audio ফিরে আসে কি না দেখুন।

**Expected:** Audio stream কেবল capture-যোগ্য source ও enabled guard-এর ক্ষেত্রে বিশ্লেষিত হবে। Intervention-এর পরে safe state বা shield stop-এ volume restore হওয়া উচিত। অন্য app-এর capture-block policy থাকলে audio event না-ও আসতে পারে।

## ৫. PIN, Biometric ও Lockout

1. Settings খুলে **PIN Lock** enable চাপুন। আগে PIN না থাকলে **Create a 4-Digit PIN** flow-এ PIN লিখে দ্বিতীয়বার confirm করুন। Success হলে ফিরে এসে PIN enabled হয়েছে কি না যাচাই করুন। কোনো default PIN নেই।
2. **Change PIN** চাপুন। নতুন PIN field আসার আগে বর্তমান PIN দিন, অথবা configured/available হলে AndroidX `BiometricPrompt`-এ authenticate করুন। ভুল পুরোনো PIN দিলে নতুন PIN enrollment শুরু হওয়া চলবে না।
3. Biometric পরীক্ষার আগে OS Settings-এ fingerprint/face enroll করুন এবং SafeScreen-এর Security Lock screen খুলুন। BiometricPrompt এলে সফল authentication-এর পর unlock/change flow এগোনো উচিত; Cancel বা failed biometric-এ PIN বদলানোর অনুমতি দেওয়া যাবে না।
4. PIN entry screen-এ 4-digit PIN ভুলভাবে পাঁচবার দিন। পঞ্চম ভুলের পর 30-second countdown দেখা এবং PIN input বন্ধ থাকা উচিত।
5. Countdown চলাকালে screen থেকে বেরিয়ে আবার খুলুন; QA device-এ process restart করে পুনরায় চেষ্টা করুন। সময় শেষ না হওয়া পর্যন্ত lockout বহাল থাকবে; সময় শেষে সঠিক PIN গ্রহণ হবে।
6. Security screen-কে ছোট portrait, landscape ও বড় font-এ পরীক্ষা করুন; scroll করে keypad ও footer ব্যবহার করা যায় কি না দেখুন।

**প্রত্যাশিত ফল:** PIN hash per-PIN salt-সহ PBKDF2-HMAC-SHA256 হিসেবে সংরক্ষিত হয়; পুরোনো PIN যাচাই ছাড়া Change PIN এগোয় না। Restart-এর পরও lockout বহাল থাকে। Biometric hardware/enrollment না থাকলে পরীক্ষাটি N/A হিসেবে চিহ্নিত করুন।

## ৬. Dynamic App Whitelist

1. Settings → **Manage App Whitelist** খুলুন। তালিকায় PackageManager থেকে পাওয়া launchable installed apps দেখা যাবে; কোনো demo banking app আগে থেকে exempt থাকবে না।
2. Search field-এ app name/package লিখুন, category chips বদলান, তারপর app-এর switch অন করে count বাড়ে কি না দেখুন।
3. Screen বন্ধ করে আবার খুলুন—নির্বাচন Room database-এ টিকে থাকতে হবে। নতুন installed launcher app install করে তালিকায় দেখা যায় কি না যাচাই করুন; uninstall করা app-এর row পরের sync-এ সরে যাবে।
4. একটি app whitelist করে সেটিকে foreground-এ আনুন, তারপর shield test করুন। Whitelisted package AI scan থেকে exempt হবে। Switch off করে আবার দেখুন।
5. Foreground package অজানা/null হলে সেটিকে exemption ধরা যাবে না; AI scan fail-safe হিসেবে চালু থাকবে।

**Expected:** Newly discovered apps default protected (`not whitelisted`); user নিজে যেকোনো তালিকাভুক্ত app exempt করতে পারে। Android package visibility-এর কারণে তালিকাটি launcher-visible apps-এ সীমিত, সব hidden/system package নয়।

## ৭. Reels & Shorts blocker / Focus

1. Accessibility service enable আছে এবং overlay permission দেওয়া আছে নিশ্চিত করুন। Home-এ **Reels Blocker** অন করুন, অথবা bottom navigation → **Focus** → **Reels & Shorts Control** খুলে **Strict Mode** অন করুন।
2. Instagram Reels, YouTube Shorts, TikTok-এ target feed খুলুন। Accessibility event/feed signature recognize হলে Home usage count feed active থাকাকালীন প্রতি minute বাড়বে. TikTok detection broad; Instagram/YouTube feed detection app-version/layout-এর ওপর নির্ভরশীল.
3. Limit control-এর minimum 15 minutes সেট করুন (UI slider-এ 15-minute steps)। Limit পেরোলে full-screen **Focus Time Active** intervention দেখা এবং Activity Timeline-এ `REELS_LIMIT` event আসা উচিত। Facebook-কে Add App দিয়ে যোগ করলে সেটিও পরীক্ষা করা যায়।
4. Feed থেকে বের হলে usage ticker থামা উচিত। Strategy-তে বর্তমানে **Hard Lockout**-ই কার্যকর; emergency 2-minute pass PIN চায় না, এটি 2 মিনিট snooze দেয়।
5. স্থানীয় দিন বদলের পর/পরদিন খুললে usage 0-তে ফেরে কি না পরীক্ষা করুন। Force-stop/restart-এর পর অন্যান্য preferences অক্ষত থাকতে হবে।

**Expected:** Accessibility permission ছাড়া feed detection/intervention চলবে না। App disabled বা Strict Mode off থাকলে overlay আসবে না। OS/app UI পরিবর্তনে signature detection ব্যর্থ হতে পারে; supported app-এ বারবার ব্যর্থ হলে bug হিসেবে নথিভুক্ত করুন।

## ৮. Activity Timeline ও Stats

1. উপরের Visual/Audio/Reels পরীক্ষায় অন্তত একটি বাস্তব event তৈরি করুন। Bottom navigation → **Stats** খুলুন; **Today / 7 Days / 30 Days** বেছে chart, counts, category breakdown ও top apps দেখুন।
2. **Detailed Activity Log**/See All খুলুন। Event category filter (Visual Blur, Audio Guard, Reels Limit) এবং date group expand/collapse পরীক্ষা করুন।
3. Log item-এ timestamp, source app, category, action/confidence আছে কি না দেখুন। AI log rate-limit-এর কারণে 2.5 seconds-এর মধ্যে হওয়া একাধিক event-এর একটি মাত্র লেখা হতে পারে।
4. Activity Timeline-এ **Clear Log** চাপুন, confirmation cancel করে আবার খুলুন—data থাকা উচিত। Confirm করলে logs মুছবে; পরে Stats counts শূন্য হবে।
5. Settings → Data & Storage → **Clear Threat History** পরীক্ষা করুন। Confirm-এর পর log/stats মুছবে; whitelist, PIN, reels usage/preferences অক্ষত থাকবে।

**প্রত্যাশিত ফল:** Clean install-এ counts 0 ও list খালি থাকবে—demo metrics আশা করবেন না। Stats ও logs local Room data ব্যবহার করে।

## ৯. Settings, Focus Schedule ও navigation

- Bottom bar-এ Home, Stats, Focus, Settings-এর মধ্যে যাতায়াত এবং Back/forward পরীক্ষা করুন; crash বা ভুল selected tab থাকা চলবে না।
- Settings-এ AI sensitivity, performance profile, Visual/Audio/Reels toggle বদলে screen আবার খুলুন; নির্বাচন persist হয়েছে কি না দেখুন।
- **Focus Time Schedules**-এ Add Custom Focus Block চাপুন, title/time/day বদলে Save করুন; schedule তালিকায় দেখা, enable/disable ও delete পরীক্ষা করুন।
- Privacy Policy/Licenses খুলে Android Back দিয়ে Settings-এ ফিরুন।
- “Prevent Uninstallation” কেবল preference সংরক্ষণ করে; Device Owner mode ছাড়া সাধারণ app uninstall আটকাতে পারে না। শুধু toggle persistence পরীক্ষা করুন; uninstall আটকানো pass criteria নয়।

## ১০. Final QA checklist ও bug নথিভুক্তি

প্রতিটি flow-এর জন্য device model, Android/API, app version, build type, permission state, steps, expected/actual ফল, logcat excerpt ও screenshot/video লিখে রাখুন। Crash হলে `adb logcat -d -t 2000` সংগ্রহ করুন। Pass, Fail, Blocked, N/A আলাদা করে চিহ্নিত করুন; AI classifier-এর একবারের ভুল detection-কে নিশ্চিত bug বলবেন না—পুনরায় ঘটানোর প্রমাণ যোগ করুন।

Release acceptance-এর আগে signed APK install/update, v1→v2 Room migration, PIN lockout restart, MediaProjection revoke/restart, overlay, audio capture, accessibility, landscape/large-font UI এবং OEM battery behavior বাস্তব device-এ নথিভুক্ত করে sign-off করুন।
