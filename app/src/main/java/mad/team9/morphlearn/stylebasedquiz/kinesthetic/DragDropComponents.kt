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
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import mad.team9.morphlearn.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun DraggableAnswer(
    content: String,
    dropTargets: List<DropTargetState>,
    enabled: Boolean,
    onMatchFound: (DropTargetState, String) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var startPositionInWindow by remember { mutableStateOf(Offset.Zero) }
    var itemSize by remember { mutableStateOf(IntSize.Zero) }

    Surface(
        modifier = Modifier
            .zIndex(if (isDragging) 10f else 1f)
            .onGloballyPositioned { coordinates ->
                if (!isDragging) {
                    startPositionInWindow = coordinates.positionInWindow()
                    itemSize = coordinates.size
                }
            }
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(enabled) {
                if (enabled) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            val centerX = startPositionInWindow.x + offsetX + (itemSize.width / 2f)
                            val centerY = startPositionInWindow.y + offsetY + (itemSize.height / 2f)
                            val centerPoint = Offset(centerX, centerY)

                            val target = dropTargets.find { it.screenBounds.contains(centerPoint) }

                            if (target != null) {
                                onMatchFound(target, content)
                            }
                            offsetX = 0f
                            offsetY = 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            offsetX = 0f
                            offsetY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    )
                }
            }
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        contentColor = TextDark,
        border = BorderStroke(
            1.dp,
            when {
                !enabled -> Color.Gray.copy(alpha = 0.5f)
                isDragging -> MorphTeal
                else -> Color.LightGray
            }
        ),
        shadowElevation = if (isDragging) 8.dp else 2.dp
    ) {
        Text(
            text = content, 
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun QuestionSlot(
    state: DropTargetState,
    enabled: Boolean,
    backgroundColor: Color,
    borderColor: Color
) {
    val isFilled = state.currentAnswer != null

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .onGloballyPositioned { layoutCoordinates ->
                state.screenBounds = layoutCoordinates.boundsInWindow()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White,
            contentColor = TextDark
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = state.questionText, 
                style = MaterialTheme.typography.bodyMedium,
                color = if (!enabled && borderColor != Color.LightGray) borderColor else TextDark
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .background(
                        color = if (!enabled) backgroundColor 
                                else if (isFilled) Color.White 
                                else BackgroundGray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = if (isFilled) 2.dp else 1.dp,
                        color = if (!enabled) borderColor 
                                else if (isFilled) MorphTeal 
                                else Color.LightGray,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.currentAnswer ?: "Drag answer here",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isFilled) MorphTeal else Color.Gray,
                    fontWeight = if (isFilled) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
