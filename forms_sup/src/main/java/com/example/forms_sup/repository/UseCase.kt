package com.example.forms_sup.repository

import com.example.forms_sup.entity.Form
import com.example.forms_sup.entity.FormDto
import com.example.forms_sup.mapper.Mapper
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject


class UseCase @Inject constructor(){

    private val SUPABASE_NAME_KEY = "Forms"

    private val converter = Mapper()

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://qcixwatrekwteykhfpcy.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFjaXh3YXRyZWt3dGV5a2hmcGN5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjQxMDA4NzYsImV4cCI6MjAzOTY3Njg3Nn0.IJOlZQKAK9JiPEHVLGXWdyLYL31x6FyN8cva0akbw84"
    ) {
        install(Postgrest)
    }

    suspend fun addForm(title: String, description: String, tags: List<String>, location: String = ""){
        supabase.from(SUPABASE_NAME_KEY).insert(FormDto(title, description, tags, location))
    }

    suspend fun allForms(): List<Form>{
        val result = supabase.from(SUPABASE_NAME_KEY).select().decodeList<FormDto>()

        return converter.FromDtoToForm(result)
    }

//    suspend fun getFormsByTags(wordsToFind: List<String>): List<FormDto> {
//        val result = supabase
//            .from(SUPABASE_NAME_KEY)
//            .select {
//                filter {
//                    contains("tags", listOf(wordsToFind))
//                }
//            }
//        return result.decodeList()
//    }


    suspend fun getFormsByText(wordsToFind: String, tags: List<String>): List<FormDto>{
        val words = wordsToFind.split(" ").filter { it.isNotBlank() }

        val result = supabase
            .from(SUPABASE_NAME_KEY)
            .select {
                filter {
                    and {
                        or {
                            words.forEach { word ->
                                ilike("title", "%$word%")
                                ilike("description", "%$word%")
                            }
                        }
                        if(tags.isNotEmpty()){
                            contains("tags", tags)
                        }
                    }
                }
            }.decodeList<FormDto>()


        fun similarityScore(description: String, words: List<String>): Int {
            val descriptionWords = description.split(" ").filter { it.isNotBlank() }
            val commonWordsCount = words.count { word ->
                descriptionWords.contains(word)
            }

            val orderMatchScore = descriptionWords.zip(words).count { (descWord, queryWord) ->
                descWord.equals(queryWord, ignoreCase = true)
            }

            return commonWordsCount * 10 + orderMatchScore * 5
        }

        return result.sortedByDescending { result ->
            similarityScore(result.description, words)
        }
    }

    suspend fun searchByCoordinates(longitude: String, latitude:String, radius: String): List<FormDto>{
        val result = supabase.from(SUPABASE_NAME_KEY).postgrest.rpc(
            function = "nearby_forms",
            parameters = buildJsonObject {
                put("lat", latitude)
                put("long", longitude)
                put("radius", radius)
            }
        ).decodeAs<List<FormDto>>()

        return result
    }
}