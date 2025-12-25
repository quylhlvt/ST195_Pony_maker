package com.oc.pony.ponymaker.create.ui.tutorial

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ActivityTutorialBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TutorialActivity : com.oc.pony.ponymaker.create.base.AbsBaseActivity<ActivityTutorialBinding>() {
    @Inject
    lateinit var sharedPreferenceUtils: com.oc.pony.ponymaker.create.utils.SharedPreferenceUtils
    var listFragment = 3
    private var viewPagerAdapter: ViewPagerAdapter? = null

    override fun getLayoutId(): Int = R.layout.activity_tutorial

    override fun initView() {

        var data = arrayListOf<com.oc.pony.ponymaker.create.data.model.TutorialModel>(
            _root_ide_package_.com.oc.pony.ponymaker.create.data.model.TutorialModel(
                R.drawable.photo_intro1,
                getString(R.string.intro1)
            ),
            _root_ide_package_.com.oc.pony.ponymaker.create.data.model.TutorialModel(
                R.drawable.photo_intro2,
                getString(R.string.intro2)
            ),
            _root_ide_package_.com.oc.pony.ponymaker.create.data.model.TutorialModel(
                R.drawable.photo_intro3,
                getString(R.string.intro3)
            )
        )
        viewPagerAdapter = ViewPagerAdapter()
        viewPagerAdapter!!.getData(data)
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(100))
        compositePageTransformer.addTransformer { page: View, position: Float ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.8f + r * 0.2f
            val absPosition = Math.abs(position)
            page.alpha = 1.0f - (1.0f - 0.3f) * absPosition
        }
        binding.viewPager.setPageTransformer(compositePageTransformer)
        bindViewModel()
        binding.viewPager.currentItem = 0

    }

    private fun bindViewModel() {
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Handler(Looper.myLooper()!!).postDelayed({  addBottomDots(position) },100)
            }
        })
        binding.btnNext.setOnClickListener {

            if (binding.viewPager.currentItem < listFragment - 1) {
                binding.viewPager.setCurrentItem(binding.viewPager.currentItem + 1)
            } else {

                if (!sharedPreferenceUtils.getBooleanValue(_root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.PERMISON)) {
                    val intent = Intent(this@TutorialActivity, _root_ide_package_.com.oc.pony.ponymaker.create.ui.permision.PermissionActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@TutorialActivity, _root_ide_package_.com.oc.pony.ponymaker.create.ui.main.MainActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
    private lateinit var dots: Array<ImageView>
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun addBottomDots(currentPage: Int) {
        binding.apply {
            linearDots.removeAllViews()
            dots = Array(3) { ImageView(applicationContext) }
            for (i in 0..<listFragment) {
                dots[i] = ImageView(applicationContext)
                if (i == currentPage) {
                    dots[i]
                        .setImageDrawable(resources.getDrawable(R.drawable.ic_bg_select))
                    val params = LinearLayout.LayoutParams(
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.dpToPx(
                            20f,
                            applicationContext
                        ).toInt(),
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.dpToPx(8f, applicationContext)
                            .toInt()
                    )
                    params.setMargins(4, 0, 4, 0)
                    linearDots.addView(dots[i], params)
                } else {
                    dots[i]
                        .setImageDrawable(
                            resources.getDrawable(R.drawable.ic_bg_not_select)
                        )
                    val params = LinearLayout.LayoutParams(
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.dpToPx(8f, applicationContext)
                            .toInt(),
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.dpToPx(8f, applicationContext)
                            .toInt()
                    )
                    params.setMargins(4, 0, 4, 0)
                    linearDots.addView(dots[i], params)
                }
            }
        }

    }
    override fun initAction() {

    }
}