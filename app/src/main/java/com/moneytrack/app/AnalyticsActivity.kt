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
import android.widget.FrameLayout
import android.widget.ImageView
import eightbitlab.com.blurview.BlurTarget
import eightbitlab.com.blurview.BlurView
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

                val root = FrameLayout(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
        }

        val wallpaperBlurTarget = BlurTarget(this).apply {
    val wallpaper = ImageView(this@AnalyticsActivity).apply {
        setImageResource(R.drawable.moneytrack_background)
        scaleType = ImageView.ScaleType.CENTER_CROP
    }

    addView(
        wallpaper,
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
    )
}

root.addView(
    wallpaperBlurTarget,
    FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )
)

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isFillViewport = true
            overScrollMode = ScrollView.OVER_SCROLL_NEVER
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(20),
                dp(20),
                dp(20),
                dp(32)
            )
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val backButton = TextView(this).apply {
            text = "‹"
            textSize = 36f
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
applyGlassCard(breakdownCard)

val density = resources.displayMetrics.density

breakdownCard.setPadding(14, 14, 14, 14)

breakdownCard.background =
    android.graphics.drawable.GradientDrawable().apply {
        shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        cornerRadius = 18f * density

        // Same translucent gray glass as MainActivity
        setColor(Color.argb(15, 245, 247, 255))

        // Same semi-transparent outline
        setStroke(
            density.toInt().coerceAtLeast(1),
            Color.argb(
    25,
    Color.red(AppearanceManager.getAccentColor(this@AnalyticsActivity)),
Color.green(AppearanceManager.getAccentColor(this@AnalyticsActivity)),
Color.blue(AppearanceManager.getAccentColor(this@AnalyticsActivity))
)
        )
    }

// Same raised effect as MainActivity
breakdownCard.elevation = 8f * density
        

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

        val breakdownCardWrapper = FrameLayout(this).apply {
    clipToOutline = true
    background = android.graphics.drawable.GradientDrawable().apply {
        shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        cornerRadius = 18f * resources.displayMetrics.density
    }
}

val breakdownBlurView = BlurView(this).apply {
    setupWith(wallpaperBlurTarget, 4f, false)
        .setBlurRadius(3f)
        .setOverlayColor(Color.TRANSPARENT)
}

breakdownCardWrapper.addView(
    breakdownBlurView,
    FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )
)

breakdownCardWrapper.addView(
    breakdownCard,
    FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    )
)

