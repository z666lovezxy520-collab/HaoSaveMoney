package com.hao.savemoney.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hao.savemoney.data.db.dao.DailyBudgetDao
import com.hao.savemoney.data.db.dao.ExpenseDao
import com.hao.savemoney.data.db.dao.FixedExpenseDao
import com.hao.savemoney.data.db.dao.MonthlyIncomeDao
import com.hao.savemoney.data.db.entity.DailyBudget
import com.hao.savemoney.data.db.entity.Expense
import com.hao.savemoney.data.db.entity.FixedExpense
import com.hao.savemoney.data.db.entity.MonthlyIncome

@Database(
    entities = [
        MonthlyIncome::class,
        FixedExpense::class,
        Expense::class,
        DailyBudget::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun monthlyIncomeDao(): MonthlyIncomeDao
    abstract fun fixedExpenseDao(): FixedExpenseDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun dailyBudgetDao(): DailyBudgetDao
}
