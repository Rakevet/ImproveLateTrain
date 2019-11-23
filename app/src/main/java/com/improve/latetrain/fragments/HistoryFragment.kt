package com.improve.latetrain.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.improve.latetrain.data.firebase.FirebaseConnection
import com.improve.latetrain.R
import com.improve.latetrain.data.firebase.AnalyticsInfo
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import kotlinx.android.synthetic.main.fragment_history.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var minutesPerDayObserver: Observer<Pair<Int, String>>
    private val firebaseFunctions = FirebaseConnection()
    private var series = BarGraphSeries(arrayOf())
    private var selectedDaySeries = BarGraphSeries(arrayOf())
    private var tempDay = "0"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        date_btn_fh.setOnClickListener {
            context?.let {
                datePickerDialog(it).show()
            }
            context?.let{
                AnalyticsInfo.sendAnalytics("chooseDateBtn", arrayListOf(Pair("", "")), it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentDay = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
        setGraph(currentDay)
    }

    override fun onStop() {
        super.onStop()
        firebaseFunctions.removeDailyMinutesListener()
        firebaseFunctions.minutesPerDay.removeObserver(minutesPerDayObserver)
    }

    private fun setGraph(currentDay: String) {
        series.color = Color.parseColor("#3880EC")
        selectedDaySeries.color = Color.parseColor("#e6a430")
        graph_view_fh.viewport.isXAxisBoundsManual = true
        graph_view_fh?.viewport?.setMaxX(31.0)
        graph_view_fh?.viewport?.setMinX(0.0)
        minutesPerDayObserver = Observer { pair ->
            val minutes = pair.first
            val day = pair.second
            if(tempDay.toInt()==day.toInt()){
                graph_view_fh.removeAllSeries()
                series = BarGraphSeries(arrayOf())
                selectedDaySeries = BarGraphSeries(arrayOf())
                series.color = Color.parseColor("#3880EC")
                selectedDaySeries.color = Color.parseColor("#e6a430")
            }
            else{
                if (day==currentDay) {
                    selectedDaySeries.appendData(DataPoint(day.toDouble(), minutes.toDouble()), true, 31, true)
                    graph_view_fh?.addSeries(selectedDaySeries)
                }
                else {
                    series.appendData(DataPoint(day.toDouble(), minutes.toDouble()), true, 31, true)
                    graph_view_fh?.addSeries(series)
                }
            }
            tempDay = day
        }
        firebaseFunctions.minutesPerDay.observe(this, minutesPerDayObserver)
        firebaseFunctions.getDailyMinutes()
    }

    private fun datePickerDialog(context: Context): DatePickerDialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(context,
            DatePickerDialog.OnDateSetListener { _, yearS, monthOfYear, dayOfMonth ->
                date_btn_fh.text = getString(R.string.date_path, dayOfMonth,(monthOfYear + 1),yearS)
                graph_view_fh.removeAllSeries()
                firebaseFunctions.removeDailyMinutesListener()
                firebaseFunctions.minutesPerDay.removeObserver(minutesPerDayObserver)
                setGraph(dayOfMonth.toString())
            }, year, month, day
        )
    }

    companion object {
        @JvmStatic
        fun newInstance(): HistoryFragment =
            HistoryFragment()
    }
}