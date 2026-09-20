package com.hao.savemoney.domain.export

import com.hao.savemoney.data.db.entity.DailyBudget
import com.hao.savemoney.data.db.entity.Expense
import com.hao.savemoney.data.db.entity.FixedExpense
import com.hao.savemoney.data.db.entity.MonthlyIncome
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PostgreSQL 数据导出服务
 * 生成标准 PostgreSQL 兼容的 SQL 文件内容
 */
@Singleton
class PostgresExporter @Inject constructor() {

    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * 生成完整的 SQL 导出内容（包含建表语句 + 数据插入）
     */
    fun exportToSql(
        incomes: List<MonthlyIncome>,
        fixedExpenses: List<FixedExpense>,
        expenses: List<Expense>,
        dailyBudgets: List<DailyBudget>
    ): String {
        val sb = StringBuilder()

        sb.appendLine("-- ============================================")
        sb.appendLine("-- Hao的存钱妙具 - 数据导出")
        sb.appendLine("-- 导出时间: ${timestampFormat.format(Date())}")
        sb.appendLine("-- PostgreSQL 兼容格式")
        sb.appendLine("-- ============================================")
        sb.appendLine()

        // 建表语句
        sb.appendLine(generateCreateTables())
        sb.appendLine()

        // 插入数据
        if (incomes.isNotEmpty()) {
            sb.appendLine(generateIncomeInserts(incomes))
            sb.appendLine()
        }

        if (fixedExpenses.isNotEmpty()) {
            sb.appendLine(generateFixedExpenseInserts(fixedExpenses))
            sb.appendLine()
        }

        if (expenses.isNotEmpty()) {
            sb.appendLine(generateExpenseInserts(expenses))
            sb.appendLine()
        }

        if (dailyBudgets.isNotEmpty()) {
            sb.appendLine(generateDailyBudgetInserts(dailyBudgets))
        }

        return sb.toString()
    }

    private fun generateCreateTables(): String = """
-- 月收入表
CREATE TABLE IF NOT EXISTS monthly_income (
    id SERIAL PRIMARY KEY,
    year_month VARCHAR(7) NOT NULL UNIQUE,
    amount DECIMAL(12,2) NOT NULL,
    saving_rate DECIMAL(4,2) NOT NULL DEFAULT 0.25,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 固定支出表
CREATE TABLE IF NOT EXISTS fixed_expense (
    id SERIAL PRIMARY KEY,
    year_month VARCHAR(7) NOT NULL,
    name VARCHAR(100) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_fixed_expense_income 
        FOREIGN KEY (year_month) REFERENCES monthly_income(year_month)
);

-- 消费记录表
CREATE TABLE IF NOT EXISTS expense (
    id SERIAL PRIMARY KEY,
    amount DECIMAL(12,2) NOT NULL,
    category VARCHAR(20) NOT NULL,
    note TEXT DEFAULT '',
    recorded_at TIMESTAMP NOT NULL,
    year_month VARCHAR(7) NOT NULL,
    CONSTRAINT fk_expense_income 
        FOREIGN KEY (year_month) REFERENCES monthly_income(year_month)
);

-- 每日预算表
CREATE TABLE IF NOT EXISTS daily_budget (
    id SERIAL PRIMARY KEY,
    date DATE NOT NULL UNIQUE,
    planned_amount DECIMAL(12,2) NOT NULL,
    spent_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    remaining_amount DECIMAL(12,2) NOT NULL,
    is_weekend BOOLEAN NOT NULL DEFAULT FALSE,
    year_month VARCHAR(7) NOT NULL,
    CONSTRAINT fk_daily_budget_income 
        FOREIGN KEY (year_month) REFERENCES monthly_income(year_month)
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_expense_year_month ON expense(year_month);
CREATE INDEX IF NOT EXISTS idx_expense_recorded_at ON expense(recorded_at);
CREATE INDEX IF NOT EXISTS idx_expense_category ON expense(category);
CREATE INDEX IF NOT EXISTS idx_daily_budget_year_month ON daily_budget(year_month);
CREATE INDEX IF NOT EXISTS idx_fixed_expense_year_month ON fixed_expense(year_month);
    """.trimIndent()

    private fun generateIncomeInserts(incomes: List<MonthlyIncome>): String {
        val sb = StringBuilder("-- 月收入数据\n")
        sb.appendLine("INSERT INTO monthly_income (year_month, amount, saving_rate, created_at) VALUES")

        val values = incomes.map { income ->
            val ts = timestampFormat.format(Date(income.createdAt))
            "('${escape(income.yearMonth)}', ${income.amount}, ${income.savingRate}, '$ts')"
        }
        sb.appendLine(values.joinToString(",\n"))
        sb.appendLine("ON CONFLICT (year_month) DO UPDATE SET")
        sb.appendLine("    amount = EXCLUDED.amount,")
        sb.appendLine("    saving_rate = EXCLUDED.saving_rate,")
        sb.appendLine("    created_at = EXCLUDED.created_at;")
        return sb.toString()
    }

    private fun generateFixedExpenseInserts(expenses: List<FixedExpense>): String {
        val sb = StringBuilder("-- 固定支出数据\n")
        sb.appendLine("INSERT INTO fixed_expense (year_month, name, amount) VALUES")

        val values = expenses.map { exp ->
            "('${escape(exp.yearMonth)}', '${escape(exp.name)}', ${exp.amount})"
        }
        sb.appendLine(values.joinToString(",\n") + ";")
        return sb.toString()
    }

    private fun generateExpenseInserts(expenses: List<Expense>): String {
        val sb = StringBuilder("-- 消费记录数据\n")
        sb.appendLine("INSERT INTO expense (amount, category, note, recorded_at, year_month) VALUES")

        val values = expenses.map { exp ->
            val ts = timestampFormat.format(Date(exp.recordedAt))
            "(${exp.amount}, '${escape(exp.category)}', '${escape(exp.note)}', '$ts', '${escape(exp.yearMonth)}')"
        }
        sb.appendLine(values.joinToString(",\n") + ";")
        return sb.toString()
    }

    private fun generateDailyBudgetInserts(budgets: List<DailyBudget>): String {
        val sb = StringBuilder("-- 每日预算数据\n")
        sb.appendLine("INSERT INTO daily_budget (date, planned_amount, spent_amount, remaining_amount, is_weekend, year_month) VALUES")

        val values = budgets.map { b ->
            "('${escape(b.date)}', ${b.plannedAmount}, ${b.spentAmount}, ${b.remainingAmount}, ${b.isWeekend}, '${escape(b.yearMonth)}')"
        }
        sb.appendLine(values.joinToString(",\n"))
        sb.appendLine("ON CONFLICT (date) DO UPDATE SET")
        sb.appendLine("    planned_amount = EXCLUDED.planned_amount,")
        sb.appendLine("    spent_amount = EXCLUDED.spent_amount,")
        sb.appendLine("    remaining_amount = EXCLUDED.remaining_amount;")
        return sb.toString()
    }

    /** 转义 SQL 字符串中的单引号 */
    private fun escape(value: String): String {
        return value.replace("'", "''")
    }
}
