package unicode.sinhala.keyboard

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView

/**
 * Lightweight key "pop" effects:
 *  - the pressed key scales up with a tiny lift, then springs back with an overshoot (bounce)
 *  - a pink/lavender preview bubble with the key label pops above the key
 *
 * Everything here is cheap: ViewPropertyAnimator only, ONE reusable PopupWindow, no allocation
 * per keystroke beyond the animator itself. Text entry never waits on any of this
 * (the key's click listener is invoked before these effects run).
 */
object KeyPop {

    private val handler = Handler(Looper.getMainLooper())
    private var popup: PopupWindow? = null
    private var bubbleText: TextView? = null
    private var bubbleRoot: FrameLayout? = null
    private var showing = false
    private val overshoot = OvershootInterpolator(2.2f)

    private val dismissRunnable = Runnable { hideNow() }

    /** True when the user turned animations off in Android (animator scale 0) or in our settings. */
    private fun animationsEnabled(context: Context): Boolean {
        if (!Prefs.getKeyAnimation(context)) return false
        val scale = try {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        } catch (t: Throwable) {
            1f
        }
        return scale > 0f
    }

    fun onKeyDown(key: View, label: String?) {
        val ctx = key.context
        if (!animationsEnabled(ctx)) return
        val lift = -2f * key.resources.displayMetrics.density
        key.animate().cancel()
        key.animate()
            .scaleX(1.10f).scaleY(1.10f).translationY(lift)
            .setDuration(55)
            .setInterpolator(null)
            .start()
        if (Prefs.getKeyPreview(ctx) && isPreviewable(label)) showBubble(key, label!!)
    }

    fun onKeyUp(key: View) {
        if (!animationsEnabled(key.context)) {
            key.scaleX = 1f; key.scaleY = 1f; key.translationY = 0f
            return
        }
        key.animate().cancel()
        key.animate()
            .scaleX(1f).scaleY(1f).translationY(0f)
            .setDuration(170)
            .setInterpolator(overshoot)
            .start()
        handler.removeCallbacks(dismissRunnable)
        handler.postDelayed(dismissRunnable, 70)
    }

    /** Release everything (call from the IME's onDestroy / window hidden). */
    fun release() {
        handler.removeCallbacks(dismissRunnable)
        hideNow()
        popup = null
        bubbleText = null
        bubbleRoot = null
    }

    private fun isPreviewable(label: String?): Boolean {
        if (label.isNullOrBlank()) return false
        // letters/digits/punctuation only; skip long function labels
        return label.codePointCount(0, label.length) <= 3
    }

    private fun ensurePopup(ctx: Context) {
        if (popup != null) return
        val d = ctx.resources.displayMetrics.density
        val bg = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(0xFFFFB6D2.toInt(), 0xFFD9B8FF.toInt())
        ).apply {
            cornerRadius = 16 * d
            setStroke((2 * d).toInt(), 0xFFFFFFFF.toInt())
        }
        val text = TextView(ctx).apply {
            gravity = Gravity.CENTER
            textSize = 28f
            setTextColor(0xFF4A1F33.toInt())
            includeFontPadding = false
        }
        val heart = TextView(ctx).apply {
            this.text = "♥"
            textSize = 10f
            setTextColor(0xFFD6457F.toInt())
        }
        val root = FrameLayout(ctx).apply {
            background = bg
            elevation = 8 * d
            addView(text, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            addView(
                heart,
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.END)
                    .apply { topMargin = (3 * d).toInt(); rightMargin = (5 * d).toInt() }
            )
        }
        popup = PopupWindow(root, (52 * d).toInt(), (62 * d).toInt(), false).apply {
            isTouchable = false
            isClippingEnabled = false
            setBackgroundDrawable(null)
        }
        bubbleText = text
        bubbleRoot = root
    }

    private fun showBubble(key: View, label: String) {
        val ctx = key.context
        ensurePopup(ctx)
        val pw = popup ?: return
        val root = bubbleRoot ?: return
        handler.removeCallbacks(dismissRunnable)
        bubbleText?.text = label
        val d = ctx.resources.displayMetrics.density
        val w = (52 * d).toInt()
        val h = (62 * d).toInt()
        val loc = IntArray(2)
        key.getLocationInWindow(loc)
        var x = loc[0] + key.width / 2 - w / 2
        val y = loc[1] - h - (4 * d).toInt()
        x = x.coerceIn(0, (ctx.resources.displayMetrics.widthPixels - w).coerceAtLeast(0))
        try {
            if (showing) {
                pw.update(x, y, w, h)
            } else {
                pw.showAtLocation(key.rootView, Gravity.NO_GRAVITY, x, y)
                showing = true
            }
            root.pivotX = w / 2f
            root.pivotY = h.toFloat()
            root.animate().cancel()
            root.scaleX = 0.6f; root.scaleY = 0.6f
            root.animate().scaleX(1f).scaleY(1f).setDuration(110).setInterpolator(overshoot).start()
        } catch (t: Throwable) {
            // Window token not ready (e.g. during rotation). Typing must never be affected.
            showing = false
        }
    }

    private fun hideNow() {
        val pw = popup ?: return
        if (showing) {
            try { pw.dismiss() } catch (_: Throwable) {}
            showing = false
        }
    }
}
