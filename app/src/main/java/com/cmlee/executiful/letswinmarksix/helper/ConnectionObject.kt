package com.cmlee.executiful.letswinmarksix.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.NetworkOnMainThreadException
import android.text.Html
import android.util.Log
import androidx.core.content.edit
import androidx.core.text.HtmlCompat
import com.cmlee.executiful.letswinmarksix.model.NoArray
import com.cmlee.executiful.letswinmarksix.model.UnitPrice
import com.cmlee.executiful.letswinmarksix.model.WinningUnit
import com.cmlee.executiful.letswinmarksix.model.drawYears.DrawDate
import com.cmlee.executiful.letswinmarksix.model.drawYears.DrawDate.Companion.checked_value
import com.cmlee.executiful.letswinmarksix.model.drawYears.DrawYear
import com.cmlee.executiful.letswinmarksix.model.drawYears.DrawYearItem
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResult
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResultArray
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResultDao
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResultRepository
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL
import java.net.UnknownHostException
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeoutException

object ConnectionObject {
    private const val TAG_DEBUG= "DEBUG :123:"
    private const val connectTO = 5
    private const val incTO = 2
    private const val readTO = 4
    private const val Scheme = "https"
    private const val Auth = "bet2.hkjc.com"
    private const val M6Path = "marksix"
    const val TAG_FIXTURES = "Fixtures"
    private const val TAG_STATISTICS = "Statistics"
    private const val TAG_JSON = "getJson"
    private const val KEY_FIXTURES = "DrawDateList"
    const val KEY_FIXTURES_UPDATE = "fixtures_save_datetime"
    const val indexTD = "|"
    const val indexTR = "#"
    fun Map<String, String>.toQuery():String{
        return this.map{ "${it.key}=${it.value}"}.joinToString("&")
    }
    const val TAG_INDEX = "NEXTDRAW"//"index"
    private const val KEY_NEXT_DRAW_DATE = "next_draw_date"
    const val KEY_NEXT = "next_draw_data"
    const val KEY_NEXT_UPDATE = "next_date_save_datetime"
    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyyMMdd")
    @SuppressLint("SimpleDateFormat")
    val sdf_now = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    val monthFmt = SimpleDateFormat("MMMM", Locale.CHINA)
    private const val iso_date_time_format = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    fun SharedPreferences.Editor.putDateTimeISO(keyName: String, instance: Calendar):SharedPreferences.Editor{
        val isoFormat = SimpleDateFormat(iso_date_time_format, Locale.getDefault())
        putString(keyName ,isoFormat.format(instance.time))
        return this
    }
    fun SharedPreferences.getDateTimeISO(keyName:String) : Calendar? {
        val isoFormat = SimpleDateFormat(iso_date_time_format, Locale.getDefault())
        getString(keyName, null)?.let {
            return try {
                isoFormat.parse(it).toCalendar()
            } catch (e: Exception) {
                return try {
                    sdf_now.parse(it).toCalendar()
                } catch (e: Exception) {
                    null
                }
            }
        }
        return null
    }

