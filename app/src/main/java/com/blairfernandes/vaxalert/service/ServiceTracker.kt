package com.blairfernandes.vaxalert.service

import android.content.Context
import android.content.SharedPreferences

enum class ServiceState {
    STARTED,
    STOPPED,
}

private const val name = "SEARCH_SERVICE_KEY"
private const val key = "SEARCH_SERVICE_STATE"

fun setServiceState(context: Context, state: ServiceState) {
    val sharedPrefs = getPreferences(context)
    sharedPrefs.edit().let {
        it.putString(key, state.name)
        it.apply()
    }
}

fun getServiceState(context: Context): ServiceState {
    val sharedPrefs = getPreferences(context)
    val value:String = sharedPrefs.getString(key, ServiceState.STOPPED.name)?: ServiceState.STOPPED.name
    return ServiceState.valueOf(value)
}

fun getPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(name, 0)
}