package com.blairfernandes.vaxalert.service

import android.util.Log
import com.blairfernandes.vaxalert.model.District
import com.blairfernandes.vaxalert.model.State
import com.blairfernandes.vaxalert.util.URL_GET_DISTRICTS
import com.blairfernandes.vaxalert.util.URL_GET_STATES
import com.github.kittinunf.fuel.Fuel
import org.json.JSONException
import org.json.JSONObject

object LocationService {
    private const val TAG = "LocationService"

    var states = ArrayList<State>()
    var districts = ArrayList<District>()

    fun getStates(complete:(Boolean) -> Unit){
        states.clear()
        Fuel.get(URL_GET_STATES)
                .appendHeader("Accept-Language", "en_US")
                .response { request, response, _ ->
                    Log.i(TAG, request.toString())
                    Log.i(TAG, response.toString())
                    if(response.statusCode == 200){
                        try {
                            val responseString = response.body().asString("application/json; charset=utf-8")
                            val statesArray = JSONObject(responseString).getJSONArray("states")
                            for(i in 0 until statesArray.length()){
                                val state = statesArray.getJSONObject(i)
                                val stateId = state.getInt("state_id")
                                val stateName = state.getString("state_name")
                                val newState = State(stateId, stateName)
                                this.states.add(newState)
                            }
                            complete(true)
                        }
                        catch (e:JSONException){
                            Log.d(TAG, "Exception : ${e.localizedMessage}")
                            complete(false)
                        }

                    }
                    else{
                        complete(false)
                    }
                }
    }

    fun getDistricts(stateId: Int, complete:(Boolean) -> Unit){
        districts.clear()
        Fuel.get("$URL_GET_DISTRICTS/$stateId")
                .appendHeader("Accept-Language", "en_US")
                .response { request, response, _ ->
                    Log.i(TAG, request.toString())
                    Log.i(TAG, response.toString())
                    if(response.statusCode == 200){
                        try {
                            val responseString = response.body().asString("application/json; charset=utf-8")
                            val districtsArray = JSONObject(responseString).getJSONArray("districts")
                            for(i in 0 until districtsArray.length()){
                                val district = districtsArray.getJSONObject(i)
                                val districtId = district.getInt("district_id")
                                val districtName = district.getString("district_name")
                                val newDistrict = District(districtId, districtName)
                                this.districts.add(newDistrict)
                            }
                            complete(true)
                        }
                        catch (e:JSONException){
                            Log.d(TAG, "Exception : ${e.localizedMessage}")
                            complete(false)
                        }

                    }
                    else{
                        complete(false)
                    }
                }
    }
}