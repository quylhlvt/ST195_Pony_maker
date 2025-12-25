package com.oc.pony.ponymaker.create.custom.listener.listenerdraw

import android.view.MotionEvent
import com.oc.pony.ponymaker.create.custom.DrawKey
import com.oc.pony.ponymaker.create.custom.DrawView


class FlipEvent : DrawEvent {
    override fun onActionDown(tattooView: DrawView?, event: MotionEvent?) {}
    override fun onActionMove(tattooView: DrawView?, event: MotionEvent?) {}
    override fun onActionUp(tattooView: DrawView?, event: MotionEvent?) {
        if (tattooView != null && tattooView.getStickerCount() > 0) tattooView.flipCurrentDraw(
            DrawKey.FLIP_HORIZONTALLY)
    }
}