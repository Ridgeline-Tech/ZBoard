---
name: Supported local model choice
slug: local-model-choice
owner: project-lead
status: active
created: 2026-11-14T09:05:27Z
updated: 2026-11-14T09:20:00Z
outcome: Users can preserve Orukeet and independently download, select, and remove Parakeet 0.6B through a supported settings flow.
uncertainty: high
probe_required: true
probe_status: completed
---

# Spec: Supported local model choice

## Executive Summary

Add a small supported-model experience to Z Board settings. The first slice keeps Orukeet and adds Parakeet 0.6B as a trusted built-in choice. Models remain separately installed and the owner explicitly chooses which installed model is active. The implementation will use a fresh Z Board-managed download and will not access OpenWhispr private storage.

## Problem and Users

Z Board currently hard-codes one Orukeet model and deletes other installed versions when activation changes. Zac prefers the perceived result from OpenWhispr's Parakeet 0.6B configuration and wants the fastest, most accurate free local option, while retaining Orukeet and user choice. That preference is subjective evidence, not benchmark proof.

## Outcome and Success Metrics

- Settings lists supported Orukeet and Parakeet 0.6B choices with distinct install and active states.
- A user can download, activate, and remove either model without deleting the other model or resetting keyboard settings.
- Parakeet uses its verified NeMo transducer package and model-specific decoding metadata.
- Existing Orukeet installations and selection continue to work.
- No cloud cleanup, generic model-file import, cross-app private-storage access, phone installation, or comparative performance claim is introduced.

## User Stories

- US-001: As a Z Board user, I want to download Parakeet 0.6B, so I can try the owner's preferred local model.
- US-002: As a Z Board user, I want to switch between installed supported models, so I retain control and can return to Orukeet.
- US-003: As a Z Board user, I want to remove one unused model, so I can reclaim storage without deleting the active model or unrelated data.

## Acceptance Scenarios

- AC-001: Given an existing Orukeet installation, when Parakeet is downloaded, then Orukeet remains installed and the current selection remains unchanged until Use is chosen.
- AC-002: Given both models are installed, when Use is chosen for either one, then that model becomes current and dictation loads its matching files and decoding profile.
- AC-003: Given Parakeet and nonempty dictionary terms, when the engine starts, then modified-beam-search uses cjkchar, max active paths 4, score 2.0, and no BPE vocabulary.
- AC-004: Given an empty dictionary, when either model starts, then greedy search is used with no hotword file.
- AC-005: Given an inactive installed model, when Delete is confirmed, then only that model is removed; the active model, settings, and other app data remain.
- AC-006: Given the active model, when Delete is requested, then the UI prevents destructive deletion until another route/model is selected.

## Scope

### In Scope

- Trusted built-in catalog with Orukeet and Parakeet 0.6B.
- Fresh Z Board-owned, per-file, integrity-pinned Parakeet download.
- Separate installed and selected model state.
- Model-specific engine metadata and settings controls.
- Focused lifecycle, configuration, and settings tests.

### Out of Scope

- Arbitrary model archives/files or a generic importer.
- Whisper Base, Moonshine Tiny, or Moonshine Streaming support in this slice.
- Access to OpenWhispr private storage or reuse of its downloaded file.
- Automatic cloud cleanup or any remote transcription/cleanup service.
- Gboard comparison, emulator comparison campaign, phone changes, app install, data reset, or benchmark claims.

## Functional Requirements

- FR-001: Preserve the existing Orukeet model ID and installed directory.
- FR-002: Add exact Parakeet model/package metadata and SHA-256 pins.
- FR-003: Thread an explicit supported model ID through download authorization and worker state.
- FR-004: Do not delete other installed models during activation.
- FR-005: Delete only an explicitly selected inactive model.
- FR-006: Configure model-specific hotword behavior without enabling cloud cleanup.

## Non-Functional Requirements

- Downloads remain resumable, range-validated, and integrity-checked.
- Settings copy states that models are supported built-ins, not arbitrary imports.
- The large Parakeet footprint is disclosed before download.
- Existing app/package identity and user data remain unchanged.

## Assumptions

- sherpa-onnx 1.13.4 supports the verified NeMo transducer files; focused engine construction tests must confirm this in Z Board.
- A Z Board release can host the exact verified model files independently of app signing.

## Needs Clarification

- None for the approved first slice. Any request for general model import or a different architecture requires a new scoped decision.

## Hypotheses and Unknowns

- Runtime speed and accuracy on Zac's phone remain unverified because no phone change or comparison is authorized.
- OpenWhispr dictionary and optional cleanup settings are unknown, so identical raw weights may not reproduce the subjective result.

## Touchpoints to Exercise

- Model catalog and store lifecycle.
- Download authorization and worker recovery.
- Engine configuration for empty and nonempty dictionary terms.
- Settings model rows and active/inactive deletion guards.

## Probe Findings

- OpenWhispr v4.0.6 maps Parakeet 0.6B to sherpa-onnx-nemo-parakeet-tdt-0.6b-v3-int8 and NEMO_TRANSDUCER.
- Its nonempty dictionary path uses modified_beam_search, maxActivePaths 4, hotwordsScore 2.0, cjkchar, and no BPE vocabulary; otherwise it uses greedy_search.
- The verified official archive contains encoder.int8.onnx, decoder.int8.onnx, joiner.int8.onnx, and tokens.txt and is about 670.6 MB extracted.

## Footguns Discovered

- Existing activation removes unused model directories and must be changed before a multi-model UI is safe.
- A model name alone is insufficient: architecture, file layout, feature dimensions, and hotword metadata must match.
- The project initializer's unquoted heredoc interpreted Markdown command syntax; generated planning artifacts were inspected and repaired before execution.

## Remaining Unknowns

- Final CI-signed update compatibility depends on the fork's authorized signing route and cannot be claimed from a local/debug build.

## Dependencies

- ZBoard issue #2 and owner approval.
- Exact verified model package hosted from a stable ZBoard release.
- Normal independent review and repository checks.

## Approval Notes

- 2026-11-14: Zac approved the first slice: built-in Orukeet + Parakeet 0.6B selection now, with general model import deferred.
- Guidance loaded before this scope: Engineering Playbook revision 2026-11-13, SHA-256 55D0475BA926642170845222B7077579BAF3210BE998BC3F419786E56D884A0C; canonical zacs-dev SHA-256 6ED41538383FDFE6C0C34726F53800FCCEBBF5FC9574F3908E2AADC469FF29D0.
