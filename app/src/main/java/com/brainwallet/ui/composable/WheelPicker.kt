package com.brainwallet.ui.composable

import android.util.Log
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.absoluteValue

interface WheelPickerContentScope {
    val state: WheelPickerState
}

interface WheelPickerDisplayScope : WheelPickerContentScope {
    @Composable
    fun Content(index: Int)
}

@Composable
fun VerticalWheelPicker(
    modifier: Modifier = Modifier,
    count: Int,
    state: WheelPickerState = rememberWheelPickerState(),
    key: ((index: Int) -> Any)? = null,
    itemHeight: Dp = 35.dp,
    unfocusedCount: Int = 2,
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    debug: Boolean = false,
    focus: @Composable () -> Unit = { WheelPickerFocusVertical() },
    display: @Composable WheelPickerDisplayScope.(index: Int) -> Unit = {
        DefaultWheelPickerDisplay(
            it
        )
    },
    content: @Composable WheelPickerContentScope.(index: Int) -> Unit,
) {
    WheelPicker(
        modifier = modifier,
        isVertical = true,
        count = count,
        state = state,
        key = key,
        itemSize = itemHeight,
        unfocusedCount = unfocusedCount,
        userScrollEnabled = userScrollEnabled,
        reverseLayout = reverseLayout,
        debug = debug,
        focus = focus,
        display = display,
        content = content,
    )
}


@Composable
private fun WheelPicker(
    modifier: Modifier,
    isVertical: Boolean,
    count: Int,
    state: WheelPickerState,
    key: ((index: Int) -> Any)?,
    itemSize: Dp,
    unfocusedCount: Int,
    userScrollEnabled: Boolean,
    reverseLayout: Boolean,
    debug: Boolean,
    focus: @Composable () -> Unit,
    display: @Composable WheelPickerDisplayScope.(index: Int) -> Unit,
    content: @Composable WheelPickerContentScope.(index: Int) -> Unit,
) {
    require(count >= 0) { "Require count >= 0" }
    require(unfocusedCount >= 0) { "Require unfocusedCount >= 0" }
    require(itemSize > 0.dp) { "Require itemSize > 0.dp" }

    SafeBox(
        modifier = modifier,
        isVertical = isVertical,
        itemSize = itemSize,
        unfocusedCount = unfocusedCount,
    ) { safeUnfocusedCount ->
        InternalWheelPicker(
            isVertical = isVertical,
            count = count,
            state = state,
            key = key,
            itemSize = itemSize,
            unfocusedCount = safeUnfocusedCount,
            userScrollEnabled = userScrollEnabled,
            reverseLayout = reverseLayout,
            debug = debug,
            focus = focus,
            display = display,
            content = content,
        )

        if (debug && unfocusedCount != safeUnfocusedCount) {
            LaunchedEffect(unfocusedCount, safeUnfocusedCount) {
                logMsg(true) { "unfocusedCount $unfocusedCount -> $safeUnfocusedCount" }
            }
        }
    }
}

