# Product Decision Log

This is the source of truth for confirmed product behavior. Update an existing
entry when a decision changes and preserve the previous direction in its notes.

| ID | Status | Decision | Notes |
|---|---|---|---|
| P-001 | Confirmed | Steady is a recurring habit tracker, not a general task manager. | One-off tasks and projects are outside the MVP. |
| P-002 | Confirmed | Habit completion is binary. | Quantities may appear in the habit name or supporting text but are not logged numerically. |
| P-003 | Confirmed | The MVP works on-device without required sign-in. | Cloud sync is outside MVP scope. |
| P-004 | Confirmed | The Today checklist is the main home-screen content. | Rewards and analytics are supporting content. |
| P-005 | Confirmed | The initial navigation has Today, Insights, and Settings. | Habit management opens from Today. |
| P-006 | Confirmed | Completed habits are struck through, greyed out, locked, and moved to the bottom. | Users cannot undo completion for that day. |
| P-007 | Confirmed | The motivational reward is called Steady Coins. | MVP coins have no monetary or redeemable value. |
| P-008 | Confirmed | A habit/date can award coins only once. | Balance must derive from deterministic earning transactions rather than a freely editable total. |
| P-009 | Confirmed | The MVP is free and supports up to seven non-archived habits. | Paused habits still consume a slot; archived habits are unlimited. |
| P-010 | Future | A possible subscription model allows four habits on Free and ten on Premium. | Do not implement during MVP. |
| P-011 | Confirmed | Paused days preserve streaks but reduce consistency. | The exact reduction formula remains undecided. |
| P-012 | Confirmed | Users choose the completion threshold required to maintain their streak. | Threshold selection UX is not designed yet. |
| P-013 | Confirmed | Reminder times can be suggested from local completion history. | Suggestions require user acceptance and support opt-out. |
| P-014 | Confirmed | Travel and temporary pauses are supported. | Paused habits do not send reminders. |
| P-015 | Confirmed | Insights is a Coming soon page in the MVP. | Functional analytics and insight reports are deferred. |
| P-016 | Confirmed | Settings allows the user to edit a local display name and profile icon. | The profile is not an account and never requires network access. |
| P-017 | Confirmed | The MVP has no Steady backend and keeps core product data on-device. | Optional external APIs may provide contextual data such as weather, but must not receive profile, habit, completion, streak, or coin data. |
| P-018 | Confirmed | Optional weather appears as an icon and temperature beside the Today date. | It is cached, failure-safe, and must not become a dashboard card. |
| D-001 | Confirmed | The visual direction is warm, calm, and premium. | Deep sage green anchors a soft neutral palette. |
| D-002 | Confirmed | Habit rows are comfortable rounded rows with icons and large completion controls. | Avoid compact checkbox lists and dense grids. |
| D-003 | Confirmed | Completion motion should be restrained and structural. | Respect reduced-motion settings. |
| D-004 | Confirmed | Adding and editing a habit uses a focused full-screen form with a live row preview. | The form includes name, icon/color, active days, preferred time, and reminder toggle. |
| D-005 | Confirmed | Habit names locally suggest an icon and color while the user types. | Tapping the icon opens manual selection; a manual choice overrides future automatic suggestions. |
| D-006 | Confirmed | A habit schedule uses either an exact time or a routine cue. | Initial cues include Anytime, After waking, After breakfast, After lunch, After dinner, and Before bed; Steady does not infer when those events occur. |
| D-007 | Confirmed | Completing every scheduled habit triggers one brief whole-day celebration. | The animation is restrained, appears once for the day, shows earned Steady Coins, and respects reduced motion. |
| A-001 | Confirmed | The repository remains limited to `app` and `core:designsystem` during design exploration. | Add modules only after a real vertical slice establishes a boundary. |

## Open decisions

- Exact Steady Coins earning amount and whether streaks affect awards
- Accidental completion protection for one-way completion
- Pause-day consistency formula
- Final typography and dark theme
- Habit creation and editing flow
- Future Insights screen hierarchy and metrics
- Premium pricing and downgrade behavior
- Whether future redeemable rewards use a separate verified currency
- Which contextual API features belong in the MVP and their permission model
- Whether Open-Meteo is adopted for MVP weather, where attribution appears, and whether location uses coarse permission or manual city selection
