package com.moneytrack.app

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduledActivity : Activity() {

    private val white = Color.WHITE
    private val muted = Color.rgb(160, 174, 194)
    private val backgroundColor = Color.rgb(8, 13, 20)
    private val cardColor = Color.rgb(17, 25, 36)
    private val inputColor = Color.rgb(12, 24, 42)
    private val blue = Color.rgb(59, 130, 246)
    private val progressBackground = Color.rgb(38, 54, 77)

    private val prefsName = "moneytrack_data"
    private val scheduledKey = "scheduled_items"

    private lateinit var listContainer: LinearLayout

    private val dateFormat = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.US
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildScreen()
        loadItems()
    }

    override fun onResume() {
        super.onResume()

        if (::listContainer.isInitialized) {
            loadItems()
        }
    }

    private fun buildScreen() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 92, 24, 24)
            setBackgroundColor(backgroundColor)
        }

        val topRow = LinearLayout(this).apply {
    orientation = LinearLayout.HORIZONTAL
    gravity = Gravity.CENTER_VERTICAL
}

val backButton = TextView(this).apply {
    text = "‹"
    textSize = 36f
    setTextColor(white)
    gravity = Gravity.CENTER
    isClickable = true
    isFocusable = true

    setOnClickListener {
        finish()
    }
}

topRow.addView(
    backButton,
    LinearLayout.LayoutParams(
        dpToPx(48),
        dpToPx(48)
    )
)

val title = TextView(this).apply {
    text = "SCHEDULED"
    textSize = 24f
    setTextColor(white)
    typeface = Typeface.DEFAULT_BOLD
    letterSpacing = 0.04f
}

topRow.addView(
    title,
    LinearLayout.LayoutParams(
        0,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        1f
    )
)

