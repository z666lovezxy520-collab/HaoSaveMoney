package com.hao.savemoney.ui.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hao.savemoney.data.repository.BudgetRepository
import com.hao.savemoney.data.repository.ExpenseRepository
import com.hao.savemoney.data.repository.IncomeRepository
import com.hao.savemoney.domain.export.PostgresExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository,
    private val exporter: PostgresExporter
) : ViewModel() {

    private val yearMonthStr = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

    private val _exportMode = MutableStateFlow(ExportMode.CURRENT_MONTH)
    val exportMode: StateFlow<ExportMode> = _exportMode.asStateFlow()

    private val _sqlPreview = MutableStateFlow("")
    val sqlPreview: StateFlow<String> = _sqlPreview.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _exportEvent = MutableSharedFlow<String>()
    val exportEvent = _exportEvent.asSharedFlow()

    fun setExportMode(mode: ExportMode) {
        _exportMode.value = mode
    }

    fun generatePreview() {
        viewModelScope.launch {
            _isGenerating.value = true
            val sql = generateSql()
            _sqlPreview.value = sql
            _isGenerating.value = false
        }
    }

    fun exportAndShare(context: Context) {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val sql = if (_sqlPreview.value.isNotEmpty()) {
                    _sqlPreview.value
                } else {
                    generateSql()
                }

                // 写入文件
                val fileName = "hao_savemoney_${yearMonthStr}.sql"
                val file = File(context.cacheDir, fileName)
                file.writeText(sql, Charsets.UTF_8)

                // 通过 FileProvider 分享
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/sql"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Hao的存钱妙具 - 数据导出")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(shareIntent, "分享 SQL 数据文件"))
                _exportEvent.emit("导出成功！")
            } catch (e: Exception) {
                _exportEvent.emit("导出失败: ${e.message}")
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private suspend fun generateSql(): String {
        return when (_exportMode.value) {
            ExportMode.CURRENT_MONTH -> {
                val incomes = incomeRepository.getIncome(yearMonthStr)?.let { listOf(it) } ?: emptyList()
                val fixed = incomeRepository.getFixedExpenses(yearMonthStr)
                val expenses = expenseRepository.getExpensesByMonth(yearMonthStr)
                val budgets = budgetRepository.getMonthBudgets(yearMonthStr)
                exporter.exportToSql(incomes, fixed, expenses, budgets)
            }
            ExportMode.ALL -> {
                val incomes = incomeRepository.getAllIncome()
                val fixed = incomeRepository.getAllFixedExpenses()
                val expenses = expenseRepository.getAllExpenses()
                val budgets = budgetRepository.getAllBudgets()
                exporter.exportToSql(incomes, fixed, expenses, budgets)
            }
        }
    }
}

enum class ExportMode {
    CURRENT_MONTH,
    ALL
}
