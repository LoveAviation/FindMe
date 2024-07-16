package com.example.findme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel {
    private val _firstName = MutableLiveData<String>()
    val firstName: LiveData<String> = _firstName

    private val _lastName = MutableLiveData<String>()
    val lastName: LiveData<String> = _lastName

    fun setNames(firstName: String, lastName: String) {
        _firstName.value = firstName
        _lastName.value = lastName
    }
}