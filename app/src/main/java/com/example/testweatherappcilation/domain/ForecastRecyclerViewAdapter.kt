package com.example.testweatherappcilation.domain

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ahmadrosid.svgloader.SvgLoader
import com.example.testweatherappcilation.R
import com.example.testweatherappcilation.data.ActualWeather
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


class ForecastRecyclerViewAdapter(
    val actualWeather: WeatherEntity?,
    context: Context,
): RecyclerView.Adapter<ForecastRecyclerViewAdapter.ForecastRecyclerViewHolder>() {

    val mContext = context

    class ForecastRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textDay = itemView.findViewById<TextView>(R.id.forecasts_day)
        val textDate = itemView.findViewById<TextView>(R.id.forecasts_date)
        val imageCondition = itemView.findViewById<ImageView>(R.id.forecast_recycler_image)
        val textDayTemp = itemView.findViewById<TextView>(R.id.forecasts_temp_day)
        val textNightTemp = itemView.findViewById<TextView>(R.id.forecasts_temp_night)
        val textCondition = itemView.findViewById<TextView>(R.id.forecasts_condition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastRecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_forecasts, parent, false)
        return ForecastRecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ForecastRecyclerViewHolder, position: Int) {
        holder.textDay.text = if (position == 0) "Сегодня" else LocalDate.parse(actualWeather?.forecastsDate?.get(position)).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru","RU")).replaceFirstChar { it.uppercase() }
        holder.textDate.text = LocalDate.parse(actualWeather?.forecastsDate?.get(position), DateTimeFormatter.ofPattern("yyyy-MM-dd")).format(DateTimeFormatter.ofPattern("dd MMM", Locale("ru","RU"))).toString()

        SvgLoader.pluck()
            .with(mContext as Activity)
            .load(
                "https://yastatic.net/weather/i/icons/funky/dark/${actualWeather?.forecastsIcon?.get(position)}.svg",
                holder.imageCondition
            )

        holder.textDayTemp.text = actualWeather?.forecastsTempDay?.get(position)
            ?.let{dayTemp -> if (dayTemp > 0) "+$dayTemp°" else "$dayTemp°"}
        holder.textNightTemp.text = actualWeather?.forecastsTempNight?.get(position)
            ?.let {nightTemp -> if (nightTemp > 0) "+$nightTemp°" else "$nightTemp°"}

        holder.textCondition.text = mContext.getString(mContext.resources.getIdentifier(actualWeather?.forecastsCondition?.get(position),"string", mContext.packageName))

    }

    override fun getItemCount(): Int {
        return actualWeather?.forecastsDate?.size ?: 0
    }
}


