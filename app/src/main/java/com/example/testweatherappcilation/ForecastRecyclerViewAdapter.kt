package com.example.testweatherappcilation

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ahmadrosid.svgloader.SvgLoader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


class ForecastRecyclerViewAdapter(
    val actualWeather: ActualWeather?,
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
        val item = actualWeather?.forecasts?.get(position)

        holder.textDay.text = if (position == 0) "Сегодня" else LocalDate.parse(item?.date).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru","RU")).replaceFirstChar { it.uppercase() }
        holder.textDate.text = LocalDate.parse(item?.date, DateTimeFormatter.ofPattern("yyyy-MM-dd")).format(DateTimeFormatter.ofPattern("dd MMM", Locale("ru","RU"))).toString()
        SvgLoader.pluck()
            .with(mContext as Activity)
            .load(
                "https://yastatic.net/weather/i/icons/funky/dark/${item?.parts?.dayShort?.icon}.svg",
                holder.imageCondition
            )


        holder.textDayTemp.text = item?.parts?.dayShort?.temp?.let{
            if (it > 0) "+$it°" else "$it°"
        }
        holder.textNightTemp.text = item?.parts?.nightShort?.temp?.let {
            if (it > 0) "+$it°" else "$it°"
        }
        holder.textCondition.text = item?.parts?.dayShort?.condition?.condition?.let {
            mContext.getString(mContext.resources.getIdentifier(it,"string", mContext.packageName))

        }
    }

    override fun getItemCount(): Int {
        return actualWeather?.forecasts?.size ?: 0
    }
}


