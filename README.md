# PayDirt

PayDirt is an Android app built around one useful question:

**If I have a little extra money, which credit card payment does the most good right now?**

If someone has an extra $5, $10, or $25, PayDirt should point to the right move, explain why it matters, and make the payoff feel concrete.

> Debt is math, not shame.

That is the product philosophy in one line.

PayDirt is meant to feel:

- calm
- direct
- unembarrassing
- specific
- lightly elegant

It is **not** trying to become a giant finance dashboard, a guilt machine, or a motivational app pretending to be a calculator.

## Why It Exists

A lot of debt tools either overwhelm people with charts or flatten everything into generic encouragement.

PayDirt is trying to do something simpler and sharper:

- show the best next move
- show what that move changes
- make progress visible in real terms
- give the user another clear next step

The core value is straightforward:

**PayDirt should make the next best debt-payoff move clear, grounded, and easy to act on.**

## Current State

This repo already contains a working Android app with a meaningful manual payoff-planning foundation.

What works today:

- manual card add and edit flow
- card detail view
- payment logging
- local persistence with Room
- payoff recommendations
- strategy comparison across avalanche, snowball, and hybrid approaches
- payoff engine unit tests
- Compose-based app shell and navigation

This is already beyond prototype territory. The app does real work today.

What exists in code but is not fully assembled into the main experience yet:

- reward screen flow
- behavior engine surfaces
- APR confidence UI
- onboarding / first recommendation reveal
- Plaid-linked account flow
- background refresh scheduling from app lifecycle
- re-auth recovery UX

So the current job is not to invent the app from scratch. It is to assemble the strongest existing parts into a product that feels complete.

## What The App Should Feel Like

PayDirt should help without performing help.

That means:

- no shame
- no fake cheerleading
- no streak pressure
- no vague "take control of your future" copy
- no dashboard sprawl that hides the next useful action

Good language for this product sounds like:

- "Debt is math, not shame."
- "Small hits, right target."
- "Connect once. Let it run."

## Core Screens

The current in-app experience centers on:

- **Home** — debt overview and a quick recommendation
- **Card Detail** — card progress and payment history
- **Add / Edit Card** — manual entry for payoff-critical inputs
- **Optimizer** — strategy comparison and target selection
- **Reward Screen** — already built in code and intended to become part of the main payment loop

## Roadmap

The project is currently sequenced into five milestones.

### M001 — Core Android Product Loop
Turn the existing app into a coherent, satisfying manual payoff experience.

Focus:
- wire reward flow after payment logging
- improve home progress and next-move surfaces
- add manual-user onboarding / first recommendation reveal
- add goal framing and behavior surfaces
- surface trust cues like APR confidence where they matter

### M002 — Linked Accounts and Live Data
Make Plaid-backed account linking, refresh, and re-auth work end-to-end.

Focus:
- Plaid Link UI and token exchange
- linked account management
- refresh scheduling
- re-auth detection and recovery
- recommendation updates from live data

### M003 — Website and Launch Web Presence
Build the simple public-facing launch site for PayDirt.

Focus:
- straightforward marketing page
- trust and explanatory content
- clear app / store CTA paths
- optional launch-interest signup

This is intentionally a **small website**, not a second product.

### M004 — Google Play Readiness and Submission
Prepare the app for testing tracks and eventual Play submission.

Focus:
- release config and signing
- store listing assets and copy
- privacy / policy / compliance readiness
- QA on launch-critical flows
- submission support

### M005 — Intelligence and Retention
Make PayDirt smarter and more useful over time once the base product is stable.

Focus:
- smarter recommendation inputs
- grounded momentum / progress surfaces
- payoff forecasting
- restrained, opt-in return loops

## Why The Sequence Looks Like This

The order is intentional:

1. make the app feel real
2. make linked data real
3. build the simple launch website
4. prepare the store release path
5. add deeper intelligence and retention

That keeps the work anchored to actual product value instead of chasing launch packaging before the app experience is strong enough.

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
- [`.gsd`](.gsd)  
  Planning and execution artifacts for milestone, requirement, and roadmap management

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

The Android app already includes Plaid-oriented repository and API scaffolding, but it still expects a backend to handle secure financial integration concerns.

That backend is intended to:

- create link tokens
- exchange public tokens
- refresh liabilities / linked account data
- unlink items securely

The current debug base URL is configured in [`Modules.kt`](app/src/main/java/com/lweiss01/paydirt/di/Modules.kt).

## Planning Files

If you want the current execution plan rather than the product overview, start here:

- [`.gsd/PROJECT.md`](.gsd/PROJECT.md)
- [`.gsd/REQUIREMENTS.md`](.gsd/REQUIREMENTS.md)
- [`.gsd/DECISIONS.md`](.gsd/DECISIONS.md)
- [`.gsd/milestones/M001/M001-CONTEXT.md`](.gsd/milestones/M001/M001-CONTEXT.md)
- [`.gsd/milestones/M001/M001-ROADMAP.md`](.gsd/milestones/M001/M001-ROADMAP.md)

Older planning context also exists here:

- [`ROADMAP.md`](ROADMAP.md)
- [`MILESTONES.md`](MILESTONES.md)
- [`CONTRIBUTING.md`](CONTRIBUTING.md)

## Development Notes

- Room schema export is enabled and written to [`app/schemas`](app/schemas)
- The repo currently contains both the usable manual-entry path and the in-progress live-data path
- The codebase is further along than a prototype, but not yet at full product completion
- Current planning assumes the website remains simple and marketing-focused, not feature-heavy

## License

This project is licensed under the MIT License. See [`LICENSE`](LICENSE).
