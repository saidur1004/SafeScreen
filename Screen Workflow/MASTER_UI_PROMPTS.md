# SafeScreen — মাস্টার UI/UX স্ক্রিন ডিজাইন ও কোড ইমপ্লিমেন্টেশন প্রম্পট বুক
> **ব্যবহারের নির্দেশিকা:**  
> এই ফাইলটিতে SafeScreen অ্যান্ড্রয়েড অ্যাপ্লিকেশনের সমস্ত স্ক্রিনের ডিজাইন ইমেজ ChatGPT থেকে তৈরি করার জন্য এবং পরবর্তীতে কোডিং এজেন্ট দ্বারা Kotlin + Jetpack Compose-এ ইমপ্লিমেন্ট করার জন্য ধাপে ধাপে সাজানো প্রম্পট দেওয়া হয়েছে।

---

# অংশ ১: ChatGPT-এর জন্য মাস্টার কনটেক্সট ইনিশিয়ালাইজেশন প্রম্পট
*(ডিজাইন তৈরির শুরুতে ChatGPT-তে নতুন একটি চ্যাট খুলে প্রথমেই নিচের সম্পূর্ণ প্রম্পটটি দিন। এটি নিশ্চিত করবে যে ChatGPT অ্যাপটির উদ্দেশ্য ও ডিজাইন থিম পুরোপুরি বুঝে নিয়েছে।)*

```markdown
Role & Persona:
You are an Elite Principal UI/UX Designer and Lead Android Systems Architect specializing in modern, award-winning Mobile App Interfaces. You design high-fidelity, production-grade Android apps adhering strictly to Google Material 3 Design Guidelines, with a futuristic "Cyber-Dark Privacy" and "Clean Minimalist Light" design language.

Project Context — SafeScreen:
SafeScreen is a cutting-edge, Privacy-First Digital Wellbeing and On-Device Content Filtering application for Android (Min SDK 29, Android 10+; Target SDK 35, Android 15).
The core philosophy is "100% On-Device AI, Absolute Zero Cloud Upload, No Trackers".

The app solves 3 primary problems:
1. Visual Protection: Detects explicit/NSFW images and video frames in real-time using on-device TFLite models and instantly applies a smooth, frosted-glass blur overlay.
2. Audio Protection: Detects background music or unwanted sound in supported media using AudioPlaybackCapture API and automatically mutes/suppresses audio.
3. Reels & Shorts Control: Curbs infinite doomscrolling in Facebook Reels, Instagram Reels, YouTube Shorts, and TikTok using daily time limits (UsageStatsManager) and Focus Intervention Overlays (AccessibilityService + WindowManager).

Design Identity & Visual Language:
- Aesthetic: Modern Cyberpunk-meets-Apple-Cleanliness. Futuristic yet calm, trust-inspiring, premium, and distraction-free.
- Color Palette (Dark Mode - Primary):
  * Deep Backgrounds: Deep Cyber Black (#07090E), Dark Obsidian Surface (#0F141F), Elevated Slate (#161D2D).
  * Neon Accents: Neon Electric Cyan (#00E5FF) for interactive states, Neon Bio-Green (#00FF9D) for active shield/security, Neon Pink (#FF2A6D) for threat/block warnings, Neon Purple (#9D4EDD) for focus mode.
  * Text & Glass: Pure White (#F1F5F9), Muted Slate (#94A3B8), Glassmorphic frosted borders (1dp border with 20% alpha gradient).
- Color Palette (Light Mode - Secondary):
  * Clean Crisp White (#FFFFFF), Soft Mist Background (#F8FAFC), Deep Charcoal Text (#0F172A), Vivid Cobalt Blue & Teal Accents.
- Typography: Clean sans-serif (Inter / Roboto Flex style), strong visual hierarchy, bold numbers for counters, readable microcopy.
- Key Elements: Frosted glassmorphic cards, glowing neon indicator rings, smooth switch toggles, rounded corners (20dp to 28dp), tactile feedback cues.

Instructions for Future Screen Generations:
1. In this conversation, I will provide sequential prompts to generate high-fidelity UI/UX design mockups for SafeScreen's complete user journey.
2. Every design MUST maintain strict visual consistency in branding, fonts, icon style (Outlined Material Symbols), and button shapes.
3. Do NOT produce generic, cluttered, or sci-fi movie interfaces. Produce realistic, implementable, pixel-perfect Android mobile UI mockups that look ready for Google Play Store showcase.
4. Ensure full responsiveness for modern Android aspect ratios (19.5:9, 20:9), proper system status bars, edge-to-edge navigation bars, and accessible touch targets (minimum 48dp).

Acknowledge that you have thoroughly understood the project vision, core values, color tokens, and design rules. Respond with: "SafeScreen Design Engine Initialized. Please provide the first screen prompt."
```

