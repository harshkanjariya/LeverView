package com.harsh.leverview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin


class LeverView : View {

    private lateinit var textPaint: TextPaint
    private var labelTextSize: Float = 50f
    private var currentValue: Float = 0.0f
    private var labels: List<String> = listOf("First", "Second", "Third")
    private var onStateChange: ((String, Int) -> Unit)? = null

    private var img: Drawable? = null

    constructor(context: Context) : super(context) {
        img = ContextCompat.getDrawable(context, R.drawable.lever)
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.LiverView, defStyle, 0
        )

        if (a.hasValue(R.styleable.LiverView_img)) {
            img = a.getDrawable(
                R.styleable.LiverView_img
            )
            img?.callback = this
        }

        if (a.hasValue(R.styleable.LiverView_labelTextSize)) {
            labelTextSize = a.getFloat(R.styleable.LiverView_labelTextSize, 50.0f)
        }

        if (a.hasValue(R.styleable.LiverView_currentValue)) {
            currentValue = a.getFloat(R.styleable.LiverView_currentValue, 0.0f)
        }

        if (a.hasValue(R.styleable.LiverView_currentValue)) {
            currentValue = a.getFloat(R.styleable.LiverView_currentValue, 0.0f)
        }

        if (a.hasValue(R.styleable.LiverView_labels)) {
            labels = a.getString(R.styleable.LiverView_labels)?.split(",")!!
        }

        a.recycle()

        // TextPaint setup
        textPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.RIGHT
        }

        invalidateTextPaintAndMeasurements()
    }

    private fun invalidateTextPaintAndMeasurements() {
        textPaint.let {
            it.color = Color.BLACK
            it.textSize = this.labelTextSize
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = paddingLeft
        val paddingTop = paddingTop + 400
        val paddingRight = paddingRight + 300
        val paddingBottom = paddingBottom + 400

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        canvas.save()
        canvas.translate(0f, height / 2.0f)
        canvas.rotate(currentValue + 0.0f)
        img?.let {
            it.setBounds(
                paddingLeft, paddingTop - height / 2,
                paddingLeft + contentWidth, paddingTop + contentHeight - height / 2
            )
            it.draw(canvas)
        }
        canvas.restore()

        val radius = (width - 100.0f) * 2
        val initY = height / 2.0f
        val angles = listOf(Math.PI / 15, 0.0, -Math.PI / 15)

        angles.forEachIndexed { index, element ->
            canvas.drawText(
                labels[index],
                (radius * cos(element)).toFloat() - radius / 2,
                initY + (radius * sin(element)).toFloat(), textPaint
            )
        }
    }

    private fun animateToRoundValue() {
        val roundValues = listOf(25.0f, 0.0f, -25.0f)
        var finalVal: Float? = null
        var finalIndex: Int = -1
        if (currentValue > roundValues[0]) {
            finalVal = roundValues[0]
            finalIndex = 0
        } else if (currentValue < roundValues[roundValues.size - 1]) {
            finalVal = roundValues[roundValues.size - 1]
            finalIndex = 2
        } else {
            for (i in 1 until roundValues.size) {
                val left = roundValues[i - 1]
                val right = roundValues[i]
                if (left > currentValue && currentValue > right) {
                    if (currentValue < (right + left) / 2) {
                        finalIndex = i - 1
                        finalVal = left
                    } else {
                        finalIndex = i
                        finalVal = right
                    }
                }
            }
        }
        if (finalVal != null) {
            val animator = ValueAnimator.ofFloat(currentValue, finalVal)
            animator.addUpdateListener {
                currentValue = it.animatedValue as Float
                invalidate()
            }
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (onStateChange != null) {
                        onStateChange?.let { it(labels[finalIndex], finalIndex) }
                    }
                }
            })
            animator.start()
        }
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private var movementLastX = -1.0f
    private var movementLastY = -1.0f
    private var movementFirstX = -1.0f
    private var movementFirstY = -1.0f
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                movementFirstX = event.x
                movementLastX = event.x

                movementFirstY = event.y
                movementLastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                movementLastX = event.x
                movementLastY = event.y

                val diff = (event.y - movementFirstY)
                val radius = (width - 100.0f) * 2
                currentValue += (asin(diff / radius) * 3)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                animateToRoundValue()
                if (abs(event.y - movementFirstY) < 5) {
                    performClick()
                }
            }
            else -> {}
        }
        return true
    }

    fun setOnStateChangeListener(listener: (String, Int) -> Unit) {
        onStateChange = listener
    }
}