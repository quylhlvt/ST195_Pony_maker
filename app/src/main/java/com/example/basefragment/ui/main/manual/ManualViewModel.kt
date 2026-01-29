package com.example.basefragment.ui.main.manual

import androidx.lifecycle.ViewModel
import com.example.basefragment.core.helper.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManualViewModel @Inject constructor( private val sharedPreferences: SharedPreferencesManager
) : ViewModel(){
}