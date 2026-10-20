# Bloom – PCOS wellness app (Onboarding, Dashboard, AdMob)

Kotlin · Jetpack Compose · MVVM · DataStore · Google Mobile Ads (AdMob)

## Setup

1. Open the `bloom/` folder in **Android Studio** (Ladybug 2024.2 or newer) with **JDK 17+**.
2. Let Gradle sync (it downloads the Android SDK platform 35 if missing).
3. Run the `app` configuration on a device or emulator (API 26+), or build an APK:
   ```
   ./gradlew assembleDebug
   # → app/build/outputs/apk/debug/app-debug.apk
   ```
4. Ad events are logged to Logcat under the tag `BloomAds`.

### Changing AdMob IDs

IDs live in `app/build.gradle.kts` and reach the code through `BuildConfig`
(`AdConfig.kt`) and the manifest placeholder `admobAppId`.

- **Debug builds always use Google's test IDs.**
- **Release builds** default to the test IDs too. To use real IDs, add to `local.properties`
  (git-ignored) or `gradle.properties`:
  ```
  ADMOB_APP_ID=ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy
  ADMOB_APP_OPEN_AD_UNIT_ID=ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz
  ADMOB_BANNER_AD_UNIT_ID=ca-app-pub-xxxxxxxxxxxxxxxx/wwwwwwwwww
  ```

## Test ad IDs used

| Purpose | ID |
|---|---|
| AdMob App ID | `ca-app-pub-3940256099942544~3347511713` |
| App Open ad unit | `ca-app-pub-3940256099942544/9257395921` |
| Adaptive banner ad unit | `ca-app-pub-3940256099942544/9214589741` |

These are Google's published sample IDs.

## What is implemented (from the Figma)

| Figma frame | Where it lives |
|---|---|
| Splash, Logo | `ui/splash`, `ui/components/Flower.kt` (Canvas-drawn logo) |
| Onboarding steps 1–4 (welcome, name, goals to track, reminder time) | `ui/onboarding` |
| Home Dashboard (Today's Goals, PCOS FAQ pager, Cycle / Symptom shortcuts, Goal Progress, Today's tip) | `ui/dashboard` |
| Insights (score chart, cycle, daily summary, symptom patterns) | `ui/insights` |
| Learn (PCOS FAQ accordion) | `ui/learn` |
| Settings (profile, tracking toggles, daily targets, notifications, sign out) | `ui/settings` |
| Log Meals / Movement / Water / Relaxation / Sleep, Log Cycle, Today's Symptoms, Edit Profile, Reminder Time, Live Notification Preview | `ui/sheets` (Material bottom sheets) |

Onboarding choices (name, tracked goals, reminder) and the "onboarding completed" flag are stored in
Preferences DataStore. The dashboard only shows the goals the user chose, and Settings changes take effect immediately.

## Architecture

```
com.bloom.app
├── BloomApplication        SDK init (background thread), wires the App Open manager to lifecycle
├── MainActivity            edge-to-edge, hosts the Compose NavHost
├── di/AppContainer         tiny manual DI (repositories, analytics, ad manager)
├── data/                   Models, UserPreferencesRepository (DataStore), WellnessRepository (today's log),
│                           LearnContent (FAQ text), DemoData (sample history for Insights)
├── ads/                    AdConfig, AdAnalytics, NetworkMonitor, AppOpenAdManager, AdaptiveBannerAd
└── ui/
    ├── navigation/         Routes + NavHost (splash → onboarding → main)
    ├── splash/             launch routing + launch ad
    ├── onboarding/         OnboardingViewModel + 4-step screen
    ├── main/               MainViewModel (one StateFlow<MainUiState>), scaffold, bottom nav, sheet host
    ├── dashboard/ insights/ learn/ settings/   one screen file per tab
    ├── sheets/             all bottom-sheet contents
    ├── components/         shared Compose components (cards, buttons, pills, progress, flower…)
    └── theme/              colors + typography (italic serif headings, sans body)
```

- **MVVM**: ViewModels expose immutable `StateFlow` UI state; composables are stateless (state + callbacks).
- **No hardcoded screen sizes**: layouts use weights, fractions of available width and aspect ratios; every tab scrolls.

## Launch flow

```
First launch : Splash → Onboarding → Home            (no App Open Ad)
Returning    : Splash → App Open Ad (if ready) → Home
Later        : Background → Foreground → App Open Ad (if eligible)
```

## Ad implementation approach