    fun SharedPreferences.getDateTimeISOFormat(keyName:String, def:String?=null) : String? {
        return try {
            getDateTimeISO(keyName)?.let {
                try {
                    sdf_now.format(it.time)
                } catch (e: Exception) {
                    def+1
                }
            }
        } catch (e: Exception) {
            def+2
        }
    }
    fun Calendar.clearTimePart(): Calendar {
        val y = get(Calendar.YEAR)
        val d = get(Calendar.DAY_OF_YEAR)
        clear()
        set(Calendar.YEAR, y)
        set(Calendar.DAY_OF_YEAR, d)
        return this
    }
    /*
    this date is after the specified date
    cp==>==>time==>==>cp==>
    this<====false===>c2<===true===>true
     */
    fun Calendar.isEarlyBy(c2:Calendar, field:Int, time: Int): Boolean {
        val cp = this.clone() as Calendar
        when (field) {
            Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR,
            Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND -> {
                cp.add(field, time)
                return c2.after(cp)
            }
            else -> throw IllegalArgumentException("Invalid field")
        }
    }
    fun Date?.toCalendar() :Calendar? {
        return this?.let {
            val c = Calendar.getInstance()
            c.time = it
            c
        }
    }
    private fun getJsonData(p:String, lang:Map<String, String>) : String? {
        val uristr = Uri.Builder().scheme(Scheme).authority(Auth).encodedPath("${M6Path}/$p.aspx")
            .encodedQuery(lang.toQuery()).build().toString()
        for(i in 1..3){
            try {
                val conn = URL(uristr).openConnection()

                with (conn as HttpURLConnection){
                    allowUserInteraction=false
                    instanceFollowRedirects=true
                    requestMethod="GET"
                    connectTimeout=(connectTO + incTO * i) * 500
                    readTimeout = (readTO+incTO *i)*500
                    connect()
                    if(responseCode== HTTP_OK && inputStream !=null){
                        val content = inputStream.readBytes()
                        inputStream.close()
                        disconnect()
                        return String(content)
                    }
                }
            } catch (e: Exception) {
                println("what ex ${e.message} ${e.javaClass.simpleName}")
                when(e){
                    is UnknownHostException->break
                    is NetworkOnMainThreadException->break
                    is TimeoutException->continue
                    else->continue
                }
            } finally {
            }
        }
        return null
    }
    private fun getJsoupDoc(p:String, lang:Map<String, String>):Document?{
        val uristr = Uri.Builder().scheme(Scheme).authority(Auth).encodedPath("${M6Path}/$p.aspx")
            .encodedQuery(lang.toQuery()).build().toString()
        for(i in 1..3){
            try {
                return Jsoup.connect((uristr)).timeout(1000 * (connectTO + readTO + i * incTO)).get()
            } catch (e: Exception) {
                println("what ex ${e.message} ${e.javaClass.simpleName}")
                when(e){
                    is UnknownHostException->break
                    is NetworkOnMainThreadException ->break
                    is TimeoutException->continue
                    else->continue
                }
            } finally {
            }
        }
        return null
    }
    private fun getJsoupDoc0():Document?{
        val uristr = Uri.Builder().scheme(Scheme).authority("bet.hkjc.com").appendPath("ch").appendPath("marksix").build().toString()
            for(i in 1..3){
                try{
                    return Jsoup.connect((uristr)).timeout(1000*(connectTO+ readTO+i* incTO))
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36").get()
                } catch (e:Exception){
                    println("what ex0 ${e.message} ${e.javaClass.simpleName}")
                    when(e){
                        is UnknownHostException->break
                        is TimeoutException->continue
                        else->continue
                    }
                }
            }
        return null
    }
    fun getLatestDrawResult(drawResult: DrawResult, processResults:(List<DrawResult>)->Unit) {
        val gson = GsonBuilder().setPrettyPrinting()//.registerTypeAdapter(LocalDate::class.java, LocalDateConverter())
            .registerTypeAdapter(Date::class.java, DayYearConverter())
            .registerTypeAdapter(UnitPrice::class.java, UnitPriceConverter())
            .registerTypeAdapter(WinningUnit::class.java, WinningUnitConverter())
            .registerTypeAdapter(NoArray::class.java, NoArrayConverter())
            .create()
        val ref = Calendar.getInstance()
        val today = Calendar.getInstance()
        ref.clear()
        ref.time = drawResult.date
//        println(lat.id)
        ref.add(Calendar.DATE, 1)
        var sd = sdf.format(ref.time)
        val arrResult = mutableListOf<DrawResult>()
//        println("ref time ${ref.time} today ${today.time}")
        while(today > ref) {
            ref.add(Calendar.MONTH, 3)
            val ed = sdf.format(ref.time)
            Log.d(TAG_JSON, "${System.currentTimeMillis()} $sd $ed")
//            withContext(Dispatchers.IO){
//            Thread{
                getJsonData(TAG_JSON, mapOf("sd" to sd, "ed" to ed, "sb" to "0"))?.let { str ->
                    try {
                        val text = Jsoup.parse(str).text()
                        if (text.isNullOrEmpty().not()) {
                            val tmp = gson.fromJson(text, DrawResultArray::class.java)
                            arrResult.addAll(tmp)
                        }
                    } catch (e: Exception) {
                        println("${e.message} what is wrong???")
                    }
                    println("$sd $ed ${arrResult.size} size")
                }
//            }.start()
//            }
            if(arrResult.isEmpty())
                break
            ref.add(Calendar.DATE, 1)
            sd = sdf.format(ref.time)
        }
        processResults(arrResult)
    }
    suspend fun getLatestResult(drawResultDao: DrawResultRepository):List<DrawResult> {
        val gson = GsonBuilder().setPrettyPrinting()//.registerTypeAdapter(LocalDate::class.java, LocalDateConverter())
            .registerTypeAdapter(Date::class.java, DayYearConverter())
            .registerTypeAdapter(UnitPrice::class.java, UnitPriceConverter())
            .registerTypeAdapter(WinningUnit::class.java, WinningUnitConverter())
            .registerTypeAdapter(NoArray::class.java, NoArrayConverter())
            .create()
        val ref = Calendar.getInstance()
        val today = Calendar.getInstance()
        ref.clear()
        val lat = drawResultDao.getLatestNotNull()
        ref.time = lat.date
//        println(lat.id)
        ref.add(Calendar.DATE, 1)
        var sd = sdf.format(ref.time)
        val arrResult = mutableListOf<DrawResult>()
//        println("ref time ${ref.time} today ${today.time}")
        while(today > ref) {
            ref.add(Calendar.MONTH, 3)
            val ed = sdf.format(ref.time)
            Log.d(TAG_JSON, "${System.currentTimeMillis()} $sd $ed")
            withContext(Dispatchers.IO){
                getJsonData(TAG_JSON, mapOf("sd" to sd, "ed" to ed, "sb" to "0"))?.let { str ->
                    try {
                        val text = Jsoup.parse(str).text()
                        if (text.isNullOrEmpty().not()) {
                            val tmp = gson.fromJson(text, DrawResultArray::class.java)
                            arrResult.addAll(tmp)
                        }
                    } catch (e: Exception) {
                        println("${e.message} what is wrong???")
                    }
                    println("$sd $ed ${arrResult.size} size")
                }
            }
            if(arrResult.isEmpty())
                break
            ref.add(Calendar.DATE, 1)
            sd = sdf.format(ref.time)
        }
        if(arrResult.isNotEmpty()) {
            arrResult.sortedByDescending { it.date }
            drawResultDao.insertOrReplace(*arrResult.distinct().filter { it.date<=Calendar.getInstance().time }.toTypedArray())
        }
        return arrResult
    }
    fun getScheduleAll(context: Context): List<Pair<Calendar, DrawDate>>{
        return getScheduleAll(context.getSharedPreferences(TAG_FIXTURES, MODE_PRIVATE))
    }
    fun getScheduleAll(schuPref: SharedPreferences): List<Pair<Calendar, DrawDate>>{
        val drawYearJsonString =
            schuPref.getString(KEY_FIXTURES, JSONArray().toString())
        val drawYear =
            GsonBuilder().registerTypeAdapter(DrawYearItem::class.java, DrawMonthConverter())
                .create().fromJson(drawYearJsonString, DrawYear::class.java)
        val flatMap = drawYear.flatMap { yy ->
            yy.drawMonth.flatMap { mm ->
                mm.drawDate.map { dd ->
                    val date = Calendar.getInstance()
                    date.clear()
                    date.set(yy.year, mm.month-1, dd.date)
                    date to dd
                }
            }
        }//.filter { it.first > today }
        return flatMap
    }
    @SuppressLint("SimpleDateFormat")
    fun getLatestDDate(schuPref: SharedPreferences): Pair<List<Pair<Calendar, DrawDate>>, List<Pair<Date, String>>> {
        val today = Calendar.getInstance()  //TODO  : 如何更新可能出現已下載的日期表之後有所改變，而不需經常訪問網址。
        today.clearTimePart()
//        println("first day of week ${today.time}")
        today.add(Calendar.DATE,-today.get(Calendar.DAY_OF_WEEK))
        val drawYearJsonString =
            schuPref.getString(KEY_FIXTURES, JSONArray().toString())
        val drawYear =
            GsonBuilder().registerTypeAdapter(DrawYearItem::class.java, DrawMonthConverter())
                .create().fromJson(drawYearJsonString, DrawYear::class.java)
        val dfm = SimpleDateFormat("dd-MM-yyyy")

        val flatMap = drawYear.flatMap { yy ->
            yy.drawMonth.flatMap { mm ->
                mm.drawDate.map { dd ->
                    val date = Calendar.getInstance()
                    date.clear()
                    date.set(yy.year, mm.month-1, dd.date)
                    date to dd
                }
            }
        }//.filter { it.first > today }
        val subtitle = drawYear.flatMap { yy ->
            yy.drawMonth.flatMap { mm ->
                mm.jackpotTxt.subTitle.filter {
                    try {
                        dfm.parse(it)!=null
                    } catch (e: Exception) {
                        false
                    }
                }.map { dfm.parse(it) to Html.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString() }
            }

        }
        return flatMap to subtitle
    }

