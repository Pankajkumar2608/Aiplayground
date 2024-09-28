package com.example.aiplayground

import android.graphics.Bitmap

data class ChatMessage(
    val user: String,
    val ai: Boolean,
    val image: Bitmap? = null
)



