package com.blairfernandes.vaxalert.util

const val BASE_URL = "https://cdn-api.co-vin.in/api/v2"
const val URL_GET_STATES = "$BASE_URL/admin/location/states"
const val URL_GET_DISTRICTS = "$BASE_URL/admin/location/districts"
//const val URL_FIND_BY_PIN = "$BASE_URL/appointment/sessions/public/findByPin"
//const val URL_FIND_BY_DISTRICT = "$BASE_URL/appointment/sessions/public/findByDistrict"
const val URL_FIND_CALENDAR_BY_PIN = "$BASE_URL/appointment/sessions/public/calendarByPin"
const val URL_FIND_CALENDAR_BY_DISTRICT = "$BASE_URL/appointment/sessions/public/calendarByDistrict"

const val SEARCH_BY = "searchBy"
const val PIN = "PIN"
const val DISTRICT_ID = "districtId"

const val AGE_18_PLUS = "Age18Plus"
const val AGE_18_44 = "Age18-44"
const val AGE_45_PLUS = "Age45Plus"

const val COST_FREE = "costfree"
const val COST_PAID = "costpaid"

const val DOSE_1 = "dose1"
const val DOSE_2 = "dose2"

const val COVAXIN = "covaxin"
const val COVISHIELD = "covishield"
const val SPUTNIKV = "sputnikv"