---

# অংশ ২: সম্পূর্ণ স্ক্রিনের তালিকা ও ব্যবহারকারীর যাত্রাপথ (User Journey)

নিচে অ্যাপটির সম্পূর্ণ ব্যবহারকারী যাত্রাপথকে ৮টি ফোল্ডার ও ১৬টি সুনির্দিষ্ট স্ক্রিন/স্টেটে বিভক্ত করা হলো:

| ক্রম | ফোল্ডার ও স্ক্রিনের নাম | স্ক্রিনের ধরন | প্রধান উদ্দেশ্য ও ভূমিকা |
| :---: | :--- | :--- | :--- |
| **01** | `01_Onboarding/01_SplashScreen` | Full Screen | ব্র্যান্ড আইডেন্টিটি, নিয়ন শিল্ড পালস ও ব্যাকগ্রাউন্ড ইনিশিয়ালাইজেশন। |
| **02** | `01_Onboarding/02_OnboardingPager` | Full Screen | ৩টি ইন্টারেক্টিভ স্লাইড (Visual, Audio, Reels) ও ১০০% প্রাইভেসি অঙ্গীকার। |
| **03** | `02_Permissions/03_PermissionHub` | Full Screen | Overlay, Screen Capture, Audio, Notification ও Accessibility পারমিশন কার্ডস। |
| **04** | `02_Permissions/04_BatteryOptimizationDialog` | Dialog / Sheet | ব্যাকগ্রাউন্ড প্রোটেকশন সচল রাখতে আনরেস্ট্রিক্টেড ব্যাটারি অনুমতির ব্যখ্যা। |
| **05** | `03_Home_Dashboard/05_HomeScreen` | Core Screen | মূল ড্যাশবোর্ড, সেন্ট্রাল পালসিং শিল্ড বাটন, লাইভ ব্লক কাউন্টার ও কুইক টগলস। |
| **06** | `03_Home_Dashboard/06_BlurStylePreviewSheet` | Bottom Sheet | Gaussian Blur vs Pixelation vs Censored Box প্রিভিউ ও ইনটেনসিটি স্লাইডার। |
| **07** | `04_Reels_And_Focus/07_ReelsControlScreen` | Full Screen | ইনস্টাগ্রাম, ইউটিউব, টিকটক টার্গেট সিলেকশন, সময়সীমা ও ইন্টারভেনশন মোড। |
| **08** | `04_Reels_And_Focus/08_FocusScheduleScreen` | Full Screen | পড়াশোনা ও ঘুমের সময়ে রিলস সম্পূর্ণ নিষিদ্ধ রাখার শিডিউলার। |
| **09** | `05_Stats_And_Analytics/09_StatsScreen` | Core Screen | সাপ্তাহিক/মাসিক ফিল্টারিং চার্ট, ক্যাটাগরি পাই চার্ট ও অ্যাপ ব্রেকডাউন। |
| **10** | `05_Stats_And_Analytics/10_DetailedActivityLog` | Full Screen | কোন সময়ে কোন অ্যাপে কী ব্লক হয়েছিল তার সম্পূর্ণ অফলাইন টাইমলাইন। |
| **11** | `06_Settings_And_Security/11_SettingsScreen` | Core Screen | AI সেনসিটিভিটি (Strict/Balanced), স্যাম্পলিং এফপিএস, ডাটা ক্লিয়ার ও পলিসি। |
| **12** | `06_Settings_And_Security/12_AppWhitelistScreen` | Full Screen | ইনস্টল থাকা অ্যাপের তালিকা ও প্রোটেকশন থেকে বাদ দেওয়ার সুইচ। |
| **13** | `06_Settings_And_Security/13_SecurityLockScreen` | Full Screen / Sheet | সেটিংস বা শিল্ড আনআর্ম করা আটকাতে সাইবার-ডার্ক PIN প্যাড ও বায়োমেট্রিক। |
| **14** | `07_Overlays_And_Dialogs/14_LiveBlurOverlayView` | Floating Window | স্ক্রিনে আপত্তিকর দৃশ্য এলে ওএস-এর ওপর যে ফ্রস্টেড ব্লার উইন্ডো রেন্ডার হবে। |
| **15** | `07_Overlays_And_Dialogs/15_FocusInterventionOverlay` | Floating Window | রিলসে ঢুকলে যে কার্ড ভাসবে: "Focus Time Active! Take a 5-min break"। |
| **16** | `08_Error_And_Empty_States/16_EmptyAndErrorStates` | State Views | নো-থ্রেট এম্পটি স্টেট, পারমিশন ডিনাইড ওয়ার্নিং ও মডেল লোডিং ফেইলর স্টেট। |

