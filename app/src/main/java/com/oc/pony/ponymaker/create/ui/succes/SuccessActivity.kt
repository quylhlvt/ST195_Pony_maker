package com.oc.pony.ponymaker.create.ui.succes

import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ActivitySuccessBinding
import com.oc.pony.ponymaker.create.utils.onClick
import com.oc.pony.ponymaker.create.utils.onSingleClick
import com.oc.pony.ponymaker.create.utils.requesPermission
import java.io.File

class SuccessActivity : com.oc.pony.ponymaker.create.base.AbsBaseActivity<ActivitySuccessBinding>() {
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
            imvShare.onClick {
                _root_ide_package_.com.oc.pony.ponymaker.create.utils.shareListFiles(
                    this@SuccessActivity,
                    arrayListOf(path)
                )
            }
            imvHome.onSingleClick {
                    startActivity(
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.newIntent(
                            applicationContext,
                            _root_ide_package_.com.oc.pony.ponymaker.create.ui.main.MainActivity::class.java
                        )
                    )
                    finish()

            }
            btnMyWork.onSingleClick {
                    startActivity(
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.newIntent(
                            applicationContext,
                            _root_ide_package_.com.oc.pony.ponymaker.create.ui.my_creation.MyCreationActivity::class.java
                        )
                    )
                    finish()
                }

            btnDownload.onClick {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    !_root_ide_package_.com.oc.pony.ponymaker.create.utils.checkPermision(application)
                ) {
                    ActivityCompat.requestPermissions(
                        this@SuccessActivity,
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.checkUsePermision(),
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.REQUEST_STORAGE_PERMISSION
                    )
                } else {
                    _root_ide_package_.com.oc.pony.ponymaker.create.utils.saveFileToExternalStorage(
                        applicationContext,
                        path,
                        ""
                    ) { check, path ->
                        if (check) {
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.download_successfully) + " " + _root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.NAME_SAVE_FILE,
                                Toast.LENGTH_SHORT
                            ).show()
                            _root_ide_package_.com.oc.pony.ponymaker.create.utils.scanMediaFile(
                                this@SuccessActivity,
                                File(path)
                            )
                        } else {
                            _root_ide_package_.com.oc.pony.ponymaker.create.utils.showToast(
                                this@SuccessActivity,
                                R.string.download_failed
                            )
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
            _root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.REQUEST_STORAGE_PERMISSION -> {
                _root_ide_package_.com.oc.pony.ponymaker.create.utils.saveFileToExternalStorage(
                    applicationContext,
                    path,
                    ""
                ) { check, path ->
                    if (check) {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.download_successfully) + " " + _root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.NAME_SAVE_FILE,
                            Toast.LENGTH_SHORT
                        ).show()
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.scanMediaFile(
                            this@SuccessActivity,
                            File(path)
                        )
                    } else {
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.showToast(
                            this@SuccessActivity,
                            R.string.download_failed
                        )
                    }
                }
            }
        }
    }
}