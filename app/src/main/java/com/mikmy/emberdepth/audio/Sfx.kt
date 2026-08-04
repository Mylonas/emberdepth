package com.mikmy.emberdepth.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class Sfx {

    companion object {
        private const val SR = 22050
        private const val CHUNK = 512
    }

    private class Voice(val data: ShortArray, var pos: Int, val gain: Float)

    private val bank = HashMap<String, ShortArray>()
    private val voices = ArrayList<Voice>(24)
    private val lock = Any()
    private var ambientData: ShortArray? = null
    private var ambientPos = 0

    @Volatile private var running = false
    @Volatile var muted = false
    private var track: AudioTrack? = null
    private var thread: Thread? = null

    fun start() {
        if (running) return
        buildBank()
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                SR, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            val bufSize = maxOf(minBuf, CHUNK * 4)
            val t = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SR)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            t.play()
            track = t
            running = true
            thread = Thread { mixLoop(t) }.also { it.isDaemon = true; it.start() }
        } catch (_: Throwable) {
            running = false
            track = null
        }
    }

    fun stop() {
        running = false
        thread?.let { runCatching { it.join(300) } }
        thread = null
        track?.let { runCatching { it.pause(); it.flush(); it.release() } }
        track = null
        synchronized(lock) { voices.clear() }
    }

    fun play(name: String, gain: Float = 1f) {
        if (!running || muted) return
        val data = bank[name] ?: return
        synchronized(lock) {
            if (voices.size > 20) voices.removeAt(0)
            voices.add(Voice(data, 0, gain))
        }
    }

    private fun mixLoop(t: AudioTrack) {
        val mix = FloatArray(CHUNK)
        val out = ShortArray(CHUNK)
        while (running) {
            java.util.Arrays.fill(mix, 0f)
            val amb = ambientData
            if (amb != null && !muted) {
                for (k in 0 until CHUNK) {
                    mix[k] += amb[ambientPos].toFloat()
                    ambientPos++
                    if (ambientPos >= amb.size) ambientPos = 0
                }
            }
            synchronized(lock) {
                var i = 0
                while (i < voices.size) {
                    val v = voices[i]
                    val d = v.data
                    var p = v.pos
                    var k = 0
                    while (k < CHUNK && p < d.size) {
                        mix[k] += d[p].toFloat() * v.gain
                        k++; p++
                    }
                    v.pos = p
                    if (p >= d.size) voices.removeAt(i) else i++
                }
            }
            for (k in 0 until CHUNK) {
                val s = mix[k]
                out[k] = when {
                    s > 32000f -> 32000
                    s < -32000f -> -32000
                    else -> s.toInt().toShort()
                }
            }
            try { t.write(out, 0, CHUNK) } catch (_: Throwable) { running = false }
        }
    }

    // ---------------------------------------------------------------- synth

    private val W_SINE = 0
    private val W_SQUARE = 1
    private val W_SAW = 2
    private val W_NOISE = 3

    private fun buildBank() {
        if (bank.isNotEmpty()) return

        // hero attack — quick metallic hit
        bank["hit"] = mix(
            tone(0.06f, 800f, 400f, 0.18f, W_SINE, 28f),
            tone(0.03f, 2000f, 1200f, 0.06f, W_NOISE, 40f)
        )

        // enemy attack — thud
        bank["thud"] = mix(
            tone(0.08f, 120f, 60f, 0.22f, W_SINE, 18f),
            tone(0.04f, 600f, 300f, 0.06f, W_NOISE, 30f)
        )

        // enemy killed — pop
        bank["kill"] = mix(
            tone(0.08f, 600f, 900f, 0.20f, W_SINE, 16f),
            tone(0.05f, 1200f, 1800f, 0.08f, W_SQUARE, 24f)
        )

        // boss killed — dramatic
        bank["boss_kill"] = mix(
            tone(0.12f, 300f, 600f, 0.22f, W_SINE, 10f),
            delay(tone(0.10f, 600f, 900f, 0.18f, W_SINE, 12f), 0.08f),
            delay(tone(0.35f, 900f, 1200f, 0.22f, W_SINE, 6f), 0.16f)
        )

        // floor cleared — ascending chime
        bank["floor"] = mix(
            tone(0.08f, 523f, 523f, 0.16f, W_SINE, 15f),
            delay(tone(0.08f, 659f, 659f, 0.16f, W_SINE, 15f), 0.06f),
            delay(tone(0.15f, 784f, 784f, 0.18f, W_SINE, 10f), 0.12f)
        )

        // gold earned — coin clink
        bank["gold"] = mix(
            tone(0.05f, 3000f, 2500f, 0.10f, W_SINE, 30f),
            tone(0.03f, 4500f, 3800f, 0.05f, W_SINE, 40f)
        )

        // level up
        bank["levelup"] = mix(
            tone(0.10f, 440f, 440f, 0.18f, W_SINE, 12f),
            delay(tone(0.10f, 659f, 659f, 0.18f, W_SINE, 12f), 0.08f),
            delay(tone(0.30f, 880f, 880f, 0.22f, W_SINE, 7f), 0.16f)
        )

        // forge — hammer strike + sizzle
        bank["forge"] = mix(
            tone(0.10f, 200f, 100f, 0.25f, W_SAW, 12f),
            tone(0.30f, 800f, 2000f, 0.08f, W_NOISE, 6f)
        )

        // rebirth — dramatic sweep
        bank["rebirth"] = mix(
            tone(0.80f, 80f, 600f, 0.28f, W_SAW, 3.5f),
            tone(0.50f, 200f, 1200f, 0.12f, W_NOISE, 5f),
            delay(tone(0.60f, 523f, 1046f, 0.20f, W_SINE, 4f), 0.40f)
        )

        // hero died
        bank["hero_died"] = mix(
            tone(0.40f, 300f, 80f, 0.24f, W_SAW, 5f),
            tone(0.20f, 150f, 50f, 0.14f, W_SINE, 8f)
        )

        // party wipe
        bank["wipe"] = mix(
            tone(0.70f, 250f, 40f, 0.30f, W_SAW, 3f),
            tone(0.70f, 130f, 30f, 0.20f, W_SINE, 3f)
        )

        // ui tap
        bank["ui_tap"] = tone(0.03f, 1800f, 1400f, 0.08f, W_SINE, 45f)

        buildAmbientLoop()
    }

    private fun buildAmbientLoop() {
        val dur = 4.0f
        val n = (dur * SR).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toFloat() / SR
            val lfo = (0.5f + 0.5f * sin(2.0 * PI * 0.5 * t)).toFloat()
            val s1 = sin(2.0 * PI * 80.0 * t).toFloat() * 0.04f * lfo
            val s2 = sin(2.0 * PI * 200.0 * t).toFloat() * 0.02f * lfo
            val s3 = sin(2.0 * PI * 120.0 * t).toFloat() * 0.02f * (1f - lfo)
            val env = if (i < SR / 4) i.toFloat() / (SR / 4) else if (i > n - SR / 4) (n - i).toFloat() / (SR / 4) else 1f
            out[i] = ((s1 + s2 + s3) * env * 32767f).toInt().coerceIn(-32767, 32767).toShort()
        }
        ambientData = out
    }

    private fun tone(dur: Float, f0: Float, f1: Float, vol: Float, wave: Int, decay: Float): ShortArray {
        val n = (dur * SR).toInt().coerceAtLeast(1)
        val out = ShortArray(n)
        var phase = 0.0
        val attack = (SR * 0.004f).toInt().coerceAtLeast(1)
        for (i in 0 until n) {
            val t = i.toFloat() / n
            val f = f0 + (f1 - f0) * t
            phase += 2.0 * PI * f / SR
            if (phase > 2 * PI) phase -= 2 * PI
            val s = when (wave) {
                W_SQUARE -> if (sin(phase) >= 0) 1f else -1f
                W_SAW -> (((phase / (2 * PI)) * 2.0) - 1.0).toFloat()
                W_NOISE -> Random.nextFloat() * 2f - 1f
                else -> sin(phase).toFloat()
            }
            var env = exp(-decay * t).toFloat()
            if (i < attack) env *= i.toFloat() / attack
            out[i] = (s * env * vol * 32767f).toInt().coerceIn(-32767, 32767).toShort()
        }
        return out
    }

    private fun delay(a: ShortArray, sec: Float): ShortArray {
        val pad = (sec * SR).toInt()
        val out = ShortArray(a.size + pad)
        System.arraycopy(a, 0, out, pad, a.size)
        return out
    }

    private fun mix(vararg parts: ShortArray): ShortArray {
        val n = parts.maxOf { it.size }
        val acc = FloatArray(n)
        for (p in parts) for (i in p.indices) acc[i] += p[i].toFloat()
        val out = ShortArray(n)
        for (i in 0 until n) out[i] = acc[i].coerceIn(-32767f, 32767f).toInt().toShort()
        return out
    }
}
