package com.pandora.core.ui.designkit.gestures

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Gesture Manager
 * Handles various gesture interactions
 */
object GestureManager {
    
    /**
     * Swipe directions
     */
    enum class SwipeDirection {
        UP, DOWN, LEFT, RIGHT, NONE
    }
    
    /**
     * Gesture callbacks
     */
    data class GestureCallbacks(
        val onSwipe: (SwipeDirection) -> Unit = {},
        val onTap: () -> Unit = {},
        val onLongPress: () -> Unit = {},
        val onDoubleTap: () -> Unit = {},
        val onPinch: (Float) -> Unit = {},
        val onDrag: (Offset) -> Unit = {}
    )
}

/**
 * Swipe gesture modifier
 */
fun Modifier.swipeGesture(
    onSwipe: (GestureManager.SwipeDirection) -> Unit,
    swipeThreshold: Dp = 50.dp
): Modifier = this.pointerInput(Unit) {
    detectDragGestures(
        onDragEnd = {
            // onDragEnd doesn't provide offset, so we'll use a different approach
        }
    ) { change, dragAmount ->
        change.consume()
        val direction = when {
            abs(dragAmount.x) > abs(dragAmount.y) -> {
                if (dragAmount.x > swipeThreshold.value) GestureManager.SwipeDirection.RIGHT
                else if (dragAmount.x < -swipeThreshold.value) GestureManager.SwipeDirection.LEFT
                else GestureManager.SwipeDirection.NONE
            }
            else -> {
                if (dragAmount.y > swipeThreshold.value) GestureManager.SwipeDirection.DOWN
                else if (dragAmount.y < -swipeThreshold.value) GestureManager.SwipeDirection.UP
                else GestureManager.SwipeDirection.NONE
            }
        }
        if (direction != GestureManager.SwipeDirection.NONE) {
            onSwipe(direction)
        }
    }
}

/**
 * Tap gesture modifier
 */
fun Modifier.tapGesture(
    onTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onDoubleTap: () -> Unit = {}
): Modifier = this.pointerInput(Unit) {
    detectTapGestures(
        onTap = { onTap() },
        onLongPress = { onLongPress() },
        onDoubleTap = { onDoubleTap() }
    )
}

/**
 * Pinch gesture modifier
 */
fun Modifier.pinchGesture(
    onPinch: (Float) -> Unit
): Modifier = this.pointerInput(Unit) {
    detectTransformGestures(
        onGesture = { _, _, zoom, _ ->
            onPinch(zoom)
        }
    )
}

/**
 * Drag gesture modifier
 */
fun Modifier.dragGesture(
    onDrag: (Offset) -> Unit
): Modifier = this.pointerInput(Unit) {
    detectDragGestures(
        onDrag = { _, dragAmount ->
            onDrag(dragAmount)
        }
    )
}

/**
 * Combined gesture modifier
 */
fun Modifier.combinedGestures(
    callbacks: GestureManager.GestureCallbacks,
    swipeThreshold: Dp = 50.dp
): Modifier = this
    .swipeGesture(callbacks.onSwipe, swipeThreshold)
    .tapGesture(
        onTap = callbacks.onTap,
        onLongPress = callbacks.onLongPress,
        onDoubleTap = callbacks.onDoubleTap
    )
    .pinchGesture(callbacks.onPinch)
    .dragGesture(callbacks.onDrag)

/**
 * Keyboard gesture support
 */
object KeyboardGestures {
    
    /**
     * Swipe to delete
     */
    fun swipeToDelete(
        modifier: Modifier = Modifier,
        onDelete: () -> Unit,
        threshold: Dp = 100.dp
    ): Modifier = modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragEnd = {
                // onDragEnd doesn't provide offset
            }
        ) { change, dragAmount ->
            change.consume()
            if (dragAmount.x < -threshold.value) {
                onDelete()
            }
        }
    }
    
    /**
     * Swipe to select
     */
    fun swipeToSelect(
        modifier: Modifier = Modifier,
        onSelect: () -> Unit,
        threshold: Dp = 50.dp
    ): Modifier = modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragEnd = {
                // onDragEnd doesn't provide offset
            }
        ) { change, dragAmount ->
            change.consume()
            if (abs(dragAmount.x) > threshold.value || abs(dragAmount.y) > threshold.value) {
                onSelect()
            }
        }
    }
    
    /**
     * Long press to edit
     */
    fun longPressToEdit(
        modifier: Modifier = Modifier,
        onEdit: () -> Unit
    ): Modifier = modifier.pointerInput(Unit) {
        detectTapGestures(
            onLongPress = { onEdit() }
        )
    }
}

/**
 * Navigation gestures
 */
object NavigationGestures {
    
    /**
     * Swipe to navigate back
     */
    fun swipeToGoBack(
        modifier: Modifier = Modifier,
        onBack: () -> Unit,
        threshold: Dp = 100.dp
    ): Modifier = modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragEnd = {
                // onDragEnd doesn't provide offset
            }
        ) { change, dragAmount ->
            change.consume()
            if (dragAmount.x > threshold.value) {
                onBack()
            }
        }
    }
    
    /**
     * Swipe to refresh
     */
    fun swipeToRefresh(
        modifier: Modifier = Modifier,
        onRefresh: () -> Unit,
        threshold: Dp = 100.dp
    ): Modifier = modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragEnd = {
                // onDragEnd doesn't provide offset
            }
        ) { change, dragAmount ->
            change.consume()
            if (dragAmount.y > threshold.value) {
                onRefresh()
            }
        }
    }
}

/**
 * AI interaction gestures
 */
object AIGestures {
    
    /**
     * Swipe up for AI suggestions
     */
    fun swipeUpForAI(
        modifier: Modifier = Modifier,
        onAISuggest: () -> Unit,
        threshold: Dp = 80.dp
    ): Modifier = modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragEnd = {
                // onDragEnd doesn't provide offset
            }
        ) { change, dragAmount ->
            change.consume()
            if (dragAmount.y < -threshold.value) {
                onAISuggest()
            }
        }
    }
    
    /**
     * Swipe down to dismiss AI
     */
    fun swipeDownToDismissAI(
        modifier: Modifier = Modifier,
        onDismiss: () -> Unit,
        threshold: Dp = 80.dp
    ): Modifier = modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragEnd = {
                // onDragEnd doesn't provide offset
            }
        ) { change, dragAmount ->
            change.consume()
            if (dragAmount.y > threshold.value) {
                onDismiss()
            }
        }
    }
}