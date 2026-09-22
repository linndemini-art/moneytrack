package com.moneytrack.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import android.content.Context
import android.text.InputType
import org.json.JSONArray
import org.json.JSONObject
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
    private lateinit var incomeDisplay: TextView

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

    private val prefs by lazy {
        getSharedPreferences(
            "moneytrack_data",
            Context.MODE_PRIVATE
        )
    }

    // DARK PREMIUM COLORS

    private val backgroundColor =
        Color.rgb(8, 13, 20)

    private val cardColor =
        Color.rgb(17, 25, 36)

    private val inputColor =
        Color.rgb(23, 33, 47)

    private val blue =
        Color.rgb(59, 130, 246)

    private val blueLight =
        Color.rgb(96, 165, 250)

    private val green =
        Color.rgb(34, 197, 94)

    private val red =
        Color.rgb(248, 113, 113)

    private val white =
        Color.rgb(248, 250, 252)

    private val secondary =
        Color.rgb(148, 163, 184)

    private val border =
        Color.rgb(35, 48, 65)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildInterface()
        loadData()
        updateTotals()
        refreshExpenses()
    }

    private fun buildInterface() {

        val root = ScrollView(this).apply {
            setBackgroundColor(backgroundColor)
        }

        val main = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 28, 22, 40)
            setBackgroundColor(backgroundColor)
        }

        main.addView(createHeader())

        main.addView(createMonthCard())

        main.addView(
            sectionTitle("Overview")
        )

        main.addView(createStats())

        main.addView(
            sectionTitle("Monthly Income")
        )

        main.addView(createIncomeCard())

        main.addView(
            sectionTitle("Add Expense")
        )

        main.addView(createExpenseCard())

        main.addView(
            sectionTitle("Recent Expenses")
        )

        expensesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        main.addView(expensesContainer)

        root.addView(main)

        setContentView(root)
    }

    private fun createHeader(): View {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 22)
        }

        val logo = ImageView(this).apply {
            setImageResource(
                com.moneytrack.app.R.drawable.smart_money_logo
            )
            scaleType =
                ImageView.ScaleType.CENTER_INSIDE
        }

        container.addView(
            logo,
            LinearLayout.LayoutParams(
                62,
                62
            )
        )

        val textContainer =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setPadding(14, 0, 0, 0)
            }

        val title = TextView(this).apply {
            text = "Smart Money"
            textSize = 25f
            setTextColor(white)
            setTypeface(
                null,
                Typeface.BOLD
            )
        }

        val subtitle = TextView(this).apply {
            text = "Take control of your money"
            textSize = 14f
            setTextColor(secondary)
            setPadding(0, 4, 0, 0)
        }

        textContainer.addView(title)
        textContainer.addView(subtitle)

        container.addView(
            textContainer,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        return container
    }

    private fun createMonthCard(): View {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 18, 20, 18)
            background = roundedBackground(
                cardColor,
                18,
                border
            )
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val month = TextView(this).apply {
            text = "Your Money"
            textSize = 18f
            setTextColor(white)
            setTypeface(
                null,
                Typeface.BOLD
            )
        }

        row.addView(
            month,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val status = TextView(this).apply {
            text = "● OFFLINE"
            textSize = 11f
            setTextColor(green)
            setTypeface(
                null,
                Typeface.BOLD
            )
        }

        row.addView(status)

        card.addView(row)

        val description = TextView(this).apply {
            text = "Your personal financial overview"
            textSize = 13f
            setTextColor(secondary)
            setPadding(0, 7, 0, 0)
        }

        card.addView(description)

        return card
    }

    private fun createStats(): View {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val monthlyCard = statCard(
            "Expenses",
            blueLight
        )

        monthlyText = monthlyCard

        val remainingCard = statCard(
            "Remaining",
            green
        )

        remainingText = remainingCard

        row.addView(
            monthlyCard,
            weightParams()
        )

        row.addView(
            Space(this),
            LinearLayout.LayoutParams(
                10,
                1
            )
        )

        row.addView(
            remainingCard,
            weightParams()
        )

        return row
    }

    private fun statCard(
        label: String,
        color: Int
    ): TextView {

        return TextView(this).apply {
            text = "$label\n€0.00"
            textSize = 16f
            setTextColor(color)
            setTypeface(
                null,
                Typeface.BOLD
            )
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                14,
                16,
                14,
                16
            )
            background = roundedBackground(
                cardColor,
                16,
                border
            )
        }
    }

    private fun createIncomeCard(): View {

        val container =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setPadding(
                    20,
                    20,
                    20,
                    20
                )
                background =
                    roundedBackground(
                        cardColor,
                        18,
                        border
                    )
            }

        val top =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.HORIZONTAL
                gravity =
                    Gravity.CENTER_VERTICAL
            }

        val label = TextView(this).apply {
            text = "Monthly income"
            textSize = 14f
            setTextColor(secondary)
        }

        top.addView(
            label,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        incomeDisplay =
            TextView(this).apply {
                text = "€0.00"
                textSize = 24f
                setTextColor(white)
                setTypeface(
                    null,
                    Typeface.BOLD
                )
            }

        top.addView(incomeDisplay)

        container.addView(top)

        incomeInput =
            createInput("Enter income (€)")

        container.addView(
            incomeInput,
            inputParams()
        )

        val button =
            createButton(
                "Save Income",
                blue
            )

        button.setOnClickListener {

            val value =
                incomeInput.text
                    .toString()
                    .toDoubleOrNull()

            if (
                value != null &&
                value >= 0
            ) {

                income = value

                saveData()
                updateTotals()

                Toast.makeText(
                    this,
                    "Income saved",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Enter a valid income amount",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        container.addView(
            button,
            buttonParams()
        )

        return container
    }

    private fun createExpenseCard(): View {

        val container =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setPadding(
                    20,
                    20,
                    20,
                    20
                )
                background =
                    roundedBackground(
                        cardColor,
                        18,
                        border
                    )
            }

        descriptionInput =
            createInput("Description")

        container.addView(
            descriptionInput,
            inputParams()
        )

        categorySpinner =
            Spinner(this).apply {
                background =
                    roundedBackground(
                        inputColor,
                        12,
                        border
                    )
            }

        val adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
            )

        categorySpinner.adapter =
            adapter

        container.addView(
            categorySpinner,
            spinnerParams()
        )

        amountInput =
            createInput("Amount (€)")

        container.addView(
            amountInput,
            inputParams()
        )

        val button =
            createButton(
                "+  Add Expense",
                blue
            )

        button.setOnClickListener {
            addExpense()
        }

        container.addView(
            button,
            buttonParams()
        )

        return container
    }

    private fun addExpense() {

        val description =
            descriptionInput.text
                .toString()
                .trim()

        val category =
            categorySpinner
                .selectedItem
                .toString()

        val amount =
            amountInput.text
                .toString()
                .toDoubleOrNull()

        if (description.isEmpty()) {

            Toast.makeText(
                this,
                "Enter a description",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (
            amount == null ||
            amount <= 0
        ) {

            Toast.makeText(
                this,
                "Enter a valid amount",
                Toast.LENGTH_SHORT
            ).show()

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

        saveData()
        updateTotals()
        refreshExpenses()

        Toast.makeText(
            this,
            "Expense added",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun refreshExpenses() {

        expensesContainer.removeAllViews()

        if (expenses.isEmpty()) {

            val empty =
                TextView(this).apply {
                    text =
                        "No expenses yet\n\nAdd your first expense above."
                    textSize = 14f
                    setTextColor(secondary)
                    gravity = Gravity.CENTER
                    setPadding(
                        20,
                        30,
                        20,
                        30
                    )
                    background =
                        roundedBackground(
                            cardColor,
                            18,
                            border
                        )
                }

            expensesContainer.addView(empty)

            return
        }

        for (
            index in
            expenses.indices.reversed()
        ) {

            val expense =
                expenses[index]

            val row =
                LinearLayout(this).apply {
                    orientation =
                        LinearLayout.HORIZONTAL
                    gravity =
                        Gravity.CENTER_VERTICAL
                    setPadding(
                        14,
                        14,
                        12,
                        14
                    )
                    background =
                        roundedBackground(
                            cardColor,
                            16,
                            border
                        )
                }

            val icon =
                TextView(this).apply {
                    text = "€"
                    textSize = 17f
                    gravity = Gravity.CENTER
                    setTextColor(blueLight)
                    setTypeface(
                        null,
                        Typeface.BOLD
                    )
                    background =
                        roundedBackground(
                            Color.rgb(
                                20,
                                45,
                                78
                            ),
                            12,
                            Color.TRANSPARENT
                        )
                }

            row.addView(
                icon,
                LinearLayout.LayoutParams(
                    44,
                    44
                )
            )

            val information =
                LinearLayout(this).apply {
                    orientation =
                        LinearLayout.VERTICAL
                    setPadding(
                        12,
                        0,
                        8,
                        0
                    )
                }

            val description =
                TextView(this).apply {
                    text =
                        expense.description
                    textSize = 15f
                    setTextColor(white)
                    setTypeface(
                        null,
                        Typeface.BOLD
                    )
                }

            val category =
                TextView(this).apply {
                    text =
                        expense.category
                    textSize = 12f
                    setTextColor(
                        secondary
                    )
                    setPadding(
                        0,
                        3,
                        0,
                        0
                    )
                }

            information.addView(
                description
            )

            information.addView(
                category
            )

            row.addView(
                information,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val right =
                LinearLayout(this).apply {
                    orientation =
                        LinearLayout.VERTICAL
                    gravity = Gravity.END
                }

            val price =
                TextView(this).apply {
                    text =
                        money(
                            expense.amount
                        )
                    textSize = 15f
                    setTextColor(red)
                    setTypeface(
                        null,
                        Typeface.BOLD
                    )
                    gravity = Gravity.END
                }

            val delete =
                TextView(this).apply {
                    text = "Delete"
                    textSize = 11f
                    setTextColor(
                        secondary
                    )
                    setPadding(
                        0,
                        5,
                        0,
                        0
                    )

                    setOnClickListener {

                        expenses.removeAt(
                            index
                        )

                        saveData()
                        updateTotals()
                        refreshExpenses()
                    }
                }

            right.addView(price)
            right.addView(delete)

            row.addView(right)

            expensesContainer.addView(row)

            if (index != 0) {

                val separator =
                    Space(this)

                expensesContainer.addView(
                    separator,
                    LinearLayout.LayoutParams(
                        1,
                        8
                    )
                )
            }
        }
    }

    private fun updateTotals() {

        var total = 0.0

        for (expense in expenses) {
            total += expense.amount
        }

        val remaining =
            income - total

        monthlyText.text =
            "Expenses\n${money(total)}"

        remainingText.text =
            "Remaining\n${money(remaining)}"

        if (remaining < 0) {
            remainingText.setTextColor(red)
        } else {
            remainingText.setTextColor(green)
        }

        incomeDisplay.text =
            money(income)

        incomeInput.setText(
            if (income > 0)
                income.toString()
            else
                ""
        )
    }

    private fun saveData() {

        val jsonArray =
            JSONArray()

        for (expense in expenses) {

            val jsonObje
