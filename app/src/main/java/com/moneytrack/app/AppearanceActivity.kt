package com.moneytrack.app

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class AppearanceActivity : Activity() {

    private val backgroundColor = Color.rgb(
        7,
        8,
        12
    )

    private val cardColor = Color.rgb(
        20,
        22,
        29
    )

    private val surfaceColor = Color.rgb(
        28,
        31,
        40
    )

    private val white = Color.WHITE

    private val secondary = Color.rgb(
        190,
        194,
        204
    )

    private val muted = Color.rgb(
        125,
        130,
        142
    )

    private lateinit var themesContainer: LinearLayout

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        buildScreen()
    }

    private fun buildScreen() {

        val root = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            setBackgroundColor(
                backgroundColor
            )

            setPadding(
                dp(16),
                dp(20),
                dp(16),
                dp(24)
            )
        }

        val title = TextView(this).apply {

            text = "APPEARANCE"

            textSize = 24f

            setTextColor(
                white
            )

            typeface =
                Typeface.DEFAULT_BOLD

            gravity =
                Gravity.CENTER_VERTICAL

            setPadding(
    dp(24),
    dp(92),
    dp(24),
    dp(24)
)

}

        root.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val subtitle = TextView(this).apply {

            text = "Choose your color theme"

            textSize = 14f

            setTextColor(
                muted
            )

            setPadding(
                0,
                0,
                0,
                dp(20)
            )
        }

        root.addView(
            subtitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        themesContainer =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL
            }

        root.addView(
            themesContainer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        addTheme(
            "Blue",
            AppearanceManager.BLUE
        )

        addTheme(
            "Amber",
            AppearanceManager.AMBER
        )

        addTheme(
            "Green",
            AppearanceManager.GREEN
        )

        addTheme(
            "Purple",
            AppearanceManager.PURPLE
        )

        addTheme(
            "Red",
            AppearanceManager.RED
        )

        addTheme(
            "Neon Blue",
            AppearanceManager.NEON_BLUE
        )

        addTheme(
            "Ocean Gold",
            AppearanceManager.OCEAN_GOLD
        )

        addTheme(
            "Royal Lavender",
            AppearanceManager.ROYAL_LAVENDER
        )

        addTheme(
            "Rose Sunset",
            AppearanceManager.ROSE_SUNSET
        )

        val scrollView = ScrollView(this).apply {
    addView(root)
}

setContentView(
    scrollView
)

}

    private fun addTheme(
        name: String,
        themeName: String
    ) {

        val theme =
            getThemeForPreview(
                themeName
            )

        val themeCard =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    dp(16),
                    dp(14),
                    dp(16),
                    dp(14)
                )

                background =
                    roundedBackground(
                        cardColor,
                        dp(16).toFloat()
                    )

                isClickable = true

                isFocusable = true
            }

        val preview =
            View(this).apply {

                background =
                    gradientBackground(
                        theme.startColor,
                        theme.endColor
                    )
            }

        themeCard.addView(
            preview,
            LinearLayout.LayoutParams(
                dp(82),
                dp(48)
            ).apply {

                marginEnd =
                    dp(16)
            }
        )

        val nameText =
            TextView(this).apply {

                text = name

                textSize = 16f

                setTextColor(
                    white
                )

                typeface =
                    Typeface.DEFAULT_BOLD

                layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
            }

        themeCard.addView(
            nameText
        )

        val selectedText =
            TextView(this).apply {

                text = "SELECTED"

                textSize = 11f

                setTextColor(
                    theme.accentColor
                )

                typeface =
                    Typeface.DEFAULT_BOLD

                visibility =
                    if (
                        AppearanceManager.getThemeName(
                            this@AppearanceActivity
                        ) == themeName
                    ) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
            }

        themeCard.addView(
            selectedText
        )

        themeCard.setOnClickListener {

            AppearanceManager.setTheme(
                this,
                themeName
            )

            refreshSelection()

            themeCard.postDelayed(
                {
                    finish()
                },
                120
            )
        }

        themeCard.tag =
            ThemeSelection(
                themeName,
                selectedText
            )

        themesContainer.addView(
            themeCard,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(76)
            ).apply {

                bottomMargin =
                    dp(10)
            }
        )
    }

    private fun refreshSelection() {

        val selectedTheme =
            AppearanceManager.getThemeName(
                this
            )

        for (
            index in 0 until themesContainer.childCount
        ) {

            val card =
                themesContainer.getChildAt(
                    index
                )

            val selection =
                card.tag as ThemeSelection

            selection.selectedText.visibility =
                if (
                    selection.themeName ==
                    selectedTheme
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    private fun getThemeForPreview(
        themeName: String
    ): AppearanceManager.Theme {

        return when (themeName) {

            AppearanceManager.AMBER ->
                AppearanceManager.Theme(
                    startColor = Color.rgb(
                        120,
                        55,
                        8
                    ),
                    endColor = Color.rgb(
                        255,
                        166,
                        52
                    ),
                    accentColor = Color.rgb(
                        255,
                        166,
                        52
                    )
                )

            AppearanceManager.GREEN ->
                AppearanceManager.Theme(
                    startColor = Color.rgb(
                        12,
                        72,
                        38
                    ),
                    endColor = Color.rgb(
                        50,
                        190,
                        105
                    ),
                    accentColor = Color.rgb(
                        72,
                        190,
                        110
                    )
                )

            AppearanceManager.PURPLE ->
                AppearanceManager.Theme(
                    startColor = Color.rgb(
                        55,
                        20,
                        90
                    ),
                    endColor = Color.rgb(
                        145,
                        75,
                        220
                    ),
                    accentColor = Color.rgb(
                        165,
                        95,
                        240
                    )
                )

            AppearanceManager.RED ->
                AppearanceManager.Theme(
                    startColor = Color.rgb(
                        95,
                        18,
                        24
                    ),
                    endColor = Color.rgb(
                        220,
                        55,
                        65
                    ),
                    accentColor = Color.rgb(
                        245,
                        75,
                        85
                    )
                )

            AppearanceManager.NEON_BLUE ->
                AppearanceManager.Theme(
                    startColor = Color.rgb(
                        5,
                        35,
                        95
                    ),
                    endColor = Color.rgb(
                        0,
                        180,
                        255
                    ),
                    accentColor = Color.rgb(
                        50,
                        200,
                        255
                    )
                )

            AppearanceManager.OCEAN_GOLD ->
                AppearanceManager.Theme(
                    startColor = Color.rgb(
                        0,
                        124,
                        190
                    ),
                    endColor = Color.rgb(
                        255,
                        247,
                        174
                    ),
                    accentColor = Color.rgb(
                        255,
                        247,
                        174
                    )
                )

            AppearanceManager.ROYAL_LAVENDER ->
                AppearanceManager.Theme(
                    startColor = Color.rgb(
                        75,
                        8,
                        109
                    ),
                    endColor = Color.rgb(
                        172,
                        192,
                        254
                    ),
                    accentColor = Color.rgb(
                        172,
                        192,
                        254
                    )
                )

            AppearanceManager.ROSE_SUNSET ->
                AppearanceManager.Theme(
                    startColor = Color.rgb(
                        215,
                        65,
                        119
                    ),
                    endColor = Color.rgb(
                        255,
                        233,
                        138
                    ),
                    accentColor = Color.rgb(
                        255,
                        233,
                        138
                    )
                )

            else ->
                AppearanceManager.Theme(
                    startColor = Color.rgb(
                        18,
                        29,
                        52
                    ),
                    endColor = Color.rgb(
                        24,
                        67,
                        125
                    ),
                    accentColor = Color.rgb(
                        74,
                        145,
                        255
                    )
                )
        }
    }

    private fun gradientBackground(
        startColor: Int,
        endColor: Int
    ): GradientDrawable {

        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                startColor,
                endColor
            )
        ).apply {

            cornerRadius =
                dp(14).toFloat()
        }
    }

    private fun roundedBackground(
        color: Int,
        radius: Float
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(
                color
            )

            cornerRadius =
                radius
        }
    }

    private fun dp(
        value: Int
    ): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }

    private data class ThemeSelection(
        val themeName: String,
        val selectedText: TextView
    )
}
