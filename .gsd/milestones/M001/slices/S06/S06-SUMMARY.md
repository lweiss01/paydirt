# S06 Summary — APR confidence and trust cues

**Milestone:** M001  
**Slice:** S06  
**Status:** complete  
**Completed:** 2026-03-25

## Goal

Make recommendation-related UI communicate APR confidence honestly where users decide what to do next, using the existing APR trust contract instead of raw APR presentation or duplicated confidence logic.

## What this slice actually delivered

S06 turned APR confidence from a partial Home-only cue into a consistent part of the recommendation/payment loop.

Delivered behavior:
- Home continues to use the shared APR trust contract in the next-move hero.
- Card detail no longer shows a raw APR-only header; it now shows an **APR CONFIDENCE** label, `APRTrustBadge`, and the shared helper explainer text.
- Reward now receives `aprSource` through the existing reward navigation payload and shows the same APR trust state after payment logging.
- The same confidence story now follows the user across the key decision points of the manual loop:
  - Home recommendation
  - card detail before payment
  - reward after payment
- Trust copy remains calm and direct: confirmed when the APR is known, estimated when inferred, and explicitly not confirmed when unknown.
- The slice reused the existing shared trust-copy contract instead of creating a separate post-payment or card-detail confidence model.

In practice, the APR-confidence flow now reads as:
**see a recommendation on Home → open card detail and see the same trust framing → log a payment → land on reward with the same APR confidence state still visible.**

## Scope completed by task

### T01 — Reuse shared APR trust copy on card detail
- Replaced the raw APR-only header block in `CardDetailScreen`.
- Added `APRTrustBadge(aprSource = card.aprSource)` to card detail.
- Added helper explainer text from `aprTrustCopyForSource(card.aprSource)`.
- Kept the card-detail layout calm and readable instead of adding a second expandable APR explainer.

### T02 — Verify shared trust-copy contract and card-detail runtime path
- Re-ran `APRConfidenceBadgeTest` to prove the shared confidence wording still says confirmed / estimated / not confirmed correctly.
- Rebuilt the app successfully after the card-detail UI change.
- Installed and launched on an attached Android device.
- Runtime-verified that card detail now shows:
  - `APR CONFIDENCE`
  - `APR not confirmed`
  - `This recommendation may sharpen once you add the exact APR from a statement.`

### T03 — Thread APR trust state into reward and render it there
- Added `aprSource` to `RewardNavigationPayload`.
- Preserved `aprSource` through reward saved-state serialization.
- Updated `NavGraph` to pass APR source from card detail into reward navigation.
- Added a reward trust card with:
  - `APR CONFIDENCE`
  - `APRTrustBadge`
  - helper explainer text from `aprTrustCopyForSource(...)`
- Runtime-verified the reward screen after a live extra-payment submit and confirmed the reward trust card is visible alongside the goal/progress and next-move content.

## Key patterns this slice established

### 1. APR trust is one shared contract, not one contract per screen
`aprTrustCopyForSource(...)` remained the single trust-language source for Home, card detail, and reward.

**Why it matters for later slices:** future APR trust tweaks should update the shared contract instead of patching individual screens with custom wording.

### 2. Confidence should survive navigation, not reset at the reward boundary
Before S06, the confidence story disappeared after payment logging because reward didn’t know the card’s APR source.

**Why it matters for later slices:** if a user-visible trust state matters before action, it should still matter after action unless there is a deliberate reason to drop it.

### 3. Honest confidence framing belongs at recommendation-adjacent moments
Trust cues are most useful where users decide whether to trust the recommendation or act on it: Home, card detail, and reward.

**Why it matters for later slices:** S07 should preserve trust framing around recommendation and reward surfaces instead of letting polish work flatten it back into generic APR labels.

## Verification run for slice closure

### Passed
1. `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat testDebugUnitTest --max-workers=1 --tests "com.lweiss01.paydirt.ui.components.APRConfidenceBadgeTest"`
   - **Result:** pass
