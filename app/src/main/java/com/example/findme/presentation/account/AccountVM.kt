package com.example.findme.presentation.account


import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.account_fb.data.DatabaseUC
import com.example.account_fb.data.StorageUC
import com.example.account_fb.entity.Account
import com.example.account_fb.other.ErrorStates
import dagger.hilt.android.internal.Contexts
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel, который отвечает за любые действия с аккаунтАМИ
 */

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

    private var _deletingState = MutableLiveData<Boolean?>(null)
    val deletingState: LiveData<Boolean?> get() = _deletingState

    fun signIn(context: Context, lifecycleOwner: LifecycleOwner, login: String, password: String, name: String, surname: String, urlAvatar: Uri?){
        _signInState.value = null
        waitForError(lifecycleOwner)
        if(urlAvatar != null){
            storageUC.uploadAvatar(context, urlAvatar, login)
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

    fun edit(context: Context, lifecycleOwner: LifecycleOwner, login: String, name: String, surname: String, password: String, avatar: String?){
        waitForError(lifecycleOwner)
        if(avatar != "null" && avatar != null && !avatar.startsWith("https://", ignoreCase = true)) {
            storageUC.changeAvatar(context, avatar.toUri(), login)
            storageUC.storageState.observe(lifecycleOwner){ avatarUrl ->
                if(avatarUrl != null){
                    editAccDB(lifecycleOwner, login, name, surname, password, avatarUrl)
                }
            }
        }else if(avatar!!.startsWith("https://", ignoreCase = true)){
            editAccDB(lifecycleOwner, login, name, surname, password, avatar)
        }
        else{
            storageUC.deleteAvatar(login)
            storageUC.storageState.observe(lifecycleOwner){ result ->
                editAccDB(lifecycleOwner, login, name, surname, password, "")
            }
        }
    }

    private fun editAccDB(lifecycleOwner: LifecycleOwner, login: String, name: String, surname: String, password: String, avatar: String?){
        databaseUC.changeData(login, name, surname, password, avatar)
        databaseUC.editResult.observe(lifecycleOwner){ result ->
            when(result){
                "SUCCESS" -> {
                    _editState.value = avatar
                }
            }
        }
    }

    fun deleteAccount(lifecycleOwner: LifecycleOwner, login: String){
        waitForError(lifecycleOwner)
        databaseUC.deleteAccount(login)
        databaseUC.deletingResult.observe(lifecycleOwner){ result ->
            if(result == true){
                storageUC.deleteAvatar(login)
            }
        }

        storageUC.storageState.observe(lifecycleOwner){ result ->
            if(result == "DELETED"){
                _deletingState.value = true
            }
        }
    }

    private var _checkState = MutableLiveData<Boolean?>(null)
    val checkState: LiveData<Boolean?> get() = _checkState

    fun checkUser(lifecycleOwner: LifecycleOwner, login: String) {
        waitForError(lifecycleOwner)
        databaseUC.checkUser(login)
        databaseUC.checkResult.observe(lifecycleOwner){ result ->
            if(result != null){
                _checkState.value = result
            }
        }
    }

    private fun waitForError(lifecycleOwner: LifecycleOwner) {
        _error.value = ErrorStates.NULL
        storageUC.errorStorage.observe(lifecycleOwner){ error ->
            _error.value = error!!
        }
        databaseUC.errorDatabase.observe(lifecycleOwner){ error ->
            _error.value = error!!
        }
    }
}