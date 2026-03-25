# PayDirt

PayDirt is an Android debt-payoff app built around one question:

**Where should the next dollar go to do the most damage?**

Instead of making payoff planning feel like homework, PayDirt is designed to show the best move, make it easy to act on, and turn progress into something users can actually feel. The long-term product vision is a behavioral payoff loop: recommendation, action, reward, repeat.

## Why PayDirt Exists

Credit card payoff apps usually do one of two things:

- act like spreadsheets with nicer fonts
- push vague "stay motivated" advice without helping users make the next concrete move

PayDirt aims for a more useful middle ground:

- show the best target right now
- explain the payoff in plain language
- keep manual entry fast
- support live account linking as the product matures
- make momentum visible without becoming cheesy

## What The App Does Today

The current Android app already supports a solid payoff-planning core:

- manual card entry and editing
- a home dashboard with total debt and a quick recommendation
- card detail views
- payment logging
- payoff optimization with avalanche, snowball, and hybrid strategies
- local persistence with Room
- dependency injection with Hilt
- Compose-based navigation and custom UI/theme work

The repo also includes in-progress infrastructure for:

- Plaid-linked account support
- background refresh via WorkManager
- Smart APR inference
- reward-loop UI
- behavior-driven retention features

## Current Product Status

This repository is best understood as:

**A working MVP foundation with partial Phase 1 product work already in place.**

That means:

- the app builds and runs
- the manual payoff planner is real and usable
- several higher-level product systems are implemented in code but not fully wired into the main user flow yet

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Hilt
- Room
- WorkManager
- Retrofit
- OkHttp
- Gson
- Vico

## Project Layout

- [`app/src/main/java/com/lweiss01/paydirt/data`](app/src/main/java/com/lweiss01/paydirt/data)
  Data layer: Room entities, DAOs, repositories, and remote API contracts
- [`app/src/main/java/com/lweiss01/paydirt/domain`](app/src/main/java/com/lweiss01/paydirt/domain)
  Business logic: payoff engine, behavior engine, APR logic, models, and use cases
- [`app/src/main/java/com/lweiss01/paydirt/ui`](app/src/main/java/com/lweiss01/paydirt/ui)
  Compose UI: screens, navigation, components, and theme
- [`app/src/main/java/com/lweiss01/paydirt/work`](app/src/main/java/com/lweiss01/paydirt/work)
  Background refresh worker scaffolding
- [`app/src/test`](app/src/test)
  Unit tests

## Running The App

### Requirements

- Android Studio
- JDK 17
- Android SDK installed locally

### Open In Android Studio

1. Open the repo root.
2. Let Gradle sync.
3. Run the `app` configuration on an emulator or connected device.

### Command Line Build

```powershell
.\gradlew.bat :app:assembleDebug
```

### Unit Tests

```powershell
.\gradlew.bat testDebugUnitTest
```

## Backend / Plaid Notes

PayDirt includes a Plaid-ready Android-side repository and Retrofit contract, but it still expects a backend to handle sensitive token exchange and account refresh operations.

Current backend responsibilities are intended to include:

- creating link tokens
- exchanging public tokens
- refreshing liabilities / linked account data
- unlinking items securely

The current debug base URL is configured in [`Modules.kt`](app/src/main/java/com/lweiss01/paydirt/di/Modules.kt).

## Roadmap And Milestones

The product direction has been broken out into repo docs so the code and plan stay aligned:

- [`ROADMAP.md`](ROADMAP.md) for phase-by-phase product direction
- [`MILESTONES.md`](MILESTONES.md) for a build-oriented checklist

In short:

- Phase 1 is foundation, live data, and the reward loop
- Phase 2 is intelligence and retention
- Phase 3 is advanced payoff tooling
- Phase 4 is launch and distribution polish

## What Still Needs To Be Wired

Some of the most important unfinished connections are:

- reward screen integration after payment logging
- behavior engine surfaced in the live app flow
- onboarding flow
- Plaid link UI and re-auth flow
- background refresh scheduling from app startup
- goal and momentum surfaces in the main experience

## Development Notes

- Room schema export is enabled and written to [`app/schemas`](app/schemas)
- The repo currently tracks both the usable manual-entry path and the in-progress live-data path
- The codebase is honest-to-goodness app code, not a throwaway prototype, but it is still mid-build from a product perspective

## License

This project is licensed under the MIT License. See [`LICENSE`](LICENSE).
