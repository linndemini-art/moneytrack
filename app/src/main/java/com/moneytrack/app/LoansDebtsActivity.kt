package com.moneytrack.app

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

class LoansDebtsActivity : Activity() {

    private val backgroundColor = Color.rgb(7, 8, 12)
    private val cardColor = Color.rgb(20, 22, 29)
    private val surfaceColor = Color.rgb(28, 31, 40)
    private val whiteColor = Color.WHITE
    private val secondaryColor = Color.rgb(190, 194, 204)
    private val mutedColor = Color.rgb(125, 130, 142)
    private val blueColor = Color.rgb(74, 145, 255)
    private val amberColor = Color.rgb(255, 166, 52)

    private lateinit var prefs: android.content.SharedPreferences

    private var showingBorrowed = true

    private lateinit var borrowedTab: TextView
    private lateinit var lentTab: TextView
    private lateinit var entriesContainer: LinearLayout
    private lateinit var totalText: TextView

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        prefs = getSharedPreferences("moneytrack_data", Context.MODE_PRIVATE)

        buildInterface()
    }

    private fun buildInterface() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(backgroundColor)

        val header = LinearLayout(this)
        header.orientation = LinearLayout.HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL
        header.setPadding(dp(18), dp(18), dp(18), dp(12))

        val backButton = TextView(this)
        backButton.text = "‹"
        backButton.textSize = 34f
        backButton.setTextColor(whiteColor)
        backButton.gravity = Gravity.CENTER
        backButton.setOnClickListener {
            finish()
        }

        header.addView(
            backButton,
            LinearLayout.LayoutParams(dp(48), dp(48))
        )

        val title = TextView(this)
        title.text = "Debt & Loan"
        title.textSize = 24f
        title.setTextColor(whiteColor)
        title.typeface = Typeface.DEFAULT_BOLD
        title.gravity = Gravity.CENTER_VERTICAL

        header.addView(
            title,
            LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            )
        )

        root.addView(header)

        val tabs = LinearLayout(this)
        tabs.orientation = LinearLayout.HORIZONTAL
        tabs.setPadding(dp(18), dp(4), dp(18), dp(12))

        borrowedTab = createTab("BORROWED")
        lentTab = createTab("LENT")

        tabs.addView(
            borrowedTab,
            LinearLayout.LayoutParams(
                0,
                dp(44),
                1f
            )
        )

        tabs.addView(
            lentTab,
            LinearLayout.LayoutParams(
                0,
                dp(44),
                1f
            )
        )

        borrowedTab.setOnClickListener {
            if (!showingBorrowed) {
                showingBorrowed = true
                updateTabs()
                updateList()
            }
        }

        lentTab.setOnClickListener {
            if (showingBorrowed) {
                showingBorrowed = false
                updateTabs()
                updateList()
            }
        }

        root.addView(tabs)

        val scrollView = ScrollView(this)
        scrollView.isFillViewport = true

        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(dp(18), dp(4), dp(18), dp(24))

        totalText = TextView(this)
        totalText.textSize = 15f
        totalText.setTextColor(secondaryColor)
        totalText.setPadding(dp(4), dp(4), dp(4), dp(14))

        content.addView(totalText)

        entriesContainer = LinearLayout(this)
        entriesContainer.orientation = LinearLayout.VERTICAL

        content.addView(entriesContainer)

        val addButton = Button(this)
        addButton.textSize = 15f
        addButton.typeface = Typeface.DEFAULT_BOLD
        addButton.setTextColor(backgroundColor)
        addButton.setAllCaps(false)
        addButton.background = roundedBackground(
            amberColor,
            dp(14).toFloat()
        )

        addButton.setOnClickListener {
            showAddDialog()
        }

        content.addView(
            addButton,
            LinearLayout.LayoutParams(
                -1,
                dp(52)
            ).apply {
                topMargin = dp(16)
            }
        )

        scrollView.addView(content)

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setContentView(root)

        updateTabs()
        updateAddButton(addButton)
        updateList()
    }

    private fun createTab(text: String): TextView {

        val tab = TextView(this)

        tab.text = text
        tab.textSize = 13f
        tab.typeface = Typeface.DEFAULT_BOLD
        tab.gravity = Gravity.CENTER
        tab.setPadding(dp(8), dp(4), dp(8), dp(4))

        return tab
    }

    private fun updateTabs() {

        borrowedTab.setTextColor(
            if (showingBorrowed) backgroundColor else secondaryColor
        )

        lentTab.setTextColor(
            if (!showingBorrowed) backgroundColor else secondaryColor
        )

        borrowedTab.background = roundedBackground(
            if (showingBorrowed) blueColor else surfaceColor,
            dp(12).toFloat()
        )

        lentTab.background = roundedBackground(
            if (!showingBorrowed) blueColor else surfaceColor,
            dp(12).toFloat()
        )
    }

    private fun updateAddButton(button: Button) {

        button.text = if (showingBorrowed) {
            "+  Add Borrowed"
        } else {
            "+  Add Lent"
        }
    }

    private fun updateList() {

        entriesContainer.removeAllViews()

        val entries = loadEntries(showingBorrowed)

        var total = 0.0

        for (entry in entries) {
            total += entry.amount

            entriesContainer.addView(
                createEntryCard(entry)
            )
        }

        totalText.text = if (showingBorrowed) {
            "Total borrowed: ${money(total)}"
        } else {
            "Total lent: ${money(total)}"
        }

        if (entries.isEmpty()) {

            val empty = TextView(this)

            empty.text = if (showingBorrowed) {
                "No borrowed money"
            } else {
                "No lent money"
            }

            empty.textSize = 15f
            empty.setTextColor(mutedColor)
            empty.gravity = Gravity.CENTER
            empty.setPadding(
                dp(12),
                dp(36),
                dp(12),
                dp(36)
            )

            entriesContainer.addView(empty)
        }
    }

    private fun createEntryCard(entry: DebtEntry): LinearLayout {

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(
            dp(16),
            dp(14),
            dp(16),
            dp(14)
        )

        card.background = roundedBackground(
            cardColor,
            dp(14).toFloat()
        )

        val name = TextView(this)
        name.text = entry.name
        name.textSize = 17f
        name.setTextColor(whiteColor)
        name.typeface = Typeface.DEFAULT_BOLD

        card.addView(name)

        val typeText = TextView(this)
        typeText.text = if (showingBorrowed) {
            "Owed"
        } else {
            "Receivable"
        }
        typeText.textSize = 13f
        typeText.setTextColor(
            if (showingBorrowed) amberColor else blueColor
        )

        card.addView(
            typeText,
            LinearLayout.LayoutParams(
                -1,
                dp(24)
            )
        )

        val amount = TextView(this)
        amount.text = "Amount: ${money(entry.amount)}"
        amount.textSize = 15f
        amount.setTextColor(secondaryColor)

        card.addView(amount)

        val date = TextView(this)
        date.text = "Date: ${entry.date}"
        date.textSize = 14f
        date.setTextColor(mutedColor)

        card.addView(date)

        val deleteButton = TextView(this)
        deleteButton.text = "Delete"
        deleteButton.textSize = 14f
        deleteButton.setTextColor(Color.rgb(255, 100, 100))
        deleteButton.gravity = Gravity.CENTER

        deleteButton.setPadding(
            dp(12),
            dp(8),
            dp(12),
            dp(8)
        )

        deleteButton.setOnClickListener {
            confirmDelete(entry)
        }

        card.addView(
            deleteButton,
            LinearLayout.LayoutParams(
                -1,
                dp(42)
            ).apply {
                topMargin = dp(8)
            }
        )

        card.layoutParams = LinearLayout.LayoutParams(
            -1,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(12)
        }

        return card
    }

    private data class DebtEntry(
        val id: Long,
        val name: String,
        val amount: Double,
        val date: String
    )
        private fun showAddDialog() {

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(
            dp(24),
            dp(8),
            dp(24),
            dp(8)
        )

        val nameInput = EditText(this)
        nameInput.hint = "Name"
        nameInput.setSingleLine(true)

        layout.addView(
            nameInput,
            LinearLayout.LayoutParams(
                -1,
                dp(56)
            )
        )

        val amountInput = EditText(this)
        amountInput.hint = "Amount (€)"
        amountInput.inputType =
            android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        amountInput.setSingleLine(true)

        layout.addView(
            amountInput,
            LinearLayout.LayoutParams(
                -1,
                dp(56)
            )
        )

        val dateButton = Button(this)
        dateButton.text = "Date: ${dateFormat.format(Calendar.getInstance().time)}"
        dateButton.setAllCaps(false)

        var selectedDate = Calendar.getInstance()

        dateButton.setOnClickListener {
            showDatePicker(selectedDate) { calendar ->
                selectedDate = calendar
                dateButton.text =
                    "Date: ${dateFormat.format(calendar.time)}"
            }
        }

        layout.addView(
            dateButton,
            LinearLayout.LayoutParams(
                -1,
                dp(52)
            )
        )

        val dialog = AlertDialog.Builder(this)
            .setTitle(
                if (showingBorrowed) {
                    "Add Borrowed"
                } else {
                    "Add Lent"
                }
            )
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val name = nameInput.text
                        .toString()
                        .trim()

                    val amountText = amountInput.text
                        .toString()
                        .trim()
                        .replace(",", ".")

                    val amount = amountText.toDoubleOrNull()

                    if (name.isEmpty()) {
                        nameInput.error = "Enter a name"
                        return@setOnClickListener
                    }

                    if (amount == null || amount <= 0.0) {
                        amountInput.error = "Enter a valid amount"
                        return@setOnClickListener
                    }

                    val date = dateFormat.format(selectedDate.time)

                    addEntry(
                        showingBorrowed,
                        DebtEntry(
                            id = System.currentTimeMillis(),
                            name = name,
                            amount = amount,
                            date = date
                        )
                    )

                    updateList()

                    dialog.dismiss()
                }
        }

        dialog.show()
    }

    private fun showDatePicker(
        initial: Calendar,
        onSelected: (Calendar) -> Unit
    ) {

        val picker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->

                val selected = Calendar.getInstance()

                selected.set(
                    year,
                    month,
                    dayOfMonth
                )

                onSelected(selected)
            },
            initial.get(Calendar.YEAR),
            initial.get(Calendar.MONTH),
            initial.get(Calendar.DAY_OF_MONTH)
        )

        picker.show()
    }

    private fun confirmDelete(entry: DebtEntry) {

        AlertDialog.Builder(this)
            .setTitle("Delete entry?")
            .setMessage(
                "Delete ${entry.name} (${money(entry.amount)})?"
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->

                deleteEntry(
                    showingBorrowed,
                    entry.id
                )

                updateList()
            }
            .show()
    }

    private fun loadEntries(
        borrowed: Boolean
    ): MutableList<DebtEntry> {

        val key = if (borrowed) {
            "loans_borrowed"
        } else {
            "loans_lent"
        }

        val result = mutableListOf<DebtEntry>()

        val jsonString = prefs.getString(key, null)
            ?: return result

        try {

            val array = JSONArray(jsonString)

            for (i in 0 until array.length()) {

                val obj = array.getJSONObject(i)

                result.add(
                    DebtEntry(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        amount = obj.getDouble("amount"),
                        date = obj.getString("date")
                    )
                )
            }

        } catch (_: Exception) {
            return mutableListOf()
        }

        return result
    }

    private fun addEntry(
        borrowed: Boolean,
        entry: DebtEntry
    ) {

        val entries = loadEntries(borrowed)

        entries.add(entry)

        saveEntries(
            borrowed,
            entries
        )
    }

    private fun deleteEntry(
        borrowed: Boolean,
        id: Long
    ) {

        val entries = loadEntries(borrowed)

        entries.removeAll {
            it.id == id
        }

        saveEntries(
            borrowed,
            entries
        )
    }

    private fun saveEntries(
        borrowed: Boolean,
        entries: List<DebtEntry>
    ) {

        val key = if (borrowed) {
            "loans_borrowed"
        } else {
            "loans_lent"
        }

        val array = JSONArray()

        for (entry in entries) {

            val obj = JSONObject()

            obj.put("id", entry.id)
            obj.put("name", entry.name)
            obj.put("amount", entry.amount)
            obj.put("date", entry.date)

            array.put(obj)
        }

        prefs.edit()
            .putString(
                key,
                array.toString()
            )
            .apply()
    }

    private fun money(value: Double): String {

        val formatter =
            NumberFormat.getCurrencyInstance(Locale.GERMANY)

        return formatter.format(value)
    }

    private fun dp(value: Int): Int {

        return (value * resources.displayMetrics.density)
            .toInt()
    }

    private fun roundedBackground(
        color: Int,
        radius: Float
    ): android.graphics.drawable.GradientDrawable {

        val drawable =
            android.graphics.drawable.GradientDrawable()

        drawable.setColor(color)
        drawable.cornerRadius = radius

        return drawable
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        finish()
    }
}
