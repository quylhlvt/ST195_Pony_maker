package com.oc.pony.ponymaker.create.ui.my_creation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ActivityMyCreationBinding
import com.oc.pony.ponymaker.create.dialog.CreateNameDialog
import com.oc.pony.ponymaker.create.dialog.DialogExit
import com.oc.pony.ponymaker.create.ui.customview.CustomviewActivity
import com.oc.pony.ponymaker.create.ui.main.MainActivity
import com.oc.pony.ponymaker.create.ui.view.ViewActivity
import com.oc.pony.ponymaker.create.utils.CONST.NAME_SAVE_FILE
import com.oc.pony.ponymaker.create.utils.CONST.REQUEST_STORAGE_PERMISSION
import com.oc.pony.ponymaker.create.utils.DataHelper
import com.oc.pony.ponymaker.create.utils.SharedPreferenceUtils
import com.oc.pony.ponymaker.create.utils.checkPermision
import com.oc.pony.ponymaker.create.utils.checkUsePermision
import com.oc.pony.ponymaker.create.utils.hide
import com.oc.pony.ponymaker.create.utils.newIntent
import com.oc.pony.ponymaker.create.utils.onClick
import com.oc.pony.ponymaker.create.utils.requesPermission
import com.oc.pony.ponymaker.create.utils.saveFileToExternalStorage
import com.oc.pony.ponymaker.create.utils.scanMediaFile
import com.oc.pony.ponymaker.create.utils.share.telegram.TelegramSharing.importToTelegram
import com.oc.pony.ponymaker.create.utils.share.whatsapp.IdGenerator.generateIdFromUrl
import com.oc.pony.ponymaker.create.utils.share.whatsapp.StickerBook.addPackIfNotAlreadyAdded
import com.oc.pony.ponymaker.create.utils.share.whatsapp.StickerPack
import com.oc.pony.ponymaker.create.utils.share.whatsapp.WhatsappSharingActivity
import com.oc.pony.ponymaker.create.utils.shareListFiles
import com.oc.pony.ponymaker.create.utils.show
import com.oc.pony.ponymaker.create.utils.showToast
import com.oc.pony.ponymaker.create.utils.toList

