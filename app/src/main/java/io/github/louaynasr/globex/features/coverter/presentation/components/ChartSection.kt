package io.github.louaynasr.globex.features.coverter.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.louaynasr.globex.features.coverter.domain.model.HistoricalRate
import java.util.Locale
import kotlin.math.roundToInt

enum class TimeRange(val label: String) {
    ONE_MONTH("1m"),
    THREE_MONTHS("3m"),
    ONE_YEAR("1y")
}

@Composable
fun ChartSection(
    selectedRange: TimeRange,
    historicalRates: List<HistoricalRate>,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedRates = remember(historicalRates) {
        historicalRates.sortedBy { it.date }
    }
    val values = remember(sortedRates) {
        sortedRates.map { it.rate }
    }

    var selectedPointIndex by remember(sortedRates) { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val maxRate = remember(values) { (values.maxOrNull() ?: 0.0) * 1.05 }
    val minRate = remember(values) { (values.minOrNull() ?: 0.0) * 0.95 }

    val lineBlue = MaterialTheme.colorScheme.primary
    val gradientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )

    val tooltipStyle = MaterialTheme.typography.labelMedium.copy(
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        fontWeight = FontWeight.Bold
    )
    val tooltipBgColor = MaterialTheme.colorScheme.secondaryContainer

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "History",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                TimeRange.entries.forEach { range ->
                    RangeButton(
                        text = range.label,
                        isSelected = selectedRange == range,
                        onClick = { onRangeSelected(range) },
                        modifier = Modifier.testTag("range_button_${range.label}")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp)) {
                val labelWidthPx = with(density) { 60.dp.toPx() }
                
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(values) {
                            detectTapGestures(
                                onTap = { offset ->
                                    if (values.isNotEmpty()) {
                                        val canvasWidth = size.width - labelWidthPx
                                        val touchX =
                                            (offset.x - labelWidthPx).coerceIn(0f, canvasWidth)
                                        val index =
                                            ((touchX / canvasWidth) * (values.size - 1)).roundToInt()
                                        selectedPointIndex = index
                                    }
                                },
                                onPress = {
                                    tryAwaitRelease()
                                    selectedPointIndex = null
                                }
                            )
                        }
                        .pointerInput(values) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val canvasWidth = size.width - labelWidthPx
                                    val touchX = (offset.x - labelWidthPx).coerceIn(0f, canvasWidth)
                                    val index =
                                        ((touchX / canvasWidth) * (values.size - 1)).roundToInt()
                                    selectedPointIndex = index
                                },
                                onDragEnd = { selectedPointIndex = null },
                                onDragCancel = { selectedPointIndex = null },
                                onDrag = { change, _ ->
                                    val canvasWidth = size.width - labelWidthPx
                                    val touchX =
                                        (change.position.x - labelWidthPx).coerceIn(0f, canvasWidth)
                                    val index =
                                        ((touchX / canvasWidth) * (values.size - 1)).roundToInt()
                                    selectedPointIndex = index
                                }
                            )
                        }
                ) {
                    if (values.size < 2) return@Canvas

                    val chartWidth = size.width - labelWidthPx
                    val height = size.height
                    val spacePerDataPoint = chartWidth / (values.size - 1)

                    fun getY(value: Double): Float {
                        val range = maxRate - minRate
                        val percentage = (value - minRate) / range
                        return (height - (percentage * height)).toFloat()
                    }

                    // Draw Y-Axis Labels and Grid Lines
                    val steps = 4
                    for (i in 0..steps) {
                        val fraction = i.toFloat() / steps
                        val rateValue = minRate + (maxRate - minRate) * fraction
                        val y = getY(rateValue)

                        // Grid Line
                        drawLine(
                            color = gridColor,
                            start = Offset(labelWidthPx, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Label
                        val labelText = String.format(Locale.US, "%.3f", rateValue)
                        val textLayoutResult = textMeasurer.measure(labelText, labelStyle)
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                x = (labelWidthPx - textLayoutResult.size.width) / 2,
                                y = y - textLayoutResult.size.height / 2
                            )
                        )
                    }

                    val strokePath = Path().apply {
                        val firstY = getY(values[0])
                        moveTo(labelWidthPx, firstY)

                        for (i in 0 until values.size - 1) {
                            val currentX = labelWidthPx + i * spacePerDataPoint
                            val nextX = labelWidthPx + (i + 1) * spacePerDataPoint
                            val currentY = getY(values[i])
                            val nextY = getY(values[i + 1])

                            val controlX1 = currentX + spacePerDataPoint / 2f
                            val controlX2 = currentX + spacePerDataPoint / 2f

                            cubicTo(
                                x1 = controlX1, y1 = currentY,
                                x2 = controlX2, y2 = nextY,
                                x3 = nextX, y3 = nextY
                            )
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(strokePath)
                        lineTo(size.width, height)
                        lineTo(labelWidthPx, height)
                        close()
                    }

                    // Draw fill gradient
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(gradientColor, Color.Transparent),
                            endY = height
                        )
                    )

                    // Draw main line
                    drawPath(
                        path = strokePath,
                        color = lineBlue,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw selected point indicator
                    selectedPointIndex?.let { index ->
                        val x = labelWidthPx + index * spacePerDataPoint
                        val y = getY(values[index])

                        // Vertical guide line
                        drawLine(
                            color = lineBlue.copy(alpha = 0.4f),
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = 1.5.dp.toPx()
                        )

                        // Point highlight
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = lineBlue,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )

                        // Tooltip
                        val rate = sortedRates[index]
                        val tooltipText =
                            "${rate.date}\nRate: ${String.format(Locale.US, "%.4f", rate.rate)}"
                        val tooltipLayout = textMeasurer.measure(tooltipText, tooltipStyle)

                        val tooltipPadding = 8.dp.toPx()
                        val tooltipWidth = tooltipLayout.size.width + tooltipPadding * 2
                        val tooltipHeight = tooltipLayout.size.height + tooltipPadding * 2

                        var tooltipX = x - tooltipWidth / 2
                        var tooltipY = y - tooltipHeight - 16.dp.toPx()

                        // Keep tooltip within bounds
                        tooltipX = tooltipX.coerceIn(labelWidthPx, size.width - tooltipWidth)
                        if (tooltipY < 0) {
                            tooltipY = y + 16.dp.toPx()
                        }

                        drawRoundRect(
                            color = tooltipBgColor,
                            topLeft = Offset(tooltipX, tooltipY),
                            size = Size(tooltipWidth, tooltipHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                8.dp.toPx(),
                                8.dp.toPx()
                            )
                        )

                        drawText(
                            textLayoutResult = tooltipLayout,
                            topLeft = Offset(tooltipX + tooltipPadding, tooltipY + tooltipPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RangeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = contentColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChartSectionPreview() {
    MaterialTheme {
        Surface {
            ChartSection(
                selectedRange = TimeRange.ONE_MONTH,
                historicalRates = listOf(
                    HistoricalRate("2023-10-01", 1.05),
                    HistoricalRate("2023-10-05", 1.08),
                    HistoricalRate("2023-10-10", 1.02),
                    HistoricalRate("2023-10-15", 1.15),
                    HistoricalRate("2023-10-20", 1.10),
                    HistoricalRate("2023-10-25", 1.20),
                    HistoricalRate("2023-10-30", 1.18)
                ),
                onRangeSelected = {}
            )
        }
    }
}
