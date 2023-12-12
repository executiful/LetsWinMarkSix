package com.cmlee.executiful.letswinmarksix.helper

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.net.Uri
import com.cmlee.executiful.letswinmarksix.model.NoArray
import com.cmlee.executiful.letswinmarksix.model.UnitPrice
import com.cmlee.executiful.letswinmarksix.model.WinningUnit
import com.cmlee.executiful.letswinmarksix.model.drawYears.DrawMonth
import com.cmlee.executiful.letswinmarksix.model.drawYears.DrawYear
import com.cmlee.executiful.letswinmarksix.model.drawYears.DrawYearItem
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResult
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResultArray
import com.cmlee.executiful.letswinmarksix.roomdb.M6Db
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeoutException

class ConnectURLThread(database: M6Db, val cacheDir: File, val lang: String) : Thread() {
    @SuppressLint("SimpleDateFormat")
    val sdf = SimpleDateFormat("yyyyMMdd")

    private val arrResult = mutableListOf<DrawResult>()
    private val drawResultDao = database.DrawResultDao()
    private val gson = GsonBuilder().setPrettyPrinting()//.registerTypeAdapter(LocalDate::class.java, LocalDateConverter())
        .registerTypeAdapter(Date::class.java, DayYearConverter())
        .registerTypeAdapter(UnitPrice::class.java, UnitPriceConverter())
        .registerTypeAdapter(WinningUnit::class.java, WinningUnitConverter())
        .registerTypeAdapter(NoArray::class.java, NoArrayConverter())
        .create()
    override fun run() {
        super.run()
        //1993
        val cal = Calendar.getInstance()
        var sd = "19930101"
        cacheDir.listFiles()?.find { it.name == "getAll2275520674976205633.json" }?.let {
            val temp = gson.fromJson(it.readText(), DrawResultArray::class.java)
            drawResultDao.insertOrIgnoreAll(*temp.distinct().toTypedArray())
            it.delete()
        }
        drawResultDao.getLatest().let { sd = DayYearConverter.getJson.format(it.date) }

//        sd = "20230701"//drawResultDao.getLatest().date.toYD.replace("/", "")
        cal.time = sdf.parse(sd)
        while(android.icu.util.Calendar.getInstance().time> cal.time) {
            cal.add(Calendar.MONTH, 3)
            val ed = sdf.format(cal.time)
            val (res, doc) = getUrlContent("getJSON", mapOf("sd" to sd, "ed" to ed, "sb" to "0"))
            try {
                if(res.code== HttpURLConnection.HTTP_OK) {
                    doc.text().also {
                        if (!it.isNullOrEmpty()) {
                            val tmp = gson.fromJson(it, DrawResultArray::class.java)
                            arrResult.addAll(tmp)
                        }
                    }
                }
            } catch (e: Exception) {
                println("${e.message} what is wrong???")
            }
            println("$sd $ed ${arrResult.size} size")
            sd = ed
        }
        arrResult.sortBy { it.date }
//        File.createTempFile("getAll", ".json", cacheDir).writeText(gson.toJson(arrResult))
        drawResultDao.insertOrIgnoreAll(*arrResult.distinct().toTypedArray())

        val failstr = TAG_ALL.filter {
            val (res, doc) = getUrlContent(it, mapOf("lang" to lang))
            if(res.code== HttpURLConnection.HTTP_OK) {
                res.let { itm ->
                    /*println*/(when (it) {
                    TAG_INDEX -> {
                        val data = runIndex(doc)
                        File.createTempFile(TAG_INDEX, ".txt", cacheDir).writeText(data)
                    }
                    TAG_FIXTURES -> {
                        runFixt(doc)
                            .also { same ->
                                File.createTempFile(TAG_FIXTURES, ".txt", cacheDir).writeText(same)
                                val drawyears = GsonBuilder().registerTypeAdapter(DrawYearItem::class.java, DrawMonthConverter()).create().fromJson(same, DrawYear::class.java)
                                drawyears.forEach { item ->
                                    item.drawMonth.forEach { month ->
                                        month.drawDate.forEach { d ->
                                            println("${month.month}, ${d.date}")
                                        }
                                    }
                                }
                            }
                    }
                    TAG_STATISTICS -> {
                        runStat(doc)
                    }
                    else -> {
//                        setProgress(workDataOf("unknow" to it))
                        false
                    }
                })
                }
                true
            } else {
//                setProgress(workDataOf(it to res.message))
                false
            }
        }
        File.createTempFile("filter", ".txt", cacheDir).writeText(failstr.joinToString())
    }

