# Requirements

This file is the explicit capability and coverage contract for the project.

Use it to track what is actively in scope, what has been validated by completed work, what is intentionally deferred, and what is explicitly out of scope.

Guidelines:
- Keep requirements capability-oriented, not a giant feature wishlist.
- Requirements should be atomic, testable, and stated in plain language.
- Every **Active** requirement should be mapped to a slice, deferred, blocked with reason, or moved out of scope.
- Each requirement should have one accountable primary owner and may have supporting slices.
- Research may suggest requirements, but research does not silently make them binding.
- Validation means the requirement was actually proven by completed work and verification, not just discussed.

## Active

### R001 — Manual payoff planning works end-to-end
- Class: primary-user-loop
- Status: active
- Description: A manual-entry user can add cards, receive payoff guidance, and use the app's core planning loop without linked accounts.
- Why it matters: This is the core product value and the minimum useful path.
- Source: user
- Primary owning slice: M001/S03
- Supporting slices: M001/S02, M001/S07
- Validation: mapped
- Notes: Must feel like a coherent product path, not a set of disconnected screens.

### R002 — Payment logging feeds a visible payoff/reward loop
- Class: core-capability
- Status: active
- Description: Logging a payment changes what the user sees through an immediate reward/result surface tied to payoff impact.
- Why it matters: The product should make progress feel concrete right after action.
- Source: user
- Primary owning slice: M001/S01
- Supporting slices: M001/S07
- Validation: mapped
- Notes: Reward flow should stay restrained and grounded in real numbers.

### R003 — Home screen shows a clear best next move from current debt state
- Class: core-capability
- Status: active
- Description: The home surface reflects current debt state and points the user to the best next payoff move.
- Why it matters: Decision clarity is the product's central promise.
- Source: user
- Primary owning slice: M001/S02
- Supporting slices: M001/S05
- Validation: mapped
- Notes: Should not collapse into a generic finance dashboard.

### R004 — Progress is visible in concrete terms, not abstract dashboards
- Class: differentiator
- Status: active
- Description: The app shows progress with concrete payoff language such as savings, goal progress, or next opportunity rather than vague analytics.
- Why it matters: The product should feel useful and specific, not abstract.
- Source: user
- Primary owning slice: M001/S02
- Supporting slices: M001/S04, M001/S05
- Validation: mapped
- Notes: Tone and framing matter as much as raw data availability.

### R005 — Users can set and track a payoff goal
- Class: core-capability
- Status: active
- Description: The user can define a payoff goal and see their progress framed against that goal.
- Why it matters: Goals give context to recommendations and reward surfaces.
- Source: inferred
- Primary owning slice: M001/S04
- Supporting slices: M001/S02
- Validation: mapped
- Notes: Goal framing should support the calm/direct tone rather than feel gamified.

### R006 — App guidance surfaces behavior and opportunity signals from live state
- Class: differentiator
- Status: active
- Description: Behavior-derived signals such as momentum, opportunity, or payoff impact appear in real app flows.
- Why it matters: This is part of what makes PayDirt feel alive rather than static.
- Source: user
- Primary owning slice: M001/S05
- Supporting slices: M001/S01, M001/S02
- Validation: mapped
- Notes: Existing BehaviorEngine and reward copy create a base to build from.

### R007 — Recommendation UI communicates confidence and uncertainty honestly
- Class: failure-visibility
- Status: active
- Description: APR estimates and other uncertain payoff inputs are surfaced with confidence cues instead of being presented as exact when they are not.
- Why it matters: Trust depends on showing ambiguity honestly.
- Source: inferred
- Primary owning slice: M001/S06
- Supporting slices: M001/S02, M001/S03
- Validation: mapped
- Notes: Existing APR confidence component suggests a near-term implementation path.

