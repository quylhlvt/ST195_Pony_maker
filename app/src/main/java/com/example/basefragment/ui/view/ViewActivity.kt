package com.example.basefragment.ui.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.example.basefragment.base.AbsBaseActivity
import com.example.basefragment.dialog.DialogExit
import com.example.basefragment.ui.customview.CustomviewActivity
import com.example.basefragment.ui.customview.CustomviewViewModel
import com.example.basefragment.utils.CONST
import com.example.basefragment.utils.DataHelper
import com.example.basefragment.utils.checkPermision
import com.example.basefragment.utils.checkUsePermision
import com.example.basefragment.utils.hide
import com.example.basefragment.utils.onSingleClick
import com.example.basefragment.utils.requesPermission
import com.example.basefragment.utils.saveFileToExternalStorage
import com.example.basefragment.utils.scanMediaFile
import com.example.basefragment.utils.shareListFiles
import com.example.basefragment.utils.show
import com.example.basefragment.utils.showToast
import com.example.basefragment.utils.toList
import com.bumptech.glide.Glide
import com.example.basefragment.R
import com.example.basefragment.databinding.ActivityViewBinding
import com.example.basefragment.utils.CONST.NAME_SAVE_FILE
import com.example.basefragment.utils.CONST.REQUEST_STORAGE_PERMISSION
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlin.getValue

@AndroidEntryPoint
class ViewActivity : AbsBaseActivity<ActivityViewBinding>() {
    val viewModel: CustomviewViewModel by viewModels()
    var path = ""
    override fun getLayoutId(): Int = R.layout.activity_view

    override fun initView() {
        path = intent.getStringExtra("data").toString()
        if (intent?.getStringExtra("type") == "avatar") {
            binding.imvEdit.show()
        } else {
            binding.imvEdit.hide()
        }
        Glide.with(applicationContext).load(path).into(binding.imv)

    }

    override fun initAction() {
        binding.apply {
            tvShare.isSelected = true
            tvDownload.isSelected = true
            imvBack.onSingleClick { finish() }
            binding.imvEdit.onSingleClick {
                viewModel.getAvatar(path) { avatar ->
                    if (avatar != null) {
                        var a =
                            DataHelper.arrBlackCentered.indexOfFirst { it.avt == avatar.pathAvatar }
                        if (a > -1) {
                            var a = avatar.pathAvatar.split("/")
                            var b = a[a.size - 2]

                            startActivity(
                                Intent(
                                    applicationContext, CustomviewActivity::class.java
                                ).putExtra(
                                    "data",
                                    DataHelper.arrBlackCentered.indexOfFirst { it.avt == avatar.pathAvatar })
                                    .putExtra(
                                        "arr", toList(avatar.arr)
                                    ).putExtra("checkEdit", true)
                                    .putExtra("fileName", File(avatar.path).name)
                            )

                    } else {
                        showToast(
                            applicationContext, R.string.please_check_your_network_connection)
                        }}

                else {
                File(path).delete()
                showToast(applicationContext, R.string.image_error_please_try_again)
                finish()
            }
            }
        }
        imvDelete.onSingleClick {
            var dialog = DialogExit(this@ViewActivity, "delete")
            dialog.onClick = {
                File(path).delete()
                finish()
            }
            dialog.show()
        }
        btnDownload.onSingleClick {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !checkPermision(application)) {
                ActivityCompat.requestPermissions(
                    this@ViewActivity, checkUsePermision(), REQUEST_STORAGE_PERMISSION
                )
            } else {
                saveFileToExternalStorage(applicationContext, path, "") { check, path ->
                    if (check) {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.download_successfully) + " " + NAME_SAVE_FILE,
                            Toast.LENGTH_SHORT
                        ).show()
                        scanMediaFile(this@ViewActivity, File(path))
                    } else {
                        showToast(this@ViewActivity, R.string.download_failed)
                    }
                }

            }
        }
        btnShareAll.onSingleClick {
            shareListFiles(this@ViewActivity, arrayListOf(path))
        }

    }
}

override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<String>, grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requesPermission(requestCode)) {
        CONST.REQUEST_STORAGE_PERMISSION -> {
            saveFileToExternalStorage(applicationContext, path, "") { check, path ->
                if (check) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.download_successfully) + " " + NAME_SAVE_FILE,
                        Toast.LENGTH_SHORT
                    ).show()
                    scanMediaFile(this@ViewActivity, File(path))
                } else {
                    showToast(this@ViewActivity, R.string.download_failed)
                }
            }
        }
    }
}
}