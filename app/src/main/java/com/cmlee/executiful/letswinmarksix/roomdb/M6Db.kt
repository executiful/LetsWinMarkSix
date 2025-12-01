package com.cmlee.executiful.letswinmarksix.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cmlee.executiful.letswinmarksix.helper.DayYearConverter
import com.cmlee.executiful.letswinmarksix.helper.NoArrayConverter
import com.cmlee.executiful.letswinmarksix.helper.UnitPriceConverter
import com.cmlee.executiful.letswinmarksix.helper.WinningUnitConverter

@Database(version = 1,
    entities = [DrawResult::class],
    views = [viewDrawYears::class],
    exportSchema = false)
@TypeConverters(/*LocalDateConverter::class, */DayYearConverter::class,
    UnitPriceConverter::class, WinningUnitConverter::class, NoArrayConverter::class)
abstract class M6Db : RoomDatabase() {
    abstract fun DrawResultDao(): DrawResultDao

    companion object {
        private var INSTANCE: M6Db? = null
//        fun dismiss(){
//            if(INSTANCE != null) {
//                INSTANCE?.close()
//                INSTANCE = null
//            }
//        }
        internal fun getDatabase(context: Context): M6Db {
            if (INSTANCE == null) {
                synchronized(M6Db::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            M6Db::class.java,
                            "m6.db3"
                        )
                            .createFromAsset("databases/m6.v4.db3")
                            .addTypeConverter(DayYearConverter())
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