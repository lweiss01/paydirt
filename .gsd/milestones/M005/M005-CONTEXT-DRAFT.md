---
depends_on: [M002]
---

# M005: Intelligence and Retention — Context Draft

**Gathered:** 2026-03-25
**Status:** Draft for later discussion

## What Was Already Established

This milestone is for making PayDirt smarter and more alive after the app core, linked-account path, website, and launch preparation are in place. The intent is not to add noisy engagement tricks, but to improve the quality and continuity of guidance.

## Why This Exists

The user wants PayDirt to eventually go beyond static recommendation screens. The repo already contains some groundwork:
- `app/src/main/java/com/lweiss01/paydirt/domain/engine/SmartAPREngine.kt`
- `app/src/main/java/com/lweiss01/paydirt/domain/engine/BehaviorEngine.kt`
- Vico chart dependencies in Gradle
- payoff/progress concepts already present in reward-oriented UI

This milestone is where those capabilities become a fuller long-term product layer.

## Provisional Scope

Likely slices for this milestone:
- smart APR in user-facing recommendations
- momentum and payoff progress surfaces
- recommendation recompute on meaningful balance changes
- payoff curve and visual forecasting
- weekly summaries and return-loop messaging
- opt-in notification nudges

## Done Probably Looks Like

- recommendations improve as richer data and heuristics become available
- progress feels grounded and useful rather than gamified
- users can understand payoff trajectory visually
- the app can bring users back with restrained, opt-in follow-up surfaces

## Known Risks

- retention features can easily violate the product tone if they become naggy or celebratory
- smart APR and other intelligence work may depend on the realities of linked-account data after M002
- visual forecasting can create false precision if confidence and uncertainty are not handled honestly
- this milestone may need reprioritization after real launch feedback

## Open Questions For Dedicated Discussion

- Which intelligence features genuinely change user decisions rather than just adding information?
- What should never be pushed through notifications or weekly summaries?
- How much forecasting is useful before it starts to imply precision the product does not have?
- Which of these features are best treated as differentiators after launch versus before launch?

## Why This Is A Draft

The long-term direction is clear enough to preserve, but the exact feature mix should wait for earlier milestone learning and live product feedback.
