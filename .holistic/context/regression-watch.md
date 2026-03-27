# Regression Watch

Use this before changing existing behavior. It is the short list of fixes and outcomes that future agents should preserve.

## Capture work and prepare a clean handoff.

- Goal: Capture work and prepare a clean handoff.
- Durable changes:
- Synced S06 APR trust work and S07 assembled-loop reward-return fix onto main, reverted Holistic-only commits from GitHub, deleted remote milestone/M001 branch, and left local main clean.
- Why this matters:
- Main branch now contains the verified S06 trust work and S07 assembled-loop fix
- GitHub no longer shows the milestone/M001 compare prompt
- repo is ready to start M002 from main
- Do not regress:
- [FIX] S07 reward done return now lands on plain card detail instead of reopening the payment sheet or breaking the app flow | files: .gsd/worktrees/M001/app/src/main/java/com/lweiss01/paydirt/ui/navigation/NavGraph.kt,.gsd/worktrees/M001/app/src/main/java/com/lweiss01/paydirt/ui/screens/cards/CardDetailScreen.kt,.gsd/worktrees/M001/.gsd/milestones/M001/slices/S07/S07-SUMMARY.md,.gsd/worktrees/M001/.gsd/milestones/M001/slices/S07/S07-UAT.md,.gsd/worktrees/M001/.gsd/milestones/M001/M001-ROADMAP.md | risk: collapsing reward done and pay-more into one return path, or moving reward payload cleanup away from card-detail return handling
- Avoid collapsing reward done and pay-more into one return path
- avoid moving reward payload cleanup away from card-detail return handling
- treat Android 16 connected-test InputManager failure as environment-specific unless reproduced as app behavior
- Source session: session-2026-03-25T20-57-34-270Z

