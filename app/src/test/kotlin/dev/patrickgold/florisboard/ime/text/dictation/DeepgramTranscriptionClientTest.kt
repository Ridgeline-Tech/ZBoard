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
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class DeepgramTranscriptionClientTest : FunSpec({
    val recording = AudioRecording(
        bytes = byteArrayOf(1, 2, 3, 4),
        sampleRateHz = 16_000,
        channelCount = 1,
        durationMs = 750L,
        mimeType = "audio/mp4",
        fileName = "recording.m4a",
    )

    test("provider matching recognizes only the real Deepgram host") {
        DeepgramTranscriptionClient.ownsEndpoint("https://api.deepgram.com/v1/listen") shouldBe true
        // A saved insecure scheme is still recognized, but the native client always uses its fixed HTTPS URL.
        DeepgramTranscriptionClient.ownsEndpoint("http://API.DEEPGRAM.COM/wrong-path") shouldBe true
        listOf(
            "https://api.deepgram.com.evil.example/v1/listen",
            "https://api.deepgram.com@evil.example/v1/listen",
            "https://evil.example/#api.deepgram.com",
            "ftp://api.deepgram.com/v1/listen",
            "api.deepgram.com/v1/listen",
        ).forEach { DeepgramTranscriptionClient.ownsEndpoint(it) shouldBe false }
    }

    test("factory selects the native client without changing compatible endpoints") {
        CloudTranscriptionClientFactory.create(
            apiKey = "test-key",
            endpointUrl = "https://api.deepgram.com/v1/listen",
            model = "nova-3",
            languageHint = "en",
            vocabulary = emptyList(),
            vocabularyField = CloudVocabularyField.NONE,
        ).shouldBeInstanceOf<DeepgramTranscriptionClient>()

        CloudTranscriptionClientFactory.create(
            apiKey = "test-key",
            endpointUrl = "https://api.mistral.ai/v1/audio/transcriptions",
            model = "voxtral-mini-latest",
            languageHint = null,
            vocabulary = emptyList(),
            vocabularyField = CloudVocabularyField.NONE,
        ).shouldBeInstanceOf<VoxtralRelayTranscriptionClient>()
    }

    test("request uses fixed HTTPS destination, Token auth, raw audio metadata and encoded query values") {
        val client = DeepgramTranscriptionClient(
            apiKeyProvider = { " test-key " },
            modelProvider = { " " },
            languageHintProvider = { " en-US " },
            vocabularyProvider = { listOf("Ownkey", " Ridgeline Tech ", "") },
            sendVocabularyProvider = { true },
        )

        val request = client.prepareRequest(recording).getOrThrow()
        request.authorizationHeader shouldBe "Token test-key"
        request.contentType shouldBe "audio/mp4"
        request.body.contentEquals(recording.bytes) shouldBe true
        request.keyterms.shouldContainExactly("Ownkey", "Ridgeline Tech")
        request.requestUrl shouldBe
            "https://api.deepgram.com/v1/listen?model=nova-3&smart_format=true&language=en-US&keyterm=Ownkey&keyterm=Ridgeline+Tech&redact_usage=true"
    }

    test("keyterms are omitted for models that do not support Nova-3 prompting") {
        val request = DeepgramTranscriptionClient(
            apiKeyProvider = { "test-key" },
            modelProvider = { "whisper-large" },
            vocabularyProvider = { listOf("Ownkey") },
            sendVocabularyProvider = { true },
        ).prepareRequest(recording).getOrThrow()

        request.keyterms shouldBe emptyList()
        request.requestUrl shouldBe
            "https://api.deepgram.com/v1/listen?model=whisper-large&smart_format=true"
    }

    test("nested Deepgram transcript is returned") {
        val response = """
            {
              "results": {
                "channels": [
                  { "alternatives": [ { "transcript": "  Native Deepgram works.  " } ] }
                ]
              }
            }
        """.trimIndent()

        DeepgramTranscriptionClient(apiKeyProvider = { "test-key" })
            .extractTranscript(response).getOrThrow() shouldBe "Native Deepgram works."
    }

    test("malformed and empty responses fail with safe bounded messages") {
        val client = DeepgramTranscriptionClient(apiKeyProvider = { "test-key" })
        client.extractTranscript("{\"provider_detail\":\"do not expose me\"}")
            .exceptionOrNull()?.message shouldBe "Deepgram returned an invalid response."
        client.extractTranscript("{\"results\":{\"channels\":[{\"alternatives\":[{\"transcript\":\" \"}]}]}}")
            .exceptionOrNull()?.message shouldBe "Deepgram returned an empty transcript."
        client.extractTranscript("not-json")
            .exceptionOrNull()?.message shouldBe "Deepgram returned an invalid response."
    }

    test("missing key and empty recording fail before any connection") {
        DeepgramTranscriptionClient(apiKeyProvider = { "" }).prepareRequest(recording)
            .exceptionOrNull()?.message shouldBe "No Deepgram API key configured."
        DeepgramTranscriptionClient(apiKeyProvider = { "test-key" }).prepareRequest(recording.copy(bytes = byteArrayOf()))
            .exceptionOrNull()?.message shouldBe "No audio was captured."
    }
})
