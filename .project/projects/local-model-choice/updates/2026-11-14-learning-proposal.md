---
status: proposed
owner: repository-maintainers
scope: test-logging-tooling
---

# Learning proposal: make test event logging module-safe

Observed evidence: `.agents/scripts/test-and-log.sh` completed the Gradle command and wrote `.agents/logs/tests/20261114T094824Z.log` plus `.agents/logs/test-runs.jsonl`, then Node treated `.agents/scripts/log-event.js` as ESM because the parent workspace `package.json` declares `type: module`; the helper uses CommonJS `require` and failed post-run.

Proposed smallest improvement: make the helper unambiguously CommonJS (for example `.cjs`) or convert it to ESM, then add a fixture that runs the wrapper from a parent ESM workspace and asserts both the command exit and event receipt.

Priority: medium. This does not change the successful test result and is not adopted in this feature PR. It requires separate maintainer review before implementation.
