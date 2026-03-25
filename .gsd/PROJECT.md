# Project

## What This Is

PayDirt is an Android app that helps people decide where a small extra credit card payment will do the most good. It already has a working manual payoff-planning foundation: card management, payment logging, payoff recommendations, strategy comparison, and local persistence. The project now expands from that app core into a coherent product loop, linked-account/live-data flows, a simple launch website, and Google Play release preparation.

## Core Value

The one thing that must work even if everything else is cut: PayDirt should make the next best debt-payoff move clear, grounded, and easy to act on.

## Current State

A Kotlin Android app exists and builds around Jetpack Compose, Hilt, Room, Navigation Compose, Retrofit, WorkManager, and domain engines for payoff planning and behavior. The manual path is usable today. Reward, behavior, Plaid, refresh, and launch-facing surfaces exist partially in code but are not yet assembled into a fully launchable product loop.

## Architecture / Key Patterns

- Android app in Kotlin with Jetpack Compose UI and Navigation Compose
- Hilt for dependency injection
- Room for local persistence and migrations
- Domain engines for payoff strategy, APR estimation, and behavior/reward state
- Retrofit API contract and repository scaffolding for Plaid-backed backend flows
- WorkManager for background linked-account refresh
- Brownfield planning rule: build forward from existing modules and shipped flows, not from a blank-slate redesign
- Product tone is a real constraint: calm, direct, unembarrassing, specific

## Capability Contract

See `.gsd/REQUIREMENTS.md` for the explicit capability contract, requirement status, and coverage mapping.

## Milestone Sequence

- [ ] M001: Core Android Product Loop — Turn the existing app into a coherent, satisfying manual payoff experience.
- [ ] M002: Linked Accounts and Live Data — Make Plaid-backed linking, refresh, and re-auth work end-to-end.
- [ ] M003: Website and Launch Web Presence — Build the simple marketing site and optional launch signup flow.
- [ ] M004: Google Play Readiness and Submission — Prepare release assets, compliance, testing, and store submission.
- [ ] M005: Intelligence and Retention — Add smarter guidance, progress surfaces, and restrained return loops after the base product is stable.
