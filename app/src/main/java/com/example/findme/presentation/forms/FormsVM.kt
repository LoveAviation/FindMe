package com.example.findme.presentation.forms

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
    private val formsRep : FormsRepository
): ViewModel(){

    private var _forms = MutableLiveData<List<Form>>()
    val forms: LiveData<List<Form>> get() = _forms

    fun getAllForms(){
        viewModelScope.launch {
            _forms.value = formsRep.getAllForms()
        }
    }

//    fun getByTags(tags: List<String>){
//        viewModelScope.launch {
//            _forms.value = formsRep.getByTags(tags)
//        }
//    }

    fun getByText(wordsToFind: String, tags: List<String>){
        viewModelScope.launch{
            _forms.value = formsRep.getByText(wordsToFind, tags)
        }
    }

    fun getWithCoordinates(wordsToFind: String, tags: List<String>, longitude: String, latitude: String, radius: String){
        viewModelScope.launch{
            val formsByText = async {formsRep.getByText(wordsToFind, tags)}.await()
            val formsByCoordinates = async {formsRep.getByCoordinates(longitude, latitude, radius)}.await()

            _forms.value = formsByText.intersect(formsByCoordinates).toList()
        }
    }

//    fun getCoords(longitude: String, latitude: String, radius: String){
//        viewModelScope.launch{
//            _forms.value = formsRep.getByCoordinates(longitude, latitude, radius)
//        }
//    }
}