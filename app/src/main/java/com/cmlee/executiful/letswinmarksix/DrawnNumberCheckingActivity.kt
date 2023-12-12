package com.cmlee.executiful.letswinmarksix

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.bankers
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.dateStart
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.legs
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.msgNumbers
import com.cmlee.executiful.letswinmarksix.databinding.ActivityDrawnNumberCheckingBinding
import com.cmlee.executiful.letswinmarksix.databinding.DrawnListItemBinding
import com.cmlee.executiful.letswinmarksix.helper.BannerAppCompatActivity
import com.cmlee.executiful.letswinmarksix.helper.DayYearConverter.Companion.sqlDate
import com.cmlee.executiful.letswinmarksix.model.NumStat.Companion.BallColor
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResult
import com.cmlee.executiful.letswinmarksix.roomdb.M6Db
import com.google.android.material.tabs.TabLayout
import kotlin.streams.toList

class DrawnNumberCheckingActivity : BannerAppCompatActivity(),
    TabLayout.OnTabSelectedListener/*, OnBackPressedCallback*/ {
private lateinit var db:M6Db
    private val ht = HandlerThread("m6thread")
    private lateinit var hr :Handler
    private val TAG = "DrawnNumberChecking"
    private lateinit var binding: ActivityDrawnNumberCheckingBinding
    private val adps = mutableListOf<DrawnAdapter>()
    private var bpcb = object : OnBackPressedCallback(
        true // default to enabled
    ) {
        override fun handleOnBackPressed() {
            finish()
        }
    }
    class DrawnVH(val item:DrawnListItemBinding):RecyclerView.ViewHolder(item.root){
//        private val intersect = bankers.plus(legs)
//@SuppressLint("SuspiciousIndentation")
    fun setnum(context: Context, c: Button, i: Int, match:Boolean) {
        val sp = SpannableString(i.toString())
        if (match) {
            sp.setSpan(UnderlineSpan(), 0, sp.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            c.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        }
        c.text = sp
        c.backgroundTintList =
            ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)), intArrayOf(i.BallColor()))
    }
        fun bind(ints: Pair<List<Boolean>, DrawResult>, context: Context) {
            with(ints.second) {
                setnum(context, item.chip1st, no.nos[0], ints.first[0])
                setnum(context, item.chip2nd, no.nos[1], ints.first[1])
                setnum(context, item.chip3rd, no.nos[2], ints.first[2])
                setnum(context, item.chip4th, no.nos[3], ints.first[3])
                setnum(context, item.chip5th, no.nos[4], ints.first[4])
                setnum(context, item.chip6th, no.nos[5], ints.first[5])
                setnum(context, item.chipsp, sno, ints.first[6])
            }

            "${context.getString(R.string.draw_number, ints.second.id)} ${ints.second.sbnameC}, ${System.lineSeparator()}${context.getString(R.string.draw_date, sqlDate.format(ints.second.date))}".apply { /* todo: show noof match entries */
                item.idDate.text = this
            }
//            item.idDate.setCompoundDrawablesRelativeWithIntrinsicBounds(if(ints.first>=6.0) R.drawable.baseline_monetization_on_24 else 0,0,0,0)
        }
    }
    class DrawnAdapter(val result: List<Pair<List<Boolean>, DrawResult>>, val context: Context) : RecyclerView.Adapter<DrawnVH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawnVH {
            val binding = DrawnListItemBinding.inflate(LayoutInflater.from(parent.context))
            return DrawnVH(binding)
        }

        override fun onBindViewHolder(holder: DrawnVH, position: Int) {
            holder.bind(result[position], context)
        }

        override fun getItemCount() = result.size
    }

    override val adUnitId = AD_UNIT_ID_SAMPLE

    override fun onAdLoaded() {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDrawnNumberCheckingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adContainerView = binding.adViewContainer
        onBackPressedDispatcher.addCallback(bpcb)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.tabPrize.isHorizontalFadingEdgeEnabled = true
        binding.tabPrize.addOnTabSelectedListener(this)
        val waitdlg = AlertDialog.Builder(this, R.style.Theme_Wait_Dialog).setView(R.layout.pause_dialog_layout).create()
        waitdlg.setCanceledOnTouchOutside(false)
        ht.start()
        hr = Handler(ht.looper)
        waitdlg.setOnShowListener {
            hr.post{
                db = M6Db.getDatabase(this)
                val allresult = db.DrawResultDao().getAll().filter{ it.date >= dateStart }
                val max1 = 6-bankers.size

                val gb = allresult.parallelStream().map { rs ->
                    val m6 = rs.no.nos.intersect(bankers).plus(rs.no.nos.intersect(legs).take(max1))
                    rs.no.nos.map { it in m6 }.plus( rs.sno in bankers || rs.sno in legs) to rs
                }.filter {  it.first.take(6).count { it } >=3}.toList().groupBy {
                    when(it.first.take(6).count{it}){
                        6->{0}
                        5->{if(it.first[6]) 1 else 2 }
                        4->{if(it.first[6]) 3 else 4 }
                        3->{if(it.first[6]) 5 else 6 }
                        else ->-1
                    }
                }//.filter { it.key>=3 }

//                adps = gb.values.map { DrawnAdapter(it, this) }
//                val result =
//                        allresult.parallelStream().map { it.no.nos.intersect(bankers).plus(it.no.nos.intersect(legs).take(max1)) to it }.filter { it.first.size >= 3 }.toList()
                runOnUiThread{
                    waitdlg.dismiss()
//                    binding.idPrizeList.adapter = adps[0]
                        //DrawnAdapter(result, this)
                    resources.getStringArray(R.array.prize).forEachIndexed { index, s ->
                        gb.get(index)?.let {
                            val ssb = SpannableStringBuilder(s)
//                            ssb.appendLine()

                            ssb.append("(${it.size})")
//                            ssb.setSpan(SubscriptSpan(), 3, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//                            ssb.setSpan(SuperscriptSpan(), 1, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//                            ssb.setSpan(TextAppearanceSpan(this, android.R.style.TextAppearance_Holo_Small), 3, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            adps.add(DrawnAdapter(it, this))
                            val t = binding.tabPrize.newTab().setText(s)
                            t.text = "$s(${it.size})"
                            binding.tabPrize.addTab(t)
                        }
                    }

                    binding.tabPrize.selectTab(binding.tabPrize.getTabAt(0))
                }
            }
        }
        waitdlg.show()
        binding.toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.action_entry->{
                    AlertDialog.Builder(this).setMessage(msgNumbers).show()
                    true
                }
                R.id.action_details->{
                    val temp = "頭獎 選中6個「攪出號碼」 獎金會因應該期獲中頭獎注數而有所不同，每期頭獎獎金基金\n" +
                            "訂為不少於港幣800萬元。 二獎 選中5個「攪出號碼」+「特別號碼」 獎金會因應該期獲中二獎注數而有所不同 三獎 選中5個「攪出號碼」 獎金會因應該期獲中三獎注數而有所不同 四獎 選中4個「攪出號碼」+「特別號碼」 固定獎金港幣9,600元 五獎 選中4個「攪出號碼」 固定獎金港幣640元 六獎 選中3個「攪出號碼」+「特別號碼」 固定獎金港幣320元 七獎 選中3個「攪出號碼」 固定獎金港幣40元"
                    AlertDialog.Builder(this).setMessage(temp.replace(" ", "\n")
                        /*                        "頭奬:選中6個號碼\n" +
                                                    "二奬:5個號碼+特別號碼\n" +
                                                    "三奬:5個號碼\n" +
                                                    "四奬:4個號碼+特別號碼\n" +
                                                    "五奬:4個號碼\n" +
                                                    "六奬:3個號碼+特別號碼\n" +
                                                    "七奬:3個號碼\n"*/
                    ).show()
                    true
                }
                else->false
            }
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        binding.idPrizeList.adapter = adps[tab!!.position]
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
//        TODO("Not yet implemented")
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
//        TODO("Not yet implemented")
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_drawn_number_checking, menu)
//        return true
//    }

/*    private fun genAllEntries() {
        val m = 6-bankers.size
        combinations.clear()

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
    companion object{
        val combinations = mutableListOf<List<Int>>()
    }
    }*/

/*    override fun handleOnBackPressed() {
        TODO("Not yet implemented")
    }*/
}