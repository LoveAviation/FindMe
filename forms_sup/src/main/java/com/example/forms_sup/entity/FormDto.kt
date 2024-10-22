package com.example.forms_sup.entity

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class FormDto(
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("tags")
    val tags: List<String>,
    @SerialName("location")
    val location: String?,
    @SerialName("author")
    val author: String?,
    @SerialName("author_avatar")
    val author_avatar: String?,
    @SerialName("author_login")
    val author_login: String
)