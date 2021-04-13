package com.billkalin.justapp.main

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton

class CustomButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = android.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    private val TAG = CustomButton::class.java.simpleName

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "id: $id onTouchEvent: down")
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d(TAG, "id: $id  onTouchEvent: move")
            }
            MotionEvent.ACTION_UP -> {
                Log.d(TAG, "id: $id onTouchEvent: up")
            }
            MotionEvent.ACTION_CANCEL -> {
                Log.d(TAG, "id: $id onTouchEvent: cancel")
            }
        }
        return super.onTouchEvent(event)
    }

}