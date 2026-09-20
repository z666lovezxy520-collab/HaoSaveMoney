package com.hao.savemoney.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hao.savemoney.data.db.entity.DailyBudget
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyBudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<DailyBudget>)

    @Update
    suspend fun update(budget: DailyBudget)

    /** 获取指定日期的预算 */
    @Query("SELECT * FROM daily_budget WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyBudget?

    /** 响应式获取指定日期的预算 */
    @Query("SELECT * FROM daily_budget WHERE date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<DailyBudget?>

    /** 获取指定月份的所有每日预算 */
    @Query("SELECT * FROM daily_budget WHERE year_month = :yearMonth ORDER BY date ASC")
    fun observeByYearMonth(yearMonth: String): Flow<List<DailyBudget>>

    /** 获取指定月份的每日预算（非响应式） */
    @Query("SELECT * FROM daily_budget WHERE year_month = :yearMonth ORDER BY date ASC")
    suspend fun getByYearMonth(yearMonth: String): List<DailyBudget>

    /** 删除指定月份的所有每日预算（重新计算时用） */
    @Query("DELETE FROM daily_budget WHERE year_month = :yearMonth")
    suspend fun deleteByYearMonth(yearMonth: String)

    /** 获取指定月份已消费总额 */
    @Query("SELECT COALESCE(SUM(spent_amount), 0) FROM daily_budget WHERE year_month = :yearMonth")
    fun observeTotalSpent(yearMonth: String): Flow<Double>

    /** 获取所有每日预算（用于导出） */
    @Query("SELECT * FROM daily_budget ORDER BY date ASC")
    suspend fun getAll(): List<DailyBudget>
}
