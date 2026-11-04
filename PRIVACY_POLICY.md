# Privacy Policy for App Notify

**Effective Date:** November 2, 2026  
**Package Name:** `dev.sivab.appnotify`

## 1. Overview

App Notify ("the App") is a client-side utility that fetches JSON data from user-configured HTTPS endpoints or built-in offline sample feeds and displays the formatted summary on Android Home-screen widgets, Lock-screen notifications, and paired companion wearables.

## 2. Data We Collect and Store

App Notify operates entirely on your device and does **not** operate any backend servers, analytics SDKs, advertising networks, or telemetry trackers.

- **Endpoint URLs & Preferences**: The HTTPS URL, selected feed type, and refresh interval you configure are stored locally on your device in private application preferences (`Context.MODE_PRIVATE`).
- **Authentication Credentials (Bearer Tokens & API Keys)**: If you configure an authenticated endpoint using a Bearer Token or custom API Key header, that credential is stored locally on your device (`Context.MODE_PRIVATE`) with Android Auto Backup disabled (`android:allowBackup="false"`). Your credentials are only transmitted over encrypted HTTPS (`TLS`) directly to the endpoint URL you specify. You can permanently delete stored credentials at any time using the **Clear token** button in the App or by clearing app storage.
- **Cached JSON Responses**: The most recent JSON response and HTTP caching validators (`ETag` and `Last-Modified`) are cached locally on your device to render widgets and reduce redundant network and battery usage.

## 3. Data Sharing

- We do **not** collect, transmit, rent, or sell any personal data or credentials to the developer or any third party.
- Network requests are made exclusively from your device directly to the HTTPS endpoint URL that you configure.

## 4. Permissions Used

- `INTERNET` & `ACCESS_NETWORK_STATE`: Used solely to fetch JSON from your configured HTTPS endpoint when online and to skip background refreshes when offline or when Data Saver is active.
- `POST_NOTIFICATIONS` & `POST_PROMOTED_NOTIFICATIONS`: Used to display optional user-enabled Live Updates on the lock screen and companion wearable notifications.
- `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_SPECIAL_USE`: Used only when you explicitly start a Live Session to keep the ongoing lock-screen status/countdown active. You can stop the session at any time via the **Stop Live** action in the notification shade or inside the App.
- `RECEIVE_BOOT_COMPLETED`: Used to restore your configured inexact background refresh schedule after a device reboot.

## 5. Contact

For questions regarding this Privacy Policy, please contact the developer via the support email listed on the Google Play Store listing page.
