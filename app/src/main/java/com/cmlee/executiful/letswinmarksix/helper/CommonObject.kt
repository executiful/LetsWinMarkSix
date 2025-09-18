package com.cmlee.executiful.letswinmarksix.helper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

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
    fun validateNumbers(leg :Int, ban:Int):Boolean{
        if (ban > 5) {
            return false
        }
        if (leg + ban < 7) return (leg == 6)
        return true
    }
}