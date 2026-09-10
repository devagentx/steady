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
consistency without creating an account.

## MVP scope

- Android-first application
- Daily and selected-weekday recurring habits
- Maximum of seven non-archived habits
- Unlimited archived habits
- Daily checklist generated from the routine
- Binary habit completion
- Streaks and weekly insights
- Steady Coins as personal motivation
- Temporary habit or routine pause
- Travel-mode support
- Local reminder timing suggestions based on completion history
- On-device storage with no required sign-in

## Explicitly out of scope

- General projects, deadlines, subtasks, or one-off task management
- Social feeds, leaderboards, friends, or public profiles
- Real-money or redeemable rewards
- Cloud sync or cross-device accounts
- Health Connect or wearable integrations
- Native quantity logging for water, protein, or steps
- A paid plan in the MVP

## Business direction

The MVP is free and allows up to seven non-archived habits.

A possible post-MVP model is:

- Free tier: up to four non-archived habits
- Premium subscription: up to ten non-archived habits

This future model must not be implemented until pricing, downgrade behavior,
data migration, and user value have been decided.

## Privacy position

- Habit and completion data stays on-device in the MVP.
- Reminder learning is performed locally.
- Habit names must not be included in telemetry or crash reports.
- Notification content should eventually support a privacy-sensitive mode.
- Users must be told that uninstalling the app may remove their data until
  backup or export exists.

## Success criteria

The MVP succeeds when users can understand today's routine immediately,
complete it with minimal interaction, and return because the experience feels
helpful rather than demanding.

