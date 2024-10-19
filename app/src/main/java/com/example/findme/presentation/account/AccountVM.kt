package com.example.findme.presentation.account


import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.account_fb.data.DatabaseUC
import com.example.account_fb.data.StorageUC
import com.example.account_fb.entity.Account
import com.example.account_fb.other.ErrorStates
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AccountVM @Inject constructor(
    private val databaseUC : DatabaseUC,
    private val storageUC : StorageUC
): ViewModel() {

    private val _error = MutableLiveData<ErrorStates>(ErrorStates.NULL)
    val error: LiveData<ErrorStates> get() = _error

    private var _signInState = MutableLiveData<String?>()
    val signInState: LiveData<String?> get() = _signInState

    private var _logInState = MutableLiveData<Account?>(null)
    val logInState: LiveData<Account?> get() = _logInState

    fun signIn(lifecycleOwner: LifecycleOwner, login: String, password: String, name: String, surname: String, urlAvatar: Uri?){
        _signInState.value = null
        waitForError(lifecycleOwner)
        if(urlAvatar != null){
            storageUC.uploadAvatar(urlAvatar, login)
            storageUC.storageState.observe(lifecycleOwner){ avatarURL ->
                if (avatarURL != null){
                    databaseUC.signupUser(login, password, name, surname, avatarURL)
                    databaseUC.signupResult.observe(lifecycleOwner){ result ->
                        if(result != null){
                             _signInState.value = result
                        }
                    }
                }
            }
        }else{
            databaseUC.signupUser(login, password, name, surname)
            databaseUC.signupResult.observe(lifecycleOwner){ result ->
                if(result != null){
                    _signInState.value = result
                }
            }
        }
    }

    fun logIn(lifecycleOwner: LifecycleOwner, login: String, password: String){
        waitForError(lifecycleOwner)
        databaseUC.loginUser(login, password)
        databaseUC.loginResult.observe(lifecycleOwner){ result ->
            if(result != null){
                _logInState.value = result
            }
        }
    }


    private var _editState = MutableLiveData<String?>()
    val editState: LiveData<String?> get() = _editState

    fun edit(lifecycleOwner: LifecycleOwner, login: String, name: String, surname: String, password: String, avatar: String?){
        waitForError(lifecycleOwner)
        if(avatar != null) {
            storageUC.changeAvatar(avatar.toUri(), login)
            storageUC.storageState.observe(lifecycleOwner){ avatarUrl ->
                if(avatarUrl != null){
                    editAccDB(lifecycleOwner, login, name, surname, password, avatarUrl)
                }
            }
        }else{
            editAccDB(lifecycleOwner, login, name, surname, password, avatar)
        }
    }

    private fun editAccDB(lifecycleOwner: LifecycleOwner, login: String, name: String, surname: String, password: String, avatar: String?){
        databaseUC.changeData(login, name, surname, password, avatar)
        databaseUC.editResult.observe(lifecycleOwner){ result ->
            when(result){
                "SUCCESS" -> _editState.value = avatar
            }
        }
    }

    private fun waitForError(lifecycleOwner: LifecycleOwner) {
        storageUC.errorStorage.observe(lifecycleOwner){ error ->
            _error.value = error!!
        }
        databaseUC.errorDatabase.observe(lifecycleOwner){ error ->
            _error.value = error!!
        }
    }
}