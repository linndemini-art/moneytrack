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
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

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
    private val savingsKey = "savings_goals_json"

    private lateinit var listContainer: LinearLayout
    private lateinit var savingsContainer: LinearLayout
    private lateinit var scheduledTab: TextView
    private lateinit var savingsTab: TextView
    private lateinit var pageTitle: TextView
    private lateinit var pageSubtitle: TextView
    private lateinit var actionButton: TextView
    private var showingSavings = false

    private data class Contribution(
        val id: String,
        val amount: Double,
        val note: String,
        val date: Long
    )

    private data class SavingsGoal(
        val id: String,
        val name: String,
        val target: Double,
        val starting: Double,
        val contributions: List<Contribution>
    ) {
        val saved: Double get() = starting + contributions.sumOf { it.amount }
        val remaining: Double get() = (target - saved).coerceAtLeast(0.0)
        val progress: Float get() =
            if (target <= 0.0) 0f else (saved / target).toFloat().coerceIn(0f, 1f)
    }

    private val dateFormat = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.US
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildScreen()
        loadItems()
        loadSavingsGoals()
        updateTabUI()
    }

    override fun onResume() {
        super.onResume()
        if (::listContainer.isInitialized) {
            loadItems()
            loadSavingsGoals()
            updateTabUI()
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
            setOnClickListener { finish() }
        }
        topRow.addView(backButton, LinearLayout.LayoutParams(dpToPx(48), dpToPx(48)))
        pageTitle = TextView(this).apply {
            text = "SCHEDULED"
            textSize = 24f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.04f
        }
        topRow.addView(pageTitle, LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ))
        root.addView(topRow, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dpToPx(6) })
        pageSubtitle = TextView(this).apply {
            text = "Keep track of important future dates"
            textSize = 14f
            setTextColor(muted)
        }
        root.addView(pageSubtitle, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dpToPx(20) })
        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = roundedBackground(cardColor, 14f)
            setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
        }
        scheduledTab = createTab("SCHEDULED")
        savingsTab = createTab("SAVINGS")
        scheduledTab.setOnClickListener { showingSavings = false; updateTabUI() }
        savingsTab.setOnClickListener { showingSavings = true; updateTabUI() }
        tabs.addView(scheduledTab, LinearLayout.LayoutParams(0, dpToPx(44), 1f))
        tabs.addView(savingsTab, LinearLayout.LayoutParams(0, dpToPx(44), 1f))
        root.addView(tabs, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dpToPx(18) })
        listContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        savingsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
        root.addView(listContainer, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        root.addView(savingsContainer, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        actionButton = TextView(this).apply {
            text = "+ ADD"
            textSize = 16f
            setTextColor(AppearanceManager.getAccentColor(this@ScheduledActivity))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(18), 0, dpToPx(18))
            setOnClickListener {
                if (showingSavings) showAddSavingsGoalDialog() else showAddDialog()
            }
        }
        root.addView(actionButton, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        setContentView(root)
    }

    private fun createTab(label: String): TextView = TextView(this).apply {
        text = label
        textSize = 12f
        gravity = Gravity.CENTER
        typeface = Typeface.DEFAULT_BOLD
        isClickable = true
        isFocusable = true
        setTextColor(muted)
        background = roundedBackground(Color.TRANSPARENT, 10f)
    }

    private fun updateTabUI() {
        val accent = AppearanceManager.getAccentColor(this)
        scheduledTab.setTextColor(if (!showingSavings) white else muted)
        savingsTab.setTextColor(if (showingSavings) white else muted)
        scheduledTab.background = roundedBackground(
            if (!showingSavings) accent else Color.TRANSPARENT, 10f
        )
        savingsTab.background = roundedBackground(
            if (showingSavings) accent else Color.TRANSPARENT, 10f
        )
        listContainer.visibility = if (showingSavings) View.GONE else View.VISIBLE
        savingsContainer.visibility = if (showingSavings) View.VISIBLE else View.GONE
        pageTitle.text = if (showingSavings) "SAVINGS GOALS" else "SCHEDULED"
        pageSubtitle.text = if (showingSavings) {
            "Set targets and track money you put aside"
        } else "Keep track of important future dates"
        actionButton.text = if (showingSavings) "+ ADD SAVINGS GOAL" else "+ ADD"
        actionButton.setTextColor(accent)
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

        val existingParts = items[index].split("|")
        val createdDate = existingParts.getOrNull(2)
            ?: dateFormat.format(Calendar.getInstance().time)
        items[index] = "$name|$date|$createdDate"

        saveItems(items)
        loadItems()
    }

        private fun loadItems() {
    listContainer.removeAllViews()

    val items = getStoredItems()

    for ((index, item) in items.withIndex()) {
        val parts = item.split("|")
        if (parts.size < 2) continue

        val createdDate = parts.getOrNull(2)
            ?.takeIf { isValidDate(it) }
            ?: dateFormat.format(Calendar.getInstance().time)

        addScheduledItem(
            name = parts[0],
            date = parts[1],
            createdDate = createdDate,
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
    AppearanceManager.getAccentColor(this),
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


    private fun loadSavingsGoals(): List<SavingsGoal> {
        val raw = getSharedPreferences(prefsName, MODE_PRIVATE)
            .getString(savingsKey, "[]") ?: "[]"
        val goals = try {
            val array = JSONArray(raw)
            val result = mutableListOf<SavingsGoal>()
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val entries = item.optJSONArray("contributions") ?: JSONArray()
                val contributions = mutableListOf<Contribution>()
                for (j in 0 until entries.length()) {
                    val entry = entries.optJSONObject(j) ?: continue
                    val amount = entry.optDouble("amount", 0.0)
                    if (!amount.isFinite() || amount <= 0.0) continue
                    contributions.add(Contribution(
                        id = entry.optString("id", UUID.randomUUID().toString()),
                        amount = amount, note = entry.optString("note", ""),
                        date = entry.optLong("date", System.currentTimeMillis())
                    ))
                }
                val name = item.optString("name", "").trim()
                val target = item.optDouble("target", 0.0)
                val starting = item.optDouble("starting", 0.0)
                if (name.isEmpty() || !target.isFinite() || target <= 0.0 ||
                    !starting.isFinite() || starting < 0.0) continue
                result.add(SavingsGoal(
                    id = item.optString("id", UUID.randomUUID().toString()),
                    name = name, target = target, starting = starting,
                    contributions = contributions
                ))
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
        if (::savingsContainer.isInitialized) renderSavingsGoals(goals)
        return goals
    }

    private fun saveSavingsGoals(goals: List<SavingsGoal>) {
        val array = JSONArray()
        goals.forEach { goal ->
            val item = JSONObject().put("id", goal.id).put("name", goal.name)
                .put("target", goal.target).put("starting", goal.starting)
            val entries = JSONArray()
            goal.contributions.forEach { entry ->
                entries.put(JSONObject().put("id", entry.id).put("amount", entry.amount)
                    .put("note", entry.note).put("date", entry.date))
            }
            item.put("contributions", entries)
            array.put(item)
        }
        getSharedPreferences(prefsName, MODE_PRIVATE).edit()
            .putString(savingsKey, array.toString()).apply()
        renderSavingsGoals(goals)
    }

    private fun renderSavingsGoals(goals: List<SavingsGoal>) {
        if (!::savingsContainer.isInitialized) return
        savingsContainer.removeAllViews()
        if (goals.isEmpty()) {
            savingsContainer.addView(TextView(this).apply {
                text = "No savings goals yet.\nCreate a goal and start putting money aside."
                textSize = 15f
                setTextColor(muted)
                gravity = Gravity.CENTER
                setPadding(dpToPx(20), dpToPx(36), dpToPx(20), dpToPx(36))
            }, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ))
            return
        }
        val accent = AppearanceManager.getAccentColor(this)
        goals.forEach { goal ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
                background = roundedBackground(cardColor, 16f)
            }
            val heading = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            heading.addView(TextView(this).apply {
                text = goal.name
                textSize = 17f
                setTextColor(white)
                typeface = Typeface.DEFAULT_BOLD
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            heading.addView(TextView(this).apply {
                text = "⋮"
                textSize = 24f
                gravity = Gravity.CENTER
                setTextColor(muted)
                setPadding(dpToPx(12), 0, 0, 0)
                setOnClickListener { showSavingsGoalOptions(goal.id) }
            })
            card.addView(heading)
            card.addView(TextView(this).apply {
                text = "€${formatMoney(goal.saved)} saved"
                textSize = 15f
                setTextColor(white)
                setPadding(0, dpToPx(10), 0, dpToPx(4))
            })
            card.addView(TextView(this).apply {
                text = "Target €${formatMoney(goal.target)}  ·  " +
                    if (goal.remaining > 0.0) "€${formatMoney(goal.remaining)} to go"
                    else "Goal reached"
                textSize = 13f
                setTextColor(muted)
            })
            card.addView(TextView(this).apply {
                text = "${(goal.progress * 100f).toInt()}%"
                textSize = 12f
                setTextColor(accent)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.END
                setPadding(0, dpToPx(10), 0, dpToPx(5))
            })
            card.addView(ScheduledProgressView(this, goal.progress, accent, progressBackground),
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(16)
                ))
            card.addView(TextView(this).apply {
                text = "+ ADD MONEY"
                textSize = 13f
                setTextColor(accent)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(14), 0, dpToPx(16))
                setOnClickListener { showAddMoneyDialog(goal.id) }
            })
                        if (goal.contributions.isNotEmpty()) {
                card.addView(TextView(this).apply {
                    text = "CONTRIBUTIONS"
                    textSize = 11f
                    letterSpacing = 0.06f
                    setTextColor(muted)
                    typeface = Typeface.DEFAULT_BOLD
                    setPadding(0, dpToPx(8), 0, dpToPx(4))
                })
                goal.contributions.sortedByDescending { it.date }.take(4).forEach { entry ->
                    val row = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(0, dpToPx(4), 0, dpToPx(4))
                    }
                    val date = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                        .format(java.util.Date(entry.date))
                    row.addView(TextView(this).apply {
                        text = if (entry.note.isBlank()) date else "$date · ${entry.note}"
                        textSize = 12f
                        setTextColor(muted)
                    }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                    row.addView(TextView(this).apply {
                        text = "+€${formatMoney(entry.amount)}"
                        textSize = 12f
                        setTextColor(white)
                        typeface = Typeface.DEFAULT_BOLD
                    })
                    card.addView(row)
                }
                if (goal.contributions.size > 4) card.addView(TextView(this).apply {
                    text = "${goal.contributions.size - 4} earlier contributions"
                    textSize = 11f
                    setTextColor(muted)
                })
            }
            savingsContainer.addView(card, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(12) })
        }
    }

    private fun formatMoney(value: Double): String =
        String.format(Locale.GERMANY, "%,.2f", value)

    private fun showAddSavingsGoalDialog(existingId: String? = null) {
        val existing = if (existingId == null) null
            else loadSavingsGoals().firstOrNull { it.id == existingId }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(24), dpToPx(16), dpToPx(24), dpToPx(8))
        }
        fun field(hintText: String, value: String = "") = EditText(this).apply {
            hint = hintText
            setSingleLine(true)
            setText(value)
            setTextColor(white)
            setHintTextColor(muted)
            background = roundedBackground(inputColor, 10f)
            setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10))
        }
        val nameField = field("Goal name", existing?.name ?: "")
        val targetField = field("Target amount (€)", existing?.target?.toString() ?: "").apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val startingField = field(
            "Already saved (€), optional", existing?.starting?.toString() ?: "0"
        ).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        layout.addView(nameField)
        layout.addView(targetField, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dpToPx(10) })
        layout.addView(startingField, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dpToPx(10) })
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add savings goal" else "Edit savings goal")
            .setView(layout).setNegativeButton("Cancel", null)
            .setPositiveButton(if (existing == null) "Create goal" else "Save", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = nameField.text.toString().trim()
                val target = targetField.text.toString().trim().replace(",", ".").toDoubleOrNull()
                val starting = startingField.text.toString().trim().ifEmpty { "0" }
                    .replace(",", ".").toDoubleOrNull()
                if (name.isEmpty()) { nameField.error = "Enter a goal name"; return@setOnClickListener }
                if (target == null || !target.isFinite() || target <= 0.0) {
                    targetField.error = "Enter a target greater than zero"
                    return@setOnClickListener
                }
                if (starting == null || !starting.isFinite() || starting < 0.0) {
                    startingField.error = "Enter zero or a positive amount"
                    return@setOnClickListener
                }
                val goals = loadSavingsGoals().toMutableList()
                if (existing == null) goals.add(SavingsGoal(
                    id = UUID.randomUUID().toString(), name = name,
                    target = target, starting = starting, contributions = emptyList()
                )) else {
                    val index = goals.indexOfFirst { it.id == existing.id }
                    if (index >= 0) goals[index] = goals[index].copy(
                        name = name, target = target, starting = starting
                    )
                }
                saveSavingsGoals(goals)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showAddMoneyDialog(goalId: String) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(24), dpToPx(16), dpToPx(24), dpToPx(8))
        }
        val amountField = EditText(this).apply {
            hint = "Amount (€)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setSingleLine(true)
            setTextColor(white)
            setHintTextColor(muted)
            background = roundedBackground(inputColor, 10f)
            setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10))
        }
        val noteField = EditText(this).apply {
            hint = "Note (optional)"
            setSingleLine(true)
            setTextColor(white)
            setHintTextColor(muted)
            background = roundedBackground(inputColor, 10f)
            setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10))
        }
        layout.addView(amountField)
        layout.addView(noteField, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dpToPx(10) })
        val dialog = AlertDialog.Builder(this).setTitle("Add money")
            .setView(layout).setNegativeButton("Cancel", null)
            .setPositiveButton("Add money", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val amount = amountField.text.toString().trim()
                    .replace(",", ".").toDoubleOrNull()
                if (amount == null || !amount.isFinite() || amount <= 0.0) {
                    amountField.error = "Enter an amount greater than zero"
                    return@setOnClickListener
                }
                val goals = loadSavingsGoals().toMutableList()
                val index = goals.indexOfFirst { it.id == goalId }
                if (index < 0) { dialog.dismiss(); return@setOnClickListener }
                val goal = goals[index]
                goals[index] = goal.copy(contributions = goal.contributions + Contribution(
                    id = UUID.randomUUID().toString(), amount = amount,
                    note = noteField.text.toString().trim(),
                    date = System.currentTimeMillis()
                ))
                saveSavingsGoals(goals)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showSavingsGoalOptions(goalId: String) {
        AlertDialog.Builder(this).setTitle("Savings goal")
            .setItems(arrayOf("Edit goal", "Delete goal")) { _, which ->
                when (which) {
                    0 -> showAddSavingsGoalDialog(goalId)
                    1 -> AlertDialog.Builder(this).setTitle("Delete savings goal?")
                        .setMessage("This will delete the goal and its contribution history.")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete") { _, _ ->
                            saveSavingsGoals(loadSavingsGoals().filterNot { it.id == goalId })
                        }.show()
                }
            }.show()
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
        private val fillColor: Int,
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
        color = fillColor
        alpha = 150
        style = Paint.Style.FILL

        setShadowLayer(
            9f * density,
            0f,
            0f,
            fillColor
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
                fillColor,
                fillColor,
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
            
