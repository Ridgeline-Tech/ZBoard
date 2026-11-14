---
id: WS-A
name: Model lifecycle and settings
owner: project-lead
status: done
created: 2026-11-14T09:20:00Z
updated: 2026-11-14T09:20:00Z
---

# Workstream: WS-A Model lifecycle and settings

## Objective

Implement safe supported-model download, retention, selection, engine configuration, and settings controls for Orukeet and Parakeet 0.6B.

## Owned Files/Areas

- lib/offline-asr model catalog, store, downloader, and engine.
- app dictation controller/service, model work scheduling, and settings UI.
- Focused tests for these contracts.

## Dependencies

- Completed source probe and approved first-slice scope.

## Risks

- A partial model-aware conversion could load wrong files or delete another installed model.

## Handoff Criteria

- All model lifecycle acceptance scenarios have focused tests and the repository compiles.
