package com.moneytrack.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import eightbitlab.com.blurview.BlurTarget
import eightbitlab.com.blurview.BlurView
import android.widget.NumberPicker
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import android.text.TextWatcher
import android.text.Editable
import java.util.Calendar
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var wallpaperBlurTarget: BlurTarget

    companion object {
        private const val REQUEST_EXPORT_BACKUP = 1001
        private const val REQUEST_IMPORT_BACKUP = 1002
    }

    private lateinit var categorySpinner: Spinner
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private lateinit var expensesContainer: LinearLayout
    private lateinit var searchResultsContainer: LinearLayout

    private lateinit var monthlyText: TextView
    private lateinit var remainingText: TextView
    private lateinit var incomeDisplay: TextView
    private lateinit var monthText: TextView

    private lateinit var menuDrawer: LinearLayout
    private lateinit var menuOverlay: View

    private var activeCircleView: CircularProgressView? = null
    private var activeDayNumberText: TextView? = null

    private var isMenuOpen = false
    private var isHistoryOpen = false
    private var isSearchOpen = false

    private var income = 0.0
    private val expenses = mutableListOf<Expense>()
    private val incomeEntries = mutableListOf<IncomeEntry>()
    

    private val prefs by lazy {
        getSharedPreferences(
            "moneytrack_data",
            Context.MODE_PRIVATE
        )
    }

    private var selectedYear = 0
    private var selectedMonth = 0

    private val midnightHandler = Handler(Looper.getMainLooper())
    private val midnightRunnable = object : Runnable {
        override fun run() {
            updateCircleStateOnly()
            midnightHandler.postDelayed(this, 60000)
        }
    }

    data class Expense(
        val description: String,
        val category: String,
        val amount: Double,
        val date: String = "",
        val time: String = ""
    )

    data class IncomeEntry(
        val source: String,
        val amount: Double,
        val date: String = "",
        val time: String = ""
    )

    private val defaultCategories = arrayOf(
        "Housing",
        "Utilities",
        "Food",
        "Grocery / household",
        "Fuel / Transport",
        "Car",
        "Phone / Internet",
        "Subscriptions",
        "Shopping",
        "Health",
        "Entertainment",
        "Travel",
        "Personal Care",
        "Gifts & Donations",
        "Other"
    )

    private val categories = defaultCategories.toMutableList()
    private val categoriesPreferenceKey = "category_names_v1"

    private val backgroundColor = Color.rgb(7, 8, 12)
    private val cardColor = Color.rgb(20, 22, 29)
    private val surfaceColor = Color.rgb(28, 31, 40)

    private lateinit var appearanceTheme: AppearanceManager.Theme

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

        loadCategories()
        buildInterface()
        loadData()
        updateMonthDisplay()
        updateTotals()
        refreshExpenses()
    }

    override fun onStart() {
        super.onStart()
        midnightHandler.removeCallbacks(midnightRunnable)
        midnightHandler.postDelayed(midnightRunnable, 60000)
    }

    override fun onStop() {
        super.onStop()
        midnightHandler.removeCallbacks(midnightRunnable)
    }

    override fun onResume() {
        super.onResume()

        loadCategories()
        buildInterface()
        loadData()
        updateMonthDisplay()
        updateTotals()
        refreshExpenses()
    }

    private class CircularProgressView(context: Context, private var progressPercent: Float) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rect = RectF()

        fun setProgress(newProgress: Float) {
            if (this.progressPercent != newProgress) {
                this.progressPercent = newProgress
                invalidate()
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val size = width.coerceAtMost(height).toFloat()
val strokeWidth = 14f * resources.displayMetrics.density
val padding = strokeWidth / 2f
            rect.set(padding, padding, size - padding, size - padding)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeWidth
            paint.color = Color.rgb(50, 50, 50)
            canvas.drawArc(rect, 0f, 360f, false, paint)

            paint.color = Color.rgb(214, 214, 214)
            paint.strokeCap = Paint.Cap.ROUND
            val sweepAngle = 360f * progressPercent.coerceIn(0f, 1f)
            canvas.drawArc(rect, -90f, sweepAngle, false, paint)
        }
    }
    private fun updateCircleStateOnly() {
        val calInstance = Calendar.getInstance()
        val currentYear = calInstance.get(Calendar.YEAR)
        val currentMonth = calInstance.get(Calendar.MONTH)
        val todayDay = calInstance.get(Calendar.DAY_OF_MONTH)

        val totalDaysInMonth = Calendar.getInstance().apply {
            set(selectedYear, selectedMonth, 1)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)

        val displayDayText: String
        val monthProgress: Float

        when {
    selectedYear == currentYear && selectedMonth == currentMonth -> {
        displayDayText = todayDay.toString()
        monthProgress = todayDay.toFloat() / totalDaysInMonth.toFloat()
    }
    selectedYear < currentYear || (selectedYear == currentYear && selectedMonth < currentMonth) -> {
        displayDayText = "--"
        monthProgress = 1f
    }
    else -> {
        displayDayText = "--"
        monthProgress = 0f
    }
        }

        activeDayNumberText?.text = displayDayText
        activeCircleView?.setProgress(monthProgress)
    }

    private fun buildInterface() {
        appearanceTheme = AppearanceManager.getTheme(this)

        isHistoryOpen = false

        val root = FrameLayout(this).apply {
        setBackgroundColor(Color.TRANSPARENT)
    }

    wallpaperBlurTarget = BlurTarget(this).apply {
    val wallpaper = ImageView(this@MainActivity).apply {
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

    val mainContent = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(Color.TRANSPARENT)
    }

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.TRANSPARENT)
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
                appearanceTheme.startColor,
                appearanceTheme.endColor,
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

        val centerContainer = FrameLayout(this).apply {
            setOnClickListener {
                openMenu()
            }
        }

        val calInstance = Calendar.getInstance()
        val currentYear = calInstance.get(Calendar.YEAR)
        val currentMonth = calInstance.get(Calendar.MONTH)
        val todayDay = calInstance.get(Calendar.DAY_OF_MONTH)

        val totalDaysInMonth = Calendar.getInstance().apply {
            set(selectedYear, selectedMonth, 1)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)

        val displayDayText: String
        val monthProgress: Float

        when {
    selectedYear == currentYear && selectedMonth == currentMonth -> {
        displayDayText = todayDay.toString()
        monthProgress = todayDay.toFloat() / totalDaysInMonth.toFloat()
    }
    selectedYear < currentYear || (selectedYear == currentYear && selectedMonth < currentMonth) -> {
        displayDayText = "--"
        monthProgress = 1f
    }
    else -> {
        displayDayText = "--"
        monthProgress = 0f
    }
        }

        val circleView = CircularProgressView(this, monthProgress)
        activeCircleView = circleView

        val circleSize = (135 * resources.displayMetrics.density).toInt()
        val circleParams = FrameLayout.LayoutParams(circleSize, circleSize).apply {
            gravity = Gravity.CENTER
        }
        centerContainer.addView(circleView, circleParams)

        val textLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        val dayNumberText = TextView(this).apply {
            text = displayDayText
            textSize = 40f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        activeDayNumberText = dayNumberText

        val dayLabelText = TextView(this).apply {
            text = "Day"
            textSize = 20f
            setTextColor(white)
            typeface = Typeface.DEFAULT
            gravity = Gravity.CENTER
        }

        textLayout.addView(dayNumberText)
        textLayout.addView(dayLabelText)

        val textParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        centerContainer.addView(textLayout, textParams)

        val centerContainerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
        centerContainerParams.setMargins(0, 4, 0, 4)
        bankCard.addView(centerContainer, centerContainerParams)

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

        val density = resources.displayMetrics.density
val isTablet = resources.configuration.smallestScreenWidthDp >= 600

val cardWidth = if (isTablet) {
    (440 * density).toInt()
} else {
    LinearLayout.LayoutParams.MATCH_PARENT
}

content.addView(
    bankCard,
    LinearLayout.LayoutParams(
        cardWidth,
        (190 * density).toInt()
    ).apply {
        gravity = Gravity.CENTER_HORIZONTAL
    }
)

        
        val monthCard = object : LinearLayout(this) {
            private var swipeStartX = 0f
            private var swipeStartY = 0f
            private var swipeHandled = false

            override fun onInterceptTouchEvent(
                event: MotionEvent
            ): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        swipeStartX = event.x
                        swipeStartY = event.y
                        swipeHandled = false
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.x - swipeStartX
                        val deltaY = event.y - swipeStartY

                        if (
                            kotlin.math.abs(deltaX) > 12f &&
                            kotlin.math.abs(deltaX) >
                                kotlin.math.abs(deltaY)
                        ) {
                            swipeHandled = true
                            parent?.requestDisallowInterceptTouchEvent(true)
                            return true
                        }
                    }
                }

                return super.onInterceptTouchEvent(event)
            }

            override fun onTouchEvent(event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_UP -> {
                        if (swipeHandled) {
                            val deltaX = event.x - swipeStartX

                            swipeHandled = false
                            parent?.requestDisallowInterceptTouchEvent(false)

                            this@MainActivity.changeMonth(
                                if (deltaX < 0) 1 else -1
                            )
                            return true
                        }
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        swipeHandled = false
                        parent?.requestDisallowInterceptTouchEvent(false)
                        return true
                    }
                }

                return if (swipeHandled) {
                    true
                } else {
                    super.onTouchEvent(event)
                }
            }
        
        }.apply {
    orientation = LinearLayout.VERTICAL
    setPadding(14, 14, 14, 14)

    val density = resources.displayMetrics.density

    background = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 18f * density

        // Translucent gray glass: 25% opaque
        setColor(Color.argb(15, 245, 247, 255))

        // Thin, semi-transparent outline
        setStroke(
            (density).toInt().coerceAtLeast(1),
            Color.argb(110, 220, 225, 235)
        )
    }

    // Raised 3D effect
    elevation = 8f * density
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
            setTextColor(appearanceTheme.accentColor)
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

                val searchButton = TextView(this).apply {
            text = "⌕  Search transactions"
            textSize = 14f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(16, 12, 16, 12)
            background = roundedBackground(
                Color.argb(20, 245, 247, 255),
                50f
            )
            setOnClickListener {
                showSearchScreen()
            }
        }

        val searchButtonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        searchButtonParams.setMargins(0, 12, 0, 0)

        monthCard.addView(searchButton, searchButtonParams)

        val monthCardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        monthCardParams.setMargins(0, 10, 0, 0)

        val overviewTitle = sectionTitle(
            "OVERVIEW",
            white
        )

        val overviewTitleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        overviewTitleParams.setMargins(2, 24, 0, 10)

                
