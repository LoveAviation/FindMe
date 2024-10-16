package com.example.account_fb.data

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.account_fb.entity.Account
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject

class DatabaseUC @Inject constructor(){

    private val databaseReference: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference.child("Users") }

    private val _loginResult = MutableLiveData<Account?>()
    val loginResult: LiveData<Account?> get() = _loginResult

    fun loginUser(login: String, password: String) {
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
                    _loginResult.value = null
                }
            } else {
                _loginResult.value = null
            }
        }
    }

    private val _signupResult = MutableLiveData<String?>()
    val signupResult: LiveData<String?> get() = _signupResult

    fun signupUser(login: String, password: String, name: String, surname: String, urlAvatar: String = ""){
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
                        else _signupResult.value = "ERROR"
                    }
                }
            }
        }
    }

    private val _editResult = MutableLiveData<String?>()
    val editResult: LiveData<String?> get() = _editResult

    fun changeData(login: String, name: String, surname: String, password: String, avatar: String?){
        _editResult.value = null
        val userRef = databaseReference.child(login)
        userRef.child("password").setValue(password).addOnSuccessListener{
            userRef.child("name").setValue(name).addOnSuccessListener{
                userRef.child("surname").setValue(surname).addOnSuccessListener{
                    if(avatar != null) {
                        userRef.child("urlAvatar").setValue(avatar).addOnSuccessListener {
                            _editResult.value = "SUCCESS"
                        }.addOnFailureListener { _editResult.value = "ERROR" }
                    }else{
                        _editResult.value = "SUCCESS"
                    }
                    Log.d(TAG, "${_editResult.value}")
                }.addOnFailureListener{ _editResult.value = "ERROR" }
            }.addOnFailureListener{ _editResult.value = "ERROR" }
        }.addOnFailureListener{ _editResult.value = "ERROR" }
    }

}