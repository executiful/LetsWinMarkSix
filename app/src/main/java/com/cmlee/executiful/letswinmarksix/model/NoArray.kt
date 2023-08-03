package com.cmlee.executiful.letswinmarksix.model

data class NoArray(val nos: IntArray) {
    val total get() = nos.sum()
}