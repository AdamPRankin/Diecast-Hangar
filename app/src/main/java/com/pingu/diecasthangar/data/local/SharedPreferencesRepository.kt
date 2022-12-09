package com.pingu.diecasthangar.data.local

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel

class SharedPreferencesRepository(application: Application) : AndroidViewModel(application) {

    private val appPref: SharedPreferences =  application.getSharedPreferences("DIECAST_HANGAR", Context.MODE_PRIVATE)
    private val editor = appPref.edit()

    fun addDarkModePref(theme: String) {
        editor.putString("THEME", theme).apply()
    }

    fun getDarkModePref(): String? {
        return  appPref.getString("DARK_MODE","auto")
    }


}