    fun getLatestSchecule(context: Context): Pair<List<Pair<Calendar, DrawDate>>, List<Pair<Date, String>>> {
        val today = Calendar.getInstance()
//        val dom = today.get(Calendar.DAY_OF_MONTH)
//        today.set(Calendar.DAY_OF_MONTH, 17)
        val schuPref = context.getSharedPreferences(TAG_FIXTURES, MODE_PRIVATE)
        val commingDDate = getLatestDDate(schuPref)
        schuPref.getDateTimeISO(KEY_FIXTURES_UPDATE)?.let {
//            val lastupdate = sdf_now.parse(it).toCalendar()
            if(it.before(today)){
//                it.add(Calendar.DAY_OF_YEAR,1)
                it.add(Calendar.HOUR_OF_DAY,12)
                if(today.before(it))
                return commingDDate

            }
        }
        if(commingDDate.first.isEmpty() ||
            commingDDate.first.count {(it.second.draw==checked_value||it.second.preSell==checked_value)&& it.first>today } < 7) {
            getJsoupDoc(TAG_FIXTURES, mapOf("lang" to "ch")) ?.let { doc->
            val regex =
                Regex("\\s*var\\s*dataJson\\s*=\\s*(.*)\\s*;", RegexOption.DOT_MATCHES_ALL)
            doc.getElementsByTag("script").forEach { element ->
                if (element.hasAttr("src").not()) {
                    regex.find(element.data())?.groupValues?.let { mr ->
                        val jsonObject = JSONObject(mr[1])
                        val objDates = jsonObject.getJSONObject("drawDates")
                        val same = objDates.getString("drawYear")
                        schuPref.edit {
                            putDateTimeISO(KEY_FIXTURES_UPDATE, today)

                                .putString(KEY_FIXTURES, same)
                        }
                        return getLatestDDate(schuPref) // get comming drawdate from the bet.hkjc.com again which is the latest!!
                    }
                }
            }
            }
        }
        return commingDDate
    }

