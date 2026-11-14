# Release-boundary handoff

## Existing authorized signing route inspected

ZBoard's checked-in release path is a manual production-signing route:

1. An authorized release machine supplies repository-root `keystore.properties` with `storeFile`, `storePassword`, `keyAlias`, and `keyPassword`.
2. `app/build.gradle.kts` uses that signing config for the `release` build only when `storeFile` is present; otherwise it deliberately falls back to the Android debug key.
3. `.github/workflows/android.yml` can build and publish assets for `v*` tags, but it contains no production-keystore injection step. Repository guidance explicitly says production-signed `android-v*` releases are a separate manual publishing process and CI release output may be debug-signed.

The task worktree had no authorized `keystore.properties`; the assembled release APK therefore used debug certificate SHA-256 `3b14828b80629cb87161a896b0482c0623d8d0130ca34b917e5592213af85977` rather than the established compatible signer `8e66a1d248902ff133925e9c43f087d004d6db1439ab1bfbe81428fcc092d33f`.

## Concrete blocker

No existing compatible production-signing material or signing service is wired into the Ridgeline-Tech fork's repository/Actions route. The current key custodian and authorized release machine are not identified in the inspected source or workflow. Triggering workflow dispatch or a `v*` tag would therefore produce an artifact that may be debug-signed and cannot be represented as an update for the installed app.

## Smallest recommendation

Recommended owner action: Zac through Rivet identifies the existing custodian of the `8e66a1...d33f` signing key and asks that custodian to run the existing manual release process for merge `baa3eed4f97564bd21888d6d291e453dc5ac029d`. The key does not need to be copied or exposed to this workspace. Return only the signed arm64 APK and release receipt for independent verification of package, version code 127, signer, SHA-256, and release asset identity before updating the catalog.

If that custodian is unavailable, the safe alternative is to wait for the upstream Ownkey maintainer to publish a signed release containing the merged change and mirror that APK byte-for-byte. Creating a new key, using the debug signer, changing package identity, uninstalling, or wiping data would not provide an update-compatible path and is not recommended.

## Guidance receipt

- Engineering Playbook: revision `2026-11-13`, SHA-256 `55D0475BA926642170845222B7077579BAF3210BE998BC3F419786E56D884A0C` (retained from task start).
- Current `zacs-dev` loaded at this handoff: SHA-256 `787FC5919EE74EB2B1B3EF28B95E8C8A16E26041CF4B9241A986434D7441DD0C`.
- Current `maintain-agent-workboard` loaded at this handoff: SHA-256 `3560ABC6C40514C49B0B8694EA658822AC330190A181693795FABADA159C5480`.
- `zacs-dev`: **Reviewed unchanged**. Its existing separation of source, qualified artifacts, publication, installation, and user outcome already covers the signer-continuity lesson; no skill edit is warranted.
- `maintain-agent-workboard`: **Reviewed unchanged**. It confirms Rivet retains board writes and that user-facing delivery remains In Progress until the install-compatible release/live outcome is verified. No board write was made by the Project Lead.
