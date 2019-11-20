package com.improve.latetrain

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import com.improve.latetrain.activities.DrawerActivity
import kotlinx.android.synthetic.main.content_drawer.*

class CustomEditTextKeyboardEvent: EditText {

    constructor(context: Context):super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if(focused){
            (context as DrawerActivity).live_bar?.animate()
                ?.translationY(0f)
                ?.setListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        if((context as DrawerActivity).live_bar?.visibility==View.VISIBLE)
                          (context as DrawerActivity).live_bar?.visibility = View.GONE
                    }
                })
            (context as DrawerActivity).bottom_nav_view?.visibility = View.GONE
        }
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK){
            (context as DrawerActivity).bottom_nav_view?.visibility = View.VISIBLE
            (context as DrawerActivity).live_bar?.visibility = View.VISIBLE
            return super.dispatchKeyEvent(event)
        }
        return super.dispatchKeyEvent(event)
    }
}