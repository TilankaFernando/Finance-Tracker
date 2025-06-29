package com.example.personalfinancetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinancetracker.models.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onDeleteClick: (Transaction) -> Unit, // Callback for delete
    private val onEditClick: (Transaction) -> Unit    // Callback for edit (if you add edit later)
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        val categoryTextView: TextView = itemView.findViewById(R.id.textViewCategory)
        val dateTextView: TextView = itemView.findViewById(R.id.textViewDate)
        val amountTextView: TextView = itemView.findViewById(R.id.textViewAmount)
        val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDelete) // Find the delete button
        val editButton: ImageButton? = itemView.findViewById(R.id.buttonEdit) // Optional edit button
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentTransaction = transactions[position]
        holder.titleTextView.text = currentTransaction.title // Assuming 'description' maps to the title
        holder.categoryTextView.text = currentTransaction.category
        holder.dateTextView.text = dateFormatter.format(currentTransaction.date)
        holder.amountTextView.text = String.format("%.2f", currentTransaction.amount)

        holder.deleteButton.setOnClickListener {
            onDeleteClick(currentTransaction) // Call the delete callback
        }

        holder.editButton?.setOnClickListener {
            onEditClick(currentTransaction)
        }
    }

    override fun getItemCount() = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}