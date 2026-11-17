package app.contadores

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class CounterDrag(private val list: LazyListState) {
    var id by mutableStateOf<String?>(null)
        private set
    var target by mutableStateOf<String?>(null)
        private set
    private var initialOffset = 0f
    private var initialPointer = 0f
    private var pointer by mutableFloatStateOf(0f)

    fun start(key: String, offset: Int, y: Float) {
        initialOffset = offset.toFloat()
        initialPointer = y
        pointer = y
        id = key
    }

    fun update(y: Float) {
        pointer = y
        updateTarget()
    }

    fun offsetFor(key: String): Float {
        if (id != key) return 0f
        val currentOffset = list.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key }?.offset ?: return 0f
        return initialOffset + pointer - initialPointer - currentOffset
    }

    fun hitHandle(y: Float, handleBottom: Float) = list.layoutInfo.visibleItemsInfo.firstOrNull {
        it.contentType == "counter" && y >= it.offset && y < it.offset + handleBottom
    }

    suspend fun scrollAtEdge(edge: Float, maximum: Float) {
        val height = list.layoutInfo.viewportSize.height
        val velocity = when {
            pointer < edge -> -(edge - pointer) / edge
            pointer > height - edge -> (pointer - height + edge) / edge
            else -> 0f
        }
        if (velocity != 0f) {
            list.scrollBy(velocity.coerceIn(-1f, 1f) * maximum)
            updateTarget()
        }
    }

    private fun updateTarget() {
        target = list.layoutInfo.visibleItemsInfo.firstOrNull {
            it.contentType == "counter" && pointer >= it.offset && pointer < it.offset + it.size
        }?.key as? String
    }

    fun clear() {
        id = null
        target = null
    }
}

internal fun Modifier.reorderCounters(dragState: CounterDrag, move: (String, String) -> Unit): Modifier = composed {
    val currentMove = androidx.compose.runtime.rememberUpdatedState(move)
    pointerInput(dragState) {
        val gripWidth = 48.dp.toPx()
        val gripBottom = 65.dp.toPx()
        val edge = 58.dp.toPx()
        val maximumScroll = 14.dp.toPx()
        coroutineScope {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                if (down.position.x >= gripWidth) return@awaitEachGesture
                val item = dragState.hitHandle(down.position.y, gripBottom) ?: return@awaitEachGesture
                val pressed = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                val id = item.key as? String ?: return@awaitEachGesture
                dragState.start(id, item.offset, pressed.position.y)
                val scroll = launch {
                    while (true) {
                        delay(16)
                        dragState.scrollAtEdge(edge, maximumScroll)
                    }
                }
                try {
                    while (true) {
                        val change = awaitPointerEvent(PointerEventPass.Initial).changes
                            .firstOrNull { it.id == pressed.id } ?: break
                        if (change.isConsumed) break
                        dragState.update(change.position.y)
                        change.consume()
                        if (!change.pressed) {
                            val target = dragState.target
                            if (target != null && target != id) currentMove.value(id, target)
                            break
                        }
                    }
                } finally {
                    scroll.cancel()
                    dragState.clear()
                }
            }
        }
    }
}
