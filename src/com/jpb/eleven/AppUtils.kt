package com.jpb.eleven

import rikka.material.app.DayNightDelegate

object AppUtils {
    fun getNightMode(nightModeString: String): Int {
        return when (nightModeString) {
            Constants.DARK_MODE_OFF -> DayNightDelegate.MODE_NIGHT_NO
            Constants.DARK_MODE_ON -> DayNightDelegate.MODE_NIGHT_YES
            Constants.DARK_MODE_FOLLOW_SYSTEM -> DayNightDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> DayNightDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }
}