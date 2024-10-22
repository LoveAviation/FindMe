package com.example.forms_sup.entity

import javax.inject.Inject

data class Form @Inject constructor(
    val title: String,
    val description: String,
    val tags: List<String>,
    val location: String?,
    val author: String?,
    val author_avatar: String?,
    val author_login: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Form) return false

        return title == other.title &&
                description == other.description &&
                author_login == other.author_login &&
                tags == other.tags
    }

    override fun hashCode(): Int {
        return listOf(title, description, author_login, tags).hashCode()
    }
}