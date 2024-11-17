package com.example.listsqre_revamped.ui.personal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PersonalViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Nothing to show in personal" // personal fragment
    }
    val text: LiveData<String> = _text
}