2. `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat testDebugUnitTest --max-workers=1 --tests "com.lweiss01.paydirt.ui.navigation.RewardNavigationPayloadTest" --tests "com.lweiss01.paydirt.ui.components.APRConfidenceBadgeTest"`
   - **Result:** pass
3. `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat :app:assembleDebug --max-workers=1`
   - **Result:** pass
4. `'/c/Users/lweis/AppData/Local/Android/Sdk/platform-tools/adb.exe' install -r app/build/outputs/apk/debug/app-debug.apk`
   - **Result:** pass
5. `'/c/Users/lweis/AppData/Local/Android/Sdk/platform-tools/adb.exe' shell am start -n com.lweiss01.paydirt/.MainActivity`
   - **Result:** pass
6. Runtime UI dump verification on card detail
   - **Result:** confirmed card detail shows `APR CONFIDENCE`, `APR not confirmed`, and the shared helper trust copy
7. Runtime UI dump verification on reward after live extra-payment submit
   - **Result:** confirmed reward shows `INTEREST CUT`, `MONTHLY GOAL`, `APR CONFIDENCE`, `APR not confirmed`, the shared helper trust copy, and `BEST NEXT MOVE` together on the reward surface
8. Observability seam spot-check via grep / source inspection
   - **Result:** confirmed `APRTrustBadge`, `aprTrustCopyForSource(...)`, `RewardNavigationPayload.aprSource`, and `reward_apr_trust_card` are present in the expected files

## Observability / diagnostic seams confirmed

### Shared trust-copy contract visibility
- `APRConfidenceBadgeTest.kt`
- `aprTrustCopyForSource(...)`
- `APRTrustBadge`

These make it inspectable whether:
- confirmed / estimated / not-confirmed labels remain stable
- helper text stays aligned across all recommendation-related surfaces

### Reward navigation visibility
- `RewardNavigationPayloadTest.kt`
- `RewardNavigationPayload.aprSource`
- `NavGraph.kt`

These make it inspectable whether:
- reward saved-state recovery preserves the APR source
- confidence state does not get dropped at the reward boundary

### Runtime visibility
- card detail trust block
- reward trust card (`reward_apr_trust_card`)
- Home next-move trust surfaces
- device UI dumps for card detail and reward

These make it inspectable whether the live app still tells one trust story across the full loop.

## Requirement impact

### R007 — substantially proven in live recommendation surfaces
S06 now proves that:
- recommendation-related UI communicates APR confidence more honestly where it matters
- Home, card detail, and reward all share the same trust contract
- raw APR-only presentation no longer undercuts recommendation trust on card detail
- reward no longer drops APR confidence context after payment logging

R007 is materially proven for the manual loop surfaces in this milestone.

### R008 — tone support improved, milestone-level tone proof still deferred
S06 kept the trust language plain and specific rather than dramatic or false-certainty-heavy. Final integrated tone proof across the assembled flow still belongs to S07.

## What changed downstream readers should know

### For S07 (core flow polish and verification)
- The manual payoff loop now includes explicit APR confidence cues on all key surfaces.
- S07 should verify that these trust cues remain visually clear without overcrowding the recommendation/reward flow.
- S07 should preserve the rule that confidence text comes from the shared trust contract rather than screen-local wording.

## Risks retired vs remaining

### Retired by S06
- Card detail no longer presents APR as a raw confidence-free number.
- Reward no longer drops APR confidence context after payment logging.
- Users are no longer told one trust story on Home and a different or incomplete story on later screens.

### Still remaining after S06
- Final assembled-flow polish and holistic verification remain for S07.
- Broader repo-wide test failures outside this slice may still exist and must not be confused with S06-specific verification.

## Bottom line

S06 successfully carried APR trust cues through the recommendation/payment loop. Home, card detail, and reward now share one confidence contract, the targeted trust and payload tests pass, the app builds, and the updated trust surfaces were verified on a real attached device.