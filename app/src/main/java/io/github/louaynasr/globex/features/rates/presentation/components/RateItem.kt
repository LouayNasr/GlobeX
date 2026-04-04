package io.github.louaynasr.globex.features.rates.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.louaynasr.globex.features.rates.domain.model.Rate
import io.github.louaynasr.globex.features.rates.domain.model.Trend

@Composable
fun RateItem(rate: Rate) {
    val (trendIcon, trendColor) = when (rate.trend) {
        Trend.UP -> Icons.AutoMirrored.Outlined.TrendingUp to Color(0xFF4CAF50) // Use specific hex or Theme colors
        Trend.DOWN -> Icons.AutoMirrored.Outlined.TrendingDown to Color(0xFFF44336)
        else -> null to Color.Gray
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = rate.flagUrl,
            contentDescription = rate.flagUrl,
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White) // FIXME: 3/17/2026 color to be dynamic
        )
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                rate.code,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(rate.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                rate.rate.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                trendIcon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = rate.changePercentage.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = trendColor
                )
            }
        }
    }
}