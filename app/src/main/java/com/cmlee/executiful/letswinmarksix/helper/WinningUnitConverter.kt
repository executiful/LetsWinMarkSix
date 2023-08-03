package com.cmlee.executiful.letswinmarksix.helper

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.cmlee.executiful.letswinmarksix.model.WinningUnit
import com.google.gson.*
import java.lang.reflect.Type
import java.text.DecimalFormat

@ProvidedTypeConverter
class WinningUnitConverter : JsonSerializer<WinningUnit>, JsonDeserializer<WinningUnit> {
    @TypeConverter
    fun fromWinningUnit(value: WinningUnit?): Float? {
        return value?.fvalue
    }

    @TypeConverter
    fun toWinningUnit(value: Float?): WinningUnit? {
        return if (value == null) null else WinningUnit(value)
    }

    companion object {
        val nbf: DecimalFormat = DecimalFormat("#,##0.0#")
    }

    override fun serialize(
        src: WinningUnit?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement {
        return JsonPrimitive(if (src == null) null else nbf.format(src.fvalue))
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): WinningUnit? {
        val amoundof = nbf.parse(json!!.asString)?.toFloat()
        return if (amoundof == null) null else WinningUnit(amoundof)
    }
}