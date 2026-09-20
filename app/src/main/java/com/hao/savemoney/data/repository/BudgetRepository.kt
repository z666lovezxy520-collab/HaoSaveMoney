package com.hao.savemoney.data.repository

import com.hao.savemoney.data.db.dao.DailyBudgetDao
import com.hao.savemoney.data.db.entity.DailyBudget
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val dailyBudgetDao: DailyBudgetDao
) {
    suspend fun saveDailyBudgets(budgets: List<DailyBudget>) {
        dailyBudgetDao.insertAll(budgets)
    }

    suspend fun updateDailyBudget(budget: DailyBudget) {
        dailyBudgetDao.update(budget)
    }

    suspend fun getTodayBudget(date: String): DailyBudget? {
        return dailyBudgetDao.getByDate(date)
    }

    fun observeTodayBudget(date: String): Flow<DailyBudget?> {
        return dailyBudgetDao.observeByDate(date)
    }

    fun observeMonthBudgets(yearMonth: String): Flow<List<DailyBudget>> {
        return dailyBudgetDao.observeByYearMonth(yearMonth)
    }

    suspend fun getMonthBudgets(yearMonth: String): List<DailyBudget> {
        return dailyBudgetDao.getByYearMonth(yearMonth)
    }

    suspend fun clearMonthBudgets(yearMonth: String) {
        dailyBudgetDao.deleteByYearMonth(yearMonth)
    }

    fun observeTotalSpent(yearMonth: String): Flow<Double> {
        return dailyBudgetDao.observeTotalSpent(yearMonth)
    }

    suspend fun getAllBudgets(): List<DailyBudget> {
        return dailyBudgetDao.getAll()
    }

    /**
     * 记录一笔消费后更新每日预算
     */
    suspend fun recordSpending(date: String, amount: Double) {
        val budget = dailyBudgetDao.getByDate(date) ?: return
        val newSpent = budget.spentAmount + amount
        val newRemaining = budget.plannedAmount - newSpent
        dailyBudgetDao.update(
            budget.copy(
                spentAmount = newSpent,
                remainingAmount = newRemaining
            )
        )
    }
}
