package com.moneytrack.app

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.TextWatcher
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
    private val card = Color.rgb(17, 25, 36)

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
            setPadding(24, 24, 24, 24)
            setBackgroundColor(backgroundColor)
        }

        val title = TextView(this).apply {
            text = "CAR FUEL CALCULATOR"
            textSize = 24f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 24)
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val distanceSection = createNumberInput(
            "Distance (km)",
            "300"
        )
        distanceInput = distanceSection.second
        root.addView(distanceSection.first)

        root.addView(createLabel("Fuel"))

        val fuelSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@CarFuelCalculatorActivity,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf("Diesel", "Petrol")
            )
        }

        root.addView(
            fuelSpinner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 18
            }
        )

        val priceSection = createNumberInput(
            "Price (€ / L)",
            "1.45"
        )
        priceInput = priceSection.second
        root.addView(priceSection.first)

        val consumptionSection = createNumberInput(
            "Consumption (L / 100 km)",
            "6.5"
        )
        consumptionInput = consumptionSection.second
        root.addView(consumptionSection.first)

        val resultsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(card)
        }

        fuelNeededValue = createResultText("Fuel needed")
        costPer100Value = createResultText("Cost per 100 km")
        estimatedCostValue = createResultText("Estimated cost")

        resultsCard.addView(fuelNeededValue)
        resultsCard.addView(costPer100Value)
        resultsCard.addView(estimatedCostValue)

        root.addView(
            resultsCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
            }
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

    private fun createLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(muted)
            setPadding(0, 8, 0, 6)
        }
    }

    private fun createNumberInput(
        label: String,
        initialValue: String
    ): Pair<LinearLayout, EditText> {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val input = EditText(this).apply {
            inputType =
                InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL

            setText(initialValue)
            setTextColor(white)
            setHintTextColor(muted)
            setBackgroundColor(card)
            setPadding(16, 12, 16, 12)
        }

        container.addView(createLabel(label))

        container.addView(
            input,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
        )

        return Pair(container, input)
    }

    private fun createResultText(label: String): TextView {
        return TextView(this).apply {
            text = "$label\n—"
            textSize = 17f
            setTextColor(white)
            setPadding(0, 8, 0, 16)
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
            "Fuel needed\n%.2f L",
            fuelNeeded
        )

        costPer100Value.text = String.format(
            Locale.US,
            "Cost per 100 km\n€%.2f",
            costPer100
        )

        estimatedCostValue.text = String.format(
            Locale.US,
            "Estimated cost\n€%.2f",
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
