package com.example.forms_sup.mapper

import com.example.forms_sup.entity.Form
import com.example.forms_sup.entity.FormDto
import javax.inject.Inject

class Mapper @Inject constructor(){

    private fun DtoToForm(input: FormDto): Form {
        return Form(
            title = input.title,
            description = input.description,
            tags = input.tags,
            location = ""
        )
    }

    fun FromDtoToForm(input: List<FormDto>): List<Form> {
        val list : MutableList<Form> = mutableListOf()

        for(formDto in input){
            list.add(DtoToForm(formDto))
        }

        return list.toList()
    }


}