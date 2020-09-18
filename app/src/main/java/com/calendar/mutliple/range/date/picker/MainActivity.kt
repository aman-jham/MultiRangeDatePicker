package com.calendar.mutliple.range.date.picker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val firstCalendarDate = Calendar.getInstance()
        firstCalendarDate.set(Calendar.DAY_OF_MONTH, 1)
        firstCalendarDate.set(Calendar.MONTH, 0)
        firstCalendarDate.add(Calendar.YEAR, -3)

        val secondCalendarDate = Calendar.getInstance()
        secondCalendarDate.set(Calendar.DAY_OF_MONTH, 0)
        secondCalendarDate.add(Calendar.MONTH, 1)
        //secondCalendarDate.time = firstCalendarDate.time
        //secondCalendarDate.add(Calendar.YEAR, 1)

//        val thirdCalendarDate = Calendar.getInstance()
//        thirdCalendarDate.time = firstCalendarDate.time
//        thirdCalendarDate.add(Calendar.MONTH, 2)

        calendar_view.setOnRangeSelectedListener { startDate, endDate, startLabel, endLabel ->
            // departure_date.text = startLabel
            // return_date.text = endLabel
        }

        calendar_view.setOnStartSelectedListener { startDate, label ->
            // departure_date.text = label
            // return_date.text = "-"
        }

        calendar_view.setOnMultiRangeSelectedListener {

        }


        calendar_view.apply {
            setMode(CalendarPicker.SelectionMode.MULTI_RANGE)

            setRangeDate(firstCalendarDate.time, secondCalendarDate.time)


            val firstCalendarDate = Calendar.getInstance()
            firstCalendarDate.set(2019, 9, 1)

            val secondDate = Calendar.getInstance()
            secondDate.set(2019, 9, 12)


            val arrayRange = ArrayList<Range>()
            var range = Range(firstCalendarDate.time, secondDate.time)
            arrayRange.add(range)
            firstCalendarDate.set(2020, 3, 1)
            secondDate.set(2020, 3, 9)
            range = Range(firstCalendarDate.time, secondDate.time)
            arrayRange.add(range)
            firstCalendarDate.set(2020, 6, 1)
            secondDate.set(2020, 6, 10)
            range = Range(firstCalendarDate.time, secondDate.time)
            arrayRange.add(range)
            setMultiSelectionDate(arrayRange)


        }


        calendar_view.postDelayed({
            calendar_view.apply {
                scrollToDate(Calendar.getInstance().time)
            }
        },0)

    }
}