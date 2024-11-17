package com.example.listsqre_revamped.ui.others

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OthersViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Nothing to show in others" // others fragment
    }
    val text: LiveData<String> = _text
}