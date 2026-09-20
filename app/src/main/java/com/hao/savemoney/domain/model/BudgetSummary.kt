package com.hao.savemoney.domain.model

/**
 * 预算摘要
 * 汇总显示月度预算信息
 */
data class BudgetSummary(
    /** 月收入 */
    val monthlyIncome: Double,
    /** 固定支出总额 */
    val fixedExpenseTotal: Double,
    /** 可支配收入 = 月收入 - 固定支出 */
    val disposableIncome: Double,
    /** 储蓄目标金额 */
    val savingTarget: Double,
    /** 应急备用金 */
    val emergencyFund: Double,
    /** 消费预算总额 */
    val consumptionBudget: Double,
    /** 已消费总额 */
    val totalSpent: Double,
    /** 消费预算剩余 */
    val budgetRemaining: Double,
    /** 今日计划额度 */
    val todayPlanned: Double,
    /** 今日已消费 */
    val todaySpent: Double,
    /** 今日剩余额度 */
    val todayRemaining: Double,
    /** 储蓄比例 */
    val savingRate: Double,
    /** 本月存钱区间：最低 ~ 最高 */
    val savingRangeMin: Double,
    val savingRangeMax: Double
) {
    /** 预算使用百分比 */
    val budgetUsagePercent: Double
        get() = if (consumptionBudget > 0) (totalSpent / consumptionBudget * 100).coerceIn(0.0, 100.0) else 0.0

    /** 今日额度使用百分比 */
    val todayUsagePercent: Double
        get() = if (todayPlanned > 0) (todaySpent / todayPlanned * 100).coerceIn(0.0, 100.0) else 0.0

    /** 是否超出今日预算 */
    val isTodayOverBudget: Boolean
        get() = todayRemaining < 0

    /** 是否超出月度预算 */
    val isMonthOverBudget: Boolean
        get() = budgetRemaining < 0

    companion object {
        fun empty() = BudgetSummary(
            monthlyIncome = 0.0,
            fixedExpenseTotal = 0.0,
            disposableIncome = 0.0,
            savingTarget = 0.0,
            emergencyFund = 0.0,
            consumptionBudget = 0.0,
            totalSpent = 0.0,
            budgetRemaining = 0.0,
            todayPlanned = 0.0,
            todaySpent = 0.0,
            todayRemaining = 0.0,
            savingRate = 0.25,
            savingRangeMin = 0.0,
            savingRangeMax = 0.0
        )
    }
}
