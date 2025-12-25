package com.oc.pony.ponymaker.create.ui.category

import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ActivityCategoryBinding
import com.oc.pony.ponymaker.create.utils.onSingleClick
import com.oc.pony.ponymaker.create.utils.showInter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CategoryActivity : com.oc.pony.ponymaker.create.base.AbsBaseActivity<ActivityCategoryBinding>() {
    @Inject
    lateinit var apiRepository: com.oc.pony.ponymaker.create.data.repository.ApiRepository
    val adapter by lazy { CategoryAdapter() }

    override fun getLayoutId(): Int = R.layout.activity_category



    override fun onRestart() {
        super.onRestart()
    }

    override fun initView() {
        if (_root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBg.size == 0) {
//            GlobalScope.launch(Dispatchers.IO) {
//                getData(apiRepository)
//            }
            finish()
        } else {
            binding.rcv.itemAnimator = null
            binding.rcv.adapter = adapter
            adapter.submitList(_root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun initAction() {
        binding.apply {
            imvBack.onSingleClick {
                showInter {
                    finish()
                }
            }
            adapter.onCLick = {
                if (_root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered[it].checkDataOnline) {
                    if (_root_ide_package_.com.oc.pony.ponymaker.create.utils.isInternetAvailable(this@CategoryActivity)) {
                        showInter {
                            var a = _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered[it].avt.split("/")
                            var b = a[a.size - 2]

                            startActivity(
                                _root_ide_package_.com.oc.pony.ponymaker.create.utils.newIntent(
                                    applicationContext,
                                    _root_ide_package_.com.oc.pony.ponymaker.create.ui.customview.CustomviewActivity::class.java
                                ).putExtra("data", it)
                            )
                        }
                    } else {
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.showToast(
                            this@CategoryActivity,
                            R.string.please_check_your_network_connection
                        )
                    }
                } else {
                    showInter {
                        var a = _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered[it].avt.split("/")
                        var b = a[a.size - 2]

                        startActivity(
                            _root_ide_package_.com.oc.pony.ponymaker.create.utils.newIntent(
                                applicationContext,
                                _root_ide_package_.com.oc.pony.ponymaker.create.ui.customview.CustomviewActivity::class.java
                            ).putExtra("data", it)
                        )
                    }
                }
            }
        }
    }
}