package com.calendar.mutliple.range.date.picker

import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.ListAdapter
import com.calendar.mutliple.range.date.R

internal class CalendarAdapter :
    ListAdapter<CalendarEntity, CalendarViewHolder>(CalendarDiffCallback()) {

    var onActionListener: (CalendarEntity, Int) -> Unit = { _, _ -> }

    override fun submitList(list: MutableList<CalendarEntity>?) {
        super.submitList(list?.toMutableList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        return when (viewType) {
            CalendarType.MONTH.ordinal -> MonthViewHolder(
                parent.inflate(R.layout.calendar_month_view)
            )
            // CalendarType.WEEK.ordinal -> WeekViewHolder(parent.inflate(R.layout.calendar_week_view))
            CalendarType.DAY.ordinal -> DayViewHolder(parent.inflate(R.layout.calendar_day_view))
            else -> EmptyViewHolder(parent.inflate(R.layout.calendar_empty_view))
        }
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.onBind(getItem(position), onActionListener)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).calendarType
    }
}

fun ViewGroup.inflate(@LayoutRes layoutId: Int, attachedToRoot: Boolean = false): View =
    from(context).inflate(layoutId, this, attachedToRoot)