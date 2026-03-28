---
id: S08
parent: M001
milestone: M001
provides:
  - Runtime-backed live closure evidence for fresh-data onboarding proof (R001).
  - Runtime-backed live closure evidence for goal-setting/reward-loop proof (R005).
  - Stabilized connected-test harness for Android 16 runtime behavior in this repo.
requires:
  - slice: S07
    provides: Assembled manual-loop seams and prior integrated flow fixes.
affects:
  - M001 completion and validation
  - Downstream milestones relying on connected-test confidence
key_files:
  - app/src/main/java/com/lweiss01/paydirt/ui/screens/cards/AddEditCardScreen.kt
  - app/src/main/java/com/lweiss01/paydirt/ui/screens/home/HomeScreen.kt
  - app/src/main/java/com/lweiss01/paydirt/ui/screens/cards/CardDetailScreen.kt
  - app/src/androidTest/java/com/lweiss01/paydirt/ui/navigation/ManualOnboardingFlowTest.kt
  - app/src/androidTest/java/com/lweiss01/paydirt/ui/navigation/GoalSettingFlowTest.kt
  - gradle/libs.versions.toml
  - .gsd/milestones/M001/slices/S08/live-closure-evidence.md
key_decisions:
  - Upgrade Espresso/test stack to retire Android 16 InputManager reflection break.
  - Keep post-save navigation callback on Main dispatcher to satisfy lifecycle-thread constraints.
  - Use feed-level deterministic scroll semantics in connected tests for stable assertions on this device/layout.
patterns_established:
  - For device-backed connected tests on long Home feeds, scroll list containers to tagged nodes before display assertions.
  - For nav callbacks triggered after suspend save calls, enforce main-thread dispatch before `popBackStack`.
observability_surfaces:
  - `adb shell pm clear com.lweiss01.paydirt`
  - Connected-test XML report under `app/build/outputs/androidTest-results/connected/debug/`
  - Connected-test HTML report under `app/build/reports/androidTests/connected/debug/index.html`
drill_down_paths:
  - .gsd/milestones/M001/slices/S08/tasks/T01-SUMMARY.md
  - .gsd/milestones/M001/slices/S08/tasks/T02-SUMMARY.md
duration: ""
verification_result: passed
completed_at: 2026-03-28T18:00:00.000Z
blocker_discovered: false
---

# S08: Fresh-data onboarding and goal-loop live closure

**S08 now closes with runtime-backed pass evidence: both required fresh-state connected flows were executed on attached device and passed.**

## What Happened

The earlier S08 blocker (no attached runtime) was retired. With Pixel 9a attached, the team reran clean-state remediation proof for both required flows and fixed the concrete seams exposed during execution:

1. Android 16 Espresso harness break (`InputManager.getInstance`) retired by upgrading test dependencies.
2. Add-card post-save navigation callback made main-thread safe to satisfy lifecycle/nav threading contract.
3. Connected test assertions stabilized using feed-level scroll-to-node behavior for deterministic visibility on this runtime/layout.

After those fixes, both `ManualOnboardingFlowTest` and `GoalSettingFlowTest` passed from clean state on live runtime.

## Verification

- `./gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --max-workers=1` — pass
- `adb install -r app/build/outputs/apk/debug/app-debug.apk` — pass
- `adb shell pm clear com.lweiss01.paydirt` — pass
- `./gradlew.bat :app:connectedDebugAndroidTest --max-workers=1 -Pandroid.testInstrumentationRunnerArguments.class=com.lweiss01.paydirt.ui.navigation.ManualOnboardingFlowTest` — pass
- `adb install -r app/build/outputs/apk/debug/app-debug.apk` — pass
- `adb shell pm clear com.lweiss01.paydirt` — pass
- `./gradlew.bat :app:connectedDebugAndroidTest --max-workers=1 -Pandroid.testInstrumentationRunnerArguments.class=com.lweiss01.paydirt.ui.navigation.GoalSettingFlowTest` — pass

## Requirements Advanced

- R001 — now live-validated on attached runtime for first-run onboarding-to-recommendation path.
- R005 — now live-validated on attached runtime for goal-edit + payment-log + reward-goal framing path.

## Requirements Validated

- R001
- R005

## New Requirements Surfaced

None.

## Requirements Invalidated or Re-scoped

None.

## Deviations

None from remediation intent. Work remained within S08 proof-and-fix seam.

## Known Limitations

None within S08 closure scope.

## Follow-ups

- Re-run full connected suite as part of post-milestone hardening if broader Android matrix is introduced.

## Files Created/Modified

- `app/src/main/java/com/lweiss01/paydirt/ui/screens/cards/AddEditCardScreen.kt`
- `app/src/main/java/com/lweiss01/paydirt/ui/screens/home/HomeScreen.kt`
- `app/src/main/java/com/lweiss01/paydirt/ui/screens/cards/CardDetailScreen.kt`
- `app/src/androidTest/java/com/lweiss01/paydirt/ui/navigation/ManualOnboardingFlowTest.kt`
- `app/src/androidTest/java/com/lweiss01/paydirt/ui/navigation/GoalSettingFlowTest.kt`
- `gradle/libs.versions.toml`
- `.gsd/milestones/M001/slices/S08/live-closure-evidence.md`
