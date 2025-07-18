package sync2app.com.syncapplive

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sync2app.com.syncapplive.additionalSettings.InformationActivity
import sync2app.com.syncapplive.constants.*
import sync2app.com.syncapplive.additionalSettings.ReSyncActivity
import sync2app.com.syncapplive.additionalSettings.RequiredBioActivity
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import java.io.File
import java.util.Objects

class WelcomeSliderKT : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var myViewPagerAdapter: MyViewPagerAdapter
    private lateinit var dotsLayout: LinearLayout
    private lateinit var dots: Array<TextView?>
    private lateinit var layouts: IntArray
    private lateinit var btnSkip: Button
    private lateinit var btnNext: Button
    private lateinit var prefManager: prefManager


    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC,
            Context.MODE_PRIVATE
        )
    }

    private val myDownloadClass: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS,
            Context.MODE_PRIVATE
        )
    }

    private val sharedTVAPPModePreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_TV_APP_MODE, Context.MODE_PRIVATE
        )
    }

    private val viewPagerPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            addBottomDots(position)

            // Changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.size - 1) {
                // Last page. Make button text to GOT IT
                btnNext.text = getString(R.string.start)
                btnSkip.visibility = View.GONE
            } else {
                // Still pages are left
                btnNext.text = getString(R.string.next)
                btnSkip.visibility = View.VISIBLE
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}

        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Checking for first time launch - before calling setContentView()
        prefManager = prefManager(this)
        if (!prefManager.isFirstTimeLaunch) {
            launchHomeScreen()
            finish()
        }

        setContentView(R.layout.activity_welcome)

        applyOritenation()

        Log.d("InitWebvIewloadStates", "WElcomeSlider: Page ")


        // Init Refresh Time
        val d_time = myDownloadClass.getLong(Constants.get_Refresh_Timer, 0L)
        d_time.let {itLong->
            if (itLong == 0L) {
                val editor = myDownloadClass.edit()
                editor.putLong(Constants.get_Refresh_Timer, Constants.T_4_HR)
                editor.apply()
            }
        }




        // Add exception
        Methods.addExceptionHandler(this)

        // Making notification bar transparent
      //  window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        Utility.hideSystemBars(window)


        viewPager = findViewById(R.id.view_pager)
        dotsLayout = findViewById(R.id.layoutDots)
        btnSkip = findViewById(R.id.btn_skip)
        btnNext = findViewById(R.id.btn_next)

        // Layouts of all welcome sliders
        // Add few more layouts if you want
        layouts = intArrayOf(
            R.layout.slider_layout_1,
            R.layout.slider_layout_2,
            R.layout.slider_layout_3,
            R.layout.slider_layout_4
        )

        // Adding bottom dots
        addBottomDots(0)

        // Making notification bar transparent
        changeStatusBarColor()

        myViewPagerAdapter = MyViewPagerAdapter()
        viewPager.adapter = myViewPagerAdapter
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)

        btnSkip.setOnClickListener { launchHomeScreen() }

        btnNext.setOnClickListener {
            // Checking for last page
            // If last page home screen will be launched
            val current = getItem(+1)
            if (current < layouts.size) {
                // Move to next screen
                viewPager.currentItem = current
            } else {
                launchHomeScreen()
            }
        }
    }



    override fun onResume() {
        if (jsonUrl == null) {

            val intent = Intent(this@WelcomeSliderKT, SplashKT::class.java)
            startActivity(intent)
            finish()
        }
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }




    @SuppressLint("SourceLockedOrientationActivity")
    private fun applyOritenation() {
        val getState = sharedBiometric.getString(Constants.IMG_TOGGLE_FOR_ORIENTATION, "").toString()

        if (getState == Constants.USE_POTRAIT){
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        }else if (getState == Constants.USE_LANDSCAPE){

            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        }else if (getState == Constants.USE_UNSEPECIFIED){
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        }

    }




    private fun addBottomDots(currentPage: Int) {
        dots = arrayOfNulls(layouts.size)

        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)

        dotsLayout.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]?.text = Html.fromHtml("&#8226;")
            dots[i]?.textSize = 35f
            dots[i]?.setTextColor(colorsInactive[currentPage])
            dotsLayout.addView(dots[i])
        }

        if (dots.isNotEmpty())
            dots[currentPage]?.setTextColor(colorsActive[currentPage])
    }

    private fun getItem(i: Int): Int {
        return viewPager.currentItem + i
    }

    private fun launchHomeScreen() {
        prefManager.isFirstTimeLaunch = false




        val getInfoPageState = sharedBiometric.getString(Constants.FIRST_INFORMATION_PAGE_COMPLETED, "").toString()
        if(getInfoPageState == Constants.FIRST_INFORMATION_PAGE_COMPLETED){

            val sharedBiometric = applicationContext.getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
            val getTvMode = sharedBiometric.getString(Constants.CALL_RE_SYNC_MANGER, "").toString()


            val get_INSTALL_TV_JSON_USER_CLICKED = sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "").toString()
            val get_installTVMode = sharedTVAPPModePreferences.getBoolean(Constants.installTVMode, false)
            val getFirstMode = sharedTVAPPModePreferences.getString(Constants.installTVModeForFirstTime, "").toString()

            if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED ){

                if (get_installTVMode && !getFirstMode.equals(Constants.installTVModeForFirstTime)){

                    Log.d("InitWebvIewloadStates", "WelcomeSliderKT: Yes cliked ")
                    val myactivity = Intent(this@WelcomeSliderKT, ReSyncActivity::class.java)
                    myactivity.putExtra("url", jsonUrl)
                    startActivity(myactivity)
                    finish()

                }else{

                    Log.d("InitWebvIewloadStates", "WelcomeSliderKT: No clicked")
                    val myactivity =  Intent(this@WelcomeSliderKT, WebViewPage::class.java)
                    myactivity. putExtra("url", jsonUrl)
                    startActivity(myactivity)
                    finish()

                }


            }else{

                if (getTvMode == Constants.CALL_RE_SYNC_MANGER) {
                    val myactivity = Intent(this@WelcomeSliderKT, ReSyncActivity::class.java)
                    myactivity.putExtra("url", jsonUrl)
                    startActivity(myactivity)
                    finish()


                } else {
                    val myactivity =  Intent(this@WelcomeSliderKT, WebViewPage::class.java)
                    myactivity. putExtra("url", jsonUrl)
                    startActivity(myactivity)
                    finish()

                }


            }



        }else{
            startActivity(Intent(applicationContext, InformationActivity::class.java))
            finish()

        }


    }

    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }

    inner class MyViewPagerAdapter : PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            val view = layoutInflater!!.inflate(layouts[position], container, false)

            when (position) {
                0 -> {
                    val img = view.findViewById<ImageView>(R.id.slider1_img)
                    loadImg(img, screen1Img)

                    val txt = view.findViewById<TextView>(R.id.slider1title)
                    val txt2 = view.findViewById<TextView>(R.id.slider1desc)
                    txt.setTextColor(Color.parseColor(screen1TextColor))
                    txt2.setTextColor(Color.parseColor(screen1TextColor))
                    txt.text = screen1TitleText
                    txt2.text = screen1Desc

                    if (screen1BgColor != null) {
                        view.setBackgroundColor(Color.parseColor(screen1BgColor))
                    }
                }
                1 -> {
                    val img = view.findViewById<ImageView>(R.id.slider2_img)
                    loadImg(img, screen2Img)

                    val txt = view.findViewById<TextView>(R.id.slider2title)
                    val txt2 = view.findViewById<TextView>(R.id.slider2desc)
                    txt.setTextColor(Color.parseColor(screen2TextColor))
                    txt2.setTextColor(Color.parseColor(screen2TextColor))
                    txt.text = screen2TitleText
                    txt2.text = screen2Desc

                    if (screen2BgColor != null) {
                        view.setBackgroundColor(Color.parseColor(screen2BgColor))
                    }
                }
                2 -> {
                    val img = view.findViewById<ImageView>(R.id.slider3_img)
                    loadImg(img, screen3Img)

                    val txt = view.findViewById<TextView>(R.id.slider3title)
                    val txt2 = view.findViewById<TextView>(R.id.slider3desc)
                    txt.setTextColor(Color.parseColor(screen3TextColor))
                    txt2.setTextColor(Color.parseColor(screen3TextColor))
                    txt.text = screen3TitleText
                    txt2.text = screen3Desc

                    if (screen3BgColor != null) {
                        view.setBackgroundColor(Color.parseColor(screen3BgColor))
                    }
                }
                3 -> {
                    val img = view.findViewById<ImageView>(R.id.slider4_img)
                    loadImg(img, screen4Img)

                    val txt = view.findViewById<TextView>(R.id.slider4title)
                    val txt2 = view.findViewById<TextView>(R.id.slider4desc)
                    txt.setTextColor(Color.parseColor(screen4TextColor))
                    txt2.setTextColor(Color.parseColor(screen4TextColor))
                    txt.text = screen4TitleText
                    txt2.text = screen4Desc

                    if (screen4BgColor != null) {
                        view.setBackgroundColor(Color.parseColor(screen4BgColor))
                    }
                }
            }

            container.addView(view)

            return view
        }

        private fun loadImg(img: ImageView, imgUrl: String?) {
            Glide.with(this@WelcomeSliderKT)
                .load(Uri.parse(imgUrl))
                .into(img)
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            val view = obj as View
            container.removeView(view)
        }
    }
}
