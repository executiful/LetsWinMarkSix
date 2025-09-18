package com.cmlee.executiful.letswinmarksix.helper

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.cmlee.executiful.letswinmarksix.CameraScanActivity
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResultDao
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResultRepository
import com.cmlee.executiful.letswinmarksix.roomdb.M6Db

class AppContainer(private val context: Context) {
    private val database: M6Db by lazy {
        M6Db.getDatabase(context)
    }
    private val drawResultDao: DrawResultDao by lazy {
        database.DrawResultDao()
    }
    val sharedFile : SharedPreferences by lazy {
        context.getSharedPreferences(
            CameraScanActivity.OCR_TICKETS,
            MODE_PRIVATE
        )
    }
    val drawResultRepository: DrawResultRepository by lazy {
        DrawResultRepository(drawResultDao)
    }
}