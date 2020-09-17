package com.calendar.mutliple.range.date.picker

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calendar.mutliple.range.date.R
import java.util.*
import java.util.Calendar.*
import kotlin.collections.ArrayList


class CalendarPicker : RecyclerView {

    private val timeZone = TimeZone.getDefault()
    private val locale = Locale.getDefault()

    private val calendarAdapter = CalendarAdapter()
    private val startCalendar = getInstance(timeZone, locale)
    private val endCalendar = getInstance(timeZone, locale)

    private var mCalendarData: MutableList<CalendarEntity> = mutableListOf()
    private var mStartDateSelection: SelectedDate? = null
    private var mEndDateSelection: SelectedDate? = null
    private var mPickerSelectionType = SelectionMode.MULTI_RANGE
    private var mShowDayOfWeekTitle = true

    private var mMultiRangeDateArray = ArrayList<Range>()
    private var mOnStartSelectedListener: (startDate: Date, label: String) -> Unit = { _, _ -> }
    private var mOnRangeSelectedListener: (startDate: Date, endDate: Date, startLabel: String, endLabel: String) -> Unit =
        { _, _, _, _ -> }

    private var mOnMultiRangeSelectedListener: (multiArray: ArrayList<Range>) -> Unit =
        { _ -> }

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        extractAttributes(attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        extractAttributes(attributeSet)
    }

    init {
        startCalendar.set(HOUR_OF_DAY, 0)
        startCalendar.set(MINUTE, 0)
        startCalendar.set(SECOND, 0)
        startCalendar.set(MILLISECOND, 0)

        endCalendar.time = startCalendar.time
        endCalendar.add(YEAR, 1)
        mMultiRangeDateArray.clear()
        setBackgroundColor(ContextCompat.getColor(context, R.color.calendar_picker_bg))
        initAdapter()
        initListener()
    }

    // region setter

    fun setRangeDate(startDate: Date, endDate: Date) {
        require(startDate.time <= endDate.time) { "startDate can't be higher than endDate" }

        startCalendar.withTime(startDate)
        endCalendar.withTime(endDate)

        refreshData()
    }


    fun setMultiSelectionDate(arrayList: ArrayList<Range>) {
        for (range in arrayList) {
            itemAnimator = null
            selectDate(range.startDate)
            selectDate(range.endDate)
        }
    }


    fun scrollToDate(date: Date) {
        val index =
            mCalendarData.indexOfFirst { it is CalendarEntity.Day && it.date.isTheSameDay(date) }
        require(index > -1) { "Date to scroll must be included in your Calendar Range Date" }
        // smoothScrollToPosition(index)
        scrollToPosition(index)
    }

    fun showDayOfWeekTitle(show: Boolean) {
        mShowDayOfWeekTitle = show
    }

    fun setSelectionDate(startDate: Date, endDate: Date? = null) {
        itemAnimator = null
        selectDate(startDate)
        if (endDate != null) selectDate(endDate)
    }

    fun setMode(mode: SelectionMode) {
        mPickerSelectionType = mode
    }

    fun setOnStartSelectedListener(callback: (startDate: Date, label: String) -> Unit) {
        mOnStartSelectedListener = callback
    }

    fun setOnRangeSelectedListener(callback: (startDate: Date, endDate: Date, startLabel: String, endLabel: String) -> Unit) {
        mOnRangeSelectedListener = callback
    }


    fun setOnMultiRangeSelectedListener(callback: (startDate: ArrayList<Range>) -> Unit) {
        mOnMultiRangeSelectedListener = callback
    }


    fun getSelectedDate(): Pair<Date?, Date?> {
        return Pair(mStartDateSelection?.day?.date, mEndDateSelection?.day?.date)
    }

    private fun initListener() {
        calendarAdapter.onActionListener = { item, position ->
            if (itemAnimator == null) itemAnimator = DefaultItemAnimator()
            if (item is CalendarEntity.Day) onDaySelected(item, position)
        }
    }

