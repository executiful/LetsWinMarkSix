package com.cmlee.executiful.letswinmarksix

import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.ensp
import com.cmlee.executiful.letswinmarksix.databinding.FragmentMonthlyDrawScheduleBinding
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.KEY_FIXTURES_UPDATE
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.TAG_FIXTURES
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.clearTimePart
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.getDateTimeISOFormat
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.getLatestDDate
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject.monthFmt
import com.cmlee.executiful.letswinmarksix.model.drawYears.DrawDate.Companion.checked_value
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match

/**
 * A simple [Fragment] subclass.
 * Use the [MonthlyDrawScheduleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MonthlyDrawScheduleFragment : AppCompatDialogFragment() {
    // TODO: Rename and change types of parameters
    private var _binding : FragmentMonthlyDrawScheduleBinding? = null
    private val binding  get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Monthly_Dialog)
    }

    override fun onStart() {
        super.onStart()
        dialog?.setTitle(R.string.draw_schedule_title)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMonthlyDrawScheduleBinding.inflate(inflater, container, false)
        val spec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        val spec7 = GridLayout.spec(GridLayout.UNDEFINED, 7,1f)
        val schuPref = requireContext().getSharedPreferences(TAG_FIXTURES, MODE_PRIVATE)
        val (allddates, jackpots) = getLatestDDate(schuPref)
        //val (allddates, jackpots) = ConnectionObject.getLatestSchecule(requireContext())
        val today = Calendar.getInstance()

        val jp = mutableListOf<String>()
        today.clearTimePart()

//        today.add(Calendar.DATE, -4)
//        val db = M6Db.getDatabase(requireContext()).DrawResultDao()
//        val results = db.getAll()
        val divider = fun(w: Int): View {
            return View(requireContext()).also {
                val lp = GridLayout.LayoutParams(spec, spec7)
                val typedArray = requireContext().obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
                val divider = typedArray.getDrawable(0)
                typedArray.recycle()

                lp.width = 0
                lp.height = w
                it.background = divider
                binding.idDates.addView(it, lp)
            }
        }
        val commontv = block@{ txt: String, r: GridLayout.Spec, c: GridLayout.Spec, w: Int ->
            with(AppCompatTextView(requireContext())) {
                val layoutPa = GridLayout.LayoutParams(r, c)

                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                text = txt
                binding.idDates.addView(this, layoutPa)
                return@block this
            }
        }
//        if(today.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY)
            today.add(Calendar.DATE, -7)
//        else
            today.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val ddates = allddates.filter { it.first >= today && it.first <= allddates.last{ that-> that.second.draw == checked_value}.first }
        divider(2)
        if (ddates.isEmpty()) {
            commontv(getString(R.string.nothing_to_show), spec, spec, MATCH_PARENT)
            divider(2)
        }
        ddates.groupBy { monthFmt.format(it.first.time) }.forEach { (i, pairs) ->
            commontv("${i}${ensp}", spec, spec7, MATCH_PARENT)
            "日,一,二,三,四,五,六".split(",").map { "$it$ensp" }.forEach { s ->
                val week = TextView(requireContext())
                val wlp = GridLayout.LayoutParams(spec, spec)

                week.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                week.setShadowLayer(.2f,.1f,.1f, Color.LTGRAY)
                week.text = s
                binding.idDates.addView(week, wlp)
            }
            divider(5)
            val lastdayofmonth = pairs.first().first.clone() as Calendar
            lastdayofmonth.set(Calendar.DATE,pairs.first().first.getActualMaximum(Calendar.DAY_OF_MONTH))

            val step = pairs.first().first.clone() as Calendar
            step.set(Calendar.DAY_OF_MONTH, 1)
            step.add(Calendar.DATE,-1)
            step.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            while (step <= lastdayofmonth) {
                val layoutParams = GridLayout.LayoutParams(spec, spec)
                layoutParams.width = 0
                layoutParams.height = WRAP_CONTENT
                val tv = TextView(requireContext())// rt.findViewById<TextView>(android.R.id.text1)
                tv.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                val sptext = SpannableString("${step.get(Calendar.DATE)}${ensp}")
                tv.isEnabled = false
                tv.text = sptext
                binding.idDates.addView(tv, layoutParams)
                if (i == monthFmt.format(step.time)) {
                    allddates.find { it.first == step }?.let {
                        tv.isEnabled = true
                        when {
                            checked_value==it.second.draw && checked_value ==it.second.preSell -> {
                                //todo
                            }

                            checked_value==it.second.preSell -> {
                                //todo
                            }

                            checked_value==it.second.jackpot -> {

                                jackpots.find { target->target.first == step.time }?.let { (_,f)-> jp.add(f) }
                                tv.background = AppCompatResources.getDrawable(
                                    requireContext(),
                                    R.drawable.dayborder
                                )
                            }

                            checked_value==it.second.draw -> {
                                tv.isSelected = true
                            }

                            checked_value!=it.second.draw -> {
                                tv.isEnabled=false
                                sptext.setSpan(StrikethroughSpan(), 0, sptext.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                        }
                    }
                } else {
                    tv.isVisible = false
                }

                step.add(Calendar.DATE, 1)
            }
            divider(2)
        }
        if(binding.idDates.isEmpty()){
            binding.idDates.background = AppCompatResources.getDrawable(requireContext(), android.R.drawable.ic_notification_overlay)
        }
        requireContext().getSharedPreferences(TAG_FIXTURES, MODE_PRIVATE).getDateTimeISOFormat(
            KEY_FIXTURES_UPDATE)?.let {
                jp.add(getString(R.string.last_update_at, it))
        }
        jp.distinct().joinToString(System.lineSeparator())
            .also { binding.idjackpotstxt.text = it }
        return binding.root
    }

    companion object {
//        val monthformat = SimpleDateFormat("")
//        private const val checked = "1"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment MonthlyDrawScheduleFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            MonthlyDrawScheduleFragment()
    }
}