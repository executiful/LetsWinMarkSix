package com.cmlee.executiful.letswinmarksix.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cmlee.executiful.letswinmarksix.helper.DayYearConvert
import com.cmlee.executiful.letswinmarksix.helper.NoArrayConverter
import com.cmlee.executiful.letswinmarksix.helper.UnitPriceConverter
import com.cmlee.executiful.letswinmarksix.helper.WinningUnitConverter

@Database(version = 1,
    entities = [DrawResult::class],
    views = [viewDrawYears::class],
    exportSchema = false)
@TypeConverters(/*LocalDateConverter::class, */DayYearConvert::class,
    UnitPriceConverter::class, WinningUnitConverter::class, NoArrayConverter::class)
abstract class M6Db : RoomDatabase() {
    abstract fun DrawResultDao(): DrawResultDao

    companion object {
        private var INSTANCE: M6Db? = null
        internal fun getDatabase(context: Context): M6Db {
            if (INSTANCE == null) {
                synchronized(M6Db::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            M6Db::class.java,
                            "m6.7.sqlite.db3"
                        )
                            .addTypeConverter(DayYearConvert())
                            //.addTypeConverter(LocalDateConverter())
                            .addTypeConverter(UnitPriceConverter())
                            .addTypeConverter(WinningUnitConverter())
                            .addTypeConverter(NoArrayConverter())
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }
    }
}