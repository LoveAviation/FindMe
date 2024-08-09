package com.example.findme.presentation

import androidx.lifecycle.ViewModel

class SaveDataVM: ViewModel() {
    var accLogin: String? = null
    var accPassword: String? = null
    var accName: String? = null
    var accSurname: String? = null

    fun clearAll(){
        accLogin = null
        accPassword = null
        accName = null
        accSurname = null
    }
}