package sync2app.com.syncapplive.additionalSettings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import sync2app.com.syncapplive.databinding.ActivitySampleDemoV2Binding

class DE_MO_202100_2222 : AppCompatActivity() {


    private lateinit var binding: ActivitySampleDemoV2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleDemoV2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtNetworkInfo.setOnHoverListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_HOVER_ENTER -> {
                    // Animate down
                    v.animate()
                        .translationY(20f) // move down 20px
                        .setDuration(200)
                        .start()
                }
                android.view.MotionEvent.ACTION_HOVER_EXIT -> {
                    // Animate back to original
                    v.animate()
                        .translationY(0f)
                        .setDuration(200)
                        .start()
                }
            }
            true
        }


    }
}