---

# অংশ ৩: পর্যায়ক্রমিক প্রম্পট পেয়ারসমূহ (Prompt A: Image + Prompt B: Kotlin)

---

## মডিউল ১: অনবোর্ডিং ফ্লো (Onboarding Flow)

### স্ক্রিন ১ ও ২: Splash Screen & Onboarding Pager
* **ফোল্ডার:** `Screen Workflow/01_Onboarding/`
* **সংরক্ষণযোগ্য ফাইলের নাম:** `01_SplashScreen.png`, `02_OnboardingPager.png`

#### প্রম্পট ১-A: UI/UX ইমেজ জেনারেশন (ChatGPT-এর জন্য)
```text
Design two high-fidelity modern Android mobile screens side-by-side for the "SafeScreen" app in Cyber-Dark theme:

Screen 1: Splash Screen
- Canvas: Modern bezel-less Android phone (Google Pixel / Samsung Galaxy format).
- Background: Deep Cyber Black (#07090E) with a subtle radial gradient glow in Neon Cyan.
- Center Hero: An intricate 3D-stylized geometric Shield icon glowing in Neon Cyan (#00E5FF) and Bio-Green (#00FF9D).
- Typography: "SafeScreen" in modern geometric bold typography with letter-spacing, followed by subtitle "Your Digital Purity & Focus Guard" in muted cool grey (#94A3B8).
- Bottom: A sleek, pulsing pill-shaped indeterminate progress bar with a label "Initializing Neural Core (100% On-Device)..."

Screen 2: Interactive Onboarding Screen
- Top: Skip button in top-right corner.
- Upper Center: An illustrated glassmorphic 3D card showing the 3 core pillars:
  1. Visual Shield (glowing eye with blur aura)
  2. Smart Audio Guard (soundwaves auto-muting into silence)
  3. Doomscroll Breaker (hourglass stopping infinite vertical scrolling)
- Center Text: Headline "Reclaim Your Focus & Purity", Subtitle "On-device AI actively shields your screen and audio from unwanted content. Zero data ever leaves your phone."
- Interactive Indicator: 3 glowing pill page-indicators (first one active in neon cyan).
- Trust Badge: A frosted glass pill with a lock icon: "100% Offline AI • Zero Cloud Upload • No Trackers".
- Bottom Action: Full-width elevated button with rounded corners (24dp) reading "Get Started" in Neon Cyan with deep black text.

Visual Style: Hyper-clean Material 3, frosted glass cards (Glassmorphism with 1dp subtle border), ultra-sharp typography, dark aesthetic. No clutter.
```

#### প্রম্পট ১-B: Kotlin & Jetpack Compose ইমপ্লিমেন্টেশন (ভবিষ্যত কোডিং এজেন্টের জন্য)
```text
Task: Implement the Splash Screen and Onboarding Pager in Kotlin & Jetpack Compose based on the saved reference images in 'Screen Workflow/01_Onboarding/'.

Architectural Requirements:
1. Target Package: `com.safescreen.app.presentation.screens.onboarding` and `...screens.splash`.
2. Clean Architecture: Use a dedicated `OnboardingViewModel` with `StateFlow<OnboardingUiState>` handling current page index and onboarding completed status (persisted via AndroidX DataStore).
3. Compose Libraries: Use `HorizontalPager` from `androidx.compose.foundation.pager.rememberPagerState`.
4. Theme Compliance: Use `SafeScreenTheme` color tokens (`DeepBlack`, `NeonCyan`, `NeonGreen`, `TextPrimary`, `TextSecondary`).
5. Animations: In Splash, animate the scale and alpha of the shield logo using `Animatable` and navigate to Onboarding or Home based on DataStore preferences after 1200ms.
6. Responsiveness: Ensure proper spacing on small (e.g. 5.5") and large (6.7"+) screens with `WindowInsets.systemBars`. Provide full `@Preview(showBackground = true)` for both Light and Dark variants.
```

---

## মডিউল ২: পারমিশন ম্যানেজমেন্ট হাব (Permission Hub Flow)

### স্ক্রিন ৩ ও ৪: Permission Hub & Battery Optimization Dialog
* **ফোল্ডার:** `Screen Workflow/02_Permissions/`
* **সংরক্ষণযোগ্য ফাইলের নাম:** `03_PermissionHub.png`, `04_BatteryOptimizationDialog.png`

