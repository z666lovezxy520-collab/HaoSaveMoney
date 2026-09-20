package com.hao.savemoney.ui.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hao.savemoney.data.db.entity.DailyBudget
import com.hao.savemoney.data.db.entity.FixedExpense
import com.hao.savemoney.data.db.entity.MonthlyIncome
import com.hao.savemoney.data.repository.BudgetRepository
import com.hao.savemoney.data.repository.IncomeRepository
import com.hao.savemoney.domain.engine.BudgetEngine
import com.hao.savemoney.domain.model.SavingPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val budgetRepository: BudgetRepository,
    private val budgetEngine: BudgetEngine
) : ViewModel() {

    private val currentYearMonth = YearMonth.now()
    private val yearMonthStr = currentYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))

    private val _incomeAmount = MutableStateFlow("")
    val incomeAmount: StateFlow<String> = _incomeAmount.asStateFlow()

    private val _fixedExpenses = MutableStateFlow<List<FixedExpense>>(emptyList())
    val fixedExpenses: StateFlow<List<FixedExpense>> = _fixedExpenses.asStateFlow()

    private val _savingPlans = MutableStateFlow<List<SavingPlan>>(emptyList())
    val savingPlans: StateFlow<List<SavingPlan>> = _savingPlans.asStateFlow()

    private val _selectedPlanIndex = MutableStateFlow(1) // 默认选标准方案
    val selectedPlanIndex: StateFlow<Int> = _selectedPlanIndex.asStateFlow()

    private val _newExpenseName = MutableStateFlow("")
    val newExpenseName: StateFlow<String> = _newExpenseName.asStateFlow()

    private val _newExpenseAmount = MutableStateFlow("")
    val newExpenseAmount: StateFlow<String> = _newExpenseAmount.asStateFlow()

    private val _saveEvent = MutableSharedFlow<Boolean>()
    val saveEvent = _saveEvent.asSharedFlow()

    init {
        loadExistingData()
    }

    private fun loadExistingData() {
        viewModelScope.launch {
            // 加载已有收入
            val existing = incomeRepository.getIncome(yearMonthStr)
            if (existing != null) {
                _incomeAmount.value = existing.amount.toString()
            }

            // 加载已有固定支出
            incomeRepository.observeFixedExpenses(yearMonthStr).collect {
                _fixedExpenses.value = it
                recalculatePlans()
            }
        }
    }

    fun updateIncomeAmount(value: String) {
        _incomeAmount.value = value
        recalculatePlans()
    }

    fun updateNewExpenseName(value: String) {
        _newExpenseName.value = value
    }

    fun updateNewExpenseAmount(value: String) {
        _newExpenseAmount.value = value
    }

    fun addFixedExpense() {
        val name = _newExpenseName.value.trim()
        val amount = _newExpenseAmount.value.toDoubleOrNull()
        if (name.isBlank() || amount == null || amount <= 0) return

        viewModelScope.launch {
            incomeRepository.addFixedExpense(
                FixedExpense(
                    yearMonth = yearMonthStr,
                    name = name,
                    amount = amount
                )
            )
            _newExpenseName.value = ""
            _newExpenseAmount.value = ""
        }
    }

    fun removeFixedExpense(expense: FixedExpense) {
        viewModelScope.launch {
            incomeRepository.removeFixedExpense(expense)
        }
    }

    fun selectPlan(index: Int) {
        _selectedPlanIndex.value = index
    }

    private fun recalculatePlans() {
        val income = _incomeAmount.value.toDoubleOrNull() ?: return
        val fixedTotal = _fixedExpenses.value.sumOf { it.amount }
        val daysInMonth = currentYearMonth.lengthOfMonth()

        _savingPlans.value = budgetEngine.generateSavingPlans(income, fixedTotal, daysInMonth)
    }

    /**
     * 保存收入设置并生成每日预算
     */
    fun saveAndGenerate() {
        val income = _incomeAmount.value.toDoubleOrNull() ?: return
        val plans = _savingPlans.value
        val selectedIdx = _selectedPlanIndex.value
        if (plans.isEmpty() || selectedIdx !in plans.indices) return

        val selectedPlan = plans[selectedIdx]

        viewModelScope.launch {
            // 1. 保存月收入
            val monthlyIncome = MonthlyIncome(
                yearMonth = yearMonthStr,
                amount = income,
                savingRate = selectedPlan.savingRate
            )
            incomeRepository.saveIncome(monthlyIncome)

            // 2. 清除旧的每日预算并重新生成
            val fixedTotal = incomeRepository.getFixedExpenseTotal(yearMonthStr)
            budgetRepository.clearMonthBudgets(yearMonthStr)

            val dailyBudgets = budgetEngine.calculateDailyBudgets(
                monthlyIncome = income,
                fixedExpenseTotal = fixedTotal,
                savingRate = selectedPlan.savingRate,
                yearMonth = currentYearMonth
            )
            budgetRepository.saveDailyBudgets(dailyBudgets)

            // 3. 通知保存成功
            _saveEvent.emit(true)
        }
    }
}
