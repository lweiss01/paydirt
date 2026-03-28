# S08 Live Closure Evidence

## Outcome

**Task status:** Passed on live runtime
**Product fix required:** Yes — test/runtime seam + deterministic test interactions
**Runtime used:** Pixel 9a (Android 16)
**Evidence date:** 2026-03-28

S08 remediation was rerun on an attached physical device with explicit clean-state resets. Both required connected flows now pass.

## Preconditions Rechecked

- `adb devices -l` showed attached device (`Pixel 9a`).
- `adb install -r app/build/outputs/apk/debug/app-debug.apk` succeeded before each proof run.
- `adb shell pm clear com.lweiss01.paydirt` succeeded before each proof run.

## Flow 1 — Fresh-data onboarding closure (R001)

Command run:

```bash
ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk \
ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk \
./gradlew.bat :app:connectedDebugAndroidTest --max-workers=1 \
  -Pandroid.testInstrumentationRunnerArguments.class=com.lweiss01.paydirt.ui.navigation.ManualOnboardingFlowTest
```

Observed result:

- `:app:connectedDebugAndroidTest` completed successfully.
- `ManualOnboardingFlowTest` passed on Pixel 9a.

Verified surfaces:

- Onboarding reveal: **exercised + pass**
- Home goal framing: **exercised + pass**
- First recommendation framing: **exercised + pass**

## Flow 2 — Goal-setting / reward closure (R005)

Command run:

```bash
ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk \
ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk \
./gradlew.bat :app:connectedDebugAndroidTest --max-workers=1 \
  -Pandroid.testInstrumentationRunnerArguments.class=com.lweiss01.paydirt.ui.navigation.GoalSettingFlowTest
```

Observed result:

- `:app:connectedDebugAndroidTest` completed successfully.
- `GoalSettingFlowTest` passed on Pixel 9a.

Verified surfaces:

- Goal edit/save framing on Home: **exercised + pass**
- Card-detail payment logging path: **exercised + pass**
- Reward goal framing (`$20 of $125`): **exercised + pass**

## Classification

This is now **live closure achieved** for S08 remediation scope.

## Artifacts

- Connected test reports:
  - `app/build/reports/androidTests/connected/debug/index.html`
  - `app/build/outputs/androidTest-results/connected/debug/TEST-Pixel 9a - 16-_app-.xml`
- Runtime readiness context:
  - `.gsd/milestones/M001/slices/S08/runtime-readiness.md`

## Notes on fixes applied during rerun

During live reruns, targeted non-product-behavior changes were made to retire real execution blockers:

1. Android 16 Espresso harness compatibility bump (`espresso-core` 3.7.0 / matching test stack).
2. Main-thread-safe post-save navigation callback in add-card flow.
3. Deterministic list-scroll semantics for connected test stability on this runtime/layout.
