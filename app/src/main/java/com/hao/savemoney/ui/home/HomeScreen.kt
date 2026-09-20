package com.hao.savemoney.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hao.savemoney.data.db.entity.Expense
import com.hao.savemoney.ui.components.BudgetProgressBar
import com.hao.savemoney.ui.components.ExpenseRecordDialog
import com.hao.savemoney.ui.components.SavingRangeCard
import com.hao.savemoney.ui.components.getCategoryColor
import com.hao.savemoney.ui.components.getCategoryEmoji
import com.hao.savemoney.ui.theme.Accent
import com.hao.savemoney.ui.theme.GradientBlueEnd
import com.hao.savemoney.ui.theme.GradientBlueStart
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToIncome: () -> Unit
) {
    val summary by viewModel.budgetSummary.collectAsState()
    val todayExpenses by viewModel.todayExpenses.collectAsState()
    val hasIncome by viewModel.hasIncomeSetup.collectAsState()
    val showDialog by viewModel.showRecordDialog.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)

    // 监听记录事件
    LaunchedEffect(Unit) {
        viewModel.recordEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // 如果没有设置收入，跳转到收入设置页
    LaunchedEffect(hasIncome) {
        if (!hasIncome) {
            onNavigateToIncome()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (hasIncome) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.showRecordDialog() },
                    containerColor = Accent,
                    contentColor = GradientBlueStart,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "记一笔")
                    Spacer(Modifier.width(8.dp))
                    Text("记一笔", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ========== 顶部：今日概览卡片 ==========
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(GradientBlueStart, GradientBlueEnd)
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Accent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "今日可用额度",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            // 今日剩余额度（大字）
                            Text(
                                text = currencyFormat.format(summary.todayRemaining),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.isTodayOverBudget)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onPrimary
                            )

                            Spacer(Modifier.height(16.dp))

                            // 今日进度条
                            BudgetProgressBar(
                                spent = summary.todaySpent,
                                total = summary.todayPlanned,
                                label = "今日已花 ${currencyFormat.format(summary.todaySpent)} / ${currencyFormat.format(summary.todayPlanned)}",
                                height = 8
                            )

                            Spacer(Modifier.height(16.dp))

                            // 本月概览
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "本月已花",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.TrendingDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            currencyFormat.format(summary.totalSpent),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "预算剩余",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = Accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            currencyFormat.format(summary.budgetRemaining),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ========== 存钱区间卡片 ==========
            item {
                SavingRangeCard(
                    savingMin = summary.savingRangeMin,
                    savingMax = summary.savingRangeMax,
                    currentSavingRate = summary.savingRate,
                    monthlyIncome = summary.monthlyIncome
                )
            }

            // ========== 本月预算使用进度 ==========
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "本月消费预算",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        BudgetProgressBar(
                            spent = summary.totalSpent,
                            total = summary.consumptionBudget,
                            label = "已使用",
                            height = 10
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${currencyFormat.format(summary.totalSpent)} / ${currencyFormat.format(summary.consumptionBudget)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ========== 今日消费列表 ==========
            item {
                Text(
                    "今日消费记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (todayExpenses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "📝 今日还没有消费记录，点击右下角按钮开始记账",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                items(todayExpenses) { expense ->
                    ExpenseListItem(expense = expense, currencyFormat = currencyFormat)
                }
            }

            // 底部间距
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // 记账弹窗
    if (showDialog) {
        ExpenseRecordDialog(
            onDismiss = { viewModel.hideRecordDialog() },
            onConfirm = { amount, category, note ->
                viewModel.recordExpense(amount, category, note)
            }
        )
    }
}

@Composable
private fun ExpenseListItem(
    expense: Expense,
    currencyFormat: NumberFormat
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 分类图标
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(getCategoryColor(expense.category).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getCategoryEmoji(expense.category),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(Modifier.width(12.dp))

                // 分类和备注
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (expense.note.isNotBlank()) {
                        Text(
                            text = expense.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 金额和时间
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "-${currencyFormat.format(expense.amount)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = timeFormat.format(Date(expense.recordedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
