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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {

    private lateinit var incomeInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var expensesContainer: LinearLayout

    private lateinit var monthlyExpenseText: TextView
    private lateinit var yearlyExpenseText: TextView
    private lateinit var remainingText: TextView
    private lateinit var incomeDisplay: TextView
    private lateinit var expenseCountText: TextView
    private lateinit var progressBar: ProgressBar

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
        getSharedPreferences("moneytrack_data", Context.MODE_PRIVATE)
    }

    // ---------------------------------------------------------
    // COLORS
    // ---------------------------------------------------------

    private val background = Color.rgb(8, 13, 20)
    private val card = Color.rgb(17, 25, 36)
    private val cardLight = Color.rgb(22, 32, 46)

    private val blue = Color.rgb(59, 130, 246)
    private val blueLight = Color.rgb(96, 165, 250)

    private val green = Color.rgb(34, 197, 94)
    private val red = Color.rgb(248, 113, 113)

    private val white = Color.rgb(248, 250, 252)
    private val textSecondary = Color.rgb(148, 163, 184)
    private val border = Color.rgb(35, 48, 65)

    // ---------------------------------------------------------
    // ACTIVITY
    // ---------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadData()
        buildInterface()
        updateDashboard()
        refreshExpenses()
    }

    // ---------------------------------------------------------
    // MAIN INTERFACE
    // ---------------------------------------------------------

    private fun buildInterface() {

        val scroll = ScrollView(this).apply {
            setBackgroundColor(background)
        }

        val main = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 28, 22, 40)
        }

        // HEADER
        main.addView(createHeader())

        // MONTH CARD
        main.addView(createMonthCard())

        // OVERVIEW
        main.addView(sectionLabel("Overview"))

        main.addView(createStatsRow())

        // INCOME
        main.addView(sectionLabel("Monthly income"))

        main.addView(createIncomeCard())

        // PROGRESS
        main.addView(createBudgetProgress())

        // ADD EXPENSE
        main.addView(sectionLabel("Add expense"))

        main.addView(createExpenseCard())

        // EXPENSES
        main.addView(sectionLabel("Recent expenses"))

        expensesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        main.addView(expensesContainer)

        scroll.addView(main)

        setContentView(scroll)
    }

    // ---------------------------------------------------------
    // HEADER
    // ---------------------------------------------------------

    private fun createHeader(): View {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 22)
        }

        val logo = ImageView(this).apply {
            setImageResource(com.moneytrack.app.R.drawable.smart_money_logo)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        container.addView(
            logo,
            LinearLayout.LayoutParams(
                64,
                64
            )
        )

        val textContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 0, 0, 0)
        }

        val title = TextView(this).apply {
            text = "Smart Money"
            textSize = 25f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
        }

        val subtitle = TextView(this).apply {
            text = "Take control of your money"
            textSize = 14f
            setTextColor(textSecondary)
            setPadding(0, 3, 0, 0)
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

    // ---------------------------------------------------------
    // MONTH CARD
    // ---------------------------------------------------------

    private fun createMonthCard(): View {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 18, 20, 18)
            background = roundedBackground(card, 18, border)
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val monthName = SimpleDateFormat(
            "MMMM yyyy",
            Locale.getDefault()
        ).format(Date())

        val monthText = TextView(this).apply {
            text = monthName
                .replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                    else it.toString()
                }

            textSize = 18f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
        }

        top.addView(
            monthText,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val offline = TextView(this).apply {
            text = "● OFFLINE"
            textSize = 11f
            setTextColor(green)
            setTypeface(null, Typeface.BOLD)
        }

        top.addView(offline)

        container.addView(top)

        val small = TextView(this).apply {
            text = "Your personal financial overview"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, 7, 0, 0)
        }

        container.addView(small)

        return container
    }

    // ---------------------------------------------------------
    // STATS
    // ---------------------------------------------------------

    private fun createStatsRow(): View {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        row.addView(
            createStatCard(
                "Expenses",
                "€0.00",
                blueLight
            ),
            weightParams()
        )

        row.addView(spaceHorizontal(10))

        row.addView(
            createStatCard(
                "Yearly",
                "€0.00",
                blue
            ),
            weightParams()
        )

        row.addView(spaceHorizontal(10))

        row.addView(
            createStatCard(
                "Remaining",
                "€0.00",
                green
            ),
            weightParams()
        )

        return row
    }

    private fun createStatCard(
        label: String,
        value: String,
        accent: Int
    ): View {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 16, 14, 16)
            background = roundedBackground(card, 16, border)
        }

        val labelView = TextView(this).apply {
            text = label
            textSize = 12f
            setTextColor(textSecondary)
        }

        val valueView = TextView(this).apply {
            text = value
            textSize = 16f
            setTextColor(accent)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 7, 0, 0)
        }

        box.addView(labelView)
        box.addView(valueView)

        when (label) {
            "Expenses" -> monthlyExpenseText = valueView
            "Yearly" -> yearlyExpenseText = valueView
            "Remaining" -> remainingText = valueView
        }

        return box
    }

    // ---------------------------------------------------------
    // INCOME
    // ---------------------------------------------------------

    private fun createIncomeCard(): View {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            background = roundedBackground(card, 18, border)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "Monthly income"
            textSize = 15f
            setTextColor(textSecondary)
        }

        header.addView(
            title,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        incomeDisplay = TextView(this).apply {
            text = "€0.00"
            textSize = 25f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
        }

        header.addView(incomeDisplay)

        container.addView(header)

        incomeInput = createInput("Enter income (€)")

        container.addView(
            incomeInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                58
            ).apply {
                topMargin = 16
            }
        )

        val save = createAccentButton(
            "Save income",
            blue
        )

        save.setOnClickListener {

            val value = incomeInput.text.toString()
                .toDoubleOrNull()

            if (value != null && value >= 0) {

                income = value

                saveData()
                updateDashboard()

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
            save,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                52
            ).apply {
                topMargin = 10
            }
        )

        return container
    }

    // ---------------------------------------------------------
    // BUDGET PROGRESS
    // ---------------------------------------------------------

    private fun createBudgetProgress(): View {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 18, 20, 18)
            background = roundedBackground(card, 18, border)
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val title = TextView(this).apply {
            text = "Budget usage"
            textSize = 14f
            setTextColor(textSecondary)
        }

        top.addView(
            title,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        expenseCountText = TextView(this).apply {
            text = "0 expenses"
            textSize = 13f
            setTextColor(textSecondary)
        }

        top.addView(expenseCountText)

        container.addView(top)

        progressBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            progress = 0

            progressDrawable = roundedProgressDrawable()
        }

        container.addView(
            progressBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                10
            ).apply {
                topMargin = 14
            }
        )

        return container
    }

    // ---------------------------------------------------------
    // ADD EXPENSE CARD
    // ---------------------------------------------------------

    private fun createExpenseCard(): View {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            background = roundedBackground(card, 18, border)
        }

        descriptionInput = createInput("Description")

        container.addView(
            descriptionInput,
            fieldParams()
        )

        categorySpinner = Spinner(this).apply {
            background = roundedBackground(cardLight, 12, border)
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        categorySpinner.adapter = adapter

        container.addView(
            categorySpinner,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                55
            ).apply {
                bottomMargin = 10
            }
        )

        amountInput = createInput("Amount (€)")

        container.addView(
            amountInput,
            fieldParams()
        )

        val add = createAccentButton(
            "+  Add expense",
            blue
        )

        add.setOnClickListener {
            addExpense()
        }

        container.addView(
            add,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                54
            )
        )

        return container
    }

    // ---------------------------------------------------------
    // ADD EXPENSE
    // ---------------------------------------------------------

    private fun addExpense() {

        val description =
            descriptionInput.text.toString().trim()

        val category =
            categorySpinner.selectedItem.toString()

        val amount =
            amountInput.text.toString().toDoubleOrNull()

        if (description.isEmpty()) {

            Toast.makeText(
                this,
                "Enter a description",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (amount == null || amount <= 0) {

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
        updateDashboard()
        refreshExpenses()

        Toast.makeText(
            this,
            "Expense added",
            Toast.LENGTH_SHORT
        ).show()
    }

    // ---------------------------------------------------------
    // EXPENSE LIST
    // ---------------------------------------------------------

    private fun refreshExpenses() {

        expensesContainer.removeAllViews()

        if (expenses.isEmpty()) {

            val empty = TextView(this).apply {
                text = "No expenses yet\nAdd your first expense above."
                textSize = 14f
                setTextColor(textSecondary)
                gravity = Gravity.CENTER
                setPadding(20, 30, 20, 30)
                background = roundedBackground(card, 18, border)
            }

            expensesContainer.addView(empty)

            return
        }

        for (index in expenses.indices.reversed()) {

            val expense = expenses[index]

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16, 16, 12, 16)
                background = roundedBackground(card, 16, border)
            }

            val icon = TextView(this).apply {
                text = "€"
                textSize = 17f
                gravity = Gravity.CENTER
                setTextColor(blueLight)
                setTypeface(null, Typeface.BOLD)
                background = roundedBackground(
                    Color.rgb(20, 45, 78),
                    12,
                    Color.TRANSPARENT
                )
            }

            row.addView(
                icon,
                LinearLayout.LayoutParams(44, 44)
            )

            val information = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12, 0, 8, 0)
            }

            val description = TextView(this).apply {
                text = expense.description
                textSize = 15f
                setTextColor(white)
                setTypeface(null, Typeface.BOLD)
            }

            val category = TextView(this).apply {
                text = expense.category
                textSize = 12f
                setTextColor(textSecondary)
                setPadding(0, 3, 0, 0)
            }

            information.addView(description)
            information.addView(category)

            row.addView(
                information,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val right = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.END
            }

            val price = TextView(this).apply {
                text = money(expense.amount)
                textSize = 15f
                setTextColor(red)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.END
            }

            val delete = TextView(this).apply {
                text = "Delete"
                textSize = 11f
                setTextColor(textSecondary)
                setPadding(0, 5, 0, 0)

                setOnClickListener {

                    expenses.removeAt(index)

                    saveData()
                    updateDashboard()
                    refreshExpenses()
                }
            }

            right.addView(price)
            right.addView(delete)

            row.addView(right)

            expensesContainer.addView(row)

            if (index != 0) {
                val separator = Space(this)

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

    // ---------------------------------------------------------
    // DASHBOARD UPDATE
    // ---------------------------------------------------------

    private fun updateDashboard() {

        val totalExpenses =
            expenses.sumOf { it.amount }

        val remaining =
            income - totalExpenses

        val yearly =
            totalExpenses

        monthlyExpenseText.text =
            money(totalExpenses)

        yearlyExpenseText.text =
            money(yearly)

        remainingText.text =
            money(remaining)

        incomeDisplay.text =
            money(income)

        incomeInput.setText(
            if (income > 0) income.toString()
            else ""
        )

        expenseCountText.text =
            "${expenses.size} " +
                    if (expenses.size == 1)
                        "expense"
                    else
                        "expenses"

        val percentage =
            if (income > 0) {
                ((totalExpenses / income) * 100)
                    .coerceIn(0.0, 100.0)
                    .toInt()
            } else {
                0
            }

        progressBar.progress = percentage

        if (remaining < 0) {
            remainingText.setTextColor(red)
        } else {
            remainingText.setTextColor(green)
        }
    }

    // ---------------------------------------------------------
    // STORAGE
    // ---------------------------------------------------------

    private fun saveData() {

        val jsonArray = JSONArray()

        for (expense in expenses) {

            val jsonObject = JSONObject()

            jsonObject.put(
                "description",
                expense.description
            )

            jsonObject.put(
                "category",
                expense.category
            )

            jsonObject.put(
                "amount",
                expense.amount
            )

            jsonArray.put(jsonObject)
        }

        prefs.edit()
            .putString(
                "income",
                income.toString()
            )
            .putString(
                "expenses",
                jsonArray.toString()
            )
            .apply()
    }

    private fun loadData() {

        val savedIncome =
            prefs.getString("income", null)

        if (!savedIncome.isNullOrEmpty()) {

            income =
                savedIncome.toDoubleOrNull()
                    ?: 0.0
        }

        val savedExpenses =
            prefs.getString("expenses", null)

        if (savedExpenses.isNullOrEmpty()) {
            return
        }

        try {

            val jsonArray =
                JSONArray(savedExpenses)

            expenses.clear()

            for (i in 0 until jsonArray.length()) {

                val obj =
                    jsonArray.getJSONObject(i)

                val description =
                    obj.getString("description")

                val category =
                    obj.getString("category")

                val amount =
                    obj.getDouble("amount")

                expenses.add(
                    Expense(
                        description,
                        category,
                        amount
                    )
                )
            }

        } catch (e: Exception) {

            expenses.clear()
        }
    }

    // ---------------------------------------------------------
    // UI HELPERS
    // ---------------------------------------------------------

    private fun sectionLabel(text: String): TextView {

        return TextView(this).apply {

            this.text = text

            textSize = 18f

            setTextColor(white)

            setTypeface(
                null,
                Typeface.BOLD
            )

            setPadding(
                2,
                26,
                2,
                11
            )
        }
    }

    private fun createInput(
        hintText: String
    ): EditText {

        return EditText(this).apply {

            hint = hintText

            setHintTextColor(
                Color.rgb(100, 116, 139)
            )

            setTextColor(white)

            textSize = 15f

            setSingleLine()

            setPadding(
                16,
                0,
                16,
                0
            )

            background =
                roundedBackground(
                    cardLight,
                    12,
                    border
                )

            if (
                hintText.contains("income", true) ||
                hintText.contains("amount", true)
            ) {

                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
        }
    }

    private fun createAccentButton(
        textValue: String,
        color: Int
    ): TextView {

        return TextView(this).apply {

            text = textValue

            textSize = 15f

            gravity = Gravity.CENTER

            setTextColor(Color.WHITE)

            setTypeface(
                null,
                Typeface.BOLD
            )

            background =
                roundedBackground(
                    color,
                    13,
                    Color.TRANSPARENT
                )

            isClickable = true
        }
    }

    private fun roundedBackground(
        color: Int,
        radius: Int,
        strokeColor: Int
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(color)

            cornerRadius =
                radius.toFloat()

            if (strokeColor != Color.TRANSPARENT) {

                setStroke(
                    1,
                    strokeColor
                )
            }
        }
    }

    private fun roundedProgressDrawable():
            android.graphics.drawable.Drawable {

        return GradientDrawable().apply {

            shape =
                GradientDrawable.RECTANGLE

            cornerRadius = 8f

            setColor(blue)
        }
    }

    private fun fieldParams():
            LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            55
        ).apply {
            bottomMargin = 10
        }
    }

    private fun weightParams():
            LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
    }

    private fun spaceHorizontal(
        width: Int
    ): Space {

        return Space(this).apply {
            minimumWidth = width
        }
    }

    private fun money(
        value: Double
    ): String {

        return NumberFormat
            .getCurrencyInstance(
                Locale.GERMANY
            )
            .format(value)
    }
}
