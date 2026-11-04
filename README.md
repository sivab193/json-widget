# App Notify

App Notify is a lightweight, battery-optimized native Android application that turns public or authenticated HTTPS JSON endpoints into glanceable cards across Home-screen widgets, Lock-screen Live Updates, Samsung Now Bar, and Galaxy Fit wearables.

Built with zero third-party runtime dependencies using standard Android platform APIs.

## Key Features

- **Public & Authenticated HTTPS Endpoints**:
  - **Public HTTPS**: Connect any public JSON object or array endpoint.
  - **Bearer Token Authentication**: Automatically sends `Authorization: Bearer <token>`.
  - **Custom API Key Header**: Send any custom header (e.g., `X-API-Key`, `Api-Key`, `x-functions-key`) with your secret key.
  - **On-Device Credential Privacy**: Credentials are stored in private sandbox storage (`MODE_PRIVATE`) with Android cloud backup disabled (`android:allowBackup="false"`), masked in the UI with a one-tap **Clear token** control, and transmitted strictly over TLS/HTTPS.
- **Configurable Background Refresh Frequency**:
  - Choose between **Manual only (0 background wakeups)**, **15 min**, **30 min (Recommended)**, **1 hr**, **3 hrs**, or **6 hrs**.
- **Multi-Layer Battery Optimization**:
  - **Non-Wakeup Inexact Alarms (`AlarmManager.ELAPSED_REALTIME`)**: Defers periodic background refreshes until the device's next natural maintenance window without waking the CPU out of deep sleep/Doze.
  - **HTTP Conditional Caching (`ETag` & `Last-Modified`)**: Sends `If-None-Match` and `If-Modified-Since` headers; when the endpoint returns `304 Not Modified`, JSON parsing and UI/wearable updates are skipped completely.
  - **Battery Saver & Data Saver Awareness**: Automatically pauses background polling when Android Battery Saver (`PowerManager.isPowerSaveMode()`) or Data Saver (`RESTRICT_BACKGROUND_STATUS_ENABLED`) is active, or when offline.
  - **Zero-Wakeup Live Countdowns**: Uses the system `Chronometer` for second-by-second countdowns without waking the app process every second.
  - **Diff-Only Wearable Alerts**: Prevents repeated wearable vibrations when the JSON payload has not changed.
- **Glanceable Surfaces**:
  - Responsive Home-screen and Lock-screen widgets (compact cards, 3-row tables, and live delivery countdowns).
  - Per-widget feed mapping via `WidgetConfigActivity` and in-app widget pinning.
  - Promoted ongoing Live Updates (`POST_PROMOTED_NOTIFICATIONS`) on Android 16+ with direct **Refresh Now** and **Stop Live** notification actions.

## Play Store Readiness Artifacts

- [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md): Ready-to-host privacy policy covering endpoint URLs, authentication headers, on-device storage, and permissions.
- [`PLAY_STORE_LISTING.md`](PLAY_STORE_LISTING.md): Complete Google Play Console metadata (Short & Full descriptions, Data Safety questionnaire answers, and Foreground Service `specialUse` declaration).

## Build & Install

```sh
./gradlew assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
For a minified Play Store release build, configure your release signing key and run `./gradlew bundleRelease` or `./gradlew assembleRelease`.
