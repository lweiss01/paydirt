# PayDirt Milestone Checklist

This checklist turns the v3 product spec and current codebase state into a practical build list.

Legend:

- `[x]` completed or substantially present in the codebase
- `[ ]` not finished yet

## Milestone 1

### MVP Foundation

- [x] Android app project scaffolded
- [x] Hilt configured
- [x] Room database configured
- [x] Compose navigation set up
- [x] Home screen implemented
- [x] Manual card add flow implemented
- [x] Manual card edit flow implemented
- [x] Card detail screen implemented
- [x] Payment logging implemented
- [x] Payoff engine implemented
- [x] Optimizer screen implemented
- [x] Support avalanche strategy
- [x] Support snowball strategy
- [x] Support hybrid strategy
- [x] Unit test coverage started for payoff engine

## Milestone 2

### Phase 1 Completion

#### Reward loop

- [x] Reward screen UI built
- [ ] Wire reward screen after payment logging
- [ ] Show cumulative savings in the main app flow
- [ ] Show monthly goal progress in the main app flow
- [ ] Show next opportunity suggestion in the main app flow
- [ ] Surface BehaviorEngine output from live app state

#### Manual payoff experience

- [x] Quick recommendation banner on home screen
- [x] Total debt summary on home screen
- [ ] First recommendation reveal flow
- [ ] Full onboarding for manual-entry users
- [ ] Goal-setting screen
- [ ] APR confidence badge in UI

#### Data model and persistence

- [x] Plaid-linked fields added to card storage
- [x] Linked account table added
- [x] Room migration `1 -> 2` added
- [ ] Add migration test coverage

#### Live data integration

- [x] Retrofit API contract for backend added
- [x] Plaid repository scaffolding added
- [x] Background refresh worker added
- [ ] Plaid Link UI flow implemented
- [ ] Exchange token flow wired from UI
- [ ] Linked account list / management UI added
- [ ] Re-auth flow wired end-to-end
- [ ] Trigger background refresh scheduling from app startup
- [ ] User-facing re-auth notification added

## Milestone 3

### Intelligence + Retention

- [x] Smart APR engine exists in domain layer
- [ ] Wire Smart APR into active user-facing flows
- [ ] Infer APR from linked transaction history
- [ ] Add momentum score to home or reward surfaces
- [ ] Add flex nudges
- [ ] Recompute recommendations on important balance changes
- [ ] Add Vico payoff curve chart
- [ ] Add weekly progress summary
- [ ] Add opt-in notification loop for "new best move"

## Milestone 4

### Advanced Payoff Tools

- [ ] Support one-time extra payment scenarios
- [ ] Add balance transfer analysis
- [ ] Add debt consolidation scenarios
- [ ] Add due date reminders
- [ ] Add milestone moments with restrained feedback
- [ ] Add optional MX fallback for account aggregation

## Milestone 5

### Launch Readiness

- [ ] Clean up remaining Gradle deprecation warnings
- [ ] Expand unit test coverage beyond payoff engine
- [ ] Add UI / integration test coverage for core flows
- [ ] Finalize app copy and onboarding text
- [ ] Prepare Play Store assets and listing
- [ ] Add export / share summary flow
- [ ] Add widget / deep-link exploration
- [ ] Decide on licensing
- [ ] Decide on release signing and release pipeline

## Suggested Next Sequence

If we want the highest-ROI path from here, I'd tackle these next:

1. `[ ]` Wire the reward screen into payment logging
2. `[ ]` Add onboarding plus manual-vs-linked path selection
3. `[ ]` Implement the Plaid Link UI and token exchange flow
4. `[ ]` Schedule background refresh and add re-auth recovery UX
5. `[ ]` Surface behavior and momentum data on the home screen
