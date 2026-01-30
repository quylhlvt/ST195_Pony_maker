package com.example.basefragment.core.extention

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.basefragment.R
import com.example.basefragment.core.helper.LanguageHelper.setLocale
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.basefragment.utils.key.RequestKey


fun Context.checkPermissions(listPermission: Array<String>): Boolean {
    return listPermission.all {
        ContextCompat.checkSelfPermission( this,it) == PackageManager.PERMISSION_GRANTED
    }
}

fun Activity.requestPermission(permissions: Array<String>, requestCode: Int) {
    ActivityCompat.requestPermissions(this, permissions, requestCode)
}
fun Fragment.requestPermission(permissions: Array<String>, requestCode: Int) {
    requestPermissions(permissions, requestCode)
}
fun Fragment.openImagePicker() {
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"
    startActivityForResult(intent, RequestKey.PICK_IMAGE_REQUEST_CODE)
}
