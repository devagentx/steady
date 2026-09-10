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

### Rewards stay quiet

Steady Coins and streaks provide encouragement but remain secondary. Avoid
confetti, slot-machine motion, aggressive badges, urgency, or loss-framed copy.

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

## Habit row behavior

- The entire row is a completion target.
- Each row contains an icon, title, optional supporting detail, and large check
  control.
- Incomplete habits appear first.
- A completed habit is struck through and greyed out.
- Completion is final for the current day.
- A completed habit automatically moves to the bottom with restrained motion.
- Tapping a completed habit explains that it is locked.
- Accidental-completion protection is still an open design question.

## Navigation

The initial model uses three destinations:

- Today
- Insights
- Settings

Habit creation and management open from Today rather than occupying a permanent
navigation destination.

## Product voice

Use concise, calm language:

- Prefer: "A steady day is taking shape."
- Prefer: "Completed habits are locked for today."
- Avoid: "You failed."
- Avoid: "Don't lose your streak!"
- Avoid exaggerated praise for routine actions.

## Anti-patterns

Do not introduce:

- Dense dashboards above the checklist
- Loud gradients or neon fitness colors
- Multiple competing progress rings
- Confetti-heavy completion
- Red punishment states for missed habits
- Premium upsells in the middle of daily completion
- Hidden limits or unexplained locked actions

