package com.example.basefragment.ui.main.playmutiplay

import androidx.lifecycle.ViewModel
import com.example.basefragment.core.helper.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class PlayMultiplayeViewModel  @Inject constructor( private val sharedPreferences: SharedPreferencesManager
) : ViewModel(){
}