package com.cmlee.executiful.letswinmarksix.helper

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
    fun validateNumbers(leg :Int, ban:Int):Boolean{
        if (ban > 5) {
            return false
        }
        if (leg + ban < 7) return (leg == 6)
        return true
    }
}