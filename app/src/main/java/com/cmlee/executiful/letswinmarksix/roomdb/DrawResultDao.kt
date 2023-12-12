package com.cmlee.executiful.letswinmarksix.roomdb

import androidx.room.*
import com.cmlee.executiful.letswinmarksix.helper.*

@Dao
@TypeConverters(/*LocalDateConverter::class, */DayYearConverter::class, UnitPriceConverter::class, WinningUnitConverter::class, NoArrayConverter::class)
interface DrawResultDao {
    @Query("SELECT * FROM DrawResult ORDER BY date DESC")
    fun getAll(): List<DrawResult>

    @Query("SELECT * FROM DrawResult ORDER BY date DESC limit 1")
    fun getLatest():DrawResult
    @Query("SELECT * FROM DrawResult WHERE p1 IS NOT NULL ORDER BY date DESC limit 1")
    fun getLatestNotNull():DrawResult

    @Query("SELECT * FROM DrawResult WHERE id = '00/081' LIMIT 1")
    fun find(): DrawResult

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrIgnore(vararg drawResults: DrawResult)

//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    fun _insertOrIgnoreAll(vararg drawResults: DrawResult)

    @Update
    fun update(vararg drawResults: DrawResult)

    @Delete
    fun delete(vararg drawResults: DrawResult)
    companion object{
        const val latestID = ""
    }
}