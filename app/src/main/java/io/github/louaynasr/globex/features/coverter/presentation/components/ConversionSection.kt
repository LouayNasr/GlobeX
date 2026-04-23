package io.github.louaynasr.globex.features.coverter.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.louaynasr.globex.features.coverter.domain.model.ConverterRate


@Composable
fun ConversionSection(
    firstCurrency: ConverterRate,
    secondCurrency: ConverterRate,
    onFirstCurrencyChangeRequest: () -> Unit,
    onSecondCurrencyChangeRequest: () -> Unit,
    firstAmount: String,
    secondAmount: String,
    onFirstAmountChanged: (String) -> Unit,
    onSecondAmountChanged: (String) -> Unit,
    onSwapCurrencies: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column {
            CurrencyInputCard(
                currency = firstCurrency,
                amount = firstAmount,
                onAmountChange = onFirstAmountChanged,
                onCurrencyClick = onFirstCurrencyChangeRequest,
            )

            Spacer(modifier = Modifier.height(12.dp))

            CurrencyInputCard(
                currency = secondCurrency,
                amount = secondAmount,
                onAmountChange = onSecondAmountChanged,
                onCurrencyClick = onSecondCurrencyChangeRequest,
                textColor = Color(0xFF3B82F6)
            )

            Text(
                text = "1 ${firstCurrency.code} = ${secondCurrency.rate} ${secondCurrency.code}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp)
            )
        }

        IconButton(
            onClick = onSwapCurrencies,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-14).dp)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6))
                .size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = "Swap",
                tint = Color.White
            )
        }
    }
}

@Composable
fun CurrencyInputCard(
    currency: ConverterRate,
    amount: String,
    onAmountChange: (String) -> Unit,
    onCurrencyClick: () -> Unit,
    textColor: Color = Color.White
) {
    Surface(
        color = Color(0xFF1E222C),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val interactionSource = remember { MutableInteractionSource() }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onCurrencyClick
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = currency.flagUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currency.code,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currency.name,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Amount TextField
            BasicTextField(
                value = amount,
                onValueChange = onAmountChange,
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(Color(0xFF3B82F6)),
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = getSymbol(currency.code),
                            style = TextStyle(
                                color = textColor.copy(alpha = 0.7f),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 6.dp, end = 4.dp)
                        )
                        innerTextField()
                    }
                }
            )
        }
    }
}

fun getSymbol(code: String): String {
    return when (code.uppercase()) {
        "AUD", "CAD", "HKD", "MXN", "NZD", "SGD", "USD" -> "$"
        "CNY", "JPY" -> "¥"
        "EUR" -> "€"
        "GBP" -> "£"
        "ILS" -> "₪"
        "INR" -> "₹"
        "KRW" -> "₩"
        "PHP" -> "₱"
        "THB" -> "฿"
        "TRY" -> "₺"
        else -> ""
    }
}