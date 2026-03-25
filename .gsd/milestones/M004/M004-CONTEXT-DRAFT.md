---
depends_on: [M001, M002, M003]
---

# M004: Google Play Readiness and Submission — Context Draft

**Gathered:** 2026-03-25
**Status:** Draft for later discussion

## What Was Already Established

This milestone exists to get PayDirt from a working Android project to a submission-ready Google Play release. The user explicitly wants help eventually preparing for and submitting the app to the Google Play Store.

## Why This Exists

Store submission is part of the real project scope, not an afterthought. The work likely includes:
- release configuration and signing
- testing-track distribution
- store listing assets and copy
- privacy/policy/compliance readiness
- QA on real launch-critical flows
- final submission support through review

## Provisional Scope

Likely slices for this milestone:
- release config and signing setup
- store listing assets and copy
- policy, privacy, and compliance readiness
- QA and launch-blocker pass
- internal/open testing release
- final submission and review support

## Done Probably Looks Like

- a signed release artifact exists and is installable through Play testing tracks
- screenshots, descriptions, and metadata are ready
- privacy/policy/disclosure gaps are closed enough for submission
- launch-critical flows are verified on real builds/devices
- the final submission package is complete and ready to shepherd through review

## Known Risks

- actual release-signing and Play Console state are still unknown
- policy/privacy requirements may change based on how linked accounts and data handling land in earlier milestones
- some launch assets will depend on the app and marketing website being more finalized
- there may be hidden launch blockers in build variants, permissions, disclosures, or account-linking behavior

## Open Questions For Dedicated Discussion

- What already exists for release signing, app bundle generation, and Play Console setup?
- Will first submission include linked accounts, or launch initially on the manual path?
- What privacy-policy and disclosure surfaces need to exist on the website versus in-app?
- What level of device coverage and QA evidence is acceptable before submission?

## Why This Is A Draft

The milestone is clear enough to preserve, but release configuration, policy, and submission details should be discussed later when the upstream product state is more stable and real release artifacts exist.
