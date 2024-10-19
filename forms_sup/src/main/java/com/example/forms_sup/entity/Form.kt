package com.example.forms_sup.entity

import javax.inject.Inject

data class Form @Inject constructor(
    val title: String,
    val description: String,
    val tags: List<String>,
    val location: String?,
    val author: String?,
    val author_avatar: String?
)