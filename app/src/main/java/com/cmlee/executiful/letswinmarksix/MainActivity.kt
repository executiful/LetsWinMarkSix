package com.cmlee.executiful.letswinmarksix

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.text.Layout
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.text.style.TabStopSpan
import android.text.style.TextAppearanceSpan
import android.text.style.UnderlineSpan
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.indices
import androidx.core.view.isVisible
import com.cmlee.executiful.letswinmarksix.BallDialogFragment.Companion.TAG_BALL_DIALOG
import com.cmlee.executiful.letswinmarksix.BallDialogFragment.Companion.newInstance
import com.cmlee.executiful.letswinmarksix.databinding.ActivityMainBinding
import com.cmlee.executiful.letswinmarksix.databinding.BallviewBinding
import com.cmlee.executiful.letswinmarksix.databinding.ColumnOfNumberBinding
import com.cmlee.executiful.letswinmarksix.databinding.NumberTextviewBinding
import com.cmlee.executiful.letswinmarksix.helper.AlertDialogHelper.ListView
import com.cmlee.executiful.letswinmarksix.helper.AlertDialogHelper.PositiveButton
import com.cmlee.executiful.letswinmarksix.helper.BannerAppCompatActivity
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.KEY_NEXT
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.KEY_NEXT_UPDATE
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.TAG_INDEX
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.UpdateLatestDraw
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.indexTD
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.indexTR
import com.cmlee.executiful.letswinmarksix.helper.DayYearConverter.Companion.jsonDate
import com.cmlee.executiful.letswinmarksix.helper.DayYearConverter.Companion.sqlDate
import com.cmlee.executiful.letswinmarksix.model.DrawStatus
import com.cmlee.executiful.letswinmarksix.model.NumStat
import com.cmlee.executiful.letswinmarksix.model.NumStat.Companion.BallColor
import com.cmlee.executiful.letswinmarksix.roomdb.M6Db
import java.lang.Math.random
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : BannerAppCompatActivity(), BallDialogFragment.IUpdateSelection {
    private lateinit var binding: ActivityMainBinding
    private lateinit var legViews: List<NumberTextviewBinding>
    private lateinit var bankerViews: List<NumberTextviewBinding>
    private lateinit var m6bViews: List<BallviewBinding>
    private lateinit var pauseDlg : AlertDialog

    private val ht = HandlerThread("m6thread")
    private lateinit var hr :Handler

    private var msgCalc:String = "good luck!!"

    @SuppressLint("FileEndsWithExt", "RestrictedApi", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ht.start()
        hr = Handler(ht.looper)

        adContainerView = binding.adViewContainer
        binding.idBallselect.removeAllViews()
        binding.ticketlayout.idLegs.removeAllViews()
        binding.ticketlayout.idBankers.removeAllViews()
        pauseDlg = AlertDialog.Builder(this, R.style.Theme_Wait_Dialog).setView(R.layout.pause_dialog_layout)
            /*.setOnCancelListener { it.dismiss() }*/.create()
//        pauseDlg.window?.setBackgroundDrawableResource(android.R.color.transparent)
        pauseDlg.setCanceledOnTouchOutside(false)
/*
        Handler(mainLooper).post {
            cacheDir.listFiles()?.forEach {
                if (it.endsWith(".json")) {
                    if (it.length() == 0L || it.readText() == "[]") it.delete()
//                }else if (it.endsWith(".txt")) {
//                    val fromJson = Gson().fromJson(it.readText(), DrawYear::class.java)
//                    File(it.name.replace(".txt", ".json")).writeText(Gson().toJson(fromJson, DrawYear::class.java))
                }
            }
        }
*/
        if(BuildConfig.DEBUG) {
            hr.post {
                cacheDir.listFiles()?.filter { it.length() == 0L }?.parallelStream()
                    ?.forEach { it.delete() }
                cacheDir.listFiles()?.let {
                    it.filter { it.isFile }
                        .groupBy { it.readText() }.filter {
                            it.value.size > 1
                        }.entries.parallelStream()
                        .forEach {
                            it.value.sortedByDescending { it.lastModified() }
                                .subList(1, it.value.lastIndex).parallelStream()
                                .forEach {
                                    println(it.name)
                                    it.delete()
                                }
                        }
                }
            }
        }
//        setSupportActionBar(binding.toolbar)
//        getString(R.string.ask_select).also { binding.toolbar.title = it }


        originalballs.also {
            legViews = it.map { NumberTextviewBinding.inflate(layoutInflater) }
            bankerViews = it.map { NumberTextviewBinding.inflate(layoutInflater) }
            m6bViews = it.map { BallviewBinding.inflate(layoutInflater) }
            val iterator = it.map{it.num.toString()}.withIndex().iterator()
            while (iterator.hasNext()) {
                val numB =
                    ColumnOfNumberBinding.inflate(layoutInflater).apply { root.removeAllViews() }
                val numL =
                    ColumnOfNumberBinding.inflate(layoutInflater).apply { root.removeAllViews() }
                binding.ticketlayout.idBankers.addView(numB.root)
                binding.ticketlayout.idLegs.addView(numL.root)
                (1..9).forEach {
                    if (iterator.hasNext()) {
                        val next = iterator.next()
                        val bankeritem = bankerViews[next.index]
                        bankeritem.idNumber.text = next.value
                        val legitem = legViews[next.index]
                        legitem.idNumber.text = next.value
                        numB.root.addView(bankeritem.root)
                        numL.root.addView(legitem.root)
                    }
                }
            }
        }
        with(binding.toolbar) {
            if (menu is MenuBuilder) (menu as MenuBuilder).setOptionalIconsVisible(true)
            menu.findItem(R.id.action_info)?.isVisible= BuildConfig.DEBUG //visible if is debug
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_generate -> {
                    if(originalballs.any{it.status!=NumStat.NUMSTATUS.UNSEL}) {
                        val dlg = AlertDialog.Builder(this).setMessage(R.string.action_redraw)
                            .setNegativeButton(android.R.string.cancel) { _, _ -> }
                        if(currentStatus!=DrawStatus.UnClassify&&!getSharedPreferences(NAME_ENTRIES, MODE_PRIVATE).contains(
                                msgNumbers)) {
                            dlg/*.setPositiveButton(R.string.action_save_n_redraw) { _, _ ->
                                saveEntry()
                                refresh()
                            }*/
                                .setNeutralButton(R.string.action_redraw) { _, _ ->
                                    refresh()
                                }
                                .show()
                        } else {
                            dlg.setPositiveButton(R.string.action_redraw){ _, _ ->
                                refresh()
                            }.show()
                        }
                    } else
                        refresh()
                    true
                }
                R.id.action_settings -> {
                    with(binding.toolbar.menu) {
                        findItem(R.id.action_past)?.let {
                            it.isEnabled = (currentStatus != DrawStatus.UnClassify)
                            true
                        }
                        findItem(R.id.action_save)?.let{
                            it.isEnabled = !((currentStatus == DrawStatus.UnClassify) || getSharedPreferences(
                                NAME_ENTRIES, MODE_PRIVATE).contains(
                                msgNumbers
                            ))
                            true
                        }
                        findItem(R.id.action_list_saved)?.let {
                            it.isEnabled = getSharedPreferences(NAME_ENTRIES, MODE_PRIVATE).all.isNotEmpty()
                            true
                        }
                    }
                    false
                }
                R.id.action_past -> {
                    show_checking()
                }
                R.id.action_disclaimer ->{
                    AlertDialog.Builder(this).setMessage(R.string.disclaimer).setTitle(R.string.action_disclaimer).show()
                    true
                }
                R.id.action_draw_schedule -> {
                    show_draw_schedule()
                }
                R.id.action_previous_next_draw->{
                    val ssb = SpannableString(getString(R.string.action_previous_next_draw))
                    ssb.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    ssb.setSpan(BackgroundColorSpan(Color.LTGRAY), 0, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    val dlg = AlertDialog.Builder(this, R.style.Theme_Monthly_Dialog)//.setTitle(ssb)
                        .setMessage(getDrawString()).create()
                        dlg.show()
                    hr.postDelayed({
                        UpdateLatestDraw(this) {
                            runOnUiThread {
                                if (it == "OK")
                                    initball()
                                else
                                    println("更新:\n    $it")
                                if(dlg.isShowing)
                                    dlg.setMessage(getDrawString())
                            }
                        }
                    },1000)
                    true
                }
                R.id.action_save ->{
                    saveEntry()
                }
                R.id.action_info ->{
                    val ssb = SpannableStringBuilder()

                    val dao = M6Db.getDatabase(this).DrawResultDao()

                    val results = dao.getAll()
//                    File.createTempFile("source", ".json", cacheDir).writeText(gson.toJson(results, DrawResultArray::class.java))
                    results.groupBy { it.id.substring(0,2) }.entries.parallelStream().forEach {
                        ent->
                        ent.value.sortedBy { it.date }.forEachIndexed { index, dr ->
                            if (dr.id != String.format("%s/%03d", ent.key, index + 1))
                                ssb.append(dr.id).append(System.lineSeparator()).append(jsonDate.format(dr.date))
                        }
//                        println("date sequence ${ent.key}")
                    }

                    ssb.append("!1")
                    ssb.appendLine(dao.getLatest().id)
                    ssb.appendLine(getString(R.string.action_redraw))
                    ssb.appendLine(getString(R.string.info_redraw))
                        ssb.setSpan(ImageSpan(this, R.drawable.baseline_refresh_24), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


                    AlertDialog.Builder(this).setMessage(ssb).setPositiveButton("刪除") { _, _ ->
                        dao.delete(dao.getLatest())
                        initball()
                    }.show().setOnCancelListener {
                        pauseDlg.show()
                    }
                    true
                }
                R.id.action_list_saved->{
                    val items = getSharedPreferences(NAME_ENTRIES, MODE_PRIVATE).all.map{
                        val ssb = SpannableString(it.key)
                        if(it.key == msgNumbers)
                            ssb.setSpan(UnderlineSpan(), 0, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        ssb
                    }
                    val adp = ArrayAdapter(this, android.R.layout.select_dialog_singlechoice, items
                        )
                    val cnv2Int = fun(str:String): List<Int> {
                        return str.split("\\s*\\+\\s*".toRegex()).map { Integer.parseInt(it) }
                    }
                    AlertDialog.Builder(this).setSingleChoiceItems(adp, -1) { di, i->
                        adp.getItem(i)?.let{
                            if(it.toString() == msgNumbers) {
                                di.PositiveButton().isVisible = false
                            } else {
                                val string = it.split('>')
                                println("this numbers are $string")
                                di.PositiveButton().isVisible = true
                            }
                        }
                    }.setNeutralButton(android.R.string.cancel) { dlg, _ ->
                        dlg.dismiss()
                    }.setPositiveButton(if(originalballs.any { it.status!=NumStat.NUMSTATUS.UNSEL }) R.string.message_replace else R.string.replace_numbers)  { dlg, _ ->
                        val selectedPosition = dlg.ListView().checkedItemPosition
                        if(dlg.ListView().indices.contains(selectedPosition)) {
                            val strings = adp.getItem(selectedPosition)?.split('>') ?: listOf()
                            dlg.dismiss()
                            val lnums = cnv2Int(strings.last())
                            val bnums = if (strings.size > 1) cnv2Int(strings.first()) else listOf()

                            originalballs.parallelStream()
                                .forEach { item ->
                                    item.status = when (item.num) {
                                        in lnums -> NumStat.NUMSTATUS.LEG
                                        in bnums -> NumStat.NUMSTATUS.BANKER
                                        else -> NumStat.NUMSTATUS.UNSEL
                                    }
                                }
                            initball()
                        }
                    }.create().also{
                        it.setOnShowListener{dlg->
                            dlg.PositiveButton().isVisible=false
                            dlg.ListView().divider = AppCompatResources.getDrawable(this, android.R.drawable.divider_horizontal_dark)
                        }
                    }.show()
                    true
                }
                else -> {
                    false
                }
            }
        }

        binding.ticketlayout.idTicket.setOnClickListener{
            val dlg = AlertDialog.Builder(this)
                .setTitle(msgCalc).setMessage(msgNumbers)
            if (!(currentStatus == DrawStatus.UnClassify || getSharedPreferences(NAME_ENTRIES, MODE_PRIVATE).contains(msgNumbers)))
                dlg.setPositiveButton(R.string.copy2clipboard) { d, _ ->
                d.dismiss()
//                saveEntry()

                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("label", msgNumbers)
                    clipboard.setPrimaryClip(clip)
                }
            dlg.show()
        }
        val spec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        genBall(numberordering)
        m6bViews.forEachIndexed { index, it ->
            val layoutParams = GridLayout.LayoutParams(spec, spec)
            layoutParams.width = 0
            layoutParams.height = 0
            binding.idBallselect.addView(it.root, layoutParams)
            balldata(it, numberordering[index])

            it.idBallnumber.setOnClickListener {
                updateball(index)
            }
            if(BuildConfig.DEBUG){
                it.idBallnumber.setOnLongClickListener {
                    if (supportFragmentManager.findFragmentByTag(TAG_BALL_DIALOG) == null) {
                        val phraseDialog = newInstance(index)
                        phraseDialog.show(
                            supportFragmentManager.beginTransaction(),
                            TAG_BALL_DIALOG
                        )
                    }
                    true
                }
            }
        }
        savedInstanceState?.run {
            getIntArray(KEY_ORDER)?.let { ord ->
                numberordering = ord.map { originalballs[it] }
            }
            getStringArray(KEY_STATUS)?.forEachIndexed { index, s ->
                originalballs[index].status = NumStat.NUMSTATUS.valueOf(s)
            }
            updateStatus()
        }
        hr.post{
            UpdateLatestDraw(this){
                runOnUiThread{
                    initball()
                }
            }
        }
    }

    private fun balldata(view: BallviewBinding, item: NumStat) {
        with(view){
            idBackground.setColorFilter(item.num.BallColor())
            idBackground.rotation = angle
            angle += 10
            val temp = (2 * minTimes / 5)
            idProgress.max = maxTimes + (minTimes / 10.0).toInt() - temp
            idProgress.progress = item.times - temp
            idTimes.text = (item.times).toString()
            idSince.text = item.since.toString()
            idBallnumber.text = item.numString
        }
    }

    override fun onResume() {
        hr.post{
            UpdateLatestDraw(this){
                if(it=="OK"){
                    initball()
                }
            }
        }
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        getSharedPreferences(NAME_ORDER, MODE_PRIVATE).edit().clear().apply()
        M6Db.getDatabase(this).close()
    }

    fun saveEntry():Boolean{
        return with(getSharedPreferences(NAME_ENTRIES, MODE_PRIVATE)){

            if(!contains(msgNumbers)){
                edit().putLong(msgNumbers, System.currentTimeMillis()).apply()
                Toast.makeText(this@MainActivity, R.string.message_save, Toast.LENGTH_SHORT).show()
                if(BuildConfig.DEBUG) {
                    with(edit()) {

                        all.entries.forEach{
                            val k = it.key.split("\\s*\\+\\s*").joinToString(numberseperator)

                            val parse = if(it.value is String) Date.parse(it.value as String) else it.value as Long
                            remove(it.key)
                            putLong(k, parse)
                        }
                        apply()
                    }
                }
                if(all.size>6)
                    edit().remove(all.entries.last().key).apply()
                true
            }     else false
        }
    }
    fun drawSpannableLine(line:Pair<String, String>, ssb:SpannableStringBuilder){
        val (name, value) = line
        ssb.append("$name$nbsp： ")

        val start = ssb.length
//            if(name.contains("多寶")&&value.equals("-")){
//                ssb.append(dotdotdot)
//            } else
        ssb.append(value)
        ssb.setSpan(TextAppearanceSpan(this, R.style.money), start, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if(name.contains('金') || name.contains('奬')) {
            val end = if(value.contains('\t')) value.indexOfFirst { it == '\t' }+start else ssb.length
            ssb.setSpan(
                ForegroundColorSpan(Color.RED),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        ssb.appendLine()
    }
    fun getDrawString() : SpannableStringBuilder {
        val pre = M6Db.getDatabase(this).DrawResultDao().getLatest()
        val sf = getSharedPreferences(TAG_INDEX, MODE_PRIVATE)

        val ssb = SpannableStringBuilder()
        var start: Int
/*        val act : (Pair<String,String>)->Unit ={(name,value)->
            ssb.append("$name$nbsp： ")

            start = ssb.length
//            if(name.contains("多寶")&&value.equals("-")){
//                ssb.append(dotdotdot)
//            } else
                ssb.append(value)
            ssb.setSpan(TextAppearanceSpan(this, R.style.money), start, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if(name.contains('金') || name.contains('奬')) {
                val end = if(value.contains('\t')) value.indexOfFirst { it == '\t' }+start else ssb.length
                ssb.setSpan(
                    ForegroundColorSpan(Color.RED),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            ssb.appendLine()
        }*/

        val pdata = mutableListOf(
            "攪珠期數" to pre.id,
            "攪珠日期" to sdf_display.format(pre.date),
            "攪珠結果" to "${pre.no.nos.joinToString(numberseperator)}$thinsp(${pre.sno})",
            "總投注額" to "$dollar${pre.inv}",
            "頭奬" to if (pre.p1 == null) "$dotdotdot" else String.format("%11s\t%6s", pre.p1, pre.p1u),
            "二奬" to if (pre.p2 == null) "$dotdotdot" else String.format("%11s\t%6s", pre.p2, pre.p2u),
            "三奬" to if (pre.p3 == null) "$dotdotdot" else String.format("%11s\t%6s", pre.p3, pre.p3u),
        )
        ssb.append("${emsp}上期攪珠").appendLine()
        start = ssb.length
        ssb.setSpan(StyleSpan(Typeface.BOLD), 0, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
// fulldollar $ \uFF04

        pdata.forEach{
            drawSpannableLine(it, ssb)
        }
        ssb.appendLine().appendLine()

        start = ssb.length
        ssb.append("${emsp}下期攪珠").appendLine()
        ssb.setSpan(
            StyleSpan(Typeface.BOLD),
            start,
            ssb.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        if(sf.all.isEmpty()) {
            start = ssb.length
            ssb.append("${getString(R.string.nothing_to_show)}").setSpan(
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                start,
                ssb.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssb.setSpan(
                TextAppearanceSpan(this, android.R.style.TextAppearance_Small),
                start,
                ssb.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            val nextstring = sf.getString(KEY_NEXT, "--") ?: ""
            if(nextstring.contains(pre.id)){
                ssb.appendLine(dotdotdot)
            } else {
                val ndata =
                    nextstring.split(indexTR).map { it.replace('$', dollar).split(indexTD) }
                        .filter { it.size == 2 }
                        .map {
                            it[0] to if (it[0].contains('金') && it[1].startsWith(dollar)) String.format(
                                "%11s",
                                it[1]
                            ) else it[1]
                        }

                ndata.forEach{
                    drawSpannableLine(it, ssb)
                }
                ssb.setSpan(
                    TabStopSpan.Standard(100),
                    0,
                    ssb.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            println("更新:$ssb")
        }
        ssb.appendLine().appendLine()
        start = ssb.length
        val updateat = sf.getString(KEY_NEXT_UPDATE, "--") ?: "--"
        ssb.append("更新時間　：　$updateat").setSpan(
            AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
            start,
            ssb.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssb.setSpan(
            TextAppearanceSpan(this, android.R.style.TextAppearance_Small),
            start,
            ssb.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

//        ssb.appendLine()
//        start = ssb.length
//        ssb.append(getText(R.string.disclaimer))
//        ssb.setSpan(TextAppearanceSpan(this, android.R.style.TextAppearance_Small), start, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return ssb
    }
    private fun show_draw_schedule(): Boolean {
        if (supportFragmentManager.findFragmentByTag(TAG_BALL_DIALOG) == null) {
            val monthlyDrawScheduleFragment = MonthlyDrawScheduleFragment.newInstance()
            monthlyDrawScheduleFragment.show(supportFragmentManager.beginTransaction(), TAG_BALL_DIALOG)
        }
        return true
    }

    private fun show_checking(): Boolean {
//        hr.post {
//            UpdateLatestDraw(this){ _ ->
                startActivity(Intent(this, DrawnNumberCheckingActivity::class.java))
//            }
//        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(KEY_ORDER, originalballs.map { numberordering.indexOf(it) }.toIntArray())
        outState.putStringArray(KEY_STATUS, originalballs.map{ it.status.toString()}.toTypedArray())
    }

    override val adUnitStringId: Int
        get() = R.string.admob_m6_lottery
    override fun onAdLoaded() {
    }

    fun updatemark(old: NumberTextviewBinding, new: NumberTextviewBinding) {

        old.idBackground.apply {
            setImageResource(R.drawable.pen_unmark_ani_vec)
            (drawable as AnimatedVectorDrawable).also {
                it.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
//                        new.idBackground.setImageResource(R.drawable.pen_mark_ani_vec)
//                        (new.idBackground.drawable as AnimatedVectorDrawable).start()
                        super.onAnimationEnd(drawable)
                    }
                })
                it.start()
            }
            new.idBackground.setImageResource(R.drawable.pen_mark_ani_vec)
            (new.idBackground.drawable as AnimatedVectorDrawable).start()
        }
    }

    fun updateball(index: Int, reset: Boolean = false): NumStat {
        val item = numberordering[index]
        val leg = legViews[item.idx]
        val banker = bankerViews[item.idx]
        val m6b = m6bViews[index]
//        val nxv: ImageFilterView = m6b.idSwitch.nextView as ImageFilterView
//        m6b.idBallinfo.strokeColor = item.num.BallColor()
        if (reset) {
            m6b.idProgress.max = maxTimes + (minTimes/2.0).toInt()
            m6b.idProgress.progress = item.times// - minTimes
            m6b.idTimes.text = item.times.toString()
            m6b.idSince.text = item.since.toString()
//            m6b.idSwitch.showNext()
            banker.idBackground.setImageResource(R.drawable.ticket_number)
            leg.idBackground.setImageResource(R.drawable.ticket_number)
            when (item.status) {
                NumStat.NUMSTATUS.LEG -> {
                    leg.idBackground.setImageResource(R.drawable.pen_unmark_ani_vec)

                    (leg.idBackground.drawable as AnimatedVectorDrawable).start()
                }

                NumStat.NUMSTATUS.BANKER -> {
                    banker.idBackground.setImageResource(R.drawable.pen_unmark_ani_vec)
                    (banker.idBackground.drawable as AnimatedVectorDrawable).start()
                }

                NumStat.NUMSTATUS.UNSEL -> {}
            }
            binding.ticketlayout.idSingle.setImageResource(R.drawable.ticket_single)
            binding.ticketlayout.idMultiple.setImageResource(R.drawable.ticket_multiple)
            binding.ticketlayout.idBanker.setImageResource(R.drawable.ticket_banker)
//            nxv.setImageResource(R.drawable.ic_baseline_radio_button_unchecked_24)
            item.status = NumStat.NUMSTATUS.UNSEL
//            m6b.idSwitch.showNext()
//            m6b.idBallinfo.strokeColor = item.num.BallColor()
            m6b.idBackground.setColorFilter(item.num.BallColor())
            m6b.idBallnumber.text = item.numString
        } else {
            m6b.imageBeside.isVisible = false
            when (item.status) {
                NumStat.NUMSTATUS.LEG -> {
//                    nxv.setImageResource(R.drawable.ic_baseline_push_pin_24)
                    item.status = NumStat.NUMSTATUS.BANKER
                    updatemark(leg, banker)
                }

                NumStat.NUMSTATUS.BANKER -> {
//                    nxv.setImageResource(R.drawable.ic_baseline_star_24)
                    item.status = NumStat.NUMSTATUS.LEG
                    updatemark(banker, leg)
                }

                NumStat.NUMSTATUS.UNSEL -> {
//                    nxv.setImageResource(R.drawable.ic_baseline_star_24)
                    item.status = NumStat.NUMSTATUS.LEG
                    m6bViews[index].idBallnumber.text = item.numString
                    leg.idBackground.apply {
                        setImageResource(R.drawable.pen_mark_ani_vec)
                        (drawable as AnimatedVectorDrawable).start()
                    }

                    val targets = originalballs.filter { (item.idx + 1 == it.idx || item.idx - 1 == it.idx) &&it.status == NumStat.NUMSTATUS.UNSEL}
                    targets.forEach {
                            println("中招號碼 ${numberordering.indexOf(it)}")

                    }
                    targets.forEach {
                        m6bViews[numberordering.indexOf(it)].imageBeside.isVisible = true
                    }
                }
            }
            updateStatus()
        }
        return item
    }

    fun setStatusAnim(v: ImageView, resId: Int) {
        v.setImageResource(resId)
        (v.drawable as AnimatedVectorDrawable).start()
    }
    fun changeStatus(status:DrawStatus, bMark:Boolean){
        when (status) {
            DrawStatus.Single -> setStatusAnim(
                binding.ticketlayout.idSingle,
                if(bMark)R.drawable.single_mark_ani_vec else R.drawable.single_unmark_ani_vec
            )

            DrawStatus.Multiple -> setStatusAnim(
                binding.ticketlayout.idMultiple,
                if(bMark)R.drawable.multiple_mark_ani_vec else R.drawable.multiple_unmark_ani_vec
            )

            DrawStatus.Banker -> setStatusAnim(
                binding.ticketlayout.idBanker,
                if(bMark)R.drawable.banker_mark_ani_vec else R.drawable.banker_unmark_ani_vec
            )

            DrawStatus.UnClassify -> {}
        }
    }
    fun updateStatus() {
        val leg = numberordering.groupBy { it.status }

        val calcDrawStatus = calcDrawStatus(
            leg[NumStat.NUMSTATUS.LEG]?.count() ?: 0,
            leg[NumStat.NUMSTATUS.BANKER]?.count() ?: 0
        )
        if (currentStatus != calcDrawStatus.first) {
            changeStatus(currentStatus, false)
            changeStatus (calcDrawStatus.first, true)
        }
        println("$TAG_BALL_DIALOG ${calcDrawStatus.first}")
        currentStatus = calcDrawStatus.first
    }

    fun refresh() {
        numberordering = originalballs.sortedBy { random() }
        numberordering.parallelStream().forEach { it.status = NumStat.NUMSTATUS.UNSEL }
        getSharedPreferences(NAME_ORDER, MODE_PRIVATE).edit().putString(KEY_ORDER, numberordering.joinToString { it.idx.toString() }).apply()
        initball()
    }
    fun waitDlg(p:(d: Dialog)->Unit){
        runOnUiThread{
            if (!pauseDlg.isShowing) {
                pauseDlg.setOnShowListener {
                    p(it as Dialog)
//                    it.dismiss()
                }
                pauseDlg.show()
            }
        }
    }

    fun initball(){
        if(System.currentTimeMillis() > lastInitMilliSec+ minInitMillSec) {
            genBall(numberordering)
            Handler(Looper.getMainLooper()).post {
                waitDlg {d->
                    numberordering.indices.toList().parallelStream().forEach {
                        runOnUiThread {
                            val item = numberordering[it]

                            when (item.status) {
                                NumStat.NUMSTATUS.LEG -> {
                                    legViews[item.idx].idBackground.setImageResource(
                                        R.drawable.number_background
                                    )
                                    bankerViews[item.idx].idBackground.setImageResource(
                                        R.drawable.ticket_number
                                    )
                                }

                                NumStat.NUMSTATUS.BANKER -> {
                                    bankerViews[item.idx].idBackground.setImageResource(
                                        R.drawable.number_background
                                    )
                                    legViews[item.idx].idBackground.setImageResource(
                                        R.drawable.ticket_number
                                    )
                                }

                                NumStat.NUMSTATUS.UNSEL -> {
                                    legViews[item.idx].idBackground.setImageResource(
                                        R.drawable.ticket_number
                                    )
                                    bankerViews[item.idx].idBackground.setImageResource(
                                        R.drawable.ticket_number
                                    )
                                        m6bViews[it].imageBeside.isVisible = originalballs.filter { item.idx + 1 == it.idx || item.idx - 1 == it.idx }
                                            .any { it.status != NumStat.NUMSTATUS.UNSEL }

                                }
                            }
                            balldata(m6bViews[it], item)
                            updateStatus()
                            d.dismiss()
                        }
                    }
                    lastInitMilliSec = System.currentTimeMillis()
                }
            }
        }
    }
    private var angle = 18.5f
    fun calcDrawStatus(leg: Int, ban: Int): Pair<DrawStatus, Int> {
        msgNumbers = "+"
        if (ban > 5) {
            msgCalc = getString(R.string.bankerexceeded)
            return DrawStatus.UnClassify to 0
        }
        if (leg + ban < 7) return if (leg == 6) {
            msgCalc = getString(R.string.singledraw)
            msgNumbers = originalballs.filter { it.status == NumStat.NUMSTATUS.LEG }.map { it.numString }.joinToString(numberseperator)
            DrawStatus.Single to 1
        } else {
            msgCalc = getString(R.string.unclassifydraw, ban, leg)
            DrawStatus.UnClassify to 0
        }
        val x = 6 - ban
        val temp = arrayOf(x, leg - x)
        temp.sort()
        val rem = (temp[1] + 1..leg).toMutableList()
        var divider = 1
        (1..temp[0]).forEach { item -> // this is avoid conversion from Int to Long and return Int, cause the result of leg!, factorial, maybe Long
            val idx = rem.indexOfFirst { it % item == 0 }
            if (idx == -1)
                divider *= item
            else
                rem[idx] /= item
        }
        val draw = rem.fold(1) { acc, i -> i * acc }.div(divider)
        return if (ban == 0) {
            msgCalc = getString(R.string.multipledraw, draw, leg)
            msgNumbers = originalballs.filter { it.status == NumStat.NUMSTATUS.LEG }.map { it.numString }.joinToString(
                numberseperator)
            DrawStatus.Multiple to leg
        } else {
            msgCalc = getString(R.string.bankerdraw, draw, ban, leg)
            val l = originalballs.filter { it.status==NumStat.NUMSTATUS.LEG }.map { it.numString }.joinToString(
                numberseperator)
            val b = originalballs.filter { it.status==NumStat.NUMSTATUS.BANKER }.map { it.numString }.joinToString(
                numberseperator)
            msgNumbers = "$b>$l"
            DrawStatus.Banker to draw
        }
    }
    fun genBall(/*db: M6Db, */order: List<NumStat>):Boolean {
        val db = M6Db.getDatabase(this)
        val drawResultDao = db.DrawResultDao()
        val dr = drawResultDao.getAll()
        val temp = dr.sortedByDescending { it.date }
            .filter { it.date >= dateStart }
            .mapIndexed { index, data -> index to data.no.nos.plus(data.sno) }
        order.parallelStream().forEach { i ->
            val f = temp.filter { it.second.contains(i.num) }
            i.times = f.count()
            if (i.times > 0)
                i.since = f.first().first
            else
                i.since = -1
        }
        return false
    }
//    private fun isNetworkAvailable(): Boolean {
//        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
//        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
//        return activeNetworkInfo != null && activeNetworkInfo.isConnected
//    }
    fun allCombination(): MutableList<List<Int>> {
        val combinations = mutableListOf<List<Int>>()
        val m = 6 - bankers.size

        //generates all possible combinations of legs using bit manipulation
        for (i in 0 until (1 shl legs.size)) {
            val combination = mutableListOf<Int>()
            for (j in legs.indices) {
                if ((i and (1 shl j)) > 0) {
                    combination.add(legs[j])
                }
            }
            if (combination.size == m) {
                combination.addAll(bankers)
                combination.sort()
                combinations.add(combination)
            }
        }
        return combinations
    }

    companion object {
        private var lastInitMilliSec = 0L
        private val minInitMillSec = 1000
        const val nbsp = '\u00A0'
        const val emsp = '\u2003'
        const val ensp = '\u2002'
        const val thinsp = '\u2009'
        const val dollar = '\uFE69'
        const val emdash = '\u2014'
        const val dotdotdot = '\u2026'
        const val numberseperator = "$thinsp+"
        const val m6_49StartDate = "2002/07/04"
        private const val NAME_ORDER = "ORDER"
        private const val KEY_ORDER = "NUMBER_ORDER"
        private const val KEY_STATUS = "status"
        private const val NAME_ENTRIES = "ENTRIES"
//        val d2 = '\uFF04'
        val bankers get() = originalballs.filter { it.status == NumStat.NUMSTATUS.BANKER }.map { it.num }
        val legs get() = originalballs.filter { it.status== NumStat.NUMSTATUS.LEG }.map{it.num}
        var msgNumbers:String = "+"

        private val originalballs = (1..49).withIndex()//.sortedBy { Math.random() }
            .map { NumStat(it.value, it.index) }
//        val sinces get() = originalballs.map { it.since }
        private var numberordering = originalballs.sortedBy { random() }
        private var currentStatus = DrawStatus.UnClassify
        fun nextCount(sel: Int): Boolean =
            numberordering.filter { it.status != NumStat.NUMSTATUS.UNSEL }.map { (it.num - sel) }
                .any { (it == -1 || it == 1) }

        val minTimes get() = numberordering.filter { it.times != -1 }.minOf { it.times }
        val maxTimes get() = numberordering.maxOf { it.times }
        val maxSince get() = numberordering.maxOf { it.since }
        val dateStart get() = sqlDate.parse(m6_49StartDate)
        val sdf_display = SimpleDateFormat("dd/MM/yyyy (EEEE)", Locale.CHINESE)
    }

    override fun getItem(idx: Int) = numberordering[idx]

    @SuppressLint("SuspiciousIndentation")
    override fun toggle(index: Int, reset: Boolean): NumStat {
        if (reset) {
            val item = numberordering[index]
            val leg = legViews[item.idx]
            val banker = bankerViews[item.idx]
            when (item.status) {
                NumStat.NUMSTATUS.LEG -> {
                    leg.idBackground.setImageResource(R.drawable.pen_unmark_ani_vec)
                    (leg.idBackground.drawable as AnimatedVectorDrawable).start()
                }

                NumStat.NUMSTATUS.BANKER -> {
                    banker.idBackground.setImageResource(R.drawable.pen_unmark_ani_vec)
                    (banker.idBackground.drawable as AnimatedVectorDrawable).start()
                }

                NumStat.NUMSTATUS.UNSEL -> {}
            }
            item.status = NumStat.NUMSTATUS.UNSEL
            val ordering = originalballs.filter { it.status == NumStat.NUMSTATUS.UNSEL }
                .sortedBy { random() }.toMutableList()

            numberordering.forEachIndexed { i, n ->
                if (n.status == NumStat.NUMSTATUS.UNSEL) {
                    val that = ordering[i]
                    with(m6bViews[i]) {
//                        idBallinfo.strokeColor = that.num.BallColor()
                        idProgress.max = maxTimes + (minTimes/2.0).toInt()
                        idProgress.progress = that.times
                        idTimes.text = that.times.toString()
                        idSince.text = that.since.toString()
                        idBallnumber.text = that.numString
                    }
                } else
                    ordering.add(i, n)
            }
            numberordering = ordering
            numberordering.filter { it.status == NumStat.NUMSTATUS.UNSEL }.forEach { next ->
                val filter =
                    numberordering.filter { it.idx == next.idx + 1 || it.idx == next.idx - 1 }
                m6bViews[numberordering.indexOf(next)].imageBeside.isVisible =
                    (filter.any { it.status != NumStat.NUMSTATUS.UNSEL })
            }

            updateStatus()
            return item
        } else
            return updateball(index, reset)
    }
}