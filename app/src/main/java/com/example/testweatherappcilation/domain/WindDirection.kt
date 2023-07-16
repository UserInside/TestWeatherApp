package com.example.testweatherappcilation.domain

import com.example.testweatherappcilation.R
import kotlinx.serialization.Serializable

@Serializable
sealed class WindDirection(val textResource: Int) {
    object NorthWest: WindDirection(R.string.nw)
    object North: WindDirection(R.string.n)
    object NorthEast: WindDirection(R.string.ne)
    object East: WindDirection(R.string.e)
    object SouthWest: WindDirection(R.string.sw)
    object South: WindDirection(R.string.s)
    object SouthEast: WindDirection(R.string.se)
    object West: WindDirection(R.string.w)
    object Calm: WindDirection(R.string.c)
    object Undefined: WindDirection(R.string.undefined)
}