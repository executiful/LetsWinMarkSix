package com.cmlee.executiful.letswinmarksix.helper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object CommonObject {
    const val NAME_NUM_GRP = "NUM_GRP"
    const val NAME_ORDER = "ORDER"
    const val KEY_SELECTED = "LEG_BANKER_IDX"
    const val KEY_ORDER = "NUMBER_ORDER"
    const val KEY_BLIND = "BLIND_BOOL"
    const val KEY_GROUP = "GROUP_BOOL"
    const val NAME_ENTRIES = "ENTRIES"
    const val TICKETRESULT = "TICKET_NUMBERS"
    const val TICKETSTRING = "TICKET_STRING"
    const val FD_WAIT_DIALOG = "Wait_Dialog"
    const val iso_date_format = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    fun bitmapToBase64(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val byteArray = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
//    fun SavedDateFormat(timeMillis:Int): Date {
//        SimpleDateFormat("ddMMMyy h:mm:ss", Locale.getDefault())
//    }

    fun validateNumbers(leg :Int, ban:Int):Boolean{
        if (ban > 5) {
            return false
        }
        if (leg + ban < 7) return (leg == 6)
        return true
    }

    private const val UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val DISPLAY_FORMAT = "yyyy-MM-dd HH:mm:ss z"
    const val HONG_KONG_TIMEZONE = "Asia/Hong_Kong"
    /**
     * Save current time as UTC ISO string
     */
    fun saveUtcTime(): String {
        return try {
            val formatter = SimpleDateFormat(UTC_FORMAT, Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            formatter.format(Date())
        } catch (e: Exception) {
            System.currentTimeMillis().toString() // Fallback to timestamp
        }
    }

    /**
     * Convert UTC string to local Date
     */
    fun utcToLocal(utcString: String): Date? {
        return try {
            val formatter = SimpleDateFormat(UTC_FORMAT, Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            formatter.parse(utcString)
        } catch (e: Exception) {
            // Try parsing as timestamp
            try {
                Date(utcString.toLong())
            } catch (e: NumberFormatException) {
                null
            }
        }
    }

    /**
     * Format Date for display in local timezone
     */
    fun formatForDisplay(date: Date): String {
        val formatter = SimpleDateFormat(DISPLAY_FORMAT, Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Get current local time as formatted string
     */
    fun getCurrentLocalTimeFormatted(): String {
        return formatForDisplay(Date())
    }
    const val TAG_DATE = "testing hong kong date"
    /**
     * Get current timestamp
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
    fun getHKInstance(): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone(HONG_KONG_TIMEZONE))
    }
    fun dateInTimezone(timeString: String, pattern: String = DISPLAY_FORMAT): Calendar?{
        return try{
//            val tz = TimeZone.getTimeZone(timezone)
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            val dateOf = formatter.parse(timeString)
            if(dateOf==null) null
            else {
                val instance = Calendar.getInstance()
                instance.time = dateOf
                instance
            }
        } catch (e :Exception){
            Log.d(TAG_DATE, e.message?:timeString)
            null
        }


    }
    /**
     * Convert time between timezones
     */
    fun convertTimeBetweenTimezones(
        timeString: String,
        fromTimezone: String=HONG_KONG_TIMEZONE,
        toTimezone: String=fromTimezone,
        pattern: String = "yyyy-MM-dd HH:mm:ss"
    ): String {
        return try {
            val fromZone = TimeZone.getTimeZone(fromTimezone)
            val toZone = TimeZone.getTimeZone(toTimezone)

            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            formatter.timeZone = fromZone
            val date = formatter.parse(timeString)

            formatter.timeZone = toZone
            formatter.format(date)
        } catch (e: Exception) {
            "Conversion failed: ${e.message}"
        }
    }

    /**
     * Get current time in specific timezone
     */
    fun getCurrentTimeInTimezone(timezoneId: String=HONG_KONG_TIMEZONE): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone(timezoneId)
            formatter.format(Date())
        } catch (e: Exception) {
            "Invalid timezone: $timezoneId"
        }
    }
//    fun SharedPreferences.getTimeInHongKong(keyName:String) : Calendar? {
////        val isoFormat = SimpleDateFormat(iso_date_time_format, Locale.getDefault())
//        getString(keyName, null)?.let {
//            return try {
//                isoFormat.parse(it).toCalendar()
//            } catch (e: Exception) {
//                return try {
//                    sdf_now.parse(it).toCalendar()
//                } catch (e: Exception) {
//                    null
//                }
//            }
//        }
//        return null
//    }

}