    private fun initAdapter() {
        layoutManager = GridLayoutManager(context, TOTAL_COLUMN_COUNT).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return mCalendarData[position].columnCount
                }
            }
        }
        adapter = calendarAdapter
        refreshData()
    }

    private fun selectDate(date: Date) {
        val index =
            mCalendarData.indexOfFirst { it is CalendarEntity.Day && it.date.isTheSameDay(date) }
        require(index > -1) {
            "Selection date must be included in your Calendar Range Date"
        }

        onDaySelected(mCalendarData[index] as CalendarEntity.Day, index)
    }

    private fun refreshData() {
        mCalendarData = buildCalendarData()
        calendarAdapter.submitList(mCalendarData)
    }

    private fun extractAttributes(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CalendarPicker)
        mPickerSelectionType =
            SelectionMode.values()[typedArray.getInt(R.styleable.CalendarPicker_pickerType, 0)]
        mShowDayOfWeekTitle =
            typedArray.getBoolean(R.styleable.CalendarPicker_showDayOfWeekTitle, true)
        typedArray.recycle()
    }

    private fun buildCalendarData(): MutableList<CalendarEntity> {
        val calendarData = mutableListOf<CalendarEntity>()
        val cal = getInstance(timeZone, locale)
        cal.withTime(startCalendar.time)

        val monthDifference = endCalendar.totalMonthDifference(startCalendar)

        cal.set(DAY_OF_MONTH, 1)
        (0..monthDifference).forEach { _ ->
            val totalDayInAMonth = cal.getActualMaximum(DAY_OF_MONTH)
            (1..totalDayInAMonth).forEach { _ ->
                val day = cal.get(DAY_OF_MONTH)
                val dayOfWeek = cal.get(DAY_OF_WEEK)
                val dateState = if (cal.isBefore(startCalendar) || cal.isAfter(endCalendar)) {
                    DateState.DISABLED
                } else {
                    DateState.WEEKDAY
                }
                when (day) {
                    1 -> {
                        calendarData.add(CalendarEntity.Month(cal.toPrettyMonthString()))
                        if (mShowDayOfWeekTitle) calendarData.add(CalendarEntity.Week)
                        calendarData.addAll(createStartEmptyView(dayOfWeek))
                        calendarData.add(
                            CalendarEntity.Day(
                                day.toString(),
                                cal.toPrettyDateString(),
                                cal.time,
                                state = dateState
                            )
                        )
                    }
                    totalDayInAMonth -> {
                        calendarData.add(
                            CalendarEntity.Day(
                                day.toString(),
                                cal.toPrettyDateString(),
                                cal.time,
                                state = dateState
                            )
                        )
                        calendarData.addAll(createEndEmptyView(dayOfWeek))
                    }
                    else -> {
                        calendarData.add(
                            CalendarEntity.Day(
                                day.toString(),
                                cal.toPrettyDateString(),
                                cal.time,
                                state = dateState
                            )
                        )
                    }
                }
                cal.add(DATE, 1)
            }
        }

        return calendarData
    }

    private fun createStartEmptyView(dayOfWeek: Int): List<CalendarEntity.Empty> {
        val numberOfEmptyView = when (dayOfWeek) {
            MONDAY -> 1
            TUESDAY -> 2
            WEDNESDAY -> 3
            THURSDAY -> 4
            FRIDAY -> 5
            SATURDAY -> 6
            else -> 0
        }

        val listEmpty = mutableListOf<CalendarEntity.Empty>()
        repeat((0 until numberOfEmptyView).count()) { listEmpty.add(CalendarEntity.Empty) }
        return listEmpty
    }

    private fun createEndEmptyView(dayOfWeek: Int): List<CalendarEntity.Empty> {
        val numberOfEmptyView = when (dayOfWeek) {
            SUNDAY -> 6
            MONDAY -> 5
            TUESDAY -> 4
            WEDNESDAY -> 3
            THURSDAY -> 2
            FRIDAY -> 1
            else -> 6
        }

        val listEmpty = mutableListOf<CalendarEntity.Empty>()
        repeat((0 until numberOfEmptyView).count()) { listEmpty.add(CalendarEntity.Empty) }
        return listEmpty
    }

    private fun onDaySelected(item: CalendarEntity.Day, position: Int) {

        //

        when (mPickerSelectionType) {


            SelectionMode.SINGLE -> {

                if (item == mStartDateSelection?.day) return

                if (mStartDateSelection != null) {
                    mCalendarData[mStartDateSelection!!.position] =
                        mStartDateSelection!!.day.copy(selection = SelectionType.NONE)
                }
                assignAsStartDate(item, position)
            }
            SelectionMode.RANGE -> {

                if (item == mStartDateSelection?.day) return

                when {
                    mStartDateSelection == null -> assignAsStartDate(item, position)

                    mEndDateSelection == null -> {

                        if (mStartDateSelection!!.position > position) {
                            mCalendarData[mStartDateSelection!!.position] =
                                mStartDateSelection!!.day.copy(selection = SelectionType.NONE)
                            assignAsStartDate(item, position)
                        } else {
                            assignAsStartDate(
                                mStartDateSelection!!.day,
                                mStartDateSelection!!.position,
                                true
                            )
                            assignAsEndDate(item, position)
                            highlightDateBetween(mStartDateSelection!!.position, position)
                        }
                    }

                    else -> {
                        resetSelection()
                        assignAsStartDate(item, position)
                    }
                }
            }
            SelectionMode.MULTI_RANGE -> {

                var isExist = true
                var index = 0
                if (item.selection != SelectionType.NONE) {

                    for (arrayIndex in 0 until mMultiRangeDateArray.size) {
                        val range = mMultiRangeDateArray[arrayIndex]
                        if (range.startDate.isTheSameDay(item.date) && item.selection == SelectionType.START ||
                            range.endDate.isTheSameDay(item.date) && item.selection == SelectionType.END
                        ) {
                            index = arrayIndex
                            resetSelection(range.startDate, range.endDate)
                            isExist = false
                            break
                        } else if (item.selection == SelectionType.BETWEEN) {
                            val array = getDatesBetween(range.startDate, range.endDate)
                            for (dates in array) {
                                if (dates.isTheSameDay(item.date)) {
                                    index = arrayIndex
                                    resetSelection(range.startDate, range.endDate)
                                    isExist = false
                                    break
                                }
                            }
                        }
                    }
                }

                if (isExist) {

                    when {
                        mStartDateSelection == null -> {

                            var isAllow = true
                            for (range in mMultiRangeDateArray) {
                                when {
                                    range.startDate.compareDate(item.date) < 0 -> {
                                        if (getDatesBetween(range.startDate, item.date).size < 21) {
                                            isAllow = false
                                        }
                                    }
                                    range.startDate.compareDate(item.date) > 0 -> {
                                        if (getDatesBetween(item.date, range.startDate).size < 21) {
                                            isAllow = false
                                        }
                                    }
                                    else -> {
                                        isAllow = false
                                    }
                                }

                            }
                            if (isAllow) {
                                if (getInstance().time.compareDate(item.date) >= 0) {
                                    assignAsStartDate(item, position)
                                } else {
                                    mCalendarData[position] =
                                        item.copy(selection = SelectionType.NONE)
                                }
                            } else {
                                mCalendarData[position] =
                                    item.copy(selection = SelectionType.NONE)
                            }
                        }

                        mEndDateSelection == null -> {

                            when {
                                mStartDateSelection!!.position > position -> {
                                    mCalendarData[mStartDateSelection!!.position] =
                                        mStartDateSelection!!.day.copy(selection = SelectionType.NONE)
                                    assignAsStartDate(item, position)
                                }

                                getDatesBetween(
                                    mStartDateSelection!!.day.date,
                                    item.date
                                ).size > 11 -> {
                                    mCalendarData[mStartDateSelection!!.position] =
                                        mStartDateSelection!!.day.copy(selection = SelectionType.START)
                                    // assignAsStartDate( mStartDateSelection!!.day, position)
                                }

                                else -> {


                                    assignAsStartDate(
                                        mStartDateSelection!!.day,
                                        mStartDateSelection!!.position,
                                        true
                                    )
                                    assignAsEndDate(item, position)
                                    highlightDateBetween(mStartDateSelection!!.position, position)
                                    val range = Range(
                                        mStartDateSelection!!.day.date,
                                        mEndDateSelection!!.day.date
                                    )
                                    mMultiRangeDateArray.add(range)
                                    mStartDateSelection = null
                                    mEndDateSelection = null

                                    mOnMultiRangeSelectedListener.invoke(mMultiRangeDateArray)


                                }
                            }
                        }

                        else -> {
                            resetSelection()
                            assignAsStartDate(item, position)
                        }
                    }
                } else {
                    mMultiRangeDateArray.removeAt(index)
                }
            }
        }

        calendarAdapter.submitList(mCalendarData)
    }

    private fun resetSelection(startDate: Date, endDate: Date) {
        val startIndex = mCalendarData.indexOfFirst {
            it is CalendarEntity.Day && it.date.isTheSameDay(startDate)
        }

        val endIndex = mCalendarData.indexOfFirst {
            it is CalendarEntity.Day && it.date.isTheSameDay(endDate)
        }

        ((startIndex) until endIndex + 1).forEach {
            val entity = mCalendarData[it]
            if (entity is CalendarEntity.Day) {
                mCalendarData[it] = entity.copy(selection = SelectionType.NONE)
            }
        }
    }

    private fun resetSelection() {
        val startDatePosition = mStartDateSelection?.position
        val endDatePosition = mEndDateSelection?.position

        if (startDatePosition != null && endDatePosition != null) {
            (startDatePosition..endDatePosition).forEach {
                val entity = mCalendarData[it]
                if (entity is CalendarEntity.Day)
                    mCalendarData[it] = entity.copy(selection = SelectionType.NONE)
            }
        }
        mEndDateSelection = null
    }


    private fun highlightDateBetween(
        startIndex: Int,
        endIndex: Int
    ) {
        ((startIndex + 1) until endIndex).forEach {
            val entity = mCalendarData[it]
            if (entity is CalendarEntity.Day) {
                mCalendarData[it] = entity.copy(selection = SelectionType.BETWEEN)
            }
        }
    }

    private fun assignAsStartDate(
        item: CalendarEntity.Day,
        position: Int,
        isRange: Boolean = false
    ) {
        val newItem = item.copy(selection = SelectionType.START, isRange = isRange)
        mCalendarData[position] = newItem
        mStartDateSelection = SelectedDate(newItem, position)
        if (!isRange) mOnStartSelectedListener.invoke(item.date, item.prettyLabel)
    }

    private fun assignAsEndDate(
        item: CalendarEntity.Day,
        position: Int
    ) {
        val newItem = item.copy(selection = SelectionType.END)
        mCalendarData[position] = newItem
        mEndDateSelection = SelectedDate(newItem, position)
        mOnRangeSelectedListener.invoke(
            mStartDateSelection!!.day.date,
            item.date,
            mStartDateSelection!!.day.prettyLabel,
            item.prettyLabel
        )
    }


    private fun getDatesBetween(
        startDate: Date, endDate: Date
    ): ArrayList<Date> {
        val datesInRange = ArrayList<Date>()
        val calendar: Calendar = GregorianCalendar()
        calendar.time = startDate
        val endCalendar: Calendar = GregorianCalendar()
        endCalendar.time = endDate

        while (calendar.before(endCalendar)) {
            val result = calendar.time
            datesInRange.add(result)
            calendar.add(DATE, 1)
        }
        return datesInRange
    }

    internal data class SelectedDate(val day: CalendarEntity.Day, val position: Int)

    enum class SelectionMode { SINGLE, RANGE, MULTI_RANGE }
}
