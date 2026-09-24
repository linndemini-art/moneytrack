package com.moneytrack.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.ScrollView
import android.widget.Space
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.content.Context
import android.content.Intent
import android.text.InputType
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : Activity() {

    companion object {
        private const val REQUEST_EXPORT_BACKUP = 1001
        private const val REQUEST_IMPORT_BACKUP = 1002
    }

    private lateinit var incomeInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var expensesContainer: LinearLayout

    private lateinit var monthlyText: TextView
    private lateinit var remainingText: TextView
    private lateinit var incomeDisplay: TextView
    private lateinit var monthText: TextView

    private lateinit var menuDrawer: LinearLayout
    private lateinit var menuOverlay: View

    private var isMenuOpen = false
    private var isHistoryOpen = false

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

    private val backgroundColor = Color.rgb(7, 8, 12)
    private val cardColor = Color.rgb(20, 22, 29)
    private val surfaceColor = Color.rgb(28, 31, 40)

    private val blueDark = Color.rgb(18, 55, 110)
    private val blue = Color.rgb(38, 104, 210)
    private val blueBright = Color.rgb(74, 145, 255)

    private val amber = Color.rgb(255, 166, 52)
    private val green = Color.rgb(72, 190, 110)

    private val white = Color.WHITE
    private val secondary = Color.rgb(168, 174, 187)
    private val muted = Color.rgb(115, 121, 135)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

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

        isHistoryOpen = false

        val root = FrameLayout(this).apply {
            setBackgroundColor(backgroundColor)
        }

        val mainContent = LinearLayout(this).apply {
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
            setPadding(12, 92, 12, 28)
        }

        val bankCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 18, 20, 18)
            background = gradientBackground(
                Color.rgb(18, 29, 52),
                Color.rgb(24, 67, 125),
                22f
            )
        }

        val cardTopRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val creditCardLabel = TextView(this).apply {
            text = "CREDIT CARD"
            textSize = 11f
            setTextColor(Color.rgb(207, 220, 242))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }

        cardTopRow.addView(
            creditCardLabel,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val bankLabel = TextView(this).apply {
            text = "BANK"
            textSize = 11f
            setTextColor(Color.rgb(207, 220, 242))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
            gravity = Gravity.END
        }

        cardTopRow.addView(
            bankLabel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        bankCard.addView(cardTopRow)

        val smartMoney = TextView(this).apply {
            text = "SMART MONEY"
            textSize = 27f
            setTextColor(white)
            typeface = Typeface.create(
                Typeface.DEFAULT,
                Typeface.BOLD
            )
            gravity = Gravity.CENTER
            letterSpacing = 0.04f

            setOnClickListener {
                openMenu()
            }
        }

        val smartMoneyParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )

        smartMoneyParams.setMargins(0, 4, 0, 4)

        bankCard.addView(
            smartMoney,
            smartMoneyParams
        )

        val cardBottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val cardLine = View(this).apply {
            setBackgroundColor(
                Color.argb(
                    90,
                    255,
                    255,
                    255
                )
            )
        }

        cardBottomRow.addView(
            cardLine,
            LinearLayout.LayoutParams(
                0,
                1,
                1f
            )
        )

        val cardDot = TextView(this).apply {
            text = "●"
            textSize = 8f
            setTextColor(amber)
            gravity = Gravity.CENTER
            setPadding(10, 0, 0, 0)
        }

        cardBottomRow.addView(
            cardDot,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        bankCard.addView(cardBottomRow)

        val bankCardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (190 * resources.displayMetrics.density).toInt()
        )

        content.addView(
            bankCard,
            bankCardParams
        )

        val monthCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 14, 14, 14)
            background = roundedBackground(
                cardColor,
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
            LinearLayout.LayoutParams(48, 48)
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
            LinearLayout.LayoutParams(48, 48)
        )

        monthCard.addView(monthNavigation)

        val status = TextView(this).apply {
            text = "●  OFFLINE"
            textSize = 11f
            setTextColor(blueBright)
            setPadding(4, 7, 0, 0)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.04f
        }

        monthCard.addView(status)

        val monthDescription = TextView(this).apply {
            text = "Your money, organized by month"
            textSize = 13f
            setTextColor(secondary)
            setPadding(4, 5, 0, 0)
        }

        monthCard.addView(monthDescription)

        val monthCardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        monthCardParams.setMargins(0, 10, 0, 0)

        content.addView(
            monthCard,
            monthCardParams
        )

        val overviewTitle = sectionTitle(
            "OVERVIEW",
            white
        )

        val overviewTitleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        overviewTitleParams.setMargins(2, 24, 0, 10)

        content.addView(
            overviewTitle,
            overviewTitleParams
        )
                val recentTitle = sectionTitle(
            "RECENT EXPENSES",
            white
        )

        val recentTitleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        recentTitleParams.setMargins(2, 24, 0, 10)

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

        val backupTitle = sectionTitle(
            "DATA BACKUP",
            white
        )

        val backupTitleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        backupTitleParams.setMargins(2, 24, 0, 10)

        content.addView(
            backupTitle,
            backupTitleParams
        )
        val overviewRow = LinearLayout(this).apply {
    orientation = LinearLayout.HORIZONTAL
    gravity = Gravity.CENTER
}

val expensesCard = createSmallCard()

val expensesLabel = smallLabel("Expenses")

monthlyText = TextView(this).apply {
    textSize = 20f
    setTextColor(white)
    typeface = Typeface.DEFAULT_BOLD
}

expensesCard.addView(expensesLabel)
expensesCard.addView(monthlyText)

overviewRow.addView(
    expensesCard,
    weightParams()
)

val remainingCard = createSmallCard()

val remainingLabel = smallLabel("Remaining")

remainingText = TextView(this).apply {
    textSize = 20f
    setTextColor(green)
    typeface = Typeface.DEFAULT_BOLD
}

remainingCard.addView(remainingLabel)
remainingCard.addView(remainingText)

val remainingParams = weightParams()
remainingParams.setMargins(10, 0, 0, 0)

overviewRow.addView(
    remainingCard,
    remainingParams
)

content.addView(
    overviewRow,
    LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
)

val incomeCard = createSmallCard()

val incomeLabel = smallLabel("Income")

incomeDisplay = TextView(this).apply {
    textSize = 20f
    setTextColor(white)
    typeface = Typeface.DEFAULT_BOLD
}

incomeCard.addView(incomeLabel)
incomeCard.addView(incomeDisplay)

val incomeCardParams = LinearLayout.LayoutParams(
    LinearLayout.LayoutParams.MATCH_PARENT,
    LinearLayout.LayoutParams.WRAP_CONTENT
)

incomeCardParams.setMargins(0, 10, 0, 0)

content.addView(
    incomeCard,
    incomeCardParams
)

        val backupCard = createMainCard()

        val exportButton = createGradientButton(
            "Export Backup",
            blueDark,
            blueBright
        )

        exportButton.setOnClickListener {
            exportBackup()
        }

        backupCard.addView(
            exportButton,
            buttonParams()
        )

        val importButton = createGradientButton(
            "Import Backup",
            Color.rgb(35, 40, 52),
            Color.rgb(70, 78, 96)
        )

        importButton.setOnClickListener {
            importBackup()
        }

        backupCard.addView(
            importButton,
            buttonParams()
        )

        val backupHint = TextView(this).apply {
            text = "Export your data before changing the app version or reinstalling it."
            textSize = 12f
            setTextColor(muted)
            setPadding(2, 10, 2, 0)
        }

        backupCard.addView(backupHint)

        content.addView(backupCard)

        scrollView.addView(content)

        mainContent.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            mainContent,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        createMenuDrawer(root)

        setContentView(root)

        loadData()
        updateMonthDisplay()
        updateTotals()
        refreshExpenses()
    }

    private fun createMenuDrawer(
        root: FrameLayout
    ) {
        menuOverlay = View(this).apply {
            setBackgroundColor(
                Color.argb(
                    150,
                    0,
                    0,
                    0
                )
            )

            alpha = 0f
            visibility = View.GONE

            setOnClickListener {
                closeMenu()
            }
        }

        root.addView(
            menuOverlay,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        menuDrawer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(
                Color.rgb(15, 17, 23)
            )
            setPadding(24, 58, 24, 24)
            elevation = 20f
        }

        val menuTitle = TextView(this).apply {
            text = "MENU"
            textSize = 24f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        menuDrawer.addView(
            menuTitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val menuLine = View(this).apply {
            setBackgroundColor(
                Color.rgb(45, 49, 60)
            )
        }

        val lineParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1
        )

        lineParams.setMargins(
            0,
            18,
            0,
            0
        )

        menuDrawer.addView(
            menuLine,
            lineParams
        )

        val historyItem = TextView(this).apply {
            text = "History"
            textSize = 18f
            setTextColor(white)
            setPadding(0, 24, 0, 24)

            setOnClickListener {
                openHistory()
            }
        }

        menuDrawer.addView(
            historyItem,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val drawerWidth =
            (resources.displayMetrics.widthPixels * 0.35f).toInt()

        val drawerParams = FrameLayout.LayoutParams(
            drawerWidth,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        drawerParams.gravity = Gravity.START

        root.addView(
            menuDrawer,
            drawerParams
        )

        menuDrawer.post {
            menuDrawer.translationX =
                -menuDrawer.width.toFloat()
        }
    }

    private fun openMenu() {

        if (isMenuOpen) {
            return
        }

        isMenuOpen = true

        menuOverlay.visibility = View.VISIBLE

        menuOverlay.animate()
            .alpha(1f)
            .setDuration(220)
            .setInterpolator(
                AccelerateDecelerateInterpolator()
            )
            .start()

        menuDrawer.animate()
            .translationX(0f)
            .setDuration(280)
            .setInterpolator(
                AccelerateDecelerateInterpolator()
            )
            .start()
    }

    private fun closeMenu() {

        if (!isMenuOpen) {
            return
        }

        isMenuOpen = false

        menuOverlay.animate()
            .alpha(0f)
            .setDuration(180)
            .setInterpolator(
                AccelerateDecelerateInterpolator()
            )
            .withEndAction {
                menuOverlay.visibility = View.GONE
            }
            .start()

        menuDrawer.animate()
            .translationX(
                -menuDrawer.width.toFloat()
            )
            .setDuration(240)
            .setInterpolator(
                AccelerateDecelerateInterpolator()
            )
            .start()
    }

    private fun openHistory() {

        closeMenu()

        isHistoryOpen = true

        val historyRoot = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
            setPadding(20, 60, 20, 28)
        }

        val historyTitle = TextView(this).apply {
            text = "‹  HISTORY"
            textSize = 24f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 10)

            setOnClickListener {
                isHistoryOpen = false
                buildInterface()
            }
        }

        historyRoot.addView(
            historyTitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val divider = View(this).apply {
            setBackgroundColor(
                Color.rgb(45, 49, 60)
            )
        }

        val dividerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1
        )

        dividerParams.setMargins(
            0,
            8,
            0,
            0
        )

        historyRoot.addView(
            divider,
            dividerParams
        )

        val monthKeys = prefs.all.keys
            .mapNotNull { key ->
                when {
                    key.startsWith("income_") ->
                        key.removePrefix("income_")

                    key.startsWith("expenses_") ->
                        key.removePrefix("expenses_")

                    else -> null
                }
            }
            .filter {
                it.matches(
                    Regex("\\d{4}-\\d{2}")
                )
            }
            .distinct()
            .filter { key ->

                val incomeValue =
                    prefs.getString(
                        "income_$key",
                        "0"
                    )?.toDoubleOrNull() ?: 0.0

                val expensesValue =
                    prefs.getString(
                        "expenses_$key",
                        null
                    )

                val hasExpenses =
                    !expensesValue.isNullOrEmpty() &&
                    try {
                        JSONArray(
                            expensesValue
                        ).length() > 0
                    } catch (_: Exception) {
                        false
                    }

                incomeValue != 0.0 || hasExpenses
            }
            .sortedDescending()

        if (monthKeys.isEmpty()) {

            val emptyText = TextView(this).apply {
                text = "No history yet"
                textSize = 20f
                setTextColor(white)
                gravity = Gravity.CENTER
                setPadding(0, 80, 0, 12)
            }

            historyRoot.addView(
                emptyText,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

        } else {

            for (key in monthKeys) {

                val parts = key.split("-")

                val year = parts[0].toInt()
                val month = parts[1].toInt() - 1

                val monthName =
                    SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.ENGLISH
                    ).format(
                        Calendar.getInstance().apply {
                            set(
                                year,
                                month,
                                1
                            )
                        }.time
                    )

                val monthItem = TextView(this).apply {
                    text = monthName
                    textSize = 18f
                    setTextColor(white)
                    setPadding(0, 22, 0, 22)

                    setOnClickListener {
                        selectedYear = year
                        selectedMonth = month
                        isHistoryOpen = false
                        buildInterface()
                    }
                }

                historyRoot.addView(
                    monthItem,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }

        setContentView(historyRoot)
    }

    override fun onBackPressed() {

        if (isMenuOpen) {
            closeMenu()
            return
        }

        if (isHistoryOpen) {
            isHistoryOpen = false
            buildInterface()
            return
        }

        super.onBackPressed()
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
            .ifEmpty {
                "No description"
            }

        val amount = amountInput.text
            .toString()
            .replace(",", ".")
            .toDoubleOrNull()

        val category = categorySpinner
            .selectedItem
            ?.toString()
            ?: "Other"

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

    private fun deleteExpense(
        index: Int
    ) {

        if (
            index < 0 ||
            index >= expenses.size
        ) {
            return
        }

        expenses.removeAt(index)

        saveData()
        updateTotals()
        refreshExpenses()
    }

    private fun updateTotals() {

        val totalExpenses =
            expenses.sumOf {
                it.amount
            }

        val remaining =
            income - totalExpenses

        monthlyText.text =
            money(totalExpenses)

        remainingText.text =
            money(remaining)

        incomeDisplay.text =
            money(income)

        remainingText.setTextColor(
            if (remaining < 0) {
                Color.rgb(
                    255,
                    95,
                    95
                )
            } else {
                blueBright
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

    private fun incomeKey(): String {
        return "income_${currentMonthKey()}"
    }

    private fun expensesKey(): String {
        return "expenses_${currentMonthKey()}"
    }

    private fun saveData() {

        val expensesArray = JSONArray()

        for (expense in expenses) {

            val item = JSONObject()

            item.put(
                "description",
                expense.description
            )

            item.put(
                "category",
                expense.category
            )

            item.put(
                "amount",
                expense.amount
            )

            expensesArray.put(item)
        }

        prefs.edit()
            .putString(
                incomeKey(),
                income.toString()
            )
            .putString(
                expensesKey(),
                expensesArray.toString()
            )
            .apply()
    }

    private fun loadData() {

        income = prefs.getString(
            incomeKey(),
            "0"
        )?.toDoubleOrNull() ?: 0.0

        expenses.clear()

        val storedExpenses = prefs.getString(
            expensesKey(),
            null
        )

        if (!storedExpenses.isNullOrEmpty()) {

            try {

                val array =
                    JSONArray(storedExpenses)

                for (
                    index in 0 until array.length()
                ) {

                    val item =
                        array.getJSONObject(index)

                    expenses.add(
                        Expense(
                            description =
                                item.optString(
                                    "description",
                                    "No description"
                                ),
                            category =
                                item.optString(
                                    "category",
                                    "Other"
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

            return
        }

        val migrationDone =
            prefs.getBoolean(
                "legacy_data_migrated",
                false
            )

        if (!migrationDone) {

            val legacyIncome =
                prefs.getString(
                    "income",
                    null
                )

            val legacyExpenses =
                prefs.getString(
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

                        val array =
                            JSONArray(
                                legacyExpenses
                            )

                        for (
                            index in 0 until array.length()
                        ) {

                            val item =
                                array.getJSONObject(
                                    index
                                )

                            expenses.add(
                                Expense(
                                    description =
                                        item.optString(
                                            "description",
                                            "No description"
                                        ),
                                    category =
                                        item.optString(
                                            "category",
                                            "Other"
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

    private fun changeMonth(
        direction: Int
    ) {

        val calendar =
            Calendar.getInstance()

        calendar.set(
            selectedYear,
            selectedMonth,
            1
        )

        calendar.add(
            Calendar.MONTH,
            direction
        )

        selectedYear =
            calendar.get(
                Calendar.YEAR
            )

        selectedMonth =
            calendar.get(
                Calendar.MONTH
            )

        loadData()
        updateMonthDisplay()
        updateTotals()
        refreshExpenses()
    }

    private fun updateMonthDisplay() {

        val calendar =
            Calendar.getInstance()

        calendar.set(
            selectedYear,
            selectedMonth,
            1
        )

        val formatter =
            SimpleDateFormat(
                "MMMM yyyy",
                Locale.ENGLISH
            )

        monthText.text =
            formatter.format(
                calendar.time
            ).replaceFirstChar {
                it.uppercase()
            }
    }

    private fun showMonthPicker() {

        val dialogLayout =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    24,
                    8,
                    24,
                    8
                )
            }

        val yearPicker =
            NumberPicker(this).apply {
                minValue = 2000
                maxValue = 2100
                value = selectedYear
            }

        val monthPicker =
            NumberPicker(this).apply {
                minValue = 0
                maxValue = 11
                value = selectedMonth

                displayedValues =
                    arrayOf(
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

        dialogLayout.addView(
            yearPicker,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        dialogLayout.addView(
            monthPicker,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        AlertDialog.Builder(this)
            .setTitle("Select month")
            .setView(dialogLayout)
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "OK"
            ) { _, _ ->

                selectedYear =
                    yearPicker.value

                selectedMonth =
                    monthPicker.value

                loadData()
                updateMonthDisplay()
                updateTotals()
                refreshExpenses()
            }
            .show()
    }

    private fun refreshExpenses() {

        expensesContainer.removeAllViews()

        if (expenses.isEmpty()) {

            val emptyCard =
                LinearLayout(this).apply {
                    orientation =
                        LinearLayout.VERTICAL

                    gravity = Gravity.CENTER

                    setPadding(
                        20,
                        22,
                        20,
                        22
                    )

                    background =
                        roundedBackground(
                            cardColor,
                            16f
                        )
                }

            val emptyTitle =
                TextView(this).apply {
                    text =
                        "No expenses this month"

                    textSize = 15f

                    setTextColor(
                        white
                    )

                    typeface =
                        Typeface.DEFAULT_BOLD

                    gravity =
                        Gravity.CENTER
                }

            val emptySubtitle =
                TextView(this).apply {
                    text =
                        "Your spending will appear here"

                    textSize = 12f

                    setTextColor(
                        muted
                    )

                    gravity =
                        Gravity.CENTER

                    setPadding(
                        0,
                        5,
                        0,
                        0
                    )
                }

            emptyCard.addView(
                emptyTitle
            )

            emptyCard.addView(
                emptySubtitle
            )

            expensesContainer.addView(
                emptyCard,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            return
        }

        for (
            index in expenses.indices.reversed()
        ) {

            val expense =
                expenses[index]

            val expenseCard =
                LinearLayout(this).apply {
                    orientation =
                        LinearLayout.HORIZONTAL

                    gravity =
                        Gravity.CENTER_VERTICAL

                    setPadding(
                        16,
                        15,
                        12,
                        15
                    )

                    background =
                        roundedBackground(
                            cardColor,
                            16f
                        )
                }

            val accent =
                View(this).apply {
                    setBackgroundColor(
                        blueBright
                    )
                }

            expenseCard.addView(
                accent,
                LinearLayout.LayoutParams(
                    3,
                    48
                )
            )

            val details =
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

                    textSize = 16f

                    setTextColor(
                        white
                    )

                    typeface =
                        Typeface.DEFAULT_BOLD
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
                        4,
                        0,
                        0
                    )
                }

            details.addView(
                description
            )

            details.addView(
                category
            )

            expenseCard.addView(
                details,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val rightColumn =
                LinearLayout(this).apply {
                    orientation =
                        LinearLayout.VERTICAL

                    gravity =
                        Gravity.END
                }

            val amountText =
                TextView(this).apply {
                    text =
                        money(
                            expense.amount
                        )

                    textSize = 16f

                    setTextColor(
                        white
                    )

                    typeface =
                        Typeface.DEFAULT_BOLD

                    gravity =
                        Gravity.END
                }

            rightColumn.addView(
                amountText
            )

            val deleteButton =
                TextView(this).apply {
                    text = "Delete"

                    textSize = 12f

                    setTextColor(
                        blueBright
                    )

                    gravity =
                        Gravity.END

                    setPadding(
                        8,
                        5,
                        0,
                        0
                    )

                    setOnClickListener {
                        deleteExpense(
                            index
                        )
                    }
                }

            rightColumn.addView(
                deleteButton
            )

            expenseCard.addView(
                rightColumn,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            val params =
                LinearLayout.LayoutParams(
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
            textSize = 17f
            setTextColor(color)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.04f
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

    private fun createSmallCard():
        LinearLayout {

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL

            setPadding(
                16,
                16,
                16,
                16
            )

            background =
                roundedBackground(
                    cardColor,
                    16f
                )
        }
    }

    private fun createMainCard():
        LinearLayout {

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL

            setPadding(
                16,
                16,
                16,
                16
            )

            background =
                gradientBackground(
                    Color.rgb(
                        18,
                        20,
                        27
                    ),
                    Color.rgb(
                        24,
                        31,
                        45
                    ),
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

            background =
                roundedBackground(
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

            background =
                gradientBackground(
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
            textSize = 31f
            setTextColor(blueBright)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT
            setPadding(
                4,
                0,
                4,
                2
            )

            background =
                roundedBackground(
                    Color.rgb(
                        24,
                        35,
                        54
                    ),
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

        val params =
            LinearLayout.LayoutParams(
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

        val params =
            LinearLayout.LayoutParams(
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

        val params =
            LinearLayout.LayoutParams(
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

    private fun exportBackup() {

        val intent =
            Intent(
                Intent.ACTION_CREATE_DOCUMENT
            ).apply {
                addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                type =
                    "application/json"

                putExtra(
                    Intent.EXTRA_TITLE,
                    "MoneyTrack_Backup.json"
                )
            }

        startActivityForResult(
            intent,
            REQUEST_EXPORT_BACKUP
        )
    }

    private fun importBackup() {

        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT
            ).apply {
                addCategory(
                    Intent.CATEGORY_OPENABLE
                )

                type =
                    "application/json"
            }

        startActivityForResult(
            intent,
            REQUEST_IMPORT_BACKUP
        )
    }

    private fun createBackupJson():
        JSONObject {

        val root =
            JSONObject()

        root.put(
            "format",
            "MoneyTrack Backup"
        )

        root.put(
            "version",
            1
        )

        val data =
            JSONObject()

        for (
            (key, value) in prefs.all
        ) {

            when (value) {

                is String -> {
                    data.put(
                        key,
                        value
                    )
                }

                is Boolean -> {
                    data.put(
                        key,
                        value
                    )
                }

                is Int -> {
                    data.put(
                        key,
                        value
                    )
                }

                is Long -> {
                    data.put(
                        key,
                        value
                    )
                }

                is Float -> {
                    data.put(
                        key,
                        value
                    )
                }

                is Double -> {
                    data.put(
                        key,
                        value
                    )
                }
            }
        }

        root.put(
            "data",
            data
        )

        return root
    }

    private fun restoreBackupJson(
        jsonText: String
    ) {

        try {

            val root =
                JSONObject(jsonText)

            if (
                root.optString(
                    "format"
                ) != "MoneyTrack Backup"
            ) {

                Toast.makeText(
                    this,
                    "Invalid MoneyTrack backup.",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            val version =
                root.optInt(
                    "version",
                    0
                )

            if (version != 1) {

                Toast.makeText(
                    this,
                    "Unsupported backup version.",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            val data =
                root.optJSONObject(
                    "data"
                )

            if (data == null) {

                Toast.makeText(
                    this,
                    "Backup contains no data.",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            val editor =
                prefs.edit()

            editor.clear()

            val keys =
                data.keys()

            while (keys.hasNext()) {

                val key =
                    keys.next()

                val value =
                    data.get(key)

                when (value) {

                    is String -> {
                        editor.putString(
                            key,
                            value
                        )
                    }

                    is Boolean -> {
                        editor.putBoolean(
                            key,
                            value
                        )
                    }

                    is Int -> {
                        editor.putInt(
                            key,
                            value
                        )
                    }

                    is Long -> {
                        editor.putLong(
                            key,
                            value
                        )
                    }

                    is Double -> {
                        editor.putFloat(
                            key,
                            value.toFloat()
                        )
                    }
                }
            }

            editor.apply()

            loadData()
            updateMonthDisplay()
            updateTotals()
            refreshExpenses()

            Toast.makeText(
                this,
                "Backup imported successfully.",
                Toast.LENGTH_LONG
            ).show()

        } catch (
            exception: Exception
        ) {

            Toast.makeText(
                this,
                "Could not import backup.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            resultCode != RESULT_OK ||
            data?.data == null
        ) {
            return
        }

        val uri =
            data.data ?: return

        when (requestCode) {

            REQUEST_EXPORT_BACKUP -> {

                try {

                    val backup =
                        createBackupJson()

                    contentResolver
                        .openOutputStream(uri)
                        ?.use { outputStream ->

                            outputStream.write(
                                backup
                                    .toString(2)
                                    .toByteArray(
                                        Charsets.UTF_8
                                    )
                            )
                        }

                    Toast.makeText(
                        this,
                        "Backup exported successfully.",
                        Toast.LENGTH_LONG
                    ).show()

                } catch (
                    exception: Exception
                ) {

                    Toast.makeText(
                        this,
                        "Could not export backup.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            REQUEST_IMPORT_BACKUP -> {

                try {

                    val jsonText =
                        contentResolver
                            .openInputStream(uri)
                            ?.use { inputStream ->

                                inputStream
                                    .bufferedReader()
                                    .use {
                                        it.readText()
                                    }
                            }

                    if (
                        jsonText.isNullOrEmpty()
                    ) {

                        Toast.makeText(
                            this,
                            "Backup file is empty.",
                            Toast.LENGTH_SHORT
                        ).show()

                        return
                    }

                    restoreBackupJson(
                        jsonText
                    )

                } catch (
                    exception: Exception
                ) {

                    Toast.makeText(
                        this,
                        "Could not read backup.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
