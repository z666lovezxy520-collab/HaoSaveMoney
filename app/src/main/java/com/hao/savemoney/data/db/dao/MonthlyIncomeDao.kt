package com.hao.savemoney.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hao.savemoney.data.db.entity.MonthlyIncome
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyIncomeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: MonthlyIncome): Long

    @Update
    suspend fun update(income: MonthlyIncome)

    /** 获取指定月份的收入 */
    @Query("SELECT * FROM monthly_income WHERE year_month = :yearMonth LIMIT 1")
    suspend fun getByYearMonth(yearMonth: String): MonthlyIncome?

    /** 响应式观察指定月份的收入 */
    @Query("SELECT * FROM monthly_income WHERE year_month = :yearMonth LIMIT 1")
    fun observeByYearMonth(yearMonth: String): Flow<MonthlyIncome?>

    /** 获取所有月份的收入记录 */
    @Query("SELECT * FROM monthly_income ORDER BY year_month DESC")
    fun observeAll(): Flow<List<MonthlyIncome>>

    /** 获取所有收入记录（非响应式，用于导出） */
    @Query("SELECT * FROM monthly_income ORDER BY year_month ASC")
    suspend fun getAll(): List<MonthlyIncome>
}
