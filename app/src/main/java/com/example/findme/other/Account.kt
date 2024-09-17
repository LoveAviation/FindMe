package com.example.findme.other

/**
 * ЗДЕСЬ НЕТ ЛОГИНА,
 * ОН СРАЗУ ПРОСТАВЛЯЕТСЯ В РОДИТЕЛЬСКОМ УЗЛЕ
 */
data class Account(
    var password: String,
    var name: String?,
    var surname: String?,
    var urlAvatar: String?
)