# Google Play Store Listing & Compliance Package

Use the following metadata and declarations when submitting **PeekJson** (`dev.sivab.peekjson`) in the Google Play Console.

---

## 1. Store Listing Copy

### App Name (Max 30 characters)
`PeekJson: JSON Widgets & Live`

### Short Description (Max 80 characters)
`Glanceable HTTPS JSON widgets & live notifications with API key auth & low battery`

### Full Description (Max 4000 characters)
Turn public or authenticated HTTPS JSON endpoints into glanceable information across your Android Home screen, lock-screen notifications, and companion wearables.

PeekJson is built for developers, self-hosters, and power users who want real-time visibility into custom APIs, status endpoints, transit feeds, delivery countdowns, or schedules—without draining battery or relying on third-party cloud relays.

KEY FEATURES
• Public & Authenticated Endpoints: Connect public HTTPS JSON endpoints or authenticate securely using Bearer Tokens (`Authorization: Bearer`) or custom API Key headers (such as `X-API-Key`).
• Responsive Home Screen Widgets: Pin multiple widgets, each mapped to its own feed. Automatically adapts between compact cards, 3-column data tables, and live chronometer countdowns.
• Active Transit & Delivery Tracking: Start a time-bounded, user-initiated session with one-tap "Refresh Now" and "Stop Live" controls. On Android 16+, eligible sessions request promoted Live Update treatment.
• Wearable Mirroring: Dedicated alert channel designed for smartbands and wearables (including Galaxy Fit), triggering only when payload data actually changes.
• Built-In Offline Samples: Preview transit arrivals, live food delivery countdowns, and daily meetings immediately out of the box.

ENGINEERED FOR BATTERY LIFE
• Configurable Sync Intervals: Choose Manual-only (zero background polling), 15 minutes, 30 minutes, 1 hour, 3 hours, or 6 hours.
• Non-Wakeup Inexact Scheduling: Uses Android's `ELAPSED_REALTIME` inexact batching so background refreshes wait for natural device maintenance windows instead of waking the CPU from deep Doze sleep.
• HTTP ETag & 304 Caching: Sends `If-None-Match` and `If-Modified-Since` headers to avoid re-downloading and re-parsing unchanged JSON payloads.
• Battery Saver & Data Saver Aware: Automatically pauses background polling whenever Android Battery Saver or Data Saver is active.

PRIVACY & SECURITY FIRST
• Zero Tracking: No analytics SDKs, no ads, and no intermediary servers.
• On-Device Credentials: API keys and Bearer tokens stay in private local storage (`allowBackup="false"`) and are transmitted strictly over TLS/HTTPS to your configured URL.

---

## 2. Google Play Data Safety Questionnaire

- **Does your app collect or share any of the required user data types?**  
  **No.** All endpoint URLs, API keys/tokens, and cached JSON payloads remain strictly on the user's device and are only sent directly to the user-configured HTTPS endpoint.
- **Is all user data encrypted in transit?**  
  **Yes** (`android:usesCleartextTraffic="false"` enforces HTTPS/TLS).
- **Can users delete their stored credentials/data?**  
  **Yes**, via the in-app **Clear token** button or Android system app storage settings.

---

## 3. Foreground Service (`specialUse`) Play Console Declaration

- **Foreground Service Type:** `specialUse`
- **Subtype Property:** `user_initiated_live_json_endpoint_monitor_and_countdown`
- **Play Console Justification Text:**
  > PeekJson allows users to explicitly start a time-bounded active transit or food-delivery tracking session. The session displays a continuously visible ETA/countdown, refreshes its user-visible state, and ends automatically when the tracked activity completes. It is strictly initiated by the user, exposes prominent Refresh and Stop actions in both the notification and app, and does not use the foreground service for upcoming meetings or arbitrary background JSON polling.
