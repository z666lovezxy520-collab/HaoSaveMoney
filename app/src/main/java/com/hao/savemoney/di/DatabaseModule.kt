package com.hao.savemoney.di

import android.content.Context
import androidx.room.Room
import com.hao.savemoney.data.db.AppDatabase
import com.hao.savemoney.data.db.dao.DailyBudgetDao
import com.hao.savemoney.data.db.dao.ExpenseDao
import com.hao.savemoney.data.db.dao.FixedExpenseDao
import com.hao.savemoney.data.db.dao.MonthlyIncomeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "hao_save_money.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideMonthlyIncomeDao(db: AppDatabase): MonthlyIncomeDao = db.monthlyIncomeDao()

    @Provides
    fun provideFixedExpenseDao(db: AppDatabase): FixedExpenseDao = db.fixedExpenseDao()

    @Provides
    fun provideExpenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideDailyBudgetDao(db: AppDatabase): DailyBudgetDao = db.dailyBudgetDao()
}
