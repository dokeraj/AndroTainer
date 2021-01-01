package com.dokeraj.androtainer.buttons

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.view.View
import androidx.core.content.ContextCompat
import com.dokeraj.androtainer.R
import kotlinx.android.synthetic.main.login_btn.view.*

class BtnLogin(val ct: Context, val view: View) {
    fun changeBtnState(enable: Boolean) {
        if (enable != view.isClickable) {
            view.isClickable = enable
            if (enable) {
                view.tvLogin.text = "Login"
                view.tvLogin.setTextColor(ContextCompat.getColor(ct, R.color.dis4))
                view.tvLogin.visibility = View.VISIBLE
                view.pbLogin.visibility = View.GONE
                view.clLogin.background.colorFilter =
                    BlendModeColorFilter(ContextCompat.getColor(ct,
                        R.color.blue_main), BlendMode.SRC)
            } else {
                view.tvLogin.visibility = View.GONE
                view.tvLogin.setTextColor(ContextCompat.getColor(ct, R.color.dis6))
                view.pbLogin.visibility = View.VISIBLE
                view.clLogin.background.colorFilter =
                    BlendModeColorFilter(ContextCompat.getColor(ct,
                        R.color.btn_disabled), BlendMode.SRC)
            }
        }
    }
}