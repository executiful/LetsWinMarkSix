package com.cmlee.executiful.letswinmarksix.helper

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.*
import java.lang.reflect.Type
import java.util.Date
/*
import androidx.room.TypeConverter
import java.util.Date
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
     }
    @TypeConverter
    fun toTimestamp(value: Date?): Long? {
        return value?.let { value.time }
    }
}
*/
@ProvidedTypeConverter
class DayYearConvert : JsonSerializer<Date>, JsonDeserializer<Date> {
//    class DY (val yd:String){ // "2020/01/02"
//        val toDY = yd.split('/').reversed().joinToString("/")
//    }
    @TypeConverter
    fun fromDY(dy: Date) :String{ // db string 2020/01/02
        val yd = sqlDate.format(dy)
        return yd
    }
    @TypeConverter
    fun toDY(yd:String):Date{
        return sqlDate.parse(yd)
    }
    override fun serialize(
        src: Date,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement {
//        TODO("Not yet implemented")
        return JsonPrimitive(jsonDate.format(src))
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): Date {
//        TODO("Not yet implemented")
        return jsonDate.parse(json.asString)//DY(json.asString)
    }
    companion object{
        @SuppressLint("SimpleDateFormat")
        val jsonDate = SimpleDateFormat("dd/MM/yyyy")

        @SuppressLint("SimpleDateFormat")
        val sqlDate = SimpleDateFormat("yyyy/MM/dd")

        @SuppressLint("SimpleDateFormat")
        val getJson = SimpleDateFormat("yyyyMMdd")
    }
}