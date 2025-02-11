package com.cmlee.executiful.letswinmarksix

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cmlee.executiful.letswinmarksix.DrawnNumberCheckingActivity.DrawnVH.Companion.colors
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.emsp
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.sdf_display
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.thinsp
import com.cmlee.executiful.letswinmarksix.databinding.ActivityLatestDrawnBinding
import com.cmlee.executiful.letswinmarksix.databinding.DrawnListItemBinding
import com.cmlee.executiful.letswinmarksix.helper.BannerAppCompatActivity
import com.cmlee.executiful.letswinmarksix.model.NumStat.Companion.BallColor
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResult
import com.cmlee.executiful.letswinmarksix.roomdb.M6Db
import kotlin.random.Random


class LatestDrawnActivity : BannerAppCompatActivity() {
    override val adUnitStringId: Int
        get() = R.string.admob_drawn_check

    override fun onAdLoaded() {
    }

    private var bpcb = object : OnBackPressedCallback(
        true // default to enabled
    ) {
        override fun handleOnBackPressed() {
            finish()
        }
    }
    private lateinit var binding: ActivityLatestDrawnBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLatestDrawnBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adContainerView = binding.adViewContainer

        setSupportActionBar(findViewById(R.id.toolbar))
//        binding.toolbarLayout.title = title
//        binding.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .setAnchorView(R.id.fab).show()
//        }
        onBackPressedDispatcher.addCallback(bpcb)
        binding.toolbar.setNavigationOnClickListener { finish() }
        populateResult()
    }

    private fun populateResult() {
        val drawResultDao = M6Db.getDatabase(this).DrawResultDao()
        val allresult = drawResultDao.getAll()//.takeLast(Random.nextInt(20,4000))
        val lostcount = allresult.takeWhile { (!it.p1!!.winner) }.size
        if (lostcount > 1) {
            "${getString(R.string.title_activity_latest_drawn)}${
                getString(
                    R.string.string_no_winner_count,
                    lostcount
                )
            }".also { binding.toolbar.title = it }
        } else if (lostcount==1){
            "${getString(R.string.title_activity_latest_drawn)}${
                getString(
                    R.string.string_this_lost)
            }".also { binding.toolbar.title = it }
        } else /*if(count==0)*/ {
            val count2 = allresult.takeWhile { it.p1!!.winner }.size
            if(count2>1) {
                "${getString(R.string.title_activity_latest_drawn)}${
                    getString(
                        R.string.string_win_count,
                        count2
                    )
                }".also { binding.toolbar.title = it }
            } else if(count2==1){
                "${getString(R.string.title_activity_latest_drawn)}${
                    getString(
                        R.string.string_this_win)
                }".also { binding.toolbar.title = it }
            }
        }
        binding.idPrizeList.adapter =
            ResultAdapter(allresult.filter { it.p1 != null }.take(20), this)
    }

    private class ResultAdapter(
        val result: List<DrawResult>,
        val context: Context
    ) : RecyclerView.Adapter<ResultAdapter.DrawnVH>() {
        private val bools = mutableListOf<Int>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawnVH {
            val binding = DrawnListItemBinding.inflate(LayoutInflater.from(parent.context))
            return DrawnVH(binding, context)
        }

        override fun onBindViewHolder(holder: DrawnVH, position: Int) {
            holder.bind(result[position])
        }

        override fun getItemCount() = result.size
        inner class DrawnVH(val item: DrawnListItemBinding, var context: Context) :
            RecyclerView.ViewHolder(item.root) {
            private fun setnum(c: AppCompatButton, i: Int) {
                c.text = "$i"
                c.backgroundTintList = ColorStateList(colors, intArrayOf(i.BallColor()))
            }

            fun bind(rs: DrawResult) {
                setnum(item.chip1st, rs.no.nos[0])
                setnum(item.chip2nd, rs.no.nos[1])
                setnum(item.chip3rd, rs.no.nos[2])
                setnum(item.chip4th, rs.no.nos[3])
                setnum(item.chip5th, rs.no.nos[4])
                setnum(item.chip6th, rs.no.nos[5])
                setnum(item.chipsp, rs.sno)
                val ssp = SpannableStringBuilder("${rs.id}, ${sdf_display.format(rs.date)}")
                val ssp2 = SpannableStringBuilder()
                ssp2.append(ssp)
                var temp = 0

                if (rs.sbnameC != null) {
                    ssp2.appendLine()
                    temp = ssp2.length
                    ssp2.append(rs.sbnameC)
                    ssp2.setSpan(
                        BackgroundColorSpan(ContextCompat.getColor(context, R.color.gold)),
                        temp,
                        ssp2.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                ssp2.appendLine().append("$emsp$emsp$emsp$emsp")
                    .append(context.getString(R.string.chprize_1st)).append(emsp)
                    .append(rs.p1.toString()).append(emsp)
                    .appendLine(rs.p1u.toString()).append("$emsp$emsp$emsp$emsp")
                    .append(context.getString(R.string.chprize_2nd)).append(emsp)
                    .append(rs.p2.toString()).append(emsp)
                    .appendLine(rs.p2u.toString()).append("$emsp$emsp$emsp$emsp")
                    .append(context.getString(R.string.chprize_3rd)).append(emsp)
                    .append(rs.p3.toString()).append(emsp)
                    .append(rs.p3u.toString())

                temp = ssp.length

                ssp.append(emsp)
                arrayOf(
                    rs.p1 to context.getString(R.string.chprize_1st),
                    rs.p2 to context.getString(R.string.chprize_2nd),
                    rs.p3 to context.getString(R.string.chprize_3rd)
                ).forEach { (r, s) ->
                    r?.let { p ->
                        ssp.append(s)
                        if (!p.winner) {
                            ssp.setSpan(
                                ForegroundColorSpan(Color.TRANSPARENT),
                                ssp.length - s.length,
                                ssp.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                }
                ssp.setSpan(
                    RelativeSizeSpan(.8f),
                    temp,
                    ssp.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                if (rs.sbcode != null) {
                    ssp.append(thinsp)
                    temp = ssp.length
                    ssp.append("${rs.sbcode}")
                    ssp.setSpan(
                        BackgroundColorSpan(ContextCompat.getColor(context, R.color.gold)),
                        temp,
                        ssp.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
//                .append(if (rs.p1 != null && rs.p1.winner) "①" else "○")
//                .append(if (rs.p2 != null && rs.p2.winner) "②" else "○")
//                .append(if (rs.p3 != null && rs.p3.winner) "③" else "○")
//                .append(if (rs.p1 != null && rs.p1.winner) "➀ " else "● ")
//                .append(if (rs.p2 != null && rs.p2.winner) "➁ " else "● ")
//                .append(if (rs.p3 != null && rs.p3.winner) "➂ " else "● ")
//                ssp2.setSpan(
//                    TextAppearanceSpan(context, android.R.style.TextAppearance_Small),
//                    0,
//                    ssp2.length,
//                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//                )
                item.root.setOnClickListener { v ->
                    v?.run {
                        if (bools.contains(result.indexOf(rs))) {
                            bools.remove(result.indexOf(rs))
                        } else {
                            bools.add(result.indexOf(rs))
                        }
                        notifyItemChanged(result.indexOf(rs))
                    }
                }
                if (bools.contains(adapterPosition)) {
                    item.idDate.text = ssp2
                    item.idDate.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        0,
                        R.drawable.baseline_expand_less_24
                    )
                } else {
                    item.idDate.text = ssp
                    item.idDate.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        0,
                        R.drawable.baseline_expand_more_24
                    )
                }
            }
        }
    }
}