import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class MyCreationActivity : WhatsappSharingActivity<ActivityMyCreationBinding>() {
    val viewModel: com.oc.pony.ponymaker.create.ui.customview.CustomviewViewModel by viewModels()
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
                                this@MyCreationActivity,
                                ViewActivity::class.java
                            ).putExtra("data", arrPathAvatar[pos]).putExtra("type", "avatar")
                        )
                    }


                    "delete" -> {
                        var dialog = DialogExit(
                            this@MyCreationActivity,
                            "delete"
                        )
                        dialog.onClick = {
                            viewModel.deleteAvatar(arrPathAvatar[pos])
                            File(arrPathAvatar[pos]).delete()
                            arrPathAvatar.removeAt(pos)
                            submitList(arrPathAvatar)
                            showToast(
                                this@MyCreationActivity,
                                R.string.file_deleted_successfully
                            )
                            checkNull()
                        }
                        dialog.show()
                    }

                    "edit" -> {
                        viewModel.getAvatar(arrPathAvatar[pos]) { avatar ->
                            if (avatar != null) {
                                var a =
                                    DataHelper.arrBlackCentered.indexOfFirst { it.avt == avatar.pathAvatar }
                                if (a > -1) {

                                    var a = avatar.pathAvatar.split("/")
                                    var b = a[a.size - 2]

                                    startActivity(
                                        Intent(
                                            this@MyCreationActivity,
                                            CustomviewActivity::class.java
                                        ).putExtra(
                                            "data",
                                            DataHelper.arrBlackCentered.indexOfFirst { it.avt == avatar.pathAvatar })
                                            .putExtra(
                                                "arr",
                                                toList(
                                                    avatar.arr
                                                )
                                            ).putExtra("checkEdit", true)
                                            .putExtra("fileName", File(avatar.path).name)
                                    )

                                } else {
                                    showToast(
                                        this@MyCreationActivity,
                                        R.string.please_check_your_network_connection
                                    )
                                }

                            } else {
                                File(arrPathAvatar[pos]).delete()
                                showToast(
                                    this@MyCreationActivity,
                                    R.string.image_error_please_try_again
                                )
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
                            layoutSticker.show()
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
                                this@MyCreationActivity,
                                ViewActivity::class.java
                            ).putExtra("data", arrPathDesign[pos])
                        )

                    }

                    "delete" -> {
                        var dialog = DialogExit(
                            this@MyCreationActivity,
                            "delete"
                        )
                        dialog.onClick = {
                            File(arrPathDesign[pos]).delete()
                            arrPathDesign.removeAt(pos)
                            submitList(arrPathDesign)
                            showToast(
                                this@MyCreationActivity,
                                R.string.file_deleted_successfully
                            )
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
        binding.layoutSticker.visibility = View.GONE
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
        startActivity(
            newIntent(
                this@MyCreationActivity,
                MainActivity::class.java
            )
        )
    }

    @SuppressLint("ResourceAsColor")
    override fun initAction() {
        binding.apply {
            root.onClick { hideLongClick() }
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
            imvBack.onClick {
                startActivity(
                    newIntent(
                        this@MyCreationActivity,
                       MainActivity::class.java
                    )
                )
            }
            btnDownload.onClick {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    !checkPermision(this@MyCreationActivity)
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
                                this@MyCreationActivity,
                                R.string.you_have_not_selected_anything_yet
                            )
                        } else {
                            adapterAvatar.arrCheckTick.forEach {
                                saveFileToExternalStorage(
                                    this@MyCreationActivity,
                                    arrPathAvatar[it],
                                    ""
                                ) { check, path ->
                                    if (check) {
                                        scanMediaFile(
                                            this@MyCreationActivity,
                                            File(path)
                                        )
                                    }
                                }
                            }
                            Toast.makeText(
                                this@MyCreationActivity,
                                getString(R.string.download_successfully) + " " + NAME_SAVE_FILE,
                                Toast.LENGTH_SHORT
                            ).show()
                            hideLongClick()
                        }
                    } else {
                        if (adapterDesign.arrCheckTick.isEmpty()) {
                            showToast(
                                this@MyCreationActivity,
                                R.string.you_have_not_selected_anything_yet
                            )
                        } else {
                            adapterDesign.arrCheckTick.forEach {
                                saveFileToExternalStorage(
                                    this@MyCreationActivity,
                                    arrPathDesign[it],
                                    ""
                                ) { check, path ->
                                    if (check) {
                                        scanMediaFile(
                                            this@MyCreationActivity,
                                            File(path)
                                        )
                                    }
                                }
                            }
                            Toast.makeText(
                                this@MyCreationActivity,
                                getString(R.string.download_successfully) + " " + NAME_SAVE_FILE,
                                Toast.LENGTH_SHORT
                            ).show()
                            hideLongClick()
                        }
                    }
                }
            }

            btnShareAll.onClick {
                if (checkAvatar) {
                    if (adapterAvatar.arrCheckTick.isEmpty()) {
                        showToast(
                            this@MyCreationActivity,
                            R.string.you_have_not_selected_anything_yet
                        )
                    } else {
                        var listPath = arrayListOf<String>()
                        adapterAvatar.arrCheckTick.forEach {
                            listPath.add(arrPathAvatar[it])
                        }
                        shareListFiles(
                            this@MyCreationActivity,
                            listPath
                        )
                        hideLongClick()
                    }
                } else {
                    if (adapterDesign.arrCheckTick.isEmpty()) {
                        showToast(
                            this@MyCreationActivity,
                            R.string.you_have_not_selected_anything_yet
                        )
                    } else {
                        var listPath = arrayListOf<String>()
                        adapterDesign.arrCheckTick.forEach {
                            listPath.add(arrPathDesign[it])
                        }
                        shareListFiles(
                            this@MyCreationActivity,
                            listPath
                        )
                        hideLongClick()
                    }
                }
            }
            btnTelegram.onClick {  // Assuming btnTelegram exists in your layout
                handleTelegram()
            }
            btnWhatsApp.onClick {  // Assuming btnWhatsapp exists in your layout
                handleWhatsapp()
            }

            imvTickAll.onClick {
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
            imvDelete.onClick {
                if (checkAvatar) {
                    if (adapterAvatar.arrCheckTick.isEmpty()) {
                        showToast(
                            this@MyCreationActivity,
                            R.string.you_have_not_selected_anything_yet
                        )
                    } else {
                        var dialog = DialogExit(
                            this@MyCreationActivity,
                            "delete"
                        )
                        dialog.onClick = {
                            adapterAvatar.arrCheckTick.forEach { pos ->
                                viewModel.deleteAvatar(arrPathAvatar[pos])
                                File(arrPathAvatar[pos]).delete()
                            }
                            getData()
//                            arrPathAvatar.remove()
                            hideLongClick()
                            showToast(
                                this@MyCreationActivity,
                                R.string.file_deleted_successfully
                            )
                        }
                        dialog.show()
                    }
                } else {
                    if (adapterDesign.arrCheckTick.isEmpty()) {
                        showToast(
                            this@MyCreationActivity,
                            R.string.you_have_not_selected_anything_yet
                        )
                    } else {
                        var dialog = DialogExit(
                            this@MyCreationActivity,
                            "delete"
                        )
                        dialog.onClick = {
                            adapterDesign.arrCheckTick.forEach { pos ->
                                viewModel.deleteAvatar(arrPathDesign[pos])
                                File(arrPathDesign[pos]).delete()
                            }
                            getData()
                            hideLongClick()
                            showToast(
                                this@MyCreationActivity,
                                R.string.file_deleted_successfully
                            )
                        }
                        dialog.show()
                    }
                }
            }

            btnAvatar.onClick {
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
            btnDesign.onClick {
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
            REQUEST_STORAGE_PERMISSION -> {

            }
        }
    }
    private fun handleTelegram() {
        val listPath =
            adapterAvatar.arrCheckTick.map { arrPathAvatar[it] }as  ArrayList


        if (listPath.isEmpty()) {
            showToast(
                this@MyCreationActivity,
                R.string.you_have_not_selected_anything_yet
            )
            return
        }

        val uriList = getUrisFromPaths(this, ArrayList(listPath))
        if (uriList.isEmpty()) {
            showToast(
                this@MyCreationActivity,
                R.string.image_error_please_try_again
            )
            return
        }
        Log.d("telegram", "${uriList}")
       importToTelegram(this, uriList)
        hideLongClick()
    }

    private fun handleWhatsapp() {
        val listPath =
            adapterAvatar.arrCheckTick.map { arrPathAvatar[it] } as  ArrayList


        if (listPath.isEmpty()) {
            showToast(
                this@MyCreationActivity,
                R.string.you_have_not_selected_anything_yet
            )
            return
        }

        if (listPath.size < 3) {
            showToast(this, R.string.limit_3_items)
            return
        }
        if (listPath.size > 30) {
            showToast(this, R.string.limit_30_items)
            return
        }

        val dialog = CreateNameDialog(this)
        dialog.show()

        dialog.onYesClick = { packageName ->
            Log.d("whatApp", "${listPath}")

            addToWhatsappActivity(this, packageName, listPath) { pack ->
                pack?.let { addToWhatsapp(it) } ?: run {
                    showToast(
                        this@MyCreationActivity,
                        R.string.save_failed
                    )
                }
            }
            dialog.dismiss()
        }
        dialog.onNoClick = { dialog.dismiss() }
        hideLongClick()
    }
    fun getUrisFromPaths(
        context: Context,
        paths: ArrayList<String>
    ): ArrayList<Uri> {

        val result = ArrayList<Uri>()

        paths.forEach { path ->
            val originalFile = File(path)
            if (!originalFile.exists()) return@forEach

            // 1. Decode bitmap
            val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)
                ?: return@forEach

            // 2. Resize về 512x512
            val resizedBitmap = Bitmap.createScaledBitmap(
                bitmap,
                512,
                512,
                true
            )

            // 3. Lưu vào cacheDir
            val resizedFile = File(
                context.cacheDir,
                "${originalFile.nameWithoutExtension}_512.png"
            )

            FileOutputStream(resizedFile).use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // 4. Tạo Uri qua FileProvider (CHUẨN)
            val uri = FileProvider.getUriForFile(
                context,
                "${packageName}.provider",
                resizedFile
            )

            result.add(uri)
        }

        return result
    }

    fun addToWhatsappActivity(
        context: Activity,
        packageName: String,
        list: ArrayList<String>,
        onResult: (StickerPack?) -> Unit
    ) {
        if (list.isEmpty()) return
        val uriList = getUrisFromPaths(context, list)
        val packId = generateIdFromUrl(context, packageName)
        val stickerPack = StickerPack(
            packId,
            packageName,
            uriList,
            context
        )
        addPackIfNotAlreadyAdded(stickerPack)
        onResult(stickerPack)
    }

}