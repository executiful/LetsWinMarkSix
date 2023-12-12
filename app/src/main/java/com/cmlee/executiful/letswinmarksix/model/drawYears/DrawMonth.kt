package com.cmlee.executiful.letswinmarksix.model.drawYears


import com.google.gson.annotations.SerializedName

data class DrawMonth(
    @SerializedName("banner")
    val banner: Banner,
    @SerializedName("drawDate")
    val drawDate: List<DrawDate>,
    @SerializedName("jackpotTxt")
    val jackpotTxt: JackpotTxt,
    @SerializedName("@month")
    val month: Int
)