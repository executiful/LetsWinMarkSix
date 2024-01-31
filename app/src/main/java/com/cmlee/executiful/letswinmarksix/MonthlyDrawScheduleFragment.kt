package com.cmlee.executiful.letswinmarksix

import android.annotation.SuppressLint
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
import android.view.WindowManager
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.ensp
import com.cmlee.executiful.letswinmarksix.databinding.FragmentMonthlyDrawScheduleBinding
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject
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
        dialog?.let {
            val layoutParams1 = WindowManager.LayoutParams()
            layoutParams1.copyFrom(it.window?.attributes)
            layoutParams1.width = resources.configuration.screenWidthDp
            layoutParams1.height = WRAP_CONTENT
            it.window?.setLayout((resources.displayMetrics.widthPixels *.9f).toInt(), WRAP_CONTENT)
            it.setTitle(R.string.draw_schedule_title)
        }
    }

    @SuppressLint("SimpleDateFormat", "SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMonthlyDrawScheduleBinding.inflate(inflater)
        val spec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
        val spec7 = GridLayout.spec(0, 7)
        val (allddates, jackpots) = ConnectionObject.getLatestSchecule(requireContext())
        val today = Calendar.getInstance()
//        today.add(Calendar.DATE, -2)
        val divider = {
            View(requireContext()).also {
                val lp = GridLayout.LayoutParams(spec, spec7)
                lp.width = MATCH_PARENT
                lp.height = 2
                it.setBackgroundColor(ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_primary))
                binding.idDates.addView(it, lp)
            }
        }
        val commontv = fun(txt: String, r: GridLayout.Spec, c: GridLayout.Spec, w: Int): TextView {
            with(TextView(requireContext())) {
                layoutParams = GridLayout.LayoutParams(r, c)
                layoutParams.width = w
                layoutParams.height = WRAP_CONTENT
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                text = txt
                binding.idDates.addView(this)
                return this
            }
        }
        today.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val ddates = allddates.filter { it.first >= today && it.first <= allddates.last{ that-> that.second.draw == checked_value}.first }
        divider()
        if (ddates.isEmpty()) {
            commontv(getString(R.string.nothing_to_show), spec, spec7, MATCH_PARENT)
            divider()
        }
        ddates.groupBy { it.first.get(Calendar.MONTH) }.forEach { (i, pairs) ->
            commontv("${i+1}${ensp}月", spec, spec7, MATCH_PARENT)
            "日,一,二,三,四,五,六".split(",").map { "$it$ensp" }.forEach { s ->
                val week = TextView(requireContext())
                week.layoutParams = GridLayout.LayoutParams(spec, spec)
                week.layoutParams.width = 0
                week.layoutParams.height = WRAP_CONTENT
                week.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                week.text = s
                binding.idDates.addView(week)
            }
            divider()
            val lastdayofmonth = pairs.first().first.clone() as Calendar
            lastdayofmonth.set(Calendar.DATE,pairs.first().first.getActualMaximum(Calendar.DAY_OF_MONTH))

            val step = pairs.first().first.clone() as Calendar

            step.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            while (step <= lastdayofmonth) {
                val layoutParams = GridLayout.LayoutParams(spec, spec)
                layoutParams.width = 0
                layoutParams.height = WRAP_CONTENT
                val tv = TextView(requireContext())// rt.findViewById<TextView>(android.R.id.text1)
                tv.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                val sptext = SpannableString("${step.get(Calendar.DATE)}$ensp")
                tv.isEnabled = false
                pairs.find { it.first == step }?.let {
                    tv.isEnabled = true
                    when {
                        checked_value==it.second.draw && checked_value ==it.second.preSell -> {
                            //todo
                        }
                        checked_value==it.second.preSell -> {
                            //todo
                        }
                        checked_value==it.second.jackpot -> {
                            tv.background = AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.dayborder
                            )
                        }
                        checked_value==it.second.draw -> {
                            tv.isSelected = true
                        }
                        checked_value!=it.second.draw -> {
                            sptext.setSpan(StrikethroughSpan(), 0, sptext.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
                tv.text = sptext
                binding.idDates.addView(tv, layoutParams)
                tv.isVisible = (step.get(Calendar.MONTH) == i)
                step.add(Calendar.DATE, 1)
            }
            divider()
        }
        if(binding.idDates.isEmpty()){
            binding.idDates.background = AppCompatResources.getDrawable(requireContext(), android.R.drawable.ic_notification_overlay)
        }
        jackpots.filter { d -> ddates.find { d.first == it.first.time } != null }
            .joinToString(System.lineSeparator()) { it.second }
            .also { binding.idjackpotstxt.text = it }
        return binding.root
    }

    companion object {
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