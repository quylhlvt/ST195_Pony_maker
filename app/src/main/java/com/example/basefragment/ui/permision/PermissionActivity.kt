package com.example.basefragment.ui.permision

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.graphics.toColorInt
import com.example.basefragment.base.AbsBaseActivity
import com.example.basefragment.ui.main.MainActivity
import com.example.basefragment.utils.CONST
import com.example.basefragment.utils.SharedPreferenceUtils
import com.example.basefragment.utils.changeText
import com.example.basefragment.utils.checkPermision
import com.example.basefragment.utils.checkUsePermision
import com.example.basefragment.utils.onSingleClick
import com.example.basefragment.utils.requesPermission
import com.example.basefragment.utils.showToast
import com.example.basefragment.R
import com.example.basefragment.databinding.ActivityPermissionBinding
import com.example.basefragment.utils.CONST.PERMISON
import com.example.basefragment.utils.CONST.REQUEST_NOTIFICATION_PERMISSION
import com.example.basefragment.utils.CONST.REQUEST_STORAGE_PERMISSION
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PermissionActivity : AbsBaseActivity<ActivityPermissionBinding>() {
    var check = false

    @Inject
    lateinit var sharedPreferenceUtils: SharedPreferenceUtils

    override fun getLayoutId(): Int = R.layout.activity_permission
    override fun initView() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.rl4.visibility = View.VISIBLE
            binding.rl2.visibility = View.GONE
        } else {
            binding.rl4.visibility = View.GONE
            binding.rl2.visibility = View.VISIBLE
        }
        val space = " "
        binding.tvTitle.text = TextUtils.concat(
            changeText(
                this,
                getString(R.string.allow),
                "#1F2F4F".toColorInt(),
                R.font.itim_regular
            ),
            space,
            changeText(
                this,
                getString(R.string.app_name),
                "#FF4798".toColorInt(),
                R.font.itim_regular
            ),
            space,
            changeText(
                this,
                getString(R.string.request_permission_to_use_notifications_to_notify_you),
                "#1F2F4F".toColorInt(),
                R.font.itim_regular
            ),
        )
        checkPer()
    }

    override fun initAction() {

        binding.btnContinue.onSingleClick {
                    sharedPreferenceUtils.putBooleanValue(PERMISON, true)
                    val intent = Intent(this@PermissionActivity, MainActivity::class.java)
                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
                    finish()


        }
        binding.swiVibrate2.onSingleClick {
            if (!checkPermision(this)) {
                ActivityCompat.requestPermissions(
                    this,
                    checkUsePermision(),
                    REQUEST_STORAGE_PERMISSION
                )
            } else {
                showToast(application, R.string.permission_granted)
            }
        }
        binding.swiVibrate4.onSingleClick {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            } else {
                showToast(application, R.string.permission_granted)
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
                binding.swiVibrate2.setImageResource(R.drawable.ic_swith_true_per)
            }

            CONST.REQUEST_NOTIFICATION_PERMISSION -> {
                binding.swiVibrate4.setImageResource(R.drawable.ic_swith_true_per)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPer()
    }

    private fun checkPer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                binding.swiVibrate4.setImageResource(R.drawable.ic_swith_true_per)
            } else {
                binding.swiVibrate4.setImageResource(R.drawable.ic_swith_false_per)
            }
        } else {
            if (checkPermision(this)) {
                binding.swiVibrate2.setImageResource(R.drawable.ic_swith_true_per)
            } else {
                binding.swiVibrate2.setImageResource(R.drawable.ic_swith_false_per)
            }
        }
    }
}