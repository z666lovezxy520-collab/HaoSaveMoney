package com.hao.savemoney.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hao.savemoney.data.db.entity.Expense
import com.hao.savemoney.data.repository.BudgetRepository
import com.hao.savemoney.data.repository.ExpenseRepository
import com.hao.savemoney.data.repository.IncomeRepository
import com.hao.savemoney.domain.engine.BudgetEngine
import com.hao.savemoney.domain.model.BudgetSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository,
    private val budgetEngine: BudgetEngine
) : ViewModel() {

    private val _budgetSummary = MutableStateFlow(BudgetSummary.empty())
    val budgetSummary: StateFlow<BudgetSummary> = _budgetSummary.asStateFlow()

    private val _todayExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val todayExpenses: StateFlow<List<Expense>> = _todayExpenses.asStateFlow()

    private val _hasIncomeSetup = MutableStateFlow(false)
    val hasIncomeSetup: StateFlow<Boolean> = _hasIncomeSetup.asStateFlow()

    private val _showRecordDialog = MutableStateFlow(false)
    val showRecordDialog: StateFlow<Boolean> = _showRecordDialog.asStateFlow()

    /** 记录完成后的事件：剩余额度消息 */
    private val _recordEvent = MutableSharedFlow<String>()
    val recordEvent = _recordEvent.asSharedFlow()

    private val today = LocalDate.now()
    private val currentYearMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    private val todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // 检查当月是否已设置收入
            val income = incomeRepository.getIncome(currentYearMonth)
            _hasIncomeSetup.value = income != null

            if (income != null) {
                refreshSummary(income.amount, income.savingRate)
                collectTodayExpenses()
            }
        }
    }

    private fun collectTodayExpenses() {
        viewModelScope.launch {
            expenseRepository.observeExpensesByDate(currentYearMonth, todayStr)
                .collect { expenses ->
                    _todayExpenses.value = expenses
                }
        }
    }

    private suspend fun refreshSummary(monthlyIncome: Double, savingRate: Double) {
        val fixedTotal = incomeRepository.getFixedExpenseTotal(currentYearMonth)
        val monthTotal = expenseRepository.getMonthTotal(currentYearMonth)
        val todayBudget = budgetRepository.getTodayBudget(todayStr)

        val summary = budgetEngine.createBudgetSummary(
            monthlyIncome = monthlyIncome,
            fixedExpenseTotal = fixedTotal,
            savingRate = savingRate,
            totalSpent = monthTotal,
            todayBudget = todayBudget
        )
        _budgetSummary.value = summary
    }

    fun showRecordDialog() {
        _showRecordDialog.value = true
    }

    fun hideRecordDialog() {
        _showRecordDialog.value = false
    }

    /**
     * 记录一笔消费
     */
    fun recordExpense(amount: Double, category: String, note: String) {
        viewModelScope.launch {
            // 1. 保存消费记录
            val expense = Expense(
                amount = amount,
                category = category,
                note = note,
                yearMonth = currentYearMonth
            )
            expenseRepository.addExpense(expense)

            // 2. 更新每日预算
            budgetRepository.recordSpending(todayStr, amount)

            // 3. 刷新摘要
            val income = incomeRepository.getIncome(currentYearMonth)
            if (income != null) {
                refreshSummary(income.amount, income.savingRate)
            }

            // 4. 关闭弹窗
            _showRecordDialog.value = false

            // 5. 发送剩余额度通知
            val todayBudget = budgetRepository.getTodayBudget(todayStr)
            val remaining = todayBudget?.remainingAmount ?: 0.0
            val message = if (remaining >= 0) {
                "✅ 记录成功！今日剩余额度: ¥%.2f".format(remaining)
            } else {
                "⚠️ 记录成功！今日已超支: ¥%.2f".format(-remaining)
            }
            _recordEvent.emit(message)
        }
    }

    fun refreshData() {
        loadData()
    }
}
