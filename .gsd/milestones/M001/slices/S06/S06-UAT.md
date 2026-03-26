# S06 UAT — APR confidence and trust cues

**Milestone:** M001  
**Slice:** S06  
**Purpose:** Verify that recommendation-related UI communicates APR confidence honestly and consistently across Home, card detail, and reward.

## Preconditions

- Install the current debug build of the app.
- Start from Home.
- Have at least one manual card in the app.
- For the clearest proof, use a card whose APR source is still unknown or inferred so the trust cue is visible and easy to compare.
- A device or emulator is required for full runtime verification.

## Test Case 1 — Home recommendation shows APR trust context

1. Launch the app and open Home.
   - **Expected:** Home loads normally.
2. Stay on the **Overview** tab.
3. Inspect the next-move hero.
   - **Expected:** A visible APR trust badge is present near the recommendation.
4. Read the helper text next to the badge.
   - **Expected:** The wording explains whether the APR is confirmed, estimated, or not confirmed.
   - **Expected:** The wording is specific and calm, not vague or overstated.

## Test Case 2 — Card detail shows the same APR trust story instead of a raw APR-only header

1. From Home, open the recommended card or any card row.
2. Inspect the top detail card.
   - **Expected:** The card shows **APR CONFIDENCE** rather than a raw standalone APR label.
3. Inspect the badge and helper text.
   - **Expected:** A visible `APRTrustBadge` appears.
   - **Expected:** The helper copy matches the same trust state shown on Home.
4. Compare the message with Home.
   - **Expected:** Home and card detail tell the same confidence story for the same card.

## Test Case 3 — Unknown APR stays explicitly unknown

1. Use a card with no confirmed APR.
2. Open Home, then card detail.
3. Read the trust state on both surfaces.
   - **Expected:** The badge says **APR not confirmed**.
   - **Expected:** The helper text says the recommendation may sharpen once the exact APR is added from a statement.
4. Confirm the UI does not pretend certainty.
   - **Expected:** No confirmed/estimated language appears incorrectly.

## Test Case 4 — Confirmed APR stays explicitly confirmed

1. Use a card whose APR came from the user or Plaid.
2. Open Home and card detail.
3. Inspect the trust surfaces.
   - **Expected:** The badge says **APR confirmed**.
   - **Expected:** Helper text explains whether the confirmed APR came from user input or linked-account data.
4. Compare both screens.
   - **Expected:** Home and card detail remain aligned.

## Test Case 5 — Reward keeps APR trust context after payment logging

1. From card detail, tap **Log Payment**.
2. Enter or keep a valid amount.
3. Mark the payment as an **extra payment**.
4. Submit the payment.
   - **Expected:** The app navigates into reward.
5. Inspect the reward screen.
   - **Expected:** The reward screen still shows the post-payment surfaces like **INTEREST CUT**, **MONTHLY GOAL**, and **BEST NEXT MOVE**.
6. Find the APR trust section.
   - **Expected:** A visible **APR CONFIDENCE** card is present on reward.
7. Read the badge and helper text.
   - **Expected:** Reward shows the same trust state and helper copy as Home/card detail for that card.

## Test Case 6 — Confidence story remains consistent across the full loop

1. Read the APR trust messaging on:
   - Home
   - card detail
   - reward
2. Compare the wording.
   - **Expected:** The badge label and helper text remain compatible across all three surfaces.
   - **Expected:** The app does not shift from “not confirmed” to “confirmed” without a real APR-source change.

## Test Case 7 — Trust messaging stays calm and direct

1. Read the trust copy across Home, card detail, and reward.
2. **Expected:**
   - The wording states what is known and what is not known.
   - It does not use scary warning language.
   - It does not fake certainty.
   - It does not drift into generic tooltip-style finance jargon.

## Edge Cases

### Edge Case A — Reward should not drop trust state
1. Open reward after logging a payment.
   - **Expected:** Reward still shows APR trust context; it does not revert to a trust-free surface.

### Edge Case B — Screen-specific wording should not drift
1. Compare helper text for the same card on Home, card detail, and reward.
   - **Expected:** The same APR source produces compatible wording on every screen.

### Edge Case C — Trust cue should not crowd out the main next move
1. Read reward after a payment.
   - **Expected:** The trust card is visible, but **INTEREST CUT**, **MONTHLY GOAL**, and **BEST NEXT MOVE** remain readable and primary.

## Failure Signals

Treat any of these as an S06 regression:
- Card detail still shows only a raw APR label/number with no trust context.
- Reward shows no APR trust context after payment logging.
- Home, card detail, and reward disagree about whether the APR is confirmed or not confirmed.
- Helper text differs materially between screens for the same APR source.
- Trust wording becomes alarmist, vague, or falsely certain.
- Reward navigation loses APR source state and falls back to a blank/incorrect trust surface.

## Evidence captured during this slice

This slice was runtime-checked on an attached Android device. The live app was inspected on:
- Home recommendation
- card detail
- reward after a live extra-payment submit

Runtime evidence confirmed:
- `APR CONFIDENCE` on card detail
- `APR CONFIDENCE` on reward
- `APR not confirmed` badge and matching helper text on card detail and reward
- reward still preserves the main post-payment structure (`INTEREST CUT`, `MONTHLY GOAL`, `BEST NEXT MOVE`) alongside the trust card

## Notes for tester

- This slice is about **trust continuity across the recommendation loop**, not about changing APR source logic itself.
- If you want to test all trust states, use different cards with different `aprSource` values (user-entered, Plaid, inferred, unknown).
- If broader repo tests are failing elsewhere, that does not invalidate this slice-specific UAT; this script focuses on the confidence/trust surfaces delivered by S06.