val monthCardWrapper = FrameLayout(this).apply {
    background = roundedBackground(Color.TRANSPARENT, 18f).apply {
        setStroke(
            (resources.displayMetrics.density).toInt().coerceAtLeast(1),
            Color.argb(55, 245, 247, 255)
        )
    }
    elevation = 8f * resources.displayMetrics.density
    clipToOutline = true
}


        val monthBlurView = BlurView(this).apply {
            setupWith(wallpaperBlurTarget, 4f, false)
                .setBlurRadius(3f)
                .setOverlayColor(Color.TRANSPARENT)
        }

        monthCardWrapper.addView(
            monthBlurView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        monthCardWrapper.addView(
    monthCard,
    FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    )
)

        content.addView(
            monthCardWrapper,
            monthCardParams
        )

        content.addView(
    overviewTitle,
    overviewTitleParams
)

        val overviewRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val expensesCard = createSmallCard()

        expensesCard.addView(
            smallLabel("Expenses")
        )

        monthlyText = TextView(this).apply {
            text = "€0.00"
            textSize = 21f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 5, 0, 0)
        }

        expensesCard.addView(monthlyText)

        val expensesCardWrapper = FrameLayout(this).apply {
    clipToOutline = true
    background = roundedBackground(Color.TRANSPARENT, 16f)
}

val expensesBlurView = BlurView(this).apply {
    setupWith(wallpaperBlurTarget, 4f, false)
        .setBlurRadius(3f)
        .setOverlayColor(Color.TRANSPARENT)
}

expensesCardWrapper.addView(
    expensesBlurView,
    FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )
)

expensesCardWrapper.addView(
    expensesCard,
    FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    )
)

overviewRow.addView(
    expensesCardWrapper,
    weightParams()
)

        val remainingCard = createSmallCard()

        remainingCard.addView(
            smallLabel("Balance")
        )

        remainingText = TextView(this).apply {
            text = "€0.00"
            textSize = 21f
            setTextColor(appearanceTheme.accentColor)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 5, 0, 0)
        }

        remainingCard.addView(remainingText)

        val remainingParams = weightParams()
        remainingParams.setMargins(10, 0, 0, 0)

                val remainingWrapper = FrameLayout(this).apply {
            clipToOutline = true
            background = roundedBackground(Color.TRANSPARENT, 16f)
        }

        val remainingBlur = BlurView(this).apply {
            setupWith(wallpaperBlurTarget, 4f, false)
                .setBlurRadius(3f)
                .setOverlayColor(Color.TRANSPARENT)
        }

        remainingWrapper.addView(
            remainingBlur,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        remainingWrapper.addView(
            remainingCard,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        overviewRow.addView(
            remainingWrapper,
            remainingParams
        )

        content.addView(
            overviewRow
        )

        val incomeTitle = sectionTitle(
            "MONTHLY INCOME",
            white
        )

        val incomeTitleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        incomeTitleParams.setMargins(2, 24, 0, 10)

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
            incomeDisplay
        )

        val clearIncomeButton =
    createLiquidGlassButton("Clear Income")

        
clearIncomeButton.setOnClickListener {
    val dialog = AlertDialog.Builder(this)
        .setMessage("Do you want to clear all income?")
        .setNegativeButton("CANCEL", null)
        .setPositiveButton("YES") { _, _ ->
            income = 0.0
            saveData()
            updateTotals()

            Toast.makeText(
                this,
                "Income cleared.",
                Toast.LENGTH_SHORT
            ).show()
        }
        .create()

    dialog.setOnShowListener {
    dialog.window?.setBackgroundDrawable(
        android.graphics.drawable.ColorDrawable(
            Color.rgb(20, 22, 29)
        )
    )

    dialog.findViewById<TextView>(android.R.id.message)
        ?.setTextColor(Color.WHITE)

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            ?.setTextColor(Color.WHITE)

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(Color.RED)
    }

    dialog.show()
}


                incomeCard.addView(
            clearIncomeButton,
            buttonParams()
        )

        content.addView(incomeCard)

        val addCard = createMainCard()

        categorySpinner = Spinner(this).apply {
            background = roundedBackground(
                surfaceColor,
                12f
            )
            setPadding(8, 0, 8, 0)
            setPopupBackgroundDrawable(
                ColorDrawable(surfaceColor)
            )
        }

        categoryAdapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            categories
        ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: android.view.ViewGroup
            ): View {
                return super.getView(
                    position,
                    convertView,
                    parent
                ).apply {
                    (this as? TextView)?.setTextColor(white)
                }
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: android.view.ViewGroup
            ): View {
                return super.getDropDownView(
                    position,
                    convertView,
                    parent
                ).apply {
                    (this as? TextView)?.setTextColor(white)
                }
            }
        }

                categoryAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        categorySpinner.adapter = categoryAdapter

        val manageCategoriesButton = Button(this).apply {
            text = "Manage Categories"
            setTextColor(white)
            
            val density = resources.displayMetrics.density

background = GradientDrawable().apply {
    shape = GradientDrawable.RECTANGLE
    cornerRadius = 12f * density

    // Same glass color and transparency
    setColor(Color.argb(64, 150, 155, 165))

    // Same semi-transparent border
    setStroke(
    density.toInt().coerceAtLeast(1),
    Color.argb(110, 220, 225, 235)
   )
}

// Same raised glass effect
elevation = 8f * density
            
            setOnClickListener {
                val editableCategories = categories
                    .filter { it != "Other" }
                    .toTypedArray()

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Manage Categories")
                    .setAdapter(
                        object : ArrayAdapter<String>(
                            this@MainActivity,
                            android.R.layout.simple_list_item_1,
                            editableCategories
                        ) {
                            override fun getView(
                                position: Int,
                                convertView: View?,
                                parent: android.view.ViewGroup
                            ): View {
                                return super.getView(
                                    position,
                                    convertView,
                                    parent
                                ).apply {
                                    (this as? TextView)?.setTextColor(white)
                                }
                            }
                        }
                    ) { _, position ->
                        val oldName = editableCategories[position]

                        val input = EditText(this@MainActivity).apply {
                            setText(oldName)
                            setSelection(text.length)
                            hint = "Category name"
                            setSingleLine(true)
                        }

                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Rename Category")
                            .setView(input)
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Save") { _, _ ->
                                val newName = input.text.toString().trim()

                                when {
                                    newName.isEmpty() -> {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Category name cannot be empty.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    categories.any {
                                        it.equals(newName, ignoreCase = true) &&
                                            it != oldName
                                    } -> {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "That category already exists.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                 
else -> {
    val index = categories.indexOf(oldName)

    if (index >= 0) {
        categories[index] = newName
        renameSavedExpenseCategories(oldName, newName)
        saveCategories()

        categoryAdapter.notifyDataSetChanged()

        Toast.makeText(
            this@MainActivity,
            "Category renamed.",
            Toast.LENGTH_SHORT
        ).show()
    }
}



 

                                }
                            }
                            .show()
                            .apply {
                                window?.setBackgroundDrawable(
                                    ColorDrawable(surfaceColor)
                                )
                                input.setTextColor(white)
                                input.setHintTextColor(Color.LTGRAY)
                                findViewById<TextView>(
                                    android.R.id.title
                                )?.setTextColor(white)
                                getButton(
                                    AlertDialog.BUTTON_POSITIVE
                                )?.setTextColor(white)
                                getButton(
                                    AlertDialog.BUTTON_NEGATIVE
                                )?.setTextColor(white)
                            }
                    }
                    .setNegativeButton("Close", null)
                    .show()
                    .apply {
                        window?.setBackgroundDrawable(
                            ColorDrawable(surfaceColor)
                        )
                        listView.setBackgroundColor(surfaceColor)
                    val titleId = resources.getIdentifier(
        "alertTitle", "id", "android"
    )
    findViewById<TextView>(titleId)?.setTextColor(white)
                    }
            }
        }

        val manageCategoriesWrapper = FrameLayout(this).apply {
    clipToOutline = true
    background = roundedBackground(Color.TRANSPARENT, 12f)
}

val manageCategoriesBlur = BlurView(this).apply {
    setupWith(wallpaperBlurTarget, 4f, false)
        .setBlurRadius(3f)
        .setOverlayColor(Color.TRANSPARENT)
}

manageCategoriesWrapper.addView(
    manageCategoriesBlur,
    FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )
)

