package com.oc.pony.ponymaker.create.ui.permision

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.graphics.toColorInt
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ActivityPermissionBinding
import com.oc.pony.ponymaker.create.ui.main.MainActivity
import com.oc.pony.ponymaker.create.utils.CONST.REQUEST_NOTIFICATION_PERMISSION
import com.oc.pony.ponymaker.create.utils.CONST.REQUEST_STORAGE_PERMISSION
import com.oc.pony.ponymaker.create.utils.onSingleClick
import com.oc.pony.ponymaker.create.utils.showDialogNotifiListener
import com.oc.pony.ponymaker.create.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PermissionActivity : com.oc.pony.ponymaker.create.base.AbsBaseActivity<ActivityPermissionBinding>() {

    private val viewModel: PermissionViewModel by viewModels()

    @Inject
    lateinit var sharedPreferenceUtils: com.oc.pony.ponymaker.create.utils.SharedPreferenceUtils

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
            com.oc.pony.ponymaker.create.utils.changeText(this, getString(R.string.allow), "#1F2F4F".toColorInt(), R.font.itim_regular),
            space,
            com.oc.pony.ponymaker.create.utils.changeText(this, getString(R.string.app_name), "#FF4798".toColorInt(), R.font.itim_regular),
            space,
            com.oc.pony.ponymaker.create.utils.changeText(this, getString(R.string.request_permission_to_use_notifications_to_notify_you), "#1F2F4F".toColorInt(), R.font.itim_regular)
        )

        checkPer()
    }

    override fun initAction() {
        binding.btnContinue.onSingleClick {
            val intent = Intent(this@PermissionActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        binding.swiVibrate2.onSingleClick {
            handlePermissionRequest(isStorage = true)
        }

        binding.swiVibrate4.onSingleClick {
            handlePermissionRequest(isStorage = false)
        }
    }

    private fun handlePermissionRequest(isStorage: Boolean) {
        val permissions = if (isStorage) {
            viewModel.getStoragePermissions()
        } else {
            viewModel.getNotificationPermissions()
        }

        if (checkPermissions(permissions)) {
            showToast(this, R.string.permission_granted)
            return
        }

        // Kiểm tra nếu đã từ chối nhiều lần và không còn show rationale → gợi ý vào Settings
        if (viewModel.needGoToSettings(sharedPreferenceUtils, isStorage)) {
            val dialogRes = if (isStorage) R.string.reques_storage else R.string.content_dialog_notification
            showDialogNotifiListener(dialogRes)
            return
        }

        // Request permission bình thường
        val requestCode = if (isStorage) REQUEST_STORAGE_PERMISSION else REQUEST_NOTIFICATION_PERMISSION
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    private fun checkPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val isGranted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }

        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                viewModel.updateStorageGranted(sharedPreferenceUtils, isGranted)

                if (isGranted) {
                    binding.swiVibrate2.setImageResource(R.drawable.ic_swith_true_per)
                    showToast(this, R.string.permission_granted)
                }
                // Không cần xử lý riêng "Don't ask again" ở đây vì đã kiểm tra trong handlePermissionRequest
            }

            REQUEST_NOTIFICATION_PERMISSION -> {
                viewModel.updateNotificationGranted(sharedPreferenceUtils, isGranted)

                if (isGranted) {
                    binding.swiVibrate4.setImageResource(R.drawable.ic_swith_true_per)
                    showToast(this, R.string.permission_granted)
                }
            }
        }

        checkPer() // Cập nhật lại trạng thái switch
    }

    override fun onResume() {
        super.onResume()
        checkPer()
    }

    private fun checkPer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notiGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            binding.swiVibrate4.setImageResource(if (notiGranted) R.drawable.ic_swith_true_per else R.drawable.ic_swith_false_per)
        } else {
            // Giả sử checkPermision() kiểm tra đúng quyền storage cũ (WRITE_EXTERNAL_STORAGE hoặc READ_EXTERNAL_STORAGE)
            val storageGranted = com.oc.pony.ponymaker.create.utils.checkPermision(this)
            binding.swiVibrate2.setImageResource(if (storageGranted) R.drawable.ic_swith_true_per else R.drawable.ic_swith_false_per)
        }
    }
}