#### প্রম্পট ২-A: UI/UX ইমেজ জেনারেশন (ChatGPT-এর জন্য)
```text
Design two connected Android screens for the "SafeScreen" Permission System in Cyber-Dark theme:

Screen 1: Permission Hub Screen
- Header: Back button, Title "System Permissions", Subtitle "To inspect screen frames and audio on-device, SafeScreen requires specific Android permissions. Your data is strictly private."
- Permission Card List (Vertical Scrollable, Frosted Glass Cards):
  1. "Display Over Other Apps" (SYSTEM_ALERT_WINDOW) — Description: "Allows drawing instant blur overlays over explicit content." Status: Granted (Bio-Green Checkmark icon).
  2. "Screen Capture" (MediaProjection) — Description: "Enables local AI frame inspection. No screenshots are ever saved." Status: Action Required ("Grant Access" neon pill button).
  3. "Audio Playback Capture" (RECORD_AUDIO) — Description: "Allows detecting and muting background music tracks." Status: Action Required ("Grant Access" button).
  4. "Accessibility Service" — Description: "Detects Reels & Shorts feeds to enforce your screen-time limits." Status: Action Required ("Enable" button).
  5. "Foreground Notification" (POST_NOTIFICATIONS) — Description: "Keeps the real-time AI shield persistent without being killed by Android." Status: Granted (Bio-Green Checkmark).
- Bottom Fixed Bar: Sticky bottom bar containing a prominent button "Enter Shield Hub" (Disabled state until critical permissions are granted).

Screen 2: Battery Optimization Dialog (Modal Overlay / Bottom Sheet)
- A bottom sheet over the dimmed Permission Hub.
- Sheet Surface: Dark Slate (#161D2D) with 24dp top corner radius and a frosted glass top drag handle.
- Illustration/Icon: A glowing battery icon with an unmetered lightning bolt in Neon Yellow (#FFD166).
- Title: "Unrestricted Battery Access"
- Body Text: "Android aggressively kills background machine learning tasks. To ensure your Shield never sleeps when explicit content appears, please exclude SafeScreen from battery optimization."
- Action Buttons: "Allow Unrestricted" (Solid Neon Cyan button) and "Later" (Text button in muted grey).

Visual Style: Material 3, elegant dark mode, high contrast, crystal-clear explanation badges, accessible touch targets.
```

#### প্রম্পট ২-B: Kotlin & Jetpack Compose ইমপ্লিমেন্টেশন (ভবিষ্যত কোডিং এজেন্টের জন্য)
```text
Task: Implement the Permission Hub Screen and Battery Optimization Dialog in Kotlin & Jetpack Compose based on 'Screen Workflow/02_Permissions/'.

Architectural Requirements:
1. Target Package: `com.safescreen.app.presentation.screens.permission`.
2. State Management: Create `PermissionViewModel` tracking permission states via Android APIs:
   - `Settings.canDrawOverlays(context)`
   - `ContextCompat.checkSelfPermission(POST_NOTIFICATIONS)`
   - `AccessibilityManager` enabled services check
   - `PowerManager.isIgnoringBatteryOptimizations`
3. Intent Launchers: Setup `rememberLauncherForActivityResult` for `StartActivityForResult` pointing to `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`, `Settings.ACTION_ACCESSIBILITY_SETTINGS`, and `Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.
4. Composables: Build reusable `PermissionCard(title, description, isGranted, onGrantClick)`.
5. Error/Warning Handling: If user denies twice, show an inline snackbar guiding them to Android App Settings.
```

---

## মডিউল ৩: মূল ড্যাশবোর্ড ও ব্লার প্রিভিউ (Home & Blur Preview Flow)

### স্ক্রিন ৫ ও ৬: Home Dashboard & Blur Style Preview Sheet
* **ফোল্ডার:** `Screen Workflow/03_Home_Dashboard/`
* **সংরক্ষণযোগ্য ফাইলের নাম:** `05_HomeScreen.png`, `06_BlurStylePreviewSheet.png`

#### প্রম্পট ৩-A: UI/UX ইমেজ জেনারেশন (ChatGPT-এর জন্য)
```text
Design two high-fidelity Android UI mockups for SafeScreen's Main Dashboard in Cyber-Dark theme:

Screen 1: Home Dashboard (Main Screen)
- Top App Bar: Left: App logo icon + "SafeScreen"; Right: Live status pill ("SHIELD ACTIVE" in neon green with a pulsating dot) + Settings Gear icon.
- Central Hero Section (Shield Button):
  * A massive, ultra-sleek circular button (160dp diameter).
  * In active state: Surrounded by concentric pulsing neon cyan & bio-green aura waves. Inner core has a crisp glowing Shield Icon.
  * Status Text below hero: "PROTECTION ENGAGED" in bold uppercase neon typography, sub-label "Analyzing screen & audio locally (5 FPS)".
- Metric Cards Row (2 Frosted Glass Cards side-by-side):
  * Card 1: "Explicit Blurred" -> Bold Number "38" in Neon Pink, subtitle "Today's interventions".
  * Card 2: "Music Muted" -> Bold Number "14" in Neon Cyan, subtitle "Audio tracks silenced".
