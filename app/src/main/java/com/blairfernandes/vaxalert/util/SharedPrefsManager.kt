package com.blairfernandes.vaxalert.util

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsManager(context: Context) {
    private val PREFSFILENAME = "vaxalertprefs"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFSFILENAME, Context.MODE_PRIVATE)

    private val AGEFILTERS = "ageFilters"
    private val DOSEFILTERS = "doseFilters"
    private val COSTFILTERS = "costFilters"
    private val VAXFILTERS = "vaxFilters"

    private val SEARCHBY = "searchBy"
    private val PIN = "pin"
    private val STATE = "state"
    private val DISTRICT = "district"

    var ageFilters: String
        get() = prefs.getString(AGEFILTERS, "{}")!!
        set(value) = prefs.edit().putString(AGEFILTERS, value).apply()

    var doseFilters: String
        get() = prefs.getString(DOSEFILTERS, "{}")!!
        set(value) = prefs.edit().putString(DOSEFILTERS, value).apply()

    var costFilters: String
        get() = prefs.getString(COSTFILTERS, "{}")!!
        set(value) = prefs.edit().putString(COSTFILTERS, value).apply()

    var vaxFilters: String
        get() = prefs.getString(VAXFILTERS, "")!!
        set(value) = prefs.edit().putString(VAXFILTERS, value).apply()

    var searchBy: Int
        get() = prefs.getInt(SEARCHBY, 0)
        set(value) = prefs.edit().putInt(SEARCHBY, value).apply()

    var pin: String
        get() = prefs.getString(PIN, "")!!
        set(value) = prefs.edit().putString(PIN, value).apply()

    var state: String
        get() = prefs.getString(STATE, "")!!
        set(value) = prefs.edit().putString(STATE, value).apply()

    var district: String
        get() = prefs.getString(DISTRICT, "")!!
        set(value) = prefs.edit().putString(DISTRICT, value).apply()
}