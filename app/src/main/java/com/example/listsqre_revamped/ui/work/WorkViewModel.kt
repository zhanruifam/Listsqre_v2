package com.example.listsqre_revamped.ui.work

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WorkViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Nothing to show in work" // work fragment
    }
    val text: LiveData<String> = _text
}