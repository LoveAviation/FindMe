package com.example.forms_sup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.forms_sup.entity.Form
import com.example.forms_sup.mapper.Mapper
import com.example.forms_sup.repository.UseCase
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.io.WKBWriter
import org.locationtech.jts.geom.Point
import javax.inject.Inject

/**
 * Класс, выходящий за пределы модуля
 * отвечает за все действия с анкетами
 *
 * .
 * ⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⣠⣴⣶⣿⣿⣷⣶⣄⣀⣀
 * ⠄⠄⠄⠄⠄⠄⠄⠄⣰⣾⣿⣿⡿⢿⣿⣿⣿⣿⣿⣿⣿⣷⣦⡀
 * ⠄⠄⠄⠄⠄⠄⢀⣾⣿⣿⡟⠁⣰⣿⣿⣿⡿⠿⠻⠿⣿⣿⣿⣿⣧⠄
 * ⠄⠄⠄⠄⠄⠄⣾⣿⣿⠏⠄⣴⣿⣿⣿⠉⠄⠄⠄⠄⠄⠈⢻⣿⣿⣇
 * ⠄⠄⠄⢀⣠⣼⣿⣿⡏⠄⢠⣿⣿⣿⠇⠄⠄⠄⠄⠄⠄⠄⠈⣿⣿⣿⡀
 * ⠄⠄⣰⣿⣿⣿⣿⣿⡇⠄⢸⣿⣿⣿⡀⠄⠄⠄⠄⠄⠄⠄⠄⣿⣿⣿⡇
 * ⠄⢰⣿⣿⡿⣿⣿⣿⡇⠄⠘⣿⣿⣿⣧⠄⠄⠄⠄⠄⠄⢀⣸⣿⣿⣿⠁
 * ⠄⣿⣿⣿⠁⣿⣿⣿⡇⠄⠄⠻⣿⣿⣿⣷⣶⣶⣶⣶⣶⣿⣿⣿⣿⠃
 * ⢰⣿⣿⡇⠄⣿⣿⣿⠄⠄⠄⠄⠈⠻⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⠁
 * ⢸⣿⣿⡇⠄⣿⣿⣿⠄⠄⠄⠄⠄⠄⠄⠉⠛⠛⠛⠉⢉⣿⣿
 * ⢸⣿⣿⣇⠄⣿⣿⣿⠄⠄⠄⠄⠄⢀⣤⣤⣤⡀⠄⠄⢸⣿⣿⣿⣷⣦
 * ⠄⢻⣿⣿⣶⣿⣿⣿⠄⠄⠄⠄⠄⠈⠻⣿⣿⣿⣦⡀⠄⠉⠉⠻⣿⣿⡇
 * ⠄⠄⠛⠿⣿⣿⣿⣿⣷⣤⡀⠄⠄⠄⠄⠈⠹⣿⣿⣇⣀⠄⣠⣾⣿⣿⡇
 * ⠄⠄⠄⠄⠄⠄⠹⣿⣿⣿⣿⣦⣤⣤⣤⣤⣾⣿⣿⣿⣿⣿⣿⣿⣿⡟
 * ⠄⠄⠄⠄⠄⠄⠄⠄⠉⠻⢿⣿⣿⣿⣿⣿⣿⠿⠋⠉⠛⠋⠉⠉⠁
 * ⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠈⠉⠉⠉⠁
 */

class FormsRepository @Inject constructor(
    private val useCase: UseCase,
    private val mapper : Mapper
) {

    private val _formInsertionResult = MutableLiveData<Boolean?>(null)
    val formInsertionResult: LiveData<Boolean?> get() = _formInsertionResult

    fun encodeToWKB(longitude: String?, latitude: String?): String? {
        if(longitude == "null" || longitude == null || latitude == "null" || latitude == null) return null
        val geometryFactory = GeometryFactory()
        val point: Point = geometryFactory.createPoint(Coordinate(longitude.toDouble(), latitude.toDouble()))
        val wkbWriter = WKBWriter()
        val wkb: ByteArray = wkbWriter.write(point)

        return wkb.joinToString("") { String.format("%02X", it) }
    }

    suspend fun addForm(title: String, description: String, tags: List<String>, longitude: String?, latitude: String?, author: String, authorAvatar: String?, authorLogin: String){
        _formInsertionResult.value = useCase.addForm(title, description, tags, encodeToWKB(longitude, latitude), author, authorAvatar, authorLogin)
    }

    suspend fun getAllForms(): List<Form>{
        return mapper.fromDtoToForm(useCase.allForms())
    }

    suspend fun getByText(wordsToFind: String, tags: List<String>): List<Form>{
        return mapper.fromDtoToForm(useCase.getFormsByText(wordsToFind, tags))
    }

    suspend fun getByCoordinates(longitude: String, latitude: String, radius: String): List<Form>{
        return mapper.fromDtoToForm(useCase.searchByCoordinates(longitude, latitude, radius))
    }

    suspend fun myForms(login: String): List<Form>{
        return mapper.fromDtoToForm(useCase.myForms(login))
    }

    suspend fun updateAccInfo(login: String, author: String, authorAvatar: String?){
        useCase.updateAccInfo(login, author, authorAvatar)
    }

    suspend fun editForm(id: Int, title: String, description: String, tags: List<String>, longitude: String?, latitude: String?, author: String?): Boolean{
        return useCase.editForm(id, title, description, tags, encodeToWKB(longitude, latitude), author)
    }

    suspend fun deleteForm(id: Int): Boolean{
        return useCase.deleteForm(id)
    }

    suspend fun deleteAllForms(login: String): Boolean{
        return useCase.deleteAllForms(login)
    }

    suspend fun getFavourites(ids: List<Int>): List<Form>{
        return mapper.fromDtoToForm(useCase.getFavourites(ids))
    }

}