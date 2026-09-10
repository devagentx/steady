# Steady repository instructions

Read `AGENTS.md`, `docs/product/product-brief.md`,
`docs/design/design-philosophy.md`, and `docs/product/decision-log.md` before
making changes.

The project is currently in design exploration. Keep the repository limited to
the `app` and `core:designsystem` modules until an approved vertical slice
creates a real need for another module. Do not add speculative architecture or
unused dependencies.

Steady must feel calm, warm, premium, accessible, and non-judgmental. The Today
checklist is the primary experience. Completed habits are final for that day,
greyed out, struck through, and moved below incomplete habits.

The MVP is local-first, has no required account or Steady backend, and supports up to seven
non-archived habits, and treats Steady Coins as non-redeemable personal
motivation. Profile, habit, completion, streak, and coin data must stay
on-device. Optional external APIs may provide contextual information such as
weather, but must not receive private product data. Insights is a Coming soon
page; Settings supports a local display name and profile icon. Future
subscriptions and real-world rewards are not MVP scope.

Use Kotlin, Jetpack Compose, Material 3, JDK 17, and the checked-in Gradle
wrapper. Add libraries only when the same change uses them. Update the decision
log whenever product behavior changes.