- Focus Banner Card:
  * "Reels & Shorts Usage: 18 / 30 mins used today" with a linear neon purple progress bar at 60%.
- Quick Feature Toggles (Horizontal row of 3 pill toggles):
  * [✔ Visual Shield] [✔ Audio Mute] [✔ Reels Blocker]
- Bottom Navigation Bar: Floating frosted pill bar with 4 items: Home (Active, Neon Cyan), Stats, Focus, Settings.

Screen 2: Blur Style Preview Bottom Sheet
- Pops up over Home Dashboard.
- Title: "Shield Blur Customization"
- Style Selector Cards (Row of 3 selectable thumbnail cards):
  1. "Frosted Glass" (Smooth Gaussian blur) [Selected with neon border]
  2. "Pixelated Censor" (8-bit digital pixel blocks)
  3. "Opaque Dark Mask" (Total black shield block)
- Slider: "Blur Intensity" with numeric percentage indicator (85%).
- Interactive Preview Area: A split image showing an abstract simulated graphic with the selected blur effect applied in real-time.
- Confirm Button: "Apply Changes" (Neon Cyan button).

Visual Style: Premium Cyber-Dark, authentic Android gesture bar, realistic shadows, ultra-detailed textures.
```

#### প্রম্পট ৩-B: Kotlin & Jetpack Compose ইমপ্লিমেন্টেশন (ভবিষ্যত কোডিং এজেন্টের জন্য)
```text
Task: Implement the Home Dashboard and Blur Customization Sheet in Kotlin & Jetpack Compose referencing 'Screen Workflow/03_Home_Dashboard/'.

Architectural Requirements:
1. Target Package: `com.safescreen.app.presentation.screens.home`.
2. ViewModel & State: `HomeViewModel` exposing `StateFlow<HomeUiState>`:
   - `isShieldActive: Boolean`
   - `statsToday: ProtectionSummary(blurredCount, mutedCount, reelsMinutesUsed, reelsMinutesLimit)`
   - `activeToggles: Map<ProtectionType, Boolean>`
   - `blurConfig: BlurConfiguration(style, intensity)`
3. Service Connection: Trigger `StartShieldUseCase` / `StopShieldUseCase` on central button toggle.
4. Compose Animation: Use `rememberInfiniteTransition` for the outer pulsing ring of `ShieldButton`.
5. Modal Bottom Sheet: Use `ModalBottomSheet` from Material 3 with `rememberModalBottomSheetState()`.
```

---

## মডিউল ৪: রিলস ও ডিজিটাল ওয়েলবিয়িং (Reels & Focus Flow)

### স্ক্রিন ৭ ও ৮: Reels Control & Focus Schedule Screen
* **ফোল্ডার:** `Screen Workflow/04_Reels_And_Focus/`
* **সংরক্ষণযোগ্য ফাইলের নাম:** `07_ReelsControlScreen.png`, `08_FocusScheduleScreen.png`

#### প্রম্পট ৪-A: UI/UX ইমেজ জেনারেশন (ChatGPT-এর জন্য)
```text
Design two modern Android screens for the Reels & Short-Video Wellbeing module in SafeScreen:

Screen 1: Reels & Shorts Control Screen
- Top Bar: Back arrow, Title "Reels & Shorts Control", Info icon.
- Explanatory Notice: A small warning badge: "Android Sandboxing Notice: App UI cannot be deleted. SafeScreen uses Accessibility Overlays & App-Level time limits to protect you."
- Monitored Platforms Section:
  * Vertical list of target apps with brand icons, app names, and independent master toggle switches:
    1. Instagram (Reels) — Switch ON
    2. YouTube (Shorts) — Switch ON
    3. TikTok — Switch ON
    4. Facebook (Reels) — Switch ON
- Daily Time Limit Section:
  * Headline: "Daily Short-Video Allowance"
  * Interactive circular arc slider or stepped slider with presets: [15 min] [30 min (Selected)] [45 min] [1 hr] [Complete Lockout].
- Intervention Strategy Selector (Radio card options):
  * Option 1: "Strict Focus Overlay" (Instantly blocks screen when Reels section is detected) [Selected]
  * Option 2: "Gentle Nudge" (Floating timer reminder)
  * Option 3: "Auto-Home Exit" (Performs back/home gesture automatically)

Screen 2: Focus Schedule Screen (Bedtime & Study Hours)
- Header: Title "Focus Time Schedules", Subtitle "Completely block short-video platforms during essential life hours."
- Schedule Cards:
  * Card 1: "Night Purity & Sleep" -> 10:00 PM – 06:00 AM (Active, recurring Daily, Bio-Green badge).
  * Card 2: "Deep Work / Study" -> 09:00 AM – 05:00 PM (Active, Mon-Fri, Neon Purple badge).
