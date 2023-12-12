package com.cmlee.executiful.letswinmarksix

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.maxSince
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.maxTimes
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.nextCount
import com.cmlee.executiful.letswinmarksix.databinding.FragmentBallDialogBinding
import com.cmlee.executiful.letswinmarksix.model.NumStat
import com.cmlee.executiful.letswinmarksix.model.NumStat.Companion.BallColor

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_INDEX = "itemindex"

/**
 * A simple [Fragment] subclass.
 * Use the [BallDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BallDialogFragment : AppCompatDialogFragment() {
    private lateinit var mUpdateSelectionListener: IUpdateSelection
    private var _binding : FragmentBallDialogBinding? = null
    private val binding  get() = _binding!!

    // TODO: Rename and change types of parameters
    private var itemIndex = 0
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IUpdateSelection) {
            mUpdateSelectionListener = context
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnClickListener")
        }
    }
    interface IUpdateSelection {
        fun toggle(index:Int, reset:Boolean = false): NumStat
        fun getItem(idx:Int): NumStat
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            itemIndex = it.getInt(ARG_INDEX, 0)
        }
        setStyle(STYLE_NO_FRAME, R.style.Theme_Ball_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_ball_dialog, container, false)
        _binding = FragmentBallDialogBinding.inflate(inflater)


        mUpdateSelectionListener.getItem(itemIndex).also {
            updatedialog(it)
            "${it.times}(${maxTimes})".also { binding.stattimes.text = it }
            "${it.since}(${maxSince})".also { binding.statsince.text = it }
            binding.ballnumber.setBackgroundColor(it.num.BallColor())
            binding.nextcount.text = if(nextCount(it.num))"yes" else "no"
            with(binding.progressBar) {
                max = maxTimes - MainActivity.minTimes
                progress = it.times - MainActivity.minTimes
            }
        }
        binding.switch1.setOnCheckedChangeListener { _, b ->
            updatedialog(
                if (b) {
                    mUpdateSelectionListener.toggle(itemIndex)
                } else {
                    mUpdateSelectionListener.toggle(itemIndex, true)
                }
            )
            if(!b)dismiss()
        }

        binding.toggleButton.setOnClickListener {
            mUpdateSelectionListener.toggle(itemIndex)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updatedialog(item:NumStat){
        binding.ballnumber.text = getString(R.string.format_ball, item.numString)
        binding.switch1.isChecked = item.status!=NumStat.NUMSTATUS.UNSEL
        Log.i("NumStat.NUMSTATUS", item.status.toString())
        if(item.status==NumStat.NUMSTATUS.UNSEL) {
            binding.toggleButton.visibility = View.GONE
            binding.texttoggle.visibility = View.GONE
//            binding.toggleButton.isChecked = false
        }
        else {
            binding.toggleButton.isChecked = item.status == NumStat.NUMSTATUS.BANKER
            binding.toggleButton.visibility = View.VISIBLE
            binding.texttoggle.visibility = View.VISIBLE
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BallDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(item: Int) =
            BallDialogFragment().apply {
                arguments = Bundle().apply {
//                    putSerializable(ARG_ITEM, item)
//                    putInt(ARG_MAX, maxstat)
//                    putInt(ARG_MIN, minstat)
                    putInt(ARG_INDEX, item)
                }
            }
        const val TAG_BALL_DIALOG = "BALL INFORMATION"
    }
}