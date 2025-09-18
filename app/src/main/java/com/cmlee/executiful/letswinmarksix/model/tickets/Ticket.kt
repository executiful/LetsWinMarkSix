package com.cmlee.executiful.letswinmarksix.model.tickets

import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.bankers
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.legs
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.m6_sep_banker
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.m6_sep_num
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResult

typealias ticket_number_list = List<Pair<List<String>, List<String>>>
data class Ticket(
    val drawYear: String, val buyDate: String,
    val drawNo: String, val drawTotal: Float,
    val drawUnit: Float,
    val drawItemNumbers: ticket_number_list,
    val ocr: String = "",
    val draws: Int = 1
){
    val drawID get() = "$drawYear/$drawNo"
    fun checkAll( rs: DrawResult): List<List<Boolean>> {
        val nos = rs.no.nos.map{it.toString()}
        val sno = rs.sno.toString()
        return drawItemNumbers.map{ (legs, bankers)->
            val max1 = 6 - bankers.size
            val m6 = nos.intersect(bankers).plus(nos.intersect(legs).take(max1))
             nos.map { c -> c in m6 }.plus(sno in bankers || sno in legs)
        }
    }
    val numbersString get() = drawItemNumbers.joinToString("${System.lineSeparator()}/ ") { (legs, bans) ->
        listOf(
            bans.joinToString(m6_sep_num),
            legs.joinToString(m6_sep_num)
        )
            .filter { it.isNotEmpty() }
            .joinToString(m6_sep_banker)
    }
}
