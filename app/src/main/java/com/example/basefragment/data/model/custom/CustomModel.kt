package com.example.basefragment.data.model.custom

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class CustomModel(
    var avatar: String,
    var listPath: ArrayList<BodyPartModel>,
    var checkDataOnline: Boolean = false,

): Parcelable
