# SafeScreen — Master UI/UX Screen Design & Production Implementation Guide
> **Document Purpose:**  
> এই ফাইলটি SafeScreen অ্যান্ড্রয়েড অ্যাপ্লিকেশনের সমস্ত স্ক্রিনের ভিজ্যুয়াল রেফারেন্স, ডিজাইন আর্কিটেকচার এবং Kotlin + Jetpack Compose-এ পিক্সেল-পারফেক্ট ইমপ্লিমেন্টেশনের জন্য একটি সম্পূর্ণ এবং স্বয়ংসম্পূর্ণ (Self-Contained) মাস্টার প্রম্পট বুক।  
> প্রতিটি স্ক্রিনের জন্য আলাদা কোডিং এজেন্ট প্রম্পট (Prompt B) এমনভাবে সমৃদ্ধ করা হয়েছে যাতে যেকোনো এআই এজেন্ট বা মানব ডেভেলপার শুধু সেই নির্দিষ্ট প্রম্পটটি পড়েই স্ক্রিনের উদ্দেশ্য, ভিজ্যুয়াল রেফারেন্স, স্টেট ম্যানেজমেন্ট (`UiState`), আর্কিটেকচারাল কানেকশন, ওএস সার্ভিস ইন্টিগ্রেশন এবং টেস্টিং ক্রাইটেরিয়া স্পষ্টভাবে বুঝতে পারে।

---

# অংশ ১: এআই ডিজাইনারের জন্য মাস্টার কনটেক্সট ইনিশিয়ালাইজেশন
*(ChatGPT-তে নতুন কনভারসেশন শুরু করার সময় এই প্রম্পটটি একবার দিতে হবে।)*

```markdown
Role & Persona:
You are an Elite Principal Mobile UI/UX Designer and Lead Android Systems Architect specializing in production-grade mobile app interfaces. You design high-fidelity, polished Android apps adhering strictly to Google Material 3 Design Guidelines, with a futuristic "Cyber-Dark Privacy" design language.

Project Context — SafeScreen:
SafeScreen is an advanced, Privacy-First Digital Wellbeing and On-Device Content Filtering application for Android (Min SDK 29, Android 10+; Target SDK 35, Android 15).
Core Philosophy: 100% On-Device AI, Absolute Zero Cloud Upload, No Third-Party Trackers.

Core Problems Addressed:
1. Visual Protection: Detects explicit/NSFW images and video frames in real-time using on-device TFLite models and instantly applies a frosted-glass blur overlay.
2. Audio Protection: Detects background music or unwanted sound in supported media using AudioPlaybackCapture API and automatically mutes/suppresses audio via AudioManager.
3. Reels & Shorts Control: Curbs infinite doomscrolling in Facebook Reels, Instagram Reels, YouTube Shorts, and TikTok using daily time limits (UsageStatsManager) and Focus Intervention Overlays (AccessibilityService + WindowManager).

Design Identity & Visual Tokens:
- Aesthetic: Cyberpunk meets Apple Cleanliness. High contrast, neon accents, dark surfaces, tactile micro-interactions.
- Color Tokens:
  * Backgrounds: Deep Cyber Black (`#07090E`), Dark Obsidian Surface (`#0F141F`), Elevated Slate (`#161D2D`), Surface Highlight (`#1E283D`).
  * Neon Accents: Electric Cyan (`#00E5FF`), Bio-Green (`#00FF9D`), Neon Pink (`#FF2A6D`), Neon Purple (`#9D4EDD`), Warning Yellow (`#FFD166`).
  * Typography & Grays: Pure White (`#F1F5F9`), Slate Gray (`#94A3B8`), Muted Slate (`#64748B`).
  * Glassmorphism: Frosted borders (`1dp` solid with `0x33FFFFFF`), glass surface gradient (`0x1A00E5FF`), subtle drop shadows.
