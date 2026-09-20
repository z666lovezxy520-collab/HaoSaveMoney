package com.hao.savemoney.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 消费记录实体
 * 每次消费后手动记录
 */
@Entity(tableName = "expense")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** 消费金额 */
    @ColumnInfo(name = "amount")
    val amount: Double,

    /** 消费分类：餐饮、交通、购物、娱乐、其他 */
    @ColumnInfo(name = "category")
    val category: String,

    /** 备注信息 */
    @ColumnInfo(name = "note")
    val note: String = "",

    /** 记录时间戳 */
    @ColumnInfo(name = "recorded_at")
    val recordedAt: Long = System.currentTimeMillis(),

    /** 所属年月 */
    @ColumnInfo(name = "year_month")
    val yearMonth: String
) {
    companion object {
        /** 消费分类常量 */
        const val CATEGORY_FOOD = "餐饮"
        const val CATEGORY_TRANSPORT = "交通"
        const val CATEGORY_SHOPPING = "购物"
        const val CATEGORY_ENTERTAINMENT = "娱乐"
        const val CATEGORY_OTHER = "其他"

        val ALL_CATEGORIES = listOf(
            CATEGORY_FOOD,
            CATEGORY_TRANSPORT,
            CATEGORY_SHOPPING,
            CATEGORY_ENTERTAINMENT,
            CATEGORY_OTHER
        )
    }
}
