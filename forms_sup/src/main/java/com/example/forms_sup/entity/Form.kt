package com.example.forms_sup.entity

import javax.inject.Inject

data class Form @Inject constructor(
    val id: Int? = null,
    val title: String,
    val description: String,
    val tags: List<String>,
    val location: String?,
    val author: String?,
    val author_avatar: String?,
    val author_login: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Form) return false

        return id == other.id
    }

    override fun hashCode(): Int {
        return listOf(id).hashCode()
    }
}