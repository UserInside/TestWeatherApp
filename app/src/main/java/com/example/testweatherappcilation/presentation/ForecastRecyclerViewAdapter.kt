package com.example.testweatherappcilation.presentation

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ahmadrosid.svgloader.SvgLoader
import com.example.testweatherappcilation.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


class ForecastRecyclerViewAdapter(
    val forecasts: List<WeatherUiModelForecasts?>?,
    context: Context,
) : RecyclerView.Adapter<ForecastRecyclerViewAdapter.ForecastRecyclerViewHolder>() {
    val mContext = context

    private var oldList = forecasts

    class ForecastRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textDay = itemView.findViewById<TextView>(R.id.forecasts_day)
        val textDate = itemView.findViewById<TextView>(R.id.forecasts_date)
        val imageCondition = itemView.findViewById<ImageView>(R.id.forecast_recycler_image)
        val textDayTemp = itemView.findViewById<TextView>(R.id.forecasts_temp_day)
        val textNightTemp = itemView.findViewById<TextView>(R.id.forecasts_temp_night)
        val textCondition = itemView.findViewById<TextView>(R.id.forecasts_condition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastRecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_forecasts, parent, false)
        return ForecastRecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ForecastRecyclerViewHolder, position: Int) {
        val item = forecasts?.get(position)
        holder.textDay.text = if (position == 0) mContext.getString(R.string.today) else item?.forecastsDay
        holder.textDate.text = item?.forecastsDate
        holder.imageCondition.let {
            SvgLoader.pluck()
                .with(mContext as Activity)
                .load("https://yastatic.net/weather/i/icons/funky/dark/${item?.forecastsIcon}.svg", it)
        }
        holder.textDayTemp.text = item?.forecastsTempDay
        holder.textNightTemp.text = item?.forecastsTempNight
        holder.textCondition.text = item?.forecastsCondition
    }

    override fun getItemCount(): Int {
        return forecasts?.size ?: 0
    }

    fun updateList(newList: List<WeatherUiModelForecasts>) {    //todo куда это вставить?
        val forecastDiffUtil = ForecastsDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(forecastDiffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}



