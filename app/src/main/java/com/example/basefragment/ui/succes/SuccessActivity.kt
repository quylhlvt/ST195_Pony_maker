package com.example.basefragment.ui.succes

import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.basefragment.base.AbsBaseActivity
import com.example.basefragment.ui.main.MainActivity
import com.example.basefragment.ui.my_creation.MyCreationActivity
import com.example.basefragment.utils.CONST
import com.example.basefragment.utils.checkPermision
import com.example.basefragment.utils.checkUsePermision
import com.example.basefragment.utils.newIntent
import com.example.basefragment.utils.onSingleClick
import com.example.basefragment.utils.requesPermission
import com.example.basefragment.utils.saveFileToExternalStorage
import com.example.basefragment.utils.scanMediaFile
import com.example.basefragment.utils.shareListFiles
import com.example.basefragment.utils.showToast
import com.bumptech.glide.Glide
import com.example.basefragment.R
import com.example.basefragment.databinding.ActivitySuccessBinding
import com.example.basefragment.utils.CONST.NAME_SAVE_FILE
import com.example.basefragment.utils.CONST.REQUEST_STORAGE_PERMISSION
import java.io.File

class SuccessActivity : AbsBaseActivity<ActivitySuccessBinding>() {
    var path = ""
    override fun getLayoutId(): Int = R.layout.activity_success

    override fun initView() {
        path = intent.getStringExtra("path").toString()
        Glide.with(applicationContext).load(path).into(binding.imv)
        binding.apply {
            tvDownload.isSelected = true
            tvMyWork.isSelected = true
            tvTitle.isSelected = true
        }
      }

    override fun initAction() {
        binding.apply {
            imvBack.onSingleClick { finish() }
            imvShare.onSingleClick { shareListFiles(this@SuccessActivity, arrayListOf(path)) }
            imvHome.onSingleClick {
                    startActivity(newIntent(applicationContext, MainActivity::class.java))
                    finish()

            }
            btnMyWork.onSingleClick {
                    startActivity(newIntent(applicationContext, MyCreationActivity::class.java))
                    finish()

            }
            btnDownload.onSingleClick {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    !checkPermision(application)
                ) {
                    ActivityCompat.requestPermissions(
                        this@SuccessActivity,
                        checkUsePermision(),
                        REQUEST_STORAGE_PERMISSION
                    )
                } else {
                    saveFileToExternalStorage(applicationContext, path, "") { check, path ->
                        if (check) {
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.download_successfully) + " " + NAME_SAVE_FILE,
                                Toast.LENGTH_SHORT
                            ).show()
                            scanMediaFile(this@SuccessActivity, File(path))
                        } else {
                            showToast(this@SuccessActivity, R.string.download_failed)
                        }
                    }
                }

            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
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
                        scanMediaFile(this@SuccessActivity, File(path))
                    } else {
                        showToast(this@SuccessActivity, R.string.download_failed)
                    }
                }
            }
        }
    }
}