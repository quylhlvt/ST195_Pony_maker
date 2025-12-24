package com.example.basefragment.custom.listener.listenerdraw

import android.view.MotionEvent
import com.example.basefragment.custom.DrawView


interface DrawEvent {
    fun onActionDown(tattooView: DrawView?, event: MotionEvent?)
    fun onActionMove(tattooView: DrawView?, event: MotionEvent?)
    fun onActionUp(tattooView: DrawView?, event: MotionEvent?)
}