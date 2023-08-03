package com.cmlee.executiful.letswinmarksix.helper

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.cmlee.executiful.letswinmarksix.model.UnitPrice
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.text.NumberFormat

@ProvidedTypeConverter
class UnitPriceConverter : JsonSerializer<UnitPrice>, JsonDeserializer<UnitPrice> {
    @TypeConverter
    fun fromUnitPrice(value: UnitPrice?): Long? {
        return value?.lvalue
    }

    @TypeConverter
    fun toUnitPrice(value: Long?): UnitPrice? {
        return if (value == null) null else UnitPrice(value)
    }

    companion object {
        val nbf = NumberFormat.getInstance()
    }

    override fun serialize(
        src: UnitPrice?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement {
        return JsonPrimitive(if (src == null) null else nbf.format(src.lvalue))
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): UnitPrice? {
        val amoundof = nbf.parse(json!!.asString)?.toLong()
        return if (amoundof == null) null else UnitPrice(amoundof)
    }
}