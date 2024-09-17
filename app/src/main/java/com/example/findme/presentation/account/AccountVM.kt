package com.example.findme.presentation.account


import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.findme.domain.DatabaseUC
import com.example.findme.domain.StorageUC
import com.example.findme.other.Account
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AccountVM @Inject constructor(
    private val databaseUC : DatabaseUC,
    private val storageUC : StorageUC
): ViewModel() {

    private var _signInState = MutableLiveData<String?>()
    val signInState: LiveData<String?> get() = _signInState

    private var _logInState = MutableLiveData<Account?>(null)
    val logInState: LiveData<Account?> get() = _logInState

    fun signIn(lifecycleOwner: LifecycleOwner, login: String, password: String, name: String, surname: String, urlAvatar: Uri?){ // КОГДА ИЗ ГАЛЕРЕИ ПРИДЕТ ФОТОГРАФИЯ, ТО СРАЗУ ПЕРЕДАСТ URi
        _signInState.value = null
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
            databaseUC.signupUser(login, password, name, surname, null)
            databaseUC.signupResult.observe(lifecycleOwner){ result ->
                if(result != null){
                    _signInState.value = result
                }
            }
        }
    }

    fun logIn(lifecycleOwner: LifecycleOwner, login: String, password: String){
        databaseUC.loginUser(login, password)
        databaseUC.loginResult.observe(lifecycleOwner){ result ->
            if(result != null){
                _logInState.value = result
            }
        }
    }
}