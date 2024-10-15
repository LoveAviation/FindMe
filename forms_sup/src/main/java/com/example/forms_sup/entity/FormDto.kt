package com.example.forms_sup.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FormDto(
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("tags")
    val tags: List<String>,
    @SerialName("location")
    val location: String?
)