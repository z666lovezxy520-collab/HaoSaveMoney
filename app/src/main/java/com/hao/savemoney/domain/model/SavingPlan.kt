package com.hao.savemoney.domain.model

/**
 * 存钱方案
 * 分为保守/标准/激进三个等级
 */
data class SavingPlan(
    /** 方案名称 */
    val name: String,
    /** 储蓄比例 */
    val savingRate: Double,
    /** 每月预计储蓄金额 */
    val monthlySaving: Double,
    /** 每日平均可消费金额 */
    val dailyAverage: Double,
    /** 年预计储蓄 */
    val yearlySaving: Double
) {
    companion object {
        const val RATE_CONSERVATIVE = 0.30  // 保守：存 30%
        const val RATE_STANDARD = 0.25      // 标准：存 25%
        const val RATE_AGGRESSIVE = 0.20    // 激进：存 20%

        /**
         * 根据可支配收入生成三个存钱方案
         */
        fun generatePlans(disposableIncome: Double, daysInMonth: Int): List<SavingPlan> {
            return listOf(
                createPlan("保守方案", RATE_CONSERVATIVE, disposableIncome, daysInMonth),
                createPlan("标准方案", RATE_STANDARD, disposableIncome, daysInMonth),
                createPlan("激进方案", RATE_AGGRESSIVE, disposableIncome, daysInMonth)
            )
        }

        private fun createPlan(
            name: String,
            rate: Double,
            disposableIncome: Double,
            daysInMonth: Int
        ): SavingPlan {
            val emergencyRate = 0.05 // 应急备用金 5%
            val saving = disposableIncome * rate
            val emergency = disposableIncome * emergencyRate
            val consumptionBudget = disposableIncome - saving - emergency
            val dailyAvg = if (daysInMonth > 0) consumptionBudget / daysInMonth else 0.0

            return SavingPlan(
                name = name,
                savingRate = rate,
                monthlySaving = saving,
                dailyAverage = dailyAvg,
                yearlySaving = saving * 12
            )
        }
    }
}
