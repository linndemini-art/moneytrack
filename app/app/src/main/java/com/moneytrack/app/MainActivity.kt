package com.moneytrack.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : Activity() {

    private lateinit var content: LinearLayout
    private lateinit var nav: LinearLayout

    private val prefs by lazy {
        getSharedPreferences("moneytrack_data", MODE_PRIVATE)
    }

    private val expenses = mutableListOf<Expense>()
    private var income = 0.0

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
        val amount: Double,
        val date: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        buildApp()
        showDashboard()
    }

    private fun buildApp() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(247, 248, 250))
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(this).apply {
            addView(
                content,
                ScrollView.LayoutParams(
                    ScrollView.LayoutParams.MATCH_PARENT,
                    ScrollView.LayoutParams.WRAP_CONTENT
                )
            )
        }

        nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding(4, 8, 4, 8)
        }

        addNavButton("⌂", "Home") { showDashboard() }
        addNavButton("€", "Expenses") { showExpenses() }
        addNavButton("◉", "Statistics") { showStatistics() }
        addNavButton("⚙", "Settings") { showSettings() }

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(
            nav,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                72
            )
        )

        setContentView(root)
    }

    private fun addNavButton(
        icon: String,
        label: String,
        action: () -> Unit
    ) {
        val button = TextView(this).apply {
            text = "$icon\n$label"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(60, 60, 65))
            setPadding(8, 4, 8, 4)
            setOnClickListener { action() }
        }

        nav.addView(
            button,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )
    }

    private fun showDashboard() {
        content.removeAllViews()

        content.addView(header("Smart Money Management"))
        content.addView(subtitle("Take control of your money"))

        content.addView(sectionTitle("Overview"))

        val month = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
            .format(Date())

        content.addView(cardText("Current month", month))

        val totalExpenses = expenses.sumOf { it.amount }
        val remaining = income - totalExpenses
        val percentage = if (income > 0) {
            ((totalExpenses / income) * 100).roundToInt()
        } else {
            0
        }

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        stats.addView(
            statCard("Income", money(income)),
            weightParams()
        )

        stats.addView(
            statCard("Expenses", money(totalExpenses)),
            weightParams()
        )

        stats.addView(
            statCard("Remaining", money(remaining)),
            weightParams()
        )

        content.addView(stats)

        content.addView(sectionTitle("Spending"))

        val progress = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            progress = percentage.coerceIn(0, 100)
        }

        content.addView(
            progress,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                18
            ).apply {
                setMargins(24, 8, 24, 8)
            }
        )

        val percentageText = TextView(this).apply {
            text = "$percentage% of your income spent"
            textSize = 14f
            setTextColor(Color.GRAY)
            setPadding(24, 4, 24, 20)
        }

        content.addView(percentageText)

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val addIncome = Button(this).apply {
            text = "+ Income"
            setOnClickListener { showAddIncome() }
        }

        val addExpense = Button(this).apply {
            text = "+ Expense"
            setOnClickListener { showAddExpense() }
        }

        buttons.addView(addIncome, weightParams())
        buttons.addView(addExpense, weightParams())

        content.addView(buttons)

        content.addView(sectionTitle("Recent Transactions"))

        if (expenses.isEmpty()) {
            content.addView(
                cardText(
                    "No expenses yet",
                    "Add your first expense to start tracking."
                )
            )
        } else {
            for (expense in expenses.asReversed().take(5)) {
                content.addView(transactionView(expense))
            }
        }
    }

    private fun showExpenses() {
        content.removeAllViews()

        content.addView(header("Expenses"))
        content.addView(subtitle("Track and manage your spending"))

        val addButton = Button(this).apply {
            text = "+ Add Expense"
            setOnClickListener { showAddExpense() }
        }

        content.addView(addButton)

        if (expenses.isEmpty()) {
            content.addView(
                cardText(
                    "No expenses",
                    "Your expense list is currently empty."
                )
            )
            return
        }

        for (expense in expenses.asReversed()) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(20, 18, 12, 18)
                setBackgroundColor(Color.WHITE)
            }

            val info = TextView(this).apply {
                text = "${expense.description}\n${expense.category}\n${expense.date}"
                textSize = 15f
                setTextColor(Color.DKGRAY)
            }

            row.addView(
                info,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val amount = TextView(this).apply {
                text = money(expense.amount)
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
            }

            row.addView(amount)

            val delete = Button(this).apply {
                text = "Delete"
                setOnClickListener {
                    expenses.remove(expense)
                    saveData()
                    showExpenses()
                }
            }

            row.addView(delete)

            content.addView(
                row,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 6, 16, 6)
                }
            )
        }
    }

    private fun showAddExpense() {
        content.removeAllViews()

        content.addView(header("Add Expense"))
        content.addView(subtitle("Record where your money went"))

        val description = EditText(this).apply {
            hint = "Description"
            setSingleLine()
        }

        content.addView(description, fieldParams())

        val spinner = Spinner(this)

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        content.addView(spinner, fieldParams())

        val amount = EditText(this).apply {
            hint = "Amount (€)"
            inputType = 2 or 8192
            setSingleLine()
        }

        content.addView(amount, fieldParams())

        val save = Button(this).apply {
            text = "Save Expense"

            setOnClickListener {
                val desc = description.text.toString().trim()
                val value = amount.text.toString().toDoubleOrNull()

                if (desc.isEmpty()) {
                    toast("Enter a description")
                    return@setOnClickListener
                }

                if (value == null || value <= 0) {
                    toast("Enter a valid amount")
                    return@setOnClickListener
                }

                val date = SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.ENGLISH
                ).format(Date())

                expenses.add(
                    Expense(
                        desc,
                        spinner.selectedItem.toString(),
                        value,
                        date
                    )
                )

                saveData()
                toast("Expense saved")
                showDashboard()
            }
        }

        content.addView(save)

        val cancel = Button(this).apply {
            text = "Cancel"
            setOnClickListener { showDashboard() }
        }

        content.addView(cancel)
    }

    private fun showAddIncome() {
        content.removeAllViews()

        content.addView(header("Income"))
        content.addView(subtitle("Set your monthly income"))

        val input = EditText(this).apply {
            hint = "Income (€)"
            inputType = 2 or 8192
            setSingleLine()
        }

        input.setText(
            if (income > 0) income.toString() else ""
        )

        content.addView(input, fieldParams())

        val save = Button(this).apply {
            text = "Save Income"

            setOnClickListener {
                val value = input.text.toString().toDoubleOrNull()

                if (value == null || value < 0) {
                    toast("Enter a valid income")
                    return@setOnClickListener
                }

                income = value
                saveData()
                toast("Income saved")
                showDashboard()
            }
        }

        content.addView(save)

        val cancel = Button(this).apply {
            text = "Cancel"
            setOnClickListener { showDashboard() }
        }

        content.addView(cancel)
    }

    private fun showStatistics() {
        content.removeAllViews()

        content.addView(header("Statistics"))
        content.addView(subtitle("Understand your spending"))

        val total = expenses.sumOf { it.amount }

        content.addView(
            statCard(
                "Total spending",
                money(total)
            )
        )

        content.addView(
            statCard(
                "Number of expenses",
                expenses.size.toString()
            )
        )

        val average = if (expenses.isNotEmpty()) {
            total / expenses.size
        } else {
            0.0
        }

        content.addView(
            statCard(
                "Average expense",
                money(average)
            )
        )

        content.addView(sectionTitle("By Category"))

        for (category in categories) {
            val categoryTotal = expenses
                .filter { it.category == category }
                .sumOf { it.amount }

            if (categoryTotal > 0) {
                content.addView(
                    cardText(
                        category,
                        money(categoryTotal)
                    )
                )
            }
        }
    }

    private fun showSettings() {
        content.removeAllViews()

        content.addView(header("Settings"))
        content.addView(subtitle("Manage your MoneyTrack data"))

        content.addView(
            cardText(
                "Data storage",
                "Your data is stored locally on this device."
            )
        )

        val clear = Button(this).apply {
            text = "Clear All Data"

            setOnClickListener {
                expenses.clear()
                income = 0.0
                saveData()
                toast("All data cleared")
                showDashboard()
            }
        }

        content.addView(clear)

        content.addView(
            cardText(
                "Smart Money Management",
                "Version 1.0"
            )
        )
    }

    private fun transactionView(expense: Expense): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 16, 18, 16)
            setBackgroundColor(Color.WHITE)

            val info = TextView(this@MainActivity).apply {
                text = "${expense.description}\n${expense.category}"
                textSize = 15f
                setTextColor(Color.DKGRAY)
            }

            addView(
                info,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val price = TextView(this@MainActivity).apply {
                text = money(expense.amount)
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
            }

            addView(price)
        }.also {
            it.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 5, 16, 5)
            }
        }
    }

    private fun header(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(25, 25, 30))
            setPadding(24, 30, 24, 4)
        }
    }

    private fun subtitle(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.GRAY)
            setPadding(24, 4, 24, 24)
        }
    }

    private fun sectionTitle(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(35, 35, 40))
            setPadding(24, 24, 24, 12)
        }
    }

    private fun cardText(
        title: String,
        value: String
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(20, 18, 20, 18)

            val t = TextView(this@MainActivity).apply {
                text = title
                textSize = 14f
                setTextColor(Color.GRAY)
            }

            val v = TextView(this@MainActivity).apply {
                text = value
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.rgb(30, 30, 35))
                setPadding(0, 5, 0, 0)
            }

            addView(t)
            addView(v)
        }.also {
            it.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 6, 16, 6)
            }
        }
    }

    private fun statCard(
        title: String,
        value: String
    ): LinearLayout {
        return cardText(title, value)
    }

    private fun fieldParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 6, 16, 6)
        }
    }

    private fun weightParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ).apply {
            setMargins(5, 5, 5, 5)
        }
    }

    private fun money(value: Double): String {
        return NumberFormat
            .getCurrencyInstance(Locale.GERMANY)
            .format(value)
    }

    private fun toast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun saveData() {
        val editor = prefs.edit()

        editor.putString("income", income.toString())

        val data = expenses.joinToString("|||") {
            "${it.description}|||${it.category}|||${it.amount}|||${it.date}"
        }

        editor.putString("expenses", data)
        editor.apply()
    }

    private fun loadData() {
        income = prefs.getString("income", "0")
            ?.toDoubleOrNull() ?: 0.0

        val data = prefs.getString("expenses", "") ?: ""

        if (data.isEmpty()) return

        val records = data.split("|||")

        var i = 0

        while (i + 3 < records.size) {
            expenses.add(
                Expense(
                    records[i],
                    records[i + 1],
                    records[i + 2].toDoubleOrNull() ?: 0.0,
                    records[i + 3]
                )
            )

            i += 4
        }
    }
}
