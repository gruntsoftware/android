package com.brainwallet.ui.composable.utils

import android.view.View
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AutoScrollController(
    private val scrollState: ScrollState,
    private val view: View,
    private val edgeSlopPx: Float,
    private val stepPx: Float,
    private val scope: CoroutineScope
) {
    private var job: Job? = null
    private var dir = 0

    fun update(event: DragAndDropEvent) {
        val y = event.toAndroidDragEvent().y
        val h = view.height.toFloat().coerceAtLeast(1f)
        val newDir = when {
            y < edgeSlopPx -> -1
            y > h - edgeSlopPx -> 1
            else -> 0
        }
        setDir(newDir)
    }

    fun stop() = setDir(0)

    private fun setDir(newDir: Int) {
        if (newDir == dir) return
        dir = newDir
        job?.cancel()
        if (dir != 0) {
            job = scope.launch {
                while (isActive && dir != 0) {
                    scrollState.scrollBy(stepPx * dir)
                    delay(16)
                }
            }
        }
    }
}

@Composable
fun rememberAutoScrollController(
    scrollState: ScrollState
): AutoScrollController {
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val density = LocalDensity.current
    val edgeSlopPx = with(density) { 72.dp.toPx() }
    val stepPx = with(density) { 28.dp.toPx() }

    return remember {
        AutoScrollController(scrollState, view, edgeSlopPx, stepPx, scope)
    }
}
