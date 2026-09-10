# Steady

Steady is a calm, offline-first Android habit tracker from **MonkMind Labs**.
It helps people build consistency through a focused daily checklist, streaks,
Steady Coins, a Coins shop with a one-day Streak Shield, and locally learned
reminder suggestions. The MVP keeps core data on-device without a Steady
backend; optional external APIs may provide contextual information such as
weather. Insights is initially presented as Coming soon.

Each habit may also carry an optional short Goal or note, shown only as
contextual text. It does not introduce quantity tracking: daily completion
remains binary and cannot be undone after completion.

Appearance supports System default, Light, and Dark. The preference is local,
and both palettes preserve Steady's intentional visual identity rather than
opting into arbitrary Material dynamic color.

## Project direction

- [Product brief](docs/product/product-brief.md)
- [Design philosophy](docs/design/design-philosophy.md)
- [Interactive design prototype](docs/design/prototype/index.html)
- [Product decision log](docs/product/decision-log.md)
- [AI agent and contributor guide](AGENTS.md)

The interactive prototype is deployed through GitHub Pages for design review.
It is a visual specification, not a web version of the production Android app.
Its browser-only simulation now persists mutable habits and supports add, edit,
reversible archive/restore, permanent delete, and reset-all-data flows. These
interactions demonstrate confirmed product rules, but their `localStorage`
implementation is prototype scaffolding rather than Android production code.

## Current status

The product behavior and visual direction are documented and represented in the
interactive prototype. The Android application is still a minimal runnable
shell; the prototype screens and local product data have not yet been
implemented in Compose.

Confirmed management behavior distinguishes reversible Archive from permanent
Delete. Reset all data erases habits, profile, completion/progress, streaks,
Steady Coins, Streak Shields, appearance, and onboarding completion, then
reopens first-launch onboarding using System default appearance.

First launch requires a non-empty local display name, offers an optional profile
icon with a calm default, explains that the profile is on-device and not an
account, and then opens an empty Today list with Add your first habit.

## Technology

- Kotlin
- Jetpack Compose with Material 3

## Modules

- `app` — minimal application shell
- `core:designsystem` — theme and reusable Compose components

Feature and data modules will be introduced only when the product design and
first vertical slice establish a real boundary for them.

## Local setup

1. Install Android Studio and Android SDK 37.
2. Use JDK 17.
3. Open the repository in Android Studio and allow Gradle sync to complete.
4. Run the `app` configuration on an Android 8.0 or newer emulator/device.

The Android application ID is `ai.monkmind.steady`.
