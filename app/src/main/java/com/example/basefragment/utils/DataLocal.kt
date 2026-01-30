package com.example.basefragment.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.basefragment.R
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
        IntroModel("1",R.drawable.img_intro1, R.string.title_1),
        IntroModel("2",R.drawable.img_intro2, R.string.title_2),
        IntroModel("3",R.drawable.img_intro3, R.string.title_3)
    )

}