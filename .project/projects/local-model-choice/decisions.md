# Decisions

## 2026-11-14 - Approved first slice

Zac approved built-in Orukeet + Parakeet 0.6B selection first, with general model import later. This is scope approval, not a mandated implementation. Preserve Orukeet and owner choice; do not make Parakeet automatic.

## 2026-11-14 - Fresh Z Board-managed model copy

Use a fresh compatible download because Zac does not require cross-app reuse. Do not access OpenWhispr private storage, remove OpenWhispr, or modify its data.

## 2026-11-14 - Trusted model catalog

Use explicit supported model manifests and per-file hashes. Defer arbitrary files, Whisper Base, Moonshine Tiny, and Moonshine Streaming because names do not prove engine/architecture/file compatibility.

## 2026-11-14 - Decoding profiles

Preserve Orukeet's verified BPE hotword behavior. For Parakeet 0.6B, use cjkchar, score 2.0, max active paths 4, and no BPE vocabulary only when dictionary terms are nonempty; use greedy search otherwise.

## 2026-11-14 - No automatic cloud cleanup

OpenWhispr's cleanup provider is a separate post-transcription cloud feature and its owner state is unknown. Z Board will not enable or imitate cloud cleanup, nor attribute any possible cleanup effect to model quality.

## 2026-11-14 - Evidence and safety boundaries

No Gboard or emulator comparison is reopened. No phone install or modification is authorized. Performance remains unverified until separately authorized physical acceptance. A distributable update requires package/version/signing continuity evidence from an authorized route.

## 2026-11-14 - Planning scaffold correction

The repository initializer completed but its unquoted heredoc interpreted Markdown command syntax, leaving placeholder planning files. We inspected generated artifacts, repaired them with literal content, and will validate the resulting project rather than trusting the initializer's exit status alone.

## 2026-11-14 - Separate source, model-data, and app-release proof

Model-data publication, source merge, package/version identity, and update-compatible signing are independent gates. Evidence: the exact Parakeet assets were published and source PR #3 merged, while the local 0.12.0 APK remained debug-signed and was not published. Future release handoffs must report these gates separately.

## 2026-11-14 - Preserve runtime uncertainty

Matching OpenWhispr's Parakeet decoder metadata is source compatibility evidence, not proof of latency, accuracy, or dictionary behavior. Independent review confirmed the source mapping and retained real-model inference as an explicit verification limit.

## 2026-11-14 - Test wrapper event logging is not the test result

The Gradle gate and structured run receipt succeeded, but the post-run `log-event.js` helper failed because the parent workspace declares Node ESM while the helper uses CommonJS. Preserve the underlying command exit/log as authoritative and fix the logger through a separately reviewed tooling change.
