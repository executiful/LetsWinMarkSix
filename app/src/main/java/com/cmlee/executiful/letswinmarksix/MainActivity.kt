package com.cmlee.executiful.letswinmarksix

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cmlee.executiful.letswinmarksix.BallDialogFragment.Companion.TAG_BALL_DIALOG
import com.cmlee.executiful.letswinmarksix.BallDialogFragment.Companion.newInstance
import com.cmlee.executiful.letswinmarksix.databinding.ActivityMainBinding
import com.cmlee.executiful.letswinmarksix.databinding.BallviewBinding
import com.cmlee.executiful.letswinmarksix.databinding.ColumnOfNumberBinding
import com.cmlee.executiful.letswinmarksix.databinding.NumberTextviewBinding
import com.cmlee.executiful.letswinmarksix.databinding.PastDrawLayoutBinding
import com.cmlee.executiful.letswinmarksix.databinding.PastItemBinding
import com.cmlee.executiful.letswinmarksix.helper.BannerAppCompatActivity
import com.cmlee.executiful.letswinmarksix.helper.ConnectURLThread
import com.cmlee.executiful.letswinmarksix.helper.DayYearConvert
import com.cmlee.executiful.letswinmarksix.helper.DayYearConvert.Companion.jsonDate
import com.cmlee.executiful.letswinmarksix.model.NumStat
import com.cmlee.executiful.letswinmarksix.model.NumStat.Companion.BallColor
import com.cmlee.executiful.letswinmarksix.model.drawYears.DrawYear
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResult
import com.cmlee.executiful.letswinmarksix.roomdb.M6Db
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import java.io.File