manageCategoriesWrapper.addView(
    manageCategoriesButton,
    FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    )
)

addCard.addView(
    manageCategoriesWrapper,
    buttonParams()
)

                val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val addIncomeButton =
    createLiquidGlassButton("Add Income")

        addIncomeButton.setOnClickListener {
            showAddIncomeDialog()
        }

        val addExpenseButton =
    createLiquidGlassButton("Add Expense")

        addExpenseButton.setOnClickListener {
            showAddExpenseDialog()
        }

        actionRow.addView(
    addExpenseButton,
    LinearLayout.LayoutParams(0, -2, 1f).apply {
        setMargins(0, 8, 5, 0)
    }
)

actionRow.addView(
    addIncomeButton,
    LinearLayout.LayoutParams(0, -2, 1f).apply {
        setMargins(5, 8, 0, 0)
    }
)

        addCard.addView(actionRow)

        content.addView(addCard)

        
        val historyButtonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val recentExpensesButton =
            createLiquidGlassButton("Recent Expenses")

        val recentIncomesButton =
            createLiquidGlassButton("Recent Incomes")

        val accentColor =
            AppearanceManager.getAccentColor(this@MainActivity)

        recentExpensesButton.background =
            createLiquidGlassBackground(accentColor, true, 14)

        recentIncomesButton.background =
            createLiquidGlassBackground(accentColor, false, 14)

            
        val historyButtonHeight =
            (34 * resources.displayMetrics.density).toInt()

        recentExpensesButton.textSize = 13f
        recentIncomesButton.textSize = 13f

        recentExpensesButton.minHeight = historyButtonHeight
        recentIncomesButton.minHeight = historyButtonHeight

        val historyButtonPadding =
            (10 * resources.displayMetrics.density).toInt()

        recentExpensesButton.setPadding(
            historyButtonPadding, 0, historyButtonPadding, 0
        )
        recentIncomesButton.setPadding(
            historyButtonPadding, 0, historyButtonPadding, 0
        )
        

        
        recentExpensesButton.setOnClickListener {
            recentExpensesButton.background =
                createLiquidGlassBackground(accentColor, true, 14)
            recentIncomesButton.background =
                createLiquidGlassBackground(accentColor, false, 14)
            refreshExpenses()
        }

        recentIncomesButton.setOnClickListener {
            recentExpensesButton.background =
                createLiquidGlassBackground(accentColor, false, 14)
            recentIncomesButton.background =
                createLiquidGlassBackground(accentColor, true, 14)
            refreshIncomes()
        }
        

        historyButtonRow.addView(
            recentExpensesButton,
            LinearLayout.LayoutParams(0, -2, 1f).apply {
                setMargins(0, 0, 5, 0)
            }
        )

        historyButtonRow.addView(
            recentIncomesButton,
            LinearLayout.LayoutParams(0, -2, 1f).apply {
                setMargins(5, 0, 0, 0)
            }
        )

        val historyButtonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        historyButtonParams.setMargins(0, 24, 0, 10)

        content.addView(historyButtonRow, historyButtonParams)
        

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

        val backupCard = createMainCard()

        val exportButton = createGradientButton(
            "Export Backup",
            appearanceTheme.startColor,
            appearanceTheme.endColor
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
wallpaperBlurTarget,
FrameLayout.LayoutParams(
FrameLayout.LayoutParams.MATCH_PARENT,
FrameLayout.LayoutParams.MATCH_PARENT
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
        val menuTextColor = Color.rgb(210, 214, 223)
        val menuFillColor = Color.argb(240, 8, 10, 15)
        val menuCardColor = Color.argb(
            170,
            Color.red(appearanceTheme.accentColor),
            Color.green(appearanceTheme.accentColor),
            Color.blue(appearanceTheme.accentColor)
        )

        val menuCardBorderColor = Color.argb(
            90,
            Color.red(appearanceTheme.accentColor),
            Color.green(appearanceTheme.accentColor),
            Color.blue(appearanceTheme.accentColor)
        )

        fun dp(value: Int): Int {
            return (value * resources.displayMetrics.density).toInt()
        }

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
            setPadding(
                dp(16),
                dp(28),
                dp(16),
                dp(16)
            )
            elevation = dp(18).toFloat()

            background = GradientDrawable().apply {
                setColor(menuFillColor)
                setStroke(
                    dp(2),
                    appearanceTheme.accentColor
                )
                cornerRadius = dp(22).toFloat()
            }
        }

        val menuTitle = TextView(this).apply {
            text = "MONEY TRACK"
            textSize = 22f
            setTextColor(menuTextColor)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            letterSpacing = 0.08f
            setPadding(
                0,
                dp(4),
                0,
                dp(20)
            )
        }

        menuDrawer.addView(
            menuTitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        fun createMenuCard(
            title: String,
            iconRes: Int,
            action: () -> Unit
        ) {
            val card = TextView(this).apply {
                text = title
                textSize = 16f
                setTextColor(menuTextColor)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER_VERTICAL
                setPadding(
                    dp(16),
                    0,
                    dp(16),
                    0
                )
                compoundDrawablePadding = dp(14)

                val icon = getDrawable(iconRes)?.mutate()
                icon?.setTint(menuTextColor)

                setCompoundDrawablesWithIntrinsicBounds(
                    icon,
                    null,
                    null,
                    null
                )

                background = GradientDrawable().apply {
                    setColor(menuCardColor)
                    setStroke(
                        dp(1),
                        menuCardBorderColor
                    )
                    cornerRadius = dp(14).toFloat()
                }

                setOnClickListener {
                    action()
                }
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
            )

            params.setMargins(
                0,
                0,
                0,
                dp(8)
            )

            menuDrawer.addView(
                card,
                params
            )
        }

        createMenuCard(
            "HISTORY",
            android.R.drawable.ic_menu_recent_history
        ) {
            openHistory()
        }

        createMenuCard(
            "ANALYTICS",
            android.R.drawable.ic_menu_sort_by_size
        ) {
            closeMenu()

            startActivity(
                Intent(
                    this@MainActivity,
                    AnalyticsActivity::class.java
                )
            )
        }

        createMenuCard(
            "DEBT & LOAN",
            android.R.drawable.ic_menu_save
        ) {
            closeMenu()

            startActivity(
                Intent(
                    this@MainActivity,
                    LoansDebtsActivity::class.java
                )
            )
        }

        createMenuCard(
            "SCHEDULED & SAVINGS",
            android.R.drawable.ic_menu_today
        ) {
            closeMenu()

            startActivity(
                Intent(
                    this@MainActivity,
                    ScheduledActivity::class.java
                )
            )
        }

        createMenuCard(
            "CAR FUEL CALCULATOR",
            android.R.drawable.ic_menu_compass
        ) {
            closeMenu()

            startActivity(
                Intent(
                    this@MainActivity,
                    CarFuelCalculatorActivity::class.java
                )
            )
        }

        createMenuCard(
            "THEME",
            android.R.drawable.ic_menu_preferences
        ) {
            closeMenu()

            startActivity(
                Intent(
                    this@MainActivity,
                    AppearanceActivity::class.java
                )
            )
        }

        val drawerWidth =
            (resources.displayMetrics.widthPixels * 0.58f).toInt()

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
        menuOverlay.animate().alpha(1f).setDuration(220).start()
        menuDrawer.animate().translationX(0f).setDuration(220)
            .setInterpolator(AccelerateDecelerateInterpolator()).start()
    }

    private fun closeMenu() {
        if (!isMenuOpen) {
            return
        }
        isMenuOpen = false
        menuOverlay.animate().alpha(0f).setDuration(180)
            .withEndAction { menuOverlay.visibility = View.GONE }.start()
        menuDrawer.animate().translationX(-menuDrawer.width.toFloat()).setDuration(180)
            .setInterpolator(AccelerateDecelerateInterpolator()).start()
    }

    private fun openHistory() {
        closeMenu()
        isHistoryOpen = true

        val historyRoot = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL
    setBackgroundColor(Color.TRANSPARENT)
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
            setBackgroundColor(Color.rgb(45, 49, 60))
        }

        val dividerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1
        )
        dividerParams.setMargins(0, 8, 0, 0)
        historyRoot.addView(divider, dividerParams)

        val historyScrollView = ScrollView(this).apply {
            isFillViewport = true
            overScrollMode = ScrollView.OVER_SCROLL_NEVER
        }

        val historyContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        historyScrollView.addView(
            historyContent,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        historyRoot.addView(
            historyScrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val monthKeys = prefs.all.keys
            .mapNotNull { key ->
                when {
                    key.startsWith("income_") -> key.removePrefix("income_")
                    key.startsWith("expenses_") -> key.removePrefix("expenses_")
                    else -> null
                }
            }
            .filter { it.matches(Regex("\\d{4}-\\d{2}")) }
            .distinct()
            .filter { key ->
                val incomeValue = prefs.getString("income_$key", "0")?.toDoubleOrNull() ?: 0.0
                val expensesValue = prefs.getString("expenses_$key", null)
                val hasExpenses = !expensesValue.isNullOrEmpty() &&
                    try { JSONArray(expensesValue).length() > 0 } catch (_: Exception) { false }
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

                val monthName = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(
                    Calendar.getInstance().apply { set(year, month, 1) }.time
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

                historyContent.addView(
                    monthItem,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }

        val historyScreenRoot = FrameLayout(this)

val historyWallpaper = ImageView(this).apply {
    setImageResource(R.drawable.moneytrack_background)
    scaleType = ImageView.ScaleType.CENTER_CROP
    contentDescription = null
}

historyScreenRoot.addView(
    historyWallpaper,
    FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )
)

historyScreenRoot.addView(
    historyRoot,
    FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )
)

setContentView(historyScreenRoot)
    }

    override fun onBackPressed() {
        if (isMenuOpen) {
            closeMenu()
            return
        }
                if (isSearchOpen) {
            isSearchOpen = false
            buildInterface()
            return
        }
        if (isHistoryOpen) {
            isHistoryOpen = false
            buildInterface()
            return
        }
        super.onBackPressed()
    }

    
    
    private fun showAddIncomeDialog() {
        val sourceInput = createInput(
            "Income source",
            InputType.TYPE_CLASS_TEXT
        )

        val input = createInput(
            "Enter income amount",
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        )

        val dialogContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 8)
            addView(sourceInput, inputParams())
            addView(input, inputParams())
        }
        

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Income")
            .setView(dialogContent)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(surfaceColor))
            val titleId = resources.getIdentifier(
    "alertTitle", "id", "android"
)
dialog.findViewById<TextView>(titleId)?.setTextColor(white)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(white)

            
            
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                if (saveIncome(sourceInput, input)) dialog.dismiss()
            }
            
        }

        dialog.show()
    }

    private fun showAddExpenseDialog() {
        val dialogSpinner = Spinner(this).apply {
            background = roundedBackground(surfaceColor, 12f)
            setPadding(8, 0, 8, 0)
            setPopupBackgroundDrawable(ColorDrawable(surfaceColor))
            adapter = categoryAdapter

            if (categories.isNotEmpty()) {
                setSelection(
                    categorySpinner.selectedItemPosition.coerceIn(
                        0, categories.lastIndex
                    )
                )
            }
        }

        val descriptionField = createInput(
            "Description (optional)",
            InputType.TYPE_CLASS_TEXT
        )

        val amountField = createInput(
            "Amount",
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        )

        val categorySelector = FrameLayout(this).apply {
    val density = resources.displayMetrics.density

    addView(
        dialogSpinner,
        FrameLayout.LayoutParams(-1, -2)
    )

    addView(
        TextView(this@MainActivity).apply {
            text = "☰"
            textSize = 20f
            setTextColor(white)
            gravity = Gravity.CENTER
            isClickable = false
            setPadding(0, 0, (12 * density).toInt(), 0)
        },
        FrameLayout.LayoutParams(
            (40 * density).toInt(),
            -1,
            Gravity.END or Gravity.CENTER_VERTICAL
        )
    )
}

val dialogContent = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL
    setPadding(20, 8, 20, 8)
    addView(categorySelector, spinnerParams())
    addView(descriptionField, inputParams())
    addView(amountField, inputParams())
}

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Expense")
            .setView(dialogContent)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add Expense", null)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(surfaceColor))
            val titleId = resources.getIdentifier(
    "alertTitle", "id", "android"
)
dialog.findViewById<TextView>(titleId)?.setTextColor(white)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(white)

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                if (addExpense(descriptionField, amountField, dialogSpinner)) {
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    
    private fun saveIncome(sourceInput: EditText, input: EditText): Boolean {
        val source = sourceInput.text.toString().trim()
            .ifEmpty { "Unspecified source" }
        val value = input.text.toString().replace(",", ".").toDoubleOrNull()

        if (value == null || value < 0) {
            Toast.makeText(
                this, "Please enter a valid income.", Toast.LENGTH_SHORT
            ).show()
            return false
        }

        val now = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)

        income += value
        incomeEntries.add(
            IncomeEntry(
                source = source,
                amount = value,
                date = dateFormat.format(now),
                time = timeFormat.format(now)
            )
        )

        saveData()
        updateTotals()
        Toast.makeText(this, "Income saved.", Toast.LENGTH_SHORT).show()
        return true
    }
    

    private fun addExpense(
        descriptionInput: EditText,
        amountInput: EditText,
        categorySpinner: Spinner
    ): Boolean {
        val description = descriptionInput.text.toString()
            .trim().ifEmpty { "No description" }
        val amount = amountInput.text.toString()
            .replace(",", ".").toDoubleOrNull()
        val category = categorySpinner.selectedItem?.toString() ?: "Other"

        
        if (amount == null || amount <= 0) {
            Toast.makeText(
                this, "Please enter a valid amount.", Toast.LENGTH_SHORT
            ).show()
            return false
        }

        val totalExpenses = expenses.sumOf { it.amount }
        val availableBalance = previousMonthsBalance() + income - totalExpenses
        if (amount > availableBalance + 0.000001) {
            Toast.makeText(
                this,
                "Insufficient balance. You have ${money(availableBalance)} available.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        val now = Calendar.getInstance().time
        
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)

        expenses.add(
            Expense(
                description = description,
                category = category,
                amount = amount,
                date = dateFormat.format(now),
                time = timeFormat.format(now)
            )
        )

        saveData()
        updateTotals()
        refreshExpenses()
        Toast.makeText(this, "Expense added.", Toast.LENGTH_SHORT).show()
        return true
    }


    private fun deleteExpense(index: Int) {
        if (index < 0 || index >= expenses.size) return
        expenses.removeAt(index)
        saveData()
        updateTotals()
        refreshExpenses()
    }

    
    private fun deleteIncome(index: Int) {
        if (index < 0 || index >= incomeEntries.size) return

        val removedIncome = incomeEntries.removeAt(index)
        income -= removedIncome.amount

        saveData()
        updateTotals()
        refreshIncomes()
    }

    
        private fun updateTotals() {
        val totalExpenses = expenses.sumOf { it.amount }
        val availableBalance = previousMonthsBalance() + income
        val remaining = availableBalance - totalExpenses

        monthlyText.text = money(totalExpenses)
        remainingText.text = money(remaining)
        incomeDisplay.text = money(income)

        remainingText.setTextColor(
            if (remaining < 0) Color.rgb(255, 95, 95) else appearanceTheme.accentColor
        )
    }

    private fun previousMonthsBalance(): Double {
        val currentKey = currentMonthKey()
        var balance = 0.0

        val savedMonths = prefs.all.keys.mapNotNull { key ->
            val match = Regex("^(?:income|expenses)_(\\d{4}-\\d{2})$").matchEntire(key)
            match?.groupValues?.get(1)
        }.distinct().filter { it < currentKey }.sorted()

        for (monthKey in savedMonths) {
            val storedIncome = prefs.getString("income_$monthKey", "0")
                ?.toDoubleOrNull() ?: 0.0
            balance += storedIncome

            val storedExpenses = prefs.getString("expenses_$monthKey", null)
            if (!storedExpenses.isNullOrEmpty()) {
                try {
                    val array = JSONArray(storedExpenses)
                    for (index in 0 until array.length()) {
                        balance -= array.optJSONObject(index)?.optDouble("amount", 0.0) ?: 0.0
                    }
                } catch (_: Exception) {
                    // Ignore malformed historical expense data rather than crashing the screen.
                }
            }
        }

        return balance
    }

    private fun currentMonthKey(): String {
        return String.format(Locale.US, "%04d-%02d", selectedYear, selectedMonth + 1)
    }

    private fun incomeKey(): String = "income_${currentMonthKey()}"
    private fun expensesKey(): String = "expenses_${currentMonthKey()}"
    
    
    private fun saveData() {
        val expensesArray = JSONArray()
        for (expense in expenses) {
            val item = JSONObject().apply {
                put("description", expense.description)
                put("category", expense.category)
                put("amount", expense.amount)
                put("date", expense.date)
                put("time", expense.time)
            }
            expensesArray.put(item)
        }

        val incomeEntriesArray = JSONArray()
        for (entry in incomeEntries) {
            val item = JSONObject().apply {
                put("source", entry.source)
                put("amount", entry.amount)
                put("date", entry.date)
                put("time", entry.time)
            }
            incomeEntriesArray.put(item)
        }

        prefs.edit()
            .putString(incomeKey(), income.toString())
            .putString(expensesKey(), expensesArray.toString())
            .putString("incomeEntries_${currentMonthKey()}", incomeEntriesArray.toString())
            .apply()
    }
    

    private fun loadCategories() {
    val storedCategories = prefs.getString(categoriesPreferenceKey, null)
    categories.clear()

    if (storedCategories.isNullOrEmpty()) {
        categories.addAll(defaultCategories)
    } else {
        try {
            val savedArray = JSONArray(storedCategories)
            val savedCategories = mutableListOf<String>()
            for (index in 0 until savedArray.length()) {
                val name = savedArray.optString(index).trim()
                if (name.isNotEmpty() && name != "Other") {
                    savedCategories.add(name)
                }
            }
            categories.addAll(savedCategories.distinct())
        } catch (_: Exception) {
            categories.addAll(defaultCategories.filter { it != "Other" })
        }
    }

    categories.removeAll { it == "Grocery / Household" }

    val foodIndex = categories.indexOf("Food")
    if (foodIndex >= 0) {
        categories.add(foodIndex + 1, "Grocery / Household")
    } else {
        categories.add("Grocery / Household")
    }

    categories.removeAll { it == "Other" }
    categories.add("Other")

    saveCategories()
} 

    
private fun renameSavedExpenseCategories(
    oldName: String,
    newName: String
) {
    val editor = prefs.edit()

    for ((key, value) in prefs.all) {
        if (!key.startsWith("expenses_") ||
            value !is String ||
            value.isEmpty()
        ) {
            continue
        }

        try {
            val array = JSONArray(value)
            var changed = false

            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue

                if (item.optString("category") == oldName) {
                    item.put("category", newName)
                    changed = true
                }
            }

            if (changed) {
                editor.putString(key, array.toString())
            }
        } catch (_: Exception) {
            // Leave unparseable saved data unchanged.
        }
    }

    editor.apply()

    for (index in expenses.indices) {
        if (expenses[index].category == oldName) {
            expenses[index] =
                expenses[index].copy(category = newName)
        }
    }

    saveData()
}

    private fun saveCategories() {
        val savedArray = JSONArray()
        categories.forEach { savedArray.put(it) }
        prefs.edit().putString(categoriesPreferenceKey, savedArray.toString()).apply()
    }

    
    private fun loadData() {
        income = prefs.getString(incomeKey(), "0")?.toDoubleOrNull() ?: 0.0

        incomeEntries.clear()
        val storedIncomeEntries =
            prefs.getString("incomeEntries_${currentMonthKey()}", null)

        if (!storedIncomeEntries.isNullOrEmpty()) {
            try {
                val array = JSONArray(storedIncomeEntries)
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    incomeEntries.add(
                        IncomeEntry(
                            source = item.optString("source", "Unspecified source"),
                            amount = item.optDouble("amount", 0.0),
                            date = item.optString("date", ""),
                            time = item.optString("time", "")
                        )
                    )
                }
            } catch (_: Exception) {
                incomeEntries.clear()
            }
        }

        expenses.clear()
        

        val storedExpenses = prefs.getString(expensesKey(), null)
        if (!storedExpenses.isNullOrEmpty()) {
            try {
                val array = JSONArray(storedExpenses)
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    expenses.add(
                        Expense(
                            description = item.optString("description", "No description"),
                            category = item.optString("category", "Other"),
                            amount = item.optDouble("amount", 0.0),
                            date = item.optString("date", ""),
                            time = item.optString("time", "")
                        )
                    )
                }
            } catch (_: Exception) {
                expenses.clear()
            }
            return
        }

        val migrationDone = prefs.getBoolean("legacy_data_migrated", false)
        if (!migrationDone) {
            val legacyIncome = prefs.getString("income", null)
            val legacyExpenses = prefs.getString("expenses", null)

            if (!legacyIncome.isNullOrEmpty() || !legacyExpenses.isNullOrEmpty()) {
                income = legacyIncome?.toDoubleOrNull() ?: 0.0
                if (!legacyExpenses.isNullOrEmpty()) {
                    try {
                        val array = JSONArray(legacyExpenses)
                        for (index in 0 until array.length()) {
                            val item = array.getJSONObject(index)
                            expenses.add(
                                Expense(
                                    description = item.optString("description", "No description"),
                                    category = item.optString("category", "Other"),
                                    amount = item.optDouble("amount", 0.0),
                                    date = item.optString("date", ""),
                                    time = item.optString("time", "")
                                )
                            )
                        }
                    } catch (_: Exception) {
                        expenses.clear()
                    }
                }
                saveData()
            }
            prefs.edit().putBoolean("legacy_data_migrated", true).apply()
        }
    }
    private fun changeMonth(direction: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, 1)
        calendar.add(Calendar.MONTH, direction)

        selectedYear = calendar.get(Calendar.YEAR)
        selectedMonth = calendar.get(Calendar.MONTH)

        buildInterface()
    }

    private fun updateMonthDisplay() {
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, 1)

        val formatter = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        monthText.text = formatter.format(calendar.time).replaceFirstChar { it.uppercase() }
    }

    

