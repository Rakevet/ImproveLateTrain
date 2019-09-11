package com.improve.latetrain.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.improve.latetrain.FirebaseInfo
import com.improve.latetrain.R
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import kotlinx.android.synthetic.main.fragment_history.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private val instance = FirebaseDatabase.getInstance()
    private var minutesPerDaysPath = instance.getReference(FirebaseInfo.TOTAL_DAYS)
    private lateinit var postListener: ChildEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val currentDay = SimpleDateFormat("dd", Locale.getDefault()).format(Date()).toInt()
        val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date()).toInt()
        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date()).toInt()
        setGraph(currentDay, currentMonth, currentYear)

        date_btn_fh.setOnClickListener {
            context?.let {
                datePickerDialog(it).show()
            }
        }
    }

    private fun setGraph(currentDay: Int, currentMonth: Int, currentYear: Int) {
        minutesPerDaysPath = instance.getReference(FirebaseInfo.TOTAL_DAYS)
            .child(currentYear.toString()).child(currentMonth.toString())
        val series = BarGraphSeries(arrayOf())
        series.color = Color.parseColor("#3880EC")
        val selectedDaySeries = BarGraphSeries(arrayOf())
        selectedDaySeries.color = Color.parseColor("#e6a430")
        graph_view_fh.viewport.isXAxisBoundsManual = true
        var max = 0
        var min = 0
        postListener = object : ChildEventListener {
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val minutes = dataSnapshot.child("minutes").getValue(Int::class.java)
                val day = dataSnapshot.child("date").getValue(Int::class.java)
                day?.let {
                    if (it > max)
                        max = it
                    if (min > it)
                        min = it
                    if (day==currentDay) {
                        selectedDaySeries.appendData(DataPoint(it.toDouble(), minutes?.toDouble() ?: return), true, max, true)
                        graph_view_fh?.viewport?.setMaxX(max.toDouble())
                        graph_view_fh?.viewport?.setMinX(min.toDouble())
                        graph_view_fh?.addSeries(selectedDaySeries)
                    }
                    else {
                        series.appendData(DataPoint(it.toDouble(), minutes?.toDouble() ?: return), true, max, true)
                        graph_view_fh?.viewport?.setMaxX(max.toDouble())
                        graph_view_fh?.viewport?.setMinX(min.toDouble())
                        graph_view_fh?.addSeries(series)
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        minutesPerDaysPath.addChildEventListener(postListener)
    }

    private fun datePickerDialog(context: Context): DatePickerDialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(context,
            DatePickerDialog.OnDateSetListener { _, yearS, monthOfYear, dayOfMonth ->
                date_btn_fh.text = getString(R.string.date_path, dayOfMonth,(monthOfYear + 1),yearS)
                minutesPerDaysPath.removeEventListener(postListener)
                graph_view_fh.removeAllSeries()
                setGraph(dayOfMonth, monthOfYear + 1, yearS)
            }, year, month, day
        )
    }

    companion object {
        @JvmStatic
        fun newInstance(): HistoryFragment =
            HistoryFragment()
    }
}