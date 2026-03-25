# PayDirt

PayDirt is an Android app that helps people decide where a small extra credit card payment will do the most good.

If someone has an extra $5, $10, or $25, the app should answer a simple question:

**Which payment creates the best payoff outcome from here?**

Debt is math, not shame. PayDirt is meant to feel calm, direct, and useful. The goal is not to flood people with dashboards or motivational language. The goal is to show the right move, explain why it matters, and make progress easier to see.

## What The App Does

Today, the app already supports the core manual payoff-planning flow:

- add and edit credit cards
- view balances, APR, and minimum payments
- log payments
- view card detail and payment history
- generate payoff recommendations
- compare avalanche, snowball, and hybrid payoff strategies

The longer-term product direction adds a stronger behavior loop around that core:

- show the best next move
- make that move easy to act on
- reflect the payoff in concrete terms
- bring the user back with another clear next step

## Current Status

This repository is a working Android app with a usable MVP foundation.

What is already in place:

- Jetpack Compose UI
- Hilt dependency injection
- Room persistence
- home dashboard
- optimizer flow
- manual card management
- payment logging
- payoff engine tests

What is partially built or scaffolded but not fully wired into the main experience yet:

- reward screen flow
- behavior engine surfaces
- Plaid-linked account flow
- background refresh scheduling
- re-auth UX
- onboarding flow from the product spec

## Product Direction

The product tone in the spec is a good summary of what PayDirt is trying to be:

- calm
- direct
- unembarrassing
- specific
- lightly elegant

That shows up in lines like:

- "Debt is math, not shame."
- "Small hits, right target."
- "Connect once. Let it run."

It also means avoiding a lot of typical finance-app language:

- no shame
- no fake cheerleading
- no streak pressure
- no vague "take control of your future" copy

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
  Background refresh work
- [`app/src/test`](app/src/test)
  Unit tests

## Running The App

Requirements:

- Android Studio
- JDK 17
- local Android SDK

Build from the command line:

```powershell
.\gradlew.bat :app:assembleDebug
```

Run unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

## Plaid / Backend Notes

The Android app includes a Plaid-ready repository and API contract, but it still expects a backend to handle secure token exchange and linked-account refresh.

That backend is intended to:

- create link tokens
- exchange public tokens
- refresh liabilities / linked account data
- unlink items securely

The current debug base URL is configured in [`Modules.kt`](app/src/main/java/com/lweiss01/paydirt/di/Modules.kt).

## Roadmap

- [`ROADMAP.md`](ROADMAP.md) tracks the broader product phases
- [`MILESTONES.md`](MILESTONES.md) turns that into a build checklist

The short version:

- Phase 1 focuses on foundation, live data, and the reward loop
- Phase 2 focuses on intelligence and retention
- Phase 3 focuses on advanced payoff tools
- Phase 4 focuses on launch polish

## Development Notes

- Room schema export is enabled and written to [`app/schemas`](app/schemas)
- The repo currently contains both the usable manual-entry path and the in-progress live-data path
- The codebase is further along than a prototype, but not yet at full product-spec completion

## License

This project is licensed under the MIT License. See [`LICENSE`](LICENSE).
