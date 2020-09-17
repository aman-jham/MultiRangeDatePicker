package com.calendar.mutliple.range.date.picker

import androidx.recyclerview.widget.DiffUtil

internal class CalendarDiffCallback : DiffUtil.ItemCallback<CalendarEntity>() {

    override fun areItemsTheSame(oldItem: CalendarEntity, newItem: CalendarEntity): Boolean {
        return oldItem::class == newItem::class
    }

    override fun areContentsTheSame(oldItem: CalendarEntity, newItem: CalendarEntity): Boolean {
        return if (oldItem is CalendarEntity.Day && newItem is CalendarEntity.Day) {
            oldItem.selection == newItem.selection && oldItem.isRange == newItem.isRange
        } else {
            oldItem.selectionType == newItem.selectionType
        }
    }
}