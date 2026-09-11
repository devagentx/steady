# Product Decision Log

This is the source of truth for confirmed product behavior. Update an existing
entry when a decision changes and preserve the previous direction in its notes.

| ID | Status | Decision | Notes |
|---|---|---|---|
| P-001 | Confirmed | Steady is a recurring habit tracker, not a general task manager. | One-off tasks and projects are outside the MVP. |
| P-002 | Confirmed | Habit completion is binary and irreversible for the day. | Every habit may have an optional short Goal or note for display-only context, including targets such as `125 g protein`, `2.5 L water`, or `10,000 steps`. Steady never parses it or displays/records partial progress, quantity controls, or numeric logs. |
| P-003 | Confirmed | The MVP works on-device without required sign-in. | Cloud sync is outside MVP scope. |
| P-004 | Confirmed | The Today checklist is the main home-screen content. | Rewards and analytics are supporting content. |
| P-005 | Confirmed | The initial navigation has Today, Insights, and Settings. | Habit management opens from Today. |
| P-006 | Confirmed | Completed habits are struck through, greyed out, locked, and moved to the bottom. | Users cannot undo completion for that day. |
| P-007 | Confirmed | The motivational reward is called Steady Coins. | MVP coins have no monetary or redeemable value. |
| P-008 | Confirmed | A habit/date can award coins only once. | Balance must derive from deterministic earning transactions rather than a freely editable total. |
| P-009 | Confirmed | The MVP is free and supports up to seven non-archived habits. | Paused habits still consume a slot; archived habits are unlimited. |
| P-010 | Future | Steady remains free while adoption and retention are validated; a Free/Pro model may be reconsidered only after meaningful adoption. | The current app has no subscription or Pro entitlement. A possible later model allows five active habits on Free and unlimited active habits on Pro, but must not be implemented during MVP. |
| P-011 | Confirmed | Paused days preserve streaks but reduce consistency. | The exact reduction formula remains undecided. |
| P-012 | Confirmed | Users choose the completion threshold required to maintain their streak. | Threshold selection UX is not designed yet. |
| P-013 | Confirmed | Reminder times can be suggested from local completion history. | Suggestions require user acceptance and support opt-out. |
| P-014 | Confirmed | Travel and temporary pauses are supported. | Paused habits do not send reminders. |
| P-015 | Confirmed | Insights shows the user's current streak and a calm motivational message while deeper insights remain Coming soon. | Functional analytics and detailed insight reports are deferred. The screen must still provide immediate value without becoming a dense dashboard. |
| P-016 | Confirmed | First launch asks only for a non-empty local display name. | Keep onboarding to a short description, name field, and Continue action. A calm default profile icon is assigned automatically and can be changed later in Settings. The profile stays on-device and is not an account. |
| P-017 | Confirmed | The MVP has no Steady backend and keeps core product data on-device. | Optional external APIs may provide contextual data such as weather, but must not receive profile, habit, completion, streak, or coin data. |
| P-018 | Confirmed | Optional weather appears as an icon and temperature beside the Today date. | It is cached, failure-safe, and must not become a dashboard card. |
| P-019 | Confirmed | Settings includes user-initiated email feedback to `ceo@monklabs.ai`. | Open the device email app with subject `Steady feedback`; Steady does not send feedback through a backend. |
| P-020 | Confirmed | Tapping the Steady Coins balance opens a shop containing a one-day Streak Shield. | A shield protects exactly one missed day and is never consumed automatically. |
| P-021 | Confirmed | After a missed day, an owned shield is offered on the next app open. | The user chooses Use shield or Let streak end; use calm, non-judgmental copy. |
| P-022 | Confirmed | Archive and Delete are distinct habit-management actions. | Archive is reversible, removes the habit from Today, and frees an active slot. Restore returns it when a slot is available. Delete permanently removes the named habit and its local history after explicit confirmation. |
| P-023 | Confirmed | Settings includes Reset all data with a two-step explicit confirmation. | Reset erases active and archived habits, profile, onboarding completion, completion/progress, streaks, Steady Coins, Streak Shields, and appearance preference. It closes open UI, selects System default, and immediately reopens first-launch onboarding without restoring demo habits. |
| P-024 | Future | Indicative Steady Plus pricing in India is ₹49–₹79 monthly or ₹399–₹599 annually, with final regional pricing supplied by Google Play Billing. | Pricing remains subject to validation against store fees, taxes, backend costs, and willingness to pay. An early-supporter lifetime purchase may be explored before recurring cloud costs become material. |
| P-025 | Future | Steady may offer an optional email-based account for backup and cross-device synchronization while remaining local-first. | Users can continue without an account. First upload requires informed opt-in. A later account adopts existing local history, and users can disable sync, export data, sign out without deleting the local copy, and separately delete server-side account data. |
| P-026 | Future | Cancelling Steady Plus never removes or hides habit history. | If the account exceeds the Free active-habit limit, existing habits remain readable, but the user must archive habits before creating or restoring another active habit. Exact cloud retention and read-only behavior must be decided before launch. |
| P-027 | Confirmed | MVP records should use stable identifiers and explicit timestamp, deletion, local-date, and timezone semantics where relevant. | This is limited preparation for future migration and sync. It does not authorize speculative backend modules, authentication, sync engines, or unused abstractions during the MVP. |
| P-028 | Confirmed | Free and Steady Plus share the same premium-quality visual design, usability, accessibility, performance, and product voice. | Paywalls gate clearly identified Premium capabilities, not polish. Free must not be intentionally degraded with advertisements, visual clutter, artificial friction, or excessive upgrade prompts. |
| P-029 | Confirmed | The preferred future commercialization path preserves the existing Android application ID, signing identity, Play listing, and local data. | If a company is formed, transfer the existing Play application rather than requiring users to move to a fresh app. A new application ID cannot directly access the old app's private database or inherit its Android backup. |
| P-030 | Confirmed | Steady should provide a versioned, user-controlled export and restore format before user history becomes difficult to move. | The format is independent of Room, uses stable IDs, preserves all meaningful product data and date/time semantics, validates before import, uses the Android system document picker, and defines safe replace/merge behavior. It supports personal backup as well as any unavoidable future app migration. |
| P-031 | Confirmed | Android domain models and repository contracts remain independent of Room entities and any future network DTOs. | Use explicit, tested mapping at persistence and network boundaries. Stable IDs and documented timestamp, deletion, local-date, and timezone semantics make records portable without treating the Room schema as a future API contract or adding unused sync infrastructure. |
| D-001 | Confirmed | The visual direction is warm, calm, and premium. | Deep sage green anchors a soft neutral palette. |
| D-002 | Confirmed | Habit rows are comfortable rounded rows with icons and large completion controls. | Avoid compact checkbox lists and dense grids. |
| D-003 | Confirmed | Completion motion should be restrained and structural. | Respect reduced-motion settings. |
| D-004 | Confirmed | Adding and editing a habit uses a focused full-screen form with a live row preview. | The form includes name, optional Goal or note, icon/color, active days, preferred time or routine cue, and reminder toggle. Goal or note is display-only context and does not change binary completion. |
| D-005 | Confirmed | Habit names locally suggest an icon and color while the user types. | Tapping the icon opens manual selection; a manual choice overrides future automatic suggestions. |
| D-006 | Confirmed | A habit schedule uses either an exact time or a routine cue. | Initial cues include Anytime, After waking, After breakfast, After lunch, After dinner, and Before bed; Steady does not infer when those events occur. |
| D-007 | Confirmed | Completing every scheduled habit triggers one brief whole-day celebration. | The animation is restrained, appears once for the day, shows earned Steady Coins, and respects reduced motion. |
| D-008 | Confirmed | Empty-state Add your first habit opens the creation form directly. | Show the Add/manage choice only when at least one habit exists. |
| D-009 | Confirmed | Settings offers System default, Light, and Dark appearance choices. | System default follows device appearance and reacts while selected; explicit Light or Dark persists locally. All screens and states use intentional Steady palettes with accessible contrast. This does not imply arbitrary Material dynamic color. |
| D-010 | Confirmed | First-launch onboarding is a minimal focused screen before Today. | Show only a concise local-profile description, required name field, and Continue action. Completion leads to empty Today; sample data remains outside onboarding as a clearly labeled prototype control. |
| D-011 | Confirmed | Destructive actions use restrained, readable red danger styling; reversible Archive does not. | Archive uses neutral/warm styling. Permanent Delete and Reset confirmations use red only for the destructive control and related warning context. |
| D-012 | Confirmed | The Steady app mark is a small line-art sapling with a subtle checkmark near its base. | The sapling reads first and the check second, expressing a routine taking root through calm progress and daily return. Use `docs/design/branding/steady-app-icon.svg` as the canonical source until Android adaptive foreground/background and monochrome launcher resources are implemented. Preserve the sage/warm-neutral palette, dark-theme counterpart, small-size legibility, and central adaptive-icon safe area. |
| D-013 | Confirmed | App launch briefly shows the Steady sapling mark with “Small actions shape a steady life.” | Keep the launch moment short and calm before onboarding or Today. Respect reduced motion and do not turn it into a carousel or blocking tutorial. |
| W-001 | Prototype only | The interactive web prototype persists its mutable demonstration state in browser `localStorage`. | This enables add/edit/archive/restore/delete/reset review across reloads. An explicit initialization marker seeds demo data only for a new visitor; a completed reset remains empty. This is simulation scaffolding, not Android persistence architecture. |
| A-001 | Confirmed | The repository remains limited to `app` and `core:designsystem` during design exploration. | Add modules only after a real vertical slice establishes a boundary. |

## Open decisions

- Exact Steady Coins earning amount and whether streaks affect awards
- Final Streak Shield price and inventory limits
- Accidental completion protection for one-way completion
- Pause-day consistency formula
- Final typography
- Future Insights screen hierarchy and metrics
- Final Premium price points, trial/introductory offers, and whether to offer an
  early-supporter lifetime purchase
- Cloud retention and access behavior after a Steady Plus subscription ends
- Future sync conflict-resolution rules and the precise boundary between
  backup processing and optional server-side insight processing
- Portable backup encryption and recovery UX
- Whether restore replaces all local data or supports a reviewed merge
- Whether future redeemable rewards use a separate verified currency
- Which contextual API features belong in the MVP and their permission model
- Whether Open-Meteo is adopted for MVP weather, where attribution appears, and whether location uses coarse permission or manual city selection
