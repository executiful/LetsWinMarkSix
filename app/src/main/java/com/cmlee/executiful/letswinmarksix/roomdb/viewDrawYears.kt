package com.cmlee.executiful.letswinmarksix.roomdb

import androidx.room.DatabaseView

@DatabaseView("SELECT substr(id, 1, 3) FROM drawresult GROUP BY substr(id, 1, 2)")
data class viewDrawYears(val yr: String)
