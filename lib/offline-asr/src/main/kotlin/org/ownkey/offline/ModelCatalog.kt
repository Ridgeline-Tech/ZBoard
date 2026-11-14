package org.ownkey.offline

/** Trust roots ship with the APK, never with an untrusted remote manifest. */
data class ModelFile(val name: String, val bytes: Long, val sha256: String) {
    init {
        require(name.matches(Regex("[A-Za-z0-9][A-Za-z0-9_.-]*")) && name != "." && name != "..")
        require(!name.endsWith(".part") && !name.endsWith(".etag"))
        require(bytes > 0 && sha256.matches(Regex("[0-9a-f]{64}")))
    }
}

data class HotwordProfile(
    val modelingUnit: String,
    val bpeVocab: String? = null,
    val score: Float,
) {
    init {
        require(modelingUnit in setOf("bpe", "cjkchar"))
        require(bpeVocab == null || bpeVocab.matches(Regex("[A-Za-z0-9][A-Za-z0-9_.-]*")))
        require(score > 0f)
    }
}

data class ModelRelease(
    val id: String,
    val baseUrl: String,
    val files: List<ModelFile>,
    val displayName: String = id,
    val description: String = "",
    val hotwords: HotwordProfile? = null,
) {
    val bytes: Long get() = files.sumOf { it.bytes }
    init {
        require(id.matches(Regex("[a-z0-9][a-z0-9.-]*")) && id != "..")
        require(baseUrl.startsWith("https://") && !baseUrl.contains('?') && !baseUrl.contains('#'))
        require(files.isNotEmpty() && files.map { it.name }.distinct().size == files.size)
        require(displayName.isNotBlank())
        require(hotwords?.bpeVocab == null || files.any { it.name == hotwords.bpeVocab })
    }
}

object ModelCatalog {
    const val RESERVE_BYTES = 256L * 1024 * 1024
    const val RECORDING_CAP_MS = 30_000L
    // Provisional internal-build policy; select a production retention tier after physical profiling.
    const val IDLE_RETENTION_MS = 2 * 60_000L
    val orukeet = ModelRelease(
        id = "orukeet-v0.1.0-int8",
        baseUrl = "https://github.com/MajesteitBart/ownkey-keyboard/releases/download/orukeet-v0.1.0-int8",
        files = listOf(
            ModelFile("encoder.int8.onnx", 653182378L, "7b55f2a504a20a8e462899f5befd45f4a1784948d76ed0127902d9cf39405487"),
            ModelFile("decoder.int8.onnx", 11845332L, "c185c2afb4c77c94bb1314807ecb3dc1623057a3dc540b83e10301af9bf4cfca"),
            ModelFile("joiner.int8.onnx", 6355335L, "1a7e90abf7172d926dd7e2edac2a5d5035c24dfb641a15e131d57b6a5f63cdd3"),
            ModelFile("tokens.txt", 93939L, "d58544679ea4bc6ac563d1f545eb7d474bd6cfa467f0a6e2c1dc1c7d37e3c35d"),
            ModelFile("bpe.vocab", 117408L, "41d5e71b3591642eff088151efd7acd4e750124cc0054c8ba9fa3245187a4804"),
            ModelFile("LICENSE-WEIGHTS", 20137L, "23ee78c8bae49cf08ea2f0c84945c66b987ebe4520881fb51b3dad4fb43d07c2"),
            ModelFile("NOTICE.md", 5271L, "440361d963edd9621e744f251332b47f2c4de2e2594ecfe42b215e3f6223fa44"),
        ),
        displayName = "Orukeet",
        description = "Multilingual on-device dictation",
        hotwords = HotwordProfile(modelingUnit = "bpe", bpeVocab = "bpe.vocab", score = 1.5f),
    )
    val parakeet = ModelRelease(
        id = "parakeet-0.6b-v3-int8",
        baseUrl = "https://github.com/Ridgeline-Tech/ZBoard/releases/download/parakeet-0.6b-v3-int8",
        files = listOf(
            ModelFile("encoder.int8.onnx", 652282294L, "1afbe69c1ffdabee94755f42e4daf1d93549131a3da11356bda84fcef0f2c479"),
            ModelFile("decoder.int8.onnx", 11845274L, "0222a86147299d4dee90edf95b6d13827edb42ad8b7c9cf0b2582f030937c0b5"),
            ModelFile("joiner.int8.onnx", 6355277L, "cbd02f3696e9944970111fb439d9c3fd365a80374565f13f2b9c7007d253fc98"),
            ModelFile("tokens.txt", 93939L, "d58544679ea4bc6ac563d1f545eb7d474bd6cfa467f0a6e2c1dc1c7d37e3c35d"),
            ModelFile("README.md", 4905L, "701a3375e3fe77308d280c673d34146162d2f1602b44f97d99086e3f537a1119"),
        ),
        displayName = "Parakeet 0.6B",
        description = "25-language on-device dictation",
        hotwords = HotwordProfile(modelingUnit = "cjkchar", score = 2.0f),
    )
    /** Existing installations remain on Orukeet until the user explicitly chooses another model. */
    val current = orukeet
    val recommended = parakeet
    // Keep trusted entries immutable so interrupted downloads and existing selections remain recoverable.
    val trusted = listOf(orukeet, parakeet)
}

enum class LocalAsrFailure {
    MODEL_MISSING, MODEL_DAMAGED, INSUFFICIENT_STORAGE, DOWNLOAD, INTEGRITY,
    UNSUPPORTED, BUSY, AUDIO, EMPTY, PROCESS_DIED, TIMEOUT, RUNTIME, CANCELLED,
}
/**
 * [detail] is a short technical note for logs and internal builds, for example the exception class of a
 * native load failure or how long the inference process lived. It never carries audio or transcripts.
 */
class LocalAsrException(val reason: LocalAsrFailure, val detail: String? = null) : Exception(reason.name)
