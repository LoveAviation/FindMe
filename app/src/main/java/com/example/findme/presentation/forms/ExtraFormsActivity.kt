package com.example.findme.presentation.forms

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.findme.R
import com.example.findme.databinding.ActivityExtraFormsBinding
import com.example.findme.presentation.FavouritesVM
import com.example.findme.presentation.MainActivity
import com.example.findme.presentation.forms.adapter.FormsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.isNotEmpty
import kotlin.getValue

@AndroidEntryPoint
class ExtraFormsActivity : AppCompatActivity() {

    private val favVM : FavouritesVM by viewModels()
    private lateinit var binding: ActivityExtraFormsBinding
    private val viewModel : FormsVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExtraFormsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener{
            finish()
        }

        loadContent()
    }

    private fun loadContent(){
        binding.loading.visibility = View.VISIBLE
        if(intent.getStringExtra(KEY) == MY_FORMS){
            binding.toolbarTitle.text = getString(R.string.my_forms_button)

            binding.extraRV.layoutManager = LinearLayoutManager(this)
            viewModel.getAccountForms(this, intent.getStringExtra(LOGIN).toString())
            viewModel.forms.observe(this){ forms ->
                if (forms != null) {
                    binding.loading.visibility = View.GONE
                    if (forms.isNotEmpty()){
                        binding.extraRV.adapter = FormsAdapter(this, forms) { selectedItem ->
                            var name = intent.getStringExtra(NAME)
                            var surname = intent.getStringExtra(SURNAME)
                            val intent = Intent(this, EditForm::class.java)
                            intent.putExtra(FormActivity.KEY_ID, selectedItem.id)
                            intent.putExtra(FormActivity.KEY_TITLE, selectedItem.title)
                            intent.putExtra(FormActivity.KEY_DESCRIPTION, selectedItem.description)
                            intent.putExtra(FormActivity.KEY_TAGS, listFormToBasic(selectedItem.tags))
                            intent.putExtra(FormActivity.KEY_AUTHOR, selectedItem.author)
                            intent.putExtra(FormActivity.KEY_AVATAR, selectedItem.authorAvatar)
                            intent.putExtra(FormActivity.KEY_LOCATION, selectedItem.location)
                            intent.putExtra(FormActivity.KEY_LOGIN, selectedItem.authorLogin)
                            intent.putExtra(MainActivity.KEY_NAME, name)
                            intent.putExtra(MainActivity.KEY_SURNAME, surname)
                            startActivity(intent)
                        }
                        binding.emptyText.visibility = View.GONE
                    }else{
                        binding.emptyText.visibility = View.VISIBLE
                        binding.extraRV.visibility = View.GONE
                    }
                }
            }

        }else if (intent.getStringExtra(KEY) == FAVOURITE){
            binding.toolbarTitle.text = getString(R.string.favourite_forms)

            favVM.getAllList()
            favVM.favForms.observe(this){ result ->
                if(result != null) {
                    binding.loading.visibility = View.GONE
                    if (result.isNotEmpty()) {
                        viewModel.favouriteForms(this, result)
                        binding.emptyText.visibility = View.GONE
                    }else{
                        binding.emptyText.visibility = View.VISIBLE
                        binding.extraRV.visibility = View.GONE
                    }
                }
            }

            binding.extraRV.layoutManager = LinearLayoutManager(this)
            viewModel.favouriteForms.observe(this){ result ->
                if (result != null) {
                    binding.extraRV.adapter = FormsAdapter(this, result) { selectedItem ->
                        val intent = Intent(this, FormActivity::class.java)
                        intent.putExtra(FormActivity.KEY_ID, selectedItem.id)
                        intent.putExtra(FormActivity.KEY_TITLE, selectedItem.title)
                        intent.putExtra(FormActivity.KEY_DESCRIPTION, selectedItem.description)
                        intent.putExtra(FormActivity.KEY_TAGS, listFormToBasic(selectedItem.tags))
                        intent.putExtra(FormActivity.KEY_AUTHOR, selectedItem.author)
                        intent.putExtra(FormActivity.KEY_AVATAR, selectedItem.authorAvatar)
                        intent.putExtra(FormActivity.KEY_LOCATION, selectedItem.location)
                        intent.putExtra(FormActivity.KEY_FAVOURITE, true)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadContent()
    }

    private fun listFormToBasic(input: List<String>): String{
        if (input.isEmpty()) return ""
        return input.joinToString(" ")
    }

    companion object{
        const val KEY = "extra type"
        const val LOGIN = "login"
        const val NAME = "name"
        const val SURNAME = "surname"
        const val FAVOURITE = "favourite forms"
        const val MY_FORMS = "my forms"
    }
}