content.addView(
    breakdownCardWrapper,
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
        applyGlassCard(ratioCard)

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

        val ratioCardWrapper = FrameLayout(this).apply {
            clipToOutline = true
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 18f * resources.displayMetrics.density
            }
        }

        val ratioBlurView = BlurView(this).apply {
            setupWith(wallpaperBlurTarget, 4f, false)
                .setBlurRadius(3f)
                .setOverlayColor(Color.TRANSPARENT)
        }

        ratioCardWrapper.addView(
            ratioBlurView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        ratioCardWrapper.addView(
            ratioCard,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        content.addView(
            ratioCardWrapper,
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
        applyGlassCard(trendCard)

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

        val trendCardWrapper = FrameLayout(this).apply {
            clipToOutline = true
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 18f * resources.displayMetrics.density
            }
        }

        val trendBlurView = BlurView(this).apply {
            setupWith(wallpaperBlurTarget, 4f, false)
                .setBlurRadius(3f)
                .setOverlayColor(Color.TRANSPARENT)
        }

        trendCardWrapper.addView(
            trendBlurView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        trendCardWrapper.addView(
            trendCard,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        content.addView(
            trendCardWrapper,
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
        applyGlassCard(dailyCard)

        val dailyCardWrapper = FrameLayout(this).apply {
            clipToOutline = true
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 18f * resources.displayMetrics.density
            }
        }

        val dailyBlurView = BlurView(this).apply {
            setupWith(wallpaperBlurTarget, 4f, false)
                .setBlurRadius(3f)
                .setOverlayColor(Color.TRANSPARENT)
        }

        dailyCardWrapper.addView(
            dailyBlurView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        dailyCardWrapper.addView(
            dailyCard,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        statsRow.addView(
            dailyCardWrapper,
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
        applyGlassCard(savingsCard)

        val savingsCardWrapper = FrameLayout(this).apply {
            clipToOutline = true
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 18f * resources.displayMetrics.density
            }
        }

        val savingsBlurView = BlurView(this).apply {
            setupWith(wallpaperBlurTarget, 4f, false)
                .setBlurRadius(3f)
                .setOverlayColor(Color.TRANSPARENT)
        }

        savingsCardWrapper.addView(
            savingsBlurView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        savingsCardWrapper.addView(
            savingsCard,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
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
            savingsCardWrapper,
            savingsParams
        )
    

        content.addView(
            statsRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val yearlyTitle = sectionTitle(
            "YEARLY OVERVIEW"
        )

        content.addView(
            yearlyTitle,
            sectionTitleParams()
        )

        var yearlyYear = selectedYear

        val yearlyCard = createCard()
        applyGlassCard(yearlyCard)

        val yearlySelector = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val previousYear = TextView(this).apply {
            text = "‹"
            textSize = 28f
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(
                0,
                0,
                dp(10),
                dp(2)
            )
        }

        val yearlyYearText = TextView(this).apply {
            text = yearlyYear.toString()
            textSize = 18f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val nextYear = TextView(this).apply {
            text = "›"
            textSize = 28f
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(
                dp(10),
                0,
                0,
                dp(2)
            )
        }

        yearlySelector.addView(
            previousYear,
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        yearlySelector.addView(
            yearlyYearText,
            LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            )
        )

        yearlySelector.addView(
            nextYear,
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        yearlyCard.addView(
            yearlySelector,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val yearlyView = YearlyOverviewView(this)

        fun updateYearlyOverview() {

            var yearlyIncome = 0.0
            var yearlyExpenses = 0.0

            for (month in 0..11) {

                val monthData = loadMonthData(
                    yearlyYear,
                    month
                )

                yearlyIncome += monthData.income
                yearlyExpenses += monthData.expenses
            }

            yearlyYearText.text =
                yearlyYear.toString()

            yearlyView.setValues(
                yearlyIncome,
                yearlyExpenses
            )
        }

        previousYear.setOnClickListener {
            yearlyYear--
            updateYearlyOverview()
        }

        nextYear.setOnClickListener {
            yearlyYear++
            updateYearlyOverview()
        }

        yearlyCard.addView(
            yearlyView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(210)
            )
        )

        updateYearlyOverview()

        
        val yearlyCardWrapper = FrameLayout(this).apply {
            clipToOutline = true
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 18f * resources.displayMetrics.density
            }
        }

        val yearlyBlurView = BlurView(this).apply {
            setupWith(wallpaperBlurTarget, 4f, false)
                .setBlurRadius(3f)
                .setOverlayColor(Color.TRANSPARENT)
        }

        yearlyCardWrapper.addView(
            yearlyBlurView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        yearlyCardWrapper.addView(
            yearlyCard,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        content.addView(
            yearlyCardWrapper,
            cardParams()
        )
        
        val yearlyBreakdownTitle = sectionTitle(
    "YEARLY SPENDING BREAKDOWN"
)

content.addView(
    yearlyBreakdownTitle,
    sectionTitleParams()
)

var breakdownYear = selectedYear
var refreshYearlyRecords: ((Int) -> Unit)? = null

val yearlyBreakdownCard = createCard()
applyGlassCard(yearlyBreakdownCard)

val breakdownYearSelector =
    LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
    }

val breakdownPreviousYear =
    TextView(this).apply {
        text = "‹"
        textSize = 28f
        setTextColor(white)
        gravity = Gravity.CENTER
        setPadding(
            0,
            0,
            dp(10),
            dp(2)
        )
    }

val breakdownYearText =
    TextView(this).apply {
        text = breakdownYear.toString()
        textSize = 18f
        setTextColor(white)
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
    }

val breakdownNextYear =
    TextView(this).apply {
        text = "›"
        textSize = 28f
        setTextColor(white)
        gravity = Gravity.CENTER
        setPadding(
            dp(10),
            0,
            0,
            dp(2)
        )
    }

breakdownYearSelector.addView(
    breakdownPreviousYear,
    LinearLayout.LayoutParams(
        dp(48),
        dp(48)
    )
)

breakdownYearSelector.addView(
    breakdownYearText,
    LinearLayout.LayoutParams(
        0,
        dp(48),
        1f
    )
)

breakdownYearSelector.addView(
    breakdownNextYear,
    LinearLayout.LayoutParams(
        dp(48),
        dp(48)
    )
)

yearlyBreakdownCard.addView(
    breakdownYearSelector,
    LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
)

val yearlyBreakdownDonut =
    DonutChartView(this)

val yearlyBreakdownLegend =
    LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
    }

fun updateYearlyBreakdown() {

    val totals =
        loadYearlyCategoryTotals(
            breakdownYear
        )

    breakdownYearText.text =
        breakdownYear.toString()

    yearlyBreakdownDonut.setData(
        totals
    )

    yearlyBreakdownLegend.removeAllViews()

    yearlyBreakdownLegend.addView(
    createCategoryLegend(
        totals
    )
)

refreshYearlyRecords?.invoke(breakdownYear)
}

breakdownPreviousYear.setOnClickListener {
    breakdownYear--
    updateYearlyBreakdown()
}

breakdownNextYear.setOnClickListener {
    breakdownYear++
    updateYearlyBreakdown()
}

yearlyBreakdownCard.addView(
    yearlyBreakdownDonut,
    LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        dp(245)
    )
)

yearlyBreakdownCard.addView(
    yearlyBreakdownLegend,
    LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
)

updateYearlyBreakdown()


val yearlyBreakdownWrapper = FrameLayout(this).apply {
    clipToOutline = true
    background = android.graphics.drawable.GradientDrawable().apply {
        shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        cornerRadius = 18f * resources.displayMetrics.density
    }
}

val yearlyBreakdownBlurView = BlurView(this).apply {
    setupWith(wallpaperBlurTarget, 4f, false)
        .setBlurRadius(3f)
        .setOverlayColor(Color.TRANSPARENT)
}

yearlyBreakdownWrapper.addView(
    yearlyBreakdownBlurView,
    FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )
)

yearlyBreakdownWrapper.addView(
    yearlyBreakdownCard,
    FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    )
)

content.addView(
    yearlyBreakdownWrapper,
    cardParams()
)

addYearlyRecords(
    content,
    wallpaperBlurTarget,
    breakdownYear
)

refreshYearlyRecords = { year ->
    content.removeViews(content.childCount - 3, 3)

    addYearlyRecords(
        content,
        wallpaperBlurTarget,
        year
    )
}

refreshYearlyRecords?.invoke(breakdownYear)

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

        
private fun applyGlassCard(card: LinearLayout) {
    val density = resources.displayMetrics.density

    card.background =
        android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 18f * density

            setColor(Color.argb(15, 245, 247, 255))

            setStroke(
                density.toInt().coerceAtLeast(1),
                Color.argb(110, 220, 225, 235)
            )
        }

    card.elevation = 8f * density
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

        private fun loadYearlyCategoryTotals(
    year: Int
): LinkedHashMap<String, Double> {

    val result =
        LinkedHashMap<String, Double>()

    for (month in 0..11) {

        val expenses = loadExpenses(
            year,
            month
        )

        for (expense in expenses) {

            val previous =
                result[expense.category] ?: 0.0

            result[expense.category] =
                previous + expense.amount
        }
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
        val originalOrder = totals.keys.toList()
        val sortedEntries = totals.entries.sortedByDescending { it.value }

            for ((category, amount) in sortedEntries) {
        val index = originalOrder.indexOf(category)

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
                ) + "  " + money(amount)
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
        private inner class YearlyOverviewView(
        context: Context
    ) : View(context) {

        private val paint = Paint(
            Paint.ANTI_ALIAS_FLAG
        )

        private var income = 0.0
        private var expenses = 0.0
        private var animatedExpenseRatio = 0f
        private var animator: ValueAnimator? = null

        fun setValues(
            newIncome: Double,
            newExpenses: Double
        ) {
            income = newIncome
            expenses = newExpenses

            val targetExpenseRatio =
                if (income > 0.0) {
                    max(
                        0f,
                        min(
                            1f,
                            (expenses / income).toFloat()
                        )
                    )
                } else {
                    0f
                }

            animator?.cancel()

            animator = ValueAnimator.ofFloat(
                0f,
                targetExpenseRatio
            ).apply {
                duration = 900L
                interpolator = DecelerateInterpolator()

                addUpdateListener { animation ->
                    animatedExpenseRatio =
                        animation.animatedValue as Float
                    invalidate()
                }

                start()
            }
        }

        override fun onDraw(
            canvas: Canvas
        ) {
            super.onDraw(canvas)

            val left = dp(2f)
            val right = width.toFloat() - dp(2f)
            val lineWidth = right - left

            val incomeLineY = dp(70f)
            val expenseLineY = dp(174f)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(5f)
            paint.strokeCap = Paint.Cap.ROUND
            paint.color = Color.rgb(42, 45, 54)

            canvas.drawLine(
                left,
                incomeLineY,
                right,
                incomeLineY,
                paint
            )

            canvas.drawLine(
                left,
                expenseLineY,
                right,
                expenseLineY,
                paint
            )

            if (income > 0.0) {
                paint.color = blue

                canvas.drawLine(
                    left,
                    incomeLineY,
                    right,
                    incomeLineY,
                    paint
                )

                if (animatedExpenseRatio > 0f) {
                    paint.color = amber

                    canvas.drawLine(
                        left,
                        expenseLineY,
                        left +
                            lineWidth *
                            animatedExpenseRatio,
                        expenseLineY,
                        paint
                    )
                }
            }

            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textSize = sp(15f)
            paint.textAlign = Paint.Align.LEFT
            paint.color = white

            canvas.drawText(
                "Income",
                left,
                dp(24f),
                paint
            )

            canvas.drawText(
                "Expenses",
                left,
                dp(128f),
                paint
            )

            canvas.drawText(
                money(income),
                left,
                dp(47f),
                paint
            )

            canvas.drawText(
                money(expenses),
                left,
                dp(151f),
                paint
            )

            paint.textAlign = Paint.Align.RIGHT
            paint.color = secondary

            canvas.drawText(
                if (income > 0.0) "100%" else "—",
                right,
                dp(47f),
                paint
            )

            canvas.drawText(
                if (income > 0.0) {
                    String.format(
                        Locale.US,
                        "%.0f%%",
                        (expenses / income) * 100.0
                    )
                } else {
                    "—"
                },
                right,
                dp(151f),
                paint
            )

            paint.textAlign = Paint.Align.LEFT
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

        private fun addYearlyRecords(
    content: LinearLayout,
    wallpaperBlurTarget: BlurTarget,
    year: Int
) {
    val calendar = Calendar.getInstance()
    val currentMonth =
        if (year == calendar.get(Calendar.YEAR)) {
            calendar.get(Calendar.MONTH)
        } else {
            Calendar.DECEMBER
        }

        val recordedMonths = (0..currentMonth).map { month ->
            loadMonthData(year, month)
        }.filter { month ->
            month.income > 0.0 || month.expenses > 0.0
        }

        val highestSpending = recordedMonths
            .filter { it.expenses > 0.0 }
            .maxByOrNull { it.expenses }

        val lowestSpending = recordedMonths
            .minByOrNull { it.expenses }

        val highestIncome = recordedMonths
            .filter { it.income > 0.0 }
            .maxByOrNull { it.income }

        val highestSaving = recordedMonths
            .filter { it.income - it.expenses > 0.0 }
            .maxByOrNull { it.income - it.expenses }

                content.addView(
            sectionTitle("YEARLY RECORDS").apply {
                textSize = 16f
            },
            sectionTitleParams()
        )

        val firstRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        firstRow.addView(
            createYearlyRecordCard(
                "Highest Spending",
                highestSpending?.let { money(it.expenses) } ?: "—",
                highestSpending?.let {
                    getMonthLabel(it.year, it.month)
                } ?: "No expenses yet",
                wallpaperBlurTarget
            ),
            yearlyRecordCardParams(true)
        )

        firstRow.addView(
            createYearlyRecordCard(
                "Lowest Spending",
                lowestSpending?.let { money(it.expenses) } ?: "—",
                lowestSpending?.let {
                    getMonthLabel(it.year, it.month)
                } ?: "No recorded months",
                wallpaperBlurTarget
            ),
            yearlyRecordCardParams(false)
        )

        content.addView(
            firstRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        )

        val secondRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        secondRow.addView(
            createYearlyRecordCard(
                "Highest Income",
                highestIncome?.let { money(it.income) } ?: "—",
                highestIncome?.let {
                    getMonthLabel(it.year, it.month)
                } ?: "No income yet",
                wallpaperBlurTarget
            ),
            yearlyRecordCardParams(true)
        )

        secondRow.addView(
            createYearlyRecordCard(
                "Highest Saving",
                highestSaving?.let {
                    money(it.income - it.expenses)
                } ?: "—",
                highestSaving?.let {
                    getMonthLabel(it.year, it.month)
                } ?: "No positive saving",
                wallpaperBlurTarget
            ),
            yearlyRecordCardParams(false)
        )

        content.addView(
            secondRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(4)
            }
        )
    }

    private fun yearlyRecordCardParams(
        firstCard: Boolean
    ): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ).apply {
            if (firstCard) {
                rightMargin = dp(6)
            } else {
                leftMargin = dp(6)
            }
        }
    }

    private fun createYearlyRecordCard(
        label: String,
        value: String,
        period: String,
        wallpaperBlurTarget: BlurTarget
    ): FrameLayout {
        val density = resources.displayMetrics.density

        val wrapper = FrameLayout(this).apply {
            clipToOutline = true
            background =
                android.graphics.drawable.GradientDrawable().apply {
                    shape =
                        android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 18f * density
                }
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            minimumHeight = dp(124)

            setPadding(
                dp(10),
                dp(14),
                dp(10),
                dp(14)
            )

            applyGlassCard(this)

            val labelView = TextView(this@AnalyticsActivity).apply {
                text = label
                textSize = 12f
                setTextColor(secondary)
                gravity = Gravity.CENTER
                maxLines = 2
                typeface = Typeface.DEFAULT_BOLD
            }

            addView(
                labelView,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            val valueView = TextView(this@AnalyticsActivity).apply {
                text = value
                textSize = 18f
                setTextColor(white)
                gravity = Gravity.CENTER
                maxLines = 2
                typeface = Typeface.DEFAULT_BOLD

                setPadding(
                    0,
                    dp(8),
                    0,
                    dp(5)
                )
            }

            addView(
                valueView,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            val periodView = TextView(this@AnalyticsActivity).apply {
                text = period
                textSize = 11f
                setTextColor(secondary)
                gravity = Gravity.CENTER
                maxLines = 2
            }

            addView(
                periodView,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        val blurView = BlurView(this).apply {
            setupWith(
                wallpaperBlurTarget,
                4f,
                false
            )
                .setBlurRadius(3f)
                .setOverlayColor(Color.TRANSPARENT)
        }

        wrapper.addView(
            blurView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        wrapper.addView(
            card,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        return wrapper
    }

    override fun onBackPressed() {
        finish()
    }
}
