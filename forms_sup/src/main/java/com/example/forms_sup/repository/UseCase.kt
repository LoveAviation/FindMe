package com.example.forms_sup.repository

import com.example.forms_sup.entity.FormDto
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

/**
 * Класс, который выполняет все действия с анкетами
 * требуются корутины.
 */

class UseCase @Inject constructor(){

    private val supabaseNameKey = "Forms"


    private val supabase = createSupabaseClient(
        supabaseUrl = "https://qcixwatrekwteykhfpcy.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFjaXh3YXRyZWt3dGV5a2hmcGN5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjQxMDA4NzYsImV4cCI6MjAzOTY3Njg3Nn0.IJOlZQKAK9JiPEHVLGXWdyLYL31x6FyN8cva0akbw84"
    ) {
        install(Postgrest)
    }

    suspend fun addForm(title: String, description: String, tags: List<String>, location: String?, author: String?, authorAvatar: String?, authorLogin: String): Boolean{
        return try {
            supabase.from(supabaseNameKey).insert(
                FormDto(null ,title, description, tags, location, author, authorAvatar, authorLogin)
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun allForms(): List<FormDto>{
        return supabase.from(supabaseNameKey).select().decodeList<FormDto>()
    }

    suspend fun getFormsByText(wordsToFind: String, tags: List<String>): List<FormDto> {
        val words = wordsToFind.split(" ").filter { it.isNotBlank() }

        val result = supabase
            .from(supabaseNameKey)
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
        val result = supabase.from(supabaseNameKey).postgrest.rpc(
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
        val myForms = supabase.from(supabaseNameKey).select{
            filter {
                eq("author_login", login)
            }
        }.decodeAs<List<FormDto>>()

        return myForms
    }

    suspend fun updateAccInfo(login: String, author: String, authorAvatar: String?){
        supabase.from(supabaseNameKey).update(
            {
                set("author", author)
            }
        ) {
            filter {
                eq("author_login", login)
                neq("author", login)
            }
        }

        supabase.from(supabaseNameKey).update(
            {
                set("author_avatar", authorAvatar)
            }
        ) {
            filter {
                eq("author_login", login)
            }
        }
    }

    suspend fun editForm(id: Int, title: String, description: String, tags: List<String>, location: String?, author: String?): Boolean{
        try {
            supabase.from(supabaseNameKey).update(
                {
                    set("title", title)
                    set("description", description)
                    set("tags", tags)
                    set("location", location)
                    set("author", author)
                }
            ){
              filter {
                    eq("id", id)
                }
            }
            return true
        }catch (_: Exception){
            return false
        }
    }

    suspend fun deleteForm(id: Int): Boolean{
        return try {
            supabase.from(supabaseNameKey).delete{
                filter {
                    eq("id", id)
                }
            }
            true
        }catch (_: Exception){
            false
        }
    }

    suspend fun getFavourites(ids: List<Int>): List<FormDto>{
        return supabase.from(supabaseNameKey).select{
            filter {
                or {
                    ids.forEach { id ->
                        eq("id", id)
                    }
                }
            }
        }.decodeAs<List<FormDto>>()
    }

    suspend fun deleteAllForms(login : String): Boolean{
        return try {
            supabase.from(supabaseNameKey).delete {
                filter {
                    eq("author_login", login)
                }
            }
            true
        }catch (_: Exception){
            false
        }
    }
}