package com.example.basefragment.ui.main.guide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.gone
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.popBack
import com.example.basefragment.core.extention.select
import com.example.basefragment.core.extention.setBulletList
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.core.extention.setTextActionBar
import com.example.basefragment.core.extention.visible
import com.example.basefragment.databinding.FragmentGuideBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class GuideFragment : BaseFragment<FragmentGuideBinding, GuideViewModel>(
    FragmentGuideBinding::inflate, GuideViewModel::class.java
) {
    private var isMuti: Boolean = false
    override fun viewListener() {
        binding.apply {
            setupActionBarListeners()
        }
    }

    override fun setupPreViews() {
        super.setupPreViews()
        isMuti = arguments?.getBoolean("isMuti", false) ?: false

    }

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): FragmentGuideBinding = FragmentGuideBinding.inflate(inflater, container, false)

    private fun FragmentGuideBinding.setupActionBar() {
        actionBar.apply {
            tvCenter.select()
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
            setTextActionBar(tvCenter, getString(R.string.rules))
        }
    }

    private fun FragmentGuideBinding.setupActionBarListeners() {
        actionBar.btnActionBarLeft.onClick(requireContext()) {
            popBack()
        }
    }

    override fun initView() {
        binding.apply {
            setupActionBar()
            if (!isMuti){
                contrainGuide2.gone()
                contrainGuide1.visible()
                img5Guide.setImageResource(R.drawable.img5_guide)
            }
            else{
                contrainGuide1.gone()
                contrainGuide2.visible()
                img5Guide.setImageResource(R.drawable.img5_guide_muti)

            }
            tvHowto.setBulletList(
                getString(R.string.this_mode_is_for_two_players_to_compete_against_each_other)

            )
            tvHowto2.setBulletList(
                getString(R.string.one_person_wears_a_yellow_team_and_one_person_wears_a_blue_team)

            )
            tvSetBomb.setBulletList(
                getString(R.string.the_two_players_take_turns_placing_three_bombs_in_their_opponent_s_playing_area),
            )
            tvSetBomb2.setBulletList(
                getString(R.string.remember_not_to_let_your_enemy_know_the_location_of_the_bomb_you_planted)
            )
            tvLetPlay.setBulletList(
                getString(R.string.in_this_mode_each_player_will_have_3_lives)
            )
            tvLetPlay2.setBulletList(
                getString(R.string.the_two_players_take_turns_flipping_cards_on_their_respective_sides_of_the_field)
            )
            tvLetPlay3.setBulletList(
                getString(R.string.whoever_flips_the_bomb_card_loses_a_life_flipping_the_french_fries_card_leaves_them_safe)
            )
            tvWho.setBulletList(
                getString(R.string.the_last_person_remaining_will_be_the_winner_congratulations)
            )


            tvHowtoMuti.setBulletList(
                getString(R.string.this_mode_is_for_large_groups_of_friends_so_gather_your_friends_and_play_together)
            )
            tvHowto2Muti.setBulletList(
                getString(R.string.people_sit_together_in_a_circle_or_around_a_table)
            )
            tvSetBombMuti.setBulletList(
                getString(R.string.choose_the_appropriate_number_of_bombs_based_on_the_number_of_participants)
            )

            tvLetPlayMuti.setBulletList(
                getString(R.string.one_by_one_each_person_flips_their_card_when_it_s_their_turn)
            )
            tvLetPlay2Muti.setBulletList(
                getString(R.string.whoever_flips_the_bomb_card_is_eliminated_from_the_round_flipping_the_french_fries_card_is_safe)
            )

            tvWhoMuti.setBulletList(
                getString(R.string.the_last_person_remaining_will_be_the_winner_congratulations)
            )
        }
//        lifecycleScope.

//        binding.textView.text = "Home Fragment"
//        binding.btnTest.setOnClickListener {
//            showSnackbar("Xin chào từ Home!")
//        }
    }

    override fun observeData() {
//        viewModel.data.observe(viewLifecycleOwner) { text ->
//            binding.textView.text = text
//        }
    }

    override fun bindViewModel() {
        /// load data local và api
//        lifecycleScope.launch {
//            viewModel.loadLocalData()
//
//        }
    }

}