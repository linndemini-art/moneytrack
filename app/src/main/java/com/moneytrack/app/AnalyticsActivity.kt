package com.moneytrack.app

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt


class AnalyticsActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences(
            "moneytrack_data",
            MODE_PRIVATE
        )
    }

    private val backgroundColor =
        Color.rgb(7, 8, 12)

    private val cardColor =
        Color.rgb(20, 22, 29)

    private val surfaceColor =
        Color.rgb(28, 31, 40)

    private val amber =
        Color.rgb(255, 166, 52)

    private val blue =
        Color.rgb(38, 104, 210)

    private val red =
        Color.rgb(220, 70, 70)

    private val lightBlue =
        Color.rgb(80, 150, 230)

    private val purple =
        Color.rgb(145, 90, 200)

    private val grey =
        Color.rgb(110, 116, 130)

    private val green =
        Color.rgb(72, 190, 110)

    private val white =
        Color.WHITE

    private val secondary =
        Color.rgb(168, 174, 187)

    private val muted =
        Color.rgb(115, 121, 135)


    private var selectedYear = 0
    private var selectedMonth = 0

    private var income = 0.0

    private val expenses =
        mutableListOf<Expense>()


    data class Expense(
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


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        window.statusBarColor =
            backgroundColor

        window.navigationBarColor =
            backgroundColor

        val calendar =
            Calendar.getInstance()

        selectedYear =
            calendar.get(Calendar.YEAR)

        selectedMonth =
            calendar.get(Calendar.MONTH)

        loadSelectedMonth()

        buildInterface()
    }


    private fun monthKey(
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


    private fun incomeKey(
        year: Int,
        month: Int
    ): String {

        return "income_${monthKey(year, month)}"
    }


    private fun expensesKey(
        year: Int,
        month: Int
    ): String {

        return "expenses_${monthKey(year, month)}"
    }


    private fun loadSelectedMonth() {

        income =
            prefs.getString(
                incomeKey(
                    selectedYear,
                    selectedMonth
                ),
                "0"
            )?.toDoubleOrNull()
                ?: 0.0

        expenses.clear()

        val storedExpenses =
            prefs.getString(
                expensesKey(
                    selectedYear,
                    selectedMonth
                ),
                null
            )

        if (
            storedExpenses.isNullOrEmpty()
        ) {
            return
        }

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
    }


    private fun totalExpenses(): Double {

        return expenses.sumOf {
            it.amount
        }
    }


    private fun remainingMoney(): Double {

        return income - totalExpenses()
    }


    private fun categoryTotals():
            Map<String, Double> {

        val totals =
            linkedMapOf<String, Double>()

        for (category in categories) {
            totals[category] = 0.0
        }

        for (expense in expenses) {

            totals[expense.category] =
                (totals[expense.category] ?: 0.0) +
                    expense.amount
        }

        return totals
    }


    private fun savingsRate(): Int {

        if (income <= 0.0) {
            return 0
        }

        val remaining =
            remainingMoney()

        return (
            (remaining / income) * 100.0
        ).roundToInt()
            .coerceIn(-100, 100)
    }


    private fun dailyAverage(): Double {

        val total =
            totalExpenses()

        if (total <= 0.0) {
            return 0.0
        }

        val calendar =
            Calendar.getInstance()

        calendar.set(
            selectedYear,
            selectedMonth,
            1
        )

        val daysInMonth =
            calendar.getActualMaximum(
                Calendar.DAY_OF_MONTH
            )

        return total / daysInMonth
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
                calendar.get(Calendar.YEAR)

            val month =
                calendar.get(Calendar.MONTH)

            val monthIncome =
                prefs.getString(
                    incomeKey(year, month),
                    "0"
                )?.toDoubleOrNull()
                    ?: 0.0

            val monthExpenses =
                readExpensesTotal(
                    year,
                    month
                )

            result.add(
                MonthData(
                    year = year,
                    month = month,
                    income = monthIncome,
                    expenses = monthExpenses
                )
            )

            calendar.add(
                Calendar.MONTH,
                1
            )
        }

        return result
    }


    private fun readExpensesTotal(
        year: Int,
        month: Int
    ): Double {

        val stored =
            prefs.getString(
                expensesKey(year, month),
                null
            )

        if (stored.isNullOrEmpty()) {
            return 0.0
        }

        return try {

            val array =
                JSONArray(stored)

            var total = 0.0

            for (
                index in 0 until array.length()
            ) {

                val item =
                    array.getJSONObject(index)

                total +=
                    item.optDouble(
                        "amount",
                        0.0
                    )
            }

            total

        } catch (
            exception: Exception
        ) {

            0.0
        }
    }


    private fun money(
        amount: Double
    ): String {

        return NumberFormat
            .getCurrencyInstance(
                Locale.GERMANY
            )
            .format(amount)
    }


    private fun monthName(
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

        return calendar.getDisplayName(
            Calendar.MONTH,
            Calendar.LONG,
            Locale.ENGLISH
        ) ?: ""
    }
        private fun buildInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20, 60, 20, 16)
        }

        val backButton = TextView(this).apply {
            text = "‹"
            textSize = 36f
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(0, 0, 18, 0)

            setOnClickListener {
                finish()
            }
        }

        header.addView(
            backButton,
            LinearLayout.LayoutParams(
                55,
                60
            )
        )

        val title = TextView(this).apply {
            text = "ANALYTICS"
            textSize = 24f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
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

        val monthTitle = TextView(this).apply {
            text = monthName(
                selectedYear,
                selectedMonth
            ) + " " + selectedYear

            textSize = 14f
            setTextColor(secondary)
            gravity = Gravity.CENTER_VERTICAL
        }

        header.addView(
            monthTitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                60
            )
        )

        root.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )


        val scrollView = ScrollView(this).apply {
            setFillViewport(true)
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 92, 20, 32)
        }

        scrollView.addView(content)

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )


        val overviewTitle =
            sectionTitle("MONTHLY OVERVIEW")

        content.addView(
            overviewTitle,
            sectionParams()
        )


        val overviewCard =
            createCard()

        val incomeValue =
            createValueText(
                money(income),
                green
            )

        overviewCard.addView(
            createLabel("Income")
        )

        overviewCard.addView(
            incomeValue,
            valueParams()
        )


        val expenseValue =
            createValueText(
                money(totalExpenses()),
                red
            )

        overviewCard.addView(
            createLabel("Expenses")
        )

        overviewCard.addView(
            expenseValue,
            valueParams()
        )


        val remaining =
            remainingMoney()

        val remainingValue =
            createValueText(
                money(remaining),
                if (remaining >= 0)
                    green
                else
                    red
            )

        overviewCard.addView(
            createLabel("Remaining")
        )

        overviewCard.addView(
            remainingValue,
            valueParams()
        )

        content.addView(
            overviewCard,
            cardParams()
        )


        val breakdownTitle =
            sectionTitle(
                "SPENDING BY CATEGORY"
            )

        content.addView(
            breakdownTitle,
            sectionParams()
        )


        val categoryCard =
            createCard()

        val categoryTotals =
            categoryTotals()

        val totalSpent =
            totalExpenses()

        for (category in categories) {

            val amount =
                categoryTotals[category]
                    ?: 0.0

            if (amount <= 0.0) {
                continue
            }

            val percentage =
                if (totalSpent > 0.0) {
                    ((amount / totalSpent) * 100)
                        .roundToInt()
                } else {
                    0
                }

            val row =
                LinearLayout(this).apply {
                    orientation =
                        LinearLayout.HORIZONTAL
                    gravity =
                        Gravity.CENTER_VERTICAL
                    setPadding(
                        0,
                        10,
                        0,
                        10
                    )
                }

            val categoryName =
                TextView(this).apply {
                    text = category
                    textSize = 15f
                    setTextColor(white)
                }

            row.addView(
                categoryName,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val percentText =
                TextView(this).apply {
                    text = "$percentage%"
                    textSize = 14f
                    setTextColor(secondary)
                    gravity = Gravity.END
                }

            row.addView(
                percentText,
                LinearLayout.LayoutParams(
                    55,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            val amountText =
                TextView(this).apply {
                    text = money(amount)
                    textSize = 14f
                    setTextColor(white)
                    gravity = Gravity.END
                }

            row.addView(
                amountText,
                LinearLayout.LayoutParams(
                    95,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            categoryCard.addView(row)
        }

        if (totalSpent <= 0.0) {

            categoryCard.addView(
                createEmptyText(
                    "No expenses recorded this month."
                )
            )
        }

        content.addView(
            categoryCard,
            cardParams()
        )


        val ratioTitle =
            sectionTitle(
                "INCOME VS. EXPENSE RATIO"
            )

        content.addView(
            ratioTitle,
            sectionParams()
        )


        val ratioCard =
            createCard()

        val ratio =
            if (income > 0.0) {
                ((totalExpenses() / income) * 100)
                    .roundToInt()
            } else {
                0
            }

        val ratioText =
            TextView(this).apply {
                text =
                    if (income > 0.0)
                        "$ratio% of income spent"
                    else
                        "No income recorded"

                textSize = 20f
                setTextColor(white)
                typeface =
                    Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 14)
            }

        ratioCard.addView(
            ratioText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )


        val ratioDetails =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
            }

        ratioDetails.addView(
            createLabel(
                "Income: ${money(income)}"
            )
        )

        ratioDetails.addView(
            createLabel(
                "Spent: ${money(totalExpenses())}"
            )
        )

        ratioDetails.addView(
            createLabel(
                "Remaining: ${money(remainingMoney())}"
            )
        )

        ratioCard.addView(
            ratioDetails,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        content.addView(
            ratioCard,
            cardParams()
        )


        val dailyTitle =
            sectionTitle(
                "DAILY AVERAGE"
            )

        content.addView(
            dailyTitle,
            sectionParams()
        )


        val dailyCard =
            createCard()

        dailyCard.addView(
            createValueText(
                money(dailyAverage()) +
                    " / day",
                amber
            ),
            valueParams()
        )

        dailyCard.addView(
            createLabel(
                "Based on this month's total spending."
            )
        )

        content.addView(
            dailyCard,
            cardParams()
        )


        val savingsTitle =
            sectionTitle(
                "SAVINGS RATE"
            )

        content.addView(
            savingsTitle,
            sectionParams()
        )


        val savingsCard =
            createCard()

        savingsCard.addView(
            createValueText(
                "${savingsRate()}%",
                green
            ),
            valueParams()
        )

        savingsCard.addView(
            createLabel(
                "Income remaining after expenses."
            )
        )

        content.addView(
            savingsCard,
            cardParams()
        )


        val trendTitle =
            sectionTitle(
                "SPENDING TREND"
            )

        content.addView(
            trendTitle,
            sectionParams()
        )


        val trendCard =
            createCard()

        val trendData =
            sixMonthData()

        for (data in trendData) {

            val row =
                LinearLayout(this).apply {
                    orientation =
                        LinearLayout.HORIZONTAL
                    gravity =
                        Gravity.CENTER_VERTICAL
                    setPadding(
                        0,
                        9,
                        0,
                        9
                    )
                }

            val monthText =
                TextView(this).apply {
                    text =
                        monthName(
                            data.year,
                            data.month
                        ).take(3) +
                            " " +
                            data.year

                    textSize = 14f
                    setTextColor(white)
                }

            row.addView(
                monthText,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val spentText =
                TextView(this).apply {
                    text =
                        money(data.expenses)

                    textSize = 14f
                    setTextColor(
                        if (data.expenses > 0)
                            red
                        else
                            muted
                    )
                    gravity = Gravity.END
                }

            row.addView(
                spentText,
                LinearLayout.LayoutParams(
                    120,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            trendCard.addView(row)
        }

        content.addView(
            trendCard,
            cardParams()
        )


        val footer =
            TextView(this).apply {
                text =
                    "Analytics are calculated from your saved MoneyTrack data."

                textSize = 12f
                setTextColor(muted)
                gravity = Gravity.CENTER
                setPadding(
                    8,
                    20,
                    8,
                    10
                )
            }

        content.addView(
            footer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )


        setContentView(root)
    }
            private fun createCard(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(cardColor)
            setPadding(18, 18, 18, 18)
        }
    }


    private fun sectionTitle(
        text: String
    ): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(secondary)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }
    }


    private fun createLabel(
        text: String
    ): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(secondary)
            setPadding(0, 4, 0, 4)
        }
    }


    private fun createValueText(
        text: String,
        color: Int
    ): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = 24f
            setTextColor(color)
            typeface = Typeface.DEFAULT_BOLD
        }
    }


    private fun createEmptyText(
        text: String
    ): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(muted)
            setPadding(0, 12, 0, 12)
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
                14
            )
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
                18,
                0,
                10
            )
        }
    }


    private fun valueParams():
            LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(
                0,
                2,
                0,
                12
            )
        }
    }


    override fun onBackPressed() {
        finish()
    }
}
