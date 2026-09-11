# Steady Interactive Prototype

This directory contains Steady's versioned interactive visual reference.

Open [`index.html`](index.html) in a browser to explore:

- Today and first-day states
- Habit completion, locking, reordering, and whole-day celebration
- Add and edit habit flows
- Optional Goal or note editing, preview, and Today-row context
- Manage Habits selection
- Mutable add/edit behavior reflected immediately on Today and Manage Habits
- Reversible archive/restore and separate permanent habit deletion
- Two-step Reset all data flow that reopens first-launch onboarding
- Minimal first-launch onboarding with a required local name
- Brief sapling launch screen with the Steady habit-building promise
- The canonical sapling/check brand mark in onboarding plus external
  light, dark, and adaptive-mask previews
- Exact-time and routine-cue scheduling
- Local profile settings
- Persisted System default, Light, and Dark appearance choices
- Steady Coins shop, Streak Shield purchase, and missed-day decision
- Insights current-streak spotlight, gentle motivation, and More insights
  coming soon state
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

Brand geometry is owned by
[`../branding/steady-app-icon.svg`](../branding/steady-app-icon.svg). The
prototype mirrors that source for theme-aware previews; update the canonical
SVG first if the mark changes.

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

## Prototype persistence

The web prototype uses browser `localStorage` only to simulate mutable on-device
behavior during design review. New visitors receive first-launch onboarding,
not demonstration habits. Sample-filled states are available only through
explicitly labeled prototype-only controls.

Archive is reversible and frees an active slot. Permanent Delete is separate,
names the affected habit in its confirmation, and cannot be undone. Reset all
data uses two explicit steps and removes all prototype app data, including
habits, profile, onboarding completion, completion/progress, streaks, Coins,
Shields, and appearance. It reopens onboarding with System default appearance;
successful onboarding then opens empty Today.

These interaction rules are confirmed product behavior. The browser storage
shape, keys, and DOM implementation are prototype-only simulation details and
must not be treated as Android persistence architecture.

The Goal or note field is optional, short, and display-only. Examples such as
`125 g protein`, `2.5 L water`, and `10,000 steps` are contextual subtitles,
not partially tracked quantities. Completion remains binary, locked after
completion for the day, and free of progress controls or numeric logging.

Appearance is also behavioral: the prototype stores the selected mode locally,
resolves System default through `prefers-color-scheme`, reacts to system changes
while System is selected, and applies the resolved Steady palette to every
screen, overlay, editor, and state before first paint.

On a fresh browser state, onboarding appears before Today and cannot continue
without a trimmed display name. It preserves the balanced composition of one
brand mark, a small eyebrow, a short description, the name field, and Continue
only. A default profile icon is assigned automatically; the name and onboarding
completion persist locally. Completion opens empty Today. Reset all data clears
onboarding state and reopens it immediately. To keep the prototype useful for
broad design review, sample habits are available only through controls
explicitly labeled **Prototype only** or **Sample demo**.

The public GitHub Pages deployment is intended for design review and portfolio
demonstration.