- Typography: Roboto / Inter style, high-legibility sans-serif, bold numbers for metric counters.
```

---

# অংশ ২: ব্যবহারকারী যাত্রাপথ ও স্ক্রিন ডিরেক্টরি ম্যাপিং

| ক্রম | মডিউল ও স্ক্রিনের নাম | স্ক্রিনের ধরন | টার্গেট Kotlin ফাইল পাথ | সংরক্ষিত ভিজ্যুয়াল রেফারেন্স ইমেজ |
| :---: | :--- | :--- | :--- | :--- |
| **01** | `01_SplashScreen` | Full Screen | `presentation/screens/splash/SplashScreen.kt` | `Screen Workflow/01_Onboarding/SafeScreen Cybersecurity Onboard Due.png` (Left) |
| **02** | `02_OnboardingPager` | Full Screen | `presentation/screens/onboarding/OnboardingScreen.kt` | `Screen Workflow/01_Onboarding/SafeScreen Cybersecurity Onboard Due.png` (Right) |
| **03** | `03_PermissionHub` | Full Screen | `presentation/screens/permission/PermissionHubScreen.kt` | `Screen Workflow/02_Permissions/SafeScreen Permission Flow Mocku.png` (Left) |
| **04** | `04_BatteryOptimizationDialog` | Modal Bottom Sheet | `presentation/screens/permission/BatteryOptimizationDialog.kt` | `Screen Workflow/02_Permissions/SafeScreen Permission Flow Mocku.png` (Right) |
| **05** | `05_HomeScreen` | Core Screen | `presentation/screens/home/HomeScreen.kt` | `Screen Workflow/03_Home_Dashboard/SafeScreen Neon Shield Dashboard.png` (Left) |
| **06** | `06_BlurStylePreviewSheet` | Modal Bottom Sheet | `presentation/screens/home/BlurStylePreviewSheet.kt` | `Screen Workflow/03_Home_Dashboard/SafeScreen Neon Shield Dashboard.png` (Right) |
| **07** | `07_ReelsControlScreen` | Full Screen | `presentation/screens/focus/ReelsControlScreen.kt` | `Screen Workflow/04_Reels_And_Focus/Neon SafeScreen Focus Dashboard.png` (Left) |
| **08** | `08_FocusScheduleScreen` | Full Screen | `presentation/screens/focus/FocusScheduleScreen.kt` | `Screen Workflow/04_Reels_And_Focus/Neon SafeScreen Focus Dashboard.png` (Right) |
| **09** | `09_StatsScreen` | Core Screen | `presentation/screens/stats/StatsScreen.kt` | `Screen Workflow/05_Stats_And_Analytics/SafeScreen Neon Protection Dashb.png` (Left) |
| **10** | `10_DetailedActivityLog` | Full Screen | `presentation/screens/stats/DetailedActivityLogScreen.kt` | `Screen Workflow/05_Stats_And_Analytics/SafeScreen Neon Protection Dashb.png` (Right) |
| **11** | `11_SettingsScreen` | Core Screen | `presentation/screens/settings/SettingsScreen.kt` | `Screen Workflow/06_Settings_And_Security/SafeScreen Neon Security UI Mock.png` (Left) |
| **12** | `12_AppWhitelistScreen` | Full Screen | `presentation/screens/settings/AppWhitelistScreen.kt` | `Screen Workflow/06_Settings_And_Security/SafeScreen Neon Security UI Mock.png` (Center) |
| **13** | `13_SecurityLockScreen` | Full Screen / Sheet | `presentation/screens/settings/SecurityLockScreen.kt` | `Screen Workflow/06_Settings_And_Security/SafeScreen Neon Security UI Mock.png` (Right) |
| **14** | `14_LiveBlurOverlayView` | WindowManager Overlay | `services/overlay/LiveBlurOverlayView.kt` | `Screen Workflow/07_Overlays_And_Dialogs/SafeScreen Protection and Focus .png` (Left) |
| **15** | `15_FocusInterventionOverlay` | WindowManager Overlay | `services/overlay/FocusInterventionOverlay.kt` | `Screen Workflow/07_Overlays_And_Dialogs/SafeScreen Protection and Focus .png` (Right) & `Neon Focus Time Alert Overlay.png` |
| **16** | `16_EmptyAndErrorStates` | Composable Components | `presentation/components/states/SafeScreenStates.kt` | `Screen Workflow/08_Error_And_Empty_States/SafeScreen Neon Status Dashboard.png` & `SafeScreen Activity Log_ All Cle.png` |

---

# অংশ ৩: সমৃদ্ধ ও স্বয়ংসম্পূর্ণ কোডিং এজেন্ট প্রম্পটসমূহ

---

## 📁 মডিউল ০১: অনবোর্ডিং ও স্প্ল্যাশ ফ্লো (Onboarding & Splash Flow)

### স্ক্রিন ০১: Splash Screen
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/presentation/screens/splash/SplashScreen.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/01_Onboarding/SafeScreen Cybersecurity Onboard Due.png` (বাম পাশের স্ক্রিন)

#### 📝 প্রম্পট ০১-B: Splash Screen ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement the high-fidelity SplashScreen in Kotlin & Jetpack Compose adhering to the Cyber-Dark theme and Clean Architecture.

Context & Reference:
Inspect 'Screen Workflow/01_Onboarding/SafeScreen Cybersecurity Onboard Due.png' (Left Screen).
The screen serves as the brand launchpad, displays neural core loading status, and handles routing to either Onboarding or Home based on DataStore preferences.

