package com.cmlee.executiful.letswinmarksix.model

import android.graphics.Color
import java.io.Serializable


data class NumStat(val num: Int, val idx: Int, var times: Int = 0, var since: Int = -1) :
    Serializable {
//    var stat1 = 0
//    var stat2 = -1

    var status = NUMSTATUS.UNSEL

    enum class NUMSTATUS(val i:Int) {
        LEG(111),
        BANKER(222),
        UNSEL(0)
    }

    val numString get() = if (status == NUMSTATUS.UNSEL) if (num % 2 == 0) "雙" else "單" else "$num"
    override fun toString(): String {
        return "︿@{System.lineSeparator()}${num}${System.lineSeparator()}﹀"
//        return "︿${System.lineSeparator()}${num}${System.lineSeparator()}﹀"
    }

    companion object {
        fun Int.BallColor() =
            when ((this % 2 + this) % 3) {
                0 -> Color.GREEN
                1 -> Color.BLUE
                else -> Color.RED
            }
    }
}