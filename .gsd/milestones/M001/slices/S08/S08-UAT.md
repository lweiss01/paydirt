# S08: Fresh-data onboarding and goal-loop live closure — UAT

**Milestone:** M001
**Executed on:** 2026-03-28
**Runtime:** Pixel 9a (Android 16)

## Preconditions (met)
- `adb devices -l` showed attached runtime.
- Debug/app-test APKs built successfully.
- `adb install -r app/build/outputs/apk/debug/app-debug.apk` succeeded.
- Clean-state reset succeeded before each flow via `adb shell pm clear com.lweiss01.paydirt`.

## Executed Test Case 1 — Fresh-data onboarding closure (R001)
Command:

```bash
ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk \
./gradlew.bat :app:connectedDebugAndroidTest --max-workers=1 \
  -Pandroid.testInstrumentationRunnerArguments.class=com.lweiss01.paydirt.ui.navigation.ManualOnboardingFlowTest
```

Result: ✅ pass

Observed acceptance points:
- Onboarding copy visible from clean state.
- Add-first-card flow returns to Home.
- First recommendation reveal displayed.
- Home goal framing surfaces displayed.

## Executed Test Case 2 — Goal-setting / reward closure (R005)
Command:

```bash
ANDROID_HOME=/c/Users/lweis/AppData/Local/Android/Sdk ANDROID_SDK_ROOT=/c/Users/lweis/AppData/Local/Android/Sdk \
./gradlew.bat :app:connectedDebugAndroidTest --max-workers=1 \
  -Pandroid.testInstrumentationRunnerArguments.class=com.lweiss01.paydirt.ui.navigation.GoalSettingFlowTest
```

Result: ✅ pass

Observed acceptance points:
- Monthly goal edited from default to `$125`.
- Updated goal reflected on Home framing.
- Card-detail payment logging path executed.
- Reward goal card reflected updated goal framing (`$20 of $125`).

## Notes
- Rerun required targeted stabilization of test/runtime seams (Android 16 Espresso compatibility, main-thread save callback, feed-level deterministic scroll assertions).
- No remaining blocker within S08 closure scope.
