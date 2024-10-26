package com.example.account_fb.data

import android.content.ContentValues.TAG
import android.media.metrics.Event
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.account_fb.entity.Account
import com.example.account_fb.other.ErrorStates
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject

class DatabaseUC @Inject constructor(){

    private val databaseReference: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference.child("Users") }

    private val _errorDatabase = MutableLiveData<ErrorStates>(ErrorStates.NULL)
    val errorDatabase: LiveData<ErrorStates> get() = _errorDatabase

    private val _loginResult = MutableLiveData<Account?>()
    val loginResult: LiveData<Account?> get() = _loginResult

    fun loginUser(login: String, password: String) {
        _errorDatabase.value = ErrorStates.NULL

        val node = databaseReference.child(login)
        node.get().addOnCompleteListener { task: Task<DataSnapshot> ->
            if (task.isSuccessful) {
                val snapshot = task.result
                val storedPassword = snapshot?.child("password")?.value
                if (storedPassword.toString() == password) {
                    _loginResult.value = Account(
                        storedPassword.toString(),
                        snapshot.child("name").value.toString(),
                        snapshot.child("surname").value.toString(),
                        snapshot.child("urlAvatar").value.toString())
                } else {
                    _errorDatabase.value = ErrorStates.WRONG_PASSWORD
                }
            } else {
                _errorDatabase.value = ErrorStates.ERROR
            }
        }
    }

    private val _signupResult = MutableLiveData<String?>()
    val signupResult: LiveData<String?> get() = _signupResult

    fun signupUser(login: String, password: String, name: String, surname: String, urlAvatar: String = ""){
        _errorDatabase.value = ErrorStates.NULL

        _signupResult.value = null
        val oldUser = databaseReference.child(login)
        oldUser.get().addOnCompleteListener { task: Task<DataSnapshot> ->
            if(task.isSuccessful){
                val snapshot = task.result.child("password").value
                if(snapshot != null){
                    _signupResult.value = "ENGAGED"
                }else{
                    val account = Account(
                        password,
                        name,
                        surname,
                        urlAvatar
                    )
                    databaseReference.child(login).setValue(account).addOnCompleteListener { result ->
                        if(result.isSuccessful) _signupResult.value = urlAvatar
                        else _errorDatabase.value = ErrorStates.ERROR
                    }
                }
            }
        }
    }

    private val _editResult = MutableLiveData<String?>()
    val editResult: LiveData<String?> get() = _editResult

    fun changeData(login: String, name: String, surname: String, password: String, avatar: String?){
        _errorDatabase.value = ErrorStates.NULL

        _editResult.value = null
        val userRef = databaseReference.child(login)
        userRef.child("password").setValue(password).addOnSuccessListener{
            userRef.child("name").setValue(name).addOnSuccessListener{
                userRef.child("surname").setValue(surname).addOnSuccessListener{
                    if(avatar != null) {
                        userRef.child("urlAvatar").setValue(avatar).addOnSuccessListener {
                            _editResult.value = "SUCCESS"
                        }.addOnFailureListener { _errorDatabase.value = ErrorStates.ERROR }
                    }else{
                        _editResult.value = "SUCCESS"
                    }
                }.addOnFailureListener{ _errorDatabase.value = ErrorStates.ERROR }
            }.addOnFailureListener{ _errorDatabase.value = ErrorStates.ERROR }
        }.addOnFailureListener{ _errorDatabase.value = ErrorStates.ERROR }
    }

}