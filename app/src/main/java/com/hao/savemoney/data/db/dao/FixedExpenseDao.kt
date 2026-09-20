package com.hao.savemoney.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hao.savemoney.data.db.entity.FixedExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: FixedExpense): Long

    @Delete
    suspend fun delete(expense: FixedExpense)

    /** 获取指定月份的所有固定支出 */
    @Query("SELECT * FROM fixed_expense WHERE year_month = :yearMonth ORDER BY id ASC")
    fun observeByYearMonth(yearMonth: String): Flow<List<FixedExpense>>

    /** 获取指定月份的固定支出总额 */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM fixed_expense WHERE year_month = :yearMonth")
    suspend fun getTotalByYearMonth(yearMonth: String): Double

    /** 获取所有固定支出（非响应式，用于导出） */
    @Query("SELECT * FROM fixed_expense ORDER BY year_month ASC, id ASC")
    suspend fun getAll(): List<FixedExpense>

    /** 获取指定月份固定支出列表（非响应式） */
    @Query("SELECT * FROM fixed_expense WHERE year_month = :yearMonth ORDER BY id ASC")
    suspend fun getByYearMonth(yearMonth: String): List<FixedExpense>
}
