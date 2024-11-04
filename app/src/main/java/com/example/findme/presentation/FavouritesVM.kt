package com.example.findme.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findme.domain.FavouritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesVM @Inject constructor(private val useCase: FavouritesUseCase): ViewModel() {

    private var _favForms = MutableLiveData<List<Int>?>(null)
    val favForms: LiveData<List<Int>?> get() = _favForms

    fun getAllList(){
        viewModelScope.launch{
            _favForms.value = useCase.getAll()
        }
    }

    fun addFavourite(id: Int){
        viewModelScope.launch(Dispatchers.IO){
            useCase.insert(id)
        }
    }

    fun deleteFavourite(id: Int){
        viewModelScope.launch(Dispatchers.IO){
            useCase.delete(id)
        }
    }

    fun deleteAll(){
        viewModelScope.launch(Dispatchers.IO){
            useCase.deleteAll()
        }
    }

}