package com.cmlee.executiful.letswinmarksix.model.drawYears


import com.google.gson.annotations.SerializedName

data class DrawYearItem(
    @SerializedName("drawMonth")
    val drawMonth: List<DrawMonth>,
    @SerializedName("@year")
    val year: String
)