package com.hao.savemoney.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hao.savemoney.data.db.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Delete
    suspend fun delete(expense: Expense)

    /** 获取指定日期的所有消费记录 */
    @Query("SELECT * FROM expense WHERE year_month = :yearMonth AND date(recorded_at / 1000, 'unixepoch', 'localtime') = :date ORDER BY recorded_at DESC")
    fun observeByDate(yearMonth: String, date: String): Flow<List<Expense>>

    /** 获取指定日期的消费总额 */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM expense 
        WHERE year_month = :yearMonth 
        AND date(recorded_at / 1000, 'unixepoch', 'localtime') = :date
    """)
    suspend fun getTotalByDate(yearMonth: String, date: String): Double

    /** 响应式获取指定日期的消费总额 */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM expense 
        WHERE year_month = :yearMonth 
        AND date(recorded_at / 1000, 'unixepoch', 'localtime') = :date
    """)
    fun observeTotalByDate(yearMonth: String, date: String): Flow<Double>

    /** 获取指定月份的所有消费记录 */
    @Query("SELECT * FROM expense WHERE year_month = :yearMonth ORDER BY recorded_at DESC")
    fun observeByYearMonth(yearMonth: String): Flow<List<Expense>>

    /** 获取指定月份的消费总额 */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM expense WHERE year_month = :yearMonth")
    suspend fun getTotalByYearMonth(yearMonth: String): Double

    /** 响应式获取指定月份消费总额 */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM expense WHERE year_month = :yearMonth")
    fun observeTotalByYearMonth(yearMonth: String): Flow<Double>

    /** 获取指定月份按分类汇总 */
    @Query("""
        SELECT category, SUM(amount) as total 
        FROM expense 
        WHERE year_month = :yearMonth 
        GROUP BY category 
        ORDER BY total DESC
    """)
    suspend fun getCategorySummary(yearMonth: String): List<CategoryTotal>

    /** 响应式按分类汇总 */
    @Query("""
        SELECT category, SUM(amount) as total 
        FROM expense 
        WHERE year_month = :yearMonth 
        GROUP BY category 
        ORDER BY total DESC
    """)
    fun observeCategorySummary(yearMonth: String): Flow<List<CategoryTotal>>

    /** 获取指定月份每日消费汇总 */
    @Query("""
        SELECT date(recorded_at / 1000, 'unixepoch', 'localtime') as day, 
               SUM(amount) as total 
        FROM expense 
        WHERE year_month = :yearMonth 
        GROUP BY day 
        ORDER BY day ASC
    """)
    fun observeDailySummary(yearMonth: String): Flow<List<DailyTotal>>

    /** 获取所有消费记录（非响应式，用于导出） */
    @Query("SELECT * FROM expense ORDER BY year_month ASC, recorded_at ASC")
    suspend fun getAll(): List<Expense>

    /** 获取指定月份消费记录（非响应式，用于导出） */
    @Query("SELECT * FROM expense WHERE year_month = :yearMonth ORDER BY recorded_at ASC")
    suspend fun getByYearMonth(yearMonth: String): List<Expense>
}

/** 分类消费汇总数据类 */
data class CategoryTotal(
    val category: String,
    val total: Double
)

/** 每日消费汇总数据类 */
data class DailyTotal(
    val day: String,
    val total: Double
)
