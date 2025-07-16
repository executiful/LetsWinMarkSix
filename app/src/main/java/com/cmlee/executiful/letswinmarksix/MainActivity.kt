package com.cmlee.executiful.letswinmarksix

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.RED
import android.graphics.Typeface
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.HandlerThread
import android.text.Layout
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TabStopSpan
import android.text.style.TextAppearanceSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.edit
import androidx.core.view.children
import androidx.core.view.indices
import androidx.core.view.isVisible
import com.cmlee.executiful.letswinmarksix.BallDialogFragment.Companion.TAG_BALL_DIALOG
import com.cmlee.executiful.letswinmarksix.databinding.ActivityMainBinding
import com.cmlee.executiful.letswinmarksix.databinding.BallBinding
import com.cmlee.executiful.letswinmarksix.databinding.ColumnOfNumberBinding
import com.cmlee.executiful.letswinmarksix.databinding.NumberTextviewBinding
import com.cmlee.executiful.letswinmarksix.databinding.RefreshDialogBinding
import com.cmlee.executiful.letswinmarksix.helper.AlertDialogHelper.ListView
import com.cmlee.executiful.letswinmarksix.helper.AlertDialogHelper.PositiveButton
import com.cmlee.executiful.letswinmarksix.helper.BannerAppCompatActivity
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.KEY_BLIND
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.KEY_GROUP
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.KEY_ORDER
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.KEY_SELECTED
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.NAME_ENTRIES
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.NAME_NUM_GRP
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.NAME_ORDER
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.TICKETRESULT
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.TICKETSTRING
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.KEY_NEXT
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.KEY_NEXT_UPDATE
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.TAG_INDEX
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.UpdateLatestDraw
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.getDateTimeISO
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.getDateTimeISOFormat
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.getLatestSchecule
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.indexTD
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.indexTR
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.isEarlyBy
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.putDateTimeISO
import com.cmlee.executiful.letswinmarksix.helper.DayYearConverter.Companion.sqlDate
import com.cmlee.executiful.letswinmarksix.model.DrawStatus
import com.cmlee.executiful.letswinmarksix.model.NumStat
import com.cmlee.executiful.letswinmarksix.model.NumStat.Companion.BallColor
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResult
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResultDao
import com.cmlee.executiful.letswinmarksix.roomdb.M6Db
import java.lang.Math.random
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random


class MainActivity : BannerAppCompatActivity(), BallDialogFragment.IUpdateSelection {
    private lateinit var binding: ActivityMainBinding
    private lateinit var legViews: List<NumberTextviewBinding>
    private lateinit var bankerViews: List<NumberTextviewBinding>
    private lateinit var m6bViews: List<BallBinding>
    private lateinit var pauseDlg : AlertDialog
    private var alertDialog:AlertDialog? = null
//     var ballColors = mutableListOf<Int>()
//    private val dislikeNumbers = mutableListOf<Int>()
//    private val likeNumbers = mutableListOf<Int>()
//    private val groupNumberMappings = mutableMapOf<String,MutableList<Int>>()
    private val ht = HandlerThread("m6thread")
    private lateinit var hr :Handler

//    private var msgCalc:String = "good luck!!"
    private val evtShowTicket = View.OnClickListener {
        alertDialog?.let {
            if (it.isShowing)
                return@OnClickListener
        }
        val dlg = AlertDialog.Builder(this, R.style.Theme_Monthly_Dialog)
            .setTitle(msgCalc).setMessage(msgNumbers)
        if (!(currentStatus == DrawStatus.UnClassify || getSharedPreferences(NAME_ENTRIES, MODE_PRIVATE).contains(msgNumbers)))
            dlg.setPositiveButton(android.R.string.copy) { d, _ ->
                d.dismiss()
//                saveEntry()

                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(getString(R.string.chprize_1st), msgNumbers)
                clipboard.setPrimaryClip(clip)
            }
        alertDialog = dlg.show()
    }

    override fun onPause() {
        super.onPause()
        M6Db.dismiss()
    }
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
        
        avoidViewOverlapping(binding.adViewContainer)
        val tmpColorArray = resources.obtainTypedArray(R.array.ball_color_array)
        tmpColorArray.getColor(0, RED)
        ballcolor.addAll(
            originalballs.indices.map {
                tmpColorArray.getColor(it, Color.TRANSPARENT)
            }
        )
        tmpColorArray.recycle()
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
        if (BuildConfig.DEBUG) {
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
//                                    println(it.name)
                                    it.delete()
                                }
                        }
                }
            }
        }
