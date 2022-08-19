package com.android.test

import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import me.zhanghai.android.appiconloader.glide.AppIconModelLoader


@GlideModule
class MyGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val iconSize = 144
        registry.prepend(PackageInfo::class.java, Bitmap::class.java, AppIconModelLoader.Factory(iconSize, false, context))
        super.registerComponents(context, glide, registry)
    }
}