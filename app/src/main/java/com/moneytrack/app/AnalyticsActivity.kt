package com.moneytrack.app

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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

    private val orange =
        Color.rgb(245, 125, 45)

    private val pink =
        Color.rgb(220, 80, 145)

    private val teal =
        Color.rgb(45, 185, 170)

    private val yellow =
        Color.rgb(235, 190, 55)

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

    private val categoryColors = arrayOf(
        amber,
        blue,
        green,
        orange,
        red,
        purple,
        lightBlue,
        pink,
        teal,
        yellow,
        grey,
        secondary
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

            val monthExpenses =
                readExpensesTotal(
                    year,
                    month
                )

            result.add(
                MonthData(
                    year = year,
                    month = month,
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
                expensesKey(
                    year,
                    month
                ),
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
            orientation =
                LinearLayout.VERTICAL

            setBackgroundColor(
                backgroundColor
            )
        }

        val header = LinearLayout(this).apply {
            orientation =
                LinearLayout.HORIZONTAL

            gravity =
                Gravity.CENTER_VERTICAL

            setPadding(
                20,
                92,
                20,
                16
            )
        }

        val title = TextView(this).apply {
            text = "←  ANALYTICS"
            textSize = 24f
            setTextColor(white)

            typeface =
                Typeface.DEFAULT_BOLD

            letterSpacing = 0.04f

            setPadding(
                0,
                0,
                0,
                10
            )

            setOnClickListener {
                finish()
            }
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
            text =
                monthName(
                    selectedYear,
                    selectedMonth
                ) + " " + selectedYear

            textSize = 14f
            setTextColor(secondary)

            gravity =
                Gravity.CENTER_VERTICAL
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

        val scrollView =
            ScrollView(this).apply {
                setFillViewport(true)
            }

        val content =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    20,
                    18,
                    20,
                    32
                )
            }

        scrollView.addView(
            content
        )

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val breakdownTitle =
            sectionTitle(
                "MONTHLY SPENDING BREAKDOWN"
            )

        content.addView(
            breakdownTitle,
            sectionParams()
        )

        val breakdownCard =
            createCard()

        val breakdownView =
            DonutChartView(this)

        breakdownView.setData(
            categoryTotals(),
            totalExpenses()
        )

        breakdownCard.addView(
            breakdownView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                330
            )
        )

        val categoryLegend =
            createCategoryLegend()

        breakdownCard.addView(
            categoryLegend,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        content.addView(
            breakdownCard,
            cardParams()
        )

        val ratioTitle =
            sectionTitle(
                "INCOME VS. EXPENSES RATIO"
            )

        content.addView(
            ratioTitle,
            sectionParams()
        )

        val ratioCard =
            createCard()

        val incomePercent =
            if (income > 0.0) {
                (
                    (income /
                        (income + totalExpenses())) *
                        100.0
                    )
                        .roundToInt()
                        .coerceIn(0, 100)
            } else {
                0
            }

        val expensePercent =
            if (income > 0.0) {
                (
                    (totalExpenses() /
                        (income + totalExpenses())) *
                        100.0
                    )
                        .roundToInt()
                        .coerceIn(0, 100)
            } else {
                0
            }

        ratioCard.addView(
            createRatioRow(
                "INCOME",
                incomePercent,
                money(income),
                green
            ),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        ratioCard.addView(
            createRatioRow(
                "EXPENSES",
                expensePercent,
                money(totalExpenses()),
                red
            ),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        content.addView(
            ratioCard,
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

        val trendView =
            SpendingTrendView(this)

        trendView.setData(
            sixMonthData()
        )

        trendCard.addView(
            trendView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                330
            )
        )

        content.addView(
            trendCard,
            cardParams()
        )

        val statsRow =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    0,
                    0,
                    0
                )
            }

        val dailyCard =
            createSquareCard()

        dailyCard.addView(
            createCardTitle(
                "DAILY AVERAGE"
            )
        )

        dailyCard.addView(
            createSquareValue(
                money(dailyAverage()),
                amber
            )
        )

        dailyCard.addView(
            createSquareSubtitle(
                "Per day"
            )
        )

        statsRow.addView(
            dailyCard,
            squareCardParams(
                true
            )
        )

        val savingsCard =
            createSquareCard()

        savingsCard.addView(
            createCardTitle(
                "SAVINGS RATE"
            )
        )

        savingsCard.addView(
            createSquareValue(
                "${savingsRate()}%",
                green
            )
        )

        savingsCard.addView(
            createSquareSubtitle(
                "Income saved"
            )
        )

        statsRow.addView(
            savingsCard,
            squareCardParams(
                false
            )
        )

        content.addView(
            statsRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                150
            )
        )

        val footer =
            TextView(this).apply {
                text =
                    "Analytics are calculated from your saved MoneyTrack data."

                textSize = 12f
                setTextColor(muted)

                gravity =
                    Gravity.CENTER

                setPadding(
                    8,
                    24,
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

    private fun createCategoryLegend():
            LinearLayout {

        val totals =
            categoryTotals()

        val total =
            totalExpenses()

        return LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL

            for (
                category in categories
            ) {

                val amount =
                    totals[category]
                        ?: 0.0

                if (amount <= 0.0) {
                    continue
                }

                val percentage =
                    if (total > 0.0) {
                        (
                            (amount / total) *
                                100.0
                            )
                                .roundToInt()
                    } else {
                        0
                    }

                val row =
                    LinearLayout(
                        this@AnalyticsActivity
                    ).apply {

                        orientation =
                            LinearLayout.HORIZONTAL

                        gravity =
                            Gravity.CENTER_VERTICAL

                        setPadding(
                            0,
                            6,
                            0,
                            6
                        )
                    }

                val dot =
                    TextView(
                        this@AnalyticsActivity
                    ).apply {

                        text = "●"
                        textSize = 14f

                        setTextColor(
                            categoryColor(
                                category
                            )
                        )

                        gravity =
                            Gravity.CENTER
                    }

                row.addView(
                    dot,
                    LinearLayout.LayoutParams(
                        30,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )

                val name =
                    TextView(
                        this@AnalyticsActivity
                    ).apply {

                        text =
                            category

                        textSize = 14f
                        setTextColor(white)
                    }

                row.addView(
                    name,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                val percent =
                    TextView(
                        this@AnalyticsActivity
                    ).apply {

                        text =
                            "$percentage%"

                        textSize = 14f
                        setTextColor(secondary)

                        gravity =
                            Gravity.END
                    }

                row.addView(
                    percent,
                    LinearLayout.LayoutParams(
                        60,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )

                addView(
                    row,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }

            if (total <= 0.0) {

                addView(
                    createEmptyText(
                        "No expenses recorded this month."
                    )
                )
            }
        }
    }

    private fun categoryColor(
        category: String
    ): Int {

        val index =
            categories.indexOf(category)

        return if (
            index >= 0 &&
            index < categoryColors.size
        ) {
            categoryColors[index]
        } else {
            secondary
        }
    }
       private fun createRatioRow(
        label: String,
        percentage: Int,
        amount: String,
        color: Int
    ): LinearLayout {

        val container =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    0,
                    8,
                    0,
                    16
                )
            }

        val header =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        val labelView =
            TextView(this).apply {

                text =
                    label

                textSize = 14f

                setTextColor(
                    white
                )

                typeface =
                    Typeface.DEFAULT_BOLD
            }

        header.addView(
            labelView,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val percentView =
            TextView(this).apply {

                text =
                    "$percentage%"

                textSize = 16f

                setTextColor(
                    color
                )

                typeface =
                    Typeface.DEFAULT_BOLD

                gravity =
                    Gravity.END
            }

        header.addView(
            percentView,
            LinearLayout.LayoutParams(
                70,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        container.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val amountView =
            TextView(this).apply {

                text =
                    amount

                textSize = 14f

                setTextColor(
                    secondary
                )

                setPadding(
                    0,
                    4,
                    0,
                    8
                )
            }

        container.addView(
            amountView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val progress =
            RatioProgressView(this)

        progress.setProgress(
            percentage,
            color
        )

        container.addView(
            progress,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                18
            )
        )

        return container
    }

    private fun createSquareCard():
            LinearLayout {

        return LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL

            gravity =
                Gravity.CENTER

            setBackgroundColor(
                cardColor
            )

            setPadding(
                12,
                12,
                12,
                12
            )
        }
    }

    private fun createCardTitle(
        text: String
    ): TextView {

        return TextView(this).apply {

            this.text =
                text

            textSize =
                12f

            setTextColor(
                secondary
            )

            typeface =
                Typeface.DEFAULT_BOLD

            gravity =
                Gravity.CENTER

            letterSpacing =
                0.05f
        }
    }

    private fun createSquareValue(
        text: String,
        color: Int
    ): TextView {

        return TextView(this).apply {

            this.text =
                text

            textSize =
                22f

            setTextColor(
                color
            )

            typeface =
                Typeface.DEFAULT_BOLD

            gravity =
                Gravity.CENTER

            setPadding(
                0,
                10,
                0,
                4
            )
        }
    }

    private fun createSquareSubtitle(
        text: String
    ): TextView {

        return TextView(this).apply {

            this.text =
                text

            textSize =
                12f

            setTextColor(
                muted
            )

            gravity =
                Gravity.CENTER
        }
    }

    private fun createCard():
            LinearLayout {

        return LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL

            setBackgroundColor(
                cardColor
            )

            setPadding(
                18,
                18,
                18,
                18
            )
        }
    }

    private fun sectionTitle(
        text: String
    ): TextView {

        return TextView(this).apply {

            this.text =
                text

            textSize =
                13f

            setTextColor(
                secondary
            )

            typeface =
                Typeface.DEFAULT_BOLD

            letterSpacing =
                0.08f
        }
    }

    private fun createEmptyText(
        text: String
    ): TextView {

        return TextView(this).apply {

            this.text =
                text

            textSize =
                14f

            setTextColor(
                muted
            )

            gravity =
                Gravity.CENTER

            setPadding(
                0,
                18,
                0,
                18
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
                18
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

    private fun squareCardParams(
        left: Boolean
    ): LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            150,
            1f
        ).apply {

            if (left) {
                setMargins(
                    0,
                    0,
                    7,
                    0
                )
            } else {
                setMargins(
                    7,
                    0,
                    0,
                    0
                )
            }
        }
    }

    private fun drawRoundRect(
        canvas: Canvas,
        paint: Paint,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radius: Float
    ) {

        canvas.drawRoundRect(
            left,
            top,
            right,
            bottom,
            radius,
            radius,
            paint
        )
    }

    private class RatioProgressView(
        context: android.content.Context
    ) : View(context) {

        private var progress = 0
        private var progressColor =
            Color.WHITE

        fun setProgress(
            value: Int,
            color: Int
        ) {

            progress =
                value.coerceIn(
                    0,
                    100
                )

            progressColor =
                color

            invalidate()
        }

        override fun onDraw(
            canvas: Canvas
        ) {

            super.onDraw(canvas)

            val width =
                width.toFloat()

            val height =
                height.toFloat()

            val radius =
                height / 2f

            val backgroundPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {

                    style =
                        Paint.Style.FILL

                    color =
                        Color.rgb(
                            45,
                            48,
                            58
                        )
                }

            drawRoundRect(
                canvas,
                backgroundPaint,
                0f,
                0f,
                width,
                height,
                radius
            )

            val progressPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {

                    style =
                        Paint.Style.FILL

                    color =
                        progressColor
                }

            val progressWidth =
                width *
                    (progress / 100f)

            if (progressWidth > 0f) {

                drawRoundRect(
                    canvas,
                    progressPaint,
                    0f,
                    0f,
                    progressWidth,
                    height,
                    radius
                )
            }
        }
    }
        private class DonutChartView(
        context: android.content.Context
    ) : View(context) {

        private var values =
            emptyMap<String, Double>()

        private var total =
            0.0

        private var colors =
            IntArray(0)

        fun setData(
            categoryValues: Map<String, Double>,
            totalValue: Double
        ) {

            values =
                categoryValues

            total =
                totalValue

            colors =
                intArrayOf(
                    Color.rgb(255, 166, 52),
                    Color.rgb(38, 104, 210),
                    Color.rgb(72, 190, 110),
                    Color.rgb(245, 125, 45),
                    Color.rgb(220, 70, 70),
                    Color.rgb(145, 90, 200),
                    Color.rgb(80, 150, 230),
                    Color.rgb(220, 80, 145),
                    Color.rgb(45, 185, 170),
                    Color.rgb(235, 190, 55),
                    Color.rgb(110, 116, 130),
                    Color.rgb(168, 174, 187)
                )

            invalidate()
        }

        override fun onDraw(
            canvas: Canvas
        ) {

            super.onDraw(canvas)

            val centerX =
                width / 2f

            val centerY =
                height / 2f

            val radius =
                minOf(
                    width,
                    height
                ) *
                    0.34f

            val strokeWidth =
                radius *
                    0.34f

            val paint =
                Paint(
                    Paint.ANTI_ALIAS_FLAG
                ).apply {

                    style =
                        Paint.Style.STROKE

                    this.strokeWidth =
                        strokeWidth

                    strokeCap =
                        Paint.Cap.BUTT
                }

            if (total <= 0.0) {

                paint.color =
                    Color.rgb(
                        45,
                        48,
                        58
                    )

                canvas.drawCircle(
                    centerX,
                    centerY,
                    radius,
                    paint
                )

                val emptyText =
                    Paint(
                        Paint.ANTI_ALIAS_FLAG
                    ).apply {

                        color =
                            Color.rgb(
                                115,
                                121,
                                135
                            )

                        textSize =
                            14f

                        textAlign =
                            Paint.Align.CENTER
                    }

                canvas.drawText(
                    "NO SPENDING",
                    centerX,
                    centerY + 5f,
                    emptyText
                )

                return
            }

            var startAngle =
                -90f

            var colorIndex =
                0

            for (
                category in values.keys
            ) {

                val amount =
                    values[category]
                        ?: 0.0

                if (amount <= 0.0) {
                    continue
                }

                val sweepAngle =
                    (
                        amount /
                            total
                        ) *
                        360f

                paint.color =
                    colors[
                        colorIndex %
                            colors.size
                    ]

                canvas.drawArc(
                    centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius,
                    startAngle,
                    sweepAngle.toFloat(),
                    false,
                    paint
                )

                startAngle +=
                    sweepAngle.toFloat()

                colorIndex++
            }

            paint.color =
                Color.rgb(
                    20,
                    22,
                    29
                )

            paint.strokeWidth =
                strokeWidth * 0.9f

            canvas.drawCircle(
                centerX,
                centerY,
                radius -
                    strokeWidth * 0.52f,
                Paint(
                    Paint.ANTI_ALIAS_FLAG
                ).apply {

                    style =
                        Paint.Style.FILL

                    color =
                        Color.rgb(
                            20,
                            22,
                            29
                        )
                }
            )

            val totalPaint =
                Paint(
                    Paint.ANTI_ALIAS_FLAG
                ).apply {

                    color =
                        Color.WHITE

                    textSize =
                        22f

                    typeface =
                        Typeface.DEFAULT_BOLD

                    textAlign =
                        Paint.Align.CENTER
                }

            canvas.drawText(
                money(total),
                centerX,
                centerY + 7f,
                totalPaint
            )
        }
    }

    private class SpendingTrendView(
        context: android.content.Context
    ) : View(context) {

        private var data =
            emptyList<MonthData>()

        private val axisColor =
            Color.rgb(
                75,
                79,
                90
            )

        private val lineColor =
            Color.rgb(
                255,
                166,
                52
            )

        fun setData(
            values: List<MonthData>
        ) {

            data =
                values

            invalidate()
        }

        override fun onDraw(
            canvas: Canvas
        ) {

            super.onDraw(canvas)

            if (data.isEmpty()) {
                return
            }

            val left =
                78f

            val right =
                width - 24f

            val top =
                28f

            val bottom =
                height - 52f

            val chartWidth =
                right - left

            val chartHeight =
                bottom - top

            val axisPaint =
                Paint(
                    Paint.ANTI_ALIAS_FLAG
                ).apply {

                    color =
                        axisColor

                    strokeWidth =
                        2f

                    style =
                        Paint.Style.STROKE
                }

            canvas.drawLine(
                left,
                top,
                left,
                bottom,
                axisPaint
            )

            canvas.drawLine(
                left,
                bottom,
                right,
                bottom,
                axisPaint
            )

            val maximum =
                data.maxOfOrNull {
                    it.expenses
                } ?: 0.0

            val maxValue =
                if (maximum > 0.0) {
                    maximum
                } else {
                    1.0
                }

            val linePaint =
                Paint(
                    Paint.ANTI_ALIAS_FLAG
                ).apply {

                    color =
                        lineColor

                    strokeWidth =
                        5f

                    style =
                        Paint.Style.STROKE

                    strokeCap =
                        Paint.Cap.ROUND

                    strokeJoin =
                        Paint.Join.ROUND
                }

            val pointPaint =
                Paint(
                    Paint.ANTI_ALIAS_FLAG
                ).apply {

                    color =
                        lineColor

                    style =
                        Paint.Style.FILL
                }

            val path =
                Path()

            data.forEachIndexed {
                index,
                monthData ->

                val x =
                    if (data.size == 1) {
                        left
                    } else {
                        left +
                            (
                                chartWidth *
                                    index /
                                    (data.size - 1)
                            )
                    }

                val y =
                    bottom -
                        (
                            monthData.expenses /
                                maxValue
                            ).toFloat() *
                            chartHeight

                if (index == 0) {
                    path.moveTo(
                        x,
                        y
                    )
                } else {
                    path.lineTo(
                        x,
                        y
                    )
                }
            }

            canvas.drawPath(
                path,
                linePaint
            )

            data.forEachIndexed {
                index,
                monthData ->

                val x =
                    if (data.size == 1) {
                        left
                    } else {
                        left +
                            (
                                chartWidth *
                                    index /
                                    (data.size - 1)
                            )
                    }

                val y =
                    bottom -
                        (
                            monthData.expenses /
                                maxValue
                            ).toFloat() *
                            chartHeight

                canvas.drawCircle(
                    x,
                    y,
                    7f,
                    pointPaint
                )

                val monthPaint =
                    Paint(
                        Paint.ANTI_ALIAS_FLAG
                    ).apply {

                        color =
                            Color.rgb(
                                168,
                                174,
                                187
                            )

                        textSize =
                            12f

                        textAlign =
                            Paint.Align.CENTER
                    }

                val monthLabel =
                    monthName(
                        monthData.year,
                        monthData.month
                    ).take(3)

                canvas.drawText(
                    monthLabel,
                    x,
                    bottom + 28f,
                    monthPaint
                )
            }

            val zeroPaint =
                Paint(
                    Paint.ANTI_ALIAS_FLAG
                ).apply {

                    color =
                        Color.rgb(
                            168,
                            174,
                            187
                        )

                    textSize =
                        11f

                    textAlign =
                        Paint.Align.RIGHT
                }

            canvas.drawText(
                money(0.0),
                left - 10f,
                bottom + 4f,
                zeroPaint
            )

            canvas.drawText(
                money(maxValue),
                left - 10f,
                top + 4f,
                zeroPaint
            )

            val axisPaintText =
                Paint(
                    Paint.ANTI_ALIAS_FLAG
                ).apply {

                    color =
                        Color.rgb(
                            168,
                            174,
                            187
                        )

                    textSize =
                        12f

                    textAlign =
                        Paint.Align.LEFT
                }

            canvas.drawText(
                "€",
                20f,
                top + 4f,
                axisPaintText
            )
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

        private fun money(
            amount: Double
        ): String {

            return NumberFormat
                .getCurrencyInstance(
                    Locale.GERMANY
                )
                .format(amount)
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
