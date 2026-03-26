# M001: Core Android Product Loop

**Gathered:** 2026-03-25
**Status:** Ready for planning

## Project Description

PayDirt already has a working Android foundation for manual credit-card payoff planning. Users can add cards, log payments, inspect card detail, and view payoff recommendations and strategy comparisons. What is missing is the assembled product loop that makes the app feel complete: a short trust-building first-run path, immediate post-payment payoff feedback, best-move-first home guidance, and calm honesty around estimated data.

## Why This Milestone

This milestone turns the current codebase from a capable MVP foundation into a coherent product experience. It should prove that PayDirt is useful and emotionally right before deeper linked-account work or launch packaging. The app's tone — calm, direct, unembarrassing, specific — is part of the milestone, not polish for later.

## User-Visible Outcome

### When this milestone is complete, the user can:

- enter card details manually, even with partial information, and still reach an honest first recommendation quickly
- log a payment and immediately see grounded payoff feedback plus the best next move
- return to the home screen and understand their current best move with enough context to trust it, without digging through dashboards

### Entry point / environment

- Entry point: Android app
- Environment: local dev Android runtime / emulator / device
- Live dependencies involved: local Room database only for the main M001 proof path

## Completion Class

- Contract complete means: the app builds, relevant tests pass, and the planned surfaces and wiring exist with real implementation rather than stubs
- Integration complete means: the manual user path works across onboarding, card entry, persistence, payoff calculation, reward/progress surfaces, and home guidance in a real app session
- Operational complete means: none beyond standard Android app launch and state persistence for this milestone

## Final Integrated Acceptance

To call this milestone complete, we must prove:

- a new manual-entry user can launch the app, read a short trust-building intro, add at least one card, receive a recommendation quickly, and understand what to do next
- logging a payment updates the visible user experience through a full reward screen that leads with real payoff impact and suggests the best next move
- the home screen, card detail flow, and reward/progress surfaces agree on the current debt state after user actions
- the milestone is exercised in a real Android runtime, not only through static artifact checks

## Risks and Unknowns

- Reward surface exists but is not yet in the navigation flow — without real wiring, the strongest product moment stays invisible
- HomeViewModel currently centers on balance totals and quick plans — best-move-first context, progress framing, and confidence cues may need non-trivial state expansion
- Onboarding is not yet a clear first-run path — without it, the manual experience may still feel like an internal tool rather than a finished product
- Partial-data support increases trust requirements — if estimates and lower-confidence recommendations are not surfaced clearly, the product will feel misleading
- Tone drift is easy — adding progress and behavior surfaces could accidentally make the app feel noisy, gamified, or preachy

## Existing Codebase / Prior Art

- `app/src/main/java/com/lweiss01/paydirt/ui/navigation/NavGraph.kt` — current app navigation; reward and onboarding flows are not yet represented here
- `app/src/main/java/com/lweiss01/paydirt/ui/screens/home/HomeViewModel.kt` — current home state and quick-plan logic; likely primary integration point for richer best-move and progress surfaces
- `app/src/main/java/com/lweiss01/paydirt/ui/screens/reward/RewardScreen.kt` — substantial reward UI already exists and expects `BehaviorEngine.PaymentImpact`
- `app/src/main/java/com/lweiss01/paydirt/domain/engine/BehaviorEngine.kt` — existing engine for payment impact, momentum, goal progress, and next-opportunity state
- `app/src/main/java/com/lweiss01/paydirt/ui/components/APRConfidenceBadge.kt` — existing UI for honest APR confidence communication
- `app/src/main/java/com/lweiss01/paydirt/ui/screens/cards/CardDetailScreen.kt` and `CardDetailViewModel.kt` — current payment logging entry point in the live flow
- `app/src/main/java/com/lweiss01/paydirt/ui/screens/cards/AddEditCardScreen.kt` and `AddEditCardViewModel.kt` — current card-entry flow to adapt for partial-data support

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

- wiring the existing reward surface into the real payment flow after every logged payment
- improving the home experience so it leads with the best move and enough context to trust it without becoming dashboard-y
- adding one short trust-building onboarding intro and earliest-possible first recommendation behavior for manual users
- adapting manual card entry so balance may be estimated and APR/minimum payment may be blank or estimated
- surfacing goal progress, momentum, and opportunity data where it materially improves clarity
- using existing APR confidence work where it helps trust in the core flow
- milestone-level polish and verification of the complete manual loop

### Out of Scope / Non-Goals

- full Plaid linking and re-auth flows
- background refresh scheduling and linked-account lifecycle behavior
- website implementation
- Google Play release packaging and submission
- advanced payoff tools such as balance transfers or consolidation analysis
- charts or dashboard-style home expansion in M001

## Technical Constraints

- Build forward from existing Compose, Hilt, Room, and domain-engine patterns rather than redesigning architecture
- Reward and behavior flows should use the existing `BehaviorEngine` and `RewardScreen` surfaces when possible
- The milestone proof path should remain valid with local/manual data only
- Recommendations should appear as soon as the app has enough data, even with one card and partial inputs
- Recommendation surfaces must carry honest confidence and estimate labeling when underlying inputs are incomplete
- Tone constraints are hard constraints: no shame, fake cheerleading, streak pressure, or generic finance-dashboard complexity

## Integration Points

- Android navigation graph — reward/onboarding/home flow assembly
- Room-backed card and payment repositories — state persistence across the core loop
- `BehaviorEngine` — payment impact, momentum, goal progress, and next-opportunity computation
- payoff engine and quick-plan calculation — recommendation logic already shown on home/optimizer surfaces
- add/edit card flow — partial-data rules and helper copy for missing APR/minimum payment

## Open Questions

- What exact copy should the intro use to establish trust, partial-data tolerance, and local/manual safety without sounding legalistic? — current thinking: short, plain, advisor-like language
- How much deeper math should be visible inline on reward/home versus tucked behind an explanation affordance? — current thinking: brief reason by default, deeper math on demand
- How should missing minimum payment be estimated in M001 when only balance is known? — current thinking: existing estimation logic may be reusable, but should remain clearly marked as estimated
