---
type: research_findings
project: local-model-choice
slug: parakeet-compatibility
created: 2026-11-14T09:06:26Z
updated: 2026-11-14T09:20:00Z
---

# Findings: Parakeet 0.6B compatibility and model choice

## Source References

- OpenWhispr v4.0.6 ModelDownloader.kt, LocalTranscriber.kt, and CleanupProviderConfig.kt.
- Z Board ModelCatalog.kt, ModelStore.kt, ModelDownloader.kt, OrukeetEngine.kt, InferenceService.kt, OfflineDictationController.kt, ModelDownloads.kt, and DictationSettings.kt.
- Official k2-fsa sherpa-onnx Parakeet 0.6B v3 int8 release archive and README.

## Observations

- The exact package is a NeMo transducer with encoder, decoder, joiner, and tokens files; it is not an Orukeet/BPE package.
- OpenWhispr's nonempty dictionary changes the decoding algorithm and hotword profile, so model weights alone do not establish identical recognition behavior.
- OpenWhispr's optional cleanup provider is a separate cloud postprocessor and must not be enabled or counted as raw ASR quality.
- Z Board's current activation cleanup and single current catalog entry are unsafe for multi-model retention.
- A fresh Z Board download is simpler and approved; private cross-app storage reuse is unnecessary.

## Options Considered

| Option | Pros | Cons | Decision |
| --- | --- | --- | --- |
| Trusted Orukeet + Parakeet catalog | Small UI, exact compatibility, clear integrity | Only two families initially | Approved |
| Generic file importer now | Broad user flexibility | Cannot validate arbitrary architectures/layouts; larger security and UX scope | Deferred |
| Reuse OpenWhispr private files | Avoid duplicate storage | Private-storage access is unavailable/unsafe and not required | Rejected |

## Fold-Forward Candidates

| Finding | Target Artifact | Proposed Change |
| --- | --- | --- |
| Activation deletes other models | ModelStore/controller | Preserve installed models; add explicit per-model delete |
| Engine is Orukeet-specific | Engine/catalog | Drive file and hotword profile from trusted metadata |
| Scheduler assumes one model | ModelDownloads | Include model ID in request, worker input, and authorization |
| Model source is large | Model release/settings | Stable per-file mirror, exact hashes, download-size disclosure |

## Open Questions

- Physical latency/accuracy and resource behavior remain unverified without separately authorized device acceptance.
- The fork's authorized release signer availability remains to be established after source qualification.
