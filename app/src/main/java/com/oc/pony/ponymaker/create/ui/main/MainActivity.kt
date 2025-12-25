package com.oc.pony.ponymaker.create.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.lifecycle.lifecycleScope
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ActivityMainBinding
import com.oc.pony.ponymaker.create.utils.DataHelper.getData
import com.oc.pony.ponymaker.create.utils.backPress
import com.oc.pony.ponymaker.create.utils.onSingleClick
import com.oc.pony.ponymaker.create.utils.showInter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : com.oc.pony.ponymaker.create.base.AbsBaseActivity<ActivityMainBinding>() {
    @Inject
    lateinit var apiRepository: com.oc.pony.ponymaker.create.data.repository.ApiRepository
    var checkCallingDataOnline = false
    override fun getLayoutId(): Int = R.layout.activity_main
    private var networkReceiver: BroadcastReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val connectivityManager =
                context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            if (!checkCallingDataOnline) {
                if (networkInfo != null && networkInfo.isConnected) {
                    var checkDataOnline = false
                    _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered.forEach {
                        if (it.checkDataOnline) {
                            checkDataOnline = true
                            return@forEach
                        }
                    }
                    if (!checkDataOnline) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            getData(apiRepository)
                        }
                    }
                } else {
                    if (_root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered.isEmpty()) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            getData(apiRepository)
                        }
                    }
                }
            }
        }
    }


    override fun initView() {
        binding.apply {
            tv1.isSelected = true
            tv2.isSelected = true
            tv3.isSelected = true
        }
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
        _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrDataOnline.observe(this) {
            it?.let {
                when (it.loadingStatus) {
                    _root_ide_package_.com.oc.pony.ponymaker.create.data.callapi.reponse.LoadingStatus.Loading -> {
                        checkCallingDataOnline = true
                    }

                    _root_ide_package_.com.oc.pony.ponymaker.create.data.callapi.reponse.LoadingStatus.Success -> {
                        if (_root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered.isNotEmpty() && !_root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered[0].checkDataOnline) {
                            checkCallingDataOnline = false
                            val listA = (it as com.oc.pony.ponymaker.create.data.callapi.reponse.DataResponse.DataSuccess).body ?: return@observe
                            checkCallingDataOnline = true
                            val sortedMap = listA
                                .toList() // Chuyển map -> list<Pair<String, List<X10>>>
                                .sortedBy { (_, list) ->
                                    list.firstOrNull()?.level ?: Int.MAX_VALUE
                                }
                                .toMap()
                            sortedMap.forEach { key, list ->
                                var a = arrayListOf<com.oc.pony.ponymaker.create.data.model.BodyPartModel>()
                                list.forEachIndexed { index, x10 ->
                                    var b = arrayListOf<com.oc.pony.ponymaker.create.data.model.ColorModel>()
                                    x10.colorArray.split(",").forEach { coler ->
                                        var c = arrayListOf<String>()
                                        if (coler == "") {
                                            for (i in 1..x10.quantity) {
                                                c.add(_root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.BASE_URL + "${_root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.BASE_CONNECT}/${x10.position}/${x10.parts}/${i}.png")
                                            }
                                            b.add(
                                                _root_ide_package_.com.oc.pony.ponymaker.create.data.model.ColorModel(
                                                    "#",
                                                    c
                                                )
                                            )
                                        } else {
                                            for (i in 1..x10.quantity) {
                                                c.add(_root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.BASE_URL + "${_root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.BASE_CONNECT}/${x10.position}/${x10.parts}/${coler}/${i}.png")
                                            }
                                            b.add(
                                                _root_ide_package_.com.oc.pony.ponymaker.create.data.model.ColorModel(
                                                    coler,
                                                    c
                                                )
                                            )
                                        }
                                    }
                                    a.add(
                                        _root_ide_package_.com.oc.pony.ponymaker.create.data.model.BodyPartModel(
                                            "${_root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.BASE_URL}${_root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.BASE_CONNECT}$key/${x10.parts}/nav.png",
                                            b
                                        )
                                    )
                                }
                                var dataModel =
                                    _root_ide_package_.com.oc.pony.ponymaker.create.data.model.CustomModel(
                                        "${_root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.BASE_URL}${_root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.BASE_CONNECT}$key/avatar.png",
                                        a,
                                        true
                                    )
                                dataModel.bodyPart.forEach { mbodyPath ->
                                    if (mbodyPath.icon.substringBeforeLast("/")
                                            .substringAfterLast("/").substringAfter("-") == "1"
                                    ) {
                                        mbodyPath.listPath.forEach {
                                            if (it.listPath[0] != "dice") {
                                                it.listPath.add(0, "dice")
                                            }
                                        }
                                    } else {
                                        mbodyPath.listPath.forEach {
                                            if (it.listPath[0] != "none") {
                                                it.listPath.add(0, "none")
                                                it.listPath.add(1, "dice")
                                            }
                                        }
                                    }
                                }
                                _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered.add(0, dataModel)
                            }
                        }
                        checkCallingDataOnline = false
                    }

                    _root_ide_package_.com.oc.pony.ponymaker.create.data.callapi.reponse.LoadingStatus.Error -> {
                        checkCallingDataOnline = false
                    }

                    else -> {
                        checkCallingDataOnline = true
                    }
                }
            }
        }
    }

    override fun initAction() {
        binding.apply {
            btnCreate.onSingleClick {
                if (!checkCallingDataOnline) {
                    startActivity(
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.newIntent(
                            applicationContext,
                            _root_ide_package_.com.oc.pony.ponymaker.create.ui.category.CategoryActivity::class.java
                        )
                    )
                } else {
                    _root_ide_package_.com.oc.pony.ponymaker.create.utils.showToast(
                        applicationContext,
                        R.string.please_wait_a_few_seconds_for_data_to_load
                    )
                }
            }
            btnQuickMaker.onSingleClick {
                if (!checkCallingDataOnline) {
                    showInter {
                        startActivity(
                            _root_ide_package_.com.oc.pony.ponymaker.create.utils.newIntent(
                                applicationContext,
                                _root_ide_package_.com.oc.pony.ponymaker.create.ui.quick_mix.QuickMixActivity::class.java
                            )
                        )
                    }
                } else {
                    _root_ide_package_.com.oc.pony.ponymaker.create.utils.showToast(
                        applicationContext,
                        R.string.please_wait_a_few_seconds_for_data_to_load
                    )
                }
            }
            btnMyAlbum.onSingleClick {
                if (!checkCallingDataOnline) {
                    showInter {
                        startActivity(
                            _root_ide_package_.com.oc.pony.ponymaker.create.utils.newIntent(
                                applicationContext,
                                _root_ide_package_.com.oc.pony.ponymaker.create.ui.my_creation.MyCreationActivity::class.java
                            )
                        )
                    }
                } else {
                    _root_ide_package_.com.oc.pony.ponymaker.create.utils.showToast(
                        applicationContext, R.string.please_wait_a_few_seconds_for_data_to_load
                    )
                }

            }
            imvSetting.onSingleClick {
                startActivity(
                    _root_ide_package_.com.oc.pony.ponymaker.create.utils.newIntent(
                        applicationContext,
                        _root_ide_package_.com.oc.pony.ponymaker.create.ui.setting.SettingActivity::class.java
                    )
                )
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(networkReceiver)
        } catch (e: Exception) {

        }
    }

    override fun onBackPressed() {
        backPress(
            _root_ide_package_.com.oc.pony.ponymaker.create.utils.SharedPreferenceUtils(
                applicationContext
            )
        )
    }
}