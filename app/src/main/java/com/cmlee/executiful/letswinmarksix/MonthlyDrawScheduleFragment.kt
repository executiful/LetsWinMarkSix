package com.cmlee.executiful.letswinmarksix

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
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
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.ensp
import com.cmlee.executiful.letswinmarksix.databinding.FragmentMonthlyDrawScheduleBinding
import com.cmlee.executiful.letswinmarksix.helper.ConnectionObject
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_DATES = "schedule_dates"

/**
 * A simple [Fragment] subclass.
 * Use the [MonthlyDrawScheduleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MonthlyDrawScheduleFragment : AppCompatDialogFragment() {
    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null
//    private var dates: MutableList<Calendar> = mutableListOf()

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
//        dialog?.window?.attributes = layoutParams1
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
//        today.add(Calendar.DATE, +30)
        val divider = {
            View(requireContext()).also {
                val lp = GridLayout.LayoutParams(spec, spec7)
                lp.width = MATCH_PARENT
                lp.height = 2
                it.setBackgroundColor(Color.RED)
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
        val ddates = allddates.filter { it.first >= today }
        divider()
        if (ddates.isEmpty()) {
            commontv(getString(R.string.nothing_to_show), spec, spec7, MATCH_PARENT)
            divider()
        }
        ddates.groupBy { it.first.get(Calendar.MONTH) }.forEach { i, pairs ->
            commontv("${i+1}${ensp}月", spec, spec7, MATCH_PARENT)
            divider()
            "日,一,二,三,四,五,六".split(",").map{ "$it$ensp"}.forEach { s ->
                val week = TextView(requireContext())
                week.layoutParams = GridLayout.LayoutParams(spec, spec)
                week.layoutParams.width = 0
                week.layoutParams.height = WRAP_CONTENT
                week.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                week.text = s
                binding.idDates.addView(week)
            }

            val step = pairs.first().first.clone() as Calendar

            step.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            while (step <= pairs.last().first) {
                val layoutParams = GridLayout.LayoutParams(spec, spec)
                layoutParams.width = 0
                layoutParams.height = WRAP_CONTENT
                val tv = TextView(requireContext())// rt.findViewById<TextView>(android.R.id.text1)
                tv.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                "${step.get(Calendar.DATE)}$ensp".also { tv.text = it }
                tv.isEnabled = false
                pairs.find { it.first == step }?.let {
                    tv.isEnabled = true
                    if ("1" == it.second.jackpot)
                        tv.background = AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.icon_snowball
                        )
                    else {
                        tv.isSelected = true
                    }
                }
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MonthlyDrawScheduleFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            MonthlyDrawScheduleFragment()
    }
}