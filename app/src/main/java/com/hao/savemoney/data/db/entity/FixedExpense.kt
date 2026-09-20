package com.hao.savemoney.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 固定支出实体
 * 如房租、水电、保险等每月必须支出的项目
 */
@Entity(tableName = "fixed_expense")
data class FixedExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** 关联的年月 */
    @ColumnInfo(name = "year_month")
    val yearMonth: String,

    /** 支出名称，如"房租"、"水电费" */
    @ColumnInfo(name = "name")
    val name: String,

    /** 支出金额 */
    @ColumnInfo(name = "amount")
    val amount: Double
)