//        setSupportActionBar(binding.toolbar)
//        getString(R.string.ask_select).also { binding.toolbar.title = it }

        if (savedInstanceState == null) {

            binding.toolbar.menu.findItem(R.id.action_view_all)?.let {
                it.isVisible = blind
            }

        } else {
            binding.toolbar.menu.findItem(R.id.action_view_all)?.let { it.isVisible = !blind }
        }

        getSharedPreferences(NAME_ORDER, MODE_PRIVATE).also { sr ->
            blind = sr.getBoolean(KEY_BLIND, blind)
            useGroup = sr.getBoolean(KEY_GROUP, useGroup)

            sr.getString(KEY_SELECTED, null)?.let {
                it.toCharArray().forEachIndexed { index, c ->
                    when (c) {
                        'B' -> originalballs[index].status = NumStat.NUMSTATUS.BANKER
                        'L' -> originalballs[index].status = NumStat.NUMSTATUS.LEG
                    }
                }
            }
            sr.getString(KEY_ORDER, null)
                ?.let { ordering ->
                    val order = ordering.stringIdx()
                    if (order.count() == numberordering.count()) {
                        numberordering = order.map { originalballs[it] }
                    }
                }
        }
        if(savedInstanceState==null) {
            Log.d("Main-Start", "savedInstanceState")
            ResetNumberDialog()
        }

        updateStatus()
        originalballs.also {
            legViews = it.map { NumberTextviewBinding.inflate(layoutInflater) }
            bankerViews = it.map { NumberTextviewBinding.inflate(layoutInflater) }
            m6bViews = it.indices.map {
                BallBinding.inflate(layoutInflater).apply {
                    this.idNumber.text = numberordering[it].numString
                    "-${System.lineSeparator()}-".also { idStatistics.text = it }
                }
            }
            val iterator = it.map { he -> he.num.toString() }.withIndex().iterator()
            while (iterator.hasNext()) {
                val numB =
                    ColumnOfNumberBinding.inflate(layoutInflater).apply { root.removeAllViews() }
                val numL =
                    ColumnOfNumberBinding.inflate(layoutInflater).apply { root.removeAllViews() }
                binding.ticketlayout.idBankers.addView(numB.root)
                binding.ticketlayout.idLegs.addView(numL.root)
                repeat(9) {
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
//        with(binding.toolbar) {
//            if (menu is MenuBuilder) (menu as MenuBuilder).setOptionalIconsVisible(true)
//            menu.findItem(R.id.action_info)?.isVisible= BuildConfig.DEBUG //visible if is debug
//        }

        populate_toobar()
        populate_menu()
        binding.ticketlayout.idTicket.setOnClickListener(evtShowTicket)

        // release later
        binding.ticketlayout.idTicket.setOnLongClickListener {
            if (currentStatus != DrawStatus.UnClassify)
                show_checking()
            else false
        }
        val db = M6Db.getDatabase(this)
        genBall(db.DrawResultDao())

        val spec = GridLayout.spec(GridLayout.UNDEFINED, 1f)

        m6bViews.forEachIndexed { index, it ->
            val layoutParams = GridLayout.LayoutParams(spec, spec)
            layoutParams.width = 0
            layoutParams.height = 0
            binding.idBallselect.addView(it.root, layoutParams)
            it.idNumber.setOnClickListener {
                updateball(index)
                updateStatus()
                getSharedPreferences(NAME_ORDER, MODE_PRIVATE).edit() {
                    putString(KEY_SELECTED, originalballs.joinToString("") {
                        when (it.status) {
                            NumStat.NUMSTATUS.LEG -> "L"
                            NumStat.NUMSTATUS.BANKER -> "B"
                            NumStat.NUMSTATUS.UNSEL -> "U"
                        }
                    })
                }
            }
            when (numberordering[index].status) {
                NumStat.NUMSTATUS.LEG -> {
                    legViews[numberordering[index].idx].idBackground.setImageResource(
                        R.drawable.number_background
                    )
                    bankerViews[numberordering[index].idx].idBackground.setImageResource(
                        R.drawable.ticket_number
                    )
                }

                NumStat.NUMSTATUS.BANKER -> {
                    bankerViews[numberordering[index].idx].idBackground.setImageResource(
                        R.drawable.number_background
                    )
                    legViews[numberordering[index].idx].idBackground.setImageResource(
                        R.drawable.ticket_number
                    )
                }

                NumStat.NUMSTATUS.UNSEL -> {
                    legViews[numberordering[index].idx].idBackground.setImageResource(
                        R.drawable.ticket_number
                    )
                    bankerViews[numberordering[index].idx].idBackground.setImageResource(
                        R.drawable.ticket_number
                    )
                    /*
                                                            m6bViews[it].imageBeside.isVisible = originalballs.filter { item.idx + 1 == it.idx || item.idx - 1 == it.idx }
                                                                .any { it.status != NumStat.NUMSTATUS.UNSEL }
                    */
                }
            }
            balldata(it, numberordering[index])

//            if (BuildConfig.DEBUG) {
//                it.idNumber.setOnLongClickListener {
//                    if (supportFragmentManager.findFragmentByTag(TAG_BALL_DIALOG) == null) {
//                        val phraseDialog = newInstance(index)
//                        phraseDialog.show(
//                            supportFragmentManager.beginTransaction(),
//                            TAG_BALL_DIALOG
//                        )
//                    }
//                    true
//                }
//            }
        }
        changeStatus(currentStatus, true)
//        pauseDlg.dismiss()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_8 -> {
                blind = !blind
                binding.toolbar.menu.findItem(R.id.action_view_all).isVisible = blind
                numberordering.forEachIndexed { index, numStat ->
                    m6bViews[index].idNumber.text = numStat.numString
                }
                true
            }
            KeyEvent.KEYCODE_MINUS->{
                val db = M6Db.getDatabase(this)
                db.DrawResultDao().let {
                    val latestNotNull = it.getLatestNotNull()

                    it.delete(latestNotNull)
                    genBall(it)
                    initball()
                }
                true
            }
            KeyEvent.KEYCODE_0 -> {
                    binding.ticketlayout.idTicket.performLongClick()
            }
            KeyEvent.KEYCODE_9->{
                val allresult = M6Db.getDatabase(this).DrawResultDao().getAll().filter { it.date>= dateStart }

                val message = AlertDialog.Builder(this).setMessage("wait")
//                    .setMessage(datelist.joinToString { sdf_display.format(it) })
                    .create()
                message.setOnShowListener {
                        val odd_indices = allresult.indices.filter { it%2!=0 }.dropLast(1)
                        val str = odd_indices/*.parallelStream()*/.filter {
                            allresult[it].no.nos.toList().intersect(allresult[it+1].no.nos.toSet()).size>2
                        }.map{sdf_display.format(allresult[it].date)}.toList().joinToString()
                        message.setMessage(str)
                }
                message.show()
                true
            }
            KeyEvent.KEYCODE_4->{
//                if(49-legs.size-bankers.size>6){
                val luckies = originalballs
                    .filterNot { it.num in legs }
                    .filterNot { it.num in bankers }
                    .sortedByDescending { if(it.since==0) Random.nextInt(50) else Random.nextInt(50) * it.since }//.take(bankers.size)
                val sb = luckies.filterIndexed { index, _ -> index> legs.size }
                    .take(bankers.size)
                    .sortedBy { it.num }
                    .map{ it.num }
                    .joinToString(numberseperator)
                val sl = luckies.take(legs.size)
//                    .sortedByDescending {  if(it.since==0) Random.nextInt(50) else Random.nextInt(50) * it.since }
//                    .take(legs.size)
                    .sortedBy{ it.num }
                    .map{ it.num }
                    .joinToString(numberseperator)
                AlertDialog.Builder(this)
                    .setTitle(legs.plus(bankers).sorted().joinToString(numberseperator))
                    .setMessage(

                        "$sb>${System.lineSeparator()}$sl"
//                            .take(7).map { it.num }.sorted()
//                            .joinToString(
//                                numberseperator
//                            )
                    ).show()
//                }
                false
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
    private class ResultHolder(val root:TextView)

    private class PassResultAdp(context: Context, list:List<DrawResult>):ArrayAdapter<DrawResult>(context, android.R.layout.simple_list_item_2, android.R.id.text1, list) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
//            convertView?.let { cnv->
                getItem(position)?.let { item->
                    val ssp = SpannableStringBuilder(item.id)
                    ssp.append(sdf_display.format(item.date))
                        .appendLine(item.inv)
                        .append(item.no.nos.joinToString(":/"))
                view.findViewById<TextView>(android.R.id.text1).text=ssp
                    ssp.clear()
                    ssp.append(context.getString(R.string.chprize_1st)).append(item.p1.toString()).appendLine(item.p1u.toString())
                    ssp.append(context.getString(R.string.chprize_2nd)).append(item.p2.toString()).appendLine(item.p2u.toString())
                    ssp.append(context.getString(R.string.chprize_3rd)).append(item.p3.toString()).appendLine(item.p3u.toString())
                view.findViewById<TextView>(android.R.id.text2).text=ssp
                }
//            }
            return view
        }
    }
    private fun populate_toobar(){
        binding.toolbar.setOnMenuItemClickListener { item ->
            alertDialog?.let{
                if(it.isShowing){
                    return@setOnMenuItemClickListener false
                }
            }
            when (item.itemId) {
                R.id.id_pass20->{
//                    val db = M6Db.getDatabase(this  ).DrawResultDao().getAll().take(20)
//                    val adp  = PassResultAdp(this, db)
//                    adp.addAll(db)
//                    AlertDialog.Builder(this, R.style.Theme_Monthly_Dialog).setAdapter(adp){a,b->
//
//                    }.show()
                    startActivity(Intent(this, LatestDrawnActivity::class.java))
                    true
                }
                R.id.action_view_all -> {
                    alertDialog = AlertDialog.Builder(this, R.style.Theme_Monthly_Dialog).setIcon(R.drawable.baseline_remove_red_eye_24)
                        .setTitle(item.title).setPositiveButton(android.R.string.ok) { _, _ ->
                            item.isVisible = false
                            blind = false
                            getSharedPreferences(NAME_ORDER, MODE_PRIVATE).edit() {
                                putBoolean(
                                    KEY_BLIND, blind
                                )
                            }
//                            val gpn = if(useGroup) getSharedPreferences(NAME_NUM_GRP, MODE_PRIVATE).all
//                            if (useGroup && item.status == NumStat.NUMSTATUS.UNSEL) getSharedPreferences(
//                                NAME_NUM_GRP,
//                                MODE_PRIVATE
//                            )
//                                .getString(item.num.toString(), item.numString) else item.numString
                            numberordering.forEachIndexed { index, item ->
                                m6bViews[index].idNumber.text = item.numString
                                if (useGroup && item.status == NumStat.NUMSTATUS.UNSEL) getSharedPreferences(
                                    NAME_NUM_GRP,
                                    MODE_PRIVATE
                                )
                                    .getString(item.num.toString(), item.numString) else item.numString
                            }
                        }.setNegativeButton(android.R.string.cancel) { _, _ ->
                        }.show()
                    alertDialog!=null
                }
                R.id.action_generate -> {
                    val dialogBinding = RefreshDialogBinding.inflate(layoutInflater)
                    val maxItem = 8
                    dialogBinding.opLike.text = getString(R.string.title_option_special_group, listOf(dialogBinding.likenumber.tag, dialogBinding.dislikenumber.tag).joinToString("/"), maxItem)
                    val spf = getSharedPreferences(NAME_NUM_GRP, MODE_PRIVATE).all
                        .map{it.value as String to it.key.trimStart('0')}.toList().groupBy ( { it.first }, { it.second } )
                    val grpNums = mapOf(dialogBinding.likenumber.tag as String to mutableListOf(), dialogBinding.dislikenumber.tag as String to mutableListOf<String>())
                    grpNums.filterKeys { it in spf.keys }.forEach { (s, strings) ->
                        spf[s]?.let { strings.addAll(it) }
                    }
                    val flatgrp = grpNums.flatMap { it.value.map { item->item to "${it.key}　${item.padStart(2)}" } }.toMap()
                    with(dialogBinding.opmornm) {
                        this.children.filter { it is RadioButton }.forEach { rb ->
                            "${rb.tag}　組".also { (rb as RadioButton).text = it }
                            rb.setOnLongClickListener { crb ->
                                grpNums[crb.tag as String]?.let { lst ->
                                    if (lst.isEmpty()) return@let false
                                    val id = dialogBinding.opmornm.checkedRadioButtonId
                                    this.setWillNotDraw(true)
                                    this.check(crb.id)
                                    dialogBinding.idselection.children.map { a -> a as AppCompatTextView }
                                        .filter { f -> f.text.startsWith(crb.tag as String) }
                                        .forEach { r -> r.performClick() }
                                    this.check(id)
                                    this.setWillNotDraw(false)
                                }
                                true
                            }
                        }
                    }
                    dialogBinding.idselection.children.map { it as AppCompatTextView }
                        .forEachIndexed { idx, actv ->
                            actv.tag = actv.text.trim() //張原本的數字掛在tag
                             actv.setBackgroundColor(ballcolor[idx])
                            resources.obtainTypedArray(R.array.grp_border_array).apply{
                                getDrawable(idx)
                                actv.setBackgroundDrawable(getDrawable(idx)!!)
                                recycle()
                            }

                            actv.text = if (flatgrp.containsKey(actv.tag as String)) {
                                "${flatgrp[actv.tag]}"
                            } else "☐　${actv.text}"
                            actv.setOnClickListener {
                                dialogBinding.opmornm.findViewById<RadioButton>(dialogBinding.opmornm.checkedRadioButtonId)
                                    ?.let { rBtn ->
                                        if (actv.text.startsWith(rBtn.tag as String) && actv.tag != null && actv.tag is String) {
                                            grpNums[rBtn.tag as String]?.remove(actv.tag)
                                            "☐　${(actv.tag as String).padStart(2)}".apply {
                                                actv.text = this
                                            }
                                        } else {
                                            if (grpNums[rBtn.tag as String]!!.size < maxItem) {
                                                grpNums.filter { k -> k != rBtn.tag }
                                                    .forEach { (_, list) ->
                                                        list.remove(actv.tag)
                                                    }
                                                grpNums[rBtn.tag as String]?.add(actv.tag as String)
                                                "${rBtn.tag} ${(actv.tag as String).padStart(2)}".apply { actv.text = this }
                                            }
                                        }
                                        alertDialog!!.PositiveButton.isEnabled = !grpNums.any{ ent-> ent.value.size in 1..2}
                                    }
                                grpNums.map { item ->
                                    "${item.key}：${item.value.sortedBy { it.trim().padStart(2, '0') }.joinToString(",") { m -> m.padStart(2) }}"
                                }.joinToString(System.lineSeparator()).apply {
                                    val sp = SpannableStringBuilder(this)
                                    sp.setSpan(ForegroundColorSpan(Color.BLUE), 0, sp.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    dialogBinding.selectednumbers.text = this
                                }
                            }
                        }
                    grpNums.map {
                        "${it.key}：${it.value.sortedBy { m->m.padStart(2, '0') }.joinToString(",") { m -> m.padStart(2) }}"
                    }.joinToString(System.lineSeparator()).apply {
                        dialogBinding.selectednumbers.text = this
                    }
                    alertDialog = AlertDialog.Builder(this, R.style.Theme_Monthly_Dialog)
                        .setIcon(R.drawable.baseline_refresh_24)
                        .setTitle(item.title)//.setView(R.layout.pause_dialog_layout)
                        .setNegativeButton(android.R.string.cancel) { _, _ -> }
                        .setView(dialogBinding.root)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            if (dialogBinding.opLike.isChecked) {
                                getSharedPreferences(NAME_NUM_GRP, MODE_PRIVATE).edit {
                                    clear()
                                    grpNums.flatMap { it.value.map { item-> item.trim() to it.key } }.sortedBy { it.first }
                                       .forEach {
                                           putString(it.first, it.second)
//                                            println("output grp ${it.first} ${it.second}")
                                    }
                                    apply()
                                }
                            }
                            blind = !dialogBinding.opInorder.isChecked
                            useGroup = dialogBinding.opLike.isChecked
                            numberordering =
                                if (dialogBinding.opInorder.isChecked) originalballs else originalballs.sortedBy { random() }
                            getSharedPreferences(NAME_ORDER, MODE_PRIVATE).edit() {
                                clear()
                                    .putString(KEY_ORDER, numberordering.idxString())
                                    .putBoolean(KEY_BLIND, blind)
                                    .putBoolean(KEY_GROUP, useGroup)
                                }
                            numberordering.parallelStream()
                                .forEach { it.status = NumStat.NUMSTATUS.UNSEL }
                            ResetNumberDialog()
                        }.create()
                    dialogBinding.opLike.setOnCheckedChangeListener { compoundButton, b ->
                        (compoundButton.isChecked).also {
                            with(if (b) View.VISIBLE else View.GONE) {
                                dialogBinding.viscon.visibility = this
                            }
                            alertDialog!!.PositiveButton.isEnabled =
                                (it&&grpNums.any { ent -> ent.value.size in 1..2 }).not()
                        }
                    }

                    alertDialog?.show()
                    dialogBinding.opLike.isChecked = false
                    alertDialog!!.PositiveButton.isEnabled = !grpNums.any { ent -> ent.value.size in 1..2 }
                    alertDialog != null
                }
                R.id.action_marksix -> {
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
                    alertDialog = AlertDialog.Builder(this, R.style.Theme_Monthly_Dialog).setMessage(R.string.disclaimer).setTitle(R.string.action_disclaimer).show()
                    alertDialog!=null
                }
                R.id.action_draw_schedule -> {
//                    try {
//                        genYi()
//                    } catch (e: Exception) {
//                        AlertDialog.Builder(this).setMessage(e.message).show()
//                    }

                    show_draw_schedule()

//                    }
                }
                R.id.action_previous_next_draw->{
                    val ssb = SpannableString(getString(R.string.action_previous_next_draw))
                    ssb.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    ssb.setSpan(BackgroundColorSpan(Color.LTGRAY), 0, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    AlertDialog.Builder(this, R.style.Theme_Monthly_Dialog)//.setTitle(ssb)
                        .setMessage(getDrawString()).show().also { dlg ->
                            alertDialog = dlg
                            val db = M6Db.getDatabase(this)
                            WebViewGetNextDraw(db.DrawResultDao()){ if (it!="") dlg.setMessage(getDrawString())}
                            hr.postDelayed({
                                UpdateLatestDraw(this) {
                                    runOnUiThread {
                                        if (it == "OK") {
                                            genBall(db.DrawResultDao())
                                            initball()
                                        }
                                        if (dlg.isShowing)
                                            dlg.setMessage(getDrawString())
                                    }
                                }
                            },800)
                        }
                    alertDialog!=null
                }
                R.id.action_save ->{
                    saveEntry()
                }
//                R.id.action_info ->{
//                    val ssb = SpannableStringBuilder()
//
//                    val dao = M6Db.getDatabase(this).DrawResultDao()
//
//                    val results = dao.getAll()
////                    File.createTempFile("source", ".json", cacheDir).writeText(gson.toJson(results, DrawResultArray::class.java))
//                    results.groupBy { it.id.substring(0,2) }.entries.parallelStream().forEach {
//                        ent->
//                        ent.value.sortedBy { it.date }.forEachIndexed { index, dr ->
//                            if (dr.id != String.format("%s/%03d", ent.key, index + 1))
//                                ssb.append(dr.id).append(System.lineSeparator()).append(jsonDate.format(dr.date))
//                        }
//                    }
//
//                    ssb.append("!1")
//                    ssb.appendLine(dao.getLatest().id)
//                    ssb.appendLine(getString(R.string.action_redraw))
//                    ssb.appendLine(getString(R.string.info_redraw))
//                        ssb.setSpan(ImageSpan(this, R.drawable.baseline_refresh_24), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//
//                    AlertDialog.Builder(this).setMessage(ssb).setPositiveButton("刪除") { _, _ ->
//                        dao.delete(dao.getLatest())
//                        initball()
//                    }.show().setOnCancelListener {
//                        pauseDlg.show()
//                    }
//                    true
//                }
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
                            di.PositiveButton.isVisible = it.toString() != msgNumbers
                        }
                    }.setNeutralButton(android.R.string.cancel) { dlg, _ ->
                        dlg.dismiss()
                    }.setPositiveButton(if(originalballs.any { it.status!=NumStat.NUMSTATUS.UNSEL }) R.string.message_replace else R.string.replace_numbers)  { dlg, _ ->
                        val selectedPosition = dlg.ListView.checkedItemPosition
                        if(dlg.ListView.indices.contains(selectedPosition)) {
                            val strings = adp.getItem(selectedPosition)?.split(m6_sep_banker) ?: listOf()
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
                            dlg.PositiveButton.isVisible=false
                            dlg.ListView.divider = AppCompatResources.getDrawable(this, android.R.drawable.divider_horizontal_dark)
                        }
                    }.show()
                    true
                }
                R.id.id_scanticket->{
//                    with(FD_WAIT_DIALOG) {
//                        if (supportFragmentManager.findFragmentByTag(this@with) == null) {
//                            val m6Fragment = M6Fragment.newInstance(",", "")
//                            m6Fragment.show(
//                                supportFragmentManager.beginTransaction(),
//                                this@with
//                            )
//                        }
//                    }

//                    launcherForOCR.launch(Intent(this, CameraScanActivity::class.java))
                    startActivity(Intent(this, CameraScanActivity::class.java))
                    true
                }
                else -> {
                    false
                }
            }
        }

    }
    fun launchLauncher(){
        launcherForOCR.launch(Intent(this, CameraScanActivity::class.java))
//        startActivity(Intent(this, ReadTicket::class.java))
    }
    @SuppressLint("SimpleDateFormat")
    private val launcherForOCR = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ rs->
        if(rs.resultCode == RESULT_OK) {
            with(TICKETRESULT) {
                rs.data?.let {
                    if(it.hasExtra(this)){
                        it.getStringExtra(this)?.let { nbs->
                            val other = (if(it.hasExtra(TICKETSTRING)) it.getStringExtra(TICKETSTRING)!! else "").split("#")
                            val dfm = SimpleDateFormat("ddMMMyy", Locale.ENGLISH)
                            val drawResultDao = M6Db.getDatabase(this@MainActivity).DrawResultDao()
                            val rec = drawResultDao.checkDrawBy(other[0], other[1], other[4].toInt())
                            val numsfmt = "相關攪珠${if(other[4]!="1") other[4] else ""}結果:" + System.lineSeparator() + if(rec.isEmpty()) getString(R.string.drawn_id_not_found) else {
                                rec.joinToString(System.lineSeparator()) { itm ->
                                    "${itm.id}:${
                                        itm.no.nos.joinToString(
                                            numberseperator
                                        )
                                    }$thinsp(${itm.sno})"
                                }

                            }

                            val sp = SpannableStringBuilder()
                            sp.appendLine("彩票期數:").appendLine(if (rec.isEmpty()) "${other[0]}/${other[1]} ${other[6]}" else {
                                "${rec.first().id} ${dfm.format(rec.first().date)}"
                            })
                            sp.appendLine("注項:")
                            sp.appendLine(nbs)
                            sp.appendLine()
                            sp.appendLine("相關攪珠${if(other[4]!="1") "${other[4]}期" else ""}結果:")
                            if (rec.isEmpty()) sp.append(getString(R.string.drawn_id_not_found))
                            else
                                for (itm in rec) {
                                    sp.appendLine(
                                        "${itm.id}:${
                                            itm.no.nos.joinToString(
                                                numberseperator
                                            )
                                        }$thinsp(${itm.sno})"
                                    )
                                }
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle(R.string.title_scanticket)
                                .setPositiveButton("再掃瞄") { _, _ -> launchLauncher() }
                                .setNeutralButton(android.R.string.copy) { _, _ -> }
                                .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
                                .setMessage(sp)
                                .show()
                        }
                    }
                }
            }
        } else if (rs.resultCode== RESULT_CANCELED){
            val title = rs.data?.getStringExtra(TICKETRESULT)?: "掃瞄彩票"
            AlertDialog.Builder(this@MainActivity).setTitle(title)
                .setPositiveButton("再掃瞄") { _, _ -> launchLauncher() }
                .setNeutralButton(android.R.string.cancel){ d,_->d.dismiss()}
                    .show()
        }
    }
    private fun populate_menu(){
        binding.toolbar.menu.findItem(R.id.action_view_all).also { v ->
            v.isVisible = blind
        }
    }
    private fun balldata(view: BallBinding, item: NumStat) {
        runOnUiThread {
            with(view) {
                arrayOf(item.since, item.times).joinToString(System.lineSeparator())
                    .also { idStatistics.text = it }

                idNumber.text =
                    if (blind && useGroup && item.status == NumStat.NUMSTATUS.UNSEL) getSharedPreferences(
                        NAME_NUM_GRP,
                        MODE_PRIVATE
                    )
                        .getString(item.num.toString(), item.numString) else item.numString

                idNumber.backgroundTintList = ColorStateList.valueOf(item.num.BallColor())
                imageView.text =
                    when (item.status) {
                        NumStat.NUMSTATUS.BANKER -> {
                            if (!idBallinfo.isChecked) this.idBallinfo.isChecked = true
                            getString(R.string.banker_indicator)
                        }

                        NumStat.NUMSTATUS.LEG -> {
                            if (!idBallinfo.isChecked) this.idBallinfo.isChecked = true
                            getString(R.string.leg_indicator)
                        }

                        NumStat.NUMSTATUS.UNSEL -> {
                            if (idBallinfo.isChecked) this.idBallinfo.isChecked = false
                            ""
                        }
                    }

                imageView.isVisible = item.status != NumStat.NUMSTATUS.UNSEL
            }
        }
    }

    fun saveEntry():Boolean{
        return with(getSharedPreferences(NAME_ENTRIES, MODE_PRIVATE)){

            if(!contains(msgNumbers)){
                edit {putLong(msgNumbers, System.currentTimeMillis())}
                Toast.makeText(this@MainActivity, R.string.message_save, Toast.LENGTH_SHORT).show()
                if(BuildConfig.DEBUG) {
                    edit {

                        all.entries.forEach{
                            val k = it.key.split("\\s*\\+\\s*").joinToString(numberseperator)

                            val parse = if(it.value is String) Date.parse(it.value as String) else it.value as Long
                            remove(it.key)
                            putLong(k, parse)
                        }

                    }
                }
                if(all.size>6)
                    edit(){ remove(all.entries.last().key) }
                true
            }     else false
        }
    }
    fun drawSpannableLine(line:Pair<String, String>, ssb:SpannableStringBuilder){
        val (name, value) = line
        ssb.append("$name$nbsp： ")

        val start = ssb.length
        ssb.append(value)
        ssb.setSpan(TextAppearanceSpan(this, R.style.money), start, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if(name.contains('金') || name.contains('奬')) {
            val end = if(value.contains('\t')) value.indexOfFirst { it == '\t' }+start else ssb.length
            ssb.setSpan(
                ForegroundColorSpan(RED),
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

        val pdata = mutableListOf(
            "攪珠期數" to pre.id,
            "攪珠日期" to sdf_display.format(pre.date),
            "攪珠結果" to "${pre.no.nos.joinToString(numberseperator)}$thinsp(${pre.sno})",
            "總投注額" to "$dollar${pre.inv}",
            "頭奬 " to if (pre.p1 == null) "$dotdotdot" else "${pre.p1}$emsp${pre.p1u}",
            "二奬 " to if (pre.p2 == null) "$dotdotdot" else "${pre.p2}$emsp${pre.p2u}",
            "三奬 " to if (pre.p3 == null) "$dotdotdot" else "${pre.p3}$emsp${pre.p3u}",
        )
        ssb.append("${emsp}上期攪珠").appendLine()
        start = ssb.length
        ssb.setSpan(StyleSpan(Typeface.BOLD), 0, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
            ssb.append(getString(R.string.nothing_to_show)).setSpan(
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
        }
        ssb.appendLine().appendLine()
        start = ssb.length
        val updateat = sf.getDateTimeISOFormat(KEY_NEXT_UPDATE) ?: "--"
        ssb.append(getString(R.string.last_update_at, updateat)).setSpan(
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

        return ssb
    }
    private fun show_draw_schedule(): Boolean {

        if (supportFragmentManager.findFragmentByTag(TAG_BALL_DIALOG) == null) {

            hr.post {
                getLatestSchecule(this)
                val monthlyDrawScheduleFragment = MonthlyDrawScheduleFragment.newInstance()
                monthlyDrawScheduleFragment.show(
                    supportFragmentManager.beginTransaction(),
                    TAG_BALL_DIALOG
                )
            }
        }
        return true
    }

    @SuppressLint("SuspiciousIndentation")
    private fun show_checking(): Boolean {
/*        if(currentStatus!=DrawStatus.UnClassify){
            latestDrawResult?.let {rs->
                val max1 = 6 - bankers.size
                val scheduleAll = getScheduleAll(this)
                val today = Calendar.getInstance()
//            today.add(Calendar.DATE, 14)

//                msgMatch =if(rs.date in scheduleAll.filter { it.first <= today }.map{it.first.time}){
//                    val m6 = rs.no.nos.intersect(bankers).plus(rs.no.nos.intersect(legs).take(max1))
//                         rs.id+" : "+
//                    rs.no.nos.map{ if(m6.contains(it)) "<$it>" else it}.plus(if(rs.sno in bankers || rs.sno in legs) "(<${rs.sno}>)" else "(${rs.sno})").joinToString(
//                        numberseperator)
//                } else ""
            }
        }*/
        startActivity(Intent(this, DrawnNumberCheckingActivity::class.java))
        return true
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putIntArray(KEY_ORDER, numberordering.map { originalballs.indexOf(it) }.toIntArray())
//        outState.putStringArray(KEY_STATUS, originalballs.map{ it.status.toString()}.toTypedArray())
//    }

    override val adUnitStringId: Int = R.string.admob_m6_lottery
    override fun onAdLoaded() {
    }

    private fun updatemark(old: NumberTextviewBinding, new: NumberTextviewBinding) {

        old.idBackground.apply {
            setImageResource(R.drawable.pen_unmark_ani_vec)
            (drawable as AnimatedVectorDrawable).also {
                it.registerAnimationCallback(object : Animatable2.AnimationCallback() {

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
        if (reset) {
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
            item.status = NumStat.NUMSTATUS.UNSEL
            m6b.idNumber.text=item.numString
        } else {
            when (item.status) {
                NumStat.NUMSTATUS.LEG -> {
                    item.status = NumStat.NUMSTATUS.BANKER
                    m6b.imageView.isVisible = true
                    updatemark(leg, banker)
                }

                NumStat.NUMSTATUS.BANKER -> {
                    item.status = NumStat.NUMSTATUS.LEG
                    m6b.imageView.isVisible = true
                    updatemark(banker, leg)
                }

                NumStat.NUMSTATUS.UNSEL -> {
                    item.status = NumStat.NUMSTATUS.LEG
                    m6bViews[index].idNumber.text=item.numString
                    m6b.imageView.isVisible = true
                    leg.idBackground.apply {
                        setImageResource(R.drawable.pen_mark_ani_vec)
                        (drawable as AnimatedVectorDrawable).start()
                    }

//                    val targets = originalballs.filter { (item.idx + 1 == it.idx || item.idx - 1 == it.idx) &&it.status == NumStat.NUMSTATUS.UNSEL}

//                    targets.forEach {
//                            println("中招號碼 ${numberordering.indexOf(it)}")
//
//                    }
                    /*targets.forEach {
                        m6bViews[numberordering.indexOf(it)].imageBeside.isVisible = true
                    }*/
//                    m6b.idDrawtype.text="L"
                }
            }
            m6b.idNumber.backgroundTintList =
                ColorStateList.valueOf(item.num.BallColor())
            m6b.imageView.text =
                when (item.status) {
                    NumStat.NUMSTATUS.BANKER -> {
//                        m6b.idBallinfo.performClick()
                        if(!m6b.idBallinfo.isChecked) m6b.idBallinfo.isChecked=true
                        getString(R.string.banker_indicator)
                    }
                    NumStat.NUMSTATUS.LEG -> {
//                        m6b.idBallinfo.performClick()
                        if(!m6b.idBallinfo.isChecked) m6b.idBallinfo.isChecked=true
                        getString(R.string.leg_indicator)
                    }
                    NumStat.NUMSTATUS.UNSEL -> {
                        m6b.idBallinfo.isChecked=false
                        ""
                    }
                }
//            updateStatus()
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
        println("$TAG_BALL_DIALOG ${calcDrawStatus.first}\\")
        if (currentStatus != calcDrawStatus.first) {
            changeStatus(currentStatus, false)
            changeStatus (calcDrawStatus.first, true)
        }
        println("$TAG_BALL_DIALOG ${calcDrawStatus.first}/")


        currentStatus = calcDrawStatus.first
    }

    private fun WebViewGetNextDraw(drawResultDao: DrawResultDao, exec: (message: String) -> Unit) {
        val nextref = getSharedPreferences("NEXTDRAW", Context.MODE_PRIVATE)
        nextref.getDateTimeISO(KEY_NEXT_UPDATE)?.let {
            val now = Calendar.getInstance()
            val latest = drawResultDao.getLatest()
            if (!it.isEarlyBy(
                    now, Calendar.MINUTE,
                    when {
                        latest.p1 == null || (nextref.getString(KEY_NEXT, "")
                            ?: "").contains(latest.id) -> 2

                        (nextref.getString(KEY_NEXT_UPDATE, "")
                            ?: "").contains("估計頭獎基金${indexTD}-") -> 3

                        else -> 15
                    }
                )
            ) {
                return
            }
        }
        val wv = WebView(this)
        with(wv.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = false
        }
        var targetfound = ""
        var isstart = false
        val cntr = object : CountDownTimer(800 * 5, 800) {
            val jsCode = "(function() {" +
                    "  var element = document.querySelector('.next-draw-table-container');" +
                    "  return element ? element.innerText : 'Element not found';" +
                    "})();"

            override fun onTick(millisUntilFinished: Long) {
                wv.evaluateJavascript(jsCode) { value ->
                    if (targetfound == "" && value != "\"Element not found\"") {
                        targetfound = value
                        Log.d("WebView", "Extracted data: $value")
                        nextref.edit {
                            putDateTimeISO(KEY_NEXT_UPDATE, Calendar.getInstance())
                            val items = targetfound.trim('"').split("\\n", "\\t")
                                .filterIndexed { index, _ -> index > 0 }
                                .chunked(2) { it.joinToString(indexTD) }
                                .joinToString(indexTR)
                            putString(KEY_NEXT, items)
                        }
                        exec(targetfound)
                    }
                }
            }

            override fun onFinish() {
                Log.d("WebView", "Finished")
                wv.destroy()
            }
        }
        wv.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (!isstart) {  //handling start once, because onPageFinished will be called twice.
                    Log.d("WebView", "start $url")
                    isstart = true
                    cntr.start()
                }
            }
        }
        wv.loadUrl("https://bet.hkjc.com/ch/marksix")
    }

    private fun ResetNumberDialog() {
        Dialog(this, R.style.Theme_Wait_Dialog).apply{

            setContentView(R.layout.pause_dialog_layout)

            setCancelable(false)
            setOnShowListener {
                if (isNetworkAvailable(this@MainActivity)) {
                    val db = M6Db.getDatabase(this@MainActivity)
//                WebViewGetNextDraw(db.DrawResultDao()){}
                    hr.postDelayed({
                        UpdateLatestDraw(this@MainActivity) { ok ->
                            runOnUiThread {
                                binding.toolbar.menu.findItem(R.id.action_view_all).run {
                                    isVisible = blind
                                }
                                if (ok == "OK") {
                                    genBall(db.DrawResultDao())
                                }
                                dismiss()
                                updateStatus()
                                binding.root.setWillNotDraw(true)
                                initball()
                                binding.root.setWillNotDraw(false)
                            }
                        }
                    }, 10)
                } else
                    dismiss()
            }
            show()
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            return networkCapabilities != null && (
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    )
//        } else {
//            @Suppress("DEPRECATION")
//            val activeNetworkInfo = connectivityManager.activeNetworkInfo
//            @Suppress("DEPRECATION")
//            return activeNetworkInfo != null && activeNetworkInfo.isConnected
//        }
    }

    private fun initball() {

            numberordering.indices.toList().parallelStream().forEach {
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
                        /*
                                                                m6bViews[it].imageBeside.isVisible = originalballs.filter { item.idx + 1 == it.idx || item.idx - 1 == it.idx }
                                                                    .any { it.status != NumStat.NUMSTATUS.UNSEL }
                        */
                    }
                }
                balldata(m6bViews[it], item)
            }
    }
    private fun calcDrawStatus(leg: Int, ban: Int): Pair<DrawStatus, Int> {
        msgNumbers = "+"
        if (ban > 5) {
            msgCalc = getString(R.string.unclassifydraw, ban, leg)
            msgNumbers = getString(R.string.msg_too_many_banker)
            return DrawStatus.UnClassify to 0
        }
        if (leg + ban < 7) return if (leg == 6) {
            msgCalc = getString(R.string.singledraw)
            msgNumbers = originalballs.filter { it.status == NumStat.NUMSTATUS.LEG }
                .joinToString(numberseperator) { it.numString }
            DrawStatus.Single to 1
        } else {
            msgCalc = getString(R.string.unclassifydraw, ban, leg)
            msgNumbers = if(ban==0) getString(R.string.msg_incomplete_single) else getString(R.string.msg_incomplete_banker)
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
            msgNumbers = originalballs.filter { it.status == NumStat.NUMSTATUS.LEG }.joinToString(
                numberseperator
            ) { it.numString }
            DrawStatus.Multiple to leg
        } else {
            msgCalc = getString(R.string.bankerdraw, draw, ban, leg)
            val l = originalballs.filter { it.status == NumStat.NUMSTATUS.LEG }.joinToString(
                numberseperator
            ) { it.numString }
            val b = originalballs.filter { it.status == NumStat.NUMSTATUS.BANKER }.joinToString(
                numberseperator
            ) { it.numString }
            msgNumbers = "$b>$l"
            DrawStatus.Banker to draw
        }
    }
    private fun genBall(drawResultDao: DrawResultDao):Boolean {
        latestDrawResult = drawResultDao.getLatest()
        val dr = drawResultDao.getAll()
        val temp = dr.sortedByDescending { it.date }
            .filter { it.date >= dateStart }
            .mapIndexed { index, data -> index to data.no.nos.plus(data.sno) }
        originalballs.parallelStream().forEach { i ->
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
    fun genYi() {
        val guaNames = listOf(
            "地势坤", "震为雷", "坎为水", "兑为泽",
            "艮为山", "离为火", "巽为风", "天行健（乾）"
        )
        val temp = resources.getStringArray(R.array.cleromancy)
        val ans = mutableSetOf<Int>()
        while (ans.size<6) {
            val i = StringBuilder()
            (1..6).toList().parallelStream().forEach {
                i.append( (0..1).sortedBy { random() }.first())
            }
            val idx = temp.indexOf(i.toString())
            if (idx == -1) throw Exception("error $i") else {
                (if(idx>48) idx-49 else idx).run{
                    if(!ans.contains(this))
                        ans.add(this)
                }
            }
        }
        ans.forEach{
            updateball(it)
        }
    updateStatus()
//        val d = temp.distinct()
        for (i in 0 until 64) {
            val binaryString = Integer.toBinaryString(i).padStart(6, '0')
            val guaName = guaNames[i % 8]
//            println("$binaryString $guaName")
        }
    }
    fun allCombination(): MutableList<List<Int>> {
        val combinations = mutableListOf<List<Int>>()
        val m = 6 - bankers.size

        //generates all possible combinations of legs using bit manipulation
        for (i in 0 until (1 shl legs.size)) {
            val combination = mutableListOf<Int>()
            for (j in legs.indices) {
                if ((i and (1 shl j)) > 0) {
                    combination.add(legs.elementAt(j))
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
        const val nbsp = '\u00A0'
        const val emsp = '\u2003'
        const val ensp = '\u2002'
        const val thinsp = '\u2009'
        const val dollar = '\uFE69'
        const val emdash = '\u2014'
        const val dotdotdot = '\u2026'

        const val numberseperator = "$thinsp+"
        const val m6_49StartDate = "2002/07/04"
        const val m6_sep_num = "+"
        const val m6_sep_banker = ">"
        val ballcolor = mutableListOf<Int>()

        val bankers get() = originalballs.filter { it.status == NumStat.NUMSTATUS.BANKER }.map { it.num }.toSet()
        val legs get() = originalballs.filter { it.status== NumStat.NUMSTATUS.LEG }.map{it.num}.toSet()
        var msgNumbers:String = "+"
//        var msgMatch:String = "?"
        var msgCalc:String = "good luck!!"

        val userChoices get() = originalballs.filterNot { it.status == NumStat.NUMSTATUS.UNSEL }.map { it.num }
        var latestDrawResult :DrawResult?=null
        private val originalballs = (1..49).withIndex()//.sortedBy { Math.random() }
            .map { NumStat(it.value, it.index) }
        private var numberordering = originalballs.toList()
        private var currentStatus = DrawStatus.UnClassify
        fun nextCount(sel: Int): Boolean =
            numberordering.filter { it.status != NumStat.NUMSTATUS.UNSEL }.map { (it.num - sel) }
                .any { (it == -1 || it == 1) }

        val minTimes get() = numberordering.filter { it.times != -1 }.minOf { it.times }
        val maxTimes get() = numberordering.maxOf { it.times }
        val maxSince get() = numberordering.maxOf { it.since }
        val dateStart get() = sqlDate.parse(m6_49StartDate)
        val sdf_display = SimpleDateFormat("dd/MM/yyyy (EEEE)", Locale.CHINESE)
        var blind: Boolean = false
        var useGroup: Boolean = false
        private fun String.stringIdx() = if(this.isEmpty()) listOf<Int>() else this.split(",").map{ it.trim().toInt() }
        private fun List<NumStat>.idxString() = this.joinToString(",") { it.idx.toString() }
    }

    override fun getItem(idx: Int) = numberordering[idx]

//    @SuppressLint("SuspiciousIndentation")
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
                        idNumber.text=that.numString
                        "${that.since}\n${that.times}".also { idStatistics.text = it }
                    }
                } else
                    ordering.add(i, n)
            }
            numberordering = ordering
/*            numberordering.filter { it.status == NumStat.NUMSTATUS.UNSEL }.forEach { next ->
                val filter =
                    numberordering.filter { it.idx == next.idx + 1 || it.idx == next.idx - 1 }
                m6bViews[numberordering.indexOf(next)].imageBeside.isVisible =
                    (filter.any { it.status != NumStat.NUMSTATUS.UNSEL })
            }*/

            updateStatus()
            return item
        } else
            return updateball(index, reset)
    }
}