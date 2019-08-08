package com.charity.travelmantics.ui.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var uid : String? = "",
    var name: String? = "",
    var password: String? = "",
    var email: String? = ""
)

@IgnoreExtraProperties
data class Destination(
    var city : String? = "",
    var price : Int? = 0,
    var description : String? = "",
    var filePath : String? = ""
)