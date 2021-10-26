package com.blairfernandes.vaxalert.util

import com.blairfernandes.vaxalert.model.Center
import com.blairfernandes.vaxalert.model.Session
import org.json.JSONArray

class JsonParser {
    fun parseCentersJSON(
        centersArray: JSONArray,
        onComplete: (centers: List<Center>) -> Unit
    ) {
        val centers = ArrayList<Center>()
        for (i in 0 until centersArray.length()) {
            val centerJson = centersArray.getJSONObject(i)
            val name = centerJson.getString("name")
            val address = centerJson.getString("address")
            val stateName = centerJson.getString("state_name")
            val districtName = centerJson.getString("district_name")
            val pincode = centerJson.getString("pincode")
            val feeType = centerJson.getString("fee_type")

            val sessions = ArrayList<Session>()
            val sessionArray = centerJson.getJSONArray("sessions")
            for (j in 0 until sessionArray.length()) {
                val sessionJson = sessionArray.getJSONObject(j)
                val date = sessionJson.getString("date")
                val availableCapacity = sessionJson.getInt("available_capacity")
                val minAgeLimit = sessionJson.getInt("min_age_limit")
                val maxAgeLimit =
                    if (sessionJson.has("max_age_limit")) sessionJson.getInt("max_age_limit") else 0
                val allowAllAge =
                    if (sessionJson.has("allow_all_age")) sessionJson.getBoolean("allow_all_age") else false
                val vaccine = sessionJson.getString("vaccine")
                val availableCapacityDose1 = sessionJson.getInt("available_capacity_dose1")
                val availableCapacityDose2 = sessionJson.getInt("available_capacity_dose2")

                val session = Session(
                    date,
                    availableCapacity,
                    minAgeLimit,
                    maxAgeLimit,
                    allowAllAge,
                    vaccine,
                    availableCapacityDose1,
                    availableCapacityDose2
                )
                sessions.add(session)
            }
            val center =
                Center(name, address, stateName, districtName, pincode, feeType, sessions)
            centers.add(center)
        }
        //passing centers list to the callback method
        onComplete(centers)
    }
}