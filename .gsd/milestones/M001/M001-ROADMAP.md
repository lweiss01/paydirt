# M001: Core Android Product Loop

**Vision:** Turn the existing PayDirt Android app into a coherent, satisfying manual payoff experience that clearly shows the next best move, makes progress visible right after action, and preserves the product's calm, direct, unembarrassing tone.

## Success Criteria

- A new manual-entry user can enter cards, reach a first recommendation, and understand what to do next without confusion.
- Logging a payment produces an immediate, grounded reward/progress response tied to the actual payoff impact.
- The home screen reflects the user's current debt picture with a concrete next move and useful progress framing.
- The main manual path feels like one product loop rather than separate screens that happen to exist.

## Key Risks / Unknowns

- Reward flow wiring may be broader than expected because the current navigation graph does not include the reward screen.
- Home state may need meaningful expansion to surface progress, momentum, and trust cues without becoming noisy.
- First-run/manual-user onboarding may require structural UI changes to make the product understandable from app open.
- Tone can regress if progress and behavior features drift toward gamification or generic finance-app dashboard patterns.

## Proof Strategy

- Reward flow wiring risk → retire in S01 by proving a real payment log can land in a reward/result surface with payoff impact.
- Home state expansion risk → retire in S02 by proving the home screen can show progress and a next move from live persisted state.
- First-run path risk → retire in S03 by proving a new manual user can reach a recommendation through a guided entry path.
- Tone regression risk → retire in S07 by proving the assembled manual flow still feels calm, direct, and grounded after all slices are integrated.

## Verification Classes

- Contract verification: Gradle build, relevant unit tests, artifact and wiring checks against real modules
- Integration verification: Android runtime walkthrough of manual onboarding/entry, recommendation, payment logging, reward, and updated home state
- Operational verification: none
- UAT / human verification: sanity check that tone and clarity still feel right in the assembled flow

## Milestone Definition of Done

This milestone is complete only when all are true:

- all slice deliverables are complete
- shared components are actually wired together
- the real entrypoint exists and is exercised
- success criteria are re-checked against live behavior, not just artifacts
- final integrated acceptance scenarios pass

## Requirement Coverage

- Covers: R001, R002, R003, R004, R005, R006, R007, R008
- Partially covers: none
- Leaves for later: R009, R010, R011, R012, R013, R014, R015, R016, R017, R018, R019, R020, R021, R022, R023, R024, R025, R026, R027, R028
- Orphan risks: none

## Slices

- [ ] **S01: Reward loop wiring after payment logging** `risk:high` `depends:[]`
  > After this: logging a payment in the live app can reach a real reward/result flow instead of ending silently in card detail.

- [ ] **S02: Home progress and next-move surfaces** `risk:medium` `depends:[S01]`
  > After this: home shows a clearer next move plus concrete progress signals from persisted app state.

- [ ] **S03: Manual-user onboarding and first recommendation reveal** `risk:high` `depends:[S01]`
  > After this: a new manual-entry user can get to a first recommendation through a guided path instead of piecing the flow together manually.

- [ ] **S04: Goal-setting and payoff framing** `risk:low` `depends:[S02,S03]`
  > After this: users can set a payoff goal and see progress framed against it in the main loop.

- [ ] **S05: Behavior engine surfaced in live UI** `risk:medium` `depends:[S02]`
  > After this: momentum, payoff impact, and next-opportunity signals show up in real app surfaces instead of remaining latent in domain code.

- [ ] **S06: APR confidence and trust cues** `risk:low` `depends:[S02,S03]`
  > After this: recommendation-related UI communicates confidence and ambiguity honestly where it matters.

- [ ] **S07: Core flow polish and verification** `risk:medium` `depends:[S01,S02,S03,S04,S05,S06]`
  > After this: the manual path works cleanly from entry to recommendation to payment log to updated progress, with verification evidence that the assembled loop holds together.

## Boundary Map

### S01 → S02

Produces:
- navigation path from payment logging to reward/result flow
- reward flow contract carrying `BehaviorEngine.PaymentImpact`-driven state into UI
- post-payment completion path back into the main app loop

Consumes:
- nothing (first slice)

### S01 → S03

Produces:
- a proven post-action payoff loop that onboarding/reveal can target as part of the user promise
- reward-oriented payoff framing that clarifies what the app does after user action

Consumes:
- nothing (first slice)

### S02 → S04

Produces:
- home-state surfaces for progress, cumulative payoff feedback, and next-move framing
- expanded `HomeViewModel` state shape or equivalent integration surface for progress-oriented UI

Consumes from S01:
- reward loop output and post-payment return path

### S02 → S05

Produces:
- primary home integration point where behavior-derived state can be surfaced
- baseline progress slots for momentum/opportunity content

Consumes from S01:
- post-payment loop that can refresh home-visible progress

### S02 → S06

Produces:
- recommendation surfaces where confidence/trust cues can be inserted without inventing new destinations

Consumes from S01:
- working payoff loop context for where trust messaging matters most

### S03 → S04

Produces:
- first-run/manual-entry path that establishes when and where users encounter payoff framing
- first recommendation reveal flow usable as the anchor for later goal framing

Consumes from S01:
- reward/payoff language patterns already established in the product loop

### S03 → S06

Produces:
- first-run surfaces where confidence cues may need to appear for manual-entry users

Consumes from S01:
- payoff loop framing established in the live app

### S04 → S07

Produces:
- goal-setting and progress framing integrated into the main loop

Consumes from S02:
- home progress surfaces
Consumes from S03:
- onboarding and first recommendation flow

### S05 → S07

Produces:
- visible behavior/momentum/opportunity signals integrated into live screens

Consumes from S02:
- expanded home surface and state integration points

### S06 → S07

Produces:
- confidence and trust cues attached to the recommendation flow

Consumes from S02:
- home recommendation surfaces
Consumes from S03:
- first-run recommendation reveal surfaces
