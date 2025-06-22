package com.cmlee.executiful.letswinmarksix.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import androidx.room.Update
import com.cmlee.executiful.letswinmarksix.helper.DayYearConverter
import com.cmlee.executiful.letswinmarksix.helper.NoArrayConverter
import com.cmlee.executiful.letswinmarksix.helper.UnitPriceConverter
import com.cmlee.executiful.letswinmarksix.helper.WinningUnitConverter

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

//    @Query("SELECT * FROM DrawResult WHERE date >= (SELECT date FROM DrawResult WHERE id like :year||'/'||:code OR ( id like :year || '/%' and sbcode=:code)) LIMIT :count")
    @Query("SELECT * FROM DrawResult WHERE date >= \n" +
            "(SELECT date FROM DrawResult WHERE (id = :year || '/' || :code OR id LIKE :year || '/%' and sbcode=:code) AND id IN (SELECT id FROM DrawResult ORDER BY date DESC LIMIT 60))  LIMIT :count")
    fun checkDrawBy(year:String, code:String, count:Int):List<DrawResult>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(vararg drawResults: DrawResult)

    @Query("SELECT last_insert_rowid()")
    fun lastInsertRowId():Int

    @Query("SELECT id FROM DrawResult WHERE rowid = last_insert_rowid()")
    fun lastInsertId() : String
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    fun _insertOrIgnoreAll(vararg drawResults: DrawResult)

    @Update
    fun update(vararg drawResults: DrawResult)

    @Delete
    fun delete(vararg drawResults: DrawResult)
//    companion object{
//        const val latestID = ""
//    }
}