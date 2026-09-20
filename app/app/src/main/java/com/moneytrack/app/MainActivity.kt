package com.moneytrack.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import java.text.NumberFormat
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var incomeInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var expensesContainer: LinearLayout
    private lateinit var monthlyText: TextView
    private lateinit var remainingText: TextView

    private var income = 0.0
    private val expenses = mutableListOf<Expense>()

    private val categories = arrayOf(
        "Housing",
        "Utilities",
        "Food",
        "Fuel / Transport",
        "Car",
        "Phone / Internet",
        "Subscriptions",
        "Shopping",
        "Health",
        "Entertainment",
        "Travel",
        "Other"
    )

    data class Expense(
        val description: String,
        val category: String,
        val amount: Double
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildInterface()
    }

    private fun buildInterface() {
        val root = ScrollView(this)

        val main = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 40, 32, 40)
            setBackgroundColor(Color.rgb(247, 248, 250))
        }

        val title = TextView(this).apply {
            text = "Smart Money Management"
            textSize = 26f
            setTextColor(Color.rgb(25, 25, 30))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val subtitle = TextView(this).apply {
            text = "Think smarter. Manage your money."
            textSize = 15f
            setTextColor(Color.GRAY)
            setPadding(0, 8, 0, 28)
        }

        main.addView(title)
        main.addView(subtitle)

        main.addView(sectionTitle("Monthly Income"))

        incomeInput = EditText(this).apply {
            hint = "Enter income (€)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setSingleLine()
        }

        main.addView(incomeInput, fieldParams())

        val saveIncomeButton = Button(this).apply {
            text = "Save Income"
            setOnClickListener {
                val value = incomeInput.text.toString().toDoubleOrNull()

                if (value != null && value >= 0) {
                    income = value
                    updateTotals()

                    Toast.makeText(
                        this@MainActivity,
                        "Income saved",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Enter a valid income amount",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        main.addView(saveIncomeButton)

        main.addView(sectionTitle("Overview"))

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        monthlyText = statView("Monthly\n€0.00")
        remainingText = statView("Remaining\n€0.00")

        stats.addView(monthlyText, weightParams())
        stats.addView(remainingText, weightParams())

        main.addView(stats)

        main.addView(sectionTitle("Add Expense"))

        descriptionInput = EditText(this).apply {
            hint = "Description"
            setSingleLine()
        }

        main.addView(descriptionInput, fieldParams())

        categorySpinner = Spinner(this)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        categorySpinner.adapter = adapter
        main.addView(categorySpinner, fieldParams())

        amountInput = EditText(this).apply {
            hint = "Amount (€)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setSingleLine()
        }

        main.addView(amountInput, fieldParams())

        val addButton = Button(this).apply {
            text = "+ Add Expense"
            setOnClickListener {
                addExpense()
            }
        }

        main.addView(addButton)

        main.addView(sectionTitle("Recent Expenses"))

        expensesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        main.addView(expensesContainer)

        root.addView(main)
        setContentView(root)
    }

    private fun addExpense() {
        val description = descriptionInput.text.toString().trim()
        val category = categorySpinner.selectedItem.toString()
        val amount = amountInput.text.toString().toDoubleOrNull()

        if (description.isEmpty()) {
            Toast.makeText(this, "Enter a description", Toast.LENGTH_SHORT).show()
            return
        }

        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        expenses.add(
            Expense(
                description,
                category,
                amount
            )
        )

        descriptionInput.text.clear()
        amountInput.text.clear()

        updateTotals()
        refreshExpenses()

        Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
    }

    private fun refreshExpenses() {
        expensesContainer.removeAllViews()

        for (index in expenses.indices.reversed()) {
            val expense = expenses[index]

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(12, 18, 12, 18)
                setBackgroundColor(Color.WHITE)
            }

            val information = TextView(this).apply {
                text = "${expense.description}\n${expense.category}"
                textSize = 16f
                setTextColor(Color.DKGRAY)
            }

            val price = TextView(this).apply {
                text = money(expense.amount)
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER_VERTICAL
            }

            val delete = Button(this).apply {
                text = "Delete"
                setOnClickListener {
                    expenses.removeAt(index)
                    updateTotals()
                    refreshExpenses()
                }
            }

            row.addView(
                information,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            row.addView(price)
            row.addView(delete)

            val separator = Space(this).apply {
                minimumHeight = 8
            }

            expensesContainer.addView(row)
            expensesContainer.addView(separator)
        }
    }

    private fun updateTotals() {
        val total = expenses.sumOf { it.amount }
        val remaining = income - total

        monthlyText.text = "Monthly\n${money(total)}"
        remainingText.text = "Remaining\n${money(remaining)}"
    }

    private fun money(value: Double): String {
        return NumberFormat
            .getCurrencyInstance(Locale.GERMANY)
            .format(value)
    }

    private fun sectionTitle(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(35, 35, 40))
            setPadding(0, 28, 0, 12)
        }
    }

    private fun statView(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 17f
            setTextColor(Color.rgb(35, 35, 40))
            setBackgroundColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(10, 24, 10, 24)
        }
    }

    private fun fieldParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = 8
        }
    }

    private fun weightParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ).apply {
            marginEnd = 6
        }
    }
}
