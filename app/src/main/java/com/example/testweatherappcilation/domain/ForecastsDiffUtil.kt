package com.example.testweatherappcilation.domain

import androidx.recyclerview.widget.DiffUtil

class ForecastsDiffUtil(
    private val oldList: List<Forecasts?>?,
    private val newList: List<Forecasts?>?
) : DiffUtil.Callback() {


    override fun getOldListSize(): Int {
        return oldList?.size ?: 0
    }

    override fun getNewListSize(): Int {
        return newList?.size ?: 0
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList?.get(oldItemPosition)?.forecastsDate == newList?.get(newItemPosition)?.forecastsDate
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList?.get(oldItemPosition)?.forecastsDate != newList?.get(newItemPosition)?.forecastsDate -> false
            oldList?.get(oldItemPosition)?.forecastsTempDay != newList?.get(newItemPosition)?.forecastsTempDay -> false
            oldList?.get(oldItemPosition)?.forecastsTempNight != newList?.get(newItemPosition)?.forecastsTempNight -> false
            oldList?.get(oldItemPosition)?.forecastsIcon != newList?.get(newItemPosition)?.forecastsIcon -> false
            oldList?.get(oldItemPosition)?.forecastsCondition != newList?.get(newItemPosition)?.forecastsCondition -> false
            else -> true
        }
    }
}