UI Structure & Components:
1. Container: Box with fillMaxSize(), background DeepBlack (#07090E) with subtle radial cyan glow behind the logo.
2. Center Hero:
   - 3D-stylized Shield graphic with inner glowing padlock.
   - Animate outer aura breathing pulse using rememberInfiniteTransition (scale: 1.0f -> 1.08f, alpha: 0.6f -> 1.0f).
3. Brand Typography:
   - "SafeScreen": "Safe" in TextPrimary (#F1F5F9), "Screen" in NeonCyan (#00E5FF), headlineLarge with bold tracking.
   - Subtitle: "Your Digital Purity & Focus Guard" in TextSecondary (#94A3B8), bodyMedium.
4. Loading Indicator:
   - Sleek pill progress bar (height: 6dp, width: 220dp, NeonCyan with animated shimmer).
   - Label: "Initializing Neural Core (100% On-Device)..." in TextMuted (#64748B), labelSmall.
5. Bottom Decorative Waves: Subtle gradient curve in NeonCyan & Bio-Green (#00FF9D).

Architecture, State & ViewModel:
- ViewModel: SplashViewModel with StateFlow<SplashUiState>.
- Sealed Interface SplashUiState:
  * data object Loading : SplashUiState
  * data class NavigateTo(val route: String) : SplashUiState
- Logic: Check DataStore onboardingCompleted preference and permission status:
  * If onboardingCompleted == false -> Navigate to Screen.Onboarding.route
  * If permissions missing -> Navigate to Screen.PermissionHub.route
  * If all granted -> Navigate to Screen.Home.route
- Delay: Minimum display time 1200ms for smooth visual experience.

Responsive & Layout Guidelines:
- Handle WindowInsets.systemBars to keep content centered regardless of punch-hole camera or status bar.
- Provide @Preview(showBackground = true) with Theme wrapper.

Acceptance Criteria:
- Shield pulses smoothly without jank.
- Clean routing without flashing wrong screens.
- Zero layout overflow on both 5.5" and 6.7"+ devices.
```

---

### স্ক্রিন ০২: Onboarding Pager
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/presentation/screens/onboarding/OnboardingScreen.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/01_Onboarding/SafeScreen Cybersecurity Onboard Due.png` (ডান পাশের স্ক্রিন)

#### 📝 প্রম্পট ০২-B: Onboarding Pager ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement the interactive 3-page OnboardingPager in Kotlin & Jetpack Compose, showcasing the three core pillars and the 100% offline privacy pledge.

Context & Reference:
Inspect 'Screen Workflow/01_Onboarding/SafeScreen Cybersecurity Onboard Due.png' (Right Screen).

UI Structure & Components:
1. Top Bar:
   - "Skip >" text button in top-end corner (navigates directly to PermissionHub).
2. Central Feature Card (GlassmorphicCard):
   - 3 visual columns/pillars:
     a) "Visual Shield" (Eye icon in glowing cyan aura, subtitle: "Blur NSFW Content").
     b) "Smart Audio Guard" (Audio waveform icon with mute badge in bio-green, subtitle: "Auto-Mute Unwanted Sound").
     c) "Doomscroll Breaker" (Hourglass icon in neon purple, subtitle: "Stop Infinite Reels & Shorts").
3. Headline & Copy:
   - Title: "Reclaim Your Focus & Purity" ("Reclaim Your" in TextPrimary, "Focus & Purity" in NeonCyan gradient).
   - Body: "On-device AI actively shields your screen and audio from unwanted content. Zero data ever leaves your phone."
4. Page Indicators:
   - 3 rounded pill dots; active page is elongated in NeonCyan, inactive in DarkSurfaceElevated.
5. Trust Badge (Frosted Pill):
   - Lock icon in Bio-Green: "100% Offline AI • Zero Cloud Upload • No Trackers".
6. Primary Action:
   - Full-width pill button (height: 56dp, shape: 28dp) reading "Get Started" (or "Next" on earlier slides) in solid NeonCyan with DeepBlack bold text.

Architecture & State:
- Use rememberPagerState(pageCount = { 3 }).
- State: OnboardingViewModel exposing StateFlow<Int> for currentPage.
- On "Get Started" or "Skip":
  * Persist onboardingCompleted = true in DataStore.
  * Navigate to Screen.PermissionHub.route with popUpTo(Screen.Onboarding.route) { inclusive = true }.

Acceptance Criteria:
- Smooth horizontal swipe gesture between pages.
- "Get Started" appears on final slide; "Next" on slides 1 & 2.
- DataStore accurately records onboarding completion.
```

---

## 📁 মডিউল ০২: পারমিশন ম্যানেজমেন্ট হাব (Permissions Flow)

### স্ক্রিন ০৩: Permission Hub Screen
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/presentation/screens/permission/PermissionHubScreen.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/02_Permissions/SafeScreen Permission Flow Mocku.png` (বাম পাশের স্ক্রিন)

#### 📝 প্রম্পট ০৩-B: Permission Hub ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement the PermissionHubScreen in Kotlin & Jetpack Compose to manage Android system permissions with full transparency and real-time status updates.

Context & Reference:
Inspect 'Screen Workflow/02_Permissions/SafeScreen Permission Flow Mocku.png' (Left Screen).
Permissions Required:
1. Display Over Other Apps (SYSTEM_ALERT_WINDOW)
2. Screen Capture (MediaProjection)
3. Audio Playback Capture (RECORD_AUDIO)
4. Accessibility Service (SafeScreenAccessibilityService)
5. Foreground Notification (POST_NOTIFICATIONS)

UI Components & Cards:
1. Header: Back button, Title "System Permissions", glowing shield badge icon.
2. Explanatory Banner: "To inspect screen frames and audio on-device, SafeScreen requires specific Android permissions. Your data is strictly private." (Privacy text highlighted in NeonCyan).
3. Permission Card List (LazyColumn):
   - Reusable Component: PermissionCard(title, codeName, description, icon, isGranted, onActionClick).
   - If Granted: Show "✔ Granted" with Bio-Green text and icon.
   - If Action Required: Show NeonCyan pill button ("Grant Access" / "Enable").
4. Bottom Sticky Action Bar:
   - "Enter Shield Hub" button (Shield icon + text).
   - Disabled (dark slate with muted text) if critical permissions (Overlay, MediaProjection) are missing.
   - Enabled (NeonCyan solid) when all mandatory permissions are satisfied.

Architecture & Integration:
- PermissionViewModel exposing StateFlow<PermissionHubUiState>.
- Check actual Android system states:
  * Settings.canDrawOverlays(context)
  * ContextCompat.checkSelfPermission(POST_NOTIFICATIONS)
  * AccessibilityManager enabled services check
  * PowerManager.isIgnoringBatteryOptimizations
- Handle Activity Result Launchers for Settings intents gracefully.
- Refresh states on Lifecycle.Event.ON_RESUME.

Acceptance Criteria:
- Cards instantly update to "✔ Granted" upon returning from Android Settings.
- Cannot advance to Home until mandatory permissions are active.
- Non-blocking explanation dialogs explaining why each permission is essential.
```

---

### স্ক্রিন ০৪: Battery Optimization Exemption Dialog
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/presentation/screens/permission/BatteryOptimizationDialog.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/02_Permissions/SafeScreen Permission Flow Mocku.png` (ডান পাশের স্ক্রিন)

#### 📝 প্রম্পট ০৪-B: Battery Optimization Dialog ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement the BatteryOptimizationDialog modal bottom sheet in Jetpack Compose to prevent Android from aggressively killing the on-device AI foreground service.

Context & Reference:
Inspect 'Screen Workflow/02_Permissions/SafeScreen Permission Flow Mocku.png' (Right Screen).

UI Components:
1. ModalBottomSheet with shape: RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), containerColor = DarkSurfaceElevated (#161D2D).
2. Top frosted drag handle.
3. Hero Icon: Glowing amber-yellow battery with lightning bolt surrounded by orbital neon rings.
4. Typography:
   - Title: "Unrestricted Battery Access" (headlineSmall, TextPrimary).
   - Body: "Android aggressively kills background machine learning tasks. To ensure your Shield never sleeps when explicit content appears, please exclude SafeScreen from battery optimization."
5. Actions:
   - Primary: "Allow Unrestricted" (solid NeonCyan pill button). Triggers Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS.
   - Secondary: "Later" (TextButton in TextSecondary). Dismisses sheet.

Acceptance Criteria:
- Opens automatically if PowerManager.isIgnoringBatteryOptimizations is false after granting overlay permission.
- Dismissing gracefully returns to PermissionHub.
```

---

## 📁 মডিউল ০৩: মূল হোম ড্যাশবোর্ড ও ব্লার প্রিভিউ (Home Dashboard Flow)

### স্ক্রিন ০৫: Home Dashboard Screen
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/presentation/screens/home/HomeScreen.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/03_Home_Dashboard/SafeScreen Neon Shield Dashboard.png` (বাম পাশের স্ক্রিন)

#### 📝 প্রম্পট ০৫-B: Home Dashboard ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement SafeScreen's flagship HomeDashboard in Kotlin & Jetpack Compose, featuring the iconic pulsing central ShieldButton, real-time metric cards, quick feature toggles, and floating bottom navigation.

Context & Reference:
Inspect 'Screen Workflow/03_Home_Dashboard/SafeScreen Neon Shield Dashboard.png' (Left Screen).

UI Structure & Layout:
1. Top Bar:
   - Left: Logo icon + "SafeScreen" typography.
   - Right: "SHIELD ACTIVE" / "DISARMED" pill badge with live pulsing dot + Settings gear icon button.
2. Central Hero: ShieldButton Component:
   - 160dp circular core with 3D shield & padlock.
   - When Active: Concentric pulsing radial glow (NeonCyan & Bio-Green), animated with rememberInfiniteTransition.
   - Status text: "PROTECTION ENGAGED" in NeonGreen bold, sub-label: "Analyzing screen & audio locally (5 FPS)".
   - When Disarmed: Muted slate border, text: "SHIELD DISARMED", sub-label: "Tap to activate protection".
3. Metric Cards Row (2 Frosted GlassmorphicCards side-by-side):
   - Card 1: "Explicit Blurred", Big Number "38" (NeonPink), "Today's interventions", right chevron. Clicking navigates to Stats.
   - Card 2: "Music Muted", Big Number "14" (NeonCyan), "Audio tracks silenced", right chevron.
4. Focus Banner Card:
   - "Reels & Shorts Usage: 18 / 30 mins used today" with a linear neon purple progress bar at 60%.
5. Quick Feature Toggles (Row of 3 cards):
   - [Visual Shield] (Eye icon, green switch toggle)
   - [Audio Mute] (Mute icon, cyan switch toggle)
   - [Reels Blocker] (Hourglass icon, purple switch toggle)
6. Floating Bottom Navigation Bar:
   - Glassmorphic floating pill with 4 items: Home (active NeonCyan), Stats, Focus, Settings.

Architecture & State:
- HomeViewModel exposing StateFlow<HomeUiState>:
  * isShieldActive: Boolean
  * blurredTodayCount: Int
  * mutedTodayCount: Int
  * reelsMinutesUsed: Int
  * reelsMinutesLimit: Int
  * isVisualShieldEnabled: Boolean
  * isAudioMuteEnabled: Boolean
  * isReelsBlockerEnabled: Boolean
- Wiring:
  * Toggling master shield calls StartShieldUseCase / StopShieldUseCase.
  * If PIN lock is enabled, intercept disarm with SecurityLockScreen.
  * Tapping metrics navigates to Screen.Stats.route.

Acceptance Criteria:
- Pulse animation is smooth (60 FPS) without recomposition bottlenecks.
- Toggles instantly update local DataStore and notify ShieldProtectionService.
- Seamless bottom navigation switching.
```

---

### স্ক্রিন ০৬: Blur Style Preview Sheet
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/presentation/screens/home/BlurStylePreviewSheet.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/03_Home_Dashboard/SafeScreen Neon Shield Dashboard.png` (ডান পাশের স্ক্রিন)

#### 📝 প্রম্পট ০৬-B: Blur Style Preview Sheet ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement the BlurStylePreviewSheet bottom sheet in Jetpack Compose to let users customize the visual filter applied to explicit content.

Context & Reference:
Inspect 'Screen Workflow/03_Home_Dashboard/SafeScreen Neon Shield Dashboard.png' (Right Screen).

UI Components:
1. Header: Palette icon + "Shield Blur Customization", subtitle: "Choose your preferred blur style for explicit content."
2. 3 Style Selection Cards:
   - Style 1: "Frosted Glass" (Smooth Gaussian blur) [Selected indicator with checkmark]
   - Style 2: "Pixelated Censor" (8-bit digital blocks)
   - Style 3: "Opaque Dark Mask" (Total black shield block)
3. Slider Control:
   - "Blur Intensity" label with percentage counter (e.g. 85%).
   - Custom NeonCyan slider with smooth tactile drag.
4. Interactive Split Comparison View:
   - Left half: Original sample graphic (landscape).
   - Right half: Simulated real-time blur applied according to selected style & intensity.
   - Center draggable split handle (<> icon).
5. Bottom Action: "✔ Apply Changes" (NeonCyan pill button).

State & Persistence:
- Persist BlurStyle (GAUSSIAN, PIXELATED, OPAQUE) and blurIntensity (0.1f - 1.0f) in DataStore.
- Inform LiveBlurOverlayManager of the new configuration.

Acceptance Criteria:
- Split comparison responds smoothly to slider drag.
- Selected style persists across app restarts.
```

---

## 📁 মডিউল ০৪: রিলস ও ডিজিটাল ওয়েলবিয়িং (Reels & Focus Flow)

### স্ক্রিন ০৭: Reels & Shorts Control Screen
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/presentation/screens/focus/ReelsControlScreen.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/04_Reels_And_Focus/Neon SafeScreen Focus Dashboard.png` (বাম স্ক্রিন) & `photo_6284782712203710858_w.jpg`

#### 📝 প্রম্পট ০৭-B: Reels & Shorts Control ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement the Reels & Shorts Control screen in Kotlin & Jetpack Compose to configure short-video platform monitoring, daily allowances, and intervention strategies.

Context & Reference:
Inspect 'Screen Workflow/04_Reels_And_Focus/Neon SafeScreen Focus Dashboard.png' (Left Screen) and 'photo_6284782712203710858_w.jpg'.

UI Structure & Components:
1. Top Bar: Back arrow, Title "Reels & Shorts Control", Info icon dialog trigger.
2. OS Notice Card (Warning Amber):
   - "Android Sandboxing Notice: App UI cannot be deleted. SafeScreen uses Accessibility Overlays & App-Level time limits to protect you."
3. Monitored Platforms Section:
   - App List with official logos and independent switches:
     * Instagram (Reels) — Switch ON
     * YouTube (Shorts) — Switch ON
     * TikTok — Switch ON
     * Facebook (Reels) — Switch ON
   - "+ Add Custom App" dashed button (opens app selector dialog for other installed short-form apps).
4. Daily Short-Video Allowance Gauge:
   - Semi-circular arc gauge or stepped slider with presets: [15 min] [30 min (Selected)] [45 min] [1 hr] [Complete Lockout].
   - Large numeric readout: "30 min per day".
5. Intervention Strategy (Radio Card Group):
   - Option 1: "Strict Focus Overlay" (Instantly blocks screen when Reels section is detected) [Selected]
   - Option 2: "Gentle Nudge" (Floating timer reminder)
   - Option 3: "Auto-Home Exit" (Performs back/home gesture automatically)
6. Bottom Action: "✔ Save Settings" (NeonCyan pill button).

Architecture & Integration:
- ViewModel: ReelsControlViewModel managing ReelsConfig data class.
- Persistence: DataStore Preferences & Room DB.
- AccessibilityService Hook: Service queries isAppMonitored(packageName) and time limit before displaying overlay.

Acceptance Criteria:
- Selecting "Complete Lockout" sets allowance to 0 min.
- Custom app additions persist in Room DB.
```

---

### স্ক্রিন ০৮: Focus Schedule Screen
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/presentation/screens/focus/FocusScheduleScreen.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/04_Reels_And_Focus/Neon SafeScreen Focus Dashboard.png` (ডান পাশের স্ক্রিন)

#### 📝 প্রম্পট ০৮-B: Focus Schedule ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement the Focus Schedule Screen in Jetpack Compose to schedule recurring distraction-free blocks (such as bedtime, study hours, or prayer times) during which short-video platforms are completely locked out.

Context & Reference:
Inspect 'Screen Workflow/04_Reels_And_Focus/Neon SafeScreen Focus Dashboard.png' (Right Screen).

UI Components:
1. Top Bar: Back arrow, Title "Focus Time Schedules", Info icon.
2. Subtitle: "Completely block short-video platforms during essential life hours."
3. Preset & Custom Schedule Cards:
   - Card 1: "Night Purity & Sleep" (Crescent moon icon, 10:00 PM – 06:00 AM, badge: "✔ Active" Bio-Green, days: "Daily", toggle switch ON).
   - Card 2: "Deep Work / Study" (Graduation cap icon, 09:00 AM – 05:00 PM, badge: "✔ Active" NeonPurple, days: "Mon – Fri", toggle switch ON).
4. Day Selector: Pill buttons for [M] [T] [W] [T] [F] [S] [S].
5. Add Schedule Button: Dashed outline card "+ Add Custom Focus Block".
6. Inspiration Hero Graphic: Clock shield graphic with message: "Stay Focused. Live Healthier. Let SafeScreen help you build better habits."
7. Floating Action Button (FAB): NeonPurple (+) button for quick schedule addition.

Architecture:
- Room FocusScheduleEntity (id, title, startTime, endTime, daysOfWeek, isEnabled).
- DAO & Repository with reactive Flow<List<FocusSchedule>>.

Acceptance Criteria:
- Toggling a schedule immediately takes effect in the background check engine.
- Adding a new custom schedule with time picker works smoothly.
```

---

## 📁 মডিউল ০৫: অ্যানালিটিক্স ও হিস্ট্রি (Stats & Analytics Flow)

### স্ক্রিন ০৯: Protection Stats Screen
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/presentation/screens/stats/StatsScreen.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/05_Stats_And_Analytics/SafeScreen Neon Protection Dashb.png` (বাম পাশের স্ক্রিন)

#### 📝 প্রম্পট ০৯-B: Protection Stats Screen ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement the data-rich ProtectionStatsScreen in Kotlin & Jetpack Compose to visualize local offline filtering metrics without any cloud analytics.

Context & Reference:
Inspect 'Screen Workflow/05_Stats_And_Analytics/SafeScreen Neon Protection Dashb.png' (Left Screen).

UI Structure & Components:
1. Top Bar:
   - App icon + "Protection Insights".
   - Time Range Selector: Pill segment [Today] [7 Days (Active Cyan)] [30 Days].
2. Privacy Guarantee Banner:
   - Green-bordered frosted card: "🔒 100% Offline SQLite. All statistics exist solely on your local device."
3. Weekly Activity Overview Card:
   - Custom Compose Canvas Bar Chart:
     * Dual bars for each day (Mon - Sun): Pink bar for Blocked Frames, Cyan bar for Muted Audio.
     * Active glowing tooltip on selected day (e.g. "Thu: 42 Blurred, 18 Muted").
4. Category Breakdown Card:
   - Donut Chart / Segmented Progress Arc:
     * 55% Explicit Visuals Blurred (NeonPink)
     * 30% Background Music Muted (NeonCyan)
     * 15% Reels Sessions Intercepted (NeonPurple)
5. Top Intercepted Apps Card:
   - Ranked list with app icons, package names, event count, and proportional fill bars:
     * 1. Chrome (54 events)
     * 2. Instagram (32 events)
     * 3. Twitter / X (19 events)
6. Bottom Action: "View Detailed Event Log" (Card with chevron right).

Architecture & State:
- StatsViewModel collecting Flow<StatsUiState> from GetStatsUseCase.
- If event count is 0, render EmptyStatsView from Module 08.

Acceptance Criteria:
- Canvas charts animate on enter and handle touch selection smoothly.
- Zero external analytics SDKs used.
```

---

### স্ক্রিন ১০: Detailed Activity Log Screen
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/presentation/screens/stats/DetailedActivityLogScreen.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/05_Stats_And_Analytics/SafeScreen Neon Protection Dashb.png` (ডান পাশের স্ক্রিন)

#### 📝 প্রম্পট ১০-B: Detailed Activity Log ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement the chronological Activity Timeline in Jetpack Compose showing every local protection event with category badges, timestamps, confidence scores, and clear-history controls.

Context & Reference:
Inspect 'Screen Workflow/05_Stats_And_Analytics/SafeScreen Neon Protection Dashb.png' (Right Screen).

UI Components:
1. Top Bar: Back arrow, "Activity Timeline", Filter icon, "Clear Log" red trash button.
2. Grouped Feed (LazyColumn):
   - Group Headers: "Today • Oct 31, 2026", "Yesterday • Oct 30, 2026" with collapsible chevrons.
   - Event Card 1: Eye-slash in NeonPink -> "Explicit Visual Content Blurred", Subtitle: "Browser (com.android.chrome) • 03:42 PM", Pill badge: "94% NSFW score".
   - Event Card 2: Mute icon in NeonCyan -> "Background Music Silenced", Subtitle: "Shorts Feed • 02:15 PM", Pill badge: "45 sec muted".
   - Event Card 3: Lock in NeonPurple -> "Reels Limit Reached — Overlay Shown", Subtitle: "Instagram • 11:20 AM".
3. Clear Log Confirmation Dialog:
   - Warning triangle in WarningYellow: "Clear Activity Log? This will permanently remove all protection activity history from this device. Are you sure?"
   - Actions: "Clear Log" (NeonCyan button) & "Cancel".

Acceptance Criteria:
- Clicking Clear Log wipes Room StatEntity table and instantly switches UI to Screen 16 Empty State.
- Stable LazyColumn items prevent scroll lag.
```

---

## 📁 মডিউল ০৬: সেটিংস, হোয়াইটলিস্ট ও সিকিউরিটি লক (Settings & Security Flow)

### স্ক্রিন ১১: Main Settings Screen
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/presentation/screens/settings/SettingsScreen.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/06_Settings_And_Security/SafeScreen Neon Security UI Mock.png` (বাম পাশের স্ক্রিন)

#### 📝 প্রম্পট ১১-B: Settings Screen ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement the comprehensive Settings Screen in Jetpack Compose to manage AI sensitivity, sampling FPS, security toggles, and data hygiene.

Context & Reference:
Inspect 'Screen Workflow/06_Settings_And_Security/SafeScreen Neon Security UI Mock.png' (Left Screen).

UI Sections & Cards:
1. Section 1: "AI Detection Engine" (Chip icon, subtitle: "Control how strictly SafeScreen works"):
   - Sensitivity Level: 3-segmented pill [Strict] [Balanced (Default)] [Relaxed].
   - Performance Profile: 3-segmented pill [Battery Saver (3 FPS)] [Balanced (5 FPS)] [High Precision (8 FPS)].
2. Section 2: "Security & Protection":
   - "PIN & Biometric Lock" switch toggle.
   - "Prevent Uninstallation Assistance" switch toggle.
3. Section 3: "App Exclusions":
   - Card: "Manage App Whitelist (5 apps excluded)" with right chevron. Navigates to Screen 12.
4. Section 4: "Data & Storage":
   - Button: "Clear Offline Database Cache" with trash icon.
5. Section 5: "About":
   - App version "SafeScreen v1.0.0", Privacy Policy link, Open-source licenses link.

Architecture:
- SettingsViewModel syncing with DataStore Preferences.
- Changing FPS dynamically updates the background MediaProjection ImageReader frame rate.

Acceptance Criteria:
- All toggles react with zero lag and persist immediately.
```

---

### স্ক্রিন ১২: App Whitelist Screen
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/presentation/screens/settings/AppWhitelistScreen.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/06_Settings_And_Security/SafeScreen Neon Security UI Mock.png` (মাঝের স্ক্রিন)

#### 📝 প্রম্পট ১২-B: App Whitelist Screen ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement the App Whitelist Screen in Jetpack Compose to let users exempt trusted applications (such as banking, personal gallery, or office apps) from screen inspection and audio capture.

Context & Reference:
Inspect 'Screen Workflow/06_Settings_And_Security/SafeScreen Neon Security UI Mock.png' (Center Screen).

UI Components:
1. Top Bar: Back arrow, "App Whitelist", Shield icon.
2. Search Bar: Magnifying glass, "Search installed apps..." with live query filtering.
3. Informational Banner: "Whitelisted apps bypass all screen frame inspection and audio filtering."
4. App List (LazyColumn):
   - Items with App Icon, App Name, Package Name, "Exempted" / "Protected" status badge, and toggle switch.
   - Defaults: Banking apps pre-suggested for exemption.
5. Sticky Bottom Summary Bar:
   - Shield icon: "5 Apps in Whitelist. These apps are exempted from SafeScreen checks."

Integration:
- Fetch installed apps using PackageManager on Dispatchers.IO.
- Save whitelisted packages in Room WhitelistDao.
- ShieldProtectionService queries isPackageWhitelisted() in real-time.

Acceptance Criteria:
- Search filters 100+ installed apps smoothly with no UI thread freeze.
```

---

### স্ক্রিন ১৩: Security Lock Screen (PIN & Biometric)
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/presentation/screens/settings/SecurityLockScreen.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/06_Settings_And_Security/SafeScreen Neon Security UI Mock.png` (ডান পাশের স্ক্রিন)

#### 📝 প্রম্পট ১৩-B: Security Lock Screen ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement the Cyber-Dark PIN Pad and Biometric authentication gate in Jetpack Compose to guard against unauthorized shield disarming or settings tampering.

Context & Reference:
Inspect 'Screen Workflow/06_Settings_And_Security/SafeScreen Neon Security UI Mock.png' (Right Screen).

UI Components:
1. Center Hero: Glowing green/cyan padlock with circuit traces.
2. Header: "SafeScreen Locked", subtitle: "Enter your 4-digit PIN to modify settings or disarm shield".
3. PIN Indicator: 4 glowing circular dots (hollow when empty, filled neon when entered).
4. Glassmorphic Numeric Keypad:
   - Grid of buttons: [1] [2 ABC] [3 DEF] [4 GHI] [5 JKL] [6 MNO] [7 PQRS] [8 TUV] [9 WXYZ].
   - Bottom Row: [Fingerprint Icon] [0] [Backspace Icon].
5. "Forgot PIN?" text button at bottom.

Architecture & Security:
- BiometricPrompt integration using BIOMETRIC_STRONG or DEVICE_CREDENTIAL.
- PIN stored as cryptographic hash (SHA-256 with salt) in DataStore.
- Shake animation and haptic feedback on invalid PIN attempt.
- 5 failed attempts locks keypad for 30 seconds.

Acceptance Criteria:
- Biometric prompt triggers automatically on screen enter if enabled.
- Correct PIN returns onUnlockSuccess() callback.
```

---

## 📁 মডিউল ০৭: সিস্টেম ওভারলে ও ফ্লোটিং উইন্ডো (System Overlays Flow)

### স্ক্রিন ১৪: Live Visual Blur Overlay View
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/services/overlay/LiveBlurOverlayView.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/07_Overlays_And_Dialogs/SafeScreen Protection and Focus .png` (বাম পাশের স্ক্রিন)

#### 📝 প্রম্পট ১৪-B: Live Blur Overlay ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement the system-level floating blur overlay in Kotlin & Jetpack Compose to dynamically mask explicit content over third-party apps via Android WindowManager.

Context & Reference:
Inspect 'Screen Workflow/07_Overlays_And_Dialogs/SafeScreen Protection and Focus .png' (Left Screen).

OS WindowManager Configuration:
- Type: WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
- Flags: FLAG_NOT_FOCUSABLE or FLAG_LAYOUT_IN_SCREEN or FLAG_NOT_TOUCH_MODAL
- Format: PixelFormat.TRANSLUCENT

UI Components:
1. Localized Blur Box: Renders over the detected bounding box of explicit content.
2. Frosted Blur Filter: Uses RenderEffect (Android 12+) or Gaussian shader / pixelation fallback.
3. Center Trust Badge:
   - Dark glass pill badge: Eye-slash icon in white + "Protected by SafeScreen AI" with glowing cyan border.
4. Non-Intrusive Pass-Through: Touch gestures outside the blurred box must pass through to the underlying application.

Architecture:
- Hosted in ShieldProtectionService using ComposeView attached to WindowManager.
- Show/Hide driven by TFLite inference result StateFlow.

Acceptance Criteria:
- Overlay displays within 50ms of threat detection.
- Disappears automatically when the user scrolls away from explicit content.
```

---

### স্ক্রিন ১৫: Focus Intervention Overlay (Reels Blocker)
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/services/overlay/FocusInterventionOverlay.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/07_Overlays_And_Dialogs/SafeScreen Protection and Focus .png` (ডান পাশের স্ক্রিন) & `Neon Focus Time Alert Overlay.png`

#### 📝 প্রম্পট ১৫-B: Focus Intervention Overlay ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement the full-screen Focus Intervention Overlay that intercepts social media doomscrolling when daily time limits are exhausted.

Context & Reference:
Inspect 'Screen Workflow/07_Overlays_And_Dialogs/SafeScreen Protection and Focus .png' (Right Screen) and 'Neon Focus Time Alert Overlay.png'.

UI Structure & Components:
1. Background: Full-screen semi-translucent dark frosted scrim (dimming underlying video).
2. Floating Intervention Card (GlassmorphicCard with NeonPurple / NeonCyan glowing border):
   - Hero: Glowing 3D Shield enclosing an Hourglass icon.
   - Headline: "Focus Time Active!" (headlineMedium, NeonCyan/Purple).
   - Message: "You've reached your daily 30-minute limit for short videos. Your time is valuable — give your mind a restful break."
3. Action Buttons:
   - Button 1: "Take a 5-Min Walk" (Walking icon, solid NeonPurple pill button). Starts a 5-minute break countdown.
   - Button 2: "Exit to Home Screen" (Home icon, outlined button). Fires Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).
   - Micro-Action: "Emergency 2-min pass (Requires PIN)". Triggers PIN prompt before temporary pass.
4. Footer: "SafeScreen • Your Focus, Our Priority".

Acceptance Criteria:
- Completely halts user interaction with the Reels feed.
- "Exit to Home" safely minimizes the target app and returns user to Android launcher.
```

---

## 📁 মডিউল ০৮: এরর, এম্পটি ও ফলব্যাক স্টেটস (Error & Empty States)

### স্ক্রিন ১৬: Empty, Warning & Error State Composables
* **টার্গেট ফাইল:** `app/src/main/java/com/safescreen/app/presentation/components/states/SafeScreenStates.kt`
* **ভিজ্যুয়াল রেফারেন্স:** `Screen Workflow/08_Error_And_Empty_States/SafeScreen Neon Status Dashboard.png` & `SafeScreen Activity Log_ All Cle.png`

#### 📝 প্রম্পট ১৬-B: Empty, Warning & Error States ইমপ্লিমেন্টেশন
```text
Role & Objective:
Implement reusable, responsive UI state composables for clean-slate empty states, permission revoked warnings, and neural model missing errors.

Context & Reference:
Inspect 'Screen Workflow/08_Error_And_Empty_States/SafeScreen Neon Status Dashboard.png' and 'SafeScreen Activity Log_ All Cle.png'.

3 Distinct State Composables to Implement:

1. EmptyStatsState (All Clean & Clear):
   - Hero: Glowing Bio-Green shield with checkmark inside, surrounded by neon sparkles.
   - Title: "All Clean & Clear" (NeonGreen).
   - Subtitle: "No explicit visuals or music interruptions detected yet today. Enjoy your safe digital journey!"
   - Metric Pill Row: [0 Blurred] [0 Muted] [0 Blocked].
   - Badge: "✔ Stay Focused, Stay Safe 💚".

2. PermissionRevokedWarning (Shield Paused):
   - Hero: Amber-yellow shield with exclamation mark.
   - Title: "⏸ Shield Paused: Screen Access Needed" (WarningYellow).
   - Subtitle: "Android revoked MediaProjection or Overlay access. SafeScreen cannot protect you until permissions are restored."
   - Action: "Restore Permissions" (Solid yellow button). Launches PermissionHub.
   - Helper Note: "Go to Settings and grant the required permissions."

3. ModelUninitializedError (AI Core Error):
   - Hero: Glowing neon purple brain icon with pink wrench.
   - Title: "AI Core Uninitialized" (NeonPink).
   - Subtitle: "The local TFLite neural model was not found in local assets. Tap to self-repair or reload model."
   - Action: "🔄 Reload Core" (NeonCyan outline button).
   - Helper Note: "This will reload the AI model from local storage."

Acceptance Criteria:
- Reusable across any screen via SafeScreenStateView(type, onAction).
- Clean previews in both Light and Dark modes.
```

---
*SafeScreen Master UI/UX Screen Design & Production Implementation Guide — Enhanced and Verified.*
