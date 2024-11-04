package com.example.account_fb.entity

/**
 * Класс аккаунта
 * @param password
 * @param name
 * @param surname
 * @param urlAvatar
 *
 * ЗДЕСЬ НЕТ ЛОГИНА,
 * ОН СРАЗУ ПРОСТАВЛЯЕТСЯ В РОДИТЕЛЬСКОМ УЗЛЕ
 */
data class Account(
    var password: String,
    var name: String?,
    var surname: String?,
    var urlAvatar: String?
)