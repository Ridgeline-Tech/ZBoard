---
name: Supported local model choice
status: done
lead: project-lead
created: 2026-11-14T09:05:27Z
updated: 2026-11-14T10:35:00Z
linear_project_id:
risk_level: high
spec_status_at_plan_time: active
---

# Delivery Plan: Supported local model choice

## What Changed After Probe

The first slice is approved as a trusted catalog rather than a generic file importer. Parakeet 0.6B will use a fresh Z Board-managed copy. Cross-app reuse is not required. Engine setup must reproduce the verified architecture and hotword profile, while making no speed/accuracy claim.

## Technical Context

The current code assumes one global ModelCatalog.current, hard-codes Orukeet files in the engine, authorizes a download using only id:token, and removes noncurrent model versions after activation. These contracts must become model-aware together; changing only settings would risk deletion or loading the wrong architecture.

## Architecture Decisions

- Keep a trusted in-code catalog. Each ModelRelease owns display/storage metadata, exact files and hashes, expected bytes, engine profile, and hotword profile.
- Preserve Orukeet's existing ID and directory. Add Parakeet as a separate immutable ID.
- Thread model ID through scheduling, worker authorization, download, install, activation, and deletion.
- Preserve all installed supported models. Selection is an atomic current-pointer update; removal is explicit and model-specific.
- Configure a generic offline transducer engine from catalog metadata. Empty dictionary terms use greedy search. Nonempty terms use only the model's verified hotword profile.
- Host the verified extracted Parakeet files in a ZBoard release and use direct pinned per-file URLs; do not unpack arbitrary archives in the app.

## Policy and Contract Checks

- [x] `.project/` remains the execution source of truth
- [x] Probe decision is explicit
- [x] Evidence gates are defined before handoff
- [x] External sync writes require dry-run or operator approval

## Generated Artifact Map

- `lib/offline-asr/`: catalog, store, engine, and lifecycle contracts.
- `app/`: settings UI and controller/service wiring.
- `.project/projects/local-model-choice/`: requirements, tasks, decisions, updates, and evidence.
- ZBoard model-data release: immutable model file hosting and attribution.

## Complexity Exceptions

- The approximately 670 MB model package makes integrity, resumability, free-space checks, and stable hosting mandatory even though the UI slice is small.

## Probe-Driven Architecture Changes

- Retain both models instead of treating all noncurrent directories as garbage.
- Treat model-specific decoding configuration as catalog data rather than a settings label.
- Defer arbitrary import until compatibility validation and user-file security can be designed separately.

## Workstream Design

- WS-A Model lifecycle and UI: catalog, engine, store, download authorization, settings selection and deletion.
- WS-B Verification and delivery: model release provenance, focused/full checks, independent review, source PR, and distributable-signing investigation.

## Milestone Strategy

1. Validate model package and approved scope.
2. Implement model-aware lifecycle and settings.
3. Publish/read back exact model-data assets.
4. Run repository checks and independent candidate review.
5. Merge source only after gates pass; claim a distributable update only if the authorized signing route proves continuity.

## Rollout Strategy

The UI defaults to the existing selection when valid. Existing Orukeet installations require no redownload. Parakeet is opt-in and never selected automatically after download. No phone rollout occurs in this task.

## Test Strategy

- Unit tests for catalog manifests, current-pointer preservation, model-specific removal, and download authorization.
- Engine configuration tests for Orukeet and Parakeet with empty/nonempty dictionary terms.
- Settings logic tests for installed/selected/downloading states and guarded delete.
- Required repository checks: compileDebugKotlin, compileReleaseKotlin, assembleRelease, and git diff --check.
- Independent review of the exact candidate and evidence.

## Rollback Strategy

Source rollback restores the previous single-model code without deleting user model directories. The existing Android v0.11.0 release and Zac's Apps catalog entry remain untouched until an independently verified compatible update exists.

## Remaining Delivery Risks

- Parakeet resource use on the target phone is unmeasured.
- Fork CI may not have the upstream signer; source readiness and install-compatible release readiness are separate gates.
- Model hosting may hit GitHub asset or bandwidth constraints and must be verified by exact readback.
