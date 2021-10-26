package com.blairfernandes.vaxalert.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SessionDetails(
    val centerName: String,
    val centerAddress: String,
    val cost: String,
    val date: String,
    val age: String,
    val vaccine: String,
    val available_capacity_dose1: Int,
    val available_capacity_dose2: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.let { parcel ->
            parcel.writeString(centerName)
            parcel.writeString(centerAddress)
            parcel.writeString(cost)
            parcel.writeString(date)
            parcel.writeString(age)
            parcel.writeString(vaccine)
            parcel.writeInt(available_capacity_dose1)
            parcel.writeInt(available_capacity_dose2)
        }
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<SessionDetails>
        {
            override fun createFromParcel(source: Parcel?): SessionDetails? {
                return source?.let { SessionDetails(it) }
            }

            override fun newArray(size: Int): Array<SessionDetails?> {
                return arrayOfNulls(size)
            }
        }
    }
}
