package com.hao.savemoney.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 应用级依赖注入模块
 * BudgetEngine 和 PostgresExporter 使用构造注入，无需手动 @Provides
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
