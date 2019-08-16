package uz.jamshid.library.progress_bar

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import uz.jamshid.library.IGRefreshLayout

class LineProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseProgressBar(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var backColor = Color.LTGRAY
    private var frontColor = Color.GRAY
    private val backPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var borderWidth = dp2px(4).toFloat()
    private var left = dp2px(10).toFloat()
    private var top = dp2px(60).toFloat()
    private var isLoaded = false

    private var progressAnimator: ValueAnimator? = null

    private var loadingPercent = 0f

    init {
        paint.color = frontColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth
        backPaint.color = backColor
        backPaint.style = Paint.Style.STROKE
        backPaint.strokeWidth = borderWidth
    }

    fun setLeftAndTop(left: Int, top: Int){
        this.left = dp2px(left).toFloat()
        this.top = dp2px(top).toFloat()
    }

    fun setBorderWidth(width: Int){
        paint.strokeWidth = dp2px(width).toFloat()
        backPaint.strokeWidth = dp2px(width).toFloat()
    }

    fun setColors(backColor: Int, frontColor: Int){
        paint.color = frontColor
        backPaint.color = backColor
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        top = (mParent.DRAG_MAX_DISTANCE/2).toFloat()
        var currentPercent = (width - left)*(mPercent/100)
        val lineWidth = width - left
        if(currentPercent<=left)
            currentPercent = left

        canvas?.drawLine(left, top, lineWidth, top, backPaint)

        if(isLoading){
            if(isLoaded)
                canvas?.drawLine(left, top, loadingPercent, top, paint)
            else
                canvas?.drawLine(loadingPercent, top, lineWidth, top, paint)
        }else{
            canvas?.drawLine(left, top, currentPercent, top, paint)
        }
    }

    override fun setPercent(percent: Float) {
        mPercent = if (percent >= 100f) 100f else percent
        invalidate()
    }

    override fun setParent(parent: IGRefreshLayout) {
        mParent = parent
    }

    override fun start() {
        isLoading = true
        mPercent = 0f
        resetAnimation()
    }

    override fun stop() {
        mPercent = 0f
        stopAnimation()
    }


    private fun resetAnimation(){
        if(progressAnimator != null && progressAnimator!!.isRunning)
            progressAnimator?.cancel()

        progressAnimator = ValueAnimator.ofFloat(left, width.toFloat())
        progressAnimator?.duration = 500
        progressAnimator?.interpolator = LinearInterpolator()
        progressAnimator?.addUpdateListener {
            loadingPercent = it.animatedValue as Float
            invalidate()
        }
        progressAnimator?.start()
        progressAnimator?.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                isLoaded = !isLoaded
                resetAnimation()
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationStart(p0: Animator?) {

            }

        })
    }

    private fun stopAnimation(){
        isLoading = false
        isLoaded = false
        if(progressAnimator != null) {
            progressAnimator?.cancel()
            progressAnimator?.removeAllListeners()
            progressAnimator = null
        }
    }

}