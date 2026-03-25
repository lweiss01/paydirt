# M001: Core Android Product Loop

**Gathered:** 2026-03-25
**Status:** Ready for planning

## Project Description

PayDirt already has a working Android foundation for manual credit-card payoff planning. Users can add cards, log payments, inspect card detail, and view payoff recommendations and strategy comparisons. What is missing is the assembled product loop that makes the app feel complete: clearer first-time entry, immediate post-payment payoff feedback, progress framing, live behavior surfaces, and honest trust cues around estimated data.

## Why This Milestone

This milestone turns the current codebase from a capable MVP foundation into a coherent product experience. It should prove that PayDirt is useful and emotionally right before deeper linked-account work or launch packaging. The app's tone — calm, direct, unembarrassing, specific — is part of the milestone, not polish for later.

## User-Visible Outcome

### When this milestone is complete, the user can:

- enter their cards manually, understand what PayDirt wants them to do, and reach a clear first recommendation
- log a payment and immediately see grounded payoff feedback, progress, and the next useful move
- return to the home screen and understand their current debt picture without digging through dashboards

### Entry point / environment

- Entry point: Android app
- Environment: local dev Android runtime / emulator / device
- Live dependencies involved: local Room database only for the main M001 proof path

## Completion Class

- Contract complete means: the app builds, relevant tests pass, and the planned surfaces and wiring exist with real implementation rather than stubs
- Integration complete means: the manual user path works across navigation, persistence, payoff calculation, reward/progress surfaces, and home guidance in a real app session
- Operational complete means: none beyond standard Android app launch and state persistence for this milestone

## Final Integrated Acceptance

To call this milestone complete, we must prove:

- a new manual-entry user can launch the app, add debt inputs, receive a first recommendation, and understand the next action to take
- logging a payment updates the visible user experience through reward, progress, or next-opportunity feedback grounded in real numbers
- the home screen, card detail flow, and reward/progress surfaces agree on the current debt state after user actions
- the milestone is exercised in a real Android runtime, not only through static artifact checks

## Risks and Unknowns

- Reward surface exists but is not yet in the navigation flow — without real wiring, the strongest product moment stays invisible
- HomeViewModel currently centers on balance totals and quick plans — progress, goal, and behavior surfaces may need non-trivial state expansion
- Onboarding is not yet a clear first-run path — without it, the manual experience may still feel like an internal tool rather than a finished product
- Tone drift is easy — adding progress and behavior surfaces could accidentally make the app feel noisy, gamified, or preachy

## Existing Codebase / Prior Art

- `app/src/main/java/com/lweiss01/paydirt/ui/navigation/NavGraph.kt` — current app navigation; reward flow is not yet represented here
- `app/src/main/java/com/lweiss01/paydirt/ui/screens/home/HomeViewModel.kt` — current home state and quick-plan logic; likely primary integration point for richer progress surfaces
- `app/src/main/java/com/lweiss01/paydirt/ui/screens/reward/RewardScreen.kt` — substantial reward UI already exists and expects `BehaviorEngine.PaymentImpact`
- `app/src/main/java/com/lweiss01/paydirt/domain/engine/BehaviorEngine.kt` — existing engine for payment impact, momentum, goal progress, and next-opportunity state
- `app/src/main/java/com/lweiss01/paydirt/ui/components/APRConfidenceBadge.kt` — existing UI for honest APR confidence communication
- `app/src/main/java/com/lweiss01/paydirt/ui/screens/cards/CardDetailScreen.kt` — current payment logging entry point in the live flow

> See `.gsd/DECISIONS.md` for all architectural and pattern decisions — it is an append-only register; read it during planning, append to it during execution.

## Relevant Requirements

- R001 — establish a coherent manual payoff path from entry to recommendation
- R002 — make payment logging feed a visible reward loop
- R003 — surface the best next move on home from current debt state
- R004 — show progress in concrete, non-dashboard language
- R005 — introduce goal framing in the live app
- R006 — expose behavior/opportunity state in real UI
- R007 — communicate uncertainty honestly through confidence cues
- R008 — preserve the intended calm, direct, unembarrassing tone

## Scope

### In Scope

- wiring the existing reward surface into the real payment flow
- improving the home experience so it reflects progress and the next move more clearly
- adding onboarding or first-recommendation reveal for manual users
- surfacing goal progress, momentum, and opportunity data where it materially improves clarity
- using existing APR confidence work where it helps trust in the core flow
- milestone-level polish and verification of the complete manual loop

### Out of Scope / Non-Goals

- full Plaid linking and re-auth flows
- background refresh scheduling and linked-account lifecycle behavior
- website implementation
- Google Play release packaging and submission
- advanced payoff tools such as balance transfers or consolidation analysis

## Technical Constraints

- Build forward from existing Compose, Hilt, Room, and domain-engine patterns rather than redesigning architecture
- Reward and behavior flows should use the existing `BehaviorEngine` and `RewardScreen` surfaces when possible
- The milestone proof path should remain valid with local/manual data only
- Tone constraints are hard constraints: no shame, fake cheerleading, streak pressure, or generic finance-dashboard complexity

## Integration Points

- Android navigation graph — reward/onboarding/home flow assembly
- Room-backed card and payment repositories — state persistence across the core loop
- `BehaviorEngine` — payment impact, momentum, goal progress, and next-opportunity computation
- payoff engine and quick-plan calculation — recommendation logic already shown on home/optimizer surfaces

## Open Questions

- Should onboarding live as a dedicated first-run flow or a lighter first-recommendation reveal inside the current navigation structure? — current thinking: whichever preserves a clean manual path with the least navigation churn
- How much of APR confidence should appear in M001 versus later linked-data milestones? — current thinking: enough to build trust in the manual path without overloading the UI
- Which home metrics are truly essential for clarity? — current thinking: next move, progress, and one or two concrete payoff stats are more important than broad summaries
