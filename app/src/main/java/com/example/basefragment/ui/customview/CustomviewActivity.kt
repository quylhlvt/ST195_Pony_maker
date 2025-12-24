package com.example.basefragment.ui.customview

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isInvisible
import com.example.basefragment.base.AbsBaseActivity
import com.example.basefragment.data.model.AvatarModel
import com.example.basefragment.data.model.BodyPartModel
import com.example.basefragment.dialog.DialogExit
import com.example.basefragment.ui.background.BackgroundActivity
import com.example.basefragment.utils.DataHelper
import com.example.basefragment.utils.fromList
import com.example.basefragment.utils.inhide
import com.example.basefragment.utils.isInternetAvailable
import com.example.basefragment.utils.onSingleClick
import com.example.basefragment.utils.saveBitmap
import com.example.basefragment.utils.show
import com.example.basefragment.utils.showToast
import com.example.basefragment.utils.viewToBitmap
import com.bumptech.glide.Glide
import com.example.basefragment.R
import com.example.basefragment.databinding.ActivityCustomizeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomviewActivity : AbsBaseActivity<ActivityCustomizeBinding>() {
    val viewModel: CustomviewViewModel by viewModels()
    var arrShowColor = arrayListOf<Boolean>()
    var countRandom = 0
    val adapterColor by lazy {
        ColorAdapter()
    }
    val adapterNav by lazy {
        NavAdapter()
    }
    val adapterPart by lazy {
        PartAdapter()
    }

    override fun getLayoutId(): Int = R.layout.activity_customize


    override fun onRestart() {
        super.onRestart()
    }

    override fun initView() {
        binding.btnSave.isSelected = true
        if (DataHelper.arrBlackCentered.size > 0) {
            binding.apply {
                rcvPart.adapter = adapterPart
                rcvPart.itemAnimator = null


                rcvColor.adapter = adapterColor
                rcvColor.itemAnimator = null


                rcvNav.adapter = adapterNav
                rcvNav.itemAnimator = null

                getData1()
                repeat(DataHelper.listImageSortView.size) {
                    listImg.add(AppCompatImageView(applicationContext).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        binding.rl.addView(this)
                    })
                }
                adapterNav.posNav = 0
                adapterNav.submitList(listData)

                adapterColor.setPos(arrInt[0][1])
                if (listData[adapterNav.posNav].listPath.size == 1) {
                    binding.llColor.visibility = View.INVISIBLE
                    binding.imvShowColor.visibility = View.INVISIBLE
                } else {
                    binding.llColor.visibility = View.VISIBLE
                    binding.imvShowColor.visibility = View.VISIBLE
                    adapterColor.submitList(listData[adapterNav.posNav].listPath)
                }


                adapterPart.setPos(arrInt[0][0])
                adapterPart.submitList(listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath)

                putImage(listData[adapterNav.posNav].icon, 1)
            }

            if (arrIntHottrend != null) {
                listData.forEachIndexed { index, partBody ->
                    putImage(
                        partBody.icon,
                        arrInt[index][0],
                        false,
                        index,
                        arrInt[index][1]
                    )
                }
                adapterPart.setPos(arrInt[adapterNav.posNav][0])
                adapterColor.setPos(arrInt[adapterNav.posNav][1])
                adapterPart.submitList(listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath)
                if (listData[adapterNav.posNav].listPath.size == 1) {
                    binding.llColor.visibility = View.INVISIBLE
                } else {
                    binding.llColor.visibility = View.VISIBLE
                    adapterColor.submitList(listData[adapterNav.posNav].listPath)
                }
            }
        } else {
            finish()
        }
    }

    var listImg = arrayListOf<AppCompatImageView>()
    fun putImage(
        icon: String,
        pos: Int,
        checkRestart: Boolean = false,
        posNav: Int? = null,
        posColor: Int? = null
    ) {
        DataHelper.listImageSortView.forEachIndexed { _pos, _data ->
            if (_data == icon) {
                handleVisibility(
                    listImg[_pos],
                    pos,
                    checkRestart,
                    posNav,
                    posColor
                )
                return@forEachIndexed
            }
        }
    }


    private fun handleVisibility(
        view: ImageView, pos: Int, checkRestart: Boolean = false,
        posNav: Int? = null,
        posColor: Int? = null
    ) {
        if (checkRestart) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
            Glide.with(applicationContext)
                .load(
                    listData[posNav ?: adapterNav.posNav]
                        .listPath[posColor ?: adapterColor.posColor]
                        .listPath[pos]
                )
                .into(view)
        }
    }

    var listData = arrayListOf<BodyPartModel>()

    //0 - path, 1 - color
    var arrInt = arrayListOf<ArrayList<Int>>()
    var blackCentered = 0
    var arrIntHottrend: ArrayList<ArrayList<Int>>? = null
    private fun getData1() {
        DataHelper.listImageSortView.clear()
        DataHelper.listImage.clear()
        blackCentered = intent.getIntExtra("data", 0)
        arrIntHottrend = intent.getSerializableExtra("arr") as? ArrayList<ArrayList<Int>>
        var checkFirst = true
        repeat(DataHelper.arrBlackCentered[blackCentered].bodyPart.size) {
            DataHelper.listImageSortView.add("")
            DataHelper.listImage.add("")
        }
        DataHelper.arrBlackCentered[blackCentered].bodyPart.forEach {
            val (x, y) = it.icon.substringBeforeLast("/").substringAfterLast("/").split("-")
                .map { it.toInt() }
            DataHelper.listImageSortView[x - 1] = it.icon
            DataHelper.listImage[y - 1] = it.icon
        }

        //thu tu navi
        DataHelper.listImage.forEachIndexed { index, icon ->
            var x = DataHelper.arrBlackCentered[blackCentered].bodyPart.indexOfFirst { it.icon == icon }
            var y = DataHelper.listImageSortView.indexOf(icon)
            if (x != -1) {
                arrShowColor.add(true)
                listData.add(DataHelper.arrBlackCentered[blackCentered].bodyPart[x])
                if (checkFirst) {
                    checkFirst = false
//                    arrIntHottrend thu tu view
                    if (arrIntHottrend != null) {
                        arrInt.add(arrayListOf(arrIntHottrend!![y][0], arrIntHottrend!![y][1]))
                    } else {
                        arrInt.add(arrayListOf(1, 0))
                    }
                } else {
                    if (arrIntHottrend != null) {
                        arrInt.add(arrayListOf(arrIntHottrend!![y][0], arrIntHottrend!![y][1]))
                    } else {
                        arrInt.add(arrayListOf(0, 0))
                    }
                }
            }
        }
    }

    var checkRevert = true
    var checkHide = false
    override fun initAction() {
        adapterColor.onClick = {
            if (!DataHelper.arrBlackCentered[blackCentered].checkDataOnline || isInternetAvailable(
                    applicationContext
                )
            ) {
                val recyclerState = binding.rcvPart.layoutManager?.onSaveInstanceState()
                adapterColor.setPos(it)
                adapterColor.submitList(listData[adapterNav.posNav].listPath)
                arrInt[adapterNav.posNav][1] = it
                adapterPart.submitList(listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath) {
                    binding.rcvPart.layoutManager?.onRestoreInstanceState(recyclerState)
                }
                putImage(listData[adapterNav.posNav].icon, adapterPart.posPath)
            } else {
                showToast(applicationContext, R.string.please_check_your_network_connection)
            }

        }
        adapterNav.onClick = {
            if (!DataHelper.arrBlackCentered[blackCentered].checkDataOnline || isInternetAvailable(
                    applicationContext
                )
            ) {
                    adapterNav.setPos(it)
                    adapterNav.submitList(listData)
                    adapterColor.setPos(arrInt[it][1])

                    if (listData[adapterNav.posNav].listPath.size == 1) {
                        binding.llColor.visibility = View.INVISIBLE
                        binding.imvShowColor.visibility = View.INVISIBLE
                    } else {
                        if (arrShowColor[adapterNav.posNav]) {
                            binding.llColor.show()
                        } else {
                            binding.llColor.inhide()
                        }
                        binding.imvShowColor.visibility = View.VISIBLE
                        adapterColor.submitList(listData[it].listPath)
                        binding.root.postDelayed(
                            { binding.rcvColor.smoothScrollToPosition(arrInt[it][1]) },
                            100
                        )
                    }
                    if (adapterColor.posColor == arrInt[adapterNav.posNav][1]) {
                        adapterPart.setPos(arrInt[adapterNav.posNav][0])
                    } else {
                        adapterPart.setPos(-1)
                    }
                    adapterPart.submitList(listData[it].listPath[adapterColor.posColor].listPath)
                    binding.root.postDelayed(
                        { binding.rcvPart.smoothScrollToPosition(arrInt[it][0]) },
                        100
                    )

            } else {
                showToast(applicationContext, R.string.please_check_your_network_connection)
            }

        }
        adapterPart.onClick = { it, type ->
            if (!DataHelper.arrBlackCentered[blackCentered].checkDataOnline || isInternetAvailable(
                    applicationContext
                )
            ) {
                when (type) {
                    "none" -> {
                        adapterPart.setPos(it)
                        adapterPart.submitList(listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath)
                        arrInt[adapterNav.posNav][0] = it
                        arrInt[adapterNav.posNav][1] = adapterColor.posColor
                        putImage(listData[adapterNav.posNav].icon, it, true)
                    }

                    "dice" -> {
                        when (listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath[0]) {
                            "none" -> {
                                if (listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath.size > 3) {
                                    var x =
                                        (2..<listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath.size).random()
                                    adapterPart.setPos(x)
                                    adapterPart.submitList(listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath)
                                    arrInt[adapterNav.posNav][0] = x
                                    arrInt[adapterNav.posNav][1] = adapterColor.posColor
                                    putImage(listData[adapterNav.posNav].icon, x)
                                } else {
                                    adapterPart.setPos(2)
                                    adapterPart.submitList(listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath)
                                    arrInt[adapterNav.posNav][0] = 2
                                    arrInt[adapterNav.posNav][1] = adapterColor.posColor
                                    putImage(listData[adapterNav.posNav].icon, 2)
                                }
                            }

                            "dice" -> {
                                if (listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath.size > 2) {
                                    var x =
                                        (1..<listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath.size).random()
                                    adapterPart.setPos(x)
                                    adapterPart.submitList(listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath)
                                    arrInt[adapterNav.posNav][0] = x
                                    arrInt[adapterNav.posNav][1] = adapterColor.posColor
                                    putImage(listData[adapterNav.posNav].icon, x)
                                } else {
                                    adapterPart.setPos(1)
                                    adapterPart.submitList(listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath)
                                    arrInt[adapterNav.posNav][0] = 1
                                    arrInt[adapterNav.posNav][1] = adapterColor.posColor
                                    putImage(listData[adapterNav.posNav].icon, 1)
                                    showToast(
                                        applicationContext,
                                        R.string.the_layer_have_only_one_item
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        adapterPart.setPos(it)
                        adapterPart.submitList(listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath)
                        arrInt[adapterNav.posNav][0] = it
                        arrInt[adapterNav.posNav][1] = adapterColor.posColor
                        putImage(listData[adapterNav.posNav].icon, it)
                    }
                }
            } else {
                showToast(applicationContext, R.string.please_check_your_network_connection)
            }
        }
        binding.apply {
            imvShowColor.onSingleClick {
                arrShowColor[adapterNav.posNav] = !arrShowColor[adapterNav.posNav]
                if (arrShowColor[adapterNav.posNav]) {
                    llColor.show()
                } else {
                    llColor.inhide()
                }
            }
            btnReset.onSingleClick {
//                if(!arrBlackCentered[blackCentered].checkDataOnline || isInternetAvailable(applicationContext)){
                var dialog = DialogExit(this@CustomviewActivity, "reset")
                dialog.onClick = {
                        DataHelper.listImage.forEach {
                            putImage("0", 0, true)
                        }
                        arrInt.forEach { i ->
                            i[0] = 0
                            i[1] = 0
                        }
                        arrInt[0][0] = 1
                        arrInt[0][1] = 0

                        adapterPart.setPos(arrInt[adapterNav.posNav][0])
                        adapterColor.setPos(arrInt[adapterNav.posNav][1])
                        adapterPart.submitList(listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath)
                        if (listData[adapterNav.posNav].listPath.size == 1) {
                            binding.llColor.visibility = View.INVISIBLE
                            binding.imvShowColor.visibility = View.INVISIBLE
                        } else {
                            if (!checkHide) {
                                binding.llColor.visibility = View.VISIBLE
                                binding.imvShowColor.visibility = View.VISIBLE
                                adapterColor.submitList(listData[adapterNav.posNav].listPath)
                            }
                        }
                        listData.forEachIndexed { index, bodyPartModel ->
                            putImage(bodyPartModel.icon, 1, true)
                        }
                        putImage(listData[0].icon, 1, false, 0, 0)
                    }

                dialog.show()
//                }else{
//                    showToast(applicationContext,R.string.please_check_your_network_connection)
//                }
            }
            imvBack.onSingleClick {
                var dialog = DialogExit(this@CustomviewActivity, "exit")
                dialog.onClick = {
                        finish()


                }
                dialog.show()
            }
            btnRevert.onSingleClick {
                checkRevert = !checkRevert
                if (checkRevert) {
                    listImg.forEach {
                        it.scaleX = 1f
                    }
                } else {
                    listImg.forEach {
                        it.scaleX = -1f
                    }
                }
            }
            btnDice.onSingleClick {
                if (!DataHelper.arrBlackCentered[blackCentered].checkDataOnline || isInternetAvailable(
                        applicationContext
                    )
                ) {
                    countRandom++
                    if (countRandom == 3) {
                        btnDice.inhide()
                    }
                        listData.forEachIndexed { index, partBody ->
                            if (partBody.listPath.size > 1) {
                                arrInt[index][1] = (0..<partBody.listPath.size).random()

                            } else {
                                arrInt[index][1] = 0
                            }
                            if (partBody.listPath[arrInt[index][1]].listPath[0] == "none") {
                                if (partBody.listPath[arrInt[index][1]].listPath.size > 3) {
                                    arrInt[index][0] =
                                        (2..<partBody.listPath[arrInt[index][1]].listPath.size).random()
                                } else {
                                    arrInt[index][0] = 2
                                }
                            } else {
                                if (partBody.listPath[arrInt[index][1]].listPath.size > 2) {
                                    arrInt[index][0] =
                                        (1..<partBody.listPath[arrInt[index][1]].listPath.size).random()
                                } else {
                                    arrInt[index][0] = 1
                                }
                            }
                            putImage(
                                partBody.icon,
                                arrInt[index][0],
                                false,
                                index,
                                arrInt[index][1]
                            )
                        }
                        adapterPart.setPos(arrInt[adapterNav.posNav][0])
                        adapterColor.setPos(arrInt[adapterNav.posNav][1])
                        adapterPart.submitList(listData[adapterNav.posNav].listPath[adapterColor.posColor].listPath)
                        if (listData[adapterNav.posNav].listPath.size == 1) {
                            binding.llColor.visibility = View.INVISIBLE
                            binding.imvShowColor.visibility = View.INVISIBLE
                        } else {
                            if (!checkHide) {
//                                if (arrShowColor[adapterNav.posNav]) {
//                                    binding.llColor.show()
//                                } else {
//                                    binding.llColor.inhide()
//                                }
                                arrShowColor[adapterNav.posNav] = true
                                binding.llColor.visibility = View.VISIBLE
                                binding.imvShowColor.visibility = View.VISIBLE
                                adapterColor.submitList(listData[adapterNav.posNav].listPath)
                            }
                        }

                } else {
                    showToast(applicationContext, R.string.please_check_your_network_connection)
                }
            }
            llLoading.onSingleClick {
                showToast(applicationContext, R.string.please_wait_a_few_seconds_for_data_to_load)
            }
            btnSave.onSingleClick {
                llLoading.visibility = View.VISIBLE
                saveBitmap(
                    this@CustomviewActivity,
                    viewToBitmap(rl),
                    intent.getStringExtra("fileName") ?: "",
                    true
                ) { it, path, pathOld ->
                    if (it) {
                        viewModel.deleteAvatar(pathOld)
                        llLoading.visibility = View.GONE
                        //lop layer
                        var x = arrayListOf<ArrayList<Int>>()
                        DataHelper.listImageSortView.forEachIndexed { _pos, icon ->
                            var y = DataHelper.listImage.indexOf(icon)
                            x.add(arrInt[y])
                        }

                        viewModel.addAvatar(
                            AvatarModel(
                                path, DataHelper.arrBlackCentered[blackCentered].avt,
                                fromList(x)
                            )
                        )
                        startActivity(
                            Intent(
                                this@CustomviewActivity,
                                BackgroundActivity::class.java
                            ).putExtra("path", path)
                        )


                    } else {
                        llLoading.visibility = View.GONE
                        showToast(this@CustomviewActivity, R.string.save_failed)
                    }
                }
            }
            btnSee.onSingleClick {
                if (btnRevert.isInvisible) {
                    btnRevert.show()
                    btnReset.show()
                    btnSave.show()
                    if (listData[adapterNav.posNav].listPath.size > 1) {
                        if (arrShowColor[adapterNav.posNav]) {
                            binding.llColor.show()
                        }
                        imvShowColor.show()
                    }
                    if (countRandom < 3) {
                        btnDice.show()
                    }
                    llPart.show()
                    llNav.show()
                    btnSee.setImageResource(R.drawable.imv_see)
                } else {
                    btnRevert.inhide()
                    btnReset.inhide()
                    btnSave.inhide()
                    imvShowColor.inhide()
                    llColor.inhide()
                    btnDice.inhide()
                    llPart.inhide()
                    llNav.inhide()
                    btnSee.setImageResource(R.drawable.imv_see_false)
                }

            }
        }
    }

    override fun onBackPressed() {
        var dialog = DialogExit(this@CustomviewActivity, "exit")
        dialog.onClick = {
                finish()


        }
        dialog.show()
    }
}