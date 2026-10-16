
package unicode.sinhala.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity.CENTER
import android.view.MotionEvent
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import unicode.sinhala.com.R
import android.util.TypedValue

class KeyboardButton : AppCompatTextView {
    private var isSpecial = false
    private var secondaryLabel: String? = null
    private val secondaryLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    var clickListener: (tag: String) -> Unit = { }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        gravity = CENTER
        isClickable = true

        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.KeyboardButton, defStyleAttr, 0)
            isSpecial = typedArray.getBoolean(R.styleable.KeyboardButton_isSpecial, false)
            typedArray.recycle()
        }

        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.foreground, typedValue, true)
        setTextColor(typedValue.data)

        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.isPressed = true
                    // Prefer visible text (what user sees). Fallback to tag only when text is empty.
                    val visible = text?.toString()?.takeIf { it.isNotEmpty() }
                    val rawTag = tag?.toString()?.takeIf { it.isNotEmpty() } ?: ""
                    val tagString = visible ?: convertTagToText(rawTag)
                    clickListener.invoke(tagString)
                    // Visual effects run AFTER the character was sent, so they can never delay typing.
                    KeyPop.onKeyDown(view, visible)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.isPressed = false
                    KeyPop.onKeyUp(view)
                    true
                }
                else -> false
            }
        }

        if (background == null) {
            // Use AppCompatResources for theme-aware drawable inflation
            background = AppCompatResources.getDrawable(context, R.drawable.key_background)
        }

        if (isSpecial) {
            background = AppCompatResources.getDrawable(context, R.drawable.key_background_special)
        }

        secondaryLabelPaint.color = currentTextColor
        secondaryLabelPaint.alpha = 150 // Semi-transparent
        secondaryLabelPaint.textAlign = Paint.Align.RIGHT
        secondaryLabelPaint.textSize = textSize * 0.5f
    }

    private fun convertTagToText(raw: String): String {
        if (raw.isEmpty()) return ""
        // If tag is a number like "32", interpret as Unicode code point or ASCII
        val digitsOnly = raw.all { it.isDigit() }
        if (digitsOnly) {
            return try {
                val code = raw.toInt()
                when (code) {
                    32 -> " "
                    else -> code.toChar().toString()
                }
            } catch (t: Throwable) {
                raw
            }
        }
        return raw
    }

    fun setSecondaryLabel(label: String?) {
        secondaryLabel = label
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        secondaryLabel?.let {
            secondaryLabelPaint.color = currentTextColor
            secondaryLabelPaint.alpha = 150
            secondaryLabelPaint.textSize = textSize * 0.5f
            
            val padding = 8f
            canvas.drawText(it, width.toFloat() - padding, secondaryLabelPaint.textSize + padding, secondaryLabelPaint)
        }
    }
}
