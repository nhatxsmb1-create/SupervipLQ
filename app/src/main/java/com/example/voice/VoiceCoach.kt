package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class VoiceCoach(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    private var isMuted = false

    private val lastCalloutTimestamps = ConcurrentHashMap<String, Long>()
    private val calloutCooldownMs = 8000L // Minimum 8 seconds between identical callouts

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val vietnameseLocale = Locale.forLanguageTag("vi-VN")
            val result = tts?.setLanguage(vietnameseLocale)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("VoiceCoach", "Vietnamese TTS not fully supported on device, falling back to default locale")
                tts?.language = Locale.getDefault()
            }
            tts?.setPitch(1.0f)
            tts?.setSpeechRate(1.05f) // Slightly faster for quick tactical alerts
            isInitialized = true
        } else {
            Log.e("VoiceCoach", "TextToSpeech initialization failed with status: $status")
        }
    }

    fun speakCallout(
        callout: String,
        tag: String,
        priority: Int = 1,
        forceIgnoreCooldown: Boolean = false
    ) {
        if (isMuted || !isInitialized || callout.isBlank()) return

        val now = System.currentTimeMillis()
        val lastTime = lastCalloutTimestamps[tag] ?: 0L

        if (!forceIgnoreCooldown && (now - lastTime < calloutCooldownMs)) {
            // Cooldown active, skip to prevent spamming
            return
        }

        lastCalloutTimestamps[tag] = now

        val queueMode = if (priority >= 3) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(callout, queueMode, null, tag)
    }

    fun setMuted(muted: Boolean) {
        this.isMuted = muted
        if (muted) {
            tts?.stop()
        }
    }

    fun setVoiceParams(pitch: Float, rate: Float) {
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