@Composable
private fun InternalWheelPicker(
    isVertical: Boolean,
    count: Int,
    state: WheelPickerState,
    key: ((index: Int) -> Any)?,
    itemSize: Dp,
    unfocusedCount: Int,
    userScrollEnabled: Boolean,
    reverseLayout: Boolean,
    debug: Boolean,
    focus: @Composable () -> Unit,
    display: @Composable WheelPickerDisplayScope.(index: Int) -> Unit,
    content: @Composable WheelPickerContentScope.(index: Int) -> Unit,
) {
    state.debug = debug
    LaunchedEffect(state, count) {
        state.updateCount(count)
    }

    val nestedScrollConnection = remember(state) {
        WheelPickerNestedScrollConnection(state)
    }.apply {
        this.isVertical = isVertical
        this.itemSizePx = with(LocalDensity.current) { itemSize.roundToPx() }
        this.reverseLayout = reverseLayout
    }

    val totalSize = remember(itemSize, unfocusedCount) {
        itemSize * (unfocusedCount * 2 + 1)
    }

    val displayScope = remember(state) {
        WheelPickerDisplayScopeImpl(state)
    }.apply {
        this.content = content
    }

    Box(
        modifier = Modifier
            .nestedScroll(nestedScrollConnection)
            .graphicsLayer {
                this.alpha = if (state.isReady) 1f else 0f
            }
            .run {
                if (totalSize > 0.dp) {
                    if (isVertical) {
                        height(totalSize).widthIn(40.dp)
                    } else {
                        width(totalSize).heightIn(40.dp)
                    }
                } else {
                    this
                }
            },
        contentAlignment = Alignment.Center,
    ) {

        val lazyListScope: LazyListScope.() -> Unit = {

            repeat(unfocusedCount) {
                item(contentType = "placeholder") {
                    ItemSizeBox(
                        isVertical = isVertical,
                        itemSize = itemSize,
                    )
                }
            }

            items(
                count = count,
                key = key,
            ) { index ->
                ItemSizeBox(
                    isVertical = isVertical,
                    itemSize = itemSize,
                ) {
                    displayScope.display(index)
                }
            }

            repeat(unfocusedCount) {
                item(contentType = "placeholder") {
                    ItemSizeBox(
                        isVertical = isVertical,
                        itemSize = itemSize,
                    )
                }
            }
        }

        if (isVertical) {
            LazyColumn(
                state = state.lazyListState,
                horizontalAlignment = Alignment.CenterHorizontally,
                reverseLayout = reverseLayout,
                userScrollEnabled = userScrollEnabled && state.isReady,
                modifier = Modifier.matchParentSize(),
                content = lazyListScope,
            )
        } else {
            LazyRow(
                state = state.lazyListState,
                verticalAlignment = Alignment.CenterVertically,
                reverseLayout = reverseLayout,
                userScrollEnabled = userScrollEnabled && state.isReady,
                modifier = Modifier.matchParentSize(),
                content = lazyListScope,
            )
        }

        ItemSizeBox(
            modifier = Modifier.align(Alignment.Center),
            isVertical = isVertical,
            itemSize = itemSize,
        ) {
            focus()
        }
    }
}

@Composable
private fun SafeBox(
    modifier: Modifier = Modifier,
    isVertical: Boolean,
    itemSize: Dp,
    unfocusedCount: Int,
    content: @Composable (safeUnfocusedCount: Int) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val maxSize = if (isVertical) maxHeight else maxWidth
        val result = remember(maxSize, itemSize, unfocusedCount) {
            val totalSize = itemSize * (unfocusedCount * 2 + 1)
            if (totalSize <= maxSize) {
                unfocusedCount
            } else {
                (((maxSize - itemSize) / 2f) / itemSize).toInt().coerceAtLeast(0)
            }
        }
        content(result)
    }
}

