package io.github.haasfynn.navy.effects

import com.intellij.openapi.diagnostic.Logger
import javax.swing.Timer

/** A short animation. [step] advances it and returns false once it is over; [finish] cleans up. */
internal interface Effect {
    fun step(seconds: Double): Boolean
    fun finish()
}

/**
 * Plays effects at 60 frames per second. The timer only runs while an effect is on screen,
 * so nothing ticks in between.
 */
internal object Effects {
    private const val FRAME_MILLIS = 16
    private val running = ArrayList<Effect>()
    private var lastFrame = 0L
    private val timer = Timer(FRAME_MILLIS) { frame() }

    fun play(effect: Effect) {
        running += effect
        if (!timer.isRunning) {
            lastFrame = System.nanoTime()
            timer.start()
        }
    }

    fun stopAll() {
        timer.stop()
        running.forEach(Effect::finish)
        running.clear()
    }

    private fun frame() {
        val now = System.nanoTime()
        // Long gaps (a busy IDE, a sleeping laptop) must not make things jump.
        val seconds = ((now - lastFrame) / 1e9).coerceAtMost(0.05)
        lastFrame = now
        val iterator = running.iterator()
        while (iterator.hasNext()) {
            val effect = iterator.next()
            val alive = try {
                effect.step(seconds)
            } catch (e: RuntimeException) {
                // A broken effect must never disturb the editor; drop it and say why.
                Logger.getInstance(Effects::class.java).warn("Effect failed and was removed", e)
                false
            }
            if (!alive) {
                iterator.remove()
                effect.finish()
            }
        }
        if (running.isEmpty()) timer.stop()
    }
}

/** For callers outside this package and outside Kotlin. */
fun stopAllEffects() = Effects.stopAll()
