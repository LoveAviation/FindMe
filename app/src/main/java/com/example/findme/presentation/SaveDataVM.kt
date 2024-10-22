package com.example.findme.presentation

import jakarta.inject.Inject


class SaveDataVM @Inject constructor() {
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