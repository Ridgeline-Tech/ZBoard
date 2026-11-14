/*
 * Copyright (C) 2026 The FlorisBoard Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.ime.text.dictation

import dev.patrickgold.florisboard.ime.text.dictation.dictionary.CloudVocabularyField
import dev.patrickgold.florisboard.ime.text.dictation.dictionary.CloudVocabularyHints
import dev.patrickgold.florisboard.ime.text.network.withCancellableHttpConnection
import dev.patrickgold.florisboard.lib.util.OwnkeyBatteryTraceLabels
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder

/** Direct pre-recorded-audio client for Deepgram's native REST contract. */
class DeepgramTranscriptionClient(
    private val apiKeyProvider: () -> String,
    private val modelProvider: () -> String = { DefaultModel },
    private val languageHintProvider: () -> String? = { null },
    private val vocabularyProvider: () -> List<String> = { emptyList() },
    private val sendVocabularyProvider: () -> Boolean = { false },
    private val connectTimeoutMs: Int = 30_000,
    private val readTimeoutMs: Int = 60_000,
    private val maxRetryAttempts: Int = 2,
    private val retryDelayMs: Long = 700,
) : TranscriptionClient {
    companion object {
        const val EndpointUrl = "https://api.deepgram.com/v1/listen"
        const val ProviderHost = "api.deepgram.com"
        const val DefaultModel = "nova-3"
        private const val MaxKeyterms = 100

        fun ownsEndpoint(endpointUrl: String): Boolean {
            val uri = runCatching { URI(endpointUrl.trim()) }.getOrNull() ?: return false
            if (uri.scheme?.equals("http", ignoreCase = true) != true &&
                uri.scheme?.equals("https", ignoreCase = true) != true
            ) return false
            return uri.host?.equals(ProviderHost, ignoreCase = true) == true
        }
    }

    internal data class PreparedRequest(
        val requestUrl: String,
        val authorizationHeader: String,
        val contentType: String,
        val body: ByteArray,
        val keyterms: List<String>,
    )

    override suspend fun transcribe(recording: AudioRecording): Result<String> {
        val request = prepareRequest(recording).getOrElse { return Result.failure(it) }
        var attempt = 0
        var lastError: Throwable? = null

        while (attempt < maxRetryAttempts) {
            attempt += 1
            val result = try {
                Result.success(executeRequest(request))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                Result.failure(error)
            }
            if (result.isSuccess) return result

            val error = result.exceptionOrNull() ?: DeepgramProtocolException("Deepgram transcription failed.")
            lastError = error
            if (!shouldRetry(error) || attempt >= maxRetryAttempts) return Result.failure(error)
            delay(retryDelayMs * attempt)
        }

        return Result.failure(lastError ?: DeepgramProtocolException("Deepgram transcription failed."))
    }

    internal fun prepareRequest(recording: AudioRecording): Result<PreparedRequest> = runCatching {
        val apiKey = apiKeyProvider().trim()
        require(apiKey.isNotEmpty()) { "No Deepgram API key configured." }
        require(recording.bytes.isNotEmpty()) { "No audio was captured." }

        val model = modelProvider().trim().ifBlank { DefaultModel }
        val language = languageHintProvider()?.trim()?.takeIf { it.isNotEmpty() }
        val keyterms = if (sendVocabularyProvider() && model.startsWith("nova-3", ignoreCase = true)) {
            CloudVocabularyHints.bound(vocabularyProvider(), maxTerms = MaxKeyterms).terms
        } else {
            emptyList()
        }

        val query = buildList {
            add("model" to model)
            add("smart_format" to "true")
            language?.let { add("language" to it) }
            keyterms.forEach { add("keyterm" to it) }
            if (keyterms.isNotEmpty()) add("redact_usage" to "true")
        }.joinToString("&") { (name, value) -> "${name.encodeQuery()}=${value.encodeQuery()}" }

        PreparedRequest(
            requestUrl = "$EndpointUrl?$query",
            authorizationHeader = "Token $apiKey",
            contentType = recording.mimeType,
            body = recording.bytes,
            keyterms = keyterms,
        )
    }

    private suspend fun executeRequest(request: PreparedRequest): String = withCancellableHttpConnection(
        traceLabel = OwnkeyBatteryTraceLabels.TranscriptionRequest,
        openConnection = {
            (URL(request.requestUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doInput = true
                doOutput = true
                instanceFollowRedirects = false
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                setRequestProperty("Authorization", request.authorizationHeader)
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", request.contentType)
            }
        },
    ) { connection ->
        connection.outputStream.use { it.write(request.body) }
        val statusCode = connection.responseCode
        if (statusCode !in 200..299) {
            runCatching { connection.errorStream?.close() }
            throw DeepgramHttpException(statusCode)
        }

        val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
        extractTranscript(responseBody).getOrThrow()
    }

    internal fun extractTranscript(responseBody: String): Result<String> = runCatching {
        val root = Json.parseToJsonElement(responseBody).jsonObject
        val results = root["results"] as? JsonObject
            ?: throw DeepgramProtocolException("Deepgram returned an invalid response.")
        val channels = results["channels"] as? JsonArray
            ?: throw DeepgramProtocolException("Deepgram returned an invalid response.")
        val channel = channels.firstOrNull() as? JsonObject
            ?: throw DeepgramProtocolException("Deepgram returned no transcript.")
        val alternatives = channel["alternatives"] as? JsonArray
            ?: throw DeepgramProtocolException("Deepgram returned an invalid response.")
        val alternative = alternatives.firstOrNull() as? JsonObject
            ?: throw DeepgramProtocolException("Deepgram returned no transcript.")
        val transcript = (alternative["transcript"] as? JsonPrimitive)?.contentOrNull.orEmpty().trim()
        transcript.takeIf { it.isNotEmpty() }
            ?: throw DeepgramProtocolException("Deepgram returned an empty transcript.")
    }.recoverCatching { error ->
        if (error is DeepgramProtocolException) throw error
        throw DeepgramProtocolException("Deepgram returned an invalid response.")
    }

    private fun shouldRetry(error: Throwable): Boolean = when (error) {
        is java.io.IOException -> true
        is DeepgramHttpException -> error.statusCode == 408 || error.statusCode == 429 || error.statusCode in 500..599
        else -> false
    }

    private class DeepgramHttpException(val statusCode: Int) :
        IllegalStateException("Deepgram request failed (HTTP $statusCode).")

    private class DeepgramProtocolException(message: String) : IllegalStateException(message)
}

internal object CloudTranscriptionClientFactory {
    fun create(
        apiKey: String,
        endpointUrl: String,
        model: String,
        languageHint: String?,
        vocabulary: List<String>,
        vocabularyField: CloudVocabularyField,
    ): TranscriptionClient {
        val resolvedEndpoint = endpointUrl.trim().ifBlank { VoxtralRelayTranscriptionClient.DefaultEndpointUrl }
        return if (DeepgramTranscriptionClient.ownsEndpoint(resolvedEndpoint)) {
            DeepgramTranscriptionClient(
                apiKeyProvider = { apiKey },
                modelProvider = { model },
                languageHintProvider = { languageHint },
                vocabularyProvider = { vocabulary },
                sendVocabularyProvider = { vocabularyField != CloudVocabularyField.NONE },
            )
        } else {
            VoxtralRelayTranscriptionClient(
                apiKeyProvider = { apiKey },
                endpointUrlProvider = { endpointUrl },
                modelProvider = { model },
                languageHintProvider = { languageHint },
                vocabularyProvider = { vocabulary },
                vocabularyFieldProvider = { vocabularyField },
            )
        }
    }
}

private fun String.encodeQuery(): String = URLEncoder.encode(this, Charsets.UTF_8.name())
