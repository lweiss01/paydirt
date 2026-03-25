---
depends_on: [M001]
---

# M002: Linked Accounts and Live Data — Context Draft

**Gathered:** 2026-03-25
**Status:** Draft for later discussion

## What Was Already Established

This milestone is intended to turn the existing Plaid-ready and background-refresh scaffolding into a real linked-account experience. The intended outcome is not just successful linking in isolation, but a complete loop where linking, refresh, re-auth, and recommendation updates all work together in the app.

## Why This Exists

PayDirt's current repo already includes meaningful linked-data groundwork:
- `app/src/main/java/com/lweiss01/paydirt/data/remote/PlaidApiService.kt`
- `app/src/main/java/com/lweiss01/paydirt/data/repository/PlaidRepository.kt`
- `app/src/main/java/com/lweiss01/paydirt/domain/usecase/PlaidUseCases.kt`
- `app/src/main/java/com/lweiss01/paydirt/work/BackgroundRefreshWorker.kt`
- linked-account and plaid-related fields in Room/domain models

But the repo evidence suggests the integration is still incomplete at the product level: UI flow, startup scheduling, re-auth recovery, and full recommendation wiring still need to be proven in the live app.

## Provisional Scope

Likely slices for this milestone:
- Plaid Link UI and token exchange
- linked account management surfaces
- refresh scheduling from app lifecycle
- re-auth detection and recovery UX
- refreshed data affecting recommendations
- migration and integration hardening

## Done Probably Looks Like

- a user can link an account through the app
- linked-account state is visible and understandable
- background refresh is actually scheduled and exercised
- stale/broken credentials surface clear recovery instead of silent failure
- live liabilities affect what PayDirt recommends
- manual and linked paths coexist safely

## Known Risks

- backend assumptions may be stale or incomplete relative to current mobile code
- background worker behavior exists in code but may not yet be triggered from the real app lifecycle
- re-auth UX likely crosses worker, notification, navigation, and account-management concerns
- this milestone crosses multiple runtime boundaries and probably needs an explicit integration-hardening slice

## Open Questions For Dedicated Discussion

- Is linked-account support required for first public launch, or can the initial launch lead with the manual loop?
- What backend exists today for Plaid token creation/exchange and liabilities refresh?
- How much account management should be in-app versus intentionally minimal?
- What is the right user-facing behavior when linked data is stale, partial, or temporarily unavailable?

## Why This Is A Draft

The milestone shape is clear enough to preserve now, but the technical assumptions need a dedicated code-and-backend reality check before locking a full context file.
