package uz.jamshid.library

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.*
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper.INVALID_POINTER
import java.lang.Exception
import kotlin.math.*


class IGRefreshLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val DRAG_RATE = .5f
    var DRAG_MAX_DISTANCE = 120
    private val MAX_OFFSET_ANIMATION_DURATION = 700
    private val DECELERATE_INTERPOLATION_FACTOR = 2f

    var mDecelerateInterpolator: Interpolator?=null
    private var mTarget: View? = null
    private var mTargetPaddingTop: Int = 0
    private var mTargetPaddingBottom: Int = 0
    private var mTargetPaddingRight: Int = 0
    private var mTargetPaddingLeft: Int = 0
    private var mRefreshing = false
    private var mNotify = false
    private var mCurrentOffsetTop = 0
    private var mActivePointerId = 0
    private var mIsBeingDragged = false
    private var mTouchSlop = 0
    private var mCurrentDragPercent = 0f
    private var mTotalDragDistance = 0
    private var mInitialMotionY = 0
    private var mFrom: Int = 0
    private var mFromDragPercent = 0f

    private var mBar: BaseProgressBar = CircleProgressBar(context)
    private var callback: InstaRefreshCallback?=null
    private var customViewSet = false

    init {
        mDecelerateInterpolator = DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR)
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

        setRefreshing(false)
        setupAttributes(attrs)
        mTotalDragDistance = dp2px(DRAG_MAX_DISTANCE)

        setWillNotDraw(false)
        ViewCompat.setChildrenDrawingOrderEnabled(this, true)
    }

    private fun setupAttributes(attrs: AttributeSet?){
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.IGRefreshLayout,
            0, 0)

        DRAG_MAX_DISTANCE = typedArray.getInt(R.styleable.IGRefreshLayout_offsetTop, 120)

        if(!typedArray.getBoolean(R.styleable.IGRefreshLayout_customBar, false)){
            setDefaultBar()
            customViewSet = true
        }
    }

    private fun setDefaultBar(){
        mBar.setParent(this)
        addView(mBar)
    }

    fun setCustomBar(bar: BaseProgressBar){
        if(customViewSet)
            throw Exception("ViewGroup can contain only one customBar")
        customViewSet = true
        mBar = bar
        mBar.setParent(this)
        addView(mBar, 0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        ensureTarget()
        if(!customViewSet)
            throw Exception("Custom View has not been initialized")
        if (mTarget == null)
            return

        val width = MeasureSpec.makeMeasureSpec(measuredWidth - paddingRight - paddingLeft, MeasureSpec.EXACTLY)
        val height = MeasureSpec.makeMeasureSpec(measuredHeight - paddingTop - paddingBottom, MeasureSpec.EXACTLY)

        mTarget?.measure(width, height)
        mBar.measure(width, height)
    }

    private fun ensureTarget(){
        if (mTarget != null)
            return

        if (childCount > 0){
            for(i in 0 until childCount){
                val child = getChildAt(i)
                if(child != mBar) {
                    mTarget = child
                    if(mTarget?.background == null)
                        mTarget?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                    mTargetPaddingTop = mTarget?.paddingTop!!
                    mTargetPaddingBottom = mTarget?.paddingBottom!!
                    mTargetPaddingRight = mTarget?.paddingRight!!
                    mTargetPaddingLeft = mTarget?.paddingLeft!!
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {

        if(!isEnabled||canChildScrollUp()||mRefreshing){
            return false
        }

        when(ev?.actionMasked){
            MotionEvent.ACTION_DOWN -> {
                setTargetOffsetTop(0)
                mActivePointerId = ev.getPointerId(0)
                mIsBeingDragged = false
                val initialMotionY = getMotionEventY(ev, mActivePointerId)
                if (initialMotionY == -1) {
                    return false
                }
                mInitialMotionY = initialMotionY
            }
            MotionEvent.ACTION_MOVE -> {
                if (mActivePointerId == INVALID_POINTER) {
                    return false
                }

                val y = getMotionEventY(ev, mActivePointerId)
                if (y == -1) {
                    return false
                }

                val yDiff = y - mInitialMotionY;
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    mIsBeingDragged = true
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
            }
        }
        return mIsBeingDragged
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {

        if (!mIsBeingDragged) {
            return super.onTouchEvent(ev)
        }

        when(ev?.actionMasked){
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = ev.findPointerIndex(mActivePointerId)
                if(pointerIndex < 0){
                    return false
                }
                val y = ev.getY(pointerIndex)
                val yDiff = y - mInitialMotionY
                val scrollTop = yDiff * DRAG_RATE
                mCurrentDragPercent = scrollTop / mTotalDragDistance
                if (mCurrentDragPercent < 0) {
                    return false
                }
                val boundedDragPercent = min(1f, abs(mCurrentDragPercent))
                val extraOS = abs(scrollTop) - mTotalDragDistance
                val slingshotDist = mTotalDragDistance.toFloat()
                val tensionSlingshotPercent = max(0f, min(extraOS, slingshotDist * 2) / slingshotDist)
                val tensionPercent = ((tensionSlingshotPercent / 4) - (tensionSlingshotPercent / 4).pow(2))*2f
                val extraMove = slingshotDist * tensionPercent / 2
                val targetY = (slingshotDist * boundedDragPercent + extraMove).toInt()

                val offsetScrollTop = scrollTop - (mTotalDragDistance/2)
                if(offsetScrollTop>0) {
                    mBar.setPercent(200 * offsetScrollTop/mTotalDragDistance)
                    mCurrentDragPercent = offsetScrollTop/mTotalDragDistance * 2
                }
                setTargetOffsetTop(targetY - mCurrentOffsetTop)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = ev.actionIndex
                mActivePointerId = ev.getPointerId(index)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
            }
            MotionEvent.ACTION_UP -> {
                if (mActivePointerId == INVALID_POINTER) {
                    return false
                }
                val pointerIndex = ev.findPointerIndex(mActivePointerId)
                val y = ev.getY(pointerIndex)
                val overScrollTop = (y - mInitialMotionY) * DRAG_RATE
                mIsBeingDragged = false
                if (overScrollTop > mTotalDragDistance) {
                    setRefreshing(true, true)
                }else {
                    mRefreshing = false
                    animateOffsetToStartPosition()
                }
                mActivePointerId = INVALID_POINTER
                return false
            }

        }

        return true
    }

    private fun canChildScrollUp(): Boolean{
        return ViewCompat.canScrollVertically(mTarget, -1)
    }

    private fun setTargetOffsetTop(offset: Int){
        mTarget?.offsetTopAndBottom(offset)
        mCurrentOffsetTop = mTarget?.top!!
    }

    private fun getMotionEventY(ev: MotionEvent, activePointerId: Int): Int{
        val index = ev.findPointerIndex(activePointerId)
        if(index < 0)
            return -1

        return ev.getY(index).toInt()
    }

    private fun onSecondaryPointerUp(ev: MotionEvent){
        val pointerIndex = ev.actionIndex
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    fun dp2px(dp: Int): Int{
        val density = context.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }


    private fun moveToStart(interpolatedTime: Float) {
        val targetTop = mFrom - (mFrom * interpolatedTime).toInt()
        val targetPercent = mFromDragPercent * (1.0f - interpolatedTime)
        val offset = targetTop - mTarget?.top!!
        mCurrentDragPercent = targetPercent
        mBar.setPercent(100*mCurrentDragPercent)
        mTarget?.setPadding(mTargetPaddingLeft, mTargetPaddingTop, mTargetPaddingRight, mTargetPaddingBottom + targetTop)
        setTargetOffsetTop(offset)
    }

    fun setRefreshing(refreshing: Boolean) {
        if (mRefreshing != refreshing) {
            setRefreshing(refreshing, false /* notify */)
        }
    }

    private fun setRefreshing(refreshing: Boolean, notify: Boolean) {
        if (mRefreshing != refreshing) {
            mNotify = notify
            ensureTarget()
            mRefreshing = refreshing
            if (mRefreshing) {
                mBar.setPercent(1f)
                animateOffsetToCorrectPosition()
            } else {
                animateOffsetToStartPosition()
            }
        }
    }

    private fun animateOffsetToStartPosition() {
        mFrom = mCurrentOffsetTop
        mFromDragPercent = mCurrentDragPercent
        val animationDuration = abs((MAX_OFFSET_ANIMATION_DURATION * mFromDragPercent).toLong())

        mAnimateToStartPosition.reset()
        mAnimateToStartPosition.duration = animationDuration
        mAnimateToStartPosition.interpolator = mDecelerateInterpolator
        mAnimateToStartPosition.setAnimationListener(mToStartListener)
        mBar.stop()
        mBar.clearAnimation()
        mBar.startAnimation(mAnimateToStartPosition)
    }

    private fun animateOffsetToCorrectPosition() {
        mFrom = mCurrentOffsetTop
        mFromDragPercent = mCurrentDragPercent

        mAnimateToCorrectPosition.reset()
        mAnimateToCorrectPosition.duration = MAX_OFFSET_ANIMATION_DURATION.toLong()
        mAnimateToCorrectPosition.interpolator = mDecelerateInterpolator

        mBar.clearAnimation()
        mBar.startAnimation(mAnimateToCorrectPosition)
        if (mRefreshing) {
            mBar.start()
            if (mNotify) {
                callback?.onRefresh()
            }
        } else {
            mBar.stop()
            animateOffsetToStartPosition()
        }
        mCurrentOffsetTop = mTarget?.top!!
        mTarget?.setPadding(mTargetPaddingLeft, mTargetPaddingTop, mTargetPaddingRight, mTotalDragDistance)
    }

    private val mAnimateToStartPosition = object : Animation() {
        public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            moveToStart(interpolatedTime)
        }
    }

    private val mAnimateToCorrectPosition = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            val targetTop: Int
            val endTarget = mTotalDragDistance
            targetTop = mFrom + ((endTarget - mFrom) * interpolatedTime).toInt()
            val offset = targetTop - mTarget?.top!!

            mCurrentDragPercent = mFromDragPercent - (mFromDragPercent - 1.0f) * interpolatedTime
            mBar.setPercent(100*mCurrentDragPercent)

            setTargetOffsetTop(offset)
        }
    }

    private val mToStartListener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {}

        override fun onAnimationRepeat(animation: Animation) {}

        override fun onAnimationEnd(animation: Animation) {
            mBar.stop()
            mCurrentOffsetTop = mTarget?.top!!
        }
    }

    fun getTotalDragDistance(): Int {
        return mTotalDragDistance
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        ensureTarget()
        if (mTarget == null)
            return

        val height = measuredHeight
        val width = measuredWidth
        val left = paddingLeft
        val top = paddingTop
        val right = paddingRight
        val bottom = paddingBottom

        mTarget?.layout(left, top + mCurrentOffsetTop, left + width - right, top + height - bottom + mCurrentOffsetTop)
        mBar.layout(left, top, left+width-right, top+height-bottom)
    }

    fun setRefreshListener(action:()->Unit){
        this.callback = object : InstaRefreshCallback{
            override fun onRefresh() = action()
        }
    }

    fun setRefreshListener(callback: InstaRefreshCallback){
        this.callback = callback
    }

    interface InstaRefreshCallback{
        fun onRefresh()
    }
}