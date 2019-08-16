package uz.jamshid.igrefreshlayout

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import uz.jamshid.library.IGRefreshLayout
import uz.jamshid.library.progress_bar.BaseProgressBar


class Circle @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseProgressBar(context, attrs, defStyleAttr) {

    var bar = ImageView(context)
    init {
        bar.setImageResource(R.drawable.ic_smile)
        bar.alpha = 0f
    }

    override fun setParent(parent: IGRefreshLayout) {
        mParent = parent
        setUpView()
    }

    override fun setPercent(percent: Float) {
        mPercent = percent
        bar.alpha = percent/100
    }

    private fun setUpView(){
        mParent.setCustomView(bar, dp2px(80), dp2px(80))
    }

    override fun start() {
        val animation1 = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        animation1.duration = 500
        animation1.repeatCount = Animation.INFINITE
        bar.startAnimation(animation1)
    }

    override fun stop() {
        bar.alpha = 0f
        bar.clearAnimation()
    }
}