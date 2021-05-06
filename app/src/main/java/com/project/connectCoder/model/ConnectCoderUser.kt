package com.project.connectCoder.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConnectCoderUser(
    val uid: String = "",
    val name: String = "",
    val userName: String = "",
    val email: String = "",
    val profession: String = "",
    val bio: String = "",
    val profile: String = ""
) : Parcelable