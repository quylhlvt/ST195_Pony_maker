package com.example.basefragment.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.basefragment.R
import com.example.basefragment.data.model.color.SelectedModel
import com.example.basefragment.data.model.intro.IntroModel
import com.example.basefragment.data.model.language.LanguageModel
import com.facebook.shimmer.Shimmer

object DataLocal {
     val KEY_LAST_CLICK_TIME = -101
    val shimmer =
        Shimmer.AlphaHighlightBuilder().setDuration(1800).setBaseAlpha(0.7f).setHighlightAlpha(0.6f)
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT).setAutoStart(true).build()

    fun getLanguageList(): ArrayList<LanguageModel> {
        return arrayListOf(
            LanguageModel("hi", "Hindi", R.drawable.ic_flag_hindi),
            LanguageModel("es", "Spanish", R.drawable.ic_flag_spanish),
            LanguageModel("fr", "French", R.drawable.ic_flag_french),
            LanguageModel("en", "English", R.drawable.ic_flag_english),
            LanguageModel("pt", "Portuguese", R.drawable.ic_flag_portugeese),
            LanguageModel("in", "Indonesian", R.drawable.ic_flag_indo),
            LanguageModel("de", "German", R.drawable.ic_flag_germani),
        )
    }

    val itemIntroList = listOf(
        IntroModel(R.drawable.img_intro1, R.string.title_1),
        IntroModel(R.drawable.img_intro2, R.string.title_2),
        IntroModel(R.drawable.img_intro3, R.string.title_3)
    )

    fun getBackgroundColorDefault(context: Context): ArrayList<SelectedModel> {
        return arrayListOf(
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_1)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_2)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_3)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_4)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_5)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_6)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_7)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_8)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_9)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_10)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_11)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_12)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_13)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_14)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_15)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_16)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_17)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_18)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_19)),
        )
    }

//    val bottomNavigationNotSelect = arrayListOf(
//        R.drawable.ic_background,
//        R.drawable.ic_sticker,
//        R.drawable.ic_speech,
//        R.drawable.ic_text,
//    )
//
//    val bottomNavigationSelected = arrayListOf(
//        R.drawable.ic_background_selected,
//        R.drawable.ic_sticker_selected,
//        R.drawable.ic_speech_selected,
//        R.drawable.ic_text_selected,
//    )

//    fun getTextFontDefault(): ArrayList<SelectedModel> {
//        return arrayListOf(
//            SelectedModel(color = R.font.itim_regular),
//            SelectedModel(color = R.font.italiana_regular),
//            SelectedModel(color = R.font.kranky_regular),
//            SelectedModel(color = R.font.damion_regular),
//            SelectedModel(color = R.font.dynalight_regular),
//            SelectedModel(color = R.font.js_math_cmmi),
//            SelectedModel(color = R.font.mysteryquest_egular),
//            SelectedModel(color = R.font.baloo_regular),
//            SelectedModel(color = R.font.bubblegum_sans_regular),
//            SelectedModel(color = R.font.cherry_bomb_one_regular),
//            SelectedModel(color = R.font.cutive_mono_egular),
//            SelectedModel(color = R.font.croissant_one_regular)
//        )
//    }

    fun getTextColorDefault(context: Context): ArrayList<SelectedModel> {
        return arrayListOf(
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_9)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.black)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.white)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_19)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_2)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_3)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_4)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_5)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_6)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_7)),
            SelectedModel(color = ContextCompat.getColor(context, R.color.color_8))
        )
    }
}