### R008 — Core manual flow feels calm, direct, and unembarrassing
- Class: quality-attribute
- Status: active
- Description: The manual entry path, payoff guidance, reward surfaces, and copy all preserve the intended product tone.
- Why it matters: Tone is part of product correctness for this app.
- Source: user
- Primary owning slice: M001/S07
- Supporting slices: M001/S01, M001/S02, M001/S03, M001/S04, M001/S05, M001/S06
- Validation: mapped
- Notes: This includes avoiding shame, fake cheerleading, and noisy UX.

### R009 — Users can link financial accounts through Plaid
- Class: integration
- Status: active
- Description: The app supports initiating and completing Plaid-backed account linking.
- Why it matters: Linked data is part of the intended product direction beyond manual entry.
- Source: user
- Primary owning slice: M002/S01
- Supporting slices: M002/S02
- Validation: mapped
- Notes: Depends on backend token exchange support already implied by repo docs.

### R010 — Users can manage linked account state in the app
- Class: integration
- Status: active
- Description: The app shows linked accounts and gives the user a way to understand or manage connected state.
- Why it matters: Linking without visibility or management creates confusion.
- Source: inferred
- Primary owning slice: M002/S02
- Supporting slices: M002/S04
- Validation: mapped
- Notes: Includes unlinking and connected-state feedback where applicable.

### R011 — Linked data refresh runs reliably through app lifecycle
- Class: operability
- Status: active
- Description: Linked account refresh scheduling is actually wired from the app lifecycle and runs with expected constraints.
- Why it matters: Linked-account usefulness depends on current data without manual babysitting.
- Source: inferred
- Primary owning slice: M002/S03
- Supporting slices: M002/S06
- Validation: mapped
- Notes: BackgroundRefreshWorker already exists; scheduling/wiring does not appear complete yet.

### R012 — Re-auth failures are visible and recoverable
- Class: failure-visibility
- Status: active
- Description: If linked data requires re-authentication, the app surfaces it clearly and gives the user a path to recover.
- Why it matters: Silent stale data breaks trust.
- Source: inferred
- Primary owning slice: M002/S04
- Supporting slices: M002/S03, M002/S06
- Validation: mapped
- Notes: Notification and in-app recovery path both matter.

### R013 — Live linked data materially affects recommendations
- Class: core-capability
- Status: active
- Description: Refreshed linked balances and liabilities influence the recommendations shown to the user.
- Why it matters: Live data only matters if it changes the actual guidance.
- Source: user
- Primary owning slice: M002/S05
- Supporting slices: M002/S03
- Validation: mapped
- Notes: Must prove real wiring, not just data sync in isolation.

### R014 — Existing and upgrading users keep working through migrations and mixed data modes
- Class: continuity
- Status: active
- Description: Existing users, upgraded installs, and manual-plus-linked combinations continue working through schema and flow changes.
- Why it matters: Brownfield evolution should not strand current users.
- Source: inferred
- Primary owning slice: M002/S06
- Supporting slices: M002/S01, M002/S02, M002/S03, M002/S04, M002/S05
- Validation: mapped
- Notes: Existing Room migration support suggests this needs explicit hardening.

### R015 — Website clearly explains what PayDirt is and who it is for
- Class: launchability
- Status: active
- Description: The website gives a straightforward explanation of the app's value, audience, and core job.
- Why it matters: Launch needs a simple public-facing explanation surface.
- Source: user
- Primary owning slice: M003/S02
- Supporting slices: M003/S01
- Validation: mapped
- Notes: Website scope is intentionally simple and marketing-focused.

### R016 — Website establishes trust around privacy and product expectations
- Class: launchability
- Status: active
- Description: The website gives visitors enough context to trust the product and understand what it does and does not do.
- Why it matters: Finance-adjacent products need trust surfaces even when the site is simple.
- Source: inferred
- Primary owning slice: M003/S03
- Supporting slices: M003/S02
- Validation: mapped
- Notes: Likely includes privacy and support-oriented content.

### R017 — Website supports launch with clear app and store CTA paths
- Class: launchability
- Status: active
- Description: The marketing site provides clear routes to install, learn more, or follow launch progress.
- Why it matters: The site should support acquisition, not just exist.
- Source: inferred
- Primary owning slice: M003/S05
- Supporting slices: M003/S02, M003/S04
- Validation: mapped
- Notes: CTA details may evolve before launch.

