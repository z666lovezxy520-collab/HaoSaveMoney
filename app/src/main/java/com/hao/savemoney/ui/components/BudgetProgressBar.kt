package com.hao.savemoney.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hao.savemoney.ui.theme.Danger
import com.hao.savemoney.ui.theme.GradientBlueEnd
import com.hao.savemoney.ui.theme.GradientBlueStart
import com.hao.savemoney.ui.theme.Success
import com.hao.savemoney.ui.theme.Warning

/**
 * 预算进度条组件
 * 带动画效果的渐变进度条，根据使用百分比变色
 */
@Composable
fun BudgetProgressBar(
    spent: Double,
    total: Double,
    modifier: Modifier = Modifier,
    label: String = "",
    showPercentage: Boolean = true,
    height: Int = 12
) {
    val percentage = if (total > 0) (spent / total).toFloat().coerceIn(0f, 1.5f) else 0f
    var animatedTarget by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animatedTarget,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    LaunchedEffect(percentage) {
        animatedTarget = percentage
    }

    // 根据百分比选择颜色
    val progressColor = when {
        percentage <= 0.6f -> Success
        percentage <= 0.85f -> Warning
        else -> Danger
    }

    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(GradientBlueStart, progressColor)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty() || showPercentage) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                if (label.isNotEmpty()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
                if (showPercentage) {
                    Text(
                        text = "${(percentage * 100).toInt().coerceAtMost(150)}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = progressColor,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
        ) {
            val barWidth = size.width
            val barHeight = size.height
            val cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)

            // 背景轨道
            drawRoundRect(
                color = Color(0xFFE8EAF0),
                topLeft = Offset.Zero,
                size = Size(barWidth, barHeight),
                cornerRadius = cornerRadius
            )

            // 进度填充
            val fillWidth = (barWidth * animatedProgress.coerceAtMost(1f))
            if (fillWidth > 0f) {
                drawRoundRect(
                    brush = gradientBrush,
                    topLeft = Offset.Zero,
                    size = Size(fillWidth, barHeight),
                    cornerRadius = cornerRadius
                )
            }
        }
    }
}

/**
 * 环形进度条 - 用于存钱目标展示
 */
@Composable
fun CircularBudgetProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 12f,
    content: @Composable () -> Unit = {}
) {
    var animatedTarget by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animatedTarget,
        animationSpec = tween(durationMillis = 1000),
        label = "circularProgress"
    )

    LaunchedEffect(progress) {
        animatedTarget = progress.coerceIn(0f, 1f)
    }

    val progressColor = when {
        progress <= 0.6f -> Success
        progress <= 0.85f -> Warning
        else -> Danger
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val diameter = minOf(size.width, size.height)
            val radius = diameter / 2 - strokeWidth
            val center = Offset(size.width / 2, size.height / 2)

            // 背景圆环
            drawCircle(
                color = Color(0xFFE8EAF0),
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )

            // 进度圆弧
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
        content()
    }
}
