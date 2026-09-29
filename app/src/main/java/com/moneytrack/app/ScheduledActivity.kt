package com.moneytrack.app

import android.app.Activity
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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

        val title = TextView(this).apply {
            text = "SCHEDULED"
            textSize = 24f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.04f
        }

        root.addView(
            title,
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
            background = roundedBackground(inputColor, 12f)
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
            background = roundedBackground(inputColor, 12f)
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

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("ADD SCHEDULED")
            .setView(dialogLayout)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("ADD", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(
                android.app.AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val name = nameInput.text.toString().trim()
                val date = dateInput.text.toString().trim()

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

    private fun showDatePicker(input: EditText) {

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

        val current = getStoredItems().toMutableList()

        current.add(
            "$name|$date"
        )

        saveItems(current)
        loadItems()
    }

    private fun loadItems() {

        listContainer.removeAllViews()

        val items = getStoredItems()

        for ((index, item) in items.withIndex()) {

            val parts = item.split("|")

            if (parts.size != 2) {
                continue
            }

            val name = parts[0]
            val date = parts[1]

            addScheduledItem(
                name = name,
                date = date,
                index = index
            )
        }
    }

    private fun addScheduledItem(
        name: String,
        date: String,
        index: Int
    ) {

        val itemContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 14)
            background = roundedBackground(cardColor, 16f)
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
        }

        topRow.addView(
            dateText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 12
            }
        )

        val deleteButton = TextView(this).apply {
            text = "×"
            textSize = 22f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(8, 0, 4, 0)

            setOnClickListener {
                deleteItem(index)
            }
        }

        topRow.addView(
            deleteButton,
            LinearLayout.LayoutParams(
                32,
                32
            )
        )

        itemContainer.addView(
            topRow,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val remainingDays = calculateRemainingDays(date)

        val progressRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val progressBackgroundView = View(this).apply {
            background = roundedBackground(
                progressBackground,
                50f
            )
        }

        val progressContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        progressContainer.addView(
            progressBackgroundView,
            LinearLayout.LayoutParams(
                0,
                12,
                1f
            )
        )

        val daysText = TextView(this).apply {
            text = if (remainingDays == 0L) {
                "0 days"
            } else {
                "$remainingDays days"
            }

            textSize = 12f
            setTextColor(muted)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(10, 0, 0, 0)
        }

        progressRow.addView(
            progressContainer,
            LinearLayout.LayoutParams(
                0,
                20,
                1f
            )
        )

        progressRow.addView(
            daysText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                20
            )
        )

        itemContainer.addView(
            progressRow,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                20
            ).apply {
                topMargin = 10
            }
        )

        itemContainer.addView(
            View(this),
            LinearLayout.LayoutParams(
                1,
                4
            )
        )

        val progressFill = View(this).apply {
            background = roundedBackground(
                blue,
                50f
            )
        }

        val progressValue = calculateProgress(remainingDays)

        progressContainer.removeAllViews()

        progressContainer.addView(
            progressFill,
            LinearLayout.LayoutParams(
                0,
                12,
                progressValue
            )
        )

        progressContainer.addView(
            View(this),
            LinearLayout.LayoutParams(
                0,
                12,
                1f - progressValue
            )
        )

        listContainer.addView(
            itemContainer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
        )
    }

    private fun calculateRemainingDays(
        dateString: String
    ): Long {

        return try {

            val targetDate = dateFormat.parse(dateString)
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
                target.timeInMillis - today.timeInMillis

            if (difference <= 0) {
                0L
            } else {
                difference / (24L * 60L * 60L * 1000L)
            }

        } catch (e: Exception) {
            0L
        }
    }

    private fun calculateProgress(
        remainingDays: Long
    ): Float {

        if (remainingDays <= 0) {
            return 1f
        }

        val maximumDays = 365f

        return (
            1f - remainingDays.toFloat() / maximumDays
        ).coerceIn(0.05f, 1f)
    }

    private fun deleteItem(index: Int) {

        val items = getStoredItems().toMutableList()

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
}
