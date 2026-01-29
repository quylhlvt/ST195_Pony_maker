package com.example.basefragment.ui.main.guide

import androidx.lifecycle.ViewModel
import com.example.basefragment.core.helper.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class GuideViewModel  @Inject constructor( private val sharedPreferences: SharedPreferencesManager
) : ViewModel(){
}