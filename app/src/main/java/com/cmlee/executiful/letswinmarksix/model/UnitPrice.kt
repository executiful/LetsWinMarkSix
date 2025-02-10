package com.cmlee.executiful.letswinmarksix.model

import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.dollar
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.emdash
import com.cmlee.executiful.letswinmarksix.helper.UnitPriceConverter.Companion.nbf

data class UnitPrice(val lvalue:Long){
    override fun toString(): String {
        return (if(lvalue==0L) "$emdash" else "$dollar${nbf.format(lvalue)}").padStart(13,' ')
    }
    val winner get() = lvalue!=0L
}
