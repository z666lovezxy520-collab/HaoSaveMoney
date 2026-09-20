package com.hao.savemoney.data.repository

import com.hao.savemoney.data.db.dao.CategoryTotal
import com.hao.savemoney.data.db.dao.DailyTotal
import com.hao.savemoney.data.db.dao.ExpenseDao
import com.hao.savemoney.data.db.entity.Expense
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    suspend fun addExpense(expense: Expense): Long {
        return expenseDao.insert(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.delete(expense)
    }

    fun observeExpensesByDate(yearMonth: String, date: String): Flow<List<Expense>> {
        return expenseDao.observeByDate(yearMonth, date)
    }

    suspend fun getTotalByDate(yearMonth: String, date: String): Double {
        return expenseDao.getTotalByDate(yearMonth, date)
    }

    fun observeTotalByDate(yearMonth: String, date: String): Flow<Double> {
        return expenseDao.observeTotalByDate(yearMonth, date)
    }

    fun observeExpensesByMonth(yearMonth: String): Flow<List<Expense>> {
        return expenseDao.observeByYearMonth(yearMonth)
    }

    suspend fun getMonthTotal(yearMonth: String): Double {
        return expenseDao.getTotalByYearMonth(yearMonth)
    }

    fun observeMonthTotal(yearMonth: String): Flow<Double> {
        return expenseDao.observeTotalByYearMonth(yearMonth)
    }

    suspend fun getCategorySummary(yearMonth: String): List<CategoryTotal> {
        return expenseDao.getCategorySummary(yearMonth)
    }

    fun observeCategorySummary(yearMonth: String): Flow<List<CategoryTotal>> {
        return expenseDao.observeCategorySummary(yearMonth)
    }

    fun observeDailySummary(yearMonth: String): Flow<List<DailyTotal>> {
        return expenseDao.observeDailySummary(yearMonth)
    }

    suspend fun getAllExpenses(): List<Expense> {
        return expenseDao.getAll()
    }

    suspend fun getExpensesByMonth(yearMonth: String): List<Expense> {
        return expenseDao.getByYearMonth(yearMonth)
    }
}
