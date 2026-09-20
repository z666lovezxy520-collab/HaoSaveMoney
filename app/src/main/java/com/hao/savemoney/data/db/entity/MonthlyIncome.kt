package com.hao.savemoney.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 月收入记录实体
 * 每月月初录入一条收入数据
 */
@Entity(tableName = "monthly_income")
data class MonthlyIncome(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** 年月标识，格式 "2026-09" */
    @ColumnInfo(name = "year_month")
    val yearMonth: String,

    /** 月收入金额 */
    @ColumnInfo(name = "amount")
    val amount: Double,

    /** 储蓄比例 (0.20 ~ 0.30) */
    @ColumnInfo(name = "saving_rate")
    val savingRate: Double = 0.25,

    /** 创建时间戳 */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
