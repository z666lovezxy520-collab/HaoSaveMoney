package com.hao.savemoney.domain.engine

import com.hao.savemoney.data.db.entity.DailyBudget
import com.hao.savemoney.domain.model.BudgetSummary
import com.hao.savemoney.domain.model.SavingPlan
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 智能浮动预算算法引擎
 *
 * 核心逻辑：
 * 1. 可支配收入 = 月收入 - 固定支出
 * 2. 储蓄金额 = 可支配收入 × 储蓄比例
 * 3. 应急备用金 = 可支配收入 × 5%
 * 4. 消费预算 = 可支配收入 - 储蓄 - 应急
 * 5. 每日额度按智能浮动分配：
 *    - 工作日(周一~五): 基础额度 × 0.85
 *    - 周六: 基础额度 × 1.30
 *    - 周日: 基础额度 × 1.15
 * 6. 动态调整：前一天节省金额的 50% 滚动累加到后续
 */
@Singleton
class BudgetEngine @Inject constructor() {

    companion object {
        /** 应急备用金比例 */
        private const val EMERGENCY_RATE = 0.05

        /** 工作日权重系数 */
        private const val WEEKDAY_WEIGHT = 0.85

        /** 周六权重系数 */
        private const val SATURDAY_WEIGHT = 1.30

        /** 周日权重系数 */
        private const val SUNDAY_WEIGHT = 1.15

        /** 前日结余滚动比例 */
        private const val ROLLOVER_RATE = 0.50

        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM")
    }

    /**
     * 计算整月的每日预算分配
     *
     * @param monthlyIncome 月收入
     * @param fixedExpenseTotal 固定支出总额
     * @param savingRate 储蓄比例 (0.20 ~ 0.30)
     * @param yearMonth 目标年月
     * @return 每日预算列表
     */
    fun calculateDailyBudgets(
        monthlyIncome: Double,
        fixedExpenseTotal: Double,
        savingRate: Double,
        yearMonth: YearMonth
    ): List<DailyBudget> {
        val disposableIncome = monthlyIncome - fixedExpenseTotal
        if (disposableIncome <= 0) return emptyList()

        val savingAmount = disposableIncome * savingRate
        val emergencyAmount = disposableIncome * EMERGENCY_RATE
        val consumptionBudget = disposableIncome - savingAmount - emergencyAmount

        val daysInMonth = yearMonth.lengthOfMonth()
        val yearMonthStr = yearMonth.format(YEAR_MONTH_FORMATTER)

        // 第一步：计算每天的权重
        val dailyWeights = (1..daysInMonth).map { day ->
            val date = yearMonth.atDay(day)
            getDayWeight(date.dayOfWeek)
        }

        // 第二步：按权重分配预算（使总额 = consumptionBudget）
        val totalWeight = dailyWeights.sum()
        val baseUnit = consumptionBudget / totalWeight

        return (1..daysInMonth).map { day ->
            val date = yearMonth.atDay(day)
            val weight = dailyWeights[day - 1]
            val plannedAmount = Math.round(baseUnit * weight * 100.0) / 100.0 // 保留2位小数
            val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY ||
                    date.dayOfWeek == DayOfWeek.SUNDAY

            DailyBudget(
                date = date.format(DATE_FORMATTER),
                plannedAmount = plannedAmount,
                spentAmount = 0.0,
                remainingAmount = plannedAmount,
                isWeekend = isWeekend,
                yearMonth = yearMonthStr
            )
        }
    }

    /**
     * 动态调整剩余天数的预算
     * 将前一天的结余按 ROLLOVER_RATE 分摊到后续天数
     *
     * @param budgets 当月所有每日预算（按日期排序）
     * @param today 今天的日期
     * @return 调整后的预算列表
     */
    fun adjustBudgets(
        budgets: List<DailyBudget>,
        today: LocalDate
    ): List<DailyBudget> {
        if (budgets.isEmpty()) return budgets

        val result = budgets.toMutableList()
        val todayStr = today.format(DATE_FORMATTER)
        val todayIndex = result.indexOfFirst { it.date == todayStr }

        if (todayIndex <= 0) return result

        // 计算前一天的结余
        val yesterday = result[todayIndex - 1]
        val savings = yesterday.remainingAmount

        if (savings > 0) {
            // 将结余的 50% 分摊到今天及之后的天数
            val rolloverAmount = savings * ROLLOVER_RATE
            val remainingDays = result.size - todayIndex

            if (remainingDays > 0) {
                val perDayBonus = rolloverAmount / remainingDays
                for (i in todayIndex until result.size) {
                    val budget = result[i]
                    if (budget.spentAmount == 0.0) { // 只调整未消费的天
                        result[i] = budget.copy(
                            plannedAmount = budget.plannedAmount + perDayBonus,
                            remainingAmount = budget.remainingAmount + perDayBonus
                        )
                    }
                }
            }
        }

        return result
    }

    /**
     * 生成存钱方案（保守/标准/激进）
     */
    fun generateSavingPlans(
        monthlyIncome: Double,
        fixedExpenseTotal: Double,
        daysInMonth: Int
    ): List<SavingPlan> {
        val disposableIncome = monthlyIncome - fixedExpenseTotal
        if (disposableIncome <= 0) return emptyList()
        return SavingPlan.generatePlans(disposableIncome, daysInMonth)
    }

    /**
     * 计算存钱区间
     * @return Pair(最小存钱额, 最大存钱额) 对应 (激进20%, 保守30%)
     */
    fun calculateSavingRange(disposableIncome: Double): Pair<Double, Double> {
        val min = disposableIncome * SavingPlan.RATE_AGGRESSIVE  // 20%
        val max = disposableIncome * SavingPlan.RATE_CONSERVATIVE // 30%
        return Pair(min, max)
    }

    /**
     * 生成预算摘要
     */
    fun createBudgetSummary(
        monthlyIncome: Double,
        fixedExpenseTotal: Double,
        savingRate: Double,
        totalSpent: Double,
        todayBudget: DailyBudget?
    ): BudgetSummary {
        val disposableIncome = monthlyIncome - fixedExpenseTotal
        val savingTarget = disposableIncome * savingRate
        val emergencyFund = disposableIncome * EMERGENCY_RATE
        val consumptionBudget = disposableIncome - savingTarget - emergencyFund
        val (savingMin, savingMax) = calculateSavingRange(disposableIncome)

        return BudgetSummary(
            monthlyIncome = monthlyIncome,
            fixedExpenseTotal = fixedExpenseTotal,
            disposableIncome = disposableIncome,
            savingTarget = savingTarget,
            emergencyFund = emergencyFund,
            consumptionBudget = consumptionBudget,
            totalSpent = totalSpent,
            budgetRemaining = consumptionBudget - totalSpent,
            todayPlanned = todayBudget?.plannedAmount ?: 0.0,
            todaySpent = todayBudget?.spentAmount ?: 0.0,
            todayRemaining = todayBudget?.remainingAmount ?: 0.0,
            savingRate = savingRate,
            savingRangeMin = savingMin,
            savingRangeMax = savingMax
        )
    }

    /**
     * 获取指定星期几的权重
     */
    private fun getDayWeight(dayOfWeek: DayOfWeek): Double {
        return when (dayOfWeek) {
            DayOfWeek.SATURDAY -> SATURDAY_WEIGHT
            DayOfWeek.SUNDAY -> SUNDAY_WEIGHT
            else -> WEEKDAY_WEIGHT
        }
    }
}