class MainActivity : BannerAppCompatActivity(), BallDialogFragment.IUpdateSelection {
    private lateinit var binding: ActivityMainBinding
    private val numbers = (1..49).map{it.toString()}
    private lateinit var legViews :List<NumberTextviewBinding>
    private lateinit var bankerViews : List<NumberTextviewBinding>
    private lateinit var m6bViews :List<BallviewBinding>
    private lateinit var db :M6Db
//    private lateinit var threadconn:ConnectURLThread
    @SuppressLint("FileEndsWithExt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(mainLooper).post{
            cacheDir.listFiles()?.forEach {
                if(it.endsWith(".json")) {
                    if (it.length() == 0L || it.readText() == "[]") it.delete()
                }else if (it.endsWith(".txt")) {
                    val fromJson = Gson().fromJson(it.readText(), DrawYear::class.java)
                    File(it.name.replace(".txt", ".json")).writeText(Gson().toJson(fromJson, DrawYear::class.java))
                }
            }

        }
        adContainerView = binding.adViewContainer
//        setSupportActionBar(binding.toolbar)
        binding.toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.action_generate-> {
                    refresh()
                    true
                }
                R.id.action_past->{
                    show_past()
                }
                R.id.action_coming->{
                    show_coming()
                }
                else -> {false}
            }
        }
        binding.idBallselect.removeAllViews()
        binding.ticketlayout.idLegs.removeAllViews()
        binding.ticketlayout.idBankers.removeAllViews()
         (1..49).let {
            legViews = it.map{ NumberTextviewBinding.inflate(layoutInflater) }
            bankerViews = it.map{ NumberTextviewBinding.inflate(layoutInflater) }
            m6bViews = it.map{ BallviewBinding.inflate(layoutInflater)}
        }
        db = M6Db.getDatabase(this)
        val spec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        ConnectURLThread(db, cacheDir, getString(R.string.lang)).start()

        m6bViews.forEachIndexed { index, it ->
            val layoutParams = GridLayout.LayoutParams(spec, spec)
            layoutParams.width=0
            layoutParams.height=0
            binding.idBallselect.addView(it.root, layoutParams)
            it.idSwitch.setFactory {
                val iv = ImageFilterView(applicationContext)
                iv.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
                iv.scaleType = ImageView.ScaleType.FIT_CENTER
                iv.background = AppCompatResources.getDrawable(this, android.R.color.holo_orange_light)
                iv
            }
            it.idSwitch.setOnClickListener {
                updateball(index)
            }
            it.idSwitch.setOnLongClickListener{
//                val(_, item) = numberording[index]
                if (supportFragmentManager.findFragmentByTag(TAG_BALL_DIALOG) == null) {
                    val phraseDialog = newInstance(index)
                    phraseDialog.show(supportFragmentManager.beginTransaction(), TAG_BALL_DIALOG)
                }
                true
            }
        }
        val iterator = numbers.withIndex().iterator()
        while (iterator.hasNext()) {
            val numB =
                ColumnOfNumberBinding.inflate(layoutInflater).also { it.root.removeAllViews() }
            val numL =
                ColumnOfNumberBinding.inflate(layoutInflater).also { it.root.removeAllViews() }
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
        redraw()
//        refresh()
    }

    private fun show_coming():Boolean {
        AlertDialog.Builder(this).setTitle(R.string.action_generate).setMessage("nothing").show()
        return true
    }

    class pastVH(private val itemBinding: PastItemBinding) : RecyclerView.ViewHolder(itemBinding.root){
        fun ballchip(num:Int, special:Boolean=false) : Chip {
            val chip = Chip(itemBinding.root.context)
            chip.text = num.toString()
            chip.textSize = itemBinding.root.resources.getDimension(R.dimen.ball_txt_size)
//            if(special) chip.chipIcon= AppCompatResources.getDrawable(itemBinding.root.context, android.R.drawable.checkbox_on_background)
            chip.chipBackgroundColor=ColorStateList.valueOf(num.BallColor())// ResourcesCompat.getColorStateList(itemBinding.root.resources, R.color.ball_red, null)
            return chip
        }
        @SuppressLint("SetTextI18n")
        fun bind(drawResult: DrawResult){
            itemBinding.dateid.text = "${drawResult.id} ${jsonDate.format(drawResult.date)} ${drawResult.sbnameC}"
//            itemBinding.nosno.text = drawResult.no.nos.plus(drawResult.sno).joinToString()

            drawResult.no.nos.forEach {
                itemBinding.nosno.addView(ballchip(it))
            }
            itemBinding.nosno.addView(ballchip(drawResult.sno, true))
        }
    }
    class pastAdp(private val pastDraws:List<DrawResult> ):RecyclerView.Adapter<pastVH>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): pastVH {
            val binding = PastItemBinding.inflate(LayoutInflater.from(parent.context))
            return pastVH(binding)
        }

        override fun onBindViewHolder(holder: pastVH, position: Int) {
            holder.bind(pastDraws[position])
        }

        override fun getItemCount() = pastDraws.size
    }

    private fun show_past():Boolean {
        val pastdraw10 = db.DrawResultDao().getAll().take(10)
//        if(pastdraw10.isEmpty()){
//            AlertDialog.Builder(this).me
//        }
        val screenWidthDp = resources.configuration.screenWidthDp
        val dlg = AppCompatDialog(this, R.style.Theme_Ball_Dialog)
//        val gridPast = RecyclerView(this)
//        gridPast.layoutManager = LinearLayoutManager(this).also { it.orientation = LinearLayoutManager.VERTICAL }

//        gridPast.adapter = pastAdp(pastdraw10)
        val pastitemlayout = PastDrawLayoutBinding.inflate(layoutInflater)
        pastitemlayout.drawlist.adapter = pastAdp(pastdraw10)
        dlg.setContentView(pastitemlayout.root)
        dlg.show()
        return true
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        redraw()
        super.onRestoreInstanceState(savedInstanceState, persistentState)
    }
    override val adUnitId = AD_UNIT_ID_SAMPLE
    override fun onAdLoaded() {
    }
    fun updatemark(old:NumberTextviewBinding, new:NumberTextviewBinding ){

        old.idBackground.apply {
            setImageResource(R.drawable.pen_unmark_ani_vec)
            (drawable as AnimatedVectorDrawable).also{
                it.registerAnimationCallback(object: Animatable2.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        new.idBackground.setImageResource(R.drawable.pen_mark_ani_vec)
                        (new.idBackground.drawable as AnimatedVectorDrawable).start()
                        super.onAnimationEnd(drawable)
                    }
                })
                it.start()}
        }
    }
    @SuppressLint("SuspiciousIndentation")
    fun updateball(index:Int, reset:Boolean = false): NumStat {
        val (idx,item) = numberording[index]
        val leg = legViews[idx]
        val banker = bankerViews[idx]
        val m6b = m6bViews[index]
        val nxv = m6b.idSwitch.nextView as ImageFilterView

            nxv.setColorFilter(item.num.BallColor())
        if(reset){
            m6b.idProgress.max = maxTimes
            m6b.idProgress.progress = item.times// - min1
            m6b.idStat2.text = item.times.toString()
            m6b.idStat1.text = item.since.toString()
            m6b.idBallnumber.text = item.numString
//            m6b.idSwitch.showNext()
            banker.idBackground.setImageResource(R.drawable.ticket_number)
            leg.idBackground.setImageResource(R.drawable.ticket_number)
            when (item.status) {
                NumStat.NUMSTATUS.LEG ->
                    leg.idBackground.setImageResource(R.drawable.pen_mark_ani_vec)
                NumStat.NUMSTATUS.BANKER ->
                    banker.idBackground.setImageResource(R.drawable.pen_mark_ani_vec)
                NumStat.NUMSTATUS.UNSEL -> {}
            }
            nxv.setImageResource(R.drawable.ic_baseline_radio_button_unchecked_24)
            item.status = NumStat.NUMSTATUS.UNSEL
            m6b.idSwitch.showNext()

        } else {
            when (item.status) {
                NumStat.NUMSTATUS.LEG -> {
                    nxv.setImageResource(R.drawable.ic_baseline_push_pin_24)
                    item.status = NumStat.NUMSTATUS.BANKER
                    updatemark(leg, banker)
                }
                NumStat.NUMSTATUS.BANKER -> {
                    nxv.setImageResource(R.drawable.ic_baseline_star_24)
                    item.status = NumStat.NUMSTATUS.LEG
                    updatemark(banker, leg)
                }
                NumStat.NUMSTATUS.UNSEL -> {
                    nxv.setImageResource(R.drawable.ic_baseline_star_24)
                    item.status = NumStat.NUMSTATUS.LEG
                    m6bViews[index].idBallnumber.text = item.numString
                    leg.idBackground.apply {
                        setImageResource(R.drawable.pen_mark_ani_vec)
                        (drawable as AnimatedVectorDrawable).start()
                    }
                }
            }
            m6b.idSwitch.showNext()
        }
        return item
    }

    fun redraw(reset:Boolean=true){
        Handler(Looper.getMainLooper()).post{
            genBall(db, numberording)
            numberording.parallelStream().forEach { (first) ->
                runOnUiThread {
                    updateball(first, reset)
                }
            }
            println("this is parallel...finished")
        }
    }
    fun refresh(){
        numberording= (1..49).withIndex().sortedBy { Math.random() }
            .map { i ->  i.index to NumStat(i.value, 0, i.index)  }
        genBall(db, numberording)

        redraw()
    }
    companion object {
        private var numberording = (1..49).withIndex()//.sortedBy { Math.random() }
            .map { i -> i.index to NumStat(i.value, i.index) }

        val minTimes get() = numberording.filter { it.second.times !=-1 }.minOf { it.second.times }
        val maxTimes get() = numberording.maxOf { it.second.times }// - min1
        val maxSince get() = numberording.maxOf { it.second.since }

        fun genBall(db: M6Db, order: List<Pair<Int, NumStat>>) {
            val drawResultDao = db.DrawResultDao()
            val dr = drawResultDao.getAll()
            val temp = dr.sortedByDescending { it.date }
                .filter { it.date >= DayYearConvert.sqlDate.parse("2002/07/04") }
                .mapIndexed { index, data -> index to data.no.nos.plus(data.sno) }
            for ((_, i) in order) {
                val f = temp.filter { it.second.contains(i.num) }
                i.times = f.count()
                if (i.times > 0)
                    i.since = f.first().first
                else
                    i.since = -1
            }
        }
    }

    override fun getItem(idx: Int) = numberording[idx].second

    override fun toggle(index: Int, reset:Boolean): NumStat {
        return updateball(index, reset)
    }
}