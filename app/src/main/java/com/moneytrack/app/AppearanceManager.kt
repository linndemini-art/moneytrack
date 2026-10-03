package com.moneytrack.app

import android.content.Context
import android.graphics.Color

object AppearanceManager {

    private const val PREFS_NAME = "moneytrack_data"
    private const val KEY_ACCENT_COLOR = "appearance_accent_color"

    private val defaultAccentColor = Color.rgb(
        74,
        145,
        255
    )

    fun getAccentColor(
        context: Context
    ): Int {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        return preferences.getInt(
            KEY_ACCENT_COLOR,
            defaultAccentColor
        )
    }

    fun setAccentColor(
        context: Context,
        color: Int
    ) {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putInt(
                KEY_ACCENT_COLOR,
                color
            )
            .apply()
    }
}
