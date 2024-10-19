package com.example.forms_sup

import com.example.forms_sup.entity.Form
import com.example.forms_sup.mapper.Mapper
import com.example.forms_sup.repository.UseCase
import javax.inject.Inject


class FormsRepository @Inject constructor(
    private val useCase: UseCase,
    private val mapper : Mapper
) {

    suspend fun addForm(title: String, description: String, tags: List<String>, location: String?, author: String, author_avatar: String){
        useCase.addForm(title, description, tags, location, author, author_avatar)
    }

    suspend fun getAllForms(): List<Form>{
        return useCase.allForms()
    }

//    suspend fun getByTags(tags: List<String>): List<Form>{
//        return mapper.FromDtoToForm(useCase.getFormsByTags(tags))
//    }

    suspend fun getByText(wordsToFind: String, tags: List<String>): List<Form>{
        return mapper.FromDtoToForm(useCase.getFormsByText(wordsToFind, tags))
    }

    suspend fun getByCoordinates(longitude: String, latitude: String, radius: String): List<Form>{
        return mapper.FromDtoToForm(useCase.searchByCoordinates(longitude, latitude, radius))
    }

}