private fun showMonthPicker() {
    val dialogLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(24, 8, 24, 8)
    }

    val yearPicker = NumberPicker(this).apply {
        minValue = 2000
        maxValue = 2100
        value = selectedYear
    }

    val monthPicker = NumberPicker(this).apply {
        minValue = 0
        maxValue = 11
        value = selectedMonth
        displayedValues = arrayOf(
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
        )
    }

    dialogLayout.addView(yearPicker, LinearLayout.LayoutParams(-1, -2))
    dialogLayout.addView(monthPicker, LinearLayout.LayoutParams(-1, -2))

    val dialog = AlertDialog.Builder(this)
        .setTitle("Select month")
        .setView(dialogLayout)
        .setNegativeButton("Cancel", null)
        .setPositiveButton("OK") { _, _ ->
            selectedYear = yearPicker.value
            selectedMonth = monthPicker.value
            buildInterface()
        }
        .create()

    dialog.setOnShowListener {
        dialog.window?.setBackgroundDrawable(ColorDrawable(surfaceColor))
        
        val titleId = resources.getIdentifier("alertTitle", "id", "android")
dialog.findViewById<TextView>(titleId)?.setTextColor(white)

        // Apply picker colors after the dialog has been shown.
        for (picker in listOf(yearPicker, monthPicker)) {
            picker.post {
                fun setTextWhite(view: View) {
                    if (view is TextView) {
                        view.setTextColor(white)
                    }

                    if (view is ViewGroup) {
                        for (i in 0 until view.childCount) {
                            setTextWhite(view.getChildAt(i))
                        }
                    }
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
    picker.setTextColor(white)
} else {
    setTextWhite(picker)
                }

                try {
                    val field = NumberPicker::class.java
                        .getDeclaredField("mSelectorWheelPaint")
                    field.isAccessible = true
                    (field.get(picker) as? Paint)?.color = white
                } catch (_: Exception) {
                    // Keep the picker functional if the field is unavailable.
                }

                picker.invalidate()
            }
        }
    }

    dialog.show()
}

