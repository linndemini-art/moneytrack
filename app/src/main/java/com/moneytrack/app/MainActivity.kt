package com.moneytrack.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.ScrollView
import android.widget.Space
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.content.Context
import android.text.InputType
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
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
    private lateinit var monthText: TextView

    private var income = 0.0
    private val expenses = mutableListOf<Expense>()

    private val prefs by lazy {
        getSharedPreferences(
            "moneytrack_data",
            Context.MODE_PRIVATE
        )
    }

    private var selectedYear = 0
    private var selectedMonth = 0

    data class Expense(
        val description: String,
        val category: String,
        val amount: Double
    )

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

    private val backgroundColor = Color.rgb(8, 8, 8)
    private val cardColor = Color.rgb(21, 21, 21)
    private val surfaceColor = Color.rgb(29, 29, 29)

    private val amberDark = Color.rgb(184, 107, 0)
    private val amber = Color.rgb(255, 157, 0)
    private val amberBright = Color.rgb(255, 181, 46)

    private val green = Color.rgb(72, 190, 110)

    private val white = Color.WHITE
    private val secondary = Color.rgb(168, 168, 168)
    private val muted = Color.rgb(125, 125, 125)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val calendar = Calendar.getInstance()

        selectedYear = calendar.get(Calendar.YEAR)
        selectedMonth = calendar.get(Calendar.MONTH)

        buildInterface()
        loadData()
        updateMonthDisplay()
        updateTotals()
        refreshExpenses()
    }

    private fun buildInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
        }

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(backgroundColor)
            overScrollMode = ScrollView.OVER_SCROLL_NEVER
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 24, 20, 36)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val logo = ImageView(this).apply {
            setImageResource(
                resources.getIdentifier(
                    "smart_money_logo",
                    "drawable",
                    packageName
                )
            )
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        header.addView(
            logo,
            LinearLayout.LayoutParams(52, 52)
        )

        val titleColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 0, 0, 0)
        }

        val title = TextView(this).apply {
            text = "Smart Money"
            textSize = 25f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
        }

        val subtitle = TextView(this).apply {
            text = "Personal finance tracker"
            textSize = 13f
            setTextColor(secondary)
            setPadding(0, 3, 0, 0)
        }

        titleColumn.addView(title)
        titleColumn.addView(subtitle)

        header.addView(
            titleColumn,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        content.addView(header)

        val monthCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            background = gradientBackground(
                Color.rgb(17, 17, 17),
                Color.rgb(42, 26, 8),
                18f
            )
        }

        val monthNavigation = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val previousButton = createNavigationButton("‹")

        previousButton.setOnClickListener {
            changeMonth(-1)
        }

        monthNavigation.addView(
            previousButton,
            LinearLayout.LayoutParams(52, 52)
        )

        monthText = TextView(this).apply {
            textSize = 20f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(8, 0, 8, 0)

            setOnClickListener {
                showMonthPicker()
            }
        }

        monthNavigation.addView(
            monthText,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val nextButton = createNavigationButton("›")

        nextButton.setOnClickListener {
            changeMonth(1)
        }

        monthNavigation.addView(
            nextButton,
            LinearLayout.LayoutParams(52, 52)
        )

        monthCard.addView(monthNavigation)

        val status = TextView(this).apply {
            text = "●  OFFLINE"
            textSize = 12f
            setTextColor(amberBright)
            setPadding(4, 8, 0, 0)
            typeface = Typeface.DEFAULT_BOLD
        }

        monthCard.addView(status)

        val monthDescription = TextView(this).apply {
            text = "Your money, organized by month"
            textSize = 13f
            setTextColor(secondary)
            setPadding(4, 7, 0, 0)
        }

        monthCard.addView(monthDescription)

        val monthCardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        monthCardParams.setMargins(0, 24, 0, 0)

        content.addView(
            monthCard,
            monthCardParams
        )

        val overviewTitle = sectionTitle(
            "Overview",
            white
        )

        val overviewTitleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        overviewTitleParams.setMargins(0, 28, 0, 12)

        content.addView(
            overviewTitle,
            overviewTitleParams
        )

        val overviewRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val expensesCard = createSmallCard()

        val expensesLabel = smallLabel("Expenses")

        val expensesValue = TextView(this).apply {
            text = "€0.00"
            textSize = 22f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 5, 0, 0)
        }

        monthlyText = expensesValue

        expensesCard.addView(expensesLabel)
        expensesCard.addView(expensesValue)

        overviewRow.addView(
            expensesCard,
            weightParams()
        )

        overviewRow.addView(
            Space(this),
            LinearLayout.LayoutParams(12, 1)
        )

        val remainingCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            background = gradientBackground(
                cardColor,
                Color.rgb(51, 32, 6),
                16f
            )
        }

        val remainingLabel = smallLabel("Remaining")

        val remainingValue = TextView(this).apply {
            text = "€0.00"
            textSize = 22f
            setTextColor(amberBright)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 5, 0, 0)
        }

        remainingText = remainingValue

        remainingCard.addView(remainingLabel)
        remainingCard.addView(remainingValue)

        overviewRow.addView(
            remainingCard,
            weightParams()
        )

        content.addView(overviewRow)

        val incomeTitle = sectionTitle(
            "Monthly Income",
            white
        )

        val incomeTitleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        incomeTitleParams.setMargins(0, 28, 0, 12)

        content.addView(
            incomeTitle,
            incomeTitleParams
        )

        val incomeCard = createMainCard()

        incomeDisplay = TextView(this).apply {
            text = "€0.00"
            textSize = 24f
            setTextColor(green)
            typeface = Typeface.DEFAULT_BOLD
        }

        incomeCard.addView(
            incomeDisplay,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        incomeInput = createInput(
            "Enter monthly income",
            InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        )

        incomeCard.addView(
            incomeInput,
            inputParams()
        )

        val saveIncomeButton = createGradientButton(
            "Save Income",
            amberDark,
            amberBright
        )

        saveIncomeButton.setOnClickListener {
            saveIncome()
        }

        incomeCard.addView(
            saveIncomeButton,
            buttonParams()
        )

        content.addView(incomeCard)
                val addTitle = sectionTitle(
            "Add Expense",
            white
        )

        val addTitleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        addTitleParams.setMargins(0, 28, 0, 12)

        content.addView(
            addTitle,
            addTitleParams
        )

        val addCard = createMainCard()

        descriptionInput = createInput(
            "Description",
            InputType.TYPE_CLASS_TEXT
        )

        addCard.addView(
            descriptionInput,
            inputParams()
        )

        amountInput = createInput(
            "Amount",
            InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        )

        addCard.addView(
            amountInput,
            inputParams()
        )

        categorySpinner = Spinner(this).apply {
            background = roundedBackground(
                surfaceColor,
                12f
            )
            setPadding(8, 0, 8, 0)
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        categorySpinner.adapter = adapter

        addCard.addView(
            categorySpinner,
            spinnerParams()
        )

        val addExpenseButton = createGradientButton(
            "Add Expense",
            amberDark,
            amberBright
        )

        addExpenseButton.setOnClickListener {
            addExpense()
        }

        addCard.addView(
            addExpenseButton,
            buttonParams()
        )

        content.addView(addCard)

        val recentTitle = sectionTitle(
            "Recent Expenses",
            white
        )

        val recentTitleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        recentTitleParams.setMargins(0, 28, 0, 12)

        content.addView(
            recentTitle,
            recentTitleParams
        )

        expensesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        content.addView(
            expensesContainer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        scrollView.addView(content)

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun saveIncome() {

        val value = incomeInput.text
            .toString()
            .replace(",", ".")
            .toDoubleOrNull()

        if (value == null || value < 0) {
            Toast.makeText(
                this,
                "Please enter a valid income.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        income = value

        saveData()
        updateTotals()

        incomeInput.text.clear()

        Toast.makeText(
            this,
            "Income saved.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun addExpense() {

        val description = descriptionInput.text
            .toString()
            .trim()

        val amount = amountInput.text
            .toString()
            .replace(",", ".")
            .toDoubleOrNull()

        val category = categorySpinner
            .selectedItem
            ?.toString()
            ?: "Other"

        if (description.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter a description.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (amount == null || amount <= 0) {
            Toast.makeText(
                this,
                "Please enter a valid amount.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        expenses.add(
            Expense(
                description = description,
                category = category,
                amount = amount
            )
        )

        saveData()
        updateTotals()
        refreshExpenses()

        descriptionInput.text.clear()
        amountInput.text.clear()

        Toast.makeText(
            this,
            "Expense added.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun deleteExpense(index: Int) {

        if (index < 0 || index >= expenses.size) {
            return
        }

        expenses.removeAt(index)

        saveData()
        updateTotals()
        refreshExpenses()
    }

    private fun updateTotals() {

        val totalExpenses = expenses.sumOf {
            it.amount
        }

        val remaining = income - totalExpenses

        monthlyText.text = money(totalExpenses)
        remainingText.text = money(remaining)
        incomeDisplay.text = money(income)

        remainingText.setTextColor(
            if (remaining < 0) {
                Color.rgb(255, 95, 95)
            } else {
                amberBright
            }
        )
    }

    private fun currentMonthKey(): String {

        return String.format(
            Locale.US,
            "%04d-%02d",
            selectedYear,
            selectedMonth + 1
        )
    }

    private fun updateMonthDisplay() {

        val calendar = Calendar.getInstance()

        calendar.set(
            selectedYear,
            selectedMonth,
            1
        )

        val formatter = SimpleDateFormat(
            "MMMM yyyy",
            Locale.ENGLISH
        )

        monthText.text = formatter.format(
            calendar.time
        )
    }

    private fun changeMonth(delta: Int) {

        saveData()

        val calendar = Calendar.getInstance()

        calendar.set(
            selectedYear,
            selectedMonth,
            1
        )

        calendar.add(
            Calendar.MONTH,
            delta
        )

        selectedYear = calendar.get(
            Calendar.YEAR
        )

        selectedMonth = calendar.get(
            Calendar.MONTH
        )

        loadData()
        updateMonthDisplay()
        updateTotals()
        refreshExpenses()

        incomeInput.text.clear()
        descriptionInput.text.clear()
        amountInput.text.clear()
    }

    private fun showMonthPicker() {

        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(24, 0, 24, 0)
        }

        val monthPicker = NumberPicker(this).apply {
            minValue = 1
            maxValue = 12
            value = selectedMonth + 1
            displayedValues = arrayOf(
                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December"
            )
        }

        val yearPicker = NumberPicker(this).apply {
            minValue = selectedYear - 10
            maxValue = selectedYear + 10
            value = selectedYear
        }

        dialogLayout.addView(
            monthPicker,
            LinearLayout.LayoutParams(
                0,
                220,
                1f
            )
        )

        dialogLayout.addView(
            yearPicker,
            LinearLayout.LayoutParams(
                0,
                220,
                1f
            )
        )

        AlertDialog.Builder(this)
            .setTitle("Select Month")
            .setView(dialogLayout)
            .setPositiveButton("Select") { _, _ ->

                saveData()

                selectedMonth = monthPicker.value - 1
                selectedYear = yearPicker.value

                loadData()
                updateMonthDisplay()
                updateTotals()
                refreshExpenses()

                incomeInput.text.clear()
                descriptionInput.text.clear()
                amountInput.text.clear()
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }
    
            private fun saveData() {

        val monthKey = currentMonthKey()
        val expensesArray = JSONArray()

        for (expense in expenses) {

            val objectItem = JSONObject()

            objectItem.put(
                "description",
                expense.description
            )

            objectItem.put(
                "category",
                expense.category
            )

            objectItem.put(
                "amount",
                expense.amount
            )

            expensesArray.put(objectItem)
        }

        prefs.edit()
            .putString(
                "income_$monthKey",
                income.toString()
            )
            .putString(
                "expenses_$monthKey",
                expensesArray.toString()
            )
            .apply()
    }

    private fun loadData() {

        income = 0.0
        expenses.clear()

        val monthKey = currentMonthKey()

        val savedIncome = prefs.getString(
            "income_$monthKey",
            null
        )

        val savedExpenses = prefs.getString(
            "expenses_$monthKey",
            null
        )

        if (
            savedIncome != null ||
            savedExpenses != null
        ) {

            if (!savedIncome.isNullOrEmpty()) {

                income = savedIncome
                    .toDoubleOrNull()
                    ?: 0.0
            }

            if (!savedExpenses.isNullOrEmpty()) {

                try {

                    val array = JSONArray(
                        savedExpenses
                    )

                    for (
                        index in 0 until array.length()
                    ) {

                        val item =
                            array.getJSONObject(index)

                        expenses.add(
                            Expense(
                                description =
                                    item.optString(
                                        "description"
                                    ),
                                category =
                                    item.optString(
                                        "category"
                                    ),
                                amount =
                                    item.optDouble(
                                        "amount",
                                        0.0
                                    )
                            )
                        )
                    }

                } catch (
                    exception: Exception
                ) {

                    expenses.clear()
                }
            }

            return
        }

        val migrationDone = prefs.getBoolean(
            "legacy_data_migrated",
            false
        )

        if (!migrationDone) {

            val legacyIncome = prefs.getString(
                "income",
                null
            )

            val legacyExpenses = prefs.getString(
                "expenses",
                null
            )

            if (
                !legacyIncome.isNullOrEmpty() ||
                !legacyExpenses.isNullOrEmpty()
            ) {

                income =
                    legacyIncome
                        ?.toDoubleOrNull()
                        ?: 0.0

                if (!legacyExpenses.isNullOrEmpty()) {

                    try {

                        val array = JSONArray(
                            legacyExpenses
                        )

                        for (
                            index in 0 until array.length()
                        ) {

                            val item =
                                array.getJSONObject(index)

                            expenses.add(
                                Expense(
                                    description =
                                        item.optString(
                                            "description"
                                        ),
                                    category =
                                        item.optString(
                                            "category"
                                        ),
                                    amount =
                                        item.optDouble(
                                            "amount",
                                            0.0
                                        )
                                )
                            )
                        }

                    } catch (
                        exception: Exception
                    ) {

                        expenses.clear()
                    }
                }

                saveData()
            }

            prefs.edit()
                .putBoolean(
                    "legacy_data_migrated",
                    true
                )
                .apply()
        }
    }

    private fun refreshExpenses() {

        expensesContainer.removeAllViews()

        if (expenses.isEmpty()) {

            val emptyCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(20, 22, 20, 22)
                background = roundedBackground(
                    cardColor,
                    16f
                )
            }

            val emptyTitle = TextView(this).apply {
                text = "No expenses this month"
                textSize = 15f
                setTextColor(white)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
            }

            val emptySubtitle = TextView(this).apply {
                text = "Your spending will appear here"
                textSize = 12f
                setTextColor(muted)
                gravity = Gravity.CENTER
                setPadding(0, 5, 0, 0)
            }

            emptyCard.addView(emptyTitle)
            emptyCard.addView(emptySubtitle)

            expensesContainer.addView(
                emptyCard,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            return
        }

        for (index in expenses.indices.reversed()) {

            val expense = expenses[index]

            val expenseCard = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16, 15, 12, 15)
                background = roundedBackground(
                    cardColor,
                    16f
                )
            }

            val accent = View(this).apply {
                setBackgroundColor(amber)
            }

            expenseCard.addView(
                accent,
                LinearLayout.LayoutParams(
                    3,
                    48
                )
            )

            val details = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12, 0, 8, 0)
            }

            val description = TextView(this).apply {
                text = expense.description
                textSize = 16f
                setTextColor(white)
                typeface = Typeface.DEFAULT_BOLD
            }

            val category = TextView(this).apply {
                text = expense.category
                textSize = 12f
                setTextColor(secondary)
                setPadding(0, 4, 0, 0)
            }

            details.addView(description)
            details.addView(category)

            expenseCard.addView(
                details,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val rightColumn = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.END
            }

            val amountText = TextView(this).apply {
                text = money(expense.amount)
                textSize = 16f
                setTextColor(white)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.END
            }

            rightColumn.addView(amountText)

            val deleteButton = TextView(this).apply {
                text = "Delete"
                textSize = 12f
                setTextColor(amberBright)
                gravity = Gravity.END
                setPadding(8, 5, 0, 0)

                setOnClickListener {
                    deleteExpense(index)
                }
            }

            rightColumn.addView(deleteButton)

            expenseCard.addView(
                rightColumn,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(
                0,
                0,
                0,
                10
            )

            expensesContainer.addView(
                expenseCard,
                params
            )
        }
    }

    private fun sectionTitle(
        text: String,
        color: Int
    ): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = 19f
            setTextColor(color)
            typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun smallLabel(
        text: String
    ): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(secondary)
        }
    }

    private fun createSmallCard(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            background = roundedBackground(
                cardColor,
                16f
            )
        }
    }

    private fun createMainCard(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            background = gradientBackground(
                Color.rgb(17, 17, 17),
                Color.rgb(28, 22, 15),
                16f
            )
        }
    }
        private fun createInput(
        hint: String,
        inputType: Int
    ): EditText {

        return EditText(this).apply {
            this.hint = hint
            this.inputType = inputType
            textSize = 15f
            setTextColor(white)
            setHintTextColor(muted)

            setPadding(
                14,
                12,
                14,
                12
            )

            background = roundedBackground(
                surfaceColor,
                12f
            )
        }
    }

    private fun createGradientButton(
        text: String,
        startColor: Int,
        endColor: Int
    ): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER

            setPadding(
                16,
                13,
                16,
                13
            )

            background = gradientBackground(
                startColor,
                endColor,
                12f
            )
        }
    }

    private fun createNavigationButton(
        text: String
    ): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = 32f
            setTextColor(amberBright)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT
            setPadding(4, 0, 4, 2)

            background = roundedBackground(
                Color.rgb(32, 24, 14),
                12f
            )
        }
    }

    private fun roundedBackground(
        color: Int,
        radius: Float
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }
    }

    private fun gradientBackground(
        startColor: Int,
        endColor: Int,
        radius: Float
    ): GradientDrawable {

        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                startColor,
                endColor
            )
        ).apply {
            cornerRadius = radius
        }
    }

    private fun inputParams():
        LinearLayout.LayoutParams {

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(
            0,
            0,
            0,
            10
        )

        return params
    }

    private fun spinnerParams():
        LinearLayout.LayoutParams {

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(
            0,
            0,
            0,
            10
        )

        return params
    }

    private fun buttonParams():
        LinearLayout.LayoutParams {

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(
            0,
            4,
            0,
            0
        )

        return params
    }

    private fun weightParams():
        LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
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
