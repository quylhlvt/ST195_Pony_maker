package com.oc.pony.ponymaker.create.ui.view

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ActivityViewBinding
import com.oc.pony.ponymaker.create.utils.hide
import com.oc.pony.ponymaker.create.utils.onSingleClick
import com.oc.pony.ponymaker.create.utils.requesPermission
import com.oc.pony.ponymaker.create.utils.show
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class ViewActivity : com.oc.pony.ponymaker.create.base.AbsBaseActivity<ActivityViewBinding>() {
    val viewModel: com.oc.pony.ponymaker.create.ui.customview.CustomviewViewModel by viewModels()
    var path = ""
    override fun getLayoutId(): Int = R.layout.activity_view

    override fun initView() {
        path = intent.getStringExtra("data").toString()
        if (intent?.getStringExtra("type") == "avatar") {
            binding.imvShare.show()
            binding.tvEditShare.text= getString(R.string.edit)
        } else {
            binding.imvShare.hide()
            binding.tvEditShare.text= getString(R.string.share)

        }
        Glide.with(applicationContext).load(path).into(binding.imv)

    }

    override fun initAction() {
        binding.apply {
            tvEditShare.isSelected = true
            tvDownload.isSelected = true
            imvBack.onSingleClick { finish() }
            imvShare.onSingleClick {
                _root_ide_package_.com.oc.pony.ponymaker.create.utils.shareListFiles(
                    this@ViewActivity,
                    arrayListOf(path)
                )
            }
            imvDelete.onSingleClick {
                var dialog = _root_ide_package_.com.oc.pony.ponymaker.create.dialog.DialogExit(
                    this@ViewActivity,
                    "delete"
                )
                dialog.onClick = {
                    File(path).delete()
                    finish()
                }
                dialog.show()
            }
            btnDownload.onSingleClick {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    !_root_ide_package_.com.oc.pony.ponymaker.create.utils.checkPermision(application)
                ) {
                    ActivityCompat.requestPermissions(
                        this@ViewActivity,
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.checkUsePermision(),
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.REQUEST_STORAGE_PERMISSION
                    )
                }else{
                    _root_ide_package_.com.oc.pony.ponymaker.create.utils.saveFileToExternalStorage(
                        applicationContext,
                        path,
                        "",
                    ) { check, path ->
                        if (check) {
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.download_successfully) + " " + _root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.NAME_SAVE_FILE,
                                Toast.LENGTH_SHORT
                            ).show()
                            _root_ide_package_.com.oc.pony.ponymaker.create.utils.scanMediaFile(
                                this@ViewActivity,
                                File(path)
                            )
                        } else {
                            _root_ide_package_.com.oc.pony.ponymaker.create.utils.showToast(
                                this@ViewActivity,
                                R.string.download_failed
                            )
                        }
                    }

                }
            }
            btnEditShareAll.onSingleClick {
                if (intent?.getStringExtra("type") == "avatar"){
                viewModel.getAvatar(path) { avatar ->
                    if (avatar != null) {
                        var a =
                            _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered.indexOfFirst { it.avt == avatar.pathAvatar }
                        if (a > -1) {
                            var a = avatar.pathAvatar.split("/")
                            var b = a[a.size - 2]

                            startActivity(
                                Intent(
                                    applicationContext,
                                    _root_ide_package_.com.oc.pony.ponymaker.create.ui.customview.CustomviewActivity::class.java
                                ).putExtra(
                                    "data",
                                    _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered.indexOfFirst { it.avt == avatar.pathAvatar })
                                    .putExtra(
                                        "arr",
                                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.toList(avatar.arr)
                                    ).putExtra("checkEdit", true)
                                    .putExtra("fileName", File(avatar.path).name)
                            )

                        } else {
                            _root_ide_package_.com.oc.pony.ponymaker.create.utils.showToast(
                                applicationContext,
                                R.string.please_check_your_network_connection
                            )
                        }

                    } else {
                        File(path).delete()
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.showToast(
                            applicationContext,
                            R.string.image_error_please_try_again
                        )
                        finish()
                    }
                }
            }else{
                    _root_ide_package_.com.oc.pony.ponymaker.create.utils.shareListFiles(
                        this@ViewActivity,
                        arrayListOf(path)
                    )
            }}

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
                    "",
                ) { check, path ->
                    if (check) {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.download_successfully) + " " + _root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.NAME_SAVE_FILE,
                            Toast.LENGTH_SHORT
                        ).show()
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.scanMediaFile(
                            this@ViewActivity,
                            File(path)
                        )
                    } else {
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.showToast(
                            this@ViewActivity,
                            R.string.download_failed
                        )
                    }
                }
            }
        }
    }
}