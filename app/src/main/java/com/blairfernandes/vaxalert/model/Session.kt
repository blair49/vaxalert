package com.blairfernandes.vaxalert.model

class Session(val date:String, val available_capacity:Int, val min_age_limit:Int, val max_age_limit:Int, val allow_all_age:Boolean, val vaccine:String, val available_capacity_dose1:Int, val available_capacity_dose2:Int){
    /**
     * Returns true if this session's [available_capacity] > 0
     */
    val isAvailable: Boolean
        get() {
            return this.available_capacity > 0
        }

    /**
     * Returns true if this session's [available_capacity_dose1] > 0
     */
    val isDose1Available: Boolean
        get() {
            return this.available_capacity_dose1 > 0
        }

    /**
     * Returns true if this session's [available_capacity_dose2] > 0
     */
    val isDose2Available: Boolean
        get() {
            return this.available_capacity_dose2 > 0
        }



}

/*
    {
          "date": "28-07-2021",
          "available_capacity": 0,
          "min_age_limit": 18,
          "max_age_limit": 44,
          "allow_all_age": true, //true, only if age 18 & above
          "vaccine": "COVISHIELD",
          "available_capacity_dose1": 0,
          "available_capacity_dose2": 0
        }
* */