### App Open Ad (`AppOpenAdManager`)

- **Preload**: the first load starts as soon as `MobileAds.initialize` completes; after every
  dismissal or show failure the next ad is requested immediately.
- **Launch (returning users)**: the splash calls `showOnLaunch(activity, 4s)`. It shows a ready ad,
  otherwise waits **at most 4 seconds** for the in-flight load, then continues to Home no matter what.
  It skips the wait entirely when offline, and skips showing if the app was backgrounded while waiting.
- **First-time users**: the splash routes to Onboarding without ever calling the ad path, and
  foreground ads stay disabled (`foregroundAdsEnabled = false`) until the main screen is reached.
- **Foreground**: a `ProcessLifecycleOwner` observer fires once per background → foreground transition
  (not on rotation or Activity changes). It shows an ad only if foreground ads are enabled, no ad is
  showing, an Activity is available and a non-expired ad is loaded; otherwise it just makes sure a load is queued.
- **Single-flight**: `isShowingAd` is set *before* `show()`, so repeated foreground events cannot stack ads.
- **Expiry**: ads older than 4 hours are discarded and reloaded.
- **Errors**: load failure (logged, no retry loop; the next launch/foreground retries), show failure and
  exceptions from `show()` all go through the same `finishShowing()` path: release the ad, preload the next, continue the app.
- **Activity tracking**: `ActivityLifecycleCallbacks` keeps a weak reference to the started Activity and
  ignores the ad's own Activity.

### Adaptive banner (`AdaptiveBannerAd`)

- Uses `AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize` with the width the layout provides.
- Shown on the Dashboard (home) tab, in the Scaffold `bottomBar` **between the content and the bottom navigation**,
  above a divider, so it can never overlap content or navigation (Scaffold insets the content by its height).
- Reserves its height while loading; collapses completely if loading fails.
- `AdView` pause/resume follows the lifecycle and `destroy()` runs on dispose.

### Analytics (bonus)

`AdAnalytics` interface with a Logcat implementation. Events: `app_open_ad_requested`, `_loaded`,
`_failed` (with `stage` = load/show and reason), `_shown`, `_dismissed`, `banner_ad_loaded`,
`banner_ad_failed`, `banner_ad_impression`. Swap `LogcatAdAnalytics` in `AppContainer` for a Firebase one if needed.

## Manual test checklist

| Case | How to check |
|---|---|
| First launch | Fresh install → splash → onboarding → home, no app open ad in Logcat |
| Finish onboarding | "Enter Bloom" or "I'll set this up later" saves the flag; relaunch goes straight to the Dashboard |
| Sign out | Settings → Sign out clears data and returns to onboarding (no app open ad) |
| Returning launch | Relaunch → `app_open_ad_requested/loaded/shown` → dismiss → Home |
| Foreground ad | On Home press the home button, reopen → ad shows |
| Offline | Airplane mode → relaunch → goes to Home without waiting |
| Rapid foreground | Toggle background/foreground quickly → only one ad at a time |
| Banner failure | Airplane mode → banner area collapses, content unaffected |

## Assumptions and limitations

- **Built from screenshots of the Figma**, not from the Figma file itself (Figma blocks automated access).
  Layout, colors, copy and component styling follow the screenshots; exact font files and illustrations were not
  available, so headings use the system serif in italic and the logo is drawn in code. Swap in the real fonts
  (`ui/theme/Theme.kt`) and any exported illustrations to get a pixel-level match.
- The icons are Material icons chosen to resemble the Figma icons.
- **Today's log is in memory** (resets when the app process restarts); only preferences are persisted.
- **Insights history is sample data** (`data/DemoData.kt`) for the days before today; today's values are live.
- Reminders are stored but **no real notification is scheduled**; "Live Notification Preview" is an in-app mock-up
  of the lock-screen design with a working countdown for the Meditation tab.
- Cycle prediction uses a fixed 28-day cycle; the FAQ text is general information, not medical advice.
- The dashboard "PCOS FAQ" cards open the Learn tab; "Mark as done" on the tip opens the sleep log.
- No consent flow (UMP/GDPR) is included. Add it before shipping in regions that require it.
- The release build is signed with the debug key so it is installable; use a real keystore for production.
- Frequency capping is available via `AdConfig.MIN_INTERVAL_BETWEEN_APP_OPEN_ADS_MS` (default 0 = off).
- The project has not been compiled in the environment it was written in; expect to fix small issues on first sync.
