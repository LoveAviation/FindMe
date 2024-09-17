package com.example.findme.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SaveDataVM @Inject constructor(): ViewModel() {
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