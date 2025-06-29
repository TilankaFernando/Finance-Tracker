package com.example.personalfinancetracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BudgetSetupActivity : AppCompatActivity() {

    private lateinit var editTextBudgetAmount: EditText
    private lateinit var buttonSaveBudget: Button
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, BudgetSetupActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_setup)

        editTextBudgetAmount = findViewById(R.id.editTextBudgetAmount)
        buttonSaveBudget = findViewById(R.id.buttonSaveBudget)
        sharedPreferences = getSharedPreferences("finance_prefs", Context.MODE_PRIVATE)

        val currentBudget = sharedPreferences.getFloat("budget_amount", 0f)
        if (currentBudget > 0) {
            editTextBudgetAmount.setText(String.format("%.2f", currentBudget))
        }

        buttonSaveBudget.setOnClickListener {
            val budgetAmountStr = editTextBudgetAmount.text.toString().trim()
            if (budgetAmountStr.isNotEmpty()) {
                val budgetAmount = budgetAmountStr.toFloatOrNull()
                if (budgetAmount != null && budgetAmount >= 0) {
                    sharedPreferences.edit().putFloat("budget_amount", budgetAmount).apply()
                    Toast.makeText(this, "Budget saved", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Invalid budget amount", Toast.LENGTH_SHORT).show()
                }

            } else {
                sharedPreferences.edit().putFloat("budget_amount", 0f).apply()
                Toast.makeText(this, " Budget reset", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}