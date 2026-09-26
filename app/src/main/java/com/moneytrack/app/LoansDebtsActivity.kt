package com.moneytrack.app

import android.app.Activity
import android.app.DatePickerDialog
import android.app.AlertDialog
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

    private val white = Color.WHITE
    private val secondary = Color.rgb(168, 174, 187)
    private val muted = Color.rgb(115, 121, 135)

    private val blue = Color.rgb(74, 145, 255)
    private val amber = Color.rgb(255, 166, 52)

    private val prefs by lazy {
        getSharedPreferences(
            "moneytrack_data",
            Context.MODE_PRIVATE
        )
    }

    private var showingBorrowed = true

    private lateinit var borrowedTab: TextView
    private lateinit var lentTab: TextView
    private lateinit var entriesContainer: LinearLayout
    private lateinit var totalText: TextView

    private val dateFormat =
        SimpleDateFormat(
            "dd MMM yyyy",
            Locale.ENGLISH
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        buildInterface()
    }

    private fun buildInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dp(20),
                dp(18),
                dp(20),
                dp(12)
            )
        }

        val back = TextView(this).apply {
            text = "‹"
            textSize = 32f
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(
                0,
                0,
                dp(12),
                0
            )

            setOnClickListener {
                finish()
            }
        }

        header.addView(
            back,
            LinearLayout.LayoutParams(
                dp(42),
                dp(48)
            )
        )

        val title = TextView(this).apply {
            text = "DEBT & LOAN"
            textSize = 20f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL
        }

        header.addView(
            title,
            LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            )
        )

        root.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(
                dp(20),
                dp(4),
                dp(20),
                dp(14)
            )
        }

        borrowedTab = createTab(
            "BORROWED"
        )

        lentTab = createTab(
            "LENT"
        )

        tabs.addView(
            borrowedTab,
            LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            )
        )

        val tabGap = View(this)

        tabs.addView(
            tabGap,
            LinearLayout.LayoutParams(
                dp(8),
                dp(48)
            )
        )

        tabs.addView(
            lentTab,
            LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            )
        )

        root.addView(
            tabs,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(20),
                0,
                dp(20),
                dp(32)
            )
        }

        totalText = TextView(this).apply {
            textSize = 16f
            setTextColor(secondary)
            gravity = Gravity.CENTER
            setPadding(
                0,
                dp(8),
                0,
                dp(16)
            )
        }

        content.addView(
            totalText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        entriesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        content.addView(
            entriesContainer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val addButton = Button(this).apply {
            text = if (showingBorrowed) {
                "+  ADD BORROWED"
            } else {
                "+  ADD LENT"
            }

            textSize = 14f
            setTextColor(white)
            isAllCaps = false
            background = roundedBackground(
                surfaceColor,
                dp(14f)
            )

            setOnClickListener {
                showAddDialog()
            }
        }

        content.addView(
            addButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                topMargin = dp(14)
            }
        )

        scrollView.addView(content)

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)

        borrowedTab.setOnClickListener {
            if (!showingBorrowed) {
                showingBorrowed = true
                updateTabs()
                updateList()
                updateAddButton(addButton)
            }
        }

        lentTab.setOnClickListener {
            if (showingBorrowed) {
                showingBorrowed = false
                updateTabs()
                updateList()
                updateAddButton(addButton)
            }
        }

        updateTabs()
        updateList()
        updateAddButton(addButton)
    }

    private fun createTab(
        text: String
    ): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = 13f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setPadding(
                dp(8),
                0,
                dp(8),
                0
            )
        }
    }

    private fun updateTabs() {

        borrowedTab.setTextColor(
            if (showingBorrowed) {
                white
            } else {
                secondary
            }
        )

        borrowedTab.background =
            roundedBackground(
                if (showingBorrowed) {
                    blue
                } else {
                    surfaceColor
                },
                dp(12f)
            )

        lentTab.setTextColor(
            if (!showingBorrowed) {
                white
            } else {
                secondary
            }
        )

        lentTab.background =
            roundedBackground(
                if (!showingBorrowed) {
                    amber
                } else {
                    surfaceColor
                },
                dp(12f)
            )
    }

    private fun updateAddButton(
        button: Button
    ) {
        button.text =
            if (showingBorrowed) {
                "+  ADD BORROWED"
            } else {
                "+  ADD LENT"
            }
    }

    private fun updateList() {

        entriesContainer.removeAllViews()

        val entries = loadEntries(
            showingBorrowed
        )

        var total = 0.0

        for (entry in entries) {
            total += entry.amount

            entriesContainer.addView(
                createEntryCard(entry)
            )
        }

        totalText.text =
            if (entries.isEmpty()) {
                if (showingBorrowed) {
                    "Nothing borrowed"
                } else {
                    "Nothing lent"
                }
            } else {
                val label =
                    if (showingBorrowed) {
                        "Total Owed"
                    } else {
                        "Total Receivable"
                    }

                "$label  ${money(total)}"
            }
    }

    private fun createEntryCard(
        entry: DebtEntry
    ): LinearLayout {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
            )

            background = roundedBackground(
                cardColor,
                dp(14f)
            )
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val name = TextView(this).apply {
            text = entry.name
            textSize = 17f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
        }

        topRow.addView(
            name,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val delete = TextView(this).apply {
            text = "Delete"
            textSize = 13f
            setTextColor(
                Color.rgb(230, 100, 100)
            )
            gravity = Gravity.CENTER
            setPadding(
                dp(10),
                dp(8),
                dp(4),
                dp(8)
            )

            setOnClickListener {
                confirmDelete(entry)
            }
        }

        topRow.addView(
            delete,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        card.addView(
            topRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val amount = TextView(this).apply {
            text = money(entry.amount)
            textSize = 18f
            setTextColor(
                if (showingBorrowed) {
                    amber
                } else {
                    blue
                }
            )
            typeface = Typeface.DEFAULT_BOLD
            setPadding(
                0,
                dp(10),
                0,
                dp(3)
            )
        }

        card.addView(
            amount,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val date = TextView(this).apply {
            text = "Date: ${entry.date}"
            textSize = 13f
            setTextColor(muted)
        }

        card.addView(
            date,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        return card.apply {
            val params =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

            params.bottomMargin = dp(10)

            layoutParams = params
        }
    }

    private data class DebtEntry(
        val id: Long,
        val name: String,
        val amount: Double,
        val date: String
    )

    private fun showAddDialog() {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(24),
                dp(8),
                dp(24),
                0
            )
        }

        val nameInput = EditText(this).apply {
            hint = "Name"
            textSize = 16f
            setSingleLine(true)
        }

        val amountInput = EditText(this).apply {
            hint = "Amount (€)"
            textSize = 16f
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setSingleLine(true)
        }

        val dateInput = TextView(this).apply {
            text = dateFormat.format(
                Calendar.getInstance().time
            )
            textSize = 16f
            setTextColor(white)
            setPadding(
                0,
                dp(18),
                0,
                dp(18)
            )

            setOnClickListener {
                showDatePicker(this)
            }
        }

        container.addView(
            nameInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        container.addView(
            amountInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        )

        container.addView(
            dateInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
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
            .setView(container)
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Add",
                null
            )
            .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val name =
                    nameInput.text
                        .toString()
                        .trim()

                val amount =
                    amountInput.text
                        .toString()
                        .replace(",", ".")
                        .toDoubleOrNull()

                if (name.isEmpty()) {
                    nameInput.error =
                        "Enter a name"
                    return@setOnClickListener
                }

                if (amount == null || amount <= 0.0) {
                    amountInput.error =
                        "Enter a valid amount"
                    return@setOnClickListener
                }

                addEntry(
                    showingBorrowed,
                    DebtEntry(
                        id = System.currentTimeMillis(),
                        name = name,
                        amount = amount,
                        date = dateInput.text.toString()
                    )
                )

                updateList()
                dialog.dismiss()
            }
        }

        dialog.show()
    }
        private fun showDatePicker(
        target: TextView
    ) {

        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate =
                    Calendar.getInstance().apply {
                        set(
                            year,
                            month,
                            day
                        )
                    }

                target.text =
                    dateFormat.format(
                        selectedDate.time
                    )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun confirmDelete(
        entry: DebtEntry
    ) {

        AlertDialog.Builder(this)
            .setTitle("Delete entry?")
            .setMessage(
                "${entry.name}\n${money(entry.amount)}"
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Delete"
            ) { _, _ ->

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

        val key =
            if (borrowed) {
                "loans_borrowed"
            } else {
                "loans_lent"
            }

        val result =
            mutableListOf<DebtEntry>()

        val json =
            prefs.getString(
                key,
                "[]"
            ) ?: "[]"

        try {

            val array =
                JSONArray(json)

            for (index in 0 until array.length()) {

                val objectData =
                    array.getJSONObject(index)

                result.add(
                    DebtEntry(
                        id = objectData.optLong(
                            "id"
                        ),
                        name = objectData.optString(
                            "name"
                        ),
                        amount = objectData.optDouble(
                            "amount",
                            0.0
                        ),
                        date = objectData.optString(
                            "date"
                        )
                    )
                )
            }

        } catch (_: Exception) {
            // Keep the list empty if stored data
            // cannot be read.
        }

        return result
    }

    private fun addEntry(
        borrowed: Boolean,
        entry: DebtEntry
    ) {

        val entries =
            loadEntries(borrowed)

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

        val entries =
            loadEntries(borrowed)

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

        val key =
            if (borrowed) {
                "loans_borrowed"
            } else {
                "loans_lent"
            }

        val array = JSONArray()

        for (entry in entries) {

            val objectData =
                JSONObject().apply {
                    put(
                        "id",
                        entry.id
                    )
                    put(
                        "name",
                        entry.name
                    )
                    put(
                        "amount",
                        entry.amount
                    )
                    put(
                        "date",
                        entry.date
                    )
                }

            array.put(objectData)
        }

        prefs.edit()
            .putString(
                key,
                array.toString()
            )
            .apply()
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
            ).toInt()
    }

    private fun roundedBackground(
        color: Int,
        radius: Float
    ): android.graphics.drawable.GradientDrawable {

        return android.graphics.drawable.GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
