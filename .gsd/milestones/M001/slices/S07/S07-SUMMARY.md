# S07 Summary — Core flow polish and verification

**Milestone:** M001  
**Slice:** S07  
**Status:** complete  
**Completed:** 2026-03-25

## Goal

Prove the assembled manual payoff loop still works as one product flow after S01-S06, then fix only the concrete runtime regressions that appear once the real path is exercised together.

## What this slice actually delivered

S07 stopped treating the milestone as a set of individually-good screens and verified the real manual loop end to end.

Delivered behavior:
- Added assembled-flow instrumentation coverage for the manual loop in `ManualLoopFlowTest`.
- Verified that the new test code compiles with the current app and androidTest sources.
- Confirmed that connected Android instrumentation on the attached Android 16 device is blocked by an environment/runtime compatibility issue rather than by the new test itself.
- Used truthful manual runtime verification on a real attached device to exercise the live assembled loop when Espresso could not provide trustworthy evidence.
- Exposed a real integrated-flow bug: **Reward → Back to card** did not return cleanly to the main card-detail surface.
- Fixed the reward return seam so the two reward exits now behave differently:
  - **Pay more** returns to card detail and preserves the suggested-payment reopen path.
  - **Back to card / done** returns to plain card detail and force-closes the payment sheet.
- Verified that the fixed loop now stays in PayDirt, avoids the prior blank/launcher failure modes, and lands on card detail with payment history visible and the payment sheet closed.

In practice, the assembled loop now reads as:
**launch app → reach Home recommendation → open card detail → log extra payment → view reward with goal/trust/next-move context → tap Back to card → land on plain card detail → back out to Home without the loop breaking.**

## Scope completed by task

### T01 — Add one assembled-loop instrumentation test that exercises the full manual payoff path
- Added `ManualLoopFlowTest` to cover the integrated manual payoff path.
- Verified the test source compiles with `:app:assembleDebugAndroidTest`.
- Confirmed the test targets the real assembled seams that matter in this milestone:
  - Home `Overview`
  - recommendation trust
  - card-detail trust
  - payment logging
  - reward goal/trust/next move
  - return to card detail
  - return to Home

### T02 — Fix one concrete integrated-flow regression exposed by assembled verification
- Reproduced a real runtime regression in the reward return path.
- Rejected two failed fixes that either caused bad navigation state or dropped the app out of a usable return flow.
- Landed the working fix in the existing navigation/state seam:
  - reward returns to the existing `card_detail/{id}` back-stack entry
  - reward sets a return signal on the saved-state handle
  - card detail owns cleanup of the reward payload after return
  - a dedicated done-return signal force-closes the payment sheet only for the **done/back to card** path
  - the existing suggestion reopen path remains available only for **pay more**
- Rebuilt and re-verified after the final fix.

## Key patterns this slice established

### 1. Final milestone proof has to come from the assembled loop, not isolated slices
S01-S06 proved important seams individually, but S07 showed that the real integration risk lived in how reward, card detail, and Home behaved together.

**Why it matters for later slices:** new milestone closure work should start with one assembled user path before polishing individual screens.

### 2. Runtime truth beats a green-but-blocked test harness
The new instrumentation test was worth adding, but the attached Android 16 device could not provide trustworthy connected-test evidence because Espresso failed before app assertions.

**Why it matters for later slices:** when the test harness is environment-blocked, use manual device proof and say so plainly instead of pretending the harness result is product evidence.

### 3. Reward exit paths need separate intent, not one shared return behavior
The reward screen has two different user intents:
- continue with another payment
- finish and return to the card

Treating both exits as the same return behavior caused the payment sheet to reopen when the user was trying to finish.

**Why it matters for later slices:** when one screen supports both continue and done actions, their navigation/state cleanup should stay distinct.

## Verification run for slice closure

### Passed
1. `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --max-workers=1`
   - **Result:** pass
2. `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat :app:assembleDebug --max-workers=1`
   - **Result:** pass after the final reward-return fix and post-diagnostic cleanup
3. `'/c/Users/lweis/AppData/Local/Android/Sdk/platform-tools/adb.exe' install -r app/build/outputs/apk/debug/app-debug.apk`
   - **Result:** pass