@Composable
private fun ItemSizeBox(
    modifier: Modifier = Modifier,
    isVertical: Boolean,
    itemSize: Dp,
    content: @Composable () -> Unit = { },
) {
    Box(
        modifier
            .run {
                if (isVertical) {
                    height(itemSize)
                } else {
                    width(itemSize)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

private class WheelPickerNestedScrollConnection(
    private val state: WheelPickerState,
) : NestedScrollConnection {
    var isVertical: Boolean = true
    var itemSizePx: Int = 0
    var reverseLayout: Boolean = false

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        state.synchronizeCurrentIndexSnapshot()
        return super.onPostScroll(consumed, available, source)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val currentIndex = state.synchronizeCurrentIndexSnapshot()
        return if (currentIndex >= 0) {
            available.flingItemCount(
                isVertical = isVertical,
                itemSize = itemSizePx,
                decay = exponentialDecay(2f),
                reverseLayout = reverseLayout,
            ).let { flingItemCount ->
                if (flingItemCount == 0) {
                    state.animateScrollToIndex(currentIndex)
                } else {
                    state.animateScrollToIndex(currentIndex - flingItemCount)
                }
            }
            available
        } else {
            super.onPreFling(available)
        }
    }
}

private fun Velocity.flingItemCount(
    isVertical: Boolean,
    itemSize: Int,
    decay: DecayAnimationSpec<Float>,
    reverseLayout: Boolean,
): Int {
    if (itemSize <= 0) return 0
    val velocity = if (isVertical) y else x
    val targetValue = decay.calculateTargetValue(0f, velocity)
    val flingItemCount = (targetValue / itemSize).toInt()
    return if (reverseLayout) -flingItemCount else flingItemCount
}

private class WheelPickerDisplayScopeImpl(
    override val state: WheelPickerState,
) : WheelPickerDisplayScope {

    var content: @Composable WheelPickerContentScope.(index: Int) -> Unit by mutableStateOf({})

    @Composable
    override fun Content(index: Int) {
        content(index)
    }
}

internal inline fun logMsg(debug: Boolean, block: () -> String) {
    if (debug) {
        Log.i("WheelPicker", block())
    }
}

//default
/**
 * The default implementation of focus view in vertical.
 */
@Composable
fun WheelPickerFocusVertical(
    modifier: Modifier = Modifier,
    dividerSize: Dp = 1.dp,
    dividerColor: Color = DefaultDividerColor,
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .background(dividerColor)
                .height(dividerSize)
                .fillMaxWidth()
                .align(Alignment.TopCenter),
        )
        Box(
            modifier = Modifier
                .background(dividerColor)
                .height(dividerSize)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        )
    }
}

/**
 * Default divider color.
 */
private val DefaultDividerColor: Color
    @Composable
    get() {
        val color = if (isSystemInDarkTheme()) Color.White else Color.Black
        return color.copy(alpha = 0.2f)
    }

/**
 * Default display.
 */
@Composable
fun WheelPickerDisplayScope.DefaultWheelPickerDisplay(
    index: Int,
) {
    val focused = index == state.currentIndexSnapshot
    val animateScale by animateFloatAsState(
        targetValue = if (focused) 1.0f else 0.8f,
        label = "Wheel picker item scale",
    )
    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = if (focused) 1.0f else 0.3f
            this.scaleX = animateScale
            this.scaleY = animateScale
        }
    ) {
        Content(index)
    }
}

//state
@Composable
fun rememberWheelPickerState(
    initialIndex: Int = 0,
): WheelPickerState {
    return rememberSaveable(saver = WheelPickerState.Saver) {
        WheelPickerState(
            initialIndex = initialIndex,
        )
    }
}

@Composable
fun WheelPickerState.CurrentIndex(
    block: suspend (Int) -> Unit,
) {
    val blockUpdated by rememberUpdatedState(block)
    LaunchedEffect(this) {
        snapshotFlow { currentIndex }
            .collect { blockUpdated(it) }
    }
}

