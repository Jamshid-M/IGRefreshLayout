# IGRefreshLayout

[![](https://jitpack.io/v/Jamshid-M/IGRefreshLayout.svg)](https://jitpack.io/#Jamshid-M/IGRefreshLayout)


## Instagram like refresh layout

###It is similar to pull to refresh layout, which has in instagram. </br>
###Main idea was taken from https://github.com/Yalantis/Phoenix and behaviour of layout was changed.


![Alt Text](https://github.com/Jamshid-M/IGRefreshLayout/blob/master/gif/cbp.gif)
![Alt Text](https://github.com/Jamshid-M/IGRefreshLayout/blob/master/gif/lbp.gif)

## Usage

For a working implementation check out source from sample directory
```
dependencies {
	implementation 'com.github.Jamshid-M:IGRefreshLayout:1.0.2'
}
```

Include IGRefreshLayout in your xml and put inside ListView or Recyclerview

```
<uz.jamshid.library.IGRefreshLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools" 
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:id="@+id/swipe"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:offsetTop="120"
        app:customBar="true"
        tools:context=".MainActivity">

    <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/rv"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
    />

</uz.jamshid.library.IGRefreshLayout>
```

Open activity and specify IGRefreshLayout object and setup InstaRefreshCallback

```
        var swipe = findViewById<IGRefreshLayout>(R.id.swipe)
        swipe.setRefreshListener {
            Handler().postDelayed({
                swipe.setRefreshing(false)
            }, 3000)
        }
```

You can use lambda callback or callback through object

Enabling and disabling refreshing state
```
swipe.setRefreshing(false)
```

Using custom views
```
swipe.setCustomBar(CircleProgressBar(this))
```
or
```
swipe.setCustomBar(LineProgressBar(this))
```

You can also change color and width of line
```
val cp = CircleProgressBar(this)
cp.setColors(Color.RED, Color.BLUE)
cp.setBorderWidth(4)

val lp = LineProgressBar(this)
lp.setColors(Color.RED, Color.BLUE)
lp.setBorderWidth(4)
```

Use your own view e.g Android components (Button, ImageView, TextView should be View)

![Alt Text](https://github.com/Jamshid-M/IGRefreshLayout/blob/master/gif/custom.gif)

Extend it from BaseProgressBar
```
class Circle @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseProgressBar(context, attrs, defStyleAttr)
```

Specify view you want in custom view 
```
var bar = ImageView(context)
```

And call setCustomView method in mParent object
```
mParent.setCustomView(bar, dp2px(80), dp2px(80))
```

That's all, after that you can bind your view into percent which cames from IGRefreshLayout
```
    override fun setPercent(percent: Float) {
        mPercent = percent
        bar.alpha = percent/100
    }
```
For detailed info go to sample directory and check out Circle.kt class


