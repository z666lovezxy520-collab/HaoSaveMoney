package com.hao.savemoney.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hao.savemoney.data.db.dao.CategoryTotal
import com.hao.savemoney.data.db.dao.DailyTotal
import com.hao.savemoney.data.db.entity.DailyBudget
import com.hao.savemoney.data.repository.BudgetRepository
import com.hao.savemoney.data.repository.ExpenseRepository
import com.hao.savemoney.data.repository.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val yearMonthStr = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

    private val _categorySummary = MutableStateFlow<List<CategoryTotal>>(emptyList())
    val categorySummary: StateFlow<List<CategoryTotal>> = _categorySummary.asStateFlow()

    private val _dailySummary = MutableStateFlow<List<DailyTotal>>(emptyList())
    val dailySummary: StateFlow<List<DailyTotal>> = _dailySummary.asStateFlow()

    private val _dailyBudgets = MutableStateFlow<List<DailyBudget>>(emptyList())
    val dailyBudgets: StateFlow<List<DailyBudget>> = _dailyBudgets.asStateFlow()

    private val _monthTotal = MutableStateFlow(0.0)
    val monthTotal: StateFlow<Double> = _monthTotal.asStateFlow()

    private val _savingProgress = MutableStateFlow(0f)
    val savingProgress: StateFlow<Float> = _savingProgress.asStateFlow()

    private val _savingTarget = MutableStateFlow(0.0)
    val savingTarget: StateFlow<Double> = _savingTarget.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // 按分类汇总
            expenseRepository.observeCategorySummary(yearMonthStr).collect {
                _categorySummary.value = it
            }
        }
        viewModelScope.launch {
            // 每日消费趋势
            expenseRepository.observeDailySummary(yearMonthStr).collect {
                _dailySummary.value = it
            }
        }
        viewModelScope.launch {
            // 每日预算状态
            budgetRepository.observeMonthBudgets(yearMonthStr).collect {
                _dailyBudgets.value = it
            }
        }
        viewModelScope.launch {
            // 月消费总额
            expenseRepository.observeMonthTotal(yearMonthStr).collect {
                _monthTotal.value = it
            }
        }
        viewModelScope.launch {
            // 存钱进度
            val income = incomeRepository.getIncome(yearMonthStr)
            if (income != null) {
                val fixedTotal = incomeRepository.getFixedExpenseTotal(yearMonthStr)
                val disposable = income.amount - fixedTotal
                val target = disposable * income.savingRate
                _savingTarget.value = target

                expenseRepository.observeMonthTotal(yearMonthStr).collect { spent ->
                    val actualSaving = disposable - spent
                    _savingProgress.value = if (target > 0) (actualSaving / target).toFloat().coerceIn(0f, 1.5f) else 0f
                }
            }
        }
    }
}
