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
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import java.util.Locale

class CarFuelCalculatorActivity : Activity() {

    private val white = Color.WHITE
    private val muted = Color.rgb(160, 174, 194)
    private val backgroundColor = Color.rgb(8, 13, 20)

    private val cardColor = Color.rgb(15, 31, 54)
    private val inputColor = Color.rgb(12, 24, 42)

    private lateinit var distanceInput: EditText
    private lateinit var priceInput: EditText
    private lateinit var consumptionInput: EditText

    private lateinit var fuelNeededValue: TextView
    private lateinit var costPer100Value: TextView
    private lateinit var estimatedCostValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(backgroundColor)
            isFillViewport = true
            clipToPadding = false
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 70, 24, 40)
            setBackgroundColor(backgroundColor)
        }

        scrollView.addView(
            root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // ---------------------------------------------------------
        // HEADER
        // ---------------------------------------------------------

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val backButton = TextView(this).apply {
            text = "‹"
            textSize = 38f
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
                52,
                52
            )
        )

        val title = TextView(this).apply {
            text = "CAR FUEL CALCULATOR"
            textSize = 23f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.035f
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
                bottomMargin = 4
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
                bottomMargin = 30
            }
        )

        // ---------------------------------------------------------
        // TRIP PARAMETERS
        // ---------------------------------------------------------

        val sectionTitle = TextView(this).apply {
            text = "TRIP PARAMETERS"
            textSize = 12f
            setTextColor(muted)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.14f
        }

        root.addView(
            sectionTitle,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
        )

        // ---------------------------------------------------------
        // DISTANCE CARD
        // ---------------------------------------------------------

        val distanceCard = createTallCard()

        distanceCard.addView(
            createFieldLabel("DISTANCE"),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val distanceField = createTallInput(
            initialValue = "300",
            unit = "KM"
        )

        distanceInput = distanceField.first

        distanceCard.addView(
            distanceField.second,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                86
            ).apply {
                topMargin = 16
            }
        )

        root.addView(
            distanceCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                190
            ).apply {
                bottomMargin = 14
            }
        )

        // ---------------------------------------------------------
        // FUEL TYPE + PRICE
        // ---------------------------------------------------------

        val fuelRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val fuelTypeCard = createTallCard()

        fuelTypeCard.addView(
            createFieldLabel("⛽  FUEL TYPE"),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val fuelSpinner = Spinner(this).apply {
            background = roundedBackground(
                inputColor,
                16f
            )

            adapter = ArrayAdapter(
                this@CarFuelCalculatorActivity,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf(
                    "Diesel",
                    "Petrol"
                )
            )
        }

        fuelTypeCard.addView(
            fuelSpinner,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                86
            ).apply {
                topMargin = 16
            }
        )

        fuelRow.addView(
            fuelTypeCard,
            LinearLayout.LayoutParams(
                0,
                190,
                1f
            ).apply {
                marginEnd = 7
            }
        )

        val priceCard = createTallCard()

        priceCard.addView(
            createFieldLabel("€  PRICE / L"),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val priceField = createTallInput(
            initialValue = "1.45",
            unit = "€/L"
        )

        priceInput = priceField.first

        priceCard.addView(
            priceField.second,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                86
            ).apply {
                topMargin = 16
            }
        )

        fuelRow.addView(
            priceCard,
            LinearLayout.LayoutParams(
                0,
                190,
                1f
            ).apply {
                marginStart = 7
            }
        )

        root.addView(
            fuelRow,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                190
            ).apply {
                bottomMargin = 14
            }
        )

        // ---------------------------------------------------------
        // CONSUMPTION CARD
        // ---------------------------------------------------------

        val consumptionCard = createTallCard()

        consumptionCard.addView(
            createFieldLabel("CONSUMPTION"),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val consumptionField = createTallInput(
            initialValue = "6.5",
            unit = "L / 100 KM"
        )

        consumptionInput = consumptionField.first

        consumptionCard.addView(
            consumptionField.second,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                86
            ).apply {
                topMargin = 16
            }
        )

        root.addView(
            consumptionCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                190
            ).apply {
                bottomMargin = 32
            }
        )

        // ---------------------------------------------------------
        // TRIP ESTIMATE TITLE
        // ---------------------------------------------------------

        val estimateTitle = TextView(this).apply {
            text = "TRIP ESTIMATE"
            textSize = 12f
            setTextColor(muted)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.14f
            gravity = Gravity.CENTER
        }

        root.addView(
            estimateTitle,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 14
            }
        )

        // ---------------------------------------------------------
        // HERO CARD
        // ---------------------------------------------------------

        val theme = AppearanceManager.getTheme(
            this@CarFuelCalculatorActivity
        )

        val heroCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(26, 24, 26, 26)

            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    theme.startColor,
                    theme.endColor
                )
            ).apply {
                cornerRadius = 26f

                setStroke(
                    1,
                    Color.argb(
                        165,
                        Color.red(theme.startColor),
                        Color.green(theme.startColor),
                        Color.blue(theme.startColor)
                    )
                )
            }

            elevation = 12f
        }

        val heroLabel = TextView(this).apply {
            text = "ESTIMATED COST"
            textSize = 11f
            setTextColor(Color.rgb(225, 235, 248))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.12f
        }

        heroCard.addView(
            heroLabel,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
        )

        estimatedCostValue = TextView(this).apply {
            text = "€28.28"
            textSize = 40f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = true
        }

        heroCard.addView(
            estimatedCostValue,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 5
            }
        )

        val estimateDescription = TextView(this).apply {
            text = "YOUR TRIP ESTIMATE"
            textSize = 10f
            setTextColor(Color.argb(220, 235, 242, 252))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.10f
        }

        heroCard.addView(
            estimateDescription,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 28
            }
        )

        val heroDivider = View(this).apply {
            setBackgroundColor(
                Color.argb(
                    100,
                    255,
                    255,
                    255
                )
            )
        }

        heroCard.addView(
            heroDivider,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                bottomMargin = 26
            }
        )

        // ---------------------------------------------------------
        // HERO METRICS
        // ---------------------------------------------------------

        val secondaryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val fuelColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val fuelLabel = TextView(this).apply {
            text = "FUEL NEEDED"
            textSize = 10f
            setTextColor(Color.rgb(215, 228, 244))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }

        fuelColumn.addView(
            fuelLabel,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        fuelNeededValue = TextView(this).apply {
            text = "19.50 L"
            textSize = 21f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 9, 0, 0)
        }

        fuelColumn.addView(
            fuelNeededValue,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        secondaryRow.addView(
            fuelColumn,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginEnd = 16
            }
        )

        val metricDivider = View(this).apply {
            setBackgroundColor(
                Color.argb(
                    75,
                    255,
                    255,
                    255
                )
            )
        }

        secondaryRow.addView(
            metricDivider,
            LinearLayout.LayoutParams(
                1,
                58
            )
        )

        val costColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val costLabel = TextView(this).apply {
            text = "COST / 100 KM"
            textSize = 10f
            setTextColor(Color.rgb(215, 228, 244))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }

        costColumn.addView(
            costLabel,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        costPer100Value = TextView(this).apply {
            text = "€9.43"
            textSize = 21f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 9, 0, 0)
        }

        costColumn.addView(
            costPer100Value,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        secondaryRow.addView(
            costColumn,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = 16
            }
        )

        heroCard.addView(
            secondaryRow,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            heroCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                310
            ).apply {
                bottomMargin = 10
            }
        )

        setContentView(scrollView)

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

    // -------------------------------------------------------------
    // CARD CREATION
    // -------------------------------------------------------------

    private fun createTallCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)

            background = roundedBackground(
                cardColor,
                22f
            )

            elevation = 4f
        }
    }

    private fun createFieldLabel(
        text: String
    ): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 11f
            setTextColor(muted)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
            setPadding(2, 0, 2, 0)
        }
    }

    private fun createTallInput(
        initialValue: String,
        unit: String
    ): Pair<EditText, LinearLayout> {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 0, 18, 0)

            background = roundedBackground(
                inputColor,
                16f
            )
        }

        val input = EditText(this).apply {
            inputType =
                InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL

            setText(initialValue)

            setTextColor(white)
            setHintTextColor(muted)

            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL
            setSingleLine(true)

            background = null

            setPadding(
                2,
                0,
                2,
                0
            )
        }

        container.addView(
            input,
            LinearLayout.LayoutParams(
                0,
                86,
                1f
            )
        )

        val unitText = TextView(this).apply {
            text = unit
            textSize = 13f
            setTextColor(muted)
            gravity = Gravity.CENTER_VERTICAL
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.04f
        }

        container.addView(
            unitText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                86
            )
        )

        return Pair(
            input,
            container
        )
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

    // -------------------------------------------------------------
    // CALCULATIONS
    // -------------------------------------------------------------

    private fun updateResults() {

        val distance =
            distanceInput.text
                .toString()
                .toDoubleOrNull()
                ?: 0.0

        val price =
            priceInput.text
                .toString()
                .toDoubleOrNull()
                ?: 0.0

        val consumption =
            consumptionInput.text
                .toString()
                .toDoubleOrNull()
                ?: 0.0

        val fuelNeeded =
            distance * consumption / 100.0

        val costPer100 =
            consumption * price

        val estimatedCost =
            fuelNeeded * price

        fuelNeededValue.text =
            String.format(
                Locale.US,
                "%.2f L",
                fuelNeeded
            )

        costPer100Value.text =
            String.format(
                Locale.US,
                "€%.2f",
                costPer100
            )

        estimatedCostValue.text =
            String.format(
                Locale.US,
                "€%.2f",
                estimatedCost
            )
    }

    // -------------------------------------------------------------
    // TEXT WATCHER
    // -------------------------------------------------------------

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
