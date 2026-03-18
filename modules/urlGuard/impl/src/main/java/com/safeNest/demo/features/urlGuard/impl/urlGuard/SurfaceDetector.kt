package com.safeNest.demo.features.urlGuard.impl.urlGuard

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Single source of truth for the current [ScreenSurface].
 */
object SurfaceDetector {

    private val _surface = MutableStateFlow<ScreenSurface>(ScreenSurface.Idle)

    val surface: StateFlow<ScreenSurface> = _surface.asStateFlow()

    fun getCurrent(): ScreenSurface = _surface.value

    // ── Listener API (Java-friendly) ──────────────────────────────────────────

    /** SAM interface for Java lambda compatibility. */
    fun interface OnSurfaceChangedListener {
        fun onChanged(surface: ScreenSurface)
    }

    private val listeners = CopyOnWriteArrayList<OnSurfaceChangedListener>()

    fun addListener(listener: OnSurfaceChangedListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: OnSurfaceChangedListener) {
        listeners.remove(listener)
    }

    // ── Internal update — called only by UrlGuardAccessibilityService ─────────

    /**
     * Push a new surface state. No-op if the value is identical to the current one
     * (prevents redundant recompositions / callbacks on repeated events).
     */
    fun update(newSurface: ScreenSurface) {
        if (_surface.value == newSurface) return
        _surface.value = newSurface
        listeners.forEach { it.onChanged(newSurface) }
    }
}
