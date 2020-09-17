package com.calendar.mutliple.range.date.picker

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.calendar.mutliple.range.date.R
import kotlinx.android.synthetic.main.calendar_day_view.view.*
import kotlinx.android.synthetic.main.calendar_month_view.view.*
import kotlinx.android.synthetic.main.calendar_week_view.view.*
import java.text.DateFormatSymbols

internal abstract class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun onBind(item: CalendarEntity, actionListener: (CalendarEntity, Int) -> Unit)
}

internal class MonthViewHolder(private val view: View) : CalendarViewHolder(view) {
    private val name by lazy { view.vMonthName }

    override fun onBind(item: CalendarEntity, actionListener: (CalendarEntity, Int) -> Unit) {
        if (item is CalendarEntity.Month) {
            name.text = item.label
        }
    }
}

internal class WeekViewHolder(private val view: View) : CalendarViewHolder(view) {

    private val parentContainer by lazy { view.parent_container }

    override fun onBind(item: CalendarEntity, actionListener: (CalendarEntity, Int) -> Unit) {
        val dateFormat = DateFormatSymbols().shortWeekdays
        (1 until dateFormat.size).forEach {
            (parentContainer.getChildAt(it - 1) as TextView).text = dateFormat[it]
        }
    }
}

internal class DayViewHolder(view: View) : CalendarViewHolder(view) {
    private val name by lazy { view.vDayName }
    private val selectionImage by lazy { view.vImageView }
    private val selectedImage by lazy { view.vImage }
 //  private val halfLeftBg by lazy { view.vHalfLeftBg }
 //  private val halfRightBg by lazy { view.vHalfRightBg }

    override fun onBind(item: CalendarEntity, actionListener: (CalendarEntity, Int) -> Unit) {
        if (item is CalendarEntity.Day) {
            name.text = item.label
            when (item.selection) {
                SelectionType.START -> {
                    selectionImage.setImageResource(R.drawable.edit_selection)
                    name.setTextColor(ContextCompat.getColor(itemView.context, R.color.calendar_day_selected_font))
//                    halfLeftBg.dehighlight()
//                    if (item.isRange) halfRightBg.highlight()
//                    else halfRightBg.dehighlight()
                    selectedImage.visibility = View.GONE
                }
                SelectionType.END -> {
                   // name.select()
                    name.setTextColor(ContextCompat.getColor(itemView.context, R.color.calendar_day_selected_font))
                    selectionImage.setImageResource(R.drawable.edit_selection)
                    selectedImage.visibility = View.VISIBLE
//                    halfLeftBg.highlight()
//                    halfRightBg.dehighlight()
                }
                SelectionType.BETWEEN -> {
//                    name.deselect()
                    name.setTextColor(ContextCompat.getColor(itemView.context, R.color.calendar_day_selected_font))
                    selectionImage.setImageResource(R.drawable.edit_selection)
//                    halfRightBg.highlight()
//                    halfLeftBg.highlight()
                    selectedImage.visibility = View.GONE
                }
                SelectionType.NONE -> {
                    selectionImage.setImageResource(R.drawable.edit_circle)
//                    halfLeftBg.dehighlight()
//                    halfRightBg.dehighlight()
                    name.setTextColor(ContextCompat.getColor(itemView.context, R.color.calendar_date_default))
                    selectedImage.visibility = View.GONE
                }
            }

            if (item.state != DateState.DISABLED) {
                itemView.setOnClickListener {
                    actionListener.invoke(
                        item,
                        adapterPosition
                    )
                }
            } else {
                itemView.setOnClickListener(null)
            }
        }
    }

    private fun getFontColor(item: CalendarEntity.Day): Int {
        return if (item.selection == SelectionType.START || item.selection == SelectionType.END) {
            ContextCompat.getColor(itemView.context, R.color.calendar_day_selected_font)
        } else {
            val color = when (item.state) {
                DateState.DISABLED -> R.color.calendar_day_disabled_font
                DateState.WEEKEND -> R.color.calendar_day_weekend_font
                else -> R.color.calendar_day_normal_font
            }
            ContextCompat.getColor(itemView.context, color)
        }
    }

    private fun View.select() {
        val drawable = ContextCompat.getDrawable(context, R.drawable.selected_day_bg)
        background = drawable
    }

    private fun View.deselect() {
        background = null
    }

    private fun View.dehighlight() {
        val color = ContextCompat.getColor(context, R.color.calendar_day_unselected_bg)
        setBackgroundColor(color)
    }

    private fun View.highlight() {
        val color = ContextCompat.getColor(context, R.color.calendar_day_range_selected_bg)
        setBackgroundColor(color)
    }
}

internal class EmptyViewHolder(view: View) : CalendarViewHolder(view) {
    override fun onBind(item: CalendarEntity, actionListener: (CalendarEntity, Int) -> Unit) {
    }
}