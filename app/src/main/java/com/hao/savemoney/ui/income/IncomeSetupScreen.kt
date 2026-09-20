package com.hao.savemoney.ui.income

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hao.savemoney.ui.theme.Accent
import com.hao.savemoney.ui.theme.GradientBlueStart
import com.hao.savemoney.ui.theme.Success
import java.text.NumberFormat
import java.util.Locale

@Composable
fun IncomeSetupScreen(
    viewModel: IncomeViewModel,
    onSaveComplete: () -> Unit
) {
    val incomeAmount by viewModel.incomeAmount.collectAsState()
    val fixedExpenses by viewModel.fixedExpenses.collectAsState()
    val savingPlans by viewModel.savingPlans.collectAsState()
    val selectedPlanIndex by viewModel.selectedPlanIndex.collectAsState()
    val newExpenseName by viewModel.newExpenseName.collectAsState()
    val newExpenseAmount by viewModel.newExpenseAmount.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)

    LaunchedEffect(Unit) {
        viewModel.saveEvent.collect { success ->
            if (success) onSaveComplete()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ========== 标题 ==========
        item {
            Column {
                Spacer(Modifier.height(16.dp))
                Text(
                    "💰 设置本月收入",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "填写收入和固定支出，系统将自动为你计算每日消费额度",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ========== 月收入输入 ==========
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "本月收入",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = incomeAmount,
                        onValueChange = { viewModel.updateIncomeAmount(it) },
                        label = { Text("月收入金额") },
                        prefix = { Text("¥ ") },
                        leadingIcon = {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // ========== 固定支出 ==========
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "固定支出",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "房租、水电、保险等每月必须支付的费用",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    // 已添加的固定支出列表
                    fixedExpenses.forEach { expense ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                expense.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    currencyFormat.format(expense.amount),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                IconButton(onClick = { viewModel.removeFixedExpense(expense) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "删除",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (fixedExpenses.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                "合计: ${currencyFormat.format(fixedExpenses.sumOf { it.amount })}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // 添加新固定支出
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newExpenseName,
                            onValueChange = { viewModel.updateNewExpenseName(it) },
                            label = { Text("名称") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = newExpenseAmount,
                            onValueChange = { viewModel.updateNewExpenseAmount(it) },
                            label = { Text("金额") },
                            prefix = { Text("¥") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { viewModel.addFixedExpense() }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "添加",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // ========== 存钱方案选择 ==========
        item {
            if (savingPlans.isNotEmpty()) {
                Text(
                    "选择存钱方案",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        itemsIndexed(savingPlans) { index, plan ->
            val isSelected = index == selectedPlanIndex
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = if (isSelected) BorderStroke(2.dp, Accent) else null,
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        Accent.copy(alpha = 0.08f)
                    else
                        MaterialTheme.colorScheme.surface
                ),
                onClick = { viewModel.selectPlan(index) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                plan.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "存${(plan.savingRate * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = Success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "月存 ${currencyFormat.format(plan.monthlySaving)}  |  日均可花 ${currencyFormat.format(plan.dailyAverage)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "年预计存款: ${currencyFormat.format(plan.yearlySaving)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Success
                        )
                    }
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "已选",
                            tint = Accent
                        )
                    }
                }
            }
        }

        // ========== 确认按钮 ==========
        item {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.saveAndGenerate() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientBlueStart
                ),
                enabled = incomeAmount.toDoubleOrNull() != null && savingPlans.isNotEmpty()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "确认设置，开始存钱！",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
