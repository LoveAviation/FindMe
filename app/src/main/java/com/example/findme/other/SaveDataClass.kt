package com.example.findme.other

import jakarta.inject.Inject

/**
 * Класс, для удобного хранения
 * аккаунта через SharedPref
 *
 * @see accLogin
 * @see accPassword
 * @see accName
 * @see accSurname
 * @see accAvatar
 */

class SaveDataClass @Inject constructor() {
    var accLogin: String? = null
    var accPassword: String? = null
    var accName: String? = null
    var accSurname: String? = null
    var accAvatar: String? = null

    fun clearAll(){
        accLogin = null
        accPassword = null
        accName = null
        accSurname = null
        accAvatar = null
    }
}