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

    private val blueStart = Color.rgb(20, 92, 190)
    private val blueEnd = Color.rgb(8, 52, 125)

    private val inputColor = Color.rgb(12, 24, 42)
    private val dividerColor = Color.rgb(52, 78, 112)
    private val green = Color.rgb(80, 220, 140)

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
            setPadding(70, 92, 24, 24)
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
                120,
                120
            )
        )

        val title = TextView(this).apply {
            text = "CAR FUEL CALCULATOR"
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
            text = "Calculate your trip cost easily"
            textSize = 14f
            setTextColor(muted)
        }

        root.addView(
            subtitle,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 22
            }
        )

        val inputsHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val inputsIcon = TextView(this).apply {
            text = "🚗"
            textSize = 17f
        }

        inputsHeader.addView(
            inputsIcon,
            LinearLayout.LayoutParams(
                28,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val inputsTitle = TextView(this).apply {
            text = "INPUTS"
            textSize = 12f
            setTextColor(muted)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.12f
        }

        inputsHeader.addView(
            inputsTitle,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            inputsHeader,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
        )

        val inputCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            background = roundedBackground(
                Color.rgb(15, 31, 54),
                22f
            )
            elevation = 4f
        }

        val distanceLabel = createFieldLabel("DISTANCE")
        inputCard.addView(distanceLabel)

        val distanceField = createInputWithUnit(
            initialValue = "300",
            unit = "km"
        )

        distanceInput = distanceField.first

        inputCard.addView(
            distanceField.second,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )
        )

        inputCard.addView(createSpacer(20))

        val fuelPriceRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val fuelColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        fuelColumn.addView(createFieldLabel("FUEL TYPE"))

        val fuelSpinner = Spinner(this).apply {
            background = roundedBackground(inputColor, 14f)

            adapter = ArrayAdapter(
                this@CarFuelCalculatorActivity,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf("Diesel", "Petrol")
            )
        }

        fuelColumn.addView(
            fuelSpinner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )
        )

        fuelPriceRow.addView(
            fuelColumn,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginEnd = 10
            }
        )

        val priceColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        priceColumn.addView(createFieldLabel("PRICE"))

        val priceField = createInputWithUnit(
            initialValue = "1.45",
            unit = "€/L"
        )

        priceInput = priceField.first

        priceColumn.addView(
            priceField.second,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )
        )

        fuelPriceRow.addView(
            priceColumn,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = 10
            }
        )

        inputCard.addView(
            fuelPriceRow,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        inputCard.addView(createSpacer(20))

        val consumptionLabel = createFieldLabel("CONSUMPTION")
        inputCard.addView(consumptionLabel)

        val consumptionField = createInputWithUnit(
            initialValue = "6.5",
            unit = "L/100km"
        )

        consumptionInput = consumptionField.first

        inputCard.addView(
            consumptionField.second,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                70
            )
        )

        root.addView(
            inputCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 26
            }
        )

        val estimateHeader = TextView(this).apply {
            text = "TRIP ESTIMATE"
            textSize = 13f
            setTextColor(muted)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.12f
        }

        root.addView(
            estimateHeader,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
        )

        val resultsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(26, 28, 26, 30)

            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    AppearanceManager.getTheme(
                        this@CarFuelCalculatorActivity
                    ).startColor,
                    AppearanceManager.getTheme(
                        this@CarFuelCalculatorActivity
                    ).endColor
                )
            ).apply {
                cornerRadius = 24f
            }

            elevation = 8f
        }

        val estimatedLabel = TextView(this).apply {
            text = "ESTIMATED COST"
            textSize = 12f
            setTextColor(Color.rgb(205, 220, 240))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.1f
        }

        resultsCard.addView(
            estimatedLabel,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
        )

        estimatedCostValue = TextView(this).apply {
            text = "€28.28"
            textSize = 38f
            setTextColor(green)
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = true
        }

        resultsCard.addView(
            estimatedCostValue,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
        )

        val estimateDescription = TextView(this).apply {
            text = "YOUR TRIP ESTIMATE"
            textSize = 11f
            setTextColor(Color.rgb(215, 228, 244))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.09f
        }

        resultsCard.addView(
            estimateDescription,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 30
            }
        )

        val resultsDivider = View(this).apply {
            setBackgroundColor(dividerColor)
        }

        resultsCard.addView(
            resultsDivider,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                bottomMargin = 28
            }
        )

        val secondaryResults = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val fuelNeededColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val fuelNeededLabel = TextView(this).apply {
            text = "FUEL NEEDED"
            textSize = 11f
            setTextColor(Color.rgb(190, 208, 232))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }

        fuelNeededColumn.addView(fuelNeededLabel)

        fuelNeededValue = TextView(this).apply {
            text = "19.50 L"
            textSize = 20f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 8, 0, 0)
        }

        fuelNeededColumn.addView(fuelNeededValue)

        secondaryResults.addView(
            fuelNeededColumn,
            LinearLayout.LayoutParams(
                0,
                76,
                1f
            )
        )

        val costColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val costLabel = TextView(this).apply {
            text = "COST / 100 KM"
            textSize = 11f
            setTextColor(Color.rgb(190, 208, 232))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }

        costColumn.addView(costLabel)

        costPer100Value = TextView(this).apply {
            text = "€9.43"
            textSize = 20f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 8, 0, 0)
        }

        costColumn.addView(costPer100Value)

        secondaryResults.addView(
            costColumn,
            LinearLayout.LayoutParams(
                0,
                76,
                1f
            )
        )

        resultsCard.addView(
            secondaryResults,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                76
            )
        )

        root.addView(
            resultsCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                330
            ).apply {
                bottomMargin = 20
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

    private fun createFieldLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 11f
            setTextColor(muted)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
            setPadding(2, 0, 2, 9)
        }
    }

    private fun createInputWithUnit(
        initialValue: String,
        unit: String
    ): Pair<EditText, LinearLayout> {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 0, 16, 0)
            background = roundedBackground(inputColor, 14f)
        }

        val input = EditText(this).apply {
            inputType =
                InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL

            setText(initialValue)
            setTextColor(white)
            setHintTextColor(muted)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setSingleLine(true)
            background = null
            setPadding(2, 0, 2, 0)
        }

        container.addView(
            input,
            LinearLayout.LayoutParams(
                0,
                70,
                1f
            )
        )

        val unitText = TextView(this).apply {
            text = unit
            textSize = 13f
            setTextColor(muted)
            gravity = Gravity.CENTER_VERTICAL
            typeface = Typeface.DEFAULT_BOLD
        }

        container.addView(
            unitText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                70
            )
        )

        return Pair(input, container)
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

    private fun blueGradientBackground(): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                blueStart,
                blueEnd
            )
        ).apply {
            cornerRadius = 20f
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

    private fun updateResults() {
        val distance =
            distanceInput.text.toString().toDoubleOrNull() ?: 0.0

        val price =
            priceInput.text.toString().toDoubleOrNull() ?: 0.0

        val consumption =
            consumptionInput.text.toString().toDoubleOrNull() ?: 0.0

        val fuelNeeded =
            distance * consumption / 100.0

        val costPer100 =
            consumption * price

        val estimatedCost =
            fuelNeeded * price

        fuelNeededValue.text = String.format(
            Locale.US,
            "%.2f L",
            fuelNeeded
        )

        costPer100Value.text = String.format(
            Locale.US,
            "€%.2f",
            costPer100
        )

        estimatedCostValue.text = String.format(
            Locale.US,
            "€%.2f",
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
