package com.example.testweatherappcilation.mvp.views.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.testweatherappcilation.mvp.models.entity.WeatherUiModelForecasts

class ForecastsDiffUtil(
    private val oldList: List<WeatherUiModelForecasts?>?,
    private val newList: List<WeatherUiModelForecasts?>?
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