# S06: APR confidence and trust cues

**Goal:** Make recommendation-related UI communicate APR confidence honestly where users decide what to do next, using the existing APR trust contract instead of raw APR presentation or duplicated confidence logic.
**Demo:** A user can open the Home recommendation and a card detail screen and see consistent APR trust messaging that clearly distinguishes confirmed, estimated, and unknown APR states.

## Demo

A user can open the Home recommendation, a card detail screen, and the post-payment reward screen and see consistent APR trust messaging that clearly distinguishes confirmed, estimated, and unknown APR states.

## Must-Haves

- Recommendation-related UI uses the existing `aprTrustCopyForSource(...)` contract rather than inventing a second APR confidence system. (R007)
- Card detail no longer shows a raw APR-only header without trust context. (R007)
- Reward now receives `aprSource` through the existing navigation payload and shows the same trust state after payment logging. (R007)
- Trust messaging stays calm and direct: clear about confidence, never alarmist, and never falsely certain. (supports R008)
- The Home recommendation surface, card-detail recommendation-adjacent surface, and reward surface use compatible trust wording so users are not told different stories in different screens. (R007)
- Existing APR trust copy tests and reward payload serialization remain green, and the updated UI still builds. (R007)

## Proof Level

- This slice proves: integration
- Real runtime required: yes
- Human/UAT required: yes

## Verification

- `app/src/test/java/com/lweiss01/paydirt/ui/components/APRConfidenceBadgeTest.kt` proves the shared APR trust copy contract still says confirmed / estimated / not confirmed correctly.
- `app/src/test/java/com/lweiss01/paydirt/ui/navigation/RewardNavigationPayloadTest.kt` proves reward navigation preserves `aprSource` alongside `PaymentImpact` through saved-state serialization.
- `grep -n "APRTrustBadge|aprTrustCopyForSource|APR confirmed|APR estimated|APR not confirmed|reward_apr_trust_card" app/src/main/java/com/lweiss01/paydirt/ui/screens/cards/CardDetailScreen.kt app/src/main/java/com/lweiss01/paydirt/ui/screens/home/HomeScreen.kt app/src/main/java/com/lweiss01/paydirt/ui/screens/reward/RewardScreen.kt app/src/main/java/com/lweiss01/paydirt/ui/components/APRConfidenceBadge.kt app/src/main/java/com/lweiss01/paydirt/ui/navigation/RewardNavigationState.kt`
- `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat testDebugUnitTest --max-workers=1 --tests "com.lweiss01.paydirt.ui.components.APRConfidenceBadgeTest" --tests "com.lweiss01.paydirt.ui.navigation.RewardNavigationPayloadTest"`
- `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat :app:assembleDebug --max-workers=1`
- Manual/runtime walkthrough on a device: open Home recommendation → note APR trust badge/helper text → open the recommended card detail → confirm card detail communicates the same APR confidence state → log an extra payment → confirm reward now shows the same APR trust state and helper text.

## Observability / Diagnostics

- Runtime signals: card detail and reward should both expose visible APR trust state instead of only raw numbers or implicit confidence.
- Inspection surfaces: `APRConfidenceBadgeTest`, `RewardNavigationPayloadTest`, `CardDetailScreen`, `RewardScreen`, `HomeScreen`, and runtime UI dumps/screenshots are the main regression seams.
- Failure visibility: regressions show up as raw APR returning without trust context, mismatched helper text between Home/card-detail/reward, or reward navigation dropping `aprSource` from the payload.
- Redaction constraints: trust cues may mention source confidence, but must not expose linked-account identifiers or sensitive account details.

## Integration Closure

- Upstream surfaces consumed: `AprSource`, `APRTrustBadge`, and `aprTrustCopyForSource(...)` from the shared APR confidence component.
- New wiring introduced in this slice: card-detail and reward APR trust rendering plus `aprSource` threaded through reward navigation; no new repositories or confidence engines.
- What remains before the milestone is truly usable end-to-end: S07 assembled-flow polish and verification.

## Decomposition Rationale

S06 started with the smallest high-value trust seam: card detail already sits next to logging payments and deciding what to do next, but it still presented APR as a raw number with no confidence framing. Once that was in place, the remaining inconsistency was reward: the post-payment surface still had no way to know APR confidence. The clean fix was to preserve the shared trust copy contract, thread `aprSource` through the existing reward navigation payload, and render the same trust cue after payment logging.

## Tasks

- [x] **T01: Reuse shared APR trust copy on card detail instead of raw APR-only presentation** `est:1h`
  - Why: card detail is recommendation-adjacent and currently lacks the confidence framing that Home already has.
  - Files: `app/src/main/java/com/lweiss01/paydirt/ui/screens/cards/CardDetailScreen.kt`
  - Do: replace the raw APR header block with `APRTrustBadge` plus helper explainer text derived from `aprTrustCopyForSource(card.aprSource)` while keeping the card detail layout calm and readable.
  - Verify: `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat :app:assembleDebug --max-workers=1`
  - Done when: card detail communicates confirmed / estimated / unknown APR honestly without duplicating trust logic.
- [x] **T02: Verify the shared trust-copy contract and card-detail build/runtime path** `est:0.75h`
  - Why: S06 only counts if the shared trust wording remains stable and the updated UI actually builds and runs.
  - Files: `app/src/test/java/com/lweiss01/paydirt/ui/components/APRConfidenceBadgeTest.kt`
  - Do: rerun the existing trust-copy tests, build the app, and runtime-check card detail on device/emulator against Home’s trust wording.
  - Verify: `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat testDebugUnitTest --max-workers=1 --tests "com.lweiss01.paydirt.ui.components.APRConfidenceBadgeTest"` and `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat :app:assembleDebug --max-workers=1`
  - Done when: shared trust-copy tests pass, the app builds, and runtime evidence shows Home and card detail using aligned trust language.
- [x] **T03: Thread APR trust state through reward navigation and render it on reward** `est:1h`
  - Why: the confidence story is still incomplete if it disappears after payment logging.
  - Files: `app/src/main/java/com/lweiss01/paydirt/ui/navigation/RewardNavigationState.kt`, `app/src/main/java/com/lweiss01/paydirt/ui/navigation/NavGraph.kt`, `app/src/main/java/com/lweiss01/paydirt/ui/screens/reward/RewardScreen.kt`, `app/src/test/java/com/lweiss01/paydirt/ui/navigation/RewardNavigationPayloadTest.kt`
  - Do: add `aprSource` to the reward navigation payload, preserve it through saved-state serialization, and render `APRTrustBadge` plus helper copy in reward using the shared trust contract.
  - Verify: `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat testDebugUnitTest --max-workers=1 --tests "com.lweiss01.paydirt.ui.navigation.RewardNavigationPayloadTest" --tests "com.lweiss01.paydirt.ui.components.APRConfidenceBadgeTest"` and `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat :app:assembleDebug --max-workers=1`
  - Done when: reward uses the same trust language as Home and card detail, and runtime evidence shows the reward trust card after payment logging.

## Files Likely Touched

- `app/src/main/java/com/lweiss01/paydirt/ui/screens/cards/CardDetailScreen.kt`
- `app/src/main/java/com/lweiss01/paydirt/ui/navigation/RewardNavigationState.kt`
- `app/src/main/java/com/lweiss01/paydirt/ui/navigation/NavGraph.kt`
- `app/src/main/java/com/lweiss01/paydirt/ui/screens/reward/RewardScreen.kt`
- `app/src/test/java/com/lweiss01/paydirt/ui/navigation/RewardNavigationPayloadTest.kt`
- `.gsd/milestones/M001/slices/S06/S06-PLAN.md`
