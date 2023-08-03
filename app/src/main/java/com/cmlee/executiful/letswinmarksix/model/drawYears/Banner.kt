package com.cmlee.executiful.letswinmarksix.model.drawYears


import com.google.gson.annotations.SerializedName

data class Banner(
    @SerializedName("image")
    val image: String,
    @SerializedName("link")
    val link: String,
    @SerializedName("target")
    val target: String
)