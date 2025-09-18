package com.cmlee.executiful.letswinmarksix.roomdb

import java.util.Calendar

class DrawResultRepository(private val drawResultDao: DrawResultDao) {
    suspend fun getLatest():DrawResult{
        return drawResultDao.getLatest()
    }
    suspend fun getAll(): List<DrawResult>{
        return drawResultDao.getAll()
    }
    suspend fun getLatestNotNull():DrawResult{
        return drawResultDao.getLatestNotNull()
    }
    suspend fun insertOrReplace(vararg drawResults: DrawResult){
        drawResults.sortedByDescending { it.date }
        return drawResultDao.insertOrReplace(*drawResults.distinct().filter { it.date<=Calendar.getInstance().time }.toTypedArray())
    }
    suspend fun checkDrawBy(year:String, code:String, count:Int):List<DrawResult>{
        return drawResultDao.checkDrawBy(year,code, count)
    }
    suspend fun deleteLatest(){
        drawResultDao.getLatest().also { drawResultDao.delete(it) }
    }
}