package com.blairfernandes.vaxalert.model

class District(val districtId:Int, val districtName:String){
    override fun toString(): String {
        return districtName
    }
}