### R018 — Website may collect launch-interest signups for notification
- Class: launchability
- Status: active
- Description: The website may optionally include a simple way for interested visitors to sign up to hear about launch.
- Why it matters: This was explicitly mentioned as a possible part of the launch site.
- Source: user
- Primary owning slice: M003/S05
- Supporting slices: none
- Validation: mapped
- Notes: Optional capability; if omitted, the site still needs to support launch clearly.

### R019 — Website is simple, deployed, and launch-ready
- Class: launchability
- Status: active
- Description: The website is intentionally small in scope, deployed, responsive, and ready to support launch.
- Why it matters: The site should help launch without becoming a second product.
- Source: user
- Primary owning slice: M003/S06
- Supporting slices: M003/S02, M003/S03, M003/S05
- Validation: mapped
- Notes: Avoid scope creep into a large web application.

### R020 — Android release builds can be signed and distributed through Play testing tracks
- Class: operability
- Status: active
- Description: The Android app can be produced as a signed release artifact suitable for internal or open Play testing.
- Why it matters: Store readiness starts with a real release artifact.
- Source: user
- Primary owning slice: M004/S01
- Supporting slices: M004/S05
- Validation: mapped
- Notes: Includes signing and release configuration, not just debug builds.

### R021 — Store listing assets and copy are ready for Play submission
- Class: launchability
- Status: active
- Description: Required screenshots, metadata, descriptions, and visual assets exist for Play Console submission.
- Why it matters: Submission cannot move without complete listing materials.
- Source: user
- Primary owning slice: M004/S02
- Supporting slices: M003/S04
- Validation: mapped
- Notes: Real app assets are preferable to placeholders.

### R022 — App meets baseline policy, privacy, and compliance expectations for Play submission
- Class: compliance/security
- Status: active
- Description: The app and related disclosures meet baseline expectations needed for Play review.
- Why it matters: Store submission can fail on policy and disclosure gaps even when the app works.
- Source: inferred
- Primary owning slice: M004/S03
- Supporting slices: M003/S03
- Validation: mapped
- Notes: Exact requirements may shift when release work begins.

### R023 — Core app flows pass a launch-readiness QA gate
- Class: quality-attribute
- Status: active
- Description: The primary user flows are verified on real devices/build variants before launch.
- Why it matters: Launch quality depends on more than compile/test success.
- Source: inferred
- Primary owning slice: M004/S04
- Supporting slices: M004/S05
- Validation: mapped
- Notes: Should focus on real blocker coverage rather than vanity test counts.

### R024 — App can be submitted to Google Play with complete release materials
- Class: launchability
- Status: active
- Description: The project reaches a state where the Play submission package is complete and ready to shepherd through review.
- Why it matters: This is an explicit part of the requested scope.
- Source: user
- Primary owning slice: M004/S06
- Supporting slices: M004/S01, M004/S02, M004/S03, M004/S04, M004/S05
- Validation: mapped
- Notes: Includes practical review support, not just asset creation.

### R025 — Recommendations get smarter over time from richer data and heuristics
- Class: differentiator
- Status: active
- Description: Recommendation quality improves from richer inputs and smarter interpretation over time.
- Why it matters: Long-term product value depends on becoming more helpful, not only more complete.
- Source: user
- Primary owning slice: M005/S01
- Supporting slices: M005/S03
- Validation: mapped
- Notes: Sequenced after launch-critical work.

### R026 — Progress and momentum feel grounded rather than gamified
- Class: differentiator
- Status: active
- Description: Progress surfaces make the user feel oriented and encouraged without streak pressure or finance-app gamification.
- Why it matters: This preserves the product's tone while improving retention.
- Source: user
- Primary owning slice: M005/S02
- Supporting slices: M005/S05
- Validation: mapped
- Notes: Builds on the tone constraints already established in M001.