class WheelPickerState internal constructor(
    initialIndex: Int,
) {
    internal var debug = false
    internal val lazyListState = LazyListState()
    internal var isReady by mutableStateOf(false)

    private var _count = 0
    private var _currentIndex by mutableIntStateOf(-1)
    private var _currentIndexSnapshot by mutableIntStateOf(-1)

    private var _pendingIndex: Int? = initialIndex.coerceAtLeast(0)
    private var _pendingIndexContinuation: CancellableContinuation<Unit>? = null
        set(value) {
            field = value
            if (value == null) _pendingIndex = null
        }

    /**
     * Index of picker when it is idle, -1 means that there is no data.
     *
     * Note that this property is observable and if you use it in the composable function
     * it will be recomposed on every change.
     */
    val currentIndex: Int get() = _currentIndex

    /**
     * Index of picker when it is idle or drag but not fling, -1 means that there is no data.
     *
     * Note that this property is observable and if you use it in the composable function
     * it will be recomposed on every change.
     */
    val currentIndexSnapshot: Int get() = _currentIndexSnapshot

    /**
     * [LazyListState.interactionSource]
     */
    val interactionSource: InteractionSource get() = lazyListState.interactionSource

    /**
     * [LazyListState.isScrollInProgress]
     */
    val isScrollInProgress: Boolean get() = lazyListState.isScrollInProgress

    suspend fun animateScrollToIndex(index: Int) {
        logMsg(debug) { "animateScrollToIndex index:$index count:$_count" }
        @Suppress("NAME_SHADOWING")
        val index = index.coerceAtLeast(0)
        lazyListState.animateScrollToItem(index)
        synchronizeCurrentIndex()
    }

    suspend fun scrollToIndex(index: Int) {
        logMsg(debug) { "scrollToIndex index:$index count:$_count" }
        @Suppress("NAME_SHADOWING")
        val index = index.coerceAtLeast(0)

        // Always cancel last continuation.
        _pendingIndexContinuation?.let {
            logMsg(debug) { "cancelAwaitIndex" }
            _pendingIndexContinuation = null
            it.cancel()
        }

        awaitIndex(index)

        lazyListState.scrollToItem(index)
        synchronizeCurrentIndex()
    }

    private suspend fun awaitIndex(index: Int) {
        if (_count > 0) return
        logMsg(debug) { "awaitIndex:$index start" }
        suspendCancellableCoroutine { cont ->
            _pendingIndex = index
            _pendingIndexContinuation = cont
            cont.invokeOnCancellation {
                logMsg(debug) { "awaitIndex:$index canceled" }
                _pendingIndexContinuation = null
            }
        }
        logMsg(debug) { "awaitIndex:$index finish" }
    }

    internal suspend fun updateCount(count: Int) {
        logMsg(debug) { "updateCount count:$count currentIndex:$_currentIndex" }

        // Update count
        _count = count

        val maxIndex = count - 1
        if (maxIndex < _currentIndex) {
            if (count > 0) {
                scrollToIndex(maxIndex)
            } else {
                synchronizeCurrentIndex()
            }
        }

        if (count > 0) {
            val pendingIndex = _pendingIndex
            if (pendingIndex != null) {
                logMsg(debug) { "Found pendingIndex:$pendingIndex pendingIndexContinuation:$_pendingIndexContinuation" }
                val continuation = _pendingIndexContinuation
                _pendingIndexContinuation = null

                if (continuation?.isActive == true) {
                    logMsg(debug) { "resume pendingIndexContinuation" }
                    continuation.resume(Unit)
                } else {
                    scrollToIndex(pendingIndex)
                }
            } else {
                if (_currentIndex < 0) {
                    synchronizeCurrentIndex()
                }
            }
        }

        isReady = count > 0
    }

    private fun synchronizeCurrentIndex() {
        val index = synchronizeCurrentIndexSnapshot()
        if (_currentIndex != index) {
            logMsg(debug) { "setCurrentIndex:$index" }
            _currentIndex = index
            _currentIndexSnapshot = index
        }
    }

    internal fun synchronizeCurrentIndexSnapshot(): Int {
        return (mostStartItemInfo()?.index ?: -1).also {
            _currentIndexSnapshot = it
        }
    }

    /**
     * The item closest to the viewport start.
     */
    private fun mostStartItemInfo(): LazyListItemInfo? {
        if (_count <= 0) return null

        val layoutInfo = lazyListState.layoutInfo
        val listInfo = layoutInfo.visibleItemsInfo

        if (listInfo.isEmpty()) return null
        if (listInfo.size == 1) return listInfo.first()

        val firstItem = listInfo.first()
        val firstOffsetDelta = (firstItem.offset - layoutInfo.viewportStartOffset).absoluteValue
        return if (firstOffsetDelta < firstItem.size / 2) {
            firstItem
        } else {
            listInfo[1]
        }
    }

    companion object {
        val Saver = listSaver(
            save = { listOf(it.currentIndex) },
            restore = { WheelPickerState(initialIndex = it[0]) },
        )
    }
}