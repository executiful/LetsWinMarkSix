package com.cmlee.executiful.letswinmarksix.model.drawYears


import com.cmlee.executiful.letswinmarksix.helper.DrawMonthConverter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

//@JsonAdapter(DrawMonthConverter::class)
data class DrawYearItem(
    @SerializedName("drawMonth")
    val drawMonth: List<DrawMonth>,
    @SerializedName("@year")
    val year: Int
)