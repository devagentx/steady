# Agent Guide

This file is the starting point for any AI agent or contributor working on
Steady.

## 60-second orientation

- **Product:** local-first Android habit tracker with a calm daily checklist.
- **Production stack:** Kotlin, Jetpack Compose, Material 3, JDK 17.
- **Current code:** a runnable Android shell, not the designed product.
- **Visual specification:** `docs/design/prototype/index.html`.
- **Behavioral authority:** confirmed entries in
  `docs/product/decision-log.md`.
- **Current phase:** design is established; implementation should proceed in
  small vertical slices without speculative architecture.

## Read before changing code

Read these documents in order:

1. [`docs/product/product-brief.md`](docs/product/product-brief.md)
2. [`docs/design/design-philosophy.md`](docs/design/design-philosophy.md)
3. [`docs/product/decision-log.md`](docs/product/decision-log.md)
4. [`docs/design/prototype/index.html`](docs/design/prototype/index.html) for
   any UI or interaction task
5. The README and the build files relevant to the change

Confirmed entries in the decision log override older prose or prototypes.
If two documents disagree, stop and resolve the conflict before coding.

## Source-of-truth hierarchy

1. Confirmed entries in `docs/product/decision-log.md`
2. Product boundaries in `docs/product/product-brief.md`
3. Reusable visual rules in `docs/design/design-philosophy.md`
4. Interactive intent in `docs/design/prototype/index.html`
5. Production Android code

The prototype demonstrates intended behavior but does not silently create a
product decision. Record new decisions before treating them as requirements.

## Current implementation state

The Android app currently renders only a minimal `Steady` screen. Product
screens, persistence, reminders, weather, Coins, and Streak Shields are not yet
implemented in Android.

The repository intentionally contains only:

- `app` for the minimal runnable Android shell
- `core:designsystem` for shared visual foundations

`core:designsystem/.../Theme.kt` is placeholder code. Its colors and enabled
Material dynamic color are not approved Steady behavior; replace them with
intentional semantic light/dark tokens when implementing the first UI slice.

Do not create feature, data, domain, database, or notification modules until a
real vertical slice establishes their responsibilities. New modules require a
clear ownership boundary and an approved product need.

## Where to make changes

| Task | Start here |
|---|---|
| Product behavior | `docs/product/decision-log.md` and `docs/product/product-brief.md` |
| Visual or interaction change | `docs/design/design-philosophy.md` and the browser prototype |
| App icon or brand mark | `docs/design/branding/steady-app-icon.svg` and `docs/design/branding/README.md` |
| Android application UI | `app/src/main/java/ai/monkmind/steady/` |
| Theme or reusable Compose UI | `core/designsystem/` |
| Dependencies and SDK versions | `gradle/libs.versions.toml` and module build files |
| CI or prototype publishing | `.github/workflows/` |

## Product guardrails

- Keep the daily checklist as the primary experience.
- Keep Insights lightweight: show the current streak and calm encouragement,
  then describe deeper patterns as Coming soon without inventing analytics.
- The MVP is offline-first and does not require an account.
- Steady Coins are motivational only in the MVP and have no redeemable value.
- Steady Coins may buy a one-day Streak Shield. A shield protects one missed
  day, is never consumed automatically, and requires the user's decision after
  the missed day.
- Do not implement future subscriptions, a backend, social features, or health
  integrations unless the task explicitly moves them into scope.
- Preserve the calm, premium, non-judgmental product voice.
- Never introduce dark patterns, guilt-driven copy, or excessive gamification.
- Archive is reversible and neutral/warm. Delete is a separate permanent,
  explicitly confirmed action using restrained danger styling.
- Reset all data is a high-friction destructive action. It erases all local
  product data and preferences and reopens first-launch onboarding using System
  default appearance.
- A true first launch requires a non-empty local display name before Today.
  Keep onboarding minimal: concise local-profile description, name field, and
  Continue. Assign the default icon automatically and leave customization to
  Settings. Completion opens empty Today with Add your first habit and must not
  seed demo habits.
- Prototype sample data must be entered through an explicitly labeled
  prototype-only control, never a silent production-behavior bypass.

## Design guardrails

- Use the semantic design direction in
  [`docs/design/design-philosophy.md`](docs/design/design-philosophy.md).
- Do not treat the current `Theme.kt` values as finalized design tokens.
- Prefer generous spacing, clear hierarchy, subtle borders, and restrained
  motion.
- Completed habits are struck through, greyed out, locked for the day, and
  moved below incomplete habits.
- Every habit may have an optional short Goal or note. Treat it as display-only
  context beside the routine cue/time; never add partial quantity tracking,
  progress controls, numeric logging, or reversible daily completion.
- Accessibility and reduced-motion behavior are requirements, not polish.
- Preserve the confirmed System default, Light, and Dark appearance modes
  across every screen and state. Store explicit choices locally and react to
  device theme changes only while System default is selected.
- Implement Android appearance with platform-native theme APIs and Steady's
  semantic light/dark tokens. Do not infer approval for Material dynamic color.
- Treat `docs/design/prototype/index.html` as the interactive visual reference.
  Open it in a browser before changing Android UI.
- Treat `docs/design/branding/steady-app-icon.svg` as the canonical Steady mark
  until Android launcher resources are implemented. Derive future adaptive
  foreground/background and monochrome assets from it; do not redraw or replace
  it in production code without updating the confirmed design decision.
- The prototype uses browser `localStorage` to simulate mutable local data,
  including add/edit/archive/restore/delete/reset flows. That persistence
  mechanism is prototype-only; preserve the confirmed behavior when Android
  implementation begins, but do not port the web storage code.
- Do not copy HTML/CSS directly into Compose. Recreate the documented intent
  using Android-native components and patterns.

## Development workflow

- Use JDK 17 and the checked-in Gradle wrapper.
- Build: `./gradlew :app:assembleDebug`
- Unit tests: `./gradlew testDebugUnitTest`
- Lint: `./gradlew lintDebug`
- Leave every touched area simpler, clearer, or more maintainable than before.
  Prefer direct code, descriptive names, small responsibilities, and existing
  patterns over new layers or abstractions.
- Do not use this rule to justify unrelated repository-wide refactoring. Improve
  code within the task's scope and preserve behavior outside it.
- Remove duplication, dead paths, obsolete comments, and unnecessary
  indirection encountered in the touched path when doing so is safe and
  directly related to the change.
- A change must not add complexity merely to anticipate possible future needs.
  If the requirement genuinely needs additional complexity, isolate it to the
  smallest boundary and document why the simpler approach is insufficient.
- Keep changes small and aligned with one confirmed product decision.
- Add dependencies only when code in the same change uses them.
- Prefer a working vertical slice over speculative abstractions.
- Update the decision log when a product rule changes.
- Update the design philosophy when a reusable visual rule changes.
- Update the prototype when a confirmed visual or interaction behavior changes.
- Update `AGENTS.md` when architecture, source-of-truth ownership, commands, or
  implementation status changes.
- Keep temporary mockups and exploration artifacts out of production source
  unless they are intentionally promoted into `docs/design/`.

## Handoff checklist

Before finishing a change:

1. Confirm the behavior matches the decision log.
2. Confirm UI changes match the design philosophy.
3. Compare UI changes with the interactive prototype.
4. Update affected documentation and prototype behavior.
5. Run the smallest relevant build, test, and lint tasks.
6. Leave no unexplained architecture or product decisions in code alone.
7. Confirm the touched code is simpler or at least no more complex, with no
   speculative abstraction or avoidable duplication introduced.
