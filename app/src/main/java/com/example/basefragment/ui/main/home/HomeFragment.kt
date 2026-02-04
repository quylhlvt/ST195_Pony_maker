package com.example.basefragment.ui.main.home

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.screenRotation
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.core.extention.toSettingFromHome
import com.example.basefragment.core.extention.toSettingFromLang
import com.example.basefragment.core.helper.RateHelper
import com.example.basefragment.databinding.FragmentHomeBinding
import com.example.basefragment.utils.LanguageManager
import com.example.basefragment.utils.music.MusicLocal
import com.example.basefragment.utils.music.MusicLocal.isInSplashOrTutorial
import com.example.basefragment.utils.state.RateState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.system.exitProcess

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(
    FragmentHomeBinding::inflate, HomeViewModel::class.java
) {
    private var countRate =0

    override fun viewListener() {
        binding.apply {
            // Click vào "Choose Character"
            btnManual.onClick(requireContext()) {
                // Navigate tới CategoryFragment

                 findNavController().navigate(R.id.action_home_to_manual)
            }

            // Click vào "Quick Mix"
            btnAuto.onClick(requireContext()) {
                val bundle = Bundle().apply {
                    putBoolean("isAutoMode", true)
                }
                findNavController().navigate(R.id.action_home_to_auto, bundle)
            }
            btnMultiplayer.onClick(requireContext()) {
                findNavController().navigate(R.id.action_home_to_multiplayer)
            }
            actionBar.btnActionBarRight.onClick(requireContext()) {
                toSettingFromHome()
            }
        }
    }
    override fun setupPreViews() {
        super.setupPreViews()

        screenRotation()
    }


    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): FragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)

    override fun initView() {
        MusicLocal.home = true
        isInSplashOrTutorial = false
        MusicLocal.play(requireContext())
        countRate = sharedPreferences.isRateCountRequest()
        binding.actionBar.apply {
            setImageActionBar(btnActionBarRight, R.drawable.ic_settings)
//            setImageActionBar(btnActionBarLeft, R.drawable.logo_app)
        }
        binding.apply {
            tv1.isSelected = true

            tv2.isSelected = true
            tv3.isSelected = true
        }
        sharedPreferences.setBackRequest(sharedPreferences.isBackRequest() + 1)
        deleteTempFolder()
//        binding.textView.text = "Home Fragment"
//        binding.btnTest.setOnClickListener {
//            showSnackbar("Xin chào từ Home!")
//        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            LanguageManager.currentLanguage.collect { newLanguage ->
                // Refresh UI when language changes
                refreshUI()
            }
        }
//        viewModel.data.observe(viewLifecycleOwner) { text ->
//            binding.textView.text = text
//        }
    }
    private fun refreshUI() {
        // Update text views với string resources mới
        binding.apply {
            tv1.text = getString(R.string.pony_maker)
            tv2.text = getString(R.string.trending_custom)
            tv3.text = getString(R.string.my_work)
            // ... update other texts
        }
    }

    override fun bindViewModel() {
    }

    private fun deleteTempFolder() {
        lifecycleScope.launch(Dispatchers.IO) {
//            val dataTemp =
//                MediaHelper.getImageInternal(requireContext(), ValueKey.RANDOM_TEMP_ALBUM)
//            if (dataTemp.isNotEmpty()) {
//                dataTemp.forEach {
//                    val file = File(it)
//                    file.delete()
//                }
//            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            countRate++
            sharedPreferences.setRateCountRequest(countRate)
            if (!sharedPreferences.isRateRequest()&& countRate % 2==0) {
                // Chưa rate -> Show dialog
                RateHelper.showRateDialog(requireActivity(), sharedPreferences) { state ->
                    if (state != RateState.CANCEL) {
                        showToast(R.string.have_rated)
                    }
                    requireActivity().finish()
                    // User cancel -> Không làm gì (ở lại app)
                }
            } else {
                requireActivity().finish()
            }
        }
    }
}