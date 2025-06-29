package com.example.personalfinancetracker

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinancetracker.models.Transaction
import com.example.personalfinancetracker.models.TransactionType
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRANSACTION = "extra_transaction"
        const val RESULT_TRANSACTION = "result_transaction"

        fun newIntent(context: Context): Intent {
            Log.d("AddTransactionActivity", "newIntent (add) called")
            return Intent(context, AddTransactionActivity::class.java)
        }

        fun newIntent(context: Context, transaction: Transaction): Intent {
            Log.d("AddTransactionActivity", "newIntent (edit) called with transaction: $transaction")
            return Intent(context, AddTransactionActivity::class.java).apply {
                putExtra(EXTRA_TRANSACTION, transaction as java.io.Serializable)
            }
        }
    }

    private lateinit var editTextTitle: EditText
    private lateinit var editTextAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var buttonDate: Button
    private lateinit var radioGroupType: RadioGroup
    private lateinit var radioButtonIncome: RadioButton
    private lateinit var radioButtonExpense: RadioButton
    private lateinit var buttonSave: Button

    private var selectedDate = Date()
    private var transactionToEdit: Transaction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction) // Assuming your layout is named this
        Log.d("AddTransactionActivity", "onCreate called")

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextAmount = findViewById(R.id.editTextAmount)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        buttonDate = findViewById(R.id.buttonDate)
        radioGroupType = findViewById(R.id.radioGroupType)
        radioButtonIncome = findViewById(R.id.radioButtonIncome)
        radioButtonExpense = findViewById(R.id.radioButtonExpense)
        buttonSave = findViewById(R.id.buttonSave)

        val categories = resources.getStringArray(R.array.transaction_categories)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = adapter

        buttonDate.setOnClickListener {
            Log.d("AddTransactionActivity", "Date button clicked")
            showDatePickerDialog()
        }

        buttonSave.setOnClickListener {
            Log.d("AddTransactionActivity", "Save button clicked")
            saveTransaction()
        }

        transactionToEdit = intent.getSerializableExtra(EXTRA_TRANSACTION) as? Transaction
        transactionToEdit?.let {
            Log.d("AddTransactionActivity", "Editing existing transaction: $it")
            fillFields(it)
        }
    }

    private fun fillFields(transaction: Transaction) {
        editTextTitle.setText(transaction.title)
        editTextAmount.setText(transaction.amount.toString())
        val categoryIndex = (spinnerCategory.adapter as ArrayAdapter<String>).getPosition(transaction.category)
        spinnerCategory.setSelection(categoryIndex)
        selectedDate = transaction.date
        updateDateButtonText()
        when (transaction.type) {
            TransactionType.INCOME -> radioButtonIncome.isChecked = true
            TransactionType.EXPENSE -> radioButtonExpense.isChecked = true
        }
        buttonSave.text = if (transactionToEdit != null) "Update Transaction" else "Save Transaction"
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, yearSelected, monthSelected, dayOfMonthSelected ->
                calendar.set(yearSelected, monthSelected, dayOfMonthSelected)
                selectedDate = calendar.time
                updateDateButtonText()
            },
            year,
            month,
            dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun updateDateButtonText() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        buttonDate.text = sdf.format(selectedDate)
    }

    private fun saveTransaction() {
        val title = editTextTitle.text.toString().trim()
        val amountStr = editTextAmount.text.toString().trim()
        val category = spinnerCategory.selectedItem?.toString() ?: "" // Handle potential null
        val type = when {
            radioButtonIncome.isChecked -> TransactionType.INCOME
            radioButtonExpense.isChecked -> TransactionType.EXPENSE
            else -> {
                Toast.makeText(this, "Please select transaction type", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Title and amount cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val newTransaction = Transaction(
            id = transactionToEdit?.id ?: System.currentTimeMillis(),
            title = title,
            amount = amount,
            category = category,
            date = selectedDate,
            type = type
        )

        Log.d("AddTransactionActivity", "saveTransaction called with: $newTransaction") // Log before saving

        val resultIntent = Intent().apply {
            putExtra(RESULT_TRANSACTION, newTransaction as java.io.Serializable)
        }
        Log.d("AddTransactionActivity", "Setting result to RESULT_OK with transaction: $newTransaction") // Log before setResult
        setResult(RESULT_OK, resultIntent)
        finish()
        Log.d("AddTransactionActivity", "finish() called") // Log after finishing
    }
}