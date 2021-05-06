package com.project.connectCoder.util

import com.project.connectCoder.R

object ImagePicker {
    private val images = arrayOf(
        R.drawable.profile_bg_1,
        R.drawable.profile_bg_2,
        R.drawable.profile_bg_3,
        R.drawable.profile_bg_4,
        R.drawable.profile_bg_5,
        R.drawable.profile_bg_6,
        R.drawable.profile_bg_7,
        R.drawable.profile_bg_8,
        R.drawable.profile_bg_9,
        R.drawable.profile_bg_10,
        R.drawable.profile_bg_11,
        R.drawable.profile_bg_12,
        R.drawable.profile_bg_13,
        R.drawable.profile_bg_14,
        R.drawable.profile_bg_15,
        R.drawable.profile_bg_16,
        R.drawable.profile_bg_17
    )

    var currentImage = 0

    fun getImage(): Int {
        currentImage = (currentImage + 1) % images.size
        return images[currentImage]
    }
}