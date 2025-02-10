package com.cmlee.executiful.letswinmarksix.model

import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.emdash

data class WinningUnit(val fvalue:Float?){
    override fun toString(): String {
        return (if(fvalue==0F) "$emdash" else "$fvalue").padStart(6, ' ')
    }
}