### R027 — Users can see payoff trajectory and consequences visually
- Class: differentiator
- Status: active
- Description: The app includes visual forecasting that helps users understand payoff consequences over time.
- Why it matters: Visual explanation can make strategy choices easier to understand.
- Source: inferred
- Primary owning slice: M005/S04
- Supporting slices: M005/S01, M005/S03
- Validation: mapped
- Notes: Existing Vico dependency supports later chart work.

### R028 — Users can opt into restrained return-loop reminders
- Class: continuity
- Status: active
- Description: Users can choose to receive limited, useful reminders or summaries that bring them back with a clear next step.
- Why it matters: Retention should be helpful, not nagging.
- Source: inferred
- Primary owning slice: M005/S06
- Supporting slices: M005/S05
- Validation: mapped
- Notes: Opt-in and tone discipline are part of the requirement.

## Validated

_None yet._

## Deferred

### R029 — Balance transfer analysis
- Class: differentiator
- Status: deferred
- Description: The product may later evaluate balance transfer scenarios.
- Why it matters: Could be valuable advanced decision support once the main product is stable.
- Source: research
- Primary owning slice: none
- Supporting slices: none
- Validation: unmapped
- Notes: Deferred to avoid diluting the main payoff loop and launch path.

### R030 — Debt consolidation scenario modeling
- Class: differentiator
- Status: deferred
- Description: The product may later compare consolidation scenarios against current payoff options.
- Why it matters: This could broaden the decision support layer in a later milestone.
- Source: research
- Primary owning slice: none
- Supporting slices: none
- Validation: unmapped
- Notes: Deferred behind core app completeness and launch work.

### R031 — Due date reminders
- Class: continuity
- Status: deferred
- Description: The product may later notify users about due dates.
- Why it matters: Potentially useful, but not central to the current payoff recommendation promise.
- Source: research
- Primary owning slice: none
- Supporting slices: none
- Validation: unmapped
- Notes: Deferred to avoid turning early retention into generic nagging.

### R032 — Export or share summary flow
- Class: launchability
- Status: deferred
- Description: The product may later let users export or share payoff summaries.
- Why it matters: Useful for later growth or support, but not required for current launch scope.
- Source: inferred
- Primary owning slice: none
- Supporting slices: none
- Validation: unmapped
- Notes: Deferred behind launch-critical work.

### R033 — Widget or deep-link support
- Class: continuity
- Status: deferred
- Description: The product may later add widgets or deep-link entrypoints.
- Why it matters: Could improve return frequency and convenience.
- Source: research
- Primary owning slice: none
- Supporting slices: none
- Validation: unmapped
- Notes: Deferred to keep early launch scope disciplined.

### R034 — Optional alternate aggregation provider beyond Plaid
- Class: integration
- Status: deferred
- Description: The product may later support another account aggregation provider if Plaid coverage is insufficient.
- Why it matters: Useful hedge, but premature until the main linked-data path is real.
- Source: research
- Primary owning slice: none
- Supporting slices: none
- Validation: unmapped
- Notes: Deferred until real-world integration feedback exists.

## Out of Scope

### R035 — Shame-based motivation, streak pressure, or cheerleading UX
- Class: anti-feature
- Status: out-of-scope
- Description: The product should not use guilt, hype, streaks, or congratulatory finance-app tropes to drive engagement.
- Why it matters: This prevents tone drift away from calm, direct, unembarrassing help.
- Source: user
- Primary owning slice: none
- Supporting slices: none
- Validation: n/a
- Notes: Applies across app, website, and launch messaging.

### R036 — Generic finance-dashboard complexity that obscures the next best move
- Class: anti-feature
- Status: out-of-scope
- Description: The product should not turn into a broad personal-finance dashboard that hides the next payoff action.
- Why it matters: This protects the core value from feature sprawl.
- Source: user
- Primary owning slice: none
- Supporting slices: none
- Validation: n/a
- Notes: Applies especially to home screen and website scope.