- Add Schedule Button: Floating Action Button (FAB) or dashed outline card "+ Add Custom Focus Block".
- Day Selector: Pill buttons for M, T, W, T, F, S, S.

Visual Style: Cyber-Dark with Neon Purple (#9D4EDD) accents for focus states, clean cards, precise toggles.
```

#### প্রম্পট ৪-B: Kotlin & Jetpack Compose ইমপ্লিমেন্টেশন (ভবিষ্যত কোডিং এজেন্টের জন্য)
```text
Task: Implement the Reels Control and Focus Schedule screens in Kotlin & Jetpack Compose referencing 'Screen Workflow/04_Reels_And_Focus/'.

Architectural Requirements:
1. Target Package: `com.safescreen.app.presentation.screens.focus`.
2. Architecture & DataStore: Store `ReelsConfig` (enabledApps, dailyMinutesLimit, selectedIntervention, focusSchedules) in DataStore Preferences and Room DB.
3. System Integration Hook: Provide hooks for `AccessibilityService` and `UsageStatsManager` to query if current time falls within active focus schedule or exceeds daily allowance.
4. Compose UI Components: Build custom stepped slider `TimeLimitSlider` and day picker `DayOfWeekSelector`.
```

---

## মডিউল ৫: অ্যানালিটিক্স ও অ্যাক্টিভিটি হিস্ট্রি (Stats & Analytics Flow)

### স্ক্রিন ৯ ও ১০: Stats Screen & Detailed Activity Log
* **ফোল্ডার:** `Screen Workflow/05_Stats_And_Analytics/`
* **সংরক্ষণযোগ্য ফাইলের নাম:** `09_StatsScreen.png`, `10_DetailedActivityLog.png`

#### প্রম্পট ৫-A: UI/UX ইমেজ জেনারেশন (ChatGPT-এর জন্য)
```text
Design two data-rich, elegant Android Analytics screens for SafeScreen in Cyber-Dark theme:

Screen 1: Protection Stats Screen
- Top Bar: Title "Protection Insights", Right: Calendar range selector (Pill tabs: "Today", "7 Days" [Selected], "30 Days").
- Privacy Guarantee Card: A green-bordered frosted card: "🔒 100% Offline SQLite. All statistics exist solely on your local device."
- Weekly Activity Chart:
  * A modern glowing bar chart showing blocked explicit frames and muted audio across Mon-Sun.
  * Tallest bar glowing in Neon Cyan with an active tooltip bubble: "Thu: 42 Blurred, 18 Muted".
- Category Breakdown Section (Donut / Segmented Progress Bar):
  * 55% Explicit Visuals Blurred (Neon Pink)
  * 30% Background Music Muted (Neon Cyan)
  * 15% Reels Sessions Intercepted (Neon Purple)
- Top Intercepted Apps List:
  * Horizontal bar breakdown: 1. Chrome (54 events), 2. Instagram (32 events), 3. Twitter/X (19 events).
- Bottom Action: "View Detailed Event Log" with right chevron.

Screen 2: Detailed Activity Log Screen
- Top Bar: Back arrow, Title "Activity Timeline", Filter icon.
- Chronological Event Feed:
  * Grouped by "Today", "Yesterday".
  * Event Item 1: Icon (Eye with slash, Neon Pink) -> Title: "Explicit Visual Content Blurred" -> Subtitle: "Browser (com.android.chrome) • 03:42 PM" -> Confidence tag: "94% NSFW score".
  * Event Item 2: Icon (Mute speaker, Neon Cyan) -> Title: "Background Music Silenced" -> Subtitle: "Shorts Feed • 02:15 PM" -> Duration: "45 sec muted".
  * Event Item 3: Icon (Lock, Neon Purple) -> Title: "Reels Limit Reached — Overlay Shown" -> Subtitle: "Instagram • 11:20 AM".
- Action: Clear Log button in top-right with confirmation dialog.

Visual Style: Clean data visualization, dark UI, vibrant neon indicators, high legibility.
```

#### প্রম্পট ৫-B: Kotlin & Jetpack Compose ইমপ্লিমেন্টেশন (ভবিষ্যত কোডিং এজেন্টের জন্য)
```text
Task: Implement the Stats Screen and Detailed Activity Log in Kotlin & Jetpack Compose referencing 'Screen Workflow/05_Stats_And_Analytics/'.

Architectural Requirements:
1. Target Package: `com.safescreen.app.presentation.screens.stats`.
2. Room Integration: Collect `Flow<List<ProtectionStat>>` from `GetStatsUseCase` injected via Hilt.
3. Canvas Charts: Implement the weekly bar chart using native Jetpack Compose `Canvas` with smooth drawing curves and touch detection for tooltips (or Vico chart library).
4. Performance: Use `LazyColumn` with stable keys (`items(items, key = { it.id })`) to prevent recomposition overhead when new events arrive.
```

---

## মডিউল ৬: সেটিংস, হোয়াইটলিস্ট ও সিকিউরিটি লক (Settings & Security Flow)

### স্ক্রিন ১১, ১২ ও ১৩: Settings, App Whitelist & Security Lock
* **ফোল্ডার:** `Screen Workflow/06_Settings_And_Security/`
* **সংরক্ষণযোগ্য ফাইলের নাম:** `11_SettingsScreen.png`, `12_AppWhitelistScreen.png`, `13_SecurityLockScreen.png`

#### প্রম্পট ৬-A: UI/UX ইমেজ জেনারেশন (ChatGPT-এর জন্য)
```text
Design three connected screens for SafeScreen's System Configuration and Security in Cyber-Dark theme:

Screen 1: Settings Screen
- Sections:
  1. "AI Detection Engine":
     - Sensitivity Level: Segmented control [Strict] [Balanced (Default)] [Relaxed].
     - Performance Profile: "Battery Saver (3 FPS)", "Balanced (5 FPS)", "High Precision (8 FPS)".
  2. "Security & Protection":
     - "PIN & Biometric Lock" switch (Toggle ON).
     - "Prevent Uninstallation Assistance" switch.
  3. "App Exclusions":
     - "Manage App Whitelist (5 apps excluded)" with right chevron.
  4. "Data & Storage":
     - "Clear Offline Database Cache" button.
  5. "About": SafeScreen v1.0.0, Privacy Policy link, Open-source licenses.

Screen 2: App Whitelist Screen
- Top Bar: Back arrow, Title "App Whitelist", Search bar with magnifying glass icon: "Search installed apps...".
- Header Note: "Whitelisted apps bypass all screen frame inspection and audio filtering."
- App List (Vertical Scrollable with app icons and package names):
  * Banking App (com.bank.app) — Switch ON (Exempted)
  * Google Drive (com.google.android.apps.docs) — Switch ON
  * WhatsApp — Switch OFF (Protected)
  * Chrome — Switch OFF (Protected)
  * Gallery / Photos — Switch ON (Exempted)

Screen 3: Security Lock Screen (PIN / Biometric)
- Center: Padlock icon glowing in Neon Green.
- Header: "SafeScreen Locked", Subtitle: "Enter your 4-digit PIN to modify settings or disarm shield".
- PIN Dots: 4 glowing circular indicators.
- Keypad: Glassmorphic numeric dial pad (1-9, Fingerprint icon on bottom-left, 0, Backspace on bottom-right).
- "Forgot PIN?" link below.

Visual Style: Consistent dark theme, sharp borders, high security aesthetic, responsive keypad layout.
```

#### প্রম্পট ৬-B: Kotlin & Jetpack Compose ইমপ্লিমেন্টেশন (ভবিষ্যত কোডিং এজেন্টের জন্য)
```text
Task: Implement Settings, App Whitelist, and Security Lock screens in Kotlin & Jetpack Compose referencing 'Screen Workflow/06_Settings_And_Security/'.

Architectural Requirements:
1. Target Package: `com.safescreen.app.presentation.screens.settings`.
2. Biometric Integration: Connect `androidx.biometric.BiometricPrompt` with `BiometricManager.Authenticators.BIOMETRIC_STRONG or DEVICE_CREDENTIAL`.
3. Whitelist Repository: Fetch device apps using `PackageManager.getInstalledApplications()` on IO Dispatcher; store excluded package names in Room DB.
4. Security Lock Logic: Require PIN verification before toggling master shield off or opening settings if `isPinLockEnabled` is true.
```

---

## মডিউল ৭: সিস্টেম ওভারলে ও ফ্লোটিং উইন্ডো (System Overlays Flow)

### স্ক্রিন ১৪ ও ১৫: Live Blur Overlay & Focus Intervention Overlay
* **ফোল্ডার:** `Screen Workflow/07_Overlays_And_Dialogs/`
* **সংরক্ষণযোগ্য ফাইলের নাম:** `14_LiveBlurOverlayView.png`, `15_FocusInterventionOverlay.png`

#### প্রম্পট ৭-A: UI/UX ইমেজ জেনারেশন (ChatGPT-এর জন্য)
```text
Design two distinct Android System Floating Overlays for SafeScreen (Simulated over a mock social media feed):

Screen 1: Live Visual Blur Overlay (Real-Time Protection in Action)
- Context: A simulated web browser or social feed displaying a mock photo.
- Overlay Effect:
  * The center portion of the screen containing explicit content is covered with a frosted, high-intensity Gaussian blur effect.
  * Over the blurred area: A neat, semi-transparent dark glass pill badge with an eye-slash icon and text: "Protected by SafeScreen AI".
  * The rest of the phone's UI (status bar, navigation bar, text around the image) remains visible and accessible.

Screen 2: Focus Intervention Overlay (Reels / Shorts Blocker Screen)
- Context: A user opened Instagram Reels or YouTube Shorts after exceeding their 30-minute daily limit.
- Overlay Appearance:
  * An unmissable full-screen dark frosted glass card floating over the dimmed video feed.
  * Hero Icon: An hourglass enclosed in a glowing Neon Purple Shield (#9D4EDD).
  * Headline: "Focus Time Active"
  * Message: "You've reached your daily 30-minute limit for short videos. Your time is valuable — give your mind a restful break."
  * Action Buttons (Side-by-side or stacked):
    1. "Take a 5-Min Walk" (Solid Neon Purple button)
    2. "Exit to Home Screen" (Outlined button, triggers back/home action)
    3. Micro-link: "Emergency 2-min pass" (requires security PIN).

Visual Style: Modern floating window look, authentic Android window manager type TYPE_APPLICATION_OVERLAY representation.
```

#### প্রম্পট ৭-B: Kotlin & Jetpack Compose ইমপ্লিমেন্টেশন (ভবিষ্যত কোডিং এজেন্টের জন্য)
```text
Task: Implement the Live Blur Overlay and Focus Intervention Overlay in Kotlin & Jetpack Compose referencing 'Screen Workflow/07_Overlays_And_Dialogs/'.

Architectural Requirements:
1. Target Package: `com.safescreen.app.services.overlay`.
2. System Service Integration: Use `WindowManager.LayoutParams`:
   - `type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`
   - `flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN`
3. Compose View in WindowManager: Inflate Compose UI into WindowManager using `ComposeView` with `setViewTreeLifecycleOwner` and `setViewTreeSavedStateRegistryOwner`.
4. Dismiss & Action Handling: Clicking "Exit to Home" should fire an intent with `Intent.ACTION_MAIN` and `Intent.CATEGORY_HOME`.
```

---

## মডিউল ৮: এরর, এম্পটি ও এজ কেস স্টেটস (Empty, Error & Edge States)

### স্ক্রিন ১৬: Empty Stats & Error Fallbacks
* **ফোল্ডার:** `Screen Workflow/08_Error_And_Empty_States/`
* **সংরক্ষণযোগ্য ফাইলের নাম:** `16_EmptyAndErrorStates.png`

#### প্রম্পট ৮-A: UI/UX ইমেজ জেনারেশন (ChatGPT-এর জন্য)
```text
Design three cohesive UI State illustrations/cards on a single showcase board in Cyber-Dark theme for SafeScreen:

State 1: Empty Stats State (Clean Slate)
- Graphic: A pristine floating shield surrounded by gentle green sparkles.
- Title: "All Clean & Clear"
- Description: "No explicit visuals or music interruptions detected yet today. Enjoy your safe digital journey!"

State 2: Critical Permission Revoked Warning
- Graphic: A shield with a caution triangle in Warning Yellow (#FFD166).
- Title: "Shield Paused: Screen Access Needed"
- Description: "Android revoked MediaProjection or Overlay access. SafeScreen cannot protect you until permissions are restored."
- Action: "Restore Permissions" (Solid yellow button with dark text).

State 3: On-Device AI Model Missing / Uninitialized
- Graphic: A neural network brain icon with a small disconnect wrench.
- Title: "AI Core Uninitialized"
- Description: "The local TFLite neural model was not found in local assets. Tap to self-repair or reload model."
- Action: "Reload Core" (Neon Cyan outline button).

Visual Style: Minimalist, clean vector style, cyber-dark aesthetic, non-intimidating, informative.
```

#### প্রম্পট ৮-B: Kotlin & Jetpack Compose ইমপ্লিমেন্টেশন (ভবিষ্যত কোডিং এজেন্টের জন্য)
```text
Task: Implement the Empty State and Error State composables in Kotlin & Jetpack Compose referencing 'Screen Workflow/08_Error_And_Empty_States/'.

Architectural Requirements:
1. Target Package: `com.safescreen.app.presentation.components.states`.
2. Reusability: Create a flexible composable `SafeScreenStateView(icon, title, message, actionText, onActionClick)`.
3. Wiring:
   - Display `EmptyState` in `StatsScreen` when Room DB returns 0 entries.
   - Display `PermissionRevokedBanner` on `HomeScreen` if background checks detect lost overlay permission.
```

---
*SafeScreen UI/UX Master Prompt Book — Production Ready.*

