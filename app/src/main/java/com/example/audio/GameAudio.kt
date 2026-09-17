package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.PI
import kotlin.math.sin

class GameAudio(private val context: Context) {

    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    private val sampleRate = 44100
    private var flapTrack: AudioTrack? = null
    private var scoreTrack: AudioTrack? = null
    private var hitTrack: AudioTrack? = null
    private var dieTrack: AudioTrack? = null
    private var buttonTrack: AudioTrack? = null

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    init {
        try {
            initTracks()
        } catch (_: Exception) {
            // AudioTrack fallback
        }
    }

    private fun initTracks() {
        val flapPcm = generateChirp(startFreq = 420.0, endFreq = 780.0, durationMs = 80, volume = 0.8)
        flapTrack = createTrack(flapPcm)

        val scorePcm = generateTwoTone(freq1 = 988.0, dur1Ms = 60, freq2 = 1319.0, dur2Ms = 140, volume = 0.9)
        scoreTrack = createTrack(scorePcm)

        val hitPcm = generateCrunch(startFreq = 260.0, endFreq = 70.0, durationMs = 90, volume = 0.9)
        hitTrack = createTrack(hitPcm)

        val diePcm = generateChirp(startFreq = 580.0, endFreq = 160.0, durationMs = 200, volume = 0.85)
        dieTrack = createTrack(diePcm)

        val btnPcm = generateChirp(startFreq = 600.0, endFreq = 900.0, durationMs = 40, volume = 0.6)
        buttonTrack = createTrack(btnPcm)
    }

    private fun createTrack(pcm: ShortArray): AudioTrack {
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufferSize, pcm.size * 2)

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()

        val track = AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(format)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(pcm, 0, pcm.size)
        return track
    }

    private fun playTrack(track: AudioTrack?) {
        if (!isSoundEnabled || track == null) return
        try {
            track.stop()
            track.reloadStaticData()
            track.play()
        } catch (_: Exception) {
            // Ignore transient audio device state exceptions
        }
    }

    fun playFlap() {
        playTrack(flapTrack)
        vibrate(15)
    }

    fun playScore() {
        playTrack(scoreTrack)
        vibrate(30)
    }

    fun playHit() {
        playTrack(hitTrack)
        vibrate(60)
    }

    fun playDie() {
        playTrack(dieTrack)
        vibrate(100)
    }

    fun playButton() {
        playTrack(buttonTrack)
        vibrate(10)
    }

    private fun vibrate(milliseconds: Long) {
        if (!isHapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        milliseconds,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(milliseconds)
            }
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    private fun generateChirp(startFreq: Double, endFreq: Double, durationMs: Int, volume: Double): ShortArray {
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / totalSamples
            val freq = startFreq + (endFreq - startFreq) * t
            phase += 2.0 * PI * freq / sampleRate
            val envelope = (1.0 - t) * volume
            val sample = (sin(phase) * envelope * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateTwoTone(freq1: Double, dur1Ms: Int, freq2: Double, dur2Ms: Int, volume: Double): ShortArray {
        val samples1 = (sampleRate * (dur1Ms / 1000.0)).toInt()
        val samples2 = (sampleRate * (dur2Ms / 1000.0)).toInt()
        val totalSamples = samples1 + samples2
        val buffer = ShortArray(totalSamples)

        var phase = 0.0
        for (i in 0 until samples1) {
            phase += 2.0 * PI * freq1 / sampleRate
            val env = (1.0 - (i.toDouble() / samples1) * 0.3) * volume
            val sample = (sin(phase) * env * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        phase = 0.0
        for (i in 0 until samples2) {
            phase += 2.0 * PI * freq2 / sampleRate
            val t = i.toDouble() / samples2
            val env = (1.0 - t) * volume
            val sample = (sin(phase) * env * Short.MAX_VALUE).toInt()
            buffer[samples1 + i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateCrunch(startFreq: Double, endFreq: Double, durationMs: Int, volume: Double): ShortArray {
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / totalSamples
            val freq = startFreq + (endFreq - startFreq) * t
            phase += 2.0 * PI * freq / sampleRate
            // Combine fundamental square-like wave with a pinch of white noise for crunch
            val sineVal = sin(phase)
            val squareVal = if (sineVal >= 0) 0.7 else -0.7
            val noise = (Math.random() * 2.0 - 1.0) * 0.25
            val mixed = (squareVal + noise) * (1.0 - t) * volume
            val sample = (mixed * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    fun release() {
        try {
            flapTrack?.release()
            scoreTrack?.release()
            hitTrack?.release()
            dieTrack?.release()
            buttonTrack?.release()
        } catch (_: Exception) {}
    }
}
