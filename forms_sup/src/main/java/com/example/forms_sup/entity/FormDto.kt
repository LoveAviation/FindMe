package com.example.forms_sup.entity

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class FormDto(
    @SerialName("id")
    val id: Int? = null,
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
    val authorAvatar: String?,
    @SerialName("author_login")
    val authorLogin: String
)