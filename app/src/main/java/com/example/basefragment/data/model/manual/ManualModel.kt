package com.example.basefragment.data.model.manual

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ManualModel(var bomb: Boolean=false ) : Parcelable
