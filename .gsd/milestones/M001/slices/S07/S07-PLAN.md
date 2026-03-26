# S07: Core flow polish and verification

**Goal:** Prove the assembled manual payoff loop still holds together after S01-S06 by exercising the real in-app path from first-run entry through recommendation, payment logging, reward, and return to the loop, while catching any polish regressions that only appear once those seams are used together.
**Demo:** A new manual-entry user can add a first card, land on Home Overview, open the recommended card, log an extra payment, see reward with goal/trust/next-move context, return to card detail, and get back to Home with the assembled loop still readable and grounded.

## Must-Haves

- The final slice proves the real entrypoint and assembled loop, not just isolated screen fragments. (R001, R002, R003, R004, R005)
- Integrated verification covers the key S05 and S06 seams now relied on in the product loop: Home `Overview`, Home recommendation trust, card-detail APR trust, reward goal framing, and reward APR trust. (R006, R007)
- Reward completion returns cleanly to card detail instead of dropping the user into a broken or ambiguous state. (R003)
- Returning from card detail to Home leaves the user in a coherent payoff loop with recommendation and progress surfaces still visible. (R002, R006)
- The assembled flow copy stays calm, direct, and grounded across onboarding, recommendation, payment logging, reward, and return. (supports R008)

## Proof Level

- This slice proves: integration
- Real runtime required: yes
- Human/UAT required: yes

## Verification

- `app/src/androidTest/java/com/lweiss01/paydirt/ui/navigation/ManualOnboardingFlowTest.kt` remains the baseline first-run entry proof.
- `app/src/androidTest/java/com/lweiss01/paydirt/ui/navigation/GoalSettingFlowTest.kt` remains the baseline goal-to-reward framing proof.
- `app/src/androidTest/java/com/lweiss01/paydirt/ui/navigation/ManualLoopFlowTest.kt` proves the assembled loop from first card creation through reward and back into the loop.
- `rg -n "home_tab_row|home_next_move_hero|reward_apr_trust_card|reward_done_button|card_detail_payment_submit|APR CONFIDENCE|Recent payment impact|Next opportunity" app/src/main/java app/src/androidTest -g '*.kt'`
- `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat :app:connectedDebugAndroidTest --max-workers=1 -Pandroid.testInstrumentationRunnerArguments.class=com.lweiss01.paydirt.ui.navigation.ManualLoopFlowTest`
- `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --max-workers=1`
- Manual runtime walkthrough on a device/emulator: first-run add card → Home Overview recommendation/trust → card detail trust → extra payment submit → reward goal/trust/next move → Back to card → Home recommendation/progress still coherent.

## Observability / Diagnostics

- Runtime signals: `home_tab_row`, `home_next_move_hero`, `card_detail_log_payment_fab`, `card_detail_payment_submit`, `reward_goal_card`, `reward_apr_trust_card`, and `reward_done_button` are the main integrated verification anchors.
- Inspection surfaces: `ManualLoopFlowTest`, the existing onboarding/goal androidTests, runtime UI dumps, and the reward/card/home test tags provide the main regression seam.
- Failure visibility: regressions show up as missing Home Overview content after onboarding, APR trust disappearing between Home/card detail/reward, reward not returning to card detail, or Home failing to show progress signals after the payment loop completes.
- Redaction constraints: integrated verification can assert visible payment amounts, trust labels, and payoff-copy headings, but must not add logging of notes, linked-account identifiers, or raw serialized payloads.

## Integration Closure

- Upstream surfaces consumed: onboarding reveal from S03, Home goal/progress surfaces from S02/S04/S05, and APR trust surfaces from S06.
- New wiring introduced in this slice: integrated androidTest coverage for the assembled manual loop; no new domain contracts or navigation destinations unless the test exposes a real polish gap.
- What remains before the milestone is truly usable end-to-end: slice summary/UAT closure once the integrated proof and any small polish fix are complete.

## Decomposition Rationale

S07 should start with proof, not speculative polish. Most milestone risks have already been retired in isolated slices, so the highest-value next move is one integrated androidTest that uses the real app entrypoint and asserts the cross-slice seams that matter most. If that test exposes a genuine gap, fix only that gap and re-run the same end-to-end verification.

## Tasks

- [ ] **T01: Add one assembled-loop instrumentation test that exercises the full manual payoff path** `est:1.25h`
  - Why: the milestone is not done until one runtime proof covers the assembled user path rather than only screen-level slices.
  - Files: `app/src/androidTest/java/com/lweiss01/paydirt/ui/navigation/ManualLoopFlowTest.kt`
  - Do: create a Compose instrumentation test that adds a first card, verifies Home opens on Overview with recommendation/trust visible, opens card detail, logs an extra payment, verifies reward goal/trust/next-move content, returns to card detail, then returns Home and confirms the loop still reads coherently.
  - Verify: `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat :app:connectedDebugAndroidTest --max-workers=1 -Pandroid.testInstrumentationRunnerArguments.class=com.lweiss01.paydirt.ui.navigation.ManualLoopFlowTest`
  - Done when: one passing instrumentation test proves the full loop survives the assembled flow.
- [x] **T02: Fix one concrete integrated-flow regression if the new test exposes it** `est:1h`
  - Why: S07 should polish only real problems surfaced by the assembled proof, not invent more churn.
  - Files: only the specific runtime seam exposed by T01
  - Do: make the smallest safe UI/navigation/test-tag adjustment needed for the assembled loop to pass cleanly.
  - Verify: rerun `ManualLoopFlowTest` and any directly affected local tests/builds.
  - Done when: the integrated-flow failure is resolved and the assembled proof passes.

## Files Likely Touched

- `app/src/androidTest/java/com/lweiss01/paydirt/ui/navigation/ManualLoopFlowTest.kt`
- `.gsd/milestones/M001/slices/S07/S07-PLAN.md`
- possibly one small UI/navigation file only if the integrated test exposes a real assembled-flow gap