4. `'/c/Users/lweis/AppData/Local/Android/Sdk/platform-tools/adb.exe' shell am start -n com.lweiss01.paydirt/.MainActivity`
   - **Result:** pass
5. Manual device walkthrough of the assembled loop
   - **Result:** pass
   - **Confirmed:** Home recommendation/trust surfaces render, card detail shows APR confidence, payment logging reaches reward, reward shows goal/trust/next move, and **Back to card** returns to plain card detail with payment history visible and the payment sheet closed
6. Final runtime UI dump verification after `Back to card`
   - **Result:** pass
   - **Confirmed:** card detail contains `CURRENT BALANCE`, `APR CONFIDENCE`, `PAYMENT HISTORY`, and `Log Payment`, while payment-sheet-only elements such as `Amount ($)` are absent

### Environment-blocked, but not product evidence
7. `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat :app:connectedDebugAndroidTest --max-workers=1 -Pandroid.testInstrumentationRunnerArguments.class=com.lweiss01.paydirt.ui.navigation.ManualLoopFlowTest`
   - **Status:** blocked
   - **Failure:** `java.lang.NoSuchMethodException: android.hardware.input.InputManager.getInstance []`
8. `ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk ./gradlew.bat :app:connectedDebugAndroidTest --max-workers=1 -Pandroid.testInstrumentationRunnerArguments.class=com.lweiss01.paydirt.ui.navigation.ManualOnboardingFlowTest`
   - **Status:** blocked with the same failure
   - **Interpretation:** attached-device Espresso failure is environmental on this Android 16 runtime, not a new S07-specific regression

## Observability / diagnostic seams confirmed

### Integrated verification seam
- `ManualLoopFlowTest.kt`
- `reward_done_button`
- `reward_apr_trust_card`
- `card_detail_log_payment_fab`
- Home `Overview` recommendation/trust surfaces

These make it inspectable whether the assembled loop still exists in code and UI.

### Runtime return-path seam
- `RewardNavigationPayload`
- `REWARD_NAVIGATION_PAYLOAD_KEY`
- `REWARD_PAYMENT_SUGGESTION_KEY`
- `REWARD_RETURNED_TO_CARD_KEY`
- `REWARD_DONE_RETURNED_TO_CARD_KEY`
- `NavGraph.kt`
- `CardDetailScreen.kt`

These make it inspectable whether:
- reward returns to the existing card-detail back-stack entry
- done/back-to-card closes the payment sheet
- pay-more can still reopen the payment sheet with a suggested amount
- reward payload cleanup happens after the return lands

## Requirement impact

### R001, R002, R003, R004, R005 — integrated manual loop now substantially proven
S07 now proves, in a live assembled run, that:
- the manual path is usable from Home recommendation through payment logging and reward
- the reward loop remains part of the main app flow rather than a dead-end screen
- the user can return to card detail cleanly after reward
- the product loop still reads coherently after action instead of collapsing into broken navigation state

### R006 and R007 — preserved in the integrated loop
S07 verified that the Home behavior surfaces and APR-trust surfaces from S05/S06 survive assembled use rather than only isolated screen checks.

### R008 — assembled tone proof is materially stronger
The final loop was reviewed as one path, and the result stayed calm, direct, and grounded across recommendation, payment logging, reward, and return.

## What changed downstream readers should know

### For milestone closure
- The main remaining uncertainty is not the manual payoff loop itself; it is the Android 16 connected-test environment.
- Product evidence for M001 should rely on the successful manual runtime walkthrough plus the androidTest compile/build evidence, not on the blocked connected-test run.

## Risks retired vs remaining

### Retired by S07
- Reward no longer returns to a blank dark screen.
- Reward no longer drops the user out to launcher/home.
- `Back to card` no longer reopens the payment sheet when the user is done.
- The milestone now has one truthful assembled-flow proof path instead of only per-slice claims.

### Still remaining after S07
- Attached-device Espresso / input-stack incompatibility on Android 16 remains unresolved.
- Broader unrelated repo test failures outside this slice still exist and are not newly introduced by S07.

## Bottom line

S07 completed the milestone’s integration proof. The assembled manual payoff loop was exercised against the real app, a genuine reward-return regression was found and fixed, and the final verified path now returns from reward to plain card detail without blanking, dropping to launcher, or reopening the payment sheet.