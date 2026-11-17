# Android App Manager

Native Android app (Kotlin + Jetpack Compose), dark UI with neon accents.

## Features
- Lists installed applications and version names
- Search by app label or package name
- Optional display of system apps
- Opens Android's standard uninstall confirmation
- Selects an APK with the system document picker and hands it to Android's package installer

## Build
Requires JDK 17, Android SDK 35 and Gradle 8.9.

```bash
gradle assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions
The workflow builds an APK on pushes to `main` and manual dispatch. Create a tag such as `v1.0.0` to publish a GitHub Release.

## Android limitations
Installation and uninstall are confirmed by Android. System apps and device-policy protected apps may not be removable by an ordinary app. Package visibility uses `QUERY_ALL_PACKAGES`, which is restricted for Google Play distribution; this project is intended for private sideloading.
