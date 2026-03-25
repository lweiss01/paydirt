# Contributing To PayDirt

Thanks for taking a look at PayDirt.

The project is still early, so the most helpful contributions are the ones that make the product clearer, more reliable, and easier to keep moving.

## Principles

When contributing, try to keep the product voice and behavior aligned with the spec:

- calm
- direct
- specific
- unembarrassing
- lightly elegant

That means:

- prefer concrete numbers over vague encouragement
- avoid shame-based or hype-heavy copy
- keep flows focused on the next useful action
- treat debt as math, not morality

## Good Contribution Areas

- payoff logic and validation
- UI polish for existing flows
- onboarding implementation
- reward loop wiring
- Plaid integration work
- test coverage
- accessibility improvements
- bug fixes and performance improvements

## Before You Start

If the change is large, open an issue first so the implementation direction is clear.

Helpful issue types:

- bug reports with reproduction steps
- roadmap-aligned feature requests
- small UI/content improvements
- code health and test coverage gaps

## Local Setup

Requirements:

- Android Studio
- JDK 17
- Android SDK

Build the app:

```powershell
.\gradlew.bat :app:assembleDebug
```

Run unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

## Pull Request Guidance

Please keep pull requests:

- focused
- easy to review
- clear about user impact

A good PR description should include:

- what changed
- why it changed
- how it was tested
- any follow-up work that is still needed

## Product Fit

Before submitting a UI or copy change, ask:

- does this make the next best move clearer?
- does this reduce friction?
- does this feel calm and credible?
- does this avoid pressure, shame, or fake celebration?

If the answer is no, it probably needs another pass.
