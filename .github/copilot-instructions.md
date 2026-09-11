# Steady repository instructions

Read `AGENTS.md`, `docs/product/product-brief.md`,
`docs/design/design-philosophy.md`, and `docs/product/decision-log.md` before
making changes.

For every UI or interaction task, also open
`docs/design/prototype/index.html` in a browser. It is the interactive visual
reference, not production web code. Recreate its intent with Android-native
Jetpack Compose patterns and keep it synchronized when confirmed behavior
changes.

The project is currently in design exploration. Keep the repository limited to
the `app` and `core:designsystem` modules until an approved vertical slice
creates a real need for another module. Do not add speculative architecture or
unused dependencies.

Steady must feel calm, warm, premium, accessible, and non-judgmental. The Today
checklist is the primary experience. Completed habits are final for that day,
greyed out, struck through, and moved below incomplete habits.
An optional short Goal or note may provide display-only context on a habit,
including quantity-shaped text such as `2.5 L water`; it must never create
partial tracking, progress controls, numeric logging, or reversible completion.
Appearance has three confirmed modes: System default, Light, and Dark. Apply
the chosen Steady palette to every screen and state, persist explicit choices
locally, and follow device changes only in System mode. Use Android-native
theme handling; do not enable arbitrary Material dynamic color by implication.

The MVP is local-first, has no required account or Steady backend, and supports up to seven
non-archived habits, and treats Steady Coins as non-redeemable personal
motivation. Coins may buy a one-day Streak Shield, but the shield is never
consumed automatically; after a missed day, the user chooses whether to use it
or let the streak end. Profile, habit, completion, streak, coin, and shield data
must stay on-device. Optional external APIs may provide contextual information
such as weather, but must not receive private product data. Insights shows the
current streak and calm motivation while deeper reports remain Coming soon;
Settings supports a local display name and profile icon.
Future subscriptions and real-world rewards are not MVP scope.

Habit management has two distinct confirmed actions: Archive is reversible,
uses neutral/warm presentation, and frees an active slot; Delete is permanent,
uses restrained red danger styling, and requires an explicit confirmation that
names the habit. Reset all data must use a high-friction confirmation, erase
habits, profile, onboarding completion, completion/progress, streaks, Coins,
Shields, and appearance, then reopen first-launch onboarding with System
default appearance.
The browser prototype simulates these rules with `localStorage`; that storage
implementation is not Android production architecture.

Fresh launch and Reset all data show onboarding before Today. Require a
non-empty local display name; icon selection is optional with a calm default.
State clearly that the profile stays on-device and is not an account. Persist
completion, name, and icon locally, then open empty Today with Add your first
habit and no seeded demo habits. Any prototype sample-data path must be
explicitly labeled prototype-only.

Use Kotlin, Jetpack Compose, Material 3, JDK 17, and the checked-in Gradle
wrapper. Add libraries only when the same change uses them. Update the decision
log whenever product behavior changes.

Every code change must leave its touched area simpler, clearer, or more
maintainable than before. Prefer direct implementations, descriptive names,
small responsibilities, reuse of established patterns, and removal of
task-related duplication or dead code. Do not add speculative abstractions,
layers, configuration, or dependencies for hypothetical future needs. Keep
cleanup within the requested scope rather than performing unrelated broad
refactors. When new complexity is truly required, isolate it behind the
smallest clear boundary and explain why a simpler solution is insufficient.
