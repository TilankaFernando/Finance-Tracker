package com.example.personalfinancetracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {

    private lateinit var spinnerCurrency: Spinner
    private lateinit var sharedPreferences: SharedPreferences
    private val currencies = arrayOf("USD", "EUR", "GBP", "LKR", "INR", "AUD", "CAD")
    private lateinit var bottomNavView: BottomNavigationView // Added for bottom nav

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize Bottom Navigation (NEW CODE)
        bottomNavView = findViewById(R.id.bottom_nav_view)
        bottomNavView.selectedItemId = R.id.nav_settings
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_analysis -> {
                    startActivity(Intent(this, AnalysisActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_import_export -> {
                    // Return to MainActivity to show export/import menu
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    false
                }
                R.id.nav_settings -> true // Already in SettingsActivity
                else -> false
            }
        }
        // (END OF NEW CODE)

        spinnerCurrency = findViewById(R.id.spinnerCurrency)
        sharedPreferences = getSharedPreferences("finance_prefs", Context.MODE_PRIVATE)

        val currencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, currencies)
        spinnerCurrency.adapter = currencyAdapter

        val currentCurrency = sharedPreferences.getString("currency", "USD")
        currentCurrency?.let {
            val position = currencies.indexOf(it)
            if (position != -1) {
                spinnerCurrency.setSelection(position)
            }
        }

        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCurrency = currencies[position]
                sharedPreferences.edit().putString("currency", selectedCurrency).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
}