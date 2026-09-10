# Steady Product Brief

Status: Draft for MVP

## Vision

Steady is a calm, offline-first Android habit tracker that helps people follow a
small daily routine without turning their life into a project-management
system.

The app answers three questions:

1. What should I do today?
2. What have I already completed?
3. Am I becoming more consistent over time?

## Target user

Steady is for people who know the recurring actions they want to perform but
currently track them mentally. Typical habits include exercise, protein goals,
water goals, supplements, and daily steps.

The initial user prefers:

- A short daily checklist
- Simple done or not-done completion
- Private on-device data
- Gentle motivation rather than competitive gamification
- Useful reminders without extensive manual configuration

## MVP promise

A user can create a recurring routine, receive a fresh checklist each day,
complete habits with one tap, temporarily pause their routine, and review
their daily progress without creating an account.

## MVP scope

- Android-first application
- Daily and selected-weekday recurring habits
- Optional short Goal or note text on every habit for display-only context
- Maximum of seven non-archived habits
- Unlimited archived habits
- Reversible habit archive and restore
- Separately confirmed permanent habit deletion
- Daily checklist generated from the routine
- Binary habit completion
- Streaks
- Steady Coins as personal motivation
- Steady Coins shop with a one-day Streak Shield
- Local profile with a display name and selectable icon
- First-launch onboarding requiring a local display name
- Appearance setting with System default, Light, and Dark choices
- User-initiated email feedback to `ceo@monklabs.ai`
- Insights destination presented as Coming soon
- Temporary habit or routine pause
- Travel-mode support
- Local reminder timing suggestions based on completion history
- On-device product data with no required sign-in or Steady backend
- Optional external API calls for contextual information such as weather
- Optional cached weather icon and temperature beside the Today date

## Explicitly out of scope

- General projects, deadlines, subtasks, or one-off task management
- Social feeds, leaderboards, friends, or public profiles
- Real-money or redeemable rewards
- Cloud sync or cross-device accounts
- Health Connect or wearable integrations
- Native quantity logging for water, protein, or steps
- A paid plan in the MVP
- Functional analytics or insight reports in the initial MVP

## Business direction

The MVP is free and allows up to seven non-archived habits.

Steady Coins may be spent on an in-app one-day Streak Shield. Coins and shop
items remain motivational only and have no monetary or redeemable value.

A possible post-MVP model is:

- Free tier: up to four non-archived habits
- Premium subscription: up to ten non-archived habits

This future model must not be implemented until pricing, downgrade behavior,
data migration, and user value have been decided.

## Privacy position

- Habit and completion data stays on-device in the MVP.
- Profile name and icon stay on-device and do not represent an online account.
- Steady has no first-party application backend in the MVP.
- External services may be used for optional contextual data such as weather.
- External requests must not include profile names, habits, completions, streaks,
  or coin balances.
- Feedback is sent only when the user explicitly opens their email application;
  Steady does not transmit it through an application backend.
- Location must be optional, permission-based, and no more precise than the
  feature requires.
- Date, time, weekday, and timezone should come from the device without a
  network request.
- A no-key weather service may be used for the non-commercial MVP, subject to
  its attribution, rate-limit, availability, and licensing requirements.
- Weather must be cached and treated as optional so the core app remains useful
  without network access.
- Reminder learning is performed locally.
- Habit names must not be included in telemetry or crash reports.
- Notification content should eventually support a privacy-sensitive mode.
- Users must be told that uninstalling the app may remove their data until
  backup or export exists.
- Users can reset all local data. Reset removes habits (active and archived),
  profile, completion/progress, streaks, Steady Coins, Streak Shields, and
  appearance preference, then reopens first-launch onboarding with System
  default appearance. It cannot be undone.

## First launch

- A true fresh launch shows focused onboarding before Today.
- A non-empty display name is required to continue.
- Profile icon selection is optional. Steady provides a calm default icon and
  allows the user to choose another during onboarding.
- The screen clearly explains that the profile stays on the device, is not an
  account, and is not uploaded.
- Successful onboarding persists completion, name, and selected/default icon
  locally, then opens the empty Today state with Add your first habit.
- Fresh launch and Reset all data never seed or restore example habits.
- The web prototype may expose an explicitly labeled prototype-only sample-data
  path for design review; Android production behavior must not bypass onboarding.

## Data management

- Archive is reversible, removes a habit from Today, and frees one of the seven
  active slots. Archived habits remain available to restore or permanently
  delete.
- Delete is permanent and separate from Archive. Confirmation names the habit
  and explains that its local history is removed.
- Reset all data is intentionally high friction and requires two explicit
  confirmation steps.
- The interactive web prototype simulates these confirmed rules with browser
  persistence. The browser storage mechanism itself is not an Android product
  or architecture decision.

## Appearance

- System default is the initial appearance choice and follows the device theme.
- Users may explicitly choose Light or Dark, and that preference persists
  locally.
- Both themes retain Steady's warm, calm, premium hierarchy and accessible
  contrast across every screen and state.
- Android should use platform-native theme observation and preference storage.
  Steady uses its intentional light and dark palettes; this decision does not
  enable arbitrary Material dynamic color.

## Success criteria

The MVP succeeds when users can understand today's routine immediately,
complete it with minimal interaction, and return because the experience feels
helpful rather than demanding.

A Goal or note may describe a target such as `125 g protein`, `2.5 L water`, or
`10,000 steps`, or hold a short personal reminder. It is optional display-only
context: Steady does not parse it, record partial quantities, show progress
controls, or change the habit's binary, irreversible-for-the-day completion.
