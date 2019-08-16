package uz.jamshid.library

import android.content.Context
import android.util.AttributeSet
import android.view.View

abstract class BaseProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    lateinit var mParent: IGRefreshLayout
    var mPercent = 0f
    var isLoading = false

    abstract fun setPercent(percent: Float)
    abstract fun setParent(parent: IGRefreshLayout)
    abstract fun start()
    abstract fun stop()
}