package com.just_for_fun.pocketledger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.just_for_fun.pocketledger.data.model.CategoryTotal
import com.just_for_fun.pocketledger.data.model.DailyTotal
import com.just_for_fun.pocketledger.data.model.enums.displayName
import kotlin.math.atan2
import kotlin.math.hypot

// Warm, semantically meaningful palette that harmonises with the amber primary.
private val chartColors = listOf(
    Color(0xFFFFBA2B), // amber   – matches app primary
    Color(0xFF4D6544), // sage green
    Color(0xFF1565C0), // deep blue
    Color(0xFFC62828), // deep red
    Color(0xFF6A1B9A), // purple
    Color(0xFF00838F), // teal
    Color(0xFFE65100), // deep orange
    Color(0xFF4527A0), // deep purple
    Color(0xFF546E7A)  // blue-grey
)

@Composable
fun ExpenseDonutChart(
    data: List<CategoryTotal>,
    selectedCategory: CategoryTotal?,
    onSliceSelected: (CategoryTotal?) -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
                .pointerInput(data, canvasSize) {
                    detectTapGestures { tapOffset ->
                        if (data.isEmpty()) {
                            onSliceSelected(null)
                            return@detectTapGestures
                        }

                        val centerX = canvasSize.width / 2f
                        val centerY = canvasSize.height / 2f
                        val radius = minOf(canvasSize.width, canvasSize.height) * 0.42f
                        val strokeWidth = radius * 0.38f
                        val innerRadius = radius - strokeWidth / 2f
                        val outerRadius = radius + strokeWidth / 2f

                        val dx = tapOffset.x - centerX
                        val dy = tapOffset.y - centerY
                        val distance = hypot(dx, dy)
                        if (distance !in innerRadius..outerRadius) {
                            onSliceSelected(null)
                            return@detectTapGestures
                        }

                        val rawAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        val angle = (rawAngle + 450f) % 360f

                        var startAngle = 0f
                        val total = data.sumOf { it.amount }
                        data.forEach { item ->
                            val sweep = ((item.amount / total) * 360.0).toFloat()
                            if (angle in startAngle..(startAngle + sweep)) {
                                onSliceSelected(item)
                                return@detectTapGestures
                            }
                            startAngle += sweep
                        }
                        onSliceSelected(null)
                    }
                }
        ) {
            if (data.isEmpty()) return@Canvas

            val radius = minOf(size.width, size.height) * 0.42f
            val strokeWidth = radius * 0.38f
            val gap = 2f  // tiny gap between slices for a cleaner look
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)

            var startAngle = -90f
            val total = data.sumOf { it.amount }
            data.forEachIndexed { index, item ->
                val sweep = ((item.amount / total) * 360.0).toFloat() - gap
                val arcColor = chartColors[index % chartColors.size]
                val isSelected = selectedCategory?.category == item.category
                val width = if (isSelected) strokeWidth * 1.14f else strokeWidth

                drawArc(
                    color = arcColor,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = width, cap = StrokeCap.Round)
                )
                startAngle += sweep + gap
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = selectedCategory?.category?.displayName() ?: "Total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "₹${"%.0f".format(selectedCategory?.amount ?: data.sumOf { it.amount })}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DailyBarChart(
    totals: List<DailyTotal>,
    daysInMonth: Int,
    selectedDay: DailyTotal?,
    onBarSelected: (DailyTotal?) -> Unit,
    modifier: Modifier = Modifier
) {
    val amountByDay = remember(totals) { totals.associateBy { it.dayOfMonth } }
    val maxAmount = remember(totals) { (totals.maxOfOrNull { it.amount } ?: 0.0).coerceAtLeast(1.0) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .onSizeChanged { canvasSize = it }
                .pointerInput(totals, daysInMonth, canvasSize) {
                    detectTapGestures { tapOffset ->
                        if (daysInMonth <= 0 || canvasSize.width == 0) {
                            onBarSelected(null)
                            return@detectTapGestures
                        }
                        val spacing = canvasSize.width.toFloat() / daysInMonth
                        val day = (tapOffset.x / spacing).toInt().coerceIn(0, daysInMonth - 1) + 1
                        onBarSelected(amountByDay[day] ?: DailyTotal(day, 0.0))
                    }
                }
        ) {
            if (daysInMonth <= 0) return@Canvas

            val spacing = size.width / daysInMonth
            val barWidth = spacing * 0.50f
            val bottomY = size.height
            val chartHeight = size.height - 8f

            for (day in 1..daysInMonth) {
                val amount = amountByDay[day]?.amount ?: 0.0
                val normalized = (amount / maxAmount).toFloat().coerceIn(0f, 1f)
                val barHeight = (chartHeight * normalized).coerceAtLeast(if (amount > 0) 6f else 0f)
                val left = (day - 1) * spacing + ((spacing - barWidth) / 2f)
                val top = bottomY - barHeight
                val barColor = if (selectedDay?.dayOfMonth == day) primaryColor else secondaryColor

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }

        if (selectedDay != null) {
            Surface(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Day ${selectedDay.dayOfMonth} — ₹${"%.0f".format(selectedDay.amount)}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

fun colorForChartIndex(index: Int): Color = chartColors[index % chartColors.size]