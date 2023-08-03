package com.cmlee.executiful.letswinmarksix.helper

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.cmlee.executiful.letswinmarksix.model.NoArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

@ProvidedTypeConverter
class NoArrayConverter : JsonSerializer<NoArray>, JsonDeserializer<NoArray> {
    @TypeConverter
    fun toNoArray(value: String): NoArray {
        return NoArray(value.split(NosSep).map { Integer.parseInt(it) }.toIntArray())
    }

    @TypeConverter
    fun fromNoArray(value: NoArray): String {
        return value.nos.joinToString(NosSep)
    }


    override fun serialize(
        src: NoArray,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement {
        return JsonPrimitive( src.nos.joinToString(NosSep))
    }
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): NoArray {
        return toNoArray(json.asString)
    }

    companion object {
        const val NosSep = "+"
    }
}