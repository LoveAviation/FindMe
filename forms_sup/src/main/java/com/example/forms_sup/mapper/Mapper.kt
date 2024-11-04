package com.example.forms_sup.mapper

import com.example.forms_sup.entity.Form
import com.example.forms_sup.entity.FormDto
import org.locationtech.jts.geom.Point
import org.locationtech.jts.io.WKBReader
import javax.inject.Inject

/**
 * Класс для перехода от анкеты к DTO
 */

class Mapper @Inject constructor(){

    private fun decodeWKB(wkbHex: String?): String? {
        if(wkbHex == null) return null
        val wkbReader = WKBReader()

        val wkbBytes = wkbHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

        val geometry = wkbReader.read(wkbBytes)

        if (geometry is Point) {
            return "${geometry.y} ${geometry.x}" //latitude, longitude
        }
        return null
    }

    private fun dtoToForm(input: FormDto): Form {
        return Form(
            id = input.id,
            title = input.title,
            description = input.description,
            tags = input.tags,
            location = decodeWKB(input.location),
            author = input.author,
            authorAvatar = input.authorAvatar,
            authorLogin = input.authorLogin
        )
    }

    fun fromDtoToForm(input: List<FormDto>): List<Form> {
        val list : MutableList<Form> = mutableListOf()

        for(formDto in input){
            list.add(dtoToForm(formDto))
        }

        return list.toList()
    }


}