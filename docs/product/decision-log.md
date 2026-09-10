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
| D-001 | Confirmed | The visual direction is warm, calm, and premium. | Deep sage green anchors a soft neutral palette. |
| D-002 | Confirmed | Habit rows are comfortable rounded rows with icons and large completion controls. | Avoid compact checkbox lists and dense grids. |
| D-003 | Confirmed | Completion motion should be restrained and structural. | Respect reduced-motion settings. |
| A-001 | Confirmed | The repository remains limited to `app` and `core:designsystem` during design exploration. | Add modules only after a real vertical slice establishes a boundary. |

## Open decisions

- Exact Steady Coins earning amount and whether streaks affect awards
- Accidental completion protection for one-way completion
- Pause-day consistency formula
- Final typography and dark theme
- Habit creation and editing flow
- Insights screen hierarchy
- Premium pricing and downgrade behavior
- Whether future redeemable rewards use a separate verified currency

