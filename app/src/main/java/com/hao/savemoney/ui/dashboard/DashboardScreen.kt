package com.hao.savemoney.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hao.savemoney.data.db.dao.CategoryTotal
import com.hao.savemoney.data.db.dao.DailyTotal
import com.hao.savemoney.ui.components.BudgetProgressBar
import com.hao.savemoney.ui.components.CircularBudgetProgress
import com.hao.savemoney.ui.components.getCategoryColor
import com.hao.savemoney.ui.components.getCategoryEmoji
import com.hao.savemoney.ui.theme.Accent
import com.hao.savemoney.ui.theme.GradientBlueEnd
import com.hao.savemoney.ui.theme.GradientBlueStart
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val categorySummary by viewModel.categorySummary.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val monthTotal by viewModel.monthTotal.collectAsState()
    val savingProgress by viewModel.savingProgress.collectAsState()
    val savingTarget by viewModel.savingTarget.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "📊 数据总览",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // ========== 存钱进度环 ==========
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "存钱目标达成进度",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(20.dp))
                    CircularBudgetProgress(
                        progress = savingProgress,
                        modifier = Modifier.size(160.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${(savingProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "目标 ${currencyFormat.format(savingTarget)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ========== 消费趋势图 ==========
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "本月消费趋势",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))

                    if (dailySummary.isNotEmpty()) {
                        DailyTrendChart(
                            data = dailySummary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    } else {
                        Text(
                            "暂无消费数据",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 40.dp)
                        )
                    }
                }
            }
        }

        // ========== 分类饼图 ==========
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "消费分类占比",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))

                    if (categorySummary.isNotEmpty() && monthTotal > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 饼图
                            CategoryPieChart(
                                data = categorySummary,
                                total = monthTotal,
                                modifier = Modifier.size(140.dp)
                            )
                            Spacer(Modifier.width(20.dp))
                            // 图例
                            Column(modifier = Modifier.weight(1f)) {
                                categorySummary.forEach { item ->
                                    CategoryLegendItem(
                                        item = item,
                                        total = monthTotal,
                                        currencyFormat = currencyFormat
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    } else {
                        Text(
                            "暂无消费数据",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 40.dp)
                        )
                    }
                }
            }
        }

        // 底部间距
        item { Spacer(Modifier.height(80.dp)) }
    }
}

/**
 * 每日消费趋势折线图
 */
@Composable
private fun DailyTrendChart(
    data: List<DailyTotal>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    val maxVal = data.maxOf { it.total }.coerceAtLeast(1.0)

    Canvas(modifier = modifier) {
        val padding = 16f
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        val stepX = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth

        // 绘制折线
        val path = Path()
        data.forEachIndexed { index, item ->
            val x = padding + index * stepX
            val y = padding + chartHeight * (1 - item.total / maxVal).toFloat()

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = GradientBlueStart,
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )

        // 绘制数据点
        data.forEachIndexed { index, item ->
            val x = padding + index * stepX
            val y = padding + chartHeight * (1 - item.total / maxVal).toFloat()

            drawCircle(
                color = GradientBlueEnd,
                radius = 5f,
                center = Offset(x, y)
            )
            drawCircle(
                color = Color.White,
                radius = 3f,
                center = Offset(x, y)
            )
        }
    }
}

/**
 * 分类消费饼图
 */
@Composable
private fun CategoryPieChart(
    data: List<CategoryTotal>,
    total: Double,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 30f
        val diameter = minOf(size.width, size.height) - strokeWidth
        val topLeft = Offset(
            (size.width - diameter) / 2,
            (size.height - diameter) / 2
        )

        var startAngle = -90f

        data.forEach { item ->
            val sweepAngle = (item.total / total * 360f).toFloat()
            val color = getCategoryColor(item.category)

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
private fun CategoryLegendItem(
    item: CategoryTotal,
    total: Double,
    currencyFormat: NumberFormat
) {
    val percentage = if (total > 0) (item.total / total * 100).toInt() else 0

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(getCategoryColor(item.category))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "${getCategoryEmoji(item.category)} ${item.category}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            "${currencyFormat.format(item.total)} ($percentage%)",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

