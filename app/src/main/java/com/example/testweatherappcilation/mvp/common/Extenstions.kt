package com.example.testweatherappcilation.mvp.common.extenstions

import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import coil.ComponentRegistry
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest

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
