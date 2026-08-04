package com.mikmy.emberdepth.render

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.mikmy.emberdepth.BuildConfig
import com.mikmy.emberdepth.core.engine.BattleEngine
import com.mikmy.emberdepth.core.model.BigNum

@SuppressLint("ViewConstructor")
class BattleView(
    context: Context,
    private val engine: BattleEngine,
    private val renderer: BattleRenderer,
    private val goldProvider: () -> BigNum,
    private val emberProvider: () -> BigNum,
    private val onEvents: (List<BattleEngine.BattleEvent>) -> Unit
) : SurfaceView(context), SurfaceHolder.Callback {

    private val FRAME_NS = 16_666_667L
    val lock = Any()

    @Volatile private var running = false
    @Volatile private var paused = false
    private var thread: Thread? = null

    init {
        holder.addCallback(this)
        isFocusable = true
        keepScreenOn = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        startThread()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        synchronized(lock) { renderer.resize(width, height) }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopThread()
    }

    fun onPause() { paused = true }
    fun onResume() { paused = false }

    private fun startThread() {
        if (running) return
        running = true
        thread = Thread {
            var last = System.nanoTime()
            var frames = 0
            var fpsWindow = 0f
            while (running) {
                if (paused) {
                    try { Thread.sleep(60) } catch (e: InterruptedException) { break }
                    last = System.nanoTime()
                    continue
                }
                val now = System.nanoTime()
                val dt = ((now - last) / 1_000_000_000.0).toFloat().coerceIn(0f, 0.05f)
                last = now

                val canvas = try {
                    holder.lockHardwareCanvas()
                } catch (e: Throwable) {
                    try { holder.lockCanvas() } catch (e2: Throwable) { null }
                }
                if (canvas == null) {
                    try { Thread.sleep(8) } catch (e: InterruptedException) { break }
                    continue
                }
                try {
                    synchronized(lock) {
                        engine.update(dt)
                        val events = engine.consumeEvents()
                        if (events.isNotEmpty()) onEvents(events)
                        renderer.update(dt)
                        renderer.draw(
                            canvas,
                            engine.heroes,
                            engine.enemies,
                            engine.enemyIndex,
                            engine.currentFloor,
                            goldProvider(),
                            emberProvider()
                        )
                    }
                } finally {
                    try { holder.unlockCanvasAndPost(canvas) } catch (_: Throwable) {}
                }

                val frameNs = System.nanoTime() - (now)
                if (frameNs < FRAME_NS) {
                    val sleepMs = (FRAME_NS - frameNs) / 1_000_000L
                    if (sleepMs > 0) {
                        try { Thread.sleep(sleepMs) } catch (e: InterruptedException) { break }
                    }
                }

                if (BuildConfig.DEBUG) {
                    frames++
                    fpsWindow += dt
                    if (fpsWindow >= 3f) {
                        Log.d("EmberDepth", "fps=%.1f".format(frames / fpsWindow))
                        frames = 0; fpsWindow = 0f
                    }
                }
            }
        }.also { it.name = "emberdepth-render"; it.start() }
    }

    private fun stopThread() {
        running = false
        thread?.let {
            it.interrupt()
            try { it.join(800) } catch (_: InterruptedException) { Thread.currentThread().interrupt() }
        }
        thread = null
    }
}
