package com.example.basefragment.ui.my_creation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.base.AbsBaseActivity
import com.example.basefragment.dialog.DialogExit
import com.example.basefragment.ui.customview.CustomviewActivity
import com.example.basefragment.ui.customview.CustomviewViewModel
import com.example.basefragment.ui.main.MainActivity
import com.example.basefragment.ui.view.ViewActivity
import com.example.basefragment.utils.CONST
import com.example.basefragment.utils.SharedPreferenceUtils
import com.example.basefragment.utils.checkPermision
import com.example.basefragment.utils.checkUsePermision
import com.example.basefragment.utils.hide
import com.example.basefragment.utils.newIntent
import com.example.basefragment.utils.onSingleClick
import com.example.basefragment.utils.requesPermission
import com.example.basefragment.utils.saveFileToExternalStorage
import com.example.basefragment.utils.scanMediaFile
import com.example.basefragment.utils.shareListFiles
import com.example.basefragment.utils.show
import com.example.basefragment.utils.showToast
import com.example.basefragment.utils.toList
import com.example.basefragment.R
import com.example.basefragment.databinding.ActivityMyCreationBinding
import com.example.basefragment.utils.CONST.NAME_SAVE_FILE
import com.example.basefragment.utils.CONST.REQUEST_STORAGE_PERMISSION
import com.example.basefragment.utils.DataHelper.arrBlackCentered
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MyCreationActivity : AbsBaseActivity<ActivityMyCreationBinding>() {
    val viewModel: CustomviewViewModel by viewModels()
    var checkAvatar = true

    @Inject
    lateinit var sharedPreference: SharedPreferenceUtils
    var arrPathAvatar = arrayListOf<String>()
    var arrPathDesign = arrayListOf<String>()
    val adapterAvatar by lazy {
        AvatarAdapter().apply {
            onClick = { pos, type ->
                when (type) {
                    "item" -> {
                            startActivity(
                                newIntent(
                                    applicationContext,
                                    ViewActivity::class.java
                                ).putExtra("data", arrPathAvatar[pos]).putExtra("type", "avatar")
                            )
                        }


                    "delete" -> {
                        var dialog = DialogExit(this@MyCreationActivity, "delete")
                        dialog.onClick = {
                            viewModel.deleteAvatar(arrPathAvatar[pos])
                            File(arrPathAvatar[pos]).delete()
                            arrPathAvatar.removeAt(pos)
                            submitList(arrPathAvatar)
                            showToast(applicationContext, R.string.file_deleted_successfully)
                            checkNull()
                        }
                        dialog.show()
                    }

                    "edit" -> {
                        viewModel.getAvatar(arrPathAvatar[pos]) { avatar ->
                            if (avatar != null) {
                                var a =
                                    arrBlackCentered.indexOfFirst { it.avt == avatar.pathAvatar }
                                if (a > -1) {

                                        var a = avatar.pathAvatar.split("/")
                                        var b = a[a.size - 2]

                                        startActivity(
                                            Intent(
                                                applicationContext,
                                                CustomviewActivity::class.java
                                            ).putExtra(
                                                "data",
                                                arrBlackCentered.indexOfFirst { it.avt == avatar.pathAvatar })
                                                .putExtra(
                                                    "arr",
                                                    toList(avatar.arr)
                                                ).putExtra("checkEdit", true)
                                                .putExtra("fileName", File(avatar.path).name)
                                        )

                                } else {
                                    showToast(
                                        applicationContext,
                                        R.string.please_check_your_network_connection
                                    )
                                }

                            } else {
                                File(arrPathAvatar[pos]).delete()
                                showToast(applicationContext, R.string.image_error_please_try_again)
                                finish()
                            }
                        }
                    }

                    "longclick" -> {
                        checkLongClick = true
                        if(arrCheckTick.indexOf(pos)>-1){
                            arrCheckTick.remove(pos)
                        }else{
                            arrCheckTick.add(pos)
                        }
                        submitList(arrPathAvatar)
                        this@MyCreationActivity.binding.apply {
                            imvTickAll.show()
                            imvDelete.show()
                            llBottom.show()
                            if (arrCheckTick.size == arrPathAvatar.size) {
                                this@MyCreationActivity.binding.imvTickAll.setImageResource(R.drawable.imv_tick_all_true)
                            }else{
                                this@MyCreationActivity.binding.imvTickAll.setImageResource(R.drawable.imv_tick_all_false)
                            }
                        }
                    }

                    "tick" -> {
                        if (pos in arrCheckTick) {
                            arrCheckTick.remove(pos)
                            this@MyCreationActivity.binding.imvTickAll.setImageResource(R.drawable.imv_tick_all_false)
                        } else {
                            arrCheckTick.add(pos)
                            if (arrCheckTick.size == arrPathAvatar.size) {
                                this@MyCreationActivity.binding.imvTickAll.setImageResource(R.drawable.imv_tick_all_true)
                            }
                        }
                        submitList(arrPathAvatar)
                    }
                }
            }
        }
    }
    val adapterDesign by lazy {
        DesignAdapter().apply {
            onClick = { pos, type ->
                when (type) {
                    "item" -> {
                            startActivity(
                                newIntent(
                                    applicationContext,
                                    ViewActivity::class.java
                                ).putExtra("data", arrPathDesign[pos])
                            )

                    }

                    "delete" -> {
                        var dialog = DialogExit(this@MyCreationActivity, "delete")
                        dialog.onClick = {
                            File(arrPathDesign[pos]).delete()
                            arrPathDesign.removeAt(pos)
                            submitList(arrPathDesign)
                            showToast(applicationContext, R.string.file_deleted_successfully)
                            checkNull()
                        }
                        dialog.show()
                    }

                    "longclick" -> {
                        checkLongClick = true
                        if(arrCheckTick.indexOf(pos)>-1){
                            arrCheckTick.remove(pos)
                        }else{
                            arrCheckTick.add(pos)
                        }
                        submitList(arrPathDesign)
                        this@MyCreationActivity.binding.apply {
                            imvTickAll.show()
                            imvDelete.show()
                            llBottom.show()
                            if (arrCheckTick.size == arrPathDesign.size) {
                                this@MyCreationActivity.binding.imvTickAll.setImageResource(R.drawable.imv_tick_all_true)
                            }else{
                                this@MyCreationActivity.binding.imvTickAll.setImageResource(R.drawable.imv_tick_all_false)
                            }
                        }
                    }

                    "tick" -> {
                        if (pos in arrCheckTick) {
                            arrCheckTick.remove(pos)
                            this@MyCreationActivity.binding.imvTickAll.setImageResource(R.drawable.imv_tick_all_false)
                        } else {
                            arrCheckTick.add(pos)
                            if (arrCheckTick.size == arrPathDesign.size) {
                                this@MyCreationActivity.binding.imvTickAll.setImageResource(R.drawable.imv_tick_all_true)
                            }
                        }
                        submitList(arrPathDesign)
                    }
                }
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_my_creation

    override fun initView() {

        binding.apply {
            tvTitle.isSelected = true
            tvShare.isSelected = true
            tvDownload.isSelected = true

            rcvAvatar.itemAnimator = null
            rcvAvatar.adapter = adapterAvatar

            rcvDesign.itemAnimator = null
            rcvDesign.adapter = adapterDesign
            getData()
            adapterAvatar.submitList(arrPathAvatar)
            adapterDesign.submitList(arrPathDesign)
            checkNull()
        }
    }

    override fun onRestart() {
        super.onRestart()
        arrPathAvatar.clear()
        arrPathDesign.clear()
        adapterDesign.submitList(arrPathDesign)
        adapterAvatar.submitList(arrPathAvatar)
        getData()
        hideLongClick()
    }

    var checkLongClick = false
    fun hideLongClick() {
        checkLongClick = false
        binding.imvTickAll.setImageResource(R.drawable.imv_tick_all_false)
        binding.imvTickAll.visibility = View.GONE
        binding.llBottom.visibility = View.GONE
        binding.imvDelete.visibility = View.GONE
        adapterAvatar.checkLongClick = false
        adapterDesign.checkLongClick = false
        adapterDesign.arrCheckTick.clear()
        adapterAvatar.arrCheckTick.clear()
        adapterAvatar.submitList(arrPathAvatar)
        adapterDesign.submitList(arrPathDesign)
        checkNull()
    }

    fun checkNull() {
        if (checkAvatar) {
            if (arrPathAvatar.isEmpty()) {
                binding.llNull.show()
            } else {
                binding.llNull.hide()
            }
        } else {
            if (arrPathDesign.isEmpty()) {
                binding.llNull.show()
            } else {
                binding.llNull.hide()
            }
        }
    }

    fun getData() {
        arrPathDesign.clear()
        arrPathAvatar.clear()
        if (File(filesDir, "design").exists()) {
            File(filesDir, "design").listFiles()?.sortedByDescending { it.name }?.forEach {
                    arrPathDesign.add(it.path)
                }
//            arrPathDesign
        }
        if (File(filesDir, "avatar").exists()) {
            File(filesDir, "avatar").listFiles()?.sortedByDescending { it.name }?.forEach {
                    arrPathAvatar.add(it.path)
                }
//            arrPathAvatar
        }
    }

    override fun onBackPressed() {
        startActivity(newIntent(applicationContext, MainActivity::class.java))
    }

    override fun initAction() {
        binding.apply {
            root.onSingleClick { hideLongClick() }
            rcvAvatar.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(
                    recyclerView: RecyclerView, motionEvent: MotionEvent
                ): Boolean {
                    return when {
                        motionEvent.action != MotionEvent.ACTION_UP || recyclerView.findChildViewUnder(
                            motionEvent.x, motionEvent.y
                        ) != null -> false

                        else -> {
                            hideLongClick()
                            true
                        }
                    }
                }

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
                override fun onTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) {}
            })
            rcvDesign.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(
                    recyclerView: RecyclerView, motionEvent: MotionEvent
                ): Boolean {
                    return when {
                        motionEvent.action != MotionEvent.ACTION_UP || recyclerView.findChildViewUnder(
                            motionEvent.x, motionEvent.y
                        ) != null -> false

                        else -> {
                            hideLongClick()
                            true
                        }
                    }
                }

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
                override fun onTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) {}
            })
            imvBack.onSingleClick {
                startActivity(
                    newIntent(
                        applicationContext,
                        MainActivity::class.java
                    )
                )
            }
            btnDownload.onSingleClick {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    !checkPermision(application)
                ) {
                    ActivityCompat.requestPermissions(
                        this@MyCreationActivity,
                        checkUsePermision(),
                        REQUEST_STORAGE_PERMISSION
                    )
                } else {
                    if (checkAvatar) {
                        if (adapterAvatar.arrCheckTick.isEmpty()) {
                            showToast(
                                applicationContext,
                                R.string.you_have_not_selected_anything_yet
                            )
                        } else {
                            adapterAvatar.arrCheckTick.forEach {
                                saveFileToExternalStorage(
                                    applicationContext,
                                    arrPathAvatar[it],
                                    ""
                                ) { check, path ->
                                    if (check) {
                                        scanMediaFile(applicationContext, File(path))
                                    }
                                }
                            }
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.download_successfully) + " " + NAME_SAVE_FILE,
                                Toast.LENGTH_SHORT
                            ).show()
                            hideLongClick()
                        }
                    } else {
                        if (adapterDesign.arrCheckTick.isEmpty()) {
                            showToast(
                                applicationContext,
                                R.string.you_have_not_selected_anything_yet
                            )
                        } else {
                            adapterDesign.arrCheckTick.forEach {
                                saveFileToExternalStorage(
                                    applicationContext,
                                    arrPathDesign[it],
                                    ""
                                ) { check, path ->
                                    if (check) {
                                        scanMediaFile(applicationContext, File(path))
                                    }
                                }
                            }
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.download_successfully) + " " + NAME_SAVE_FILE,
                                Toast.LENGTH_SHORT
                            ).show()
                            hideLongClick()
                        }
                    }
                }
            }
            btnShareAll.onSingleClick {
                if (checkAvatar) {
                    if (adapterAvatar.arrCheckTick.isEmpty()) {
                        showToast(applicationContext, R.string.you_have_not_selected_anything_yet)
                    } else {
                        var listPath = arrayListOf<String>()
                        adapterAvatar.arrCheckTick.forEach {
                            listPath.add(arrPathAvatar[it])
                        }
                        shareListFiles(this@MyCreationActivity, listPath)
                        hideLongClick()
                    }
                } else {
                    if (adapterDesign.arrCheckTick.isEmpty()) {
                        showToast(applicationContext, R.string.you_have_not_selected_anything_yet)
                    } else {
                        var listPath = arrayListOf<String>()
                        adapterDesign.arrCheckTick.forEach {
                            listPath.add(arrPathDesign[it])
                        }
                        shareListFiles(this@MyCreationActivity, listPath)
                        hideLongClick()
                    }
                }
            }
            imvTickAll.onSingleClick {
                if (checkAvatar) {
                    if (arrPathAvatar.size == adapterAvatar.arrCheckTick.size) {
                        binding.imvTickAll.setImageResource(R.drawable.imv_tick_all_false)
                        adapterAvatar.arrCheckTick.clear()
                        adapterAvatar.submitList(arrPathAvatar)
                    } else {
                        binding.imvTickAll.setImageResource(R.drawable.imv_tick_all_true)
                        adapterAvatar.arrCheckTick.clear()
                        arrPathAvatar.forEachIndexed { pos, _ ->
                            adapterAvatar.arrCheckTick.add(pos)
                        }
                        adapterAvatar.submitList(arrPathAvatar)
                    }
                } else {
                    if (arrPathDesign.size == adapterDesign.arrCheckTick.size) {
                        binding.imvTickAll.setImageResource(R.drawable.imv_tick_all_false)
                        adapterDesign.arrCheckTick.clear()
                        adapterDesign.submitList(arrPathDesign)
                    } else {
                        binding.imvTickAll.setImageResource(R.drawable.imv_tick_all_true)
                        adapterDesign.arrCheckTick.clear()
                        arrPathDesign.forEachIndexed { pos, _ ->
                            adapterDesign.arrCheckTick.add(pos)
                        }
                        adapterDesign.submitList(arrPathDesign)
                    }
                }
            }
            imvDelete.onSingleClick {
                if (checkAvatar) {
                    if (adapterAvatar.arrCheckTick.isEmpty()) {
                        showToast(
                            applicationContext,
                            R.string.you_have_not_selected_anything_yet
                        )
                    } else {
                        var dialog = DialogExit(this@MyCreationActivity, "delete")
                        dialog.onClick = {
                            adapterAvatar.arrCheckTick.forEach { pos ->
                                viewModel.deleteAvatar(arrPathAvatar[pos])
                                File(arrPathAvatar[pos]).delete()
                            }
                            getData()
//                            arrPathAvatar.remove()
                            hideLongClick()
                            showToast(applicationContext, R.string.file_deleted_successfully)
                        }
                        dialog.show()
                    }
                } else {
                    if (adapterDesign.arrCheckTick.isEmpty()) {
                        showToast(
                            applicationContext,
                            R.string.you_have_not_selected_anything_yet
                        )
                    } else {
                        var dialog = DialogExit(this@MyCreationActivity, "delete")
                        dialog.onClick = {
                            adapterDesign.arrCheckTick.forEach { pos ->
                                viewModel.deleteAvatar(arrPathDesign[pos])
                                File(arrPathDesign[pos]).delete()
                            }
                            getData()
                            hideLongClick()
                            showToast(applicationContext, R.string.file_deleted_successfully)
                        }
                        dialog.show()
                    }
                }
            }

            btnAvatar.onSingleClick {
                if (!checkAvatar) {
                    checkAvatar = true
                    btnAvatar.setBackgroundResource(R.drawable.bg_btn_my_work)
                    btnAvatar.setTextColor(R.color.white)
                    btnDesign.setTextColor(R.color.app_color)
                    btnDesign.setBackgroundResource(R.drawable.bg_btn_my_work_false)
                    rcvAvatar.show()
                    rcvDesign.hide()
                    hideLongClick()
                }
            }
            btnDesign.onSingleClick {
                if (checkAvatar) {
                    checkAvatar = false
                    btnAvatar.setBackgroundResource(R.drawable.bg_btn_my_work_false)
                    btnDesign.setBackgroundResource(R.drawable.bg_btn_my_work)
                    btnDesign.setTextColor(R.color.white)
                    btnAvatar.setTextColor(R.color.white)
                    rcvDesign.show()
                    rcvAvatar.hide()
                    hideLongClick()
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

            }
        }
    }
}