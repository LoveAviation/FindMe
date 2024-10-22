package com.example.forms_sup.repository

import android.content.ContentValues.TAG
import android.util.Log
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

    suspend fun addForm(title: String, description: String, tags: List<String>, location: String?, author: String?, author_avatar: String?, author_login: String): Boolean{
        supabase.from(SUPABASE_NAME_KEY).insert(FormDto(title, description, tags, location, author, author_avatar, author_login))

        Log.d(TAG, "STARTING RESULT")
        val result = supabase.from(SUPABASE_NAME_KEY).select{
            filter {
                and{
                    eq("title", title)
                    eq("description", description)
                    if(location != null){
                        eq("location", location)
                    }
                    eq("author_login", author_login)
                }
            }
        }.decodeList<FormDto>()

        return result.isNotEmpty()
    }

    suspend fun allForms(): List<Form>{
        val result = supabase.from(SUPABASE_NAME_KEY).select().decodeList<FormDto>()
        return converter.FromDtoToForm(result)
    }

    suspend fun getFormsByText(wordsToFind: String, tags: List<String>): List<FormDto> {
        val words = wordsToFind.split(" ").filter { it.isNotBlank() }

        val result = supabase
            .from(SUPABASE_NAME_KEY)
            .select {
                filter {
                    and {
                        or {
                            words.forEach { word ->
                                ilike("description", "%$word%")
                                ilike("title", "%$word%")
                            }
                        }
                        if (tags.isNotEmpty()) {
                            contains("tags", tags)
                        }
                    }
                }
            }.decodeList<FormDto>()

        fun similarityScore(title: String, description: String, words: List<String>): Int {
            val titleWords = title.split(" ").filter { it.isNotBlank() }
            val descriptionWords = description.split(" ").filter { it.isNotBlank() }

            val commonTitleWordsCount = words.count { word ->
                titleWords.contains(word)
            }

            val commonDescriptionWordsCount = words.count { word ->
                descriptionWords.contains(word)
            }

            val orderMatchScoreTitle = titleWords.zip(words).count { (titleWord, queryWord) ->
                titleWord.equals(queryWord, ignoreCase = true)
            }

            val orderMatchScoreDescription = descriptionWords.zip(words).count { (descWord, queryWord) ->
                descWord.equals(queryWord, ignoreCase = true)
            }
            return commonTitleWordsCount * 12 + orderMatchScoreTitle * 6 +
                    commonDescriptionWordsCount * 10 + orderMatchScoreDescription * 5
        }


        return result.sortedByDescending { form ->
            similarityScore(form.title, form.description, words)
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

    suspend fun myForms(login: String): List<FormDto>{
        val myForms = supabase.from(SUPABASE_NAME_KEY).select{
            filter {
                eq("author_login", login)
            }
        }.decodeAs<List<FormDto>>()

        return myForms
    }

    suspend fun updateAccInfo(login: String, author: String, author_avatar: String?){
        supabase.from(SUPABASE_NAME_KEY).update(
            {
                set("author", author)
                set("author_avatar", author_avatar)
            }
        ) {
            filter {
                eq("author_login", login)
            }
        }
    }
}