    fun initUrl(uristr: String, i: Int): HttpURLConnection {
        with(URL(uristr).openConnection() as HttpURLConnection) {
            allowUserInteraction = false
            instanceFollowRedirects = true
            requestMethod = "GET"

            connectTimeout = (connectTO + incTO * i) * 1000
            readTimeout = (readTO + incTO * i) * 1000
            return this
        }
    }
    data class response(var code:Int=-1,var message:String="")
    private fun getUrlContent(p: String, lang: Map<String, String>): Pair<response, Document> {
        var getstr = "<HTML></HTML>"
        val resp = response()
        val uristr = Uri.Builder().scheme(SCHE).authority(AUTH)
            .encodedPath("$M6/$p.aspx")
            .encodedQuery(lang.toQuery()).build().toString()
        for (i in 1..4) {
            try {
                val conn = initUrl(uristr, i)
                println("嘗試第$i 次$uristr")
                conn.connect()
                if (conn.responseCode == HttpURLConnection.HTTP_OK && conn.inputStream != null) {
                    getstr = String(conn.inputStream.readBytes())
                    resp.code = conn.responseCode
                    resp.message = conn.responseMessage
                    conn.inputStream.close()
                    conn.disconnect()
                    break
                }
            } catch (e: Exception) {
                resp.message = e.message!!
                break
            } catch (eto: TimeoutException) {
                println("connection exception>>>><<< $eto.message")
                continue
            }
        }
        return resp to Jsoup.parse(getstr)
    }

    companion object {
        //            val ofPattern = DateTimeFormatter.ofPattern("yyyyMMdd")
        const val AUTH = "bet.hkjc.com"
        const val SCHE = "https"
        const val M6 = "marksix"
        const val STARTING = "!!starting!!"
        const val connectTO = 8// timeout
        const val readTO = 10 // timeout
        const val incTO = 5

        const val TAG_INDEX = "index"
        const val TAG_FIXTURES = "Fixtures"
        const val TAG_STATISTICS = "Statistics"
        val TAG_ALL = arrayOf(TAG_INDEX, TAG_FIXTURES/*, TAG_STATISTICS*/)
        fun Map<String, String>.toQuery():String{
            return this.map{ "${it.key}=${it.value}"}.joinToString("&")
        }
        fun runIndex(parse: Document):String {
            val sb = StringBuilder()
            parse.getElementsByClass("m6-index-div").forEach { head ->
                val td = head.getElementsByTag("td")
                td.groupBy { it.parent() }.forEach{
                    sb.append(it.key?.className())
                        .append(it.value.filter { tds ->
                            tds.classNames().any { cs -> "content|snowball1".contains(cs) }
                        }.joinToString(":") { m->m.text() }).appendLine()
                }
            }
            return sb.toString()
        }
        fun runStat(parse: Document) :String{
            val sb = StringBuffer()
            parse.getElementsByClass("msStatTableCell").forEach{ cell ->
                val imgs = cell.getElementsByTag("img").`val`()
                val t1 = cell.getElementsByClass("statNum1").text()
                val t2 = cell.getElementsByClass("statNum2").text()
                println("$imgs $t1 $t2 ;;;;;;;;;;;;;;;;;;;;;;;")
                sb.append(imgs).append(t1).append(t2).appendLine()
            }
            return sb.toString()
        }
        fun runFixt(parse: Document) :String {
            val regex = Regex("\\s*var\\s*dataJson\\s*=\\s*(.*)\\s*;", RegexOption.DOT_MATCHES_ALL)
            parse.getElementsByTag("script").forEach { element ->
                if (element.hasAttr("src").not()) {
                    regex.find(element.data())?.let { mr ->
                        val jsonObject = JSONObject(mr.groupValues[1])
                        val objDates = jsonObject.getJSONObject("drawDates")
                        return objDates.getString("drawYear")
                    }
                }
            }
            return Gson().toJson(JSONObject())
        }
    }
}