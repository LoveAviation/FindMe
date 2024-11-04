package com.example.findme.presentation.forms

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forms_sup.FormsRepository
import com.example.forms_sup.entity.Form
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormsVM @Inject constructor(
    private val formsRep : FormsRepository,
): ViewModel(){

    private var _forms = MutableLiveData<List<Form>?>()
    val forms: LiveData<List<Form>?> get() = _forms

    private val _formAddingResult = MutableLiveData<Boolean?>(null)
    val formAddingResult: LiveData<Boolean?> get() = _formAddingResult

    fun getAllForms(context: Context){
        if (isInternetAvailable(context)) {
            viewModelScope.launch {
                _forms.value = formsRep.getAllForms()
            }
        }
    }

    fun getByText(context: Context, wordsToFind: String, tags: List<String>){
        if (isInternetAvailable(context)) {
            viewModelScope.launch {
                _forms.value = formsRep.getByText(wordsToFind, tags)
            }
        }
    }

    fun getWithCoordinates(context: Context, wordsToFind: String, tags: List<String>, longitude: String, latitude: String, radius: String){
        if (isInternetAvailable(context)) {
            viewModelScope.launch {
                val formsByText = async { formsRep.getByText(wordsToFind, tags) }.await()
                val formsByCoordinates =
                    async { formsRep.getByCoordinates(longitude, latitude, radius) }.await()

                _forms.value = formsByText.intersect(formsByCoordinates).toList()
            }
        }
    }

    fun addForm(context: Context, lifecycleOwner: LifecycleOwner, title: String, description: String, tags: List<String>, longitude: String, latitude: String, author: String, author_avatar: String?, author_login: String){
        if (isInternetAvailable(context)) {
            viewModelScope.launch {
                var avatar = author_avatar
                if (author_avatar.isNullOrEmpty()) avatar = null
                formsRep.addForm(title, description, tags, longitude, latitude, author, avatar, author_login
                )
                formsRep.formInsertionResult.observe(lifecycleOwner) { result ->
                    if (result != null) {
                        _formAddingResult.value = result
                    }
                }
            }
        }
    }
    fun getAccountForms(context: Context, login: String){
        if (isInternetAvailable(context)) {
            viewModelScope.launch {
                _forms.value = formsRep.myForms(login)
            }
        }
    }

    fun updateAccInfo(context: Context, login: String, author: String, author_avatar: String?){
        if (isInternetAvailable(context)) {
            viewModelScope.launch {
                formsRep.updateAccInfo(login, author, author_avatar)
            }
        }
    }

    private val _formEditingResult = MutableLiveData<Boolean?>(null)
    val formEditingResult: LiveData<Boolean?> get() = _formEditingResult

    fun editForm(context: Context, id: Int, title: String, description: String, tags: List<String>, longitude: String?, latitude: String?, author: String?){
        if (isInternetAvailable(context)) {
            viewModelScope.launch {
                _formEditingResult.value =
                    formsRep.editForm(id, title, description, tags, longitude, latitude, author)
            }
        }
    }

    private val _formDeletingResult = MutableLiveData<Boolean?>(null)
    val formDeletingResult: LiveData<Boolean?> get() = _formDeletingResult

    fun deleteForm(context: Context, id: Int){
        if (isInternetAvailable(context)) {
            viewModelScope.launch {
                _formDeletingResult.value = formsRep.deleteForm(id)
            }
        }
    }

    fun deleteAllForms(context: Context, login: String){
        if (isInternetAvailable(context)) {
            viewModelScope.launch {
                _formDeletingResult.value = formsRep.deleteAllForms(login)
            }
        }
    }

    private var _favouriteForms = MutableLiveData<List<Form>?>()
    val favouriteForms: LiveData<List<Form>?> get() = _favouriteForms

    fun favouriteForms(context: Context, ids: List<Int>){
        if (isInternetAvailable(context)) {
            viewModelScope.launch {
                _favouriteForms.value = formsRep.getFavourites(ids)
            }
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val network = connectivityManager?.activeNetwork
        val networkCapabilities = connectivityManager?.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }
}