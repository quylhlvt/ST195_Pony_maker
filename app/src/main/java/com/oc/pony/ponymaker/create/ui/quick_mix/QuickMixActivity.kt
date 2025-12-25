package com.oc.pony.ponymaker.create.ui.quick_mix

import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ActivityQuickMixBinding
import com.oc.pony.ponymaker.create.utils.onSingleClick
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QuickMixActivity : com.oc.pony.ponymaker.create.base.AbsBaseActivity<ActivityQuickMixBinding>() {
    var sizeMix = 21
    var arrMix = arrayListOf<com.oc.pony.ponymaker.create.data.model.CustomModel>()
    @Inject
    lateinit var apiRepository: com.oc.pony.ponymaker.create.data.repository.ApiRepository
    val adapter by lazy { QuickAdapter() }
    override fun getLayoutId(): Int = R.layout.activity_quick_mix

    override fun initView() {
        if (_root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBg.size == 0) {
//            GlobalScope.launch(Dispatchers.IO) {
//                getData(apiRepository)
//            }
            finish()
        } else {
            binding.rcv.itemAnimator = null
            binding.rcv.adapter = adapter
            val resultList = mutableListOf<ArrayList<ArrayList<Int>>>()
            adapter.arrListImageSortView.clear()
            for (pos in 0..<sizeMix){
                var mModel = _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered[pos% _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered.size]
                var list = arrayListOf<String>()
                repeat(mModel.bodyPart.size) {
                    list.add("")
                }
                mModel.bodyPart.forEach {
                    val (x, y) = it.icon.substringBeforeLast("/").substringAfterLast("/").split("-")
                        .map { it.toInt() }
                    list[x - 1] = it.icon
                }
                adapter.arrListImageSortView.add(list)

                val i = arrayListOf<ArrayList<Int>>() // mỗi pos có danh sách riêng
                val bodyPart = mModel.bodyPart
                adapter.arrListImageSortView[pos].forEachIndexed { index, data ->
                    val x = bodyPart.find { it.icon == data }
                    val pair = if (x != null) {
                        val path = x.listPath[0].listPath
                        val color = x.listPath
                        val randomValue = if (path[0] == "none") {
                            if (path.size > 3) (2 until path.size).random() else 2
                        } else {
                            if (path.size > 2) (1 until path.size).random() else 1
                        }
                        val randomColor = (0 until color.size).random()
                        arrayListOf(randomValue, randomColor)
                    } else {
                        arrayListOf(-1, -1)
                    }
                    i.add(pair)
                }
                resultList.add(i)
                arrMix.add(mModel)
            }
//            arrBlackCentered.forEachIndexed { pos, mModel ->
//
//            }

            adapter.listArrayInt.clear()
            adapter.listArrayInt.addAll(resultList)
            adapter.submitList(arrMix)
        }
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun initAction() {
        binding.apply {
            imvBack.onSingleClick { finish() }
            adapter.onCLick = {
                if (_root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered[it% _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered.size].checkDataOnline) {
                    if (_root_ide_package_.com.oc.pony.ponymaker.create.utils.isInternetAvailable(this@QuickMixActivity)) {
                            startActivity(
                                _root_ide_package_.com.oc.pony.ponymaker.create.utils.newIntent(
                                    applicationContext,
                                    _root_ide_package_.com.oc.pony.ponymaker.create.ui.customview.CustomviewActivity::class.java
                                )
                                    .putExtra("data", it% _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered.size).putExtra(
                                    "arr",
                                    adapter.listArrayInt[it]
                                )
                            )

                    } else {
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.showToast(
                            this@QuickMixActivity,
                            R.string.please_check_your_network_connection
                        )
                    }
                } else {
                        startActivity(
                            _root_ide_package_.com.oc.pony.ponymaker.create.utils.newIntent(
                                applicationContext,
                                _root_ide_package_.com.oc.pony.ponymaker.create.ui.customview.CustomviewActivity::class.java
                            )
                                .putExtra("data", it% _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered.size).putExtra(
                                "arr",
                                adapter.listArrayInt[it]
                            )
                        )
                    }

            }
        }
    }
}