root.addView(
    topRow,
    LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        bottomMargin = 6
    }
)

        val subtitle = TextView(this).apply {
            text = "Keep track of important future dates"
            textSize = 14f
            setTextColor(muted)
        }

        root.addView(
            subtitle,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 24
            }
        )

        listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        root.addView(
            listContainer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val addButton = TextView(this).apply {
            text = "+ ADD"
            textSize = 16f
            setTextColor(blue)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 18, 0, 18)

            setOnClickListener {
                showAddDialog()
            }
        }

        root.addView(
            addButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(root)
    }

    private fun showAddDialog() {

        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 12)
        }

        val nameLabel = TextView(this).apply {
            text = "NAME"
            textSize = 11f
            setTextColor(muted)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }

        dialogLayout.addView(nameLabel)

        val nameInput = EditText(this).apply {
            hint = "Salary"
            textSize = 16f
            setTextColor(white)
            setHintTextColor(muted)
            setSingleLine(true)
            background = roundedBackground(
                inputColor,
                12f
            )
            setPadding(14, 0, 14, 0)
        }

        dialogLayout.addView(
            nameInput,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                52
            ).apply {
                topMargin = 8
                bottomMargin = 18
            }
        )

        val dateLabel = TextView(this).apply {
            text = "DATE"
            textSize = 11f
            setTextColor(muted)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }

        dialogLayout.addView(dateLabel)

        val dateInput = EditText(this).apply {
            hint = "16/08/2027"
            textSize = 16f
            setTextColor(white)
            setHintTextColor(muted)
            setSingleLine(true)
            isFocusable = false
            isClickable = true
            background = roundedBackground(
                inputColor,
                12f
            )
            setPadding(14, 0, 14, 0)

            setOnClickListener {
                showDatePicker(this)
            }
        }

        dialogLayout.addView(
            dateInput,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                52
            ).apply {
                topMargin = 8
            }
        )

        val dialog = AlertDialog.Builder(this)
            .setTitle("ADD SCHEDULED")
            .setView(dialogLayout)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("ADD", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val name = nameInput.text
                    .toString()
                    .trim()

                val date = dateInput.text
                    .toString()
                    .trim()

                if (name.isEmpty() || date.isEmpty()) {
                    return@setOnClickListener
                }

                if (!isValidDate(date)) {
                    return@setOnClickListener
                }

                addItem(name, date)

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showEditDialog(
        index: Int,
        currentName: String,
        currentDate: String
    ) {

        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 12)
        }

        val nameLabel = TextView(this).apply {
            text = "NAME"
            textSize = 11f
            setTextColor(muted)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }

        dialogLayout.addView(nameLabel)

        val nameInput = EditText(this).apply {
            setText(currentName)
            textSize = 16f
            setTextColor(white)
            setHintTextColor(muted)
            setSingleLine(true)
            background = roundedBackground(
                inputColor,
                12f
            )
            setPadding(14, 0, 14, 0)
        }

        dialogLayout.addView(
            nameInput,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                52
            ).apply {
                topMargin = 8
                bottomMargin = 18
            }
        )

        val dateLabel = TextView(this).apply {
            text = "DATE"
            textSize = 11f
            setTextColor(muted)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }

        dialogLayout.addView(dateLabel)

        val dateInput = EditText(this).apply {
            setText(currentDate)
            textSize = 16f
            setTextColor(white)
            setHintTextColor(muted)
            setSingleLine(true)
            isFocusable = false
            isClickable = true
            background = roundedBackground(
                inputColor,
                12f
            )
            setPadding(14, 0, 14, 0)

            setOnClickListener {
                showDatePicker(this)
            }
        }

        dialogLayout.addView(
            dateInput,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                52
            ).apply {
                topMargin = 8
            }
        )

        val dialog = AlertDialog.Builder(this)
            .setTitle("EDIT SCHEDULED")
            .setView(dialogLayout)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("SAVE", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val name = nameInput.text
                    .toString()
                    .trim()

                val date = dateInput.text
                    .toString()
                    .trim()

                if (name.isEmpty() || date.isEmpty()) {
                    return@setOnClickListener
                }

                if (!isValidDate(date)) {
                    return@setOnClickListener
                }

                updateItem(
                    index,
                    name,
                    date
                )

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showDatePicker(
        input: EditText
    ) {

        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->

                input.setText(
                    String.format(
                        Locale.US,
                        "%02d/%02d/%04d",
                        dayOfMonth,
                        month + 1,
                        year
                    )
                )

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun addItem(
        name: String,
        date: String
    ) {

        val current = getStoredItems()
            .toMutableList()

        val createdDate =
    dateFormat.format(
        Calendar.getInstance().time
    )

current.add(
    "$name|$date|$createdDate"
)

        saveItems(current)
        loadItems()
    }

    private fun updateItem(
        index: Int,
        name: String,
        date: String
    ) {

        val items = getStoredItems()
            .toMutableList()

        if (index < 0 || index >= items.size) {
            return
        }

        items[index] = "$name|$date"

        saveItems(items)
        loadItems()
    }

    private fun loadItems() {

        listContainer.removeAllViews()

        val items = getStoredItems()

        for ((index, item) in items.withIndex()) {

            val parts = item.split("|")

if (parts.size != 3) {
    continue
}

addScheduledItem(
    name = parts[0],
    date = parts[1],
    createdDate = parts[2],
    index = index
)
        }
    }

    private fun addScheduledItem(
    name: String,
    date: String,
    createdDate: String,
    index: Int
) {

        val itemContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dpToPx(16),
                dpToPx(16),
                dpToPx(16),
                dpToPx(16)
            )
            background = roundedBackground(
                cardColor,
                16f
            )

            setOnClickListener {
                showEditDialog(
                    index,
                    name,
                    date
                )
            }
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val nameText = TextView(this).apply {
            text = name
            textSize = 16f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        topRow.addView(
            nameText,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val dateText = TextView(this).apply {
            text = date
            textSize = 13f
            setTextColor(muted)
            gravity = Gravity.CENTER
            maxLines = 1
        }

        topRow.addView(
            dateText,
            LinearLayout.LayoutParams(
                dpToPx(88),
                dpToPx(32)
            )
        )

        val deleteButton = TextView(this).apply {
            text = "×"
            textSize = 24f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true

            setOnClickListener { view ->

                view.isPressed = true

                deleteItem(index)
            }
        }

        topRow.addView(
            deleteButton,
            LinearLayout.LayoutParams(
                dpToPx(40),
                dpToPx(40)
            )
        )

        itemContainer.addView(
            topRow,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(40)
            )
        )

        val remainingDays = calculateRemainingDays(date)
val progressValue = calculateProgress(
    createdDate,
    date
)

        val progressRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val progressView = ScheduledProgressView(
            this,
            progressValue,
            blue,
            progressBackground
        )

        progressRow.addView(
            progressView,
            LinearLayout.LayoutParams(
                0,
                dpToPx(16),
                1f
            )
        )

        val daysText = TextView(this).apply {
            text = if (remainingDays == 0L) {
                "0 days"
            } else {
                "$remainingDays days"
            }

            textSize = 13f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            includeFontPadding = false
        }

        progressRow.addView(
            daysText,
            LinearLayout.LayoutParams(
                dpToPx(72),
                dpToPx(20)
            ).apply {
                marginStart = dpToPx(12)
            }
        )

        itemContainer.addView(
            progressRow,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(20)
            ).apply {
                topMargin = dpToPx(14)
            }
        )

        listContainer.addView(
            itemContainer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(10)
            }
        )
    }

        private fun calculateRemainingDays(
        dateString: String
    ): Long {

        return try {

            dateFormat.isLenient = false

            val targetDate =
                dateFormat.parse(dateString)
                    ?: return 0L

            val today = Calendar.getInstance().apply {

                set(
                    Calendar.HOUR_OF_DAY,
                    0
                )

                set(
                    Calendar.MINUTE,
                    0
                )

                set(
                    Calendar.SECOND,
                    0
                )

                set(
                    Calendar.MILLISECOND,
                    0
                )
            }

            val target = Calendar.getInstance().apply {

                time = targetDate

                set(
                    Calendar.HOUR_OF_DAY,
                    0
                )

                set(
                    Calendar.MINUTE,
                    0
                )

                set(
                    Calendar.SECOND,
                    0
                )

                set(
                    Calendar.MILLISECOND,
                    0
                )
            }

            val difference =
                target.timeInMillis -
                    today.timeInMillis

            if (difference <= 0L) {
                0L
            } else {
                difference /
                    (24L * 60L * 60L * 1000L)
            }

        } catch (e: Exception) {
            0L
        }
    }

    private fun calculateProgress(
    createdDateString: String,
    targetDateString: String
): Float {

    return try {

        dateFormat.isLenient = false

        val createdDate =
            dateFormat.parse(createdDateString)
                ?: return 0f

        val targetDate =
            dateFormat.parse(targetDateString)
                ?: return 0f

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val created = Calendar.getInstance().apply {
            time = createdDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val target = Calendar.getInstance().apply {
            time = targetDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val totalDays =
            (
                target.timeInMillis -
                    created.timeInMillis
            ) / (24L * 60L * 60L * 1000L)

        val elapsedDays =
            (
                today.timeInMillis -
                    created.timeInMillis
            ) / (24L * 60L * 60L * 1000L)

        if (totalDays <= 0L) {
            return 1f
        }

        (
            elapsedDays.toFloat() /
                totalDays.toFloat()
        ).coerceIn(0f, 1f)

    } catch (e: Exception) {
        0f
    }
}

    private fun deleteItem(
        index: Int
    ) {

        val items = getStoredItems()
            .toMutableList()

        if (index < 0 || index >= items.size) {
            return
        }

        items.removeAt(index)

        saveItems(items)
        loadItems()
    }

    private fun getStoredItems(): List<String> {

        val value = getSharedPreferences(
            prefsName,
            MODE_PRIVATE
        ).getString(
            scheduledKey,
            ""
        ) ?: ""

        if (value.isEmpty()) {
            return emptyList()
        }

        return value.split(";;")
    }

    private fun saveItems(
        items: List<String>
    ) {

        getSharedPreferences(
            prefsName,
            MODE_PRIVATE
        ).edit()
            .putString(
                scheduledKey,
                items.joinToString(";;")
            )
            .apply()
    }

    private fun isValidDate(
        date: String
    ): Boolean {

        return try {

            dateFormat.isLenient = false
            dateFormat.parse(date)

            true

        } catch (e: Exception) {
            false
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

    private fun dpToPx(
        dp: Int
    ): Int {

        return (
            dp *
                resources.displayMetrics.density
            ).toInt()
    }

    private class ScheduledProgressView(
        context: Context,
        private val progress: Float,
        fillColor: Int,
        trackColor: Int
    ) : View(context) {

        private val density =
            resources.displayMetrics.density

        private val trackPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = trackColor
                style = Paint.Style.FILL
            }

        private val glowPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(
                    0,
                    229,
                    255
                )
                alpha = 150
                style = Paint.Style.FILL

                setShadowLayer(
                    9f * density,
                    0f,
                    0f,
                    Color.rgb(
                        0,
                        229,
                        255
                    )
                )
            }

        private val fillPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
            }

        init {
            setLayerType(
                View.LAYER_TYPE_SOFTWARE,
                null
            )
        }

        override fun onDraw(
            canvas: Canvas
        ) {

            super.onDraw(canvas)

            val width = width.toFloat()
            val height = height.toFloat()
            val radius = height / 2f

            val safeProgress =
                progress.coerceIn(
                    0f,
                    1f
                )

            canvas.drawRoundRect(
                0f,
                0f,
                width,
                height,
                radius,
                radius,
                trackPaint
            )

            if (safeProgress <= 0f) {
                return
            }

            val fillWidth =
                width * safeProgress

            canvas.save()

            val fillPath =
                android.graphics.Path().apply {
                    addRoundRect(
                        0f,
                        0f,
                        fillWidth,
                        height,
                        radius,
                        radius,
                        android.graphics.Path.Direction.CW
                    )
                }

            canvas.clipPath(fillPath)

            canvas.drawRoundRect(
                0f,
                0f,
                width,
                height,
                radius,
                radius,
                glowPaint
            )

            fillPaint.shader = LinearGradient(
                0f,
                0f,
                width,
                0f,
                Color.rgb(
                    0,
                    229,
                    255
                ),
                Color.rgb(
                    33,
                    150,
                    243
                ),
                Shader.TileMode.CLAMP
            )

            canvas.drawRoundRect(
                0f,
                0f,
                width,
                height,
                radius,
                radius,
                fillPaint
            )

            fillPaint.shader = null

            canvas.restore()
        }
    }
}
    