    fun download_next_draw(sharedPreferences:SharedPreferences) :String?{
        val default = null
/*        val regex =
            Regex("\\s*var\\s*$KEY_NEXT_DRAW_DATE\\s*=\\s*\"([^\"]*)\"\\s*;", RegexOption.DOT_MATCHES_ALL)
        val doc = getJsoupDoc(TAG_INDEX, mapOf("lang" to "ch"))
            ?: return null
        val m6_div= doc.select(".m6-index-div")
        m6_div.select("script").first()?.data()?.let { data ->
            regex.find(data)?.let{mr->
                sharedPreferences.edit() { putString(KEY_NEXT_DRAW_DATE, mr.groupValues[1]) }
            }
        }
        m6_div.select(".m6-index-div table:not(:has(table)):has(.snowball1)").first()
            ?.let { tbl ->
                tbl.getElementsByTag("tr").joinToString(indexTR) {it.getElementsByTag("td").joinToString(
                    indexTD){ it.text() }  }.run{
                    if(this.isNotEmpty()) {
                        sharedPreferences.edit()
                            .putString(KEY_NEXT, this)
                            .putDateTimeISO(KEY_NEXT_UPDATE, Calendar.getInstance())
                           // .apply()
                        return this
                    }
                }
            }*/
        return default
    }
    /**
     * return next_draw_data
     */
    fun getIndex(context: Context, latest: DrawResult) :String?{
        val indexsharedPreferences = context.getSharedPreferences(TAG_INDEX, MODE_PRIVATE)
        val now = Calendar.getInstance()
        indexsharedPreferences.getDateTimeISO(KEY_NEXT_UPDATE)?.let{
            try {
//                val date = sdf_now.parse(it).toCalendar()

                if(it.isEarlyBy(now, Calendar.MINUTE,
                        when {
                            latest.p1==null || (indexsharedPreferences.getString(KEY_NEXT, "")?:"").contains(latest.id) -> 2
                            (indexsharedPreferences.getString(KEY_NEXT_UPDATE, "")?:"").contains("估計頭獎基金${indexTD}-")->3
                            else -> 15
                        }
                    )){
                    return download_next_draw(indexsharedPreferences)
                }
                return indexsharedPreferences.getString(KEY_NEXT, null)
            } catch (e: Exception){
                return null
            }
        }
        return download_next_draw(indexsharedPreferences)
    }
    suspend fun UpdateLatestDraw(repo : DrawResultRepository, exec:(message:String, drawResult: DrawResult)->Unit){
//         getJsoupDoc0()?.let { document ->
//             val str = document.select(".next-draw-table-container")
//             println("what next item ${str.text()}")
//         }

//        val db = M6Db.getDatabase(repo)
//        if(db.isOpen) {//java.lang.IllegalStateException:
//            val drawResultDao = db.DrawResultDao()
//            getIndex(context, drawResultDao.getLatest())?.also {
                // load schedule , today not found ==> fixtures => draw schedule
                // latest date < today => error
//                val (_, _) = getLatestSchecule(context)
                val latestResult = getLatestResult(repo)

                if (latestResult.isNotEmpty()) {
                    exec("OK", repo.getLatest())
                    println("what is this ??<< OK")
                }
                else {
                    exec("what",repo.getLatest())
                    println("what is this ??<< what")
                }
//                return
//            }
//            exec("nook")
//            println("what is this ??<< nook")

//        } else
//         exec("db not open??")
    }
}