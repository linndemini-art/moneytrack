package com.moneytrack.app

import android.content.Context
import android.graphics.Color

object AppearanceManager {

    private const val PREFS_NAME = "moneytrack_data"
    private const val KEY_THEME = "appearance_theme"

    const val BLUE = "blue"
    const val AMBER = "amber"
    const val GREEN = "green"
    const val PURPLE = "purple"
    const val RED = "red"
    const val NEON_BLUE = "neon_blue"

    data class Theme(
        val startColor: Int,
        val endColor: Int,
        val accentColor: Int
    )

    fun getThemeName(
        context: Context
    ): String {

        return context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .getString(
                KEY_THEME,
                BLUE
            )
            ?: BLUE
    }

    fun setTheme(
        context: Context,
        themeName: String
    ) {

        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                KEY_THEME,
                themeName
            )
            .apply()
    }

    fun getTheme(
        context: Context
    ): Theme {

        return when (
            getThemeName(context)
        ) {

            AMBER -> Theme(
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

            GREEN -> Theme(
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

            PURPLE -> Theme(
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

            RED -> Theme(
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

            NEON_BLUE -> Theme(
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

            else -> Theme(
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

    fun getAccentColor(
        context: Context
    ): Int {

        return getTheme(
            context
        ).accentColor
    }
}
