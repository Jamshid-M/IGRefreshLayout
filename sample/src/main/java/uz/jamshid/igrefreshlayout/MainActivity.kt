package uz.jamshid.igrefreshlayout

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import uz.jamshid.library.CircleProgressBar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = RVAdapter(listOf("asd","sad","asd")){
            swipe.setRefreshing(false)
        }

        swipe.setRefreshListener {
            Handler().postDelayed({
                swipe.setRefreshing(false)
            }, 3000)
        }

        val cc = CircleProgressBar(this)

        val l = LineProgressBar(this)
        l.setColors(Color.parseColor("#84ff9d"), Color.parseColor("#004500"))
        swipe.setCustomBar(l)
    }
}
