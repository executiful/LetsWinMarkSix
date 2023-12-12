package com.cmlee.executiful.letswinmarksix.roomdb

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cmlee.executiful.letswinmarksix.model.NoArray
import com.cmlee.executiful.letswinmarksix.model.UnitPrice
import com.cmlee.executiful.letswinmarksix.model.WinningUnit
import java.util.Date

@Entity(indices = [Index(value = ["date"])])
data class DrawResult(
    @PrimaryKey val id: String,

    val date: Date,
    val no: NoArray,
    val sno: Int,
    val sbcode: String? = null,
    val sbnameE: String? = null,
    val sbnameC: String? = null,
    val inv: String? = null,
    val p1: UnitPrice?,
    val p1u: WinningUnit?,
    val p2: UnitPrice?,
    val p2u: WinningUnit?,
    val p3: UnitPrice?,
    val p3u: WinningUnit?,
    val p4: UnitPrice?,
    val p4u: WinningUnit?,
    val p5: UnitPrice?,
    val p5u: WinningUnit?,
    val p6: UnitPrice?,
    val p6u: WinningUnit?,
    val p7: UnitPrice?,
    val p7u: WinningUnit?
)