private fun showSearchScreen() {
    isSearchOpen = true

    val screenRoot = FrameLayout(this)

    val wallpaper = ImageView(this).apply {
        setImageResource(R.drawable.moneytrack_background)
        scaleType = ImageView.ScaleType.CENTER_CROP
        contentDescription = null
    }

    screenRoot.addView(
        wallpaper,
        FrameLayout.LayoutParams(-1, -1)
    )

    val content = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(20, 50, 20, 24)
    }

    val monthName = SimpleDateFormat(
        "MMMM yyyy",
        Locale.ENGLISH
    ).format(
        Calendar.getInstance().apply {
            set(selectedYear, selectedMonth, 1)
        }.time
    )

    val header = TextView(this).apply {
        text = "‹  SEARCH"
        textSize = 23f
        setTextColor(white)
        typeface = Typeface.DEFAULT_BOLD
        setPadding(0, 0, 0, 6)
        setOnClickListener {
            isSearchOpen = false
            buildInterface()
        }
    }

    content.addView(
        header,
        LinearLayout.LayoutParams(-1, -2)
    )

    val monthLabel = TextView(this).apply {
        text = monthName
        textSize = 13f
        setTextColor(muted)
        setPadding(2, 0, 0, 18)
    }

    content.addView(
        monthLabel,
        LinearLayout.LayoutParams(-1, -2)
    )

    val searchInput = EditText(this).apply {
        hint = "Search transactions..."
        textSize = 15f
        setTextColor(white)
        setHintTextColor(muted)
        setSingleLine(true)
        inputType = InputType.TYPE_CLASS_TEXT
        setPadding(16, 12, 16, 12)
        background = roundedBackground(
            Color.argb(35, 245, 247, 255),
            12f
        )
    }

    content.addView(
        searchInput,
        LinearLayout.LayoutParams(-1, -2)
    )

    val resultsScroll = ScrollView(this).apply {
        isFillViewport = true
        overScrollMode = ScrollView.OVER_SCROLL_NEVER
    }

    searchResultsContainer = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, 16, 0, 8)
    }

        refreshSearchResults("")

    searchInput.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {}

        override fun onTextChanged(
            s: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
            refreshSearchResults(s?.toString().orEmpty())
        }

        override fun afterTextChanged(s: Editable?) {}
    })

    resultsScroll.addView(
        searchResultsContainer,
        ViewGroup.LayoutParams(-1, -2)
    )

    content.addView(
        resultsScroll,
        LinearLayout.LayoutParams(-1, 0, 1f)
    )

    screenRoot.addView(
        content,
        FrameLayout.LayoutParams(-1, -1)
    )

    setContentView(screenRoot)
}

