package com.example.basefragment.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize  //Parcelable
@Entity
data class AvatarModel(
    @PrimaryKey()
    var path : String,
    var pathAvatar : String,
    var arr : String) : Parcelable, Serializable {
}