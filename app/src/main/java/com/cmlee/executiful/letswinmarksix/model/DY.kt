package com.cmlee.executiful.letswinmarksix.model

data class DY(val dy:String){ // "2020/01/02"
    val toYD = dy//.split('/').reversed().joinToString("/")
}