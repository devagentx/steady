# Steady

Steady is a calm, offline-first Android habit tracker from **MonkMind Labs**.
It helps people build consistency through a focused daily checklist, streaks,
transparent scoring, weekly insights, and locally learned reminder suggestions.

## Technology

- Kotlin and coroutines
- Jetpack Compose with Material 3
- Room and DataStore
- WorkManager
- Hilt and KSP
- Multi-module, offline-first architecture

## Modules

- `app` — application entry point and navigation host
- `core:model` — shared product models
- `core:database` — Room database and DAOs
- `core:data` — repository implementations
- `core:domain` — business rules and use cases
- `core:designsystem` — theme and reusable Compose components
- `core:notifications` — reminders and background work
- `feature:today` — daily checklist
- `feature:habits` — habit management
- `feature:insights` — consistency and weekly insights
- `feature:settings` — preferences and data controls

## Local setup

1. Install Android Studio and Android SDK 37.
2. Use JDK 17.
3. Open the repository in Android Studio and allow Gradle sync to complete.
4. Run the `app` configuration on an Android 8.0 or newer emulator/device.

The Android application ID is `ai.monkmind.steady`.
