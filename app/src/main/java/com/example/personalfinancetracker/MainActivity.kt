package com.example.personalfinancetracker

import android.app.Activity
import java.io.FileReader

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinancetracker.models.Transaction
import com.example.personalfinancetracker.models.TransactionType
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewTransactions: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var fabAddTransaction: FloatingActionButton
    private lateinit var textViewIncome: TextView
    private lateinit var textViewExpenses: TextView
    private lateinit var textViewEmpty: TextView
    private lateinit var categorySpendingLayout: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var textViewBudgetStatus: TextView
    private lateinit var buttonSetBudget: Button
    private lateinit var imageButtonMenu: ImageButton
    private lateinit var bottomNavView: BottomNavigationView // Added for bottom nav

    private val addEditRequestCode = 100
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val fileName = "transactions.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize Bottom Navigation (NEW CODE STARTS HERE)
        bottomNavView = findViewById(R.id.bottom_nav_view)
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already in MainActivity
                    true
                }
                R.id.nav_analysis -> {
                    startActivity(Intent(this, AnalysisActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_import_export -> {
                    // Show export/import menu when Data tab is clicked
                    imageButtonMenu.performClick()
                    false // Keep current tab selected
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
        // (NEW CODE ENDS HERE)

        sharedPreferences = getSharedPreferences("finance_prefs", Context.MODE_PRIVATE)
        transactionRepository = TransactionRepository(this)

        textViewIncome = findViewById(R.id.textViewIncome)
        textViewExpenses = findViewById(R.id.textViewExpenses)
        textViewEmpty = findViewById(R.id.textViewEmpty)
        categorySpendingLayout = findViewById(R.id.categorySpendingLayout)

        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions)
        recyclerViewTransactions.layoutManager = LinearLayoutManager(this)

        textViewBudgetStatus = findViewById(R.id.textViewBudgetStatus)

        loadTransactions()

        fabAddTransaction = findViewById(R.id.fabAddTransaction)
        fabAddTransaction.setOnClickListener {
            val intent = AddTransactionActivity.newIntent(this)
            startActivityForResult(intent, addEditRequestCode)
        }

        buttonSetBudget = findViewById(R.id.buttonSetBudget)
        imageButtonMenu = findViewById(R.id.imageButtonMenu)

        buttonSetBudget.setOnClickListener {
            Log.d("MainActivity", "Set Budget button clicked")
            val intent = BudgetSetupActivity.newIntent(this)
            startActivity(intent)
        }

        imageButtonMenu.setOnClickListener { view -> showPopupMenu(view) }
    }

    // ALL YOUR EXISTING METHODS BELOW - NO CHANGES MADE
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view, Gravity.END)
        popupMenu.menuInflater.inflate(R.menu.menu_main, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {

                R.id.action_export_text -> {
                    exportTransactions()
                    true
                }
                R.id.action_export_json -> {
                    exportTransactions()
                    true
                }
                R.id.action_restore -> {
                    restoreTransactions()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun exportTransactions() {
        CoroutineScope(Dispatchers.IO).launch {
            val transactions = transactionRepository.getAllTransactions()
            val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
            val formattedData = gson.toJson(transactions)

            try {
                val file = File(getExternalFilesDir(null), fileName)
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(formattedData.toByteArray())
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Export successful: ${file.absolutePath}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: IOException) {
                Log.e("Export", "Error exporting transactions", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, R.string.export_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun restoreTransactions() {
        CoroutineScope(Dispatchers.IO).launch {
            val file = File(getExternalFilesDir(null), fileName)
            if (file.exists()) {
                try {
                    val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
                    val transactions: List<Transaction> = gson.fromJson(
                        FileReader(file),
                        object : TypeToken<List<Transaction>>() {}.type
                    )

                    transactionRepository.clearAllTransactions()
                    transactions.forEach { transactionRepository.saveTransaction(it) }

                    loadTransactions()
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.restore_successful,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("Restore", "Error during restore: ${e.message}", e)
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.restore_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.no_backup_file_found,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun updateBudgetStatus(transactions: List<Transaction>, textViewBudgetStatus: TextView) {
        val budgetAmount = sharedPreferences.getFloat("budget_amount", 0f).toDouble()
        Log.d("BudgetStatus", "Budget Amount at start: $budgetAmount")
        val totalExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        Log.d("BudgetStatus", "Total Expenses: $totalExpenses")

        val currencySymbol = sharedPreferences.getString("currency", "USD") ?: "USD"

        if (budgetAmount > 0) {
            val remainingBudget = budgetAmount - totalExpenses
            val budgetStatusText: String
            val budgetStatusColor: Int

            if (remainingBudget >= 0) {
                budgetStatusText = String.format(
                    "Budget Remaining: %s%.2f",
                    getCurrencySymbol(currencySymbol),
                    remainingBudget
                )
                budgetStatusColor = ContextCompat.getColor(this, R.color.budget_remaining_color)
            } else {
                budgetStatusText = String.format(
                    "Budget Overspent: %s%.2f",
                    getCurrencySymbol(currencySymbol),
                    -remainingBudget
                )
                budgetStatusColor = ContextCompat.getColor(this, R.color.budget_overspent_color)
                showBudgetAlert(-remainingBudget, currencySymbol)
            }

            textViewBudgetStatus.text = budgetStatusText
            textViewBudgetStatus.setTextColor(budgetStatusColor)
            textViewBudgetStatus.visibility = View.VISIBLE
        } else {
            textViewBudgetStatus.visibility = View.GONE
        }
    }

    private fun showBudgetAlert(overspentAmount: Double, currencySymbol: String) {
        AlertDialog.Builder(this)
            .setTitle("Budget Warning!")
            .setMessage("You've exceeded your budget by ${getCurrencySymbol(currencySymbol)}${"%.2f".format(overspentAmount)}")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Adjust Budget") { dialog, _ ->
                startActivity(Intent(this, BudgetSetupActivity::class.java))
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun loadTransactions() {
        CoroutineScope(Dispatchers.IO).launch {
            val transactions = transactionRepository.getAllTransactions().sortedByDescending { it.date }
            runOnUiThread {
                transactionAdapter = TransactionAdapter(
                    transactions,
                    onDeleteClick = { transactionToDelete ->
                        CoroutineScope(Dispatchers.IO).launch {
                            transactionRepository.deleteTransaction(transactionToDelete.id)
                            loadTransactions()
                            runOnUiThread {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Transaction deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    onEditClick = { transactionToEdit ->
                        val intent = AddTransactionActivity.newIntent(this@MainActivity, transactionToEdit)
                        startActivityForResult(intent, addEditRequestCode)
                    }
                )
                recyclerViewTransactions.adapter = transactionAdapter
                updateSummary(transactions)
                updateCategorySpending(transactions)
                updateBudgetStatus(transactions, textViewBudgetStatus)
                checkIfListEmpty(transactions)
            }
        }
    }

    private fun checkIfListEmpty(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            recyclerViewTransactions.visibility = View.GONE
            textViewEmpty.visibility = View.VISIBLE
        } else {
            recyclerViewTransactions.visibility = View.VISIBLE
            textViewEmpty.visibility = View.GONE
        }
    }

    private fun updateSummary(transactions: List<Transaction>) {
        var totalIncome = 0.0
        var totalExpenses = 0.0

        for (transaction in transactions) {
            if (transaction.type == TransactionType.INCOME) {
                totalIncome += transaction.amount
            } else {
                totalExpenses += transaction.amount
            }
        }

        val currencySymbol = sharedPreferences.getString("currency", "USD") ?: "USD"

        val formattedIncome = String.format("Income: +%s%.2f", getCurrencySymbol(currencySymbol), totalIncome)
        val formattedExpenses = String.format("Expenses: -%s%.2f", getCurrencySymbol(currencySymbol), totalExpenses)

        textViewIncome.text = formattedIncome
        textViewExpenses.text = formattedExpenses
    }

    private fun updateCategorySpending(transactions: List<Transaction>) {
        val categoryExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { (_, amount) -> amount }

        categorySpendingLayout.removeAllViews()

        if (categoryExpenses.isNotEmpty()) {
            val currencySymbol = sharedPreferences.getString("currency", "USD") ?: "USD"
            for ((category, amount) in categoryExpenses) {
                val categoryTextView = TextView(this)
                categoryTextView.text =
                    String.format("%s: %s%.2f", category, getCurrencySymbol(currencySymbol), amount)
                categoryTextView.textSize = 16f
                categoryTextView.setTextColor(resources.getColor(R.color.dark_text))
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 8)
                categoryTextView.layoutParams = params
                categorySpendingLayout.addView(categoryTextView)
            }
        } else {
            val noSpendingTextView = TextView(this)
            noSpendingTextView.text = getString(R.string.no_expenses_recorded)
            noSpendingTextView.setTextColor(resources.getColor(R.color.secondary_text))
            categorySpendingLayout.addView(noSpendingTextView)
        }
    }

    private fun getCurrencySymbol(currencyCode: String): String {
        return when (currencyCode) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "LKR" -> "රු"
            else -> currencyCode
        }
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
        // Highlight home tab when returning (NEW CODE)
        bottomNavView.selectedItemId = R.id.nav_home
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == addEditRequestCode && resultCode == Activity.RESULT_OK) {
            val newTransaction =
                data?.getSerializableExtra(AddTransactionActivity.RESULT_TRANSACTION) as? Transaction
            newTransaction?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    transactionRepository.saveTransaction(it)
                    loadTransactions()
                }
            }
        }
    }
}