private fun refreshSearchResults(query: String) {
    searchResultsContainer.removeAllViews()

    val search = query.trim()

    val results = mutableListOf<Triple<Long, Boolean, Int>>()

    expenses.forEachIndexed { index, expense ->
        val matches = search.isEmpty() ||
            expense.description.contains(search, ignoreCase = true) ||
            expense.category.contains(search, ignoreCase = true) ||
            money(expense.amount).contains(search, ignoreCase = true) ||
            expense.amount.toString().contains(search, ignoreCase = true)

        if (matches) {
            val timestamp = runCatching {
                SimpleDateFormat(
                    "dd MMM yyyy HH:mm",
                    Locale.ENGLISH
                ).parse("${expense.date} ${expense.time}")?.time
            }.getOrNull() ?: 0L

            results.add(Triple(timestamp, false, index))
        }
    }

    incomeEntries.forEachIndexed { index, entry ->
        val matches = search.isEmpty() ||
            entry.source.contains(search, ignoreCase = true) ||
            money(entry.amount).contains(search, ignoreCase = true) ||
            entry.amount.toString().contains(search, ignoreCase = true)

        if (matches) {
            val timestamp = runCatching {
                SimpleDateFormat(
                    "dd MMM yyyy HH:mm",
                    Locale.ENGLISH
                ).parse("${entry.date} ${entry.time}")?.time
            }.getOrNull() ?: 0L

            results.add(Triple(timestamp, true, index))
        }
    }

    if (results.isEmpty()) {
        val emptyText = TextView(this).apply {
            text = if (search.isEmpty()) {
                "No transactions this month"
            } else {
                "No matching transactions"
            }
            textSize = 14f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(16, 32, 16, 32)
        }

        searchResultsContainer.addView(
            emptyText,
            LinearLayout.LayoutParams(-1, -2)
        )
        return
    }

    for ((_, isIncome, index) in results.sortedByDescending { it.first }) {
        val title: String
        val subtitle: String
        val date: String
        val amount: Double

        if (isIncome) {
            val entry = incomeEntries[index]
            title = entry.source
            subtitle = "INCOME"
            date = listOf(entry.date, entry.time)
                .filter { it.isNotEmpty() }
                .joinToString(" • ")
            amount = entry.amount
        } else {
            val expense = expenses[index]
            title = expense.description
            subtitle = expense.category
            date = listOf(expense.date, expense.time)
                .filter { it.isNotEmpty() }
                .joinToString(" • ")
            amount = expense.amount
        }

        val density = resources.displayMetrics.density

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 15, 12, 15)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f * density
                setColor(Color.argb(15, 245, 247, 255))
                setStroke(
                    density.toInt().coerceAtLeast(1),
                    Color.argb(110, 220, 225, 235)
                )
            }
            elevation = 8f * density
        }

        val accent = View(this).apply {
            setBackgroundColor(
                if (isIncome) appearanceTheme.accentColor
                else Color.rgb(255, 95, 95)
            )
        }
        card.addView(accent, LinearLayout.LayoutParams(3, 48))

        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 0, 8, 0)
        }

        val titleText = TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
        }

        val subtitleText = TextView(this).apply {
            text = subtitle
            textSize = 12f
            setTextColor(white)
            setPadding(0, 4, 0, 0)
        }

        details.addView(titleText)
        details.addView(subtitleText)

        if (date.isNotEmpty()) {
            val dateText = TextView(this).apply {
                text = date
                textSize = 11f
                setTextColor(muted)
                setPadding(0, 4, 0, 0)
            }
            details.addView(dateText)
        }

        card.addView(
            details,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val amountText = TextView(this).apply {
            text = "${if (isIncome) "+" else "−"}${money(amount)}"
            textSize = 15f
            setTextColor(
                if (isIncome) appearanceTheme.accentColor
                else Color.rgb(255, 95, 95)
            )
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.END
        }

        card.addView(amountText)

        val params = LinearLayout.LayoutParams(-1, -2)
        params.setMargins(0, 0, 0, 10)
        searchResultsContainer.addView(card, params)
    }
}


