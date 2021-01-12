package com.dokeraj.androtainer.buttons

import android.content.Context
import android.graphics.drawable.TransitionDrawable
import android.view.View
import android.view.animation.AnimationUtils
import com.dokeraj.androtainer.R
import kotlinx.android.synthetic.main.delete_docker_container_btn.view.*

class BtnDeleteContainer(val ct: Context, val view: View) {
    fun changeBtnState(enable: Boolean) {
        val fadeIn = AnimationUtils.loadAnimation(ct, R.anim.fade_in_btn)
        val colorTransition = view.clContainerDelete.background as TransitionDrawable

        if (enable != view.isClickable) {
            view.isClickable = enable
            if (enable) {
                view.tvContainerDelete.visibility = View.VISIBLE
                view.pbContainerDelete.visibility = View.GONE
                view.ivTrashContainerDelete.visibility = View.VISIBLE
                colorTransition.reverseTransition(200)
            } else {
                view.pbContainerDelete.animation = fadeIn
                colorTransition.startTransition(200)
                view.tvContainerDelete.visibility = View.GONE
                view.pbContainerDelete.visibility = View.VISIBLE
                view.ivTrashContainerDelete.visibility = View.GONE
            }
        }
    }
}