# Decisions Register

<!-- Append-only. Never edit or remove existing rows.
     To reverse a decision, add a new row that supersedes it.
     Read this file at the start of any planning or research phase. -->

| # | When | Scope | Decision | Choice | Rationale | Revisable? | Made By |
|---|------|-------|----------|--------|-----------|------------|---------|
| D001 | M001 | arch | Milestone sequencing | Core app first, then linked accounts, then website, then Play submission, then intelligence/retention | Launch depends on a coherent app core and real data flows before outward packaging and later refinement | Yes — if release priorities change materially | collaborative |
| D002 | M001 | scope | Website scope | Simple launch/marketing site with optional launch-interest signup, not a large web product | User explicitly wants a simple page for the app and possibly a signup to hear about launch | No | human |
| D003 | M001 | convention | Product tone | Calm, direct, unembarrassing, specific language across app and launch surfaces | Tone is part of product correctness, not decorative copy | No | human |
| D004 | M001 | pattern | M001 planning focus | Use the manual-entry payoff loop as the first milestone's integration path | The repo already has a usable manual foundation, while linked data remains a larger integration risk for a later milestone | Yes — if manual path proves insufficient for launch | collaborative |
| D005 | M001 | pattern | Post-payment experience in M001 | Every logged payment navigates to a full reward screen that leads with impact dollars and then suggests the best next payoff move with brief reasoning and optional deeper math | The payment moment is the strongest product beat. Making it a real destination creates a coherent loop, reinforces payoff value immediately, and keeps the app mathematically honest without praise theater | Yes — if live testing shows the flow is too interruptive | collaborative |
| D006 | M001 | pattern | Manual onboarding and earliest recommendation behavior in M001 | Use one short trust-building intro, then go straight to single-form card entry and show a recommendation as soon as the app has enough data, even if only one card is entered | PayDirt should earn value quickly instead of forcing a full debt inventory or a multi-screen tour. The app should stay useful early and explain that recommendations improve as more cards are added | Yes — if first-run testing shows users are confused without more guidance | collaborative |
| D007 | M001 | convention | Handling partial card data in M001 | Balance is required but may be estimated; APR and minimum payment may be blank or estimated; recommendation confidence and payoff values must be labeled honestly on recommendation surfaces | The app should work with incomplete information while never pretending that estimated inputs are precise. Trust depends on early usefulness plus explicit confidence cues | No | collaborative |
