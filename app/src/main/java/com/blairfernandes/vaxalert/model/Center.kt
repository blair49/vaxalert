package com.blairfernandes.vaxalert.model

class Center(val name:String, val address:String, val state_name: String, val district_name: String, val pincode:String, val fee_type:String, val sessions:ArrayList<Session>)

/*
* {
      "name": "Wada RH",
      "address": "Wada RH",
      "state_name": "Maharashtra",
      "district_name": "Palghar",
      "pincode": 421303,
      "fee_type": "Free",
      "sessions": [
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
      ]
    }
* */