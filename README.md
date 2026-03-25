# PayDirt

PayDirt is an Android app for credit card payoff planning. It helps users see where an extra payment will hit hardest, track balances and payments over time, and turn payoff math into a simple recommendation they can act on quickly.

This project is built as a native Android app with Jetpack Compose, Hilt, Room, and a small Plaid-ready networking layer. The product direction in this repo is based on the v3 product spec and focuses on a behavioral payoff loop: recommend the best move, help the user take it, and make the win feel concrete.

## Current Status

The app is running and builds successfully with `:app:assembleDebug`.

What is implemented today:

- Home dashboard with total debt, card summaries, and a quick payoff recommendation
- Manual card entry and editing flow
- Card detail screen with payment history and payment logging
- Payoff optimizer with avalanche, snowball, and hybrid strategies
- Local persistence with Room
- Dependency injection with Hilt
- Compose navigation and custom app theme
- Plaid-oriented data models, repository, worker, and API contract scaffolding
- Room migration from schema version 1 to 2

What is present in code but not fully wired end-to-end yet:

- Reward screen integration into the user flow
- Behavior engine-driven feedback loop
- Plaid linking UI and onboarding flow
- Background refresh scheduling from app startup
- Re-auth nudges and notification UX

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Hilt
- Room
- WorkManager
- Retrofit + Gson
- OkHttp
- Vico charts

## Package / App Info

- Application ID: `com.lweiss01.paydirt`
- Min SDK: `26`
- Target SDK: `35`
- Compile SDK: `35`

## Project Structure

The codebase is organized into a straightforward layered structure:

- [`app/src/main/java/com/lweiss01/paydirt/data`](app/src/main/java/com/lweiss01/paydirt/data) for Room entities/DAOs, repositories, and remote API contracts
- [`app/src/main/java/com/lweiss01/paydirt/domain`](app/src/main/java/com/lweiss01/paydirt/domain) for engines, models, and use cases
- [`app/src/main/java/com/lweiss01/paydirt/ui`](app/src/main/java/com/lweiss01/paydirt/ui) for Compose screens, navigation, and theme
- [`app/src/main/java/com/lweiss01/paydirt/work`](app/src/main/java/com/lweiss01/paydirt/work) for background refresh work
- [`app/src/test`](app/src/test) for unit tests

## Running The App

### Prerequisites

- Android Studio
- Android SDK installed through Android Studio
- JDK 17

### Open In Android Studio

1. Open the project root: `C:\Users\lweis\Documents\paydirt`
2. Let Gradle sync
3. Run the `app` configuration on an emulator or Android device

### Command Line Build

```powershell
.\gradlew.bat :app:assembleDebug
```

### Unit Tests

```powershell
.\gradlew.bat testDebugUnitTest
```

## Plaid / Backend Notes

The app includes a Plaid-ready repository and Retrofit interface, but it expects a backend to handle secure token exchange and Plaid API calls. The current debug base URL is configured in [`Modules.kt`](app/src/main/java/com/lweiss01/paydirt/di/Modules.kt).

Expected backend responsibilities:

- Create link tokens
- Exchange Plaid public tokens
- Refresh liabilities / linked account data
- Unlink items securely

Until the linking flow is fully wired in the Android UI, PayDirt is best treated as a strong manual-entry payoff planner with live-data infrastructure in progress.

## Roadmap

The roadmap derived from the v3 product spec lives in [`ROADMAP.md`](ROADMAP.md).
The execution-oriented milestone checklist lives in [`MILESTONES.md`](MILESTONES.md).

Short version:

- Phase 1 focuses on foundation, live data, and the reward loop
- Phase 2 deepens retention and intelligence
- Phase 3 adds advanced payoff tools
- Phase 4 focuses on shipping polish and distribution

## Development Notes

- Room schema export is enabled and writes to [`app/schemas`](app/schemas)
- The repository includes both manual debt-tracking flows and early live-data scaffolding
- The current codebase is closest to an MVP foundation plus partial Phase 1 work, not the full spec yet

## License

No license file is currently present in the repository. Add one before publishing the project broadly.
