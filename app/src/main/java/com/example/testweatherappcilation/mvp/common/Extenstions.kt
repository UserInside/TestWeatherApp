package com.example.testweatherappcilation.mvp.common

import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import android.widget.Toast
import coil.ComponentRegistry
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.testweatherappcilation.R

fun ImageView.loadImageFromUrl(imageUrl: String) {
    val imageLoader = ImageLoader.Builder(this.context)
        .components(fun ComponentRegistry.Builder.() {
            add(SvgDecoder.Factory())
        })
        .build()

    val imageRequest = ImageRequest.Builder(this.context)
        .data(imageUrl)
        .target(
            onSuccess = { result ->
                val bitmap = (result as BitmapDrawable).bitmap
                this.setImageBitmap(bitmap)
            },
        )
        .build()

    imageLoader.enqueue(imageRequest)
}

fun Activity.toastLocationAccessDenied() {
    Toast.makeText(
        this,
        getString(R.string.location_access_denied),
        Toast.LENGTH_SHORT
    ).show()
}

fun Activity.toastWrongCoordinates() {
    Toast.makeText(
        this, getString(R.string.wrong_coordinates),
        Toast.LENGTH_LONG
    ).show()
}