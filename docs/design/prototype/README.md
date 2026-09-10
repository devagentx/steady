# Steady Interactive Prototype

This directory contains Steady's versioned interactive visual reference.

Open [`index.html`](index.html) in a browser to explore:

- Today and first-day states
- Habit completion, locking, reordering, and whole-day celebration
- Add and edit habit flows
- Manage Habits selection
- Exact-time and routine-cue scheduling
- Local profile settings
- Steady Coins shop, Streak Shield purchase, and missed-day decision
- Insights Coming soon state
- Travel and pause interactions

## Purpose

The prototype is a behavioral and visual specification for the Android app. It
is not production web code and must not be ported line-for-line into Jetpack
Compose.

The sources of truth are:

1. `docs/product/decision-log.md` for confirmed behavior
2. `docs/design/design-philosophy.md` for reusable design rules
3. This prototype for interactive visual intent
4. Jetpack Compose for the production implementation

If these disagree, resolve the documentation conflict before changing Android
code.

## Updating the prototype

- Update it when a confirmed interaction or visual behavior changes.
- Preserve keyboard access and reduced-motion behavior.
- Keep it dependency-free and self-contained unless there is a strong reason
  to introduce a build system.
- Do not add real user data, secrets, API keys, analytics, or production
  network calls.
- Validate the embedded JavaScript after changes.

The public GitHub Pages deployment is intended for design review and portfolio
demonstration.
