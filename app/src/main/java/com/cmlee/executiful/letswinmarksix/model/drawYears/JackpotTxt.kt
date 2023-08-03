package com.cmlee.executiful.letswinmarksix.model.drawYears


import com.google.gson.annotations.SerializedName

data class JackpotTxt(
    @SerializedName("subTitle")
    val subTitle: List<String>,
    @SerializedName("title")
    val title: String,
    @SerializedName("txt")
    val txt: List<String>
)