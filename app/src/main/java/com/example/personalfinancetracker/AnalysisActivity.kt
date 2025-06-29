package com.example.personalfinancetracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinancetracker.models.TransactionType
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import android.graphics.Typeface
import com.github.mikephil.charting.formatter.PercentFormatter

class AnalysisActivity : AppCompatActivity() {

    private lateinit var textViewTotalExpenses: TextView
    private lateinit var categoryAnalysisLayout: LinearLayout
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var pieChart: PieChart

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, AnalysisActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        // Bottom navigation setup
        bottomNavView = findViewById(R.id.bottom_nav_view)
        bottomNavView.selectedItemId = R.id.nav_analysis
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_analysis -> true
                R.id.nav_import_export -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    false
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

        textViewTotalExpenses = findViewById(R.id.textViewTotalExpensesAnalysis)
        categoryAnalysisLayout = findViewById(R.id.categoryAnalysisLayout)
        pieChart = findViewById(R.id.pieChart)

        transactionRepository = TransactionRepository(this)
        sharedPreferences = getSharedPreferences("finance_prefs", Context.MODE_PRIVATE)

        loadAnalysis()
    }

    private fun loadAnalysis() {
        val transactions = transactionRepository.getAllTransactions()
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }

        val totalExpenses = expenses.sumOf { it.amount }
        val currencySymbol = sharedPreferences.getString("currency", "USD") ?: "USD"
        textViewTotalExpenses.text = String.format("Total Expenses: %s%.2f", getCurrencySymbol(currencySymbol), totalExpenses)

        val categoryExpenses = expenses
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { (_, amount) -> amount }

        categoryAnalysisLayout.removeAllViews()

        if (categoryExpenses.isNotEmpty()) {
            val pieEntries = ArrayList<PieEntry>()
            for ((category, amount) in categoryExpenses) {
                // Add to list
                val categoryTextView = TextView(this)
                categoryTextView.text = String.format("%s: %s%.2f", category, getCurrencySymbol(currencySymbol), amount)
                categoryTextView.textSize = 18f // Increased text size
                categoryTextView.setTextColor(resources.getColor(R.color.white)) // White text color
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 8)
                categoryTextView.layoutParams = params
                categoryAnalysisLayout.addView(categoryTextView)

                // Add to pie chart
                pieEntries.add(PieEntry(amount.toFloat(), category))
            }

            // Create pie chart
            val dataSet = PieDataSet(pieEntries, "Category Wise Spending")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

            // Set value formatter to show percentages and values
            val formatter = PercentFormatter(pieChart)
            dataSet.valueFormatter = formatter

            // Increase the size of the price amounts
            dataSet.valueTextSize = 16f  // Increased size for values (price amounts)

            // Create pie data
            val pieData = PieData(dataSet)

            pieChart.data = pieData
            pieChart.description.isEnabled = false
            pieChart.centerText = "Expenses"
            pieChart.setEntryLabelTextSize(13f) // Set the label text size (category names)
            pieChart.setEntryLabelColor(resources.getColor(R.color.black))
            pieChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD) // Set label color to black
            pieChart.animateY(1000)

            // Set the legend text color to white and increase the text size
            val legend = pieChart.legend
            legend.textColor = resources.getColor(R.color.white)
            legend.textSize = 14f // Increased text size for the legend

            pieChart.invalidate()

        } else {
            val noSpendingTextView = TextView(this)
            noSpendingTextView.text = "No expenses recorded yet."
            noSpendingTextView.textSize = 50f
            noSpendingTextView.setTextColor(resources.getColor(R.color.white))
            categoryAnalysisLayout.addView(noSpendingTextView)

            pieChart.clear()
            pieChart.centerText = "No Data"
            pieChart.invalidate()
        }
    }

    private fun getCurrencySymbol(currencyCode: String): String {
        return when (currencyCode) {
            "EUR" -> "€"
            "GBP" -> "£"
            "LKR" -> "රු"
            "INR" -> "₹"
            "AUD", "CAD", "USD" -> "$"
            else -> currencyCode
        }
    }
}
