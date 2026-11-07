# Changelog

## 1.2.0

- Renamed the app, Gradle project, Java namespace, and Android package to PeekJson (`dev.sivab.peekjson`).
- Removed the family-location sample and all personal names from application and preview data.
- Added a time-bounded Route 21 active bus-tracking demo with a changing ETA.
- Restricted promoted Live Update requests to user-started active transit and food-delivery sessions.
- Added automatic session completion plus notification actions for Refresh and Stop.
- Updated Galaxy Fit alerts once per minute during active tracking and replaced the previous app alert.
- Added in-app diagnostics for Android version, promotion settings, and device-controlled Now Bar behavior.
- Changed generic endpoints and upcoming events to regular notifications rather than promoted Live Updates.
- Requested notification permission in context instead of immediately at app launch.
- Fixed fresh-clone debug signing and added environment-only Play upload signing support.
- Disabled cloud backup and device transfer for credentials and cached endpoint data.
- Added a Play Store launch checklist and corrected lock-screen/Now Bar claims.
