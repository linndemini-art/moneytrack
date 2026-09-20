package com.moneytrack.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {

    private var income = 0.0
    private var expenses = 0.0

    private lateinit var incomeText: TextView
    private lateinit var expensesText: TextView
    private lateinit var remainingText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showDashboard()
    }

    private fun showDashboard() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(247, 248, 250))
            setPadding(24, 40, 24, 24)
        }

        val title = TextView(this).apply {
            text = "Smart Money Management"
            textSize = 28f
            setTextColor(Color.BLACK)
        }

        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "Take control of your money"
            textSize = 16f
            setTextColor(Color.GRAY)
            setPadding(0, 8, 0, 30)
        }

        root.addView(subtitle)

        val month = TextView(this).apply {
            text = SimpleDateFormat(
                "MMMM yyyy",
                Locale.ENGLISH
            ).format(Date())
            textSize = 20f
            setTextColor(Color.DKGRAY)
        }

        root.addView(month)

        val cards = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 20, 0, 20)
        }

        incomeText = createCard("Income", income)
        expensesText = createCard("Expenses", expenses)
        remainingText = createCard("Remaining", income - expenses)

        cards.addView(incomeText)
        cards.addView(expensesText)
        cards.addView(remainingText)

        root.addView(cards)

        val addIncome = Button(this).apply {
            text = "+ Add Income"
            setOnClickListener {
                addIncome()
            }
        }

        root.addView(addIncome)

        val addExpense = Button(this).apply {
            text = "+ Add Expense"
            setOnClickListener {
                addExpense()
            }
        }

        root.addView(addExpense)

        setContentView(root)
    }

    private fun createCard(
        title: String,
        value: Double
    ): TextView {

        return TextView(this).apply {
            text = "$title\n${money(value)}"
            textSize = 19f
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(Color.DKGRAY)
            setBackgroundColor(Color.WHITE)
            setPadding(20, 20, 20, 20)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                90
            ).apply {
                setMargins(0, 6, 0, 6)
            }
        }
    }

    private fun addIncome() {

        val input = EditText(this).apply {
            hint = "Income (€)"
            inputType = 2
        }

        AlertDialog.Builder(this)
            .setTitle("Add Income")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->

                val value = input.text
                    .toString()
                    .toDoubleOrNull()

                if (value != null && value > 0) {
                    income += value
                    updateDashboard()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addExpense() {

        val input = EditText(this).apply {
            hint = "Expense (€)"
            inputType = 2
        }

        AlertDialog.Builder(this)
            .setTitle("Add Expense")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->

                val value = input.text
                    .toString()
                    .toDoubleOrNull()

                if (value != null && value > 0) {
                    expenses += value
                    updateDashboard()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateDashboard() {

        incomeText.text = "Income\n${money(income)}"
        expensesText.text = "Expenses\n${money(expenses)}"
        remainingText.text = "Remaining\n${money(income - expenses)}"
    }

    private fun money(value: Double): String {

        return NumberFormat
            .getCurrencyInstance(Locale.GERMANY)
            .format(value)
    }
}
