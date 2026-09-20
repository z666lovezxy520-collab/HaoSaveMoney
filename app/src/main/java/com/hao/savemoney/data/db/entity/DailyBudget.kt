package com.hao.savemoney.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 每日预算实体
 * 由智能浮动算法计算生成，记录每日计划/实际消费
 */
@Entity(tableName = "daily_budget")
data class DailyBudget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** 日期，格式 "2026-09-19" */
    @ColumnInfo(name = "date")
    val date: String,

    /** 计划消费额度（算法计算） */
    @ColumnInfo(name = "planned_amount")
    val plannedAmount: Double,

    /** 实际已消费金额 */
    @ColumnInfo(name = "spent_amount")
    val spentAmount: Double = 0.0,

    /** 剩余可用额度 */
    @ColumnInfo(name = "remaining_amount")
    val remainingAmount: Double = plannedAmount,

    /** 是否为周末 */
    @ColumnInfo(name = "is_weekend")
    val isWeekend: Boolean = false,

    /** 所属年月 */
    @ColumnInfo(name = "year_month")
    val yearMonth: String
)
