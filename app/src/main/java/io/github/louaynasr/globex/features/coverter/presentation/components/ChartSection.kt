package io.github.louaynasr.globex.features.coverter.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.louaynasr.globex.features.coverter.domain.model.HistoricalRate

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

    val maxRate = remember(values) { (values.maxOrNull() ?: 0.0) * 1.02 } // 2% padding top
    val minRate = remember(values) { (values.minOrNull() ?: 0.0) * 0.98 } // 2% padding bottom

    val lineBlue = Color(0xFF3B82F6)
    val gradientColor = Color(0xFF3B82F6).copy(alpha = 0.15f)

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
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E222C))
                    .padding(4.dp)
            ) {
                TimeRange.entries.forEach { range ->
                    RangeButton(
                        text = range.label,
                        isSelected = selectedRange == range,
                        onClick = { onRangeSelected(range) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E222C)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (values.size < 2) return@Canvas

                    val width = size.width
                    val height = size.height
                    val spacePerDataPoint = width / (values.size - 1)

                    fun getY(value: Double): Float {
                        val range = maxRate - minRate
                        val percentage = (value - minRate) / range
                        return (height - (percentage * height)).toFloat()
                    }

                    val strokePath = Path().apply {
                        val firstY = getY(values[0])
                        moveTo(0f, firstY)

                        for (i in 0 until values.size - 1) {
                            val currentX = i * spacePerDataPoint
                            val nextX = (i + 1) * spacePerDataPoint
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
                        lineTo(width, height)
                        lineTo(0f, height)
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

                    // Draw a subtle glow effect
                    drawPath(
                        path = strokePath,
                        color = lineBlue.copy(alpha = 0.2f),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw a dot at the last point for enhancement
//                    val lastX = width
//                    val lastY = getY(values.last())
//                    drawCircle(
//                        color = Color.White,
//                        radius = 4.dp.toPx(),
//                        center = androidx.compose.ui.geometry.Offset(lastX, lastY)
//                    )
//                    drawCircle(
//                        color = lineBlue,
//                        radius = 2.dp.toPx(),
//                        center = androidx.compose.ui.geometry.Offset(lastX, lastY)
//                    )
                }
            }
        }
    }
}

@Composable
fun RangeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF3B82F6) else Color.Transparent
    val contentColor = if (isSelected) Color.White else Color.Gray

    Box(
        modifier = Modifier
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
