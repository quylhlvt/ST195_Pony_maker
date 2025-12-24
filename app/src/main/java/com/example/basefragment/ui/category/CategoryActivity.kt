package com.example.basefragment.ui.category

import android.os.Bundle
import com.example.basefragment.R
import com.example.basefragment.base.AbsBaseActivity
import com.example.basefragment.data.repository.ApiRepository
import com.example.basefragment.ui.customview.CustomviewActivity
import com.example.basefragment.utils.DataHelper
import com.example.basefragment.utils.isInternetAvailable
import com.example.basefragment.utils.newIntent
import com.example.basefragment.utils.onSingleClick
import com.example.basefragment.utils.showToast
import com.example.basefragment.databinding.ActivityCategoryBinding

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CategoryActivity : AbsBaseActivity<ActivityCategoryBinding>() {
    @Inject
    lateinit var apiRepository: ApiRepository
    val adapter by lazy { CategoryAdapter() }

    override fun getLayoutId(): Int = R.layout.activity_category



    override fun onRestart() {
        super.onRestart()
    }

    override fun initView() {
        if (DataHelper.arrBg.size == 0) {
//            GlobalScope.launch(Dispatchers.IO) {
//                getData(apiRepository)
//            }
            finish()
        } else {
            binding.rcv.itemAnimator = null
            binding.rcv.adapter = adapter
            adapter.submitList(DataHelper.arrBlackCentered)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun initAction() {
        binding.apply {
            imvBack.onSingleClick {
                    finish()

            }
            adapter.onCLick = {
                if (DataHelper.arrBlackCentered[it].checkDataOnline) {
                    if (isInternetAvailable(this@CategoryActivity)) {
                            var a = DataHelper.arrBlackCentered[it].avt.split("/")
                            var b = a[a.size - 2]

                            startActivity(
                                newIntent(
                                    applicationContext,
                                    CustomviewActivity::class.java
                                ).putExtra("data", it)
                            )

                    } else {
                        showToast(
                            this@CategoryActivity,
                            R.string.please_check_your_network_connection
                        )
                    }
                } else {
                        var a = DataHelper.arrBlackCentered[it].avt.split("/")
                        var b = a[a.size - 2]

                        startActivity(
                            newIntent(
                                applicationContext,
                                CustomviewActivity::class.java
                            ).putExtra("data", it)
                        )

                }
            }
        }
    }
}