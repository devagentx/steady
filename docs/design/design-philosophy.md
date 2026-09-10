# Steady Design Philosophy

Status: Exploring

This document defines Steady's visual and interaction intent. Exact tokens and
screens may evolve, but changes should preserve these principles.

## Core feeling

Steady should feel like a quiet, well-made object used every morning:

- Warm, not clinical
- Calm, not sleepy
- Premium, not exclusive
- Motivating, not addictive
- Minimal, not empty
- Friendly, not childish

## Design principles

### The routine comes first

Today's habits are the visual and functional priority. Coins, streaks, insights,
settings, and monetization must not compete with the checklist.

### Completion creates calm

Completing a habit should reduce visual noise. The item becomes grey, receives a
strikethrough, locks for the day, and moves below incomplete habits. The
remaining actions become easier to scan.

Completing the final scheduled habit is the exception to the otherwise quiet
feedback: show one brief whole-day celebration using restrained sage and
warm-gold motion. It should feel earned and elegant rather than explosive, show
the Steady Coins awarded, occur only once for that day, and provide a
reduced-motion fallback.

### Rewards stay quiet

Steady Coins and streaks provide encouragement but remain secondary. Avoid
confetti, slot-machine motion, aggressive badges, urgency, or loss-framed copy.

The Coins balance may open a small shop. Its primary MVP item is a one-day
Streak Shield. Buying and using a shield must feel like a calm recovery option,
not a pressure mechanic. Never consume a shield automatically; after a missed
day, ask whether the user wants to use it or let the streak end.

### Every state explains itself

Users should understand why an action is unavailable, why a habit moved, how
many habit slots remain, and what pausing will affect.

### Motion communicates structure

Use short, restrained motion for completion, reordering, sheets, and progress.
Motion must explain a state change rather than decorate the interface. Respect
the system reduced-motion preference.

### Accessibility is part of the visual system

- Maintain readable contrast.
- Use at least 48dp touch targets for primary actions.
- Never communicate completion through color alone.
- Support scalable text and screen readers.
- Keep labels understandable without icons.

## Visual language

### Working palette

These values are provisional until the design is finalized.

| Role | Value | Intent |
|---|---|---|
| Background | `#F7F5EF` | Warm off-white canvas |
| Surface | `#FFFCF5` | Slightly elevated content surface |
| Primary sage | `#426B5A` | Calm, healthy brand accent |
| Light sage | `#DCE9E1` | Selected and supportive surfaces |
| Primary text | `#202521` | Softer than pure black |
| Secondary text | `#69716B` | Quiet supporting information |
| Border | `#E6E3DA` | Definition without heavy shadow |
| Warm highlight | `#C99A62` | Restrained streak or reward accent |

### Appearance

Steady supports three appearance choices:

- **System default** — the initial choice; follows the current device theme and
  reacts when the device changes between light and dark.
- **Light** — keeps the warm off-white canvas and deep sage hierarchy.
- **Dark** — uses warm green-black canvases, softly lifted charcoal-sage
  surfaces, light sage accents, and restrained warm highlights.

An explicit Light or Dark choice persists locally. Every destination, editor,
sheet, dialog, toast, empty state, completed state, shop state, and celebration
must use the selected appearance; dark theme is not a Settings-only preview.
Theme changes preserve semantic hierarchy and readable contrast rather than
simply inverting colors.

Working dark palette:

| Role | Value | Intent |
|---|---|---|
| Background | `#151A17` | Warm green-black canvas |
| Surface | `#1E2521` | Quiet lifted content surface |
| Primary sage text/accent | `#9BC8B0` | Accessible calm accent on dark surfaces |
| Primary filled action | `#527D69` | Stable action fill with light foreground |
| Primary text | `#F1EEE6` | Warm near-white |
| Secondary text | `#ADB6AF` | Readable subdued context |
| Border | `#39413C` | Low-noise separation |
| Warm highlight | `#DDB37E` | Restrained reward emphasis |

