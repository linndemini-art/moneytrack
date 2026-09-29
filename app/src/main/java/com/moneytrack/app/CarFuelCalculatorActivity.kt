package com.moneytrack.app

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import java.util.Locale

class CarFuelCalculatorActivity : Activity() {

    private val white = Color.WHITE
    private val muted = Color.rgb(160, 174, 194)
    private val backgroundColor = Color.rgb(8, 13, 20)
    private val cardColor = Color.rgb(17, 25, 36)
    private val inputColor = Color.rgb(22, 32, 46)
    private val accent = Color.rgb(255, 166, 52)

    private lateinit var distanceInput: EditText
    private lateinit var priceInput: EditText
    private lateinit var consumptionInput: EditText

    private lateinit var fuelNeededValue: TextView
    private lateinit var costPer100Value: TextView
    private lateinit var estimatedCostValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 92, 24, 24)
            setBackgroundColor(backgroundColor)
        }

        val title = TextView(this).apply {
            text = "CAR FUEL CALCULATOR"
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
            text = "Calculate your fuel cost before the trip"
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

        val inputCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            background = roundedBackground(cardColor, 18f)
        }

        inputCard.addView(createSectionLabel("DISTANCE"))

        val distanceSection = createInput(
            "Kilometers",
            "300"
        )
        distanceInput = distanceSection
        inputCard.addView(distanceInput)

        inputCard.addView(createSpacer(14))

        inputCard.addView(createSectionLabel("FUEL TYPE"))

        val fuelSpinner = Spinner(this).apply {
            background = roundedBackground(inputColor, 12f)

            adapter = ArrayAdapter(
                this@CarFuelCalculatorActivity,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf("Diesel", "Petrol")
            )
        }

        inputCard.addView(
            fuelSpinner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                52
            )
        )

        inputCard.addView(createSpacer(14))

        inputCard.addView(createSectionLabel("FUEL PRICE"))

        priceInput = createInput(
            "Price per liter",
            "1.45"
        )
        inputCard.addView(priceInput)

        inputCard.addView(createSpacer(14))

        inputCard.addView(createSectionLabel("CONSUMPTION"))

        consumptionInput = createInput(
            "Liters per 100 km",
            "6.5"
        )
        inputCard.addView(consumptionInput)

        root.addView(
            inputCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 18
            }
        )

        val resultsTitle = TextView(this).apply {
            text = "TRIP ESTIMATE"
            textSize = 12f
            setTextColor(muted)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.12f
        }

        root.addView(
            resultsTitle,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
        )

        val resultsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            background = roundedBackground(cardColor, 18f)
        }

        fuelNeededValue = createResult(
            "FUEL NEEDED",
            "—"
        )

        costPer100Value = createResult(
            "COST PER 100 KM",
            "—"
        )

        estimatedCostValue = createMainResult()

        resultsCard.addView(fuelNeededValue)
        resultsCard.addView(createDivider())
        resultsCard.addView(costPer100Value)
        resultsCard.addView(createDivider())
        resultsCard.addView(estimatedCostValue)

        root.addView(
            resultsCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(root)

        val updateCalculation = {
            updateResults()
        }

        distanceInput.addTextChangedListener(
            SimpleTextWatcher(updateCalculation)
        )

        priceInput.addTextChangedListener(
            SimpleTextWatcher(updateCalculation)
        )

        consumptionInput.addTextChangedListener(
            SimpleTextWatcher(updateCalculation)
        )

        updateResults()
    }

    private fun createSectionLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 11f
            setTextColor(muted)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
            setPadding(2, 0, 0, 8)
        }
    }

    private fun createInput(
        hint: String,
        initialValue: String
    ): EditText {
        return EditText(this).apply {
            inputType =
                InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL

            setText(initialValue)
            setHint(hint)
            setTextColor(white)
            setHintTextColor(muted)
            textSize = 16f
            singleLine = true
            setPadding(16, 0, 16, 0)
            background = roundedBackground(inputColor, 12f)

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                52
            )
        }
    }

    private fun createResult(
        label: String,
        value: String
    ): TextView {
        return TextView(this).apply {
            text = "$label\n$value"
            textSize = 15f
            setTextColor(white)
            setPadding(2, 4, 2, 12)
        }
    }

    private fun createMainResult(): TextView {
        return TextView(this).apply {
            text = "ESTIMATED COST\n—"
            textSize = 20f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(2, 12, 2, 4)
        }
    }

    private fun createDivider(): View {
        return View(this).apply {
            setBackgroundColor(Color.rgb(38, 48, 63))

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                topMargin = 2
                bottomMargin = 2
            }
        }
    }

    private fun createSpacer(height: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                1,
                height
            )
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

    private fun updateResults() {
        val distance =
            distanceInput.text.toString().toDoubleOrNull() ?: 0.0

        val price =
            priceInput.text.toString().toDoubleOrNull() ?: 0.0

        val consumption =
            consumptionInput.text.toString().toDoubleOrNull() ?: 0.0

        val fuelNeeded = distance * consumption / 100.0
        val costPer100 = consumption * price
        val estimatedCost = fuelNeeded * price

        fuelNeededValue.text = String.format(
            Locale.US,
            "FUEL NEEDED\n%.2f L",
            fuelNeeded
        )

        costPer100Value.text = String.format(
            Locale.US,
            "COST PER 100 KM\n€%.2f",
            costPer100
        )

        estimatedCostValue.text = String.format(
            Locale.US,
            "ESTIMATED COST\n€%.2f",
            estimatedCost
        )
    }

    private class SimpleTextWatcher(
        private val onChanged: () -> Unit
    ) : TextWatcher {

        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) = Unit

        override fun onTextChanged(
            s: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
            onChanged()
        }

        override fun afterTextChanged(
            s: android.text.Editable?
        ) = Unit
    }
}
