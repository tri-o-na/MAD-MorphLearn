package mad.team9.morphlearn.stylebasedquiz.kinesthetic

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun DraggableAnswer(
    content: String,
    dropTargets: List<DropTargetState>,
    onMatchFound: (DropTargetState, String) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(Offset.Zero) }

    Surface(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        // COLLISION DETECTION LOGIC
                        val target = dropTargets.find { it.screenBounds.contains(currentPosition) }
                        if (target != null) {
                            onMatchFound(target, content)
                        }
                        // Snap back to bank
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        // Update current global position for collision check
                        currentPosition = change.position + Offset(offsetX, offsetY)
                    }
                )
            }
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color.LightGray),
        shadowElevation = if (isDragging) 8.dp else 2.dp
    ) {
        Text(text = content, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
    }
}

@Composable
fun QuestionSlot(state: DropTargetState) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            // This captures the coordinates of the slot on the screen
            .onGloballyPositioned { layoutCoordinates ->
                state.screenBounds = layoutCoordinates.boundsInWindow()
            },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = state.questionText, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 48.dp)
                    .background(Color(0xFFF1F3F4), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.currentAnswer ?: "Drag here",
                    color = if (state.currentAnswer != null) Color(0xFF006064) else Color.Gray
                )
            }
        }
    }
}
