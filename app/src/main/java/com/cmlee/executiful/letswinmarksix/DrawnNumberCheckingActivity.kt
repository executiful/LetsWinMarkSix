package com.cmlee.executiful.letswinmarksix

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.bankers
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.dateStart
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.legs
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.msgMatch
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.msgNumbers
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.thinsp
import com.cmlee.executiful.letswinmarksix.databinding.ActivityDrawnNumberCheckingBinding
import com.cmlee.executiful.letswinmarksix.databinding.DrawnListItemBinding
import com.cmlee.executiful.letswinmarksix.helper.BannerAppCompatActivity
import com.cmlee.executiful.letswinmarksix.helper.DayYearConverter.Companion.sqlDate
import com.cmlee.executiful.letswinmarksix.model.NumStat.Companion.BallColor
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResult
import com.cmlee.executiful.letswinmarksix.roomdb.M6Db
import com.google.android.material.tabs.TabLayout

class DrawnNumberCheckingActivity : BannerAppCompatActivity(),
    TabLayout.OnTabSelectedListener/*, OnBackPressedCallback*/ {
    private lateinit var db: M6Db
    private val ht = HandlerThread("m6thread")
    private lateinit var hr: Handler
    private lateinit var binding: ActivityDrawnNumberCheckingBinding
    private val adps = mutableListOf<DrawnAdapter>()
    private var bpcb = object : OnBackPressedCallback(
        true // default to enabled
    ) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    class DrawnVH(val item: DrawnListItemBinding) : RecyclerView.ViewHolder(item.root) {
        companion object{
            val colors = arrayOf(intArrayOf(android.R.attr.state_enabled))
        }
        private fun setnum(c: AppCompatButton, i: Int, match: Boolean) {
            val sp = SpannableString(i.toString())
//        c.isSelected = !match
//            sp.setSpan(StyleSpan(if (match)Typeface.BOLD_ITALIC else Typeface.NORMAL), 0, sp.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sp.setSpan(if (match)UnderlineSpan() else StyleSpan(Typeface.NORMAL), 0, sp.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            c.text = sp
//        c.text = sp
            c.backgroundTintList =
                ColorStateList(
                    colors,
                    intArrayOf(i.BallColor())
                )
        }

        fun bind(ints: Pair<List<Boolean>, DrawResult>, context: Context) {
            with(ints.second) {
                setnum(item.chip1st, no.nos[0], ints.first[0])
                setnum(item.chip2nd, no.nos[1], ints.first[1])
                setnum(item.chip3rd, no.nos[2], ints.first[2])
                setnum(item.chip4th, no.nos[3], ints.first[3])
                setnum(item.chip5th, no.nos[4], ints.first[4])
                setnum(item.chip6th, no.nos[5], ints.first[5])
                setnum(item.chipsp, sno, ints.first[6])
            }

            "${
                context.getString(
                    R.string.draw_number,
                    ints.second.id
                )
            } ${ints.second.sbnameC}, ${System.lineSeparator()}${
                context.getString(
                    R.string.draw_date,
                    sqlDate.format(ints.second.date)
                )
            }".apply { /* todo: show noof match entries */
                item.idDate.text = this
            }
//            item.idDate.setCompoundDrawablesRelativeWithIntrinsicBounds(if(ints.first>=6.0) R.drawable.baseline_monetization_on_24 else 0,0,0,0)
        }
    }

    class DrawnAdapter(private val result: List<Pair<List<Boolean>, DrawResult>>, val context: Context) :
        RecyclerView.Adapter<DrawnVH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawnVH {
            val binding = DrawnListItemBinding.inflate(LayoutInflater.from(parent.context))
            return DrawnVH(binding)
        }

        override fun onBindViewHolder(holder: DrawnVH, position: Int) {
            holder.bind(result[position], context)
        }

        override fun getItemCount() = result.size
    }

    override val adUnitStringId: Int
        get() = R.string.admob_drawn_check

    override fun onAdLoaded() {
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
        val waitdlg = AlertDialog.Builder(this, R.style.Theme_Wait_Dialog)
            .setView(R.layout.pause_dialog_layout).create()
        waitdlg.setCanceledOnTouchOutside(false)
        ht.start()
        hr = Handler(ht.looper)
        waitdlg.setOnShowListener {
            hr.post {
                population(waitdlg)
                /*                UpdateLatestDraw(this){
                                    if (it == "OK") {
                                        runOnUiThread {
                                            waitdlg.show()
                                            adps.clear()
                                            binding.tabPrize.removeAllTabs()
                                        }
                                        population(waitdlg)
                                    }
                                }*/
            }
        }
        waitdlg.show()
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_entry -> {
                    AlertDialog.Builder(this).setMessage(msgNumbers+"\n"+ msgMatch).show()
                    true
                }

                R.id.action_details -> {
                    val temp =
                        "頭獎 選中6個「攪出號碼」 獎金會因應該期獲中頭獎注數而有所不同，每期頭獎獎金基金\n" +
                                "訂為不少於港幣800萬元。 二獎 選中5個「攪出號碼」+「特別號碼」 獎金會因應該期獲中二獎注數而有所不同 三獎 選中5個「攪出號碼」 獎金會因應該期獲中三獎注數而有所不同 四獎 選中4個「攪出號碼」+「特別號碼」 固定獎金港幣9,600元 五獎 選中4個「攪出號碼」 固定獎金港幣640元 六獎 選中3個「攪出號碼」+「特別號碼」 固定獎金港幣320元 七獎 選中3個「攪出號碼」 固定獎金港幣40元"
                    AlertDialog.Builder(this).setMessage(
                        temp.replace(" ", "\n")
                    ).show()
                    true
                }

                else -> false
            }
        }
    }

    private fun population(waitdlg: AlertDialog) {
        db = M6Db.getDatabase(this)
        val allresult = db.DrawResultDao().getAll().filter { it.date >= dateStart }
        val max1 = 6 - bankers.size

        val gb = allresult/*.parallelStream()*/.map { rs ->
            val m6 = rs.no.nos.intersect(bankers).plus(rs.no.nos.intersect(legs).take(max1))
            rs.no.nos.map { it in m6 }.plus(rs.sno in bankers || rs.sno in legs) to rs
        }.filter { f -> f.first.take(6).count { it } >= 3 }.toList().groupBy { a ->
            when (a.first.take(6).count { it }) {
                6 -> 0
                5 -> if (a.first[6]) 1 else 2
                4 -> if (a.first[6]) 3 else 4
                3 -> if (a.first[6]) 5 else 6
                else -> -1
            }
        }

        runOnUiThread {
            waitdlg.hide()
            resources.getStringArray(R.array.prize).forEachIndexed { index, s ->
                gb[index]?.let {
                    val ssb = SpannableStringBuilder("${thinsp}(${it.size})")
                    ssb.setSpan(
                        TextAppearanceSpan(this, R.style.countnumber),
                        0,
                        ssb.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
//                    ssb.setSpan(SuperscriptSpan(), 0, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//                    ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.count)), 0, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    ssb.insert(0, s)
                    adps.add(DrawnAdapter(it, this))
                    val t = binding.tabPrize.newTab().setText(ssb)
                    t.id = index
                    binding.tabPrize.addTab(t)
                }
            }
            binding.tabPrize.selectTab(binding.tabPrize.getTabAt(0))
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