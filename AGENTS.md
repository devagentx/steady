# Agent Guide

This file is the starting point for any AI agent or contributor working on
Steady.

## Read before changing code

Read these documents in order:

1. [`docs/product/product-brief.md`](docs/product/product-brief.md)
2. [`docs/design/design-philosophy.md`](docs/design/design-philosophy.md)
3. [`docs/product/decision-log.md`](docs/product/decision-log.md)
4. The README and the build files relevant to the change

Confirmed entries in the decision log override older prose or prototypes.
If two documents disagree, stop and resolve the conflict before coding.

## Current project phase

Steady is in product and visual design exploration. The repository intentionally
contains only:

- `app` for the minimal runnable Android shell
- `core:designsystem` for shared visual foundations

Do not create feature, data, domain, database, or notification modules until a
real vertical slice establishes their responsibilities. New modules require a
clear ownership boundary and an approved product need.

## Product guardrails

- Keep the daily checklist as the primary experience.
- The MVP is offline-first and does not require an account.
- Steady Coins are motivational only in the MVP and have no redeemable value.
- Do not implement future subscriptions, a backend, social features, or health
  integrations unless the task explicitly moves them into scope.
- Preserve the calm, premium, non-judgmental product voice.
- Never introduce dark patterns, guilt-driven copy, or excessive gamification.

## Design guardrails

- Use the semantic design direction in
  [`docs/design/design-philosophy.md`](docs/design/design-philosophy.md).
- Do not treat the current `Theme.kt` values as finalized design tokens.
- Prefer generous spacing, clear hierarchy, subtle borders, and restrained
  motion.
- Completed habits are struck through, greyed out, locked for the day, and
  moved below incomplete habits.
- Accessibility and reduced-motion behavior are requirements, not polish.

## Development workflow

- Use JDK 17 and the checked-in Gradle wrapper.
- Build: `./gradlew :app:assembleDebug`
- Unit tests: `./gradlew testDebugUnitTest`
- Lint: `./gradlew lintDebug`
- Keep changes small and aligned with one confirmed product decision.
- Add dependencies only when code in the same change uses them.
- Prefer a working vertical slice over speculative abstractions.
- Update the decision log when a product rule changes.
- Update the design philosophy when a reusable visual rule changes.
- Keep temporary mockups and exploration artifacts out of production source
  unless they are intentionally promoted into `docs/design/`.

## Handoff checklist

Before finishing a change:

1. Confirm the behavior matches the decision log.
2. Confirm UI changes match the design philosophy.
3. Update affected documentation.
4. Run the smallest relevant build, test, and lint tasks.
5. Leave no unexplained architecture or product decisions in code alone.

