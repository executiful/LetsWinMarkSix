package com.cmlee.executiful.letswinmarksix.model.drawYears


import com.google.gson.annotations.SerializedName

data class DrawDate(
    @SerializedName("@date")
    val date: Int,
    @SerializedName("@draw")
    val draw: String,
    @SerializedName("@jackpot")
    val jackpot: String,
    @SerializedName("@preSell")
    val preSell: String
) {
    companion object{
        const val checked_value = "1"

    }
}