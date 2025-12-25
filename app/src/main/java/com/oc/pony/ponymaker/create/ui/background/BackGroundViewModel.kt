package com.oc.pony.ponymaker.create.ui.background

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date

class BackGroundViewModel : ViewModel() {
    var backgroundImageList: ArrayList<com.oc.pony.ponymaker.create.data.model.SelectedModel> = arrayListOf()
    var backgroundColorList: ArrayList<com.oc.pony.ponymaker.create.data.model.SelectedModel> = arrayListOf()
    var stickerList: ArrayList<com.oc.pony.ponymaker.create.data.model.SelectedModel> = arrayListOf()
    var speechList: ArrayList<com.oc.pony.ponymaker.create.data.model.SelectedModel> = arrayListOf()
    var textFontList: ArrayList<com.oc.pony.ponymaker.create.data.model.SelectedModel> = arrayListOf()
    var textColorList: ArrayList<com.oc.pony.ponymaker.create.data.model.SelectedModel> = arrayListOf()

    private val _typeNavigation = MutableStateFlow<Int>(-1)
    val typeNavigation = _typeNavigation.asStateFlow()

    private val _typeBackground = MutableStateFlow<Int>(-1)
    val typeBackground = _typeBackground.asStateFlow()

    private val _isFocusEditText = MutableStateFlow<Boolean>(false)
    val isFocusEditText = _isFocusEditText.asStateFlow()

    var currentDraw: com.oc.pony.ponymaker.create.custom.Draw? = null

    var drawViewList: ArrayList<com.oc.pony.ponymaker.create.custom.Draw> = arrayListOf()

    lateinit var layoutParams: ViewGroup.MarginLayoutParams

    var originalMarginBottom: Int = 0

    var pathDefault = ""

    fun setTypeNavigation(type: Int) {
        _typeNavigation.value = type
    }

    fun setTypeBackground(type: Int) {
        _typeBackground.value = type
    }

    fun setIsFocusEditText(status: Boolean) {
        _isFocusEditText.value = status
    }

    suspend fun loadDataDefault(context: Context) {
        backgroundImageList.clear()
        backgroundImageList.addAll(
            _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBg.map {
                _root_ide_package_.com.oc.pony.ponymaker.create.data.model.SelectedModel(
                    path = it
                )
            }
           )

        backgroundColorList.clear()
        backgroundColorList.addAll(
            _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.getBackgroundColorDefault(
                context
            )
        )


        stickerList.clear()
        stickerList.addAll(
            _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrStiker.map {
                _root_ide_package_.com.oc.pony.ponymaker.create.data.model.SelectedModel(
                    path = it
                )
            })


        speechList.clear()
        speechList.addAll(
            _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBgText.map {
                _root_ide_package_.com.oc.pony.ponymaker.create.data.model.SelectedModel(
                    path = it
                )
            })

        textFontList.clear()
        textFontList.addAll(_root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.getTextFontDefault())
        textFontList.first().isSelected = true

        textColorList.clear()
        textColorList.addAll(
            _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.getTextColorDefault(
                context
            )
        )
        textColorList[1].isSelected = true
    }

    suspend fun updateBackgroundImageSelected(position: Int) {
        backgroundColorList = backgroundColorList.map { it.copy(isSelected = false) }.toCollection(ArrayList())
        backgroundImageList.forEachIndexed { index, model ->
            model.isSelected = index == position
        }
    }

    suspend fun updateBackgroundColorSelected(position: Int) {
        backgroundImageList = backgroundImageList.map { it.copy(isSelected = false) }.toCollection(ArrayList())
        backgroundColorList.forEachIndexed { index, model ->
            model.isSelected = index == position
        }
    }

    fun updateTextFontSelected(position: Int) {
        textFontList = textFontList.map { it.copy(isSelected = false) }.toCollection(ArrayList())
        textFontList.forEachIndexed { index, model ->
            model.isSelected = index == position
        }
    }

    fun updateTextColorSelected(position: Int) {
        textColorList = textColorList.map { it.copy(isSelected = false) }.toCollection(ArrayList())
        textColorList.forEachIndexed { index, model ->
            model.isSelected = index == position
        }
    }

    fun updateCurrentCurrentDraw(draw: com.oc.pony.ponymaker.create.custom.Draw) {
        currentDraw = draw
    }

    fun addDrawView(draw: com.oc.pony.ponymaker.create.custom.Draw) {
        drawViewList.add(draw)
    }

    fun deleteDrawView(draw: com.oc.pony.ponymaker.create.custom.Draw) {
        drawViewList.removeIf { it == draw }
    }

    fun updatePathDefault(path: String){
        pathDefault = path
    }
    fun loadDrawableEmoji(context: Context, bitmap: Bitmap, isCharacter: Boolean = false, isText: Boolean = false): com.oc.pony.ponymaker.create.custom.DrawableDraw {
        val drawable = bitmap.toDrawable(context.resources)
        val drawableEmoji = _root_ide_package_.com.oc.pony.ponymaker.create.custom.DrawableDraw(
            drawable,
            "${SimpleDateFormat("dd_MM_yyyy_hh_mm_ss").format(Date())}.png"
        )
        drawableEmoji.isCharacter = isCharacter
        drawableEmoji.isText = isText
        return drawableEmoji
    }

    fun resetDraw() {
        drawViewList.clear()

    }
}