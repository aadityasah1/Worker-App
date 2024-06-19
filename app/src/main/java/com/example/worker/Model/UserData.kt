package com.example.worker.Model

import android.media.Image

data class UserData(
    val name: String = "",
    val email: String = "",
    val profileImage: String = ""
){
    constructor(): this ("", "", "")
}