### R037 — Pretending uncertain debt data is precise
- Class: constraint
- Status: out-of-scope
- Description: The product should not present estimated or incomplete data as exact.
- Why it matters: Honest confidence handling is part of product trust.
- Source: inferred
- Primary owning slice: none
- Supporting slices: none
- Validation: n/a
- Notes: Reinforces APR confidence and linked-data failure visibility requirements.

### R038 — A large feature-rich website beyond launch and marketing needs
- Class: anti-feature
- Status: out-of-scope
- Description: The website should not grow into a large feature-rich web product in this plan.
- Why it matters: This prevents the website from becoming a distraction from the app and launch.
- Source: user
- Primary owning slice: none
- Supporting slices: none
- Validation: n/a
- Notes: A simple page plus possible launch signup is the intended scope.

## Traceability

| ID | Class | Status | Primary owner | Supporting | Proof |
|---|---|---|---|---|---|
| R001 | primary-user-loop | active | M001/S03 | M001/S02, M001/S07 | mapped |
| R002 | core-capability | active | M001/S01 | M001/S07 | mapped |
| R003 | core-capability | active | M001/S02 | M001/S05 | mapped |
| R004 | differentiator | active | M001/S02 | M001/S04, M001/S05 | mapped |
| R005 | core-capability | active | M001/S04 | M001/S02 | mapped |
| R006 | differentiator | active | M001/S05 | M001/S01, M001/S02 | mapped |
| R007 | failure-visibility | active | M001/S06 | M001/S02, M001/S03 | mapped |
| R008 | quality-attribute | active | M001/S07 | M001/S01, M001/S02, M001/S03, M001/S04, M001/S05, M001/S06 | mapped |
| R009 | integration | active | M002/S01 | M002/S02 | mapped |
| R010 | integration | active | M002/S02 | M002/S04 | mapped |
| R011 | operability | active | M002/S03 | M002/S06 | mapped |
| R012 | failure-visibility | active | M002/S04 | M002/S03, M002/S06 | mapped |
| R013 | core-capability | active | M002/S05 | M002/S03 | mapped |
| R014 | continuity | active | M002/S06 | M002/S01, M002/S02, M002/S03, M002/S04, M002/S05 | mapped |
| R015 | launchability | active | M003/S02 | M003/S01 | mapped |
| R016 | launchability | active | M003/S03 | M003/S02 | mapped |
| R017 | launchability | active | M003/S05 | M003/S02, M003/S04 | mapped |
| R018 | launchability | active | M003/S05 | none | mapped |
| R019 | launchability | active | M003/S06 | M003/S02, M003/S03, M003/S05 | mapped |
| R020 | operability | active | M004/S01 | M004/S05 | mapped |
| R021 | launchability | active | M004/S02 | M003/S04 | mapped |
| R022 | compliance/security | active | M004/S03 | M003/S03 | mapped |
| R023 | quality-attribute | active | M004/S04 | M004/S05 | mapped |
| R024 | launchability | active | M004/S06 | M004/S01, M004/S02, M004/S03, M004/S04, M004/S05 | mapped |
| R025 | differentiator | active | M005/S01 | M005/S03 | mapped |
| R026 | differentiator | active | M005/S02 | M005/S05 | mapped |
| R027 | differentiator | active | M005/S04 | M005/S01, M005/S03 | mapped |
| R028 | continuity | active | M005/S06 | M005/S05 | mapped |
| R029 | differentiator | deferred | none | none | unmapped |
| R030 | differentiator | deferred | none | none | unmapped |
| R031 | continuity | deferred | none | none | unmapped |
| R032 | launchability | deferred | none | none | unmapped |
| R033 | continuity | deferred | none | none | unmapped |
| R034 | integration | deferred | none | none | unmapped |
| R035 | anti-feature | out-of-scope | none | none | n/a |
| R036 | anti-feature | out-of-scope | none | none | n/a |
| R037 | constraint | out-of-scope | none | none | n/a |
| R038 | anti-feature | out-of-scope | none | none | n/a |

## Coverage Summary

- Active requirements: 28
- Mapped to slices: 28
- Validated: 0
- Unmapped active requirements: 0
