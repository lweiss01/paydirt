# S07 UAT — Core flow polish and verification

**Milestone:** M001  
**Slice:** S07  
**Purpose:** Verify that the assembled manual payoff loop works cleanly from recommendation to payment log to reward to return, and that the integrated flow still feels coherent and grounded.

## Preconditions

- Install the current debug build of the app.
- Use a device or emulator with the latest S07 build.
- Have at least one manual card in the app, or use the first-run flow to add one.
- The clearest proof uses a card with visible Home recommendation content and a working extra-payment flow.
- If connected Android tests are unavailable on the target runtime, use the manual device walkthrough below as the authoritative proof path.

## Test Case 1 — Home opens into a usable recommendation surface

1. Launch the app.
   - **Expected:** The app opens normally and stays in PayDirt.
2. Open Home.
   - **Expected:** Home shows the main recommendation/progress loop.
3. Stay on **Overview**.
   - **Expected:** The next move/recommendation surface is visible.
   - **Expected:** Home still shows progress and trust context rather than a blank or partial state.

## Test Case 2 — Recommended card opens into card detail with APR trust visible

1. From Home, open the recommended card or a visible card row.
   - **Expected:** Card detail opens successfully.
2. Inspect the top detail card.
   - **Expected:** `CURRENT BALANCE` is visible.
   - **Expected:** `APR CONFIDENCE` is visible.
   - **Expected:** The card-detail screen is fully rendered and interactive.

## Test Case 3 — Logging an extra payment reaches reward

1. From card detail, tap **Log Payment**.
   - **Expected:** The payment sheet opens.
2. Enter a valid amount.
3. Keep or enable **extra payment**.
4. Submit the payment.
   - **Expected:** The app navigates into reward.
5. Inspect reward.
   - **Expected:** Reward shows the expected post-payment surfaces such as:
     - `INTEREST CUT`
     - `MONTHLY GOAL`
     - `APR CONFIDENCE`
     - `BEST NEXT MOVE`

## Test Case 4 — Back to card returns to plain card detail

1. From reward, tap **Back to card**.
   - **Expected:** The app returns to card detail.
2. Inspect the returned screen.
   - **Expected:** Card detail shows normal detail content such as `CURRENT BALANCE`, `APR CONFIDENCE`, and `PAYMENT HISTORY`.
   - **Expected:** The floating `Log Payment` action is visible.
   - **Expected:** The payment sheet is **not** open.
3. Check for payment-sheet-only elements.
   - **Expected:** `Amount ($)` is absent.
   - **Expected:** `This is an extra payment` is absent.
   - **Expected:** No blank dark screen appears.
   - **Expected:** The app does not drop to launcher/home.

## Test Case 5 — Back out to Home and keep the loop coherent

1. From card detail, tap the top-left **Back** control.
   - **Expected:** The app returns to Home.
2. Inspect Home.
   - **Expected:** The recommendation/progress loop is still visible and coherent.
   - **Expected:** The user can continue using the payoff loop without confusion.
3. Confirm app stability.
   - **Expected:** The app remains in PayDirt and does not blank or lose the current task.

## Test Case 6 — Pay more remains distinct from done/back to card

1. Reach reward again after logging a payment.
2. Use **Pay more** instead of **Back to card**.
   - **Expected:** The return path preserves the continue-paying behavior.
3. Compare this with **Back to card**.
   - **Expected:** The two actions do not behave the same.
   - **Expected:** Only the continue-paying path is allowed to reopen payment-entry behavior.

## Test Case 7 — Tone still feels calm and direct across the assembled loop

1. Read the copy across:
   - Home recommendation
   - card detail trust framing
   - reward goal/trust/next-move surfaces
2. **Expected:**
   - wording stays direct and grounded
   - the UI does not drift into gamified or hype-heavy language
   - the integrated path still feels like one product loop rather than disconnected screens

## Edge Cases

### Edge Case A — Reward return must not blank the app
1. Return from reward to card detail.
   - **Expected:** No persistent blank dark screen appears.

### Edge Case B — Reward return must not drop to launcher
1. Return from reward to card detail.
   - **Expected:** The app remains in the PayDirt task.

### Edge Case C — Done return must not reopen the payment sheet
1. Tap **Back to card** from reward.
   - **Expected:** The payment sheet does not reopen.

### Edge Case D — Pay-more path should remain available
1. Use **Pay more** from reward.
   - **Expected:** The continue-paying flow still works and is not broken by the done-return fix.

## Failure Signals

Treat any of these as an S07 regression:
- reward returns to a blank dark screen
- reward drops the user to launcher/home
- `Back to card` lands on an open payment sheet instead of plain card detail
- reward loses `MONTHLY GOAL`, `APR CONFIDENCE`, or `BEST NEXT MOVE`
- returning to Home leaves the user in a broken or confusing loop state
- the assembled flow tone becomes noisy, gamified, or disconnected between screens

## Evidence captured during this slice

This slice was verified through:
- compiled androidTest coverage for `ManualLoopFlowTest`
- successful `:app:assembleDebug` and `:app:assembleDebugAndroidTest` builds
- live install/launch on an attached Android device
- manual runtime walkthrough of the assembled loop
- final UI dump confirmation after **Back to card** showing:
  - `CURRENT BALANCE`
  - `APR CONFIDENCE`
  - `PAYMENT HISTORY`
  - `Log Payment`
  - and absence of payment-sheet-only fields such as `Amount ($)`

Connected Android instrumentation on the attached Android 16 runtime remained environment-blocked by:
- `java.lang.NoSuchMethodException: android.hardware.input.InputManager.getInstance []`

That failure affected both the new S07 flow test and an existing onboarding flow test, so it was treated as environment evidence rather than product-regression evidence.

## Notes for tester

- For this slice, the most important proof is not whether each individual screen loads; it is whether the full manual loop still holds together after the user acts.
- If the target device has the same Android 16 Espresso/input-stack issue, rely on the manual UAT path above and record the environment block separately.
- Keep verification truthful: passing build/compile evidence does not replace runtime proof for this slice.