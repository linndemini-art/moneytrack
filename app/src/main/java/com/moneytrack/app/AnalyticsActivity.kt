package com.moneytrack.app

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class AnalyticsActivity : Activity() {

    private val backgroundColor = Color.rgb(7, 8, 12)
    private val cardColor = Color.rgb(20, 22, 29)

    private val white = Color.WHITE
    private val secondary = Color.rgb(168, 174, 187)
    private val muted = Color.rgb(115, 121, 135)

    private val blue = Color.rgb(74, 145, 255)
    private val green = Color.rgb(72, 190, 110)
    private val amber = Color.rgb(255, 166, 52)

    private val prefs by lazy {
        getSharedPreferences(
            "moneytrack_data",
            Context.MODE_PRIVATE
        )
    }

    private var selectedYear = 0
    private var selectedMonth = 0

    data class ExpenseData(
        val description: String,
        val category: String,
        val amount: Double
    )

    data class MonthData(
        val year: Int,
        val month: Int,
        val income: Double,
        val expenses: Double
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        val calendar = Calendar.getInstance()

        selectedYear =
            calendar.get(Calendar.YEAR)

        selectedMonth =
            calendar.get(Calendar.MONTH)

        buildInterface()
    }

    private fun buildInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
        }

        val scroll = ScrollView(this).apply {
            setBackgroundColor(backgroundColor)
            isFillViewport = true
            overScrollMode =
                ScrollView.OVER_SCROLL_NEVER
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(20),
                dp(70),
                dp(20),
                dp(32)
            )
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = TextView(this).apply {
            text = "←"
            textSize = 28f
            setTextColor(white)
            gravity = Gravity.CENTER

            setOnClickListener {
                finish()
            }
        }

        header.addView(
            back,
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        val title = TextView(this).apply {
            text = "ANALYTICS"
            textSize = 23f
            setTextColor(white)

            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )

            letterSpacing = 0.04f
        }

        header.addView(
            title,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val monthLabel = TextView(this).apply {
            text = selectedMonthName()
            textSize = 14f
            setTextColor(secondary)
            gravity = Gravity.END
        }

        header.addView(monthLabel)

        content.addView(header)

        val intro = TextView(this).apply {
            text =
                "Your spending, income and monthly trends"

            textSize = 13f
            setTextColor(muted)

            setPadding(
                dp(48),
                0,
                0,
                dp(20)
            )
        }

        content.addView(intro)

        content.addView(
            sectionTitle(
                "MONTHLY SPENDING BREAKDOWN"
            ),
            sectionParams()
        )

        content.addView(
            buildMonthlyBreakdown(),
            cardParams()
        )

        content.addView(
            sectionTitle(
                "INCOME VS EXPENSES"
            ),
            sectionParams()
        )

        content.addView(
            buildIncomeExpenses(),
            cardParams()
        )

        content.addView(
            sectionTitle(
                "SPENDING TREND"
            ),
            sectionParams()
        )

        content.addView(
            buildSpendingTrend(),
            cardParams()
        )

        val current = loadMonthData(
            selectedYear,
            selectedMonth
        )

        val days = daysInMonth(
            selectedYear,
            selectedMonth
        )

        val dailyAverage =
            if (days > 0) {
                current.expenses / days
            } else {
                0.0
            }

        val savingsRate =
            if (current.income > 0.0) {
                (
                    (
                        current.income -
                            current.expenses
                    ) /
                        current.income
                    ) * 100.0
            } else {
                0.0
            }

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        stats.addView(
            createStatCard(
                "Daily Average",
                money(dailyAverage)
            ),
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val savingsParams =
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

        savingsParams.setMargins(
            dp(10),
            0,
            0,
            0
        )

        stats.addView(
            createStatCard(
                "Savings Rate",
                String.format(
                    Locale.US,
                    "%.0f%%",
                    savingsRate
                )
            ),
            savingsParams
        )

        content.addView(stats)

        content.addView(
            sectionTitle(
                "YEARLY OVERVIEW"
            ),
            sectionParams()
        )

        content.addView(
            buildYearlyOverview(),
            cardParams()
        )

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)
    }

    private fun buildMonthlyBreakdown(): LinearLayout {

        val card = createCard()

        val totals = loadCategoryTotals()
        val total = totals.values.sum()

        if (totals.isEmpty()) {
            card.addView(
                emptyText(
                    "No expenses recorded this month."
                )
            )

            return card
        }

        for ((category, amount) in totals) {

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL

                setPadding(
                    0,
                    dp(5),
                    0,
                    dp(8)
                )
            }

            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val categoryText = TextView(
                this@AnalyticsActivity
            ).apply {
                text = category
                textSize = 14f
                setTextColor(white)
            }

            line.addView(
                categoryText,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val percent =
                if (total > 0.0) {
                    amount / total * 100.0
                } else {
                    0.0
                }

            val amountText = TextView(
                this@AnalyticsActivity
            ).apply {
                text = String.format(
                    Locale.US,
                    "%.0f%%   %s",
                    percent,
                    money(amount)
                )

                textSize = 13f
                setTextColor(secondary)
                gravity = Gravity.END
            }

            line.addView(amountText)

            row.addView(line)

            val progress = ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {

                max = 100

                progress =
                    percent
                        .roundToInt()
                        .coerceIn(0, 100)

                progressTintList =
                    android.content.res.ColorStateList.valueOf(
                        blue
                    )

                progressBackgroundTintList =
                    android.content.res.ColorStateList.valueOf(
                        Color.rgb(
                            43,
                            46,
                            56
                        )
                    )
            }

            row.addView(
                progress,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(5)
                )
            )

            card.addView(row)
        }

        return card
    }
        private fun buildIncomeExpenses(): LinearLayout {

        val card = createCard()

        val data = loadMonthData(
            selectedYear,
            selectedMonth
        )

        val spentPercent =
            if (data.income > 0.0) {
                data.expenses /
                    data.income *
                    100.0
            } else {
                0.0
            }

        addMetricRow(
            card,
            "Income",
            money(data.income),
            green
        )

        addMetricRow(
            card,
            "Expenses",
            money(data.expenses),
            amber
        )

        addMetricRow(
            card,
            "Remaining",
            money(
                data.income -
                    data.expenses
            ),
            blue
        )

        val summary = TextView(this).apply {

            text =
                if (data.income > 0.0) {

                    String.format(
                        Locale.US,
                        "%.0f%% of income spent",
                        spentPercent
                    )

                } else {

                    "No income recorded this month"
                }

            textSize = 13f
            setTextColor(muted)
            gravity = Gravity.CENTER

            setPadding(
                0,
                dp(12),
                0,
                0
            )
        }

        card.addView(summary)

        return card
    }

    private fun addMetricRow(
        card: LinearLayout,
        label: String,
        value: String,
        valueColor: Int
    ) {

        val row = LinearLayout(this).apply {
            orientation =
                LinearLayout.HORIZONTAL

            gravity =
                Gravity.CENTER_VERTICAL

            setPadding(
                0,
                dp(7),
                0,
                dp(7)
            )
        }

        val labelView = TextView(this).apply {
            text = label
            textSize = 14f
            setTextColor(secondary)
        }

        row.addView(
            labelView,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val valueView = TextView(this).apply {
            text = value
            textSize = 17f
            setTextColor(valueColor)

            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )
        }

        row.addView(valueView)

        card.addView(row)
    }

    private fun buildSpendingTrend(): LinearLayout {

        val card = createCard()

        val data = sixMonthData()

        if (data.all {
                it.expenses <= 0.0
            }) {

            card.addView(
                emptyText(
                    "No spending recorded in the last six months."
                )
            )

            return card
        }

        val maxValue =
            maxOf(
                data.maxOf {
                    it.expenses
                },
                1.0
            )

        for (item in data) {

            val row = LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    0,
                    dp(5),
                    0,
                    dp(7)
                )
            }

            val line = LinearLayout(this).apply {
                orientation =
                    LinearLayout.HORIZONTAL
            }

            val monthText = TextView(
                this@AnalyticsActivity
            ).apply {
                text = monthShort(
                    item.year,
                    item.month
                )

                textSize = 14f
                setTextColor(white)
            }

            line.addView(
                monthText,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val amountText = TextView(
                this@AnalyticsActivity
            ).apply {
                text = money(item.expenses)
                textSize = 13f
                setTextColor(secondary)
                gravity = Gravity.END
            }

            line.addView(amountText)

            row.addView(line)

            val progress = ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {

                max = 100

                progress =
                    (
                        item.expenses /
                            maxValue *
                            100.0
                    )
                        .roundToInt()
                        .coerceIn(0, 100)

                progressTintList =
                    android.content.res.ColorStateList.valueOf(
                        blue
                    )

                progressBackgroundTintList =
                    android.content.res.ColorStateList.valueOf(
                        Color.rgb(
                            43,
                            46,
                            56
                        )
                    )
            }

            row.addView(
                progress,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(5)
                )
            )

            card.addView(row)
        }

        return card
    }

    private fun buildYearlyOverview(): LinearLayout {

        val card = createCard()

        val year = selectedYear

        var yearlyIncome = 0.0
        var yearlyExpenses = 0.0

        for (month in 0..11) {

            val data = loadMonthData(
                year,
                month
            )

            yearlyIncome += data.income
            yearlyExpenses += data.expenses

            if (
                data.income <= 0.0 &&
                data.expenses <= 0.0
            ) {
                continue
            }

            val row = LinearLayout(this).apply {
                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    0,
                    dp(7),
                    0,
                    dp(7)
                )
            }

            val monthText = TextView(this).apply {
                text = monthShort(
                    year,
                    month
                )

                textSize = 14f
                setTextColor(white)
            }

            row.addView(
                monthText,
                LinearLayout.LayoutParams(
                    dp(55),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            val incomeText =
                TextView(this@AnalyticsActivity).apply {
                    text = money(data.income)
                    textSize = 13f
                    setTextColor(green)
                    gravity = Gravity.END
                }

            row.addView(
                incomeText,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val expenseText =
                TextView(this@AnalyticsActivity).apply {
                    text = money(data.expenses)
                    textSize = 13f
                    setTextColor(amber)
                    gravity = Gravity.END
                }

            row.addView(
                expenseText,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            card.addView(row)
        }

        val divider = TextView(this).apply {
            setBackgroundColor(
                Color.rgb(
                    43,
                    46,
                    56
                )
            )
        }

        card.addView(
            divider,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            )
        )

        val totalRow = LinearLayout(this).apply {
            orientation =
                LinearLayout.HORIZONTAL

            gravity =
                Gravity.CENTER_VERTICAL

            setPadding(
                0,
                dp(12),
                0,
                0
            )
        }

        val totalLabel = TextView(this).apply {
            text = "YEAR TOTAL"
            textSize = 13f
            setTextColor(secondary)

            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )
        }

        totalRow.addView(
            totalLabel,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val totalText = TextView(this).apply {
            text = String.format(
                Locale.US,
                "%s  /  %s",
                money(yearlyIncome),
                money(yearlyExpenses)
            )

            textSize = 13f
            setTextColor(white)

            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )

            gravity = Gravity.END
        }

        totalRow.addView(totalText)

        card.addView(totalRow)

        return card
    }
        private fun createStatCard(
        label: String,
        value: String
    ): LinearLayout {

        val card = createCard()

        val labelView = TextView(this).apply {
            text = label
            textSize = 12f
            setTextColor(muted)
        }

        card.addView(labelView)

        val valueView = TextView(this).apply {
            text = value
            textSize = 18f
            setTextColor(white)

            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )

            setPadding(
                0,
                dp(5),
                0,
                0
            )
        }

        card.addView(valueView)

        return card
    }

    private fun createCard(): LinearLayout {

        return LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL

            setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
            )

            background =
                roundedBackground(cardColor)

            elevation = dp(2).toFloat()
        }
    }

    private fun emptyText(
        text: String
    ): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(muted)
            gravity = Gravity.CENTER

            setPadding(
                0,
                dp(8),
                0,
                dp(8)
            )
        }
    }

    private fun sectionTitle(
        text: String
    ): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(muted)

            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )

            letterSpacing = 0.06f
        }
    }

    private fun sectionParams():
        LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(
                0,
                dp(18),
                0,
                dp(8)
            )
        }
    }

    private fun cardParams():
        LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(
                0,
                0,
                0,
                dp(4)
            )
        }
    }

    private fun roundedBackground(
        color: Int
    ): android.graphics.drawable.GradientDrawable {

        return android.graphics.drawable
            .GradientDrawable().apply {

                setColor(color)

                cornerRadius =
                    dp(16).toFloat()
            }
    }

    private fun currentMonthKey(
        year: Int,
        month: Int
    ): String {

        return String.format(
            Locale.US,
            "%04d-%02d",
            year,
            month + 1
        )
    }

    private fun loadMonthData(
        year: Int,
        month: Int
    ): MonthData {

        val key =
            currentMonthKey(
                year,
                month
            )

        val income =
            prefs.getString(
                "income_" + key,
                "0"
            )?.toDoubleOrNull()
                ?: 0.0

        val expenses =
            loadExpenses(
                year,
                month
            ).sumOf {
                it.amount
            }

        return MonthData(
            year,
            month,
            income,
            expenses
        )
    }

    private fun loadExpenses(
        year: Int,
        month: Int
    ): List<ExpenseData> {

        val key =
            currentMonthKey(
                year,
                month
            )

        val stored =
            prefs.getString(
                "expenses_" + key,
                null
            )

        if (stored.isNullOrEmpty()) {
            return emptyList()
        }

        return try {

            val array =
                JSONArray(stored)

            val result =
                mutableListOf<ExpenseData>()

            for (index in 0 until array.length()) {

                val item =
                    array.getJSONObject(index)

                result.add(
                    ExpenseData(
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

            result

        } catch (exception: Exception) {

            emptyList()
        }
    }

    private fun loadCategoryTotals():
        Map<String, Double> {

        val totals =
            linkedMapOf<String, Double>()

        val expenses =
            loadExpenses(
                selectedYear,
                selectedMonth
            )

        for (expense in expenses) {

            totals[expense.category] =
                (
                    totals[expense.category]
                        ?: 0.0
                ) + expense.amount
        }

        return totals
            .filter {
                it.value > 0.0
            }
            .toList()
            .sortedByDescending {
                it.second
            }
            .toMap(
                LinkedHashMap()
            )
    }
            private fun sixMonthData():
        List<MonthData> {

        val result =
            mutableListOf<MonthData>()

        val calendar =
            Calendar.getInstance()

        calendar.set(
            selectedYear,
            selectedMonth,
            1
        )

        calendar.add(
            Calendar.MONTH,
            -5
        )

        repeat(6) {

            val year =
                calendar.get(
                    Calendar.YEAR
                )

            val month =
                calendar.get(
                    Calendar.MONTH
                )

            result.add(
                loadMonthData(
                    year,
                    month
                )
            )

            calendar.add(
                Calendar.MONTH,
                1
            )
        }

        return result
    }

    private fun daysInMonth(
        year: Int,
        month: Int
    ): Int {

        val calendar =
            Calendar.getInstance()

        calendar.set(
            year,
            month,
            1
        )

        return calendar.getActualMaximum(
            Calendar.DAY_OF_MONTH
        )
    }

    private fun selectedMonthName():
        String {

        val calendar =
            Calendar.getInstance()

        calendar.set(
            selectedYear,
            selectedMonth,
            1
        )

        return SimpleDateFormat(
            "MMMM yyyy",
            Locale.ENGLISH
        ).format(
            calendar.time
        )
    }

    private fun monthShort(
        year: Int,
        month: Int
    ): String {

        val calendar =
            Calendar.getInstance()

        calendar.set(
            year,
            month,
            1
        )

        return SimpleDateFormat(
            "MMM",
            Locale.ENGLISH
        ).format(
            calendar.time
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

    private fun dp(
        value: Int
    ): Int {

        return (
            value *
                resources.displayMetrics.density
            ).roundToInt()
    }

    override fun onBackPressed() {
        finish()
    }
}
    
