package com.hao.savemoney.data.repository

import com.hao.savemoney.data.db.dao.FixedExpenseDao
import com.hao.savemoney.data.db.dao.MonthlyIncomeDao
import com.hao.savemoney.data.db.entity.FixedExpense
import com.hao.savemoney.data.db.entity.MonthlyIncome
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepository @Inject constructor(
    private val monthlyIncomeDao: MonthlyIncomeDao,
    private val fixedExpenseDao: FixedExpenseDao
) {
    // ========== 月收入 ==========

    suspend fun saveIncome(income: MonthlyIncome): Long {
        val existing = monthlyIncomeDao.getByYearMonth(income.yearMonth)
        return if (existing != null) {
            monthlyIncomeDao.update(income.copy(id = existing.id))
            existing.id.toLong()
        } else {
            monthlyIncomeDao.insert(income)
        }
    }

    suspend fun getIncome(yearMonth: String): MonthlyIncome? {
        return monthlyIncomeDao.getByYearMonth(yearMonth)
    }

    fun observeIncome(yearMonth: String): Flow<MonthlyIncome?> {
        return monthlyIncomeDao.observeByYearMonth(yearMonth)
    }

    fun observeAllIncome(): Flow<List<MonthlyIncome>> {
        return monthlyIncomeDao.observeAll()
    }

    suspend fun getAllIncome(): List<MonthlyIncome> {
        return monthlyIncomeDao.getAll()
    }

    // ========== 固定支出 ==========

    suspend fun addFixedExpense(expense: FixedExpense): Long {
        return fixedExpenseDao.insert(expense)
    }

    suspend fun removeFixedExpense(expense: FixedExpense) {
        fixedExpenseDao.delete(expense)
    }

    fun observeFixedExpenses(yearMonth: String): Flow<List<FixedExpense>> {
        return fixedExpenseDao.observeByYearMonth(yearMonth)
    }

    suspend fun getFixedExpenseTotal(yearMonth: String): Double {
        return fixedExpenseDao.getTotalByYearMonth(yearMonth)
    }

    suspend fun getFixedExpenses(yearMonth: String): List<FixedExpense> {
        return fixedExpenseDao.getByYearMonth(yearMonth)
    }

    suspend fun getAllFixedExpenses(): List<FixedExpense> {
        return fixedExpenseDao.getAll()
    }
}
