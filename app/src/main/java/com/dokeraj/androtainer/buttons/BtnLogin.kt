package com.dokeraj.androtainer.buttons

import android.content.Context
import android.graphics.drawable.TransitionDrawable
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.dokeraj.androtainer.R
import kotlinx.android.synthetic.main.login_btn.view.*

class BtnLogin(val ct: Context, val view: View) {
    fun changeBtnState(enable: Boolean) {
        val fadeIn = AnimationUtils.loadAnimation(ct, R.anim.fade_in_btn)
        val colorTransition = view.clLogin.background as TransitionDrawable

        if (enable != view.isClickable) {
            view.isClickable = enable
            if (enable) {
                view.tvLogin.setTextColor(ContextCompat.getColor(ct, R.color.dis4))
                view.tvLogin.visibility = View.VISIBLE
                view.pbLogin.visibility = View.GONE
                colorTransition.reverseTransition(200)
            } else {
                view.pbLogin.animation = fadeIn
                colorTransition.startTransition(200)
                view.tvLogin.visibility = View.GONE
                view.tvLogin.setTextColor(ContextCompat.getColor(ct, R.color.dis6))
                view.pbLogin.visibility = View.VISIBLE
            }
        }
    }
}