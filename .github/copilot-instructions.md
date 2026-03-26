# Holistic - paydirt

## Session Start

At the start of every session, before doing anything else:
1. Read `HOLISTIC.md` in full.
2. Read `AGENTS.md`.
3. Summarise to the user: what was last worked on, what's planned next, and any known fixes to protect.
4. Ask: "Continue as planned, tweak the plan, or do something different?"
5. Use the repo-local Holistic helper in this repo: Windows `.\.holistic\system\holistic.cmd resume --agent cursor`; macOS/Linux `./.holistic/system/holistic resume --agent cursor`.

## Current Objective

Capture work and prepare a clean handoff.

## Latest Status

Committed: docs: refresh holistic status

## Do Not Regress - Known Fixes

- [FIX] S07 reward done return now lands on plain card detail instead of reopening the payment sheet or breaking the app flow | files: .gsd/worktrees/M001/app/src/main/java/com/lweiss01/paydirt/ui/navigation/NavGraph.kt,.gsd/worktrees/M001/app/src/main/java/com/lweiss01/paydirt/ui/screens/cards/CardDetailScreen.kt,.gsd/worktrees/M001/.gsd/milestones/M001/slices/S07/S07-SUMMARY.md,.gsd/worktrees/M001/.gsd/milestones/M001/slices/S07/S07-UAT.md,.gsd/worktrees/M001/.gsd/milestones/M001/M001-ROADMAP.md | risk: collapsing reward done and pay-more into one return path, or moving reward payload cleanup away from card-detail return handling
## Before ending this session

Run:
```
holistic handoff --summary "..." --next "..."
```
This keeps repo memory current for the next agent.
