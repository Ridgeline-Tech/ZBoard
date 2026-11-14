---
timestamp: 2026-11-14T21:00:00Z
status: in-progress
task: ZBoard-5
stream: native-cloud-transcription
---

# Native Deepgram Implementation

## Completed
- Bound the source change to Ridgeline-Tech/ZBoard issue #5: https://github.com/Ridgeline-Tech/ZBoard/issues/5.
- Recorded Zac's exact direct-connection decision in `decisions.md`.
- Loaded Engineering Playbook mirror revision `2026-11-13` (`417cdf38fe8a8cc438dfc5c199273b359f7ee1b4b0971bc7f6e58d96d1233b34`), `zacs-dev` (`787fc5919ee74eb2b1b3ef28b95e8c8a16e26041cf4b9241a986434d7441dd0c`), and `zacs-verification-and-handoff` (`72caab127f623b60cc24464d03a17dd286fe9d80bed7e4d66b14268313a011d7`).
- Started from merged main commit `4b8dc9e4d6ba9c17bcfe3a24825522fe21fcf8c6` and tree `2a216ef23063d2f22dc3f6cc3856cd637363f49c` on branch `codex/deepgram-native-stt`.
- Added a fixed-host HTTPS Deepgram adapter, nested transcript parsing, bounded provider errors, and host-based routing that leaves the existing generic cloud client intact.
- Added focused source-level request, response, host-routing, and vocabulary tests without sending audio or making provider calls.
- `:app:testDebugUnitTest` passed in 20m52s; all seven Deepgram contract cases and the existing unit suites passed.
- `:app:compileDebugKotlin :app:compileReleaseKotlin :app:assembleRelease` passed in 5m53s, including lint-vital and R8 release packaging.
- `.agents/scripts/pm/validate.sh` passed with 0 errors and 0 warnings through Git-for-Windows Bash; `git diff --check` passed.

## In Progress
- Commit the exact verified candidate and obtain independent review.

## Blockers
- None.

## Next Actions
- Commit the exact candidate, obtain independent review, then run the existing pull-request and CI gates without changing catalog, signer, or device state.
