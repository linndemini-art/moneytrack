package com.moneytrack.app

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class AnalyticsActivity : Activity() {

    private val backgroundColor = Color.rgb(7, 8, 12)
    private val cardColor = Color.rgb(20, 22, 29)
    private val surfaceColor = Color.rgb(28, 31, 40)

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

        selectedYear = calendar.get(Calendar.YEAR)
        selectedMonth = calendar.get(Calendar.MONTH)

        buildInterface()
    }

    private fun buildInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
        }

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(backgroundColor)
            isFillViewport = true
            overScrollMode = ScrollView.OVER_SCROLL_NEVER
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(20),
                dp(92),
                dp(20),
                dp(32)
            )
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val backButton = TextView(this).apply {
            text = "←"
            textSize = 28f
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(
                0,
                0,
                dp(10),
                dp(2)
            )

            setOnClickListener {
                finish()
            }
        }

        header.addView(
            backButton,
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        val title = TextView(this).apply {
            text = "ANALYTICS"
            textSize = 23f
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

        val monthLabel = TextView(this).apply {
            text = selectedMonthName()
            textSize = 14f
            setTextColor(secondary)
            gravity = Gravity.END
        }

        header.addView(
            monthLabel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        content.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val intro = TextView(this).apply {
            text = "Your spending, income and monthly trends"
            textSize = 13f
            setTextColor(muted)
            setPadding(
                dp(48),
                0,
                0,
                dp(22)
            )
        }

        content.addView(intro)

        val breakdownTitle = sectionTitle(
            "MONTHLY SPENDING BREAKDOWN"
        )

        content.addView(
            breakdownTitle,
            sectionTitleParams()
        )

        val breakdownCard = createCard()

        val categoryTotals = loadCategoryTotals()

        val donutView = DonutChartView(this)

        donutView.setData(categoryTotals)

        breakdownCard.addView(
            donutView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(245)
            )
        )

        val legend = createCategoryLegend(
            categoryTotals
        )

        breakdownCard.addView(
            legend,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        content.addView(
            breakdownCard,
            cardParams()
        )

        val ratioTitle = sectionTitle(
            "INCOME VS EXPENSES"
        )

        content.addView(
            ratioTitle,
            sectionTitleParams()
        )

        val currentMonth = loadMonthData(
            selectedYear,
            selectedMonth
        )

        val ratioCard = createCard()

        val ratioView = RatioProgressView(this)

        ratioView.setValues(
            currentMonth.income,
            currentMonth.expenses
        )

        ratioCard.addView(
            ratioView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(170)
            )
        )

        content.addView(
            ratioCard,
            cardParams()
        )

        val trendTitle = sectionTitle(
            "SPENDING TREND"
        )

        content.addView(
            trendTitle,
            sectionTitleParams()
        )

        val trendCard = createCard()

        val trendData = loadSixMonthTrend()

        val trendView = SpendingTrendView(this)

        trendView.setData(trendData)

        trendCard.addView(
            trendView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(300)
            )
        )

        content.addView(
            trendCard,
            cardParams()
        )

        val statsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val totalExpenses = currentMonth.expenses

        val calendarForDays = Calendar.getInstance().apply {
            set(
                selectedYear,
                selectedMonth,
                1
            )
        }

        val daysInMonth =
            calendarForDays.getActualMaximum(
                Calendar.DAY_OF_MONTH
            )

        val dailyAverage =
            if (daysInMonth > 0) {
                totalExpenses / daysInMonth
            } else {
                0.0
            }

        val savingsRate =
            if (currentMonth.income > 0.0) {
                (
                    (currentMonth.income - totalExpenses) /
                        currentMonth.income
                    ) * 100.0
            } else {
                0.0
            }

        val dailyCard = createStatCard(
            "Daily Average",
            money(dailyAverage)
        )

        statsRow.addView(
            dailyCard,
            statCardParams()
        )

        val savingsCard = createStatCard(
            "Savings Rate",
            String.format(
                Locale.US,
                "%.0f%%",
                savingsRate
            )
        )

        val savingsParams = statCardParams()

        savingsParams.setMargins(
            dp(10),
            0,
            0,
            0
        )

        statsRow.addView(
            savingsCard,
            savingsParams
        )

        content.addView(
            statsRow,
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
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)
    }

    private fun selectedMonthName(): String {

        val calendar = Calendar.getInstance().apply {
            set(
                selectedYear,
                selectedMonth,
                1
            )
        }

        return SimpleDateFormat(
            "MMMM yyyy",
            Locale.ENGLISH
        ).format(calendar.time)
    }

    private fun sectionTitle(
        text: String
    ): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.04f
        }
    }

    private fun sectionTitleParams():
        LinearLayout.LayoutParams {

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(
            0,
            dp(24),
            0,
            dp(10)
        )

        return params
    }

    private fun createCard():
        LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
            )

            background = roundedBackground(
                cardColor,
                18f
            )
        }
    }

    private fun cardParams():
        LinearLayout.LayoutParams {

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(
            0,
            0,
            0,
            dp(4)
        )

        return params
    }

    private fun createStatCard(
        label: String,
        value: String
    ): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER

            setPadding(
                dp(12),
                dp(20),
                dp(12),
                dp(20)
            )

            background = roundedBackground(
                cardColor,
                18f
            )

            val labelText = TextView(this@AnalyticsActivity).apply {
                text = label
                textSize = 12f
                setTextColor(secondary)
                gravity = Gravity.CENTER
            }

            addView(labelText)

            val valueText = TextView(this@AnalyticsActivity).apply {
                text = value
                textSize = 21f
                setTextColor(white)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER

                setPadding(
                    0,
                    dp(6),
                    0,
                    0
                )
            }

            addView(valueText)
        }
    }

    private fun statCardParams():
        LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
    }

    private fun roundedBackground(
        color: Int,
        radius: Float
    ): android.graphics.drawable.GradientDrawable {

        return android.graphics.drawable.GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius.toInt()).toFloat()
        }
    }

    private fun dp(value: Int): Int {
        return (
            value *
                resources.displayMetrics.density
            ).toInt()
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

        val key = currentMonthKey(
            year,
            month
        )

        val income = prefs.getString(
            "income_$key",
            "0"
        )?.toDoubleOrNull() ?: 0.0

        val expenses = loadExpenses(
            year,
            month
        )

        return MonthData(
            year = year,
            month = month,
            income = income,
            expenses = expenses.sumOf {
                it.amount
            }
        )
    }

    private fun loadExpenses(
        year: Int,
        month: Int
    ): List<ExpenseData> {

        val key = currentMonthKey(
            year,
            month
        )

        val stored = prefs.getString(
            "expenses_$key",
            null
        )

        if (stored.isNullOrEmpty()) {
            return emptyList()
        }

        val result = mutableListOf<ExpenseData>()

        try {

            val array = JSONArray(stored)

            for (
                index in 0 until array.length()
            ) {

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

        } catch (
            exception: Exception
        ) {
            result.clear()
        }

        return result
    }

    private fun loadCategoryTotals():
        LinkedHashMap<String, Double> {

        val result =
            LinkedHashMap<String, Double>()

        val expenses = loadExpenses(
            selectedYear,
            selectedMonth
        )

        for (expense in expenses) {

            val previous =
                result[expense.category] ?: 0.0

            result[expense.category] =
                previous + expense.amount
        }

        return result
    }

    private fun createCategoryLegend(
        totals: LinkedHashMap<String, Double>
    ): LinearLayout {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        if (totals.isEmpty()) {

            val emptyText = TextView(this).apply {
                text = "No expenses recorded this month."
                textSize = 13f
                setTextColor(muted)
                gravity = Gravity.CENTER
                setPadding(
                    0,
                    dp(6),
                    0,
                    dp(6)
                )
            }

            container.addView(emptyText)

            return container
        }

        val total =
            totals.values.sum()

        val colors = donutColors()

        var index = 0

        for ((category, amount) in totals) {

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(
                    0,
                    dp(5),
                    0,
                    dp(5)
                )
            }

            val colorDot = View(this).apply {
                setBackgroundColor(
                    colors[
                        index % colors.size
                    ]
                )
            }

            row.addView(
                colorDot,
                LinearLayout.LayoutParams(
                    dp(10),
                    dp(10)
                )
            )

            val categoryText =
                TextView(this).apply {
                    text = category
                    textSize = 13f
                    setTextColor(secondary)
                    setPadding(
                        dp(10),
                        0,
                        0,
                        0
                    )
                }

            row.addView(
                categoryText,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val percentage =
                if (total > 0.0) {
                    amount / total * 100.0
                } else {
                    0.0
                }

            val percentageText =
                TextView(this).apply {
                    text = String.format(
                        Locale.US,
                        "%.1f%%",
                        percentage
                    )
                    textSize = 13f
                    setTextColor(white)
                    typeface =
                        Typeface.DEFAULT_BOLD
                    gravity = Gravity.END
                }

            row.addView(
                percentageText,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            container.addView(row)

            index++
        }

        return container
    }

    private fun donutColors(): IntArray {

        return intArrayOf(
            Color.rgb(74, 145, 255),
            Color.rgb(255, 166, 52),
            Color.rgb(72, 190, 110),
            Color.rgb(213, 91, 116),
            Color.rgb(160, 104, 230),
            Color.rgb(51, 191, 191),
            Color.rgb(232, 108, 55),
            Color.rgb(119, 151, 209),
            Color.rgb(215, 190, 69),
            Color.rgb(87, 167, 112),
            Color.rgb(188, 91, 183),
            Color.rgb(126, 126, 139)
        )
    }

    private fun loadSixMonthTrend():
        List<MonthData> {

        val result = mutableListOf<MonthData>()

        val calendar = Calendar.getInstance().apply {
            set(
                selectedYear,
                selectedMonth,
                1
            )
        }

        calendar.add(
            Calendar.MONTH,
            -5
        )

        repeat(6) {

            val year =
                calendar.get(Calendar.YEAR)

            val month =
                calendar.get(Calendar.MONTH)

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

    private inner class RatioProgressView(
        context: Context
    ) : View(context) {

        private val paint = Paint(
            Paint.ANTI_ALIAS_FLAG
        )

        private var income = 0.0
        private var expenses = 0.0

        private var animatedIncome = 0f
        private var animatedExpenses = 0f

        private var animator: ValueAnimator? = null

        fun setValues(
            newIncome: Double,
            newExpenses: Double
        ) {

            income = max(
                0.0,
                newIncome
            )

            expenses = max(
                0.0,
                newExpenses
            )

            animator?.cancel()

            animator = ValueAnimator.ofFloat(
                0f,
                1f
            ).apply {

                duration = 900L

                interpolator =
                    DecelerateInterpolator()

                addUpdateListener {

                    val progress =
                        it.animatedValue as Float

                    animatedIncome =
                        progress.toFloat()

                    animatedExpenses =
                        progress.toFloat()

                    invalidate()
                }

                start()
            }
        }

        override fun onDraw(
            canvas: Canvas
        ) {

            super.onDraw(canvas)

            val width =
                width.toFloat()

            val total =
                income + expenses

            val incomeRatio =
                if (total > 0.0) {
                    income / total
                } else {
                    0.0
                }

            val expenseRatio =
                if (total > 0.0) {
                    expenses / total
                } else {
                    0.0
                }

            val incomePercent =
                incomeRatio * 100.0

            val expensePercent =
                expenseRatio * 100.0

            val left = dp(4).toFloat()
            val right =
                width - dp(4).toFloat()

            val lineWidth =
                right - left

            val lineY =
                dp(82).toFloat()

            paint.style =
                Paint.Style.STROKE

            paint.strokeWidth =
                dp(12).toFloat()

            paint.strokeCap =
                Paint.Cap.ROUND

            paint.color =
                Color.rgb(
                    42,
                    45,
                    54
                )

            canvas.drawLine(
                left,
                lineY,
                right,
                lineY,
                paint
            )

            if (total > 0.0) {

                val incomeEnd =
                    left +
                        lineWidth *
                        incomeRatio.toFloat() *
                        animatedIncome

                if (incomeEnd > left) {

                    paint.color = blue

                    canvas.drawLine(
                        left,
                        lineY,
                        incomeEnd,
                        lineY,
                        paint
                    )
                }

                val expenseStart =
                    left +
                        lineWidth *
                        incomeRatio.toFloat()

                val expenseEnd =
                    expenseStart +
                        lineWidth *
                        expenseRatio.toFloat() *
                        animatedExpenses

                if (expenseEnd > expenseStart) {

                    paint.color = amber

                    canvas.drawLine(
                        expenseStart,
                        lineY,
                        min(
                            right,
                            expenseEnd
                        ),
                        lineY,
                        paint
                    )
                }
            }

            paint.style =
                Paint.Style.FILL

            paint.typeface =
                Typeface.DEFAULT_BOLD

            paint.textSize =
                sp(15f)

            paint.color = white

            canvas.drawText(
                "Income",
                left,
                dp(34).toFloat(),
                paint
            )

            canvas.drawText(
                "Expenses",
                left,
                dp(142).toFloat(),
                paint
            )

            paint.textAlign =
                Paint.Align.RIGHT

            val incomeText =
                String.format(
                    Locale.US,
                    "%.0f%%  %s",
                    incomePercent,
                    money(income)
                )

            canvas.drawText(
                incomeText,
                right,
                dp(34).toFloat(),
                paint
            )

            val expenseText =
                String.format(
                    Locale.US,
                    "%.0f%%  %s",
                    expensePercent,
                    money(expenses)
                )

            canvas.drawText(
                expenseText,
                right,
                dp(142).toFloat(),
                paint
            )

            paint.textAlign =
                Paint.Align.LEFT

            paint.textSize =
                sp(11f)

            paint.typeface =
                Typeface.DEFAULT

            paint.color =
                Color.rgb(
                    115,
                    121,
                    135
                )

            canvas.drawText(
                "Combined monthly ratio",
                left,
                dp(105).toFloat(),
                paint
            )
        }

        private fun dp(
            value: Int
        ): Float {

            return value *
                resources.displayMetrics.density
        }

        private fun sp(
            value: Float
        ): Float {

            return value *
                resources.displayMetrics.scaledDensity
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
        private inner class DonutChartView(
        context: Context
    ) : View(context) {

        private val paint = Paint(
            Paint.ANTI_ALIAS_FLAG
        )

        private val rect = RectF()

        private var values =
            LinkedHashMap<String, Double>()

        private var total = 0.0

        fun setData(
            data: LinkedHashMap<String, Double>
        ) {

            values =
                LinkedHashMap(data)

            total =
                values.values.sum()

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
                min(
                    width,
                    height
                ) * 0.30f

            paint.style =
                Paint.Style.STROKE

            paint.strokeWidth =
                dp(30f)

            paint.strokeCap =
                Paint.Cap.BUTT

            if (total <= 0.0) {

                paint.color =
                    Color.rgb(
                        43,
                        46,
                        56
                    )

                canvas.drawCircle(
                    centerX,
                    centerY,
                    radius,
                    paint
                )

                paint.style =
                    Paint.Style.FILL

                paint.color =
                    Color.rgb(
                        115,
                        121,
                        135
                    )

                paint.textSize =
                    sp(13f)

                paint.textAlign =
                    Paint.Align.CENTER

                canvas.drawText(
                    "No spending",
                    centerX,
                    centerY + dp(5f),
                    paint
                )

                paint.textAlign =
                    Paint.Align.LEFT

                return
            }

            rect.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
            )

            val colors = intArrayOf(
                Color.rgb(74, 145, 255),
                Color.rgb(255, 166, 52),
                Color.rgb(72, 190, 110),
                Color.rgb(213, 91, 116),
                Color.rgb(160, 104, 230),
                Color.rgb(51, 191, 191),
                Color.rgb(232, 108, 55),
                Color.rgb(119, 151, 209),
                Color.rgb(215, 190, 69),
                Color.rgb(87, 167, 112),
                Color.rgb(188, 91, 183),
                Color.rgb(126, 126, 139)
            )

            var startAngle = -90f
            var colorIndex = 0

            for (amount in values.values) {

                val sweep =
                    (
                        amount / total
                    ).toFloat() * 360f

                if (sweep > 0f) {

                    paint.color =
                        colors[
                            colorIndex %
                                colors.size
                        ]

                    canvas.drawArc(
                        rect,
                        startAngle,
                        sweep,
                        false,
                        paint
                    )

                    startAngle += sweep
                }

                colorIndex++
            }

            paint.style =
                Paint.Style.FILL

            paint.color =
                Color.rgb(
                    20,
                    22,
                    29
                )

            canvas.drawCircle(
                centerX,
                centerY,
                radius - dp(17f),
                paint
            )

            paint.color = Color.WHITE
            paint.textAlign =
                Paint.Align.CENTER
            paint.typeface =
                Typeface.DEFAULT_BOLD

            paint.textSize =
                sp(18f)

            canvas.drawText(
                money(total),
                centerX,
                centerY + dp(4f),
                paint
            )

            paint.typeface =
                Typeface.DEFAULT

            paint.textSize =
                sp(11f)

            paint.color =
                Color.rgb(
                    115,
                    121,
                    135
                )

            canvas.drawText(
                "total spending",
                centerX,
                centerY + dp(23f),
                paint
            )

            paint.textAlign =
                Paint.Align.LEFT
        }

        private fun dp(
            value: Float
        ): Float {

            return value *
                resources.displayMetrics.density
        }

        private fun sp(
            value: Float
        ): Float {

            return value *
                resources.displayMetrics.scaledDensity
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

    private inner class SpendingTrendView(
        context: Context
    ) : View(context) {

        private val paint = Paint(
            Paint.ANTI_ALIAS_FLAG
        )

        private val linePath =
            android.graphics.Path()

        private var data =
            emptyList<MonthData>()

        fun setData(
            newData: List<MonthData>
        ) {

            data =
                newData.toList()

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
                dp(48f)

            val right =
                width.toFloat() -
                    dp(16f)

            val top =
                dp(20f)

            val bottom =
                height.toFloat() -
                    dp(48f)

            val chartWidth =
                right - left

            val chartHeight =
                bottom - top

            var maxValue =
                data.maxOf {
                    it.expenses
                }

            if (maxValue <= 0.0) {
                maxValue = 100.0
            }

            maxValue *= 1.15

            paint.style =
                Paint.Style.STROKE

            paint.strokeWidth =
                dp(1f)

            paint.color =
                Color.rgb(
                    42,
                    45,
                    54
                )

            for (line in 0..4) {

                val y =
                    bottom -
                        chartHeight *
                        line / 4f

                canvas.drawLine(
                    left,
                    y,
                    right,
                    y,
                    paint
                )
            }

            paint.style =
                Paint.Style.FILL

            paint.textSize =
                sp(10f)

            paint.typeface =
                Typeface.DEFAULT

            paint.color =
                Color.rgb(
                    115,
                    121,
                    135
                )

            for (line in 0..4) {

                val value =
                    maxValue *
                        line / 4.0

                val y =
                    bottom -
                        chartHeight *
                        line / 4f

                val text =
                    moneyShort(value)

                canvas.drawText(
                    text,
                    0f,
                    y + dp(4f),
                    paint
                )
            }

            linePath.reset()

            val pointCount =
                data.size

            for (
                index in data.indices
            ) {

                val x =
                    if (pointCount == 1) {
                        left +
                            chartWidth / 2f
                    } else {
                        left +
                            chartWidth *
                            index /
                            (pointCount - 1).toFloat()
                    }

                val value =
                    data[index].expenses

                val y =
                    bottom -
                        (
                            value /
                                maxValue
                        ).toFloat() *
                        chartHeight

                if (index == 0) {
                    linePath.moveTo(
                        x,
                        y
                    )
                } else {
                    linePath.lineTo(
                        x,
                        y
                    )
                }
            }

            paint.style =
                Paint.Style.STROKE

            paint.strokeWidth =
                dp(3f)

            paint.strokeCap =
                Paint.Cap.ROUND

            paint.strokeJoin =
                Paint.Join.ROUND

            paint.color =
                Color.rgb(
                    74,
                    145,
                    255
                )

            canvas.drawPath(
                linePath,
                paint
            )

            paint.style =
                Paint.Style.FILL

            for (
                index in data.indices
            ) {

                val x =
                    if (pointCount == 1) {
                        left +
                            chartWidth / 2f
                    } else {
                        left +
                            chartWidth *
                            index /
                            (pointCount - 1).toFloat()
                    }

                val value =
                    data[index].expenses

                val y =
                    bottom -
                        (
                            value /
                                maxValue
                        ).toFloat() *
                        chartHeight

                paint.color =
                    Color.rgb(
                        7,
                        8,
                        12
                    )

                canvas.drawCircle(
                    x,
                    y,
                    dp(6f),
                    paint
                )

                paint.color =
                    Color.rgb(
                        74,
                        145,
                        255
                    )

                canvas.drawCircle(
                    x,
                    y,
                    dp(3.5f),
                    paint
                )
            }

            paint.color =
                Color.rgb(
                    168,
                    174,
                    187
                )

            paint.textSize =
                sp(10f)

            paint.textAlign =
                Paint.Align.CENTER

            for (
                index in data.indices
            ) {

                val x =
                    if (pointCount == 1) {
                        left +
                            chartWidth / 2f
                    } else {
                        left +
                            chartWidth *
                            index /
                            (pointCount - 1).toFloat()
                    }

                val label =
                    monthShort(
                        data[index].year,
                        data[index].month
                    )

                canvas.drawText(
                    label,
                    x,
                    height.toFloat() -
                        dp(18f),
                    paint
                )
            }

            paint.textAlign =
                Paint.Align.LEFT
        }

        private fun monthShort(
            year: Int,
            month: Int
        ): String {

            val calendar =
                Calendar.getInstance().apply {
                    set(
                        year,
                        month,
                        1
                    )
                }

            return SimpleDateFormat(
                "MMM",
                Locale.ENGLISH
            ).format(
                calendar.time
            )
        }

        private fun moneyShort(
            value: Double
        ): String {

            if (value >= 1000.0) {

                return String.format(
                    Locale.US,
                    "€%.1fk",
                    value / 1000.0
                )
            }

            return String.format(
                Locale.US,
                "€%.0f",
                value
            )
        }

        private fun dp(
            value: Float
        ): Float {

            return value *
                resources.displayMetrics.density
        }

        private fun sp(
            value: Float
        ): Float {

            return value *
                resources.displayMetrics.scaledDensity
        }
    }
        private fun loadCurrentMonthIncome(): Double {

        val key = currentMonthKey(
            selectedYear,
            selectedMonth
        )

        return prefs.getString(
            "income_$key",
            "0"
        )?.toDoubleOrNull() ?: 0.0
    }

    private fun currentMonthExpenses(): Double {

        return loadExpenses(
            selectedYear,
            selectedMonth
        ).sumOf {
            it.amount
        }
    }

    private fun formatPercentage(
        value: Double
    ): String {

        return String.format(
            Locale.US,
            "%.0f%%",
            value
        )
    }

    private fun safeSavingsRate(
        income: Double,
        expenses: Double
    ): Double {

        if (income <= 0.0) {
            return 0.0
        }

        return (
            (income - expenses) /
                income
            ) * 100.0
    }

    private fun getMonthLabel(
        year: Int,
        month: Int
    ): String {

        val calendar =
            Calendar.getInstance().apply {
                set(
                    year,
                    month,
                    1
                )
            }

        return SimpleDateFormat(
            "MMMM yyyy",
            Locale.ENGLISH
        ).format(
            calendar.time
        )
    }

    override fun onBackPressed() {
        finish()
    }
}