On Android, implement appearance with platform-native theme observation and
persisted app preferences. Use Steady's deliberate semantic light/dark color
tokens; do not enable Material dynamic color unless a separate confirmed
decision defines how generated colors preserve the brand and accessibility.

### Shape

- Habit rows use comfortable rounded corners around 16–20dp.
- Icons sit inside softly tinted rounded containers.
- Prefer subtle borders to floating cards with heavy shadows.
- Bottom sheets and dialogs should feel anchored and spacious.

### Typography

- Use highly readable Android typography for body content.
- Headings may have a warmer editorial character if accessibility and font
  loading remain reliable.
- Use weight and spacing before introducing additional colors.
- Final font families and type scale are not yet confirmed.

## Today screen hierarchy

1. Greeting and date
2. Daily progress
3. Incomplete habits
4. Completed habits
5. Add or manage habits
6. Bottom navigation

Steady Coins and streaks appear inside or near the progress summary without
becoming the largest element.

Optional weather may appear as a small icon and temperature beside the date.
It must disappear gracefully when unavailable rather than reserving an empty
card or error state.

## Habit row behavior

- The entire row is a completion target.
- Each row contains an icon, title, optional supporting detail, and large check
  control.
- Supporting detail composes the optional Goal or note first and the routine
  cue or time second, separated by a quiet middle dot. Omit either part when it
  is absent rather than reserving space or adding another row.
- Goal or note is display-only context. It may state a target or personal
  reminder, but it must never become a progress control, partial quantity
  display, or numeric log.
- Incomplete habits appear first.
- A completed habit is struck through and greyed out.
- Completion is final for the current day.
- A completed habit automatically moves to the bottom with restrained motion.
- Tapping a completed habit explains that it is locked.
- Accidental-completion protection is still an open design question.

## Navigation

The initial model uses three destinations:

- Today
- Insights — a Coming soon destination in the MVP
- Settings

Habit creation and management open from Today rather than occupying a permanent
navigation destination.

Settings provides a small local profile editor for display name and icon. It
must clearly state that this information stays on the device and does not create
an online account. The app may use optional external services for contextual
information, but those services must remain visually and technically separate
from the user's private habit data.

## Add and edit habit form

Habit creation and editing use the same focused full-screen composition:

1. Top app bar with cancel/back and save
2. Live habit-row preview
3. Habit name
4. Optional Goal or note
5. Icon and color selection
6. Active-day schedule
7. Exact time or routine cue
8. Reminder toggle
9. Archive action when editing

The form should feel like shaping one small practice, not configuring a complex
automation.

Goal or note uses a concise single-line field with a sensible character limit.
Its help text must explain that it adds context only and that completion remains
done or not done. The live preview and saved Today row show the value without
obscuring the habit's routine cue or time.

When no habits exist, Add your first habit opens this form directly. Do not show
a management choice that has no valid management action.

As the user types a name, a local keyword map may suggest an icon and color.
This behavior must be immediate, deterministic, and understandable. Tapping the
icon opens a manual icon-and-color selector. Once the user makes a manual
choice, that choice must remain stable even if the name changes.

Scheduling should support either an exact clock time or a familiar routine cue
such as After waking, After breakfast, After lunch, After dinner, Before bed, or
Anytime. A cue describes the user's intention; the app must not imply that it
can detect when the real-world event occurred.

## Product voice

Use concise, calm language:

- Prefer: "A steady day is taking shape."
- Prefer: "Completed habits are locked for today."
- Avoid: "You failed."
- Avoid: "Don't lose your streak!"
- Avoid exaggerated praise for routine actions.
- Coming soon pages should describe user value, not internal release plans or
  prioritization decisions.

## Anti-patterns

Do not introduce:

- Dense dashboards above the checklist
- Loud gradients or neon fitness colors
- Multiple competing progress rings
- Confetti-heavy completion
- Red punishment states for missed habits
- Premium upsells in the middle of daily completion
- Hidden limits or unexplained locked actions
