package com.jpb.eleven

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.jpb.eleven.AppUtils
import com.jpb.eleven.SPDelegates
import com.jpb.eleven.SPUtils
import org.lineageos.eleven.BuildConfig
import java.util.Locale

const val SP_NAME = "${BuildConfig.APPLICATION_ID}_preferences"

object GlobalValues {

  private fun getPreferences(): SharedPreferences {
    return SPUtils.sp
  }

  var darkMode: String by SPDelegates(Constants.PREF_DARK_MODE, Constants.DARK_MODE_FOLLOW_SYSTEM)

  var rengeTheme: Boolean by SPDelegates(Constants.RENGE_THEME, false)

  var md3Theme: Boolean by SPDelegates(Constants.PREF_MD3, false)

  var locale: Locale = Locale.getDefault()
    get() {
      val tag = getPreferences().getString(Constants.PREF_LOCALE, null)
      if (tag.isNullOrEmpty() || "SYSTEM" == tag) {
        return Locale.getDefault()
      }
      return Locale.forLanguageTag(tag)
    }
    set(value) {
      field = value
      getPreferences().edit { putString(Constants.PREF_LOCALE, value.toLanguageTag()) }
    }
}