private fun refreshExpenses() {
        expensesContainer.removeAllViews()

        if (expenses.isEmpty()) {
            val emptyCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(20, 22, 20, 22)
                background = roundedBackground(cardColor, 16f)
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
                LinearLayout.LayoutParams(-1, -2)
            )
            return
        }

        for (index in expenses.indices.reversed()) {
            val expense = expenses[index]

            val expenseCard = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16, 15, 12, 15)
                val density = resources.displayMetrics.density

background = GradientDrawable().apply {
    shape = GradientDrawable.RECTANGLE
    cornerRadius = 16f * density

    // Same glass color and transparency
    setColor(Color.argb(15, 245, 247, 255))

    // Same semi-transparent border
    setStroke(
        density.toInt().coerceAtLeast(1),
        Color.argb(110, 220, 225, 235)
    )
}

// Same raised glass effect
elevation = 8f * density
            }

            val accent = View(this).apply {
                setBackgroundColor(appearanceTheme.accentColor)
            }
            expenseCard.addView(accent, LinearLayout.LayoutParams(3, 48))

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
                setTextColor(white)
                setPadding(0, 4, 0, 0)
            }

            details.addView(description)
            details.addView(category)

            if (expense.date.isNotEmpty() || expense.time.isNotEmpty()) {
                val timestamp = TextView(this).apply {
                    text = listOf(expense.date, expense.time).filter { it.isNotEmpty() }.joinToString(" • ")
                    textSize = 11f
                    setTextColor(muted)
                    setPadding(0, 4, 0, 0)
                }
                details.addView(timestamp)
            }

            expenseCard.addView(
                details,
                LinearLayout.LayoutParams(0, -2, 1f)
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
            expenseCard.addView(rightColumn, LinearLayout.LayoutParams(-2, -2))

            val params = LinearLayout.LayoutParams(-1, -2)
            params.setMargins(0, 0, 0, 10)

            val deleteWidth = (92 * resources.displayMetrics.density).toInt()

            val swipeLayout = SwipeDeleteLayout(this).apply {
                clipChildren = true
                clipToPadding = true
            }

            swipeLayout.addView(
                TextView(this).apply {
                    text = "Delete"
                    textSize = 13f
                    setTextColor(white)
                    gravity = Gravity.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                    background = roundedBackground(Color.rgb(190, 50, 55), 16f)
                    setOnClickListener { deleteExpense(index) }
                },
                FrameLayout.LayoutParams(
                    deleteWidth,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.END
                )
            )

            val expenseWrapper = FrameLayout(this).apply {
    clipToOutline = true
    background = roundedBackground(Color.TRANSPARENT, 16f)
}

val expenseBlur = BlurView(this).apply {
    setupWith(wallpaperBlurTarget, 4f, false) 
        .setBlurRadius(3f)
        .setOverlayColor(Color.TRANSPARENT)
}

expenseWrapper.addView(
    expenseBlur,
    FrameLayout.LayoutParams(-1, -1)
)

expenseWrapper.addView(
    expenseCard,
    FrameLayout.LayoutParams(-1, -2)
)

swipeLayout.addView(
    expenseWrapper,
    FrameLayout.LayoutParams(-1, -2)
)

            
            
            swipeLayout.setDeleteWidth(deleteWidth)
            swipeLayout.setOnSwipeDelete {
                deleteExpense(index)
            }
            expensesContainer.addView(swipeLayout, params)
        }
    }

    
    private fun refreshIncomes() {
        expensesContainer.removeAllViews()

        if (incomeEntries.isEmpty()) {
            val emptyCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(20, 22, 20, 22)
                background = roundedBackground(cardColor, 16f)
            }

            val emptyTitle = TextView(this).apply {
                text = "No incomes this month"
                textSize = 15f
                setTextColor(white)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
            }

            val emptySubtitle = TextView(this).apply {
                text = "Your income records will appear here"
                textSize = 12f
                setTextColor(muted)
                gravity = Gravity.CENTER
                setPadding(0, 5, 0, 0)
            }

            emptyCard.addView(emptyTitle)
            emptyCard.addView(emptySubtitle)
            expensesContainer.addView(
                emptyCard,
                LinearLayout.LayoutParams(-1, -2)
            )
            return
        }

        for (index in incomeEntries.indices.reversed()) {
            val entry = incomeEntries[index]
            val density = resources.displayMetrics.density

            val incomeCard = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16, 15, 12, 15)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 16f * density
                    setColor(Color.argb(15, 245, 247, 255))
                    setStroke(
                        density.toInt().coerceAtLeast(1),
                        Color.argb(110, 220, 225, 235)
                    )
                }
                elevation = 8f * density
            }

            val accent = View(this).apply {
                setBackgroundColor(appearanceTheme.accentColor)
            }
            incomeCard.addView(accent, LinearLayout.LayoutParams(3, 48))

            val details = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12, 0, 8, 0)
            }

            val sourceText = TextView(this).apply {
                text = entry.source
                textSize = 16f
                setTextColor(white)
                typeface = Typeface.DEFAULT_BOLD
            }
            details.addView(sourceText)

            if (entry.date.isNotEmpty() || entry.time.isNotEmpty()) {
                val timestamp = TextView(this).apply {
                    text = listOf(entry.date, entry.time)
                        .filter { it.isNotEmpty() }
                        .joinToString(" • ")
                    textSize = 11f
                    setTextColor(muted)
                    setPadding(0, 4, 0, 0)
                }
                details.addView(timestamp)
            }

            incomeCard.addView(
                details,
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            val amountText = TextView(this).apply {
                text = money(entry.amount)
                textSize = 16f
                setTextColor(white)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.END
            }
            incomeCard.addView(amountText, LinearLayout.LayoutParams(-2, -2))

            val params = LinearLayout.LayoutParams(-1, -2)
            params.setMargins(0, 0, 0, 10)

            val deleteWidth = (92 * density).toInt()
            val swipeLayout = SwipeDeleteLayout(this).apply {
                clipChildren = true
                clipToPadding = true
            }

            swipeLayout.addView(
                TextView(this).apply {
                    text = "Delete"
                    textSize = 13f
                    setTextColor(white)
                    gravity = Gravity.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                    background = roundedBackground(Color.rgb(190, 50, 55), 16f)
                    setOnClickListener { deleteIncome(index) }
                },
                FrameLayout.LayoutParams(
                    deleteWidth,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.END
                )
            )

            val incomeWrapper = FrameLayout(this).apply {
                clipToOutline = true
                background = roundedBackground(Color.TRANSPARENT, 16f)
            }

            val incomeBlur = BlurView(this).apply {
                setupWith(wallpaperBlurTarget, 4f, false)
                    .setBlurRadius(3f)
                    .setOverlayColor(Color.TRANSPARENT)
            }

            incomeWrapper.addView(
                incomeBlur,
                FrameLayout.LayoutParams(-1, -1)
            )
            incomeWrapper.addView(
                incomeCard,
                FrameLayout.LayoutParams(-1, -2)
            )
            swipeLayout.addView(
                incomeWrapper,
                FrameLayout.LayoutParams(-1, -2)
            )

            swipeLayout.setDeleteWidth(deleteWidth)
            swipeLayout.setOnSwipeDelete {
                deleteIncome(index)
            }
            expensesContainer.addView(swipeLayout, params)
        }
    }
    

    private inner class SwipeDeleteLayout(context: Context) : FrameLayout(context) {
        
        private var downX = 0f
        private var downY = 0f
        private var deleteWidth = 0
        private var dragging = false
        private var onSwipeDelete: (() -> Unit)? = null

        fun setDeleteWidth(width: Int) {
            deleteWidth = width

            post {
                getChildAt(0)?.translationX = deleteWidth.toFloat()
            }
        }

        fun setOnSwipeDelete(action: () -> Unit) {
            onSwipeDelete = action
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    downY = event.y
                    dragging = false
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - downX
                    val dy = event.y - downY

                    if (!dragging &&
                        kotlin.math.abs(dx) > 12 &&
                        kotlin.math.abs(dx) > kotlin.math.abs(dy)
                    ) {
                        dragging = true
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }

                    if (dragging) {
                        val translation = dx.coerceIn(
                            -deleteWidth * 2f,
                            0f
                        )

                        getChildAt(0)?.translationX =
                            deleteWidth + translation

                        getChildAt(1)?.translationX = translation
                    }

                    return true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (dragging) {
                        parent?.requestDisallowInterceptTouchEvent(false)

                        val dx = event.x - downX
                        val button = getChildAt(0)
                        val card = getChildAt(1)

                        when {
                            event.actionMasked == MotionEvent.ACTION_UP &&
                                dx <= -deleteWidth * 1.5f -> {
                                card?.animate()
                                    ?.translationX(-deleteWidth * 2f)
                                    ?.setDuration(140)
                                    ?.withEndAction {
                                        onSwipeDelete?.invoke()
                                    }
                                    ?.start()
                            }

                            event.actionMasked == MotionEvent.ACTION_UP &&
                                dx <= -deleteWidth * 0.45f -> {
                                button?.animate()
                                    ?.translationX(0f)
                                    ?.setDuration(180)
                                    ?.setInterpolator(AccelerateDecelerateInterpolator())
                                    ?.start()

                                card?.animate()
                                    ?.translationX(-deleteWidth.toFloat())
                                    ?.setDuration(180)
                                    ?.setInterpolator(AccelerateDecelerateInterpolator())
                                    ?.start()
                            }

                            else -> {
                                button?.animate()
                                    ?.translationX(deleteWidth.toFloat())
                                    ?.setDuration(180)
                                    ?.setInterpolator(AccelerateDecelerateInterpolator())
                                    ?.start()

                                card?.animate()
                                    ?.translationX(0f)
                                    ?.setDuration(180)
                                    ?.setInterpolator(AccelerateDecelerateInterpolator())
                                    ?.start()
                            }
                        }
                    }

                    dragging = false
                    return true
                }
            }

            return true
        }
    }
    

    private fun sectionTitle(text: String, color: Int): TextView = TextView(this).apply {
        this.text = text
        textSize = 17f
        setTextColor(color)
        typeface = Typeface.DEFAULT_BOLD
        letterSpacing = 0.04f
    }

    private fun smallLabel(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 13f
        setTextColor(secondary)
    }

    
private fun createSmallCard(): LinearLayout = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL
    setPadding(16, 16, 16, 16)

    val density = resources.displayMetrics.density

    background = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 16f * density

        // Same translucent gray glass as the month menu
        setColor(Color.argb(15, 245, 247, 255))

        // Same thin, semi-transparent outline
        setStroke(
            density.toInt().coerceAtLeast(1),
            Color.argb(110, 220, 225, 235)
        )
    }

    // Same raised 3D effect
    elevation = 8f * density
}


    private fun createMainCard(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(16, 16, 16, 16)
        background = gradientBackground(Color.rgb(18, 20, 27), Color.rgb(24, 31, 45), 16f)
    }

    private fun createInput(hint: String, inputType: Int): EditText = EditText(this).apply {
        this.hint = hint
        this.inputType = inputType
        textSize = 15f
        setTextColor(white)
        setHintTextColor(muted)
        setPadding(14, 12, 14, 12)
        background = roundedBackground(surfaceColor, 12f)
    }

    private fun createGradientButton(text: String, startColor: Int, endColor: Int): TextView = TextView(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(Color.WHITE)
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        setPadding(16, 13, 16, 13)
        background = gradientBackground(startColor, endColor, 12f)
    }

    private fun createNavigationButton(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 31f
        setTextColor(blueBright)
        gravity = Gravity.CENTER
        typeface = Typeface.DEFAULT
        setPadding(4, 0, 4, 2)
        background = roundedBackground(Color.rgb(24, 35, 54), 12f)
    }

    private fun roundedBackground(color: Int, radius: Float): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius
    }

    private fun gradientBackground(startColor: Int, endColor: Int, radius: Float): GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        intArrayOf(startColor, endColor)
    ).apply {
        cornerRadius = radius
    }

        private fun createLiquidGlassButton(text: String): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(backgroundColor)
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            minHeight = (42 * resources.displayMetrics.density).toInt()
            setPadding(16, 0, 16, 0)

            background = createLiquidGlassBackground(
                AppearanceManager.getAccentColor(this@MainActivity),
                true,
                14
            )
        }

    private fun createLiquidGlassBackground(
        color: Int,
        selected: Boolean,
        radiusDp: Int
    ): LayerDrawable {
        val density = resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()

        val radius = dp(radiusDp).toFloat()

        val surface = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.argb(
                    if (selected) 245 else 205,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                ),
                Color.argb(
                    if (selected) 220 else 175,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
            )
        ).apply {
            cornerRadius = radius
            setStroke(
                dp(1),
                Color.argb(
                    if (selected) 235 else 145,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
            )
        }

        val depth = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.TRANSPARENT,
                Color.argb(35, 0, 0, 0),
                Color.argb(if (selected) 95 else 115, 0, 0, 0)
            )
        ).apply {
            cornerRadius = radius
        }

        val reflection = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.argb(if (selected) 80 else 55, 255, 255, 255),
                Color.argb(25, 255, 255, 255),
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        ).apply {
            cornerRadius = radius
        }

        val rim = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.argb(if (selected) 110 else 65, 255, 255, 255),
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                Color.argb(65, 0, 0, 0)
            )
        ).apply {
            cornerRadius = radius
            setStroke(
                dp(1),
                Color.argb(if (selected) 75 else 40, 255, 255, 255)
            )
        }

        return LayerDrawable(
            arrayOf(surface, depth, reflection, rim)
        ).apply {
            setLayerInset(1, dp(1), dp(1), dp(1), dp(1))
            setLayerInset(2, dp(1), dp(1), dp(1), dp(1))
            setLayerInset(3, dp(1), dp(1), dp(1), dp(1))
        }
    }

    private fun inputParams(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(-1, -2).apply {
        setMargins(0, 0, 0, 10)
    }

    private fun spinnerParams(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(-1, -2).apply {
        setMargins(0, 0, 0, 10)
    }

    private fun buttonParams(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(-1, -2).apply {
        setMargins(0, 4, 0, 0)
    }

    private fun weightParams(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(0, -2, 1f)

    private fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale.GERMANY).format(value)

    private fun exportBackup() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "MoneyTrack_Backup.json")
        }
        startActivityForResult(intent, REQUEST_EXPORT_BACKUP)
    }

    private fun importBackup() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        startActivityForResult(intent, REQUEST_IMPORT_BACKUP)
    }

    private fun createBackupJson(): JSONObject {
        val root = JSONObject()
        root.put("format", "MoneyTrack Backup")
        root.put("version", 1)

        val data = JSONObject()
        for ((key, value) in prefs.all) {
            when (value) {
                is String -> data.put(key, value)
                is Boolean -> data.put(key, value)
                is Int -> data.put(key, value)
                is Long -> data.put(key, value)
                is Float -> data.put(key, value)
                is Double -> data.put(key, value)
            }
        }
        root.put("data", data)
        return root
    }

    private fun restoreBackupJson(jsonText: String) {
        try {
            val root = JSONObject(jsonText)
            if (root.optString("format") != "MoneyTrack Backup") {
                Toast.makeText(this, "Invalid MoneyTrack backup.", Toast.LENGTH_SHORT).show()
                return
            }
            if (root.optInt("version", 0) != 1) {
                Toast.makeText(this, "Unsupported backup version.", Toast.LENGTH_SHORT).show()
                return
            }
            val data = root.optJSONObject("data") ?: run {
                Toast.makeText(this, "Backup contains no data.", Toast.LENGTH_SHORT).show()
                return
            }

            val editor = prefs.edit()
            editor.clear()
            val keys = data.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                when (val value = data.get(key)) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Double -> editor.putFloat(key, value.toFloat())
                }
            }
            editor.apply()

            loadData()
            updateMonthDisplay()
            updateTotals()
            refreshExpenses()
            Toast.makeText(this, "Backup imported successfully.", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Could not import backup.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data?.data == null) return
        val uri = data.data ?: return

        when (requestCode) {
            REQUEST_EXPORT_BACKUP -> {
                try {
                    val backup = createBackupJson()
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(backup.toString(2).toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(this, "Backup exported successfully.", Toast.LENGTH_LONG).show()
                } catch (_: Exception) {
                    Toast.makeText(this, "Could not export backup.", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_IMPORT_BACKUP -> {
                try {
                    val jsonText = contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader().use { it.readText() }
                    }
                    if (jsonText.isNullOrEmpty()) {
                        Toast.makeText(this, "Backup file is empty.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    restoreBackupJson(jsonText)
                } catch (_: Exception) {
                    Toast.makeText(this, "Could not read backup.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
