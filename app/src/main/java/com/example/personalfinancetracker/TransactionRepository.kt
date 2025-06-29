package com.example.personalfinancetracker

import android.content.Context
import android.content.SharedPreferences
import com.example.personalfinancetracker.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class TransactionRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("finance_data", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun saveTransaction(transaction: Transaction) {
        val transactions = getAllTransactions().toMutableList()
        val existingIndex = transactions.indexOfFirst { it.id == transaction.id }
        if (existingIndex != -1) {
            transactions[existingIndex] = transaction
        } else {
            transactions.add(transaction)
        }
        saveTransactions(transactions)
    }

     fun deleteTransaction(transactionId: Long) {
        val transactions = getAllTransactions().toMutableList()
        transactions.removeAll { it.id == transactionId }
        saveTransactions(transactions)
    }
    fun clearAllTransactions() {
        saveTransactions(emptyList())
    }


    fun getAllTransactions(): List<Transaction> {
        val transactionsJson = sharedPreferences.getString("transactions", null)
        return if (transactionsJson.isNullOrEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<Transaction>>() {}.type
            gson.fromJson(transactionsJson, type) ?: emptyList()
        }
    }

    private fun saveTransactions(transactions: List<Transaction>) {
        val transactionsJson = gson.toJson(transactions)
        sharedPreferences.edit().putString("transactions", transactionsJson).apply()
    }
}