package com.cmlee.executiful.letswinmarksix.helper

import com.cmlee.executiful.letswinmarksix.model.drawYears.DrawMonth
import com.cmlee.executiful.letswinmarksix.model.drawYears.DrawYearItem
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class DrawMonthConverter : JsonDeserializer<DrawYearItem?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): DrawYearItem? {
        json?.asJsonObject?.apply {
            if (has(DrawYearItem::drawMonth.name)) {
                get(DrawYearItem::drawMonth.name).also {
                    if (it.isJsonArray.not()) {
                        val m1: DrawMonth = context?.deserialize(it, DrawMonth::class.java)!!
                        return DrawYearItem(
                            listOf(m1),
                            json.asJsonObject.get("@year").asInt
                        )
                    }
                }
            }
        }
        return Gson().fromJson(json, typeOfT)
    }
}