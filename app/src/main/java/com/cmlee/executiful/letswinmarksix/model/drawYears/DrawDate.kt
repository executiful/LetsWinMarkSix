package com.cmlee.executiful.letswinmarksix.model.drawYears


import com.google.gson.annotations.SerializedName

data class DrawDate(
    @SerializedName("@date")
    val date: String,
    @SerializedName("@draw")
    val draw: String,
    @SerializedName("@jackpot")
    val jackpot: String,
    @SerializedName("@preSell")
    val preSell: String
)