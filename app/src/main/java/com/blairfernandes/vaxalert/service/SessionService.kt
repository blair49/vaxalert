package com.blairfernandes.vaxalert.service

import android.util.Log
import com.blairfernandes.vaxalert.util.URL_FIND_CALENDAR_BY_DISTRICT
import com.blairfernandes.vaxalert.util.URL_FIND_CALENDAR_BY_PIN
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Parameters
import java.text.SimpleDateFormat
import java.util.*

object SessionService {
    private const val TAG = "SessionService"
    /*fun findByPin(pin:String, complete:(Boolean) -> Unit){
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyy")
        val date = simpleDateFormat.format(Date())
        val params: Parameters = listOf("pincode" to pin, "date" to date)

        Fuel.get(URL_FIND_BY_PIN, params)
                .appendHeader("Accept-Language", "en_US")
                .response { _, response, _ ->
                    Log.i(TAG, response.toString())
                    if(response.statusCode == 200){
                        complete(true)
                    }
                    else{
                        complete(false)
                    }
                }
    }*/

    /*fun findByDistrict(districtId:Int, complete:(Boolean) -> Unit){
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyy")
        val date = simpleDateFormat.format(Date())
        val params: Parameters = listOf("district_id" to districtId, "date" to date)
        Fuel.get(URL_FIND_BY_DISTRICT, params)
                .appendHeader("Accept-Language", "en_US")
                .response { _, response, _ ->
                    Log.i(TAG, response.toString())
                    if(response.statusCode == 200){
                        complete(true)
                    }
                    else{
                        complete(false)
                    }
                }
    }*/

    fun findCalendarByPin(pin:String, complete:(Boolean, String) -> Unit){
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyy")
        val date = simpleDateFormat.format(Date())
        val params: Parameters = listOf("pincode" to pin, "date" to date)
        Fuel.get(URL_FIND_CALENDAR_BY_PIN, params)
            .appendHeader("Accept-Language", "en_US")
            .response { _, response, _ ->
                Log.i(TAG, response.toString())
                if(response.statusCode == 200){
                    val responseString = response.body().asString("application/json; charset=utf-8")
                    complete(true, responseString)
                }
                else{
                    complete(false, response.toString())
                }
            }
    }

    fun findCalendarByDistrict(districtId:Int, complete:(Boolean, String) -> Unit){
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyy")
        val date = simpleDateFormat.format(Date())
        val params: Parameters = listOf("district_id" to districtId, "date" to date)
        Fuel.get(URL_FIND_CALENDAR_BY_DISTRICT, params)
            .appendHeader("Accept-Language", "en_US")
            .response { _, response, _ ->
                Log.i(TAG, response.toString())
                if(response.statusCode == 200){
                    val responseString = response.body().asString("application/json; charset=utf-8")
                    complete(true, responseString)
                }
                else{
                    complete(false, response.toString())
                }
            }
    }

}