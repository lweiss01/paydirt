# PayDirt Roadmap

This roadmap is adapted from the v3 product spec and reconciled against the current Android codebase as of 2026-03-25.

## Product Direction

PayDirt is meant to make debt payoff feel actionable, not abstract. The core loop is:

1. Show the best next move
2. Make it easy to take that move
3. Reflect the payoff in a satisfying, grounded way
4. Bring the user back with another clear next step

## Current Snapshot

What is already working in the repo:

- Manual card management
- Payment logging
- Payoff optimization and recommendation logic
- Home dashboard
- Card detail flow
- Local persistence and DI setup
- Plaid-ready repository and background worker scaffolding
- Reward screen implementation in code

What is not fully connected yet:

- End-to-end Plaid linking flow
- Full onboarding experience
- Reward screen wired into the main payment flow
- Background refresh scheduling from app launch
- Re-auth notification and recovery flow
- Behavior engine surfaced in the live UI

## Phase 1

### Foundation + Live Data + Reward Loop

Status: In progress

Spec goals:

- Payoff engine with multiple strategies
- Smart APR inference
- Manual card entry fallback
- Plaid linking as the preferred path
- Token exchange via backend
- Weekly background refresh
- Re-auth nudge
- Full onboarding
- First recommendation reveal
- Payment logging
- Reward screen
- Behavior engine
- Cumulative savings tracking
- Monthly goal progress
- Next opportunity suggestion
- APR confidence tracking
- Room migration support

Implemented or substantially present:

- Payoff engine with avalanche, snowball, and hybrid strategies
- Manual card entry
- Payment logging
- Optimizer recommendation flow
- Smart APR infrastructure
- Plaid repository and API contract scaffolding
- Background worker implementation
- Reward screen code
- Plaid-linked data fields in Room
- Room migration from version 1 to 2

Still needed to complete Phase 1:

- Wire the reward screen into the payment experience
- Surface behavior engine outputs in the home flow
- Build the onboarding screens from the spec
- Connect Plaid link / re-auth UI
- Schedule background refresh from app startup
- Add user-facing re-auth notification behavior
- Add goal progress and next-opportunity UI to the main app flow

## Phase 2

### Intelligence + Retention

Status: Planned

Spec themes:

- Smart APR from live transaction history
- Momentum score
- Flex nudges
- Updated recommendations after balance changes
- Payoff curve chart
- Weekly summaries
- Notification-based return loops

Recommended implementation order:

1. Wire `BehaviorEngine` into `HomeViewModel` and payment logging
2. Add payoff curve visualization with Vico
3. Introduce goal progress and momentum surfaces on the home screen
4. Add opt-in nudges and notification plumbing
5. Expand live-data APR inference once Plaid linking is fully active

## Phase 3

### Advanced Features

Status: Planned

Focus areas:

- One-time extra payment scenarios
- Balance transfer analysis
- Debt consolidation comparisons
- Milestone moments
- Due date reminders
- Optional MX fallback if Plaid coverage is insufficient

## Phase 4

### Ship

Status: Future

Focus areas:

- Play Store launch readiness
- Share / export surfaces
- Widget and deep-link support
- Marketing site and distribution polish
- iOS evaluation

## Next Best Moves

If we want the fastest path to a stronger MVP, the next most valuable steps are:

1. Wire the reward screen after payment logging so the payoff loop becomes visible
2. Add onboarding and explicit manual-vs-linked entry paths
3. Connect Plaid link, refresh scheduling, and re-auth flow end-to-end
4. Surface behavior engine metrics on the home screen
5. Add a small set of UI and domain tests around the payoff and reward flows
