package com.hao.savemoney.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hao.savemoney.data.db.entity.Expense
import com.hao.savemoney.ui.theme.CategoryEntertain
import com.hao.savemoney.ui.theme.CategoryFood
import com.hao.savemoney.ui.theme.CategoryOther
import com.hao.savemoney.ui.theme.CategoryShopping
import com.hao.savemoney.ui.theme.CategoryTransport

/**
 * 分类标签组件
 * 用于选择消费分类，带有分类特定颜色
 */
@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = getCategoryColor(label)
    val emoji = getCategoryEmoji(label)

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = "$emoji $label",
                style = MaterialTheme.typography.labelLarge
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = categoryColor.copy(alpha = 0.2f),
            selectedLabelColor = categoryColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = categoryColor.copy(alpha = 0.3f),
            selectedBorderColor = categoryColor,
            enabled = true,
            selected = selected
        ),
        modifier = modifier
    )
}

fun getCategoryColor(category: String) = when (category) {
    Expense.CATEGORY_FOOD -> CategoryFood
    Expense.CATEGORY_TRANSPORT -> CategoryTransport
    Expense.CATEGORY_SHOPPING -> CategoryShopping
    Expense.CATEGORY_ENTERTAINMENT -> CategoryEntertain
    else -> CategoryOther
}

fun getCategoryEmoji(category: String) = when (category) {
    Expense.CATEGORY_FOOD -> "🍜"
    Expense.CATEGORY_TRANSPORT -> "🚌"
    Expense.CATEGORY_SHOPPING -> "🛒"
    Expense.CATEGORY_ENTERTAINMENT -> "🎮"
    else -> "📦"
}
