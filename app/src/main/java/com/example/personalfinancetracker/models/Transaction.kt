package com.example.personalfinancetracker.models


import java.io.Serializable
import java.util.Date

data class Transaction(
    val id: Long = System.currentTimeMillis(), // Simple unique ID
    val title: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val type: TransactionType // Income or Expense


) : Serializable

enum class TransactionType : Serializable {
    INCOME, EXPENSE
}