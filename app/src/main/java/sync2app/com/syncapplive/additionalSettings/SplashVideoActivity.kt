package sync2app.com.syncapplive.additionalSettings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.databinding.ActivitySplashVideoBinding
import java.io.File


class SplashVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashVideoBinding

    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC,
            Context.MODE_PRIVATE
        )
    }

    private val sharedP: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS,
            Context.MODE_PRIVATE
        )
    }


    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private var exoPlayer: ExoPlayer? = null


    @SuppressLint("SourceLockedOrientationActivity", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("InitWebvIewloadStates", "Splash Screen: Page ")

        applyOritenation()


        //add exception
        Methods.addExceptionHandler(this)



        // to make app full screen
        Utility.hideSystemBars(window)


        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        binding.textSkipVideo.setOnClickListener {
            startActivity(Intent(this, TvActivityOrAppMode::class.java))
            finish()
        }


        val get_State_Imge_or_Video = sharedBiometric.getString(Constants.imgToggleImageSplashOrVideoSplash, "").toString()
        val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "").toString()

        if (get_imageUseBranding.equals(Constants.imageUseBranding)) {
            if (get_State_Imge_or_Video.equals(Constants.imgToggleImageSplashOrVideoSplash)) {
                playTheSplashVideo()
            } else {
                showSplashImageLogo()
            }

        } else {
            startActivity(Intent(this, TvActivityOrAppMode::class.java))
            finish()
        }

    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun showSplashImageLogo() {
        binding.exoPlayerView.visibility = View.GONE
        binding.splashImage.visibility = View.VISIBLE

        val currentOrientation = resources.configuration.orientation
        val get_TV_or_App_Mode = sharedBiometric.getString(Constants.MY_TV_OR_APP_MODE, "").toString()

        if (currentOrientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE || get_TV_or_App_Mode.equals(
                Constants.TV_Mode
            )
        ) {
            //  requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            val fileType = "PortraitSplash.png"
            loadImage(fileType)

        } else {
            //    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            val fileType = "LandscapeSplash.png"
            loadImage(fileType)

        }


    }

    private fun loadImage(fileTypes: String) {
        val getFolderClo = sharedP.getString(Constants.getFolderClo, "").orEmpty()
        val getFolderSubpath = sharedP.getString(Constants.getFolderSubpath, "").orEmpty()

        val relativePath = "${Constants.Syn2AppLive}/$getFolderClo/$getFolderSubpath/${Constants.App}/Config"
        val fileDir = File(getExternalFilesDir(null), relativePath)
        val imageFile = File(fileDir, fileTypes)

        if (imageFile.exists()) {
            Glide.with(this).load(imageFile).centerCrop().into(binding.splashImage)
            delayedSkipToHome()
        } else {
            startActivity(Intent(this, TvActivityOrAppMode::class.java))
            finish()
        }
    }

    private fun loadMyVideo(fileType: String) {
        val getFolderClo = sharedP.getString(Constants.getFolderClo, "").orEmpty()
        val getFolderSubpath = sharedP.getString(Constants.getFolderSubpath, "").orEmpty()

        val relativePath = "${Constants.Syn2AppLive}/$getFolderClo/$getFolderSubpath/${Constants.App}/Config"
        val videoDir = File(getExternalFilesDir(null), relativePath)
        val videoFile = File(videoDir, fileType)

        if (videoFile.exists()) {
            loadUrlExo(Uri.parse(videoFile.toString()))
        } else {
            startActivity(Intent(this, TvActivityOrAppMode::class.java))
            finish()
        }
    }



    private fun loadUrlExo(urls: Uri) {
        try {
            exoPlayer = ExoPlayer.Builder(this@SplashVideoActivity).build()
            binding.exoPlayerView.player = exoPlayer
            binding.exoPlayerView.keepScreenOn = false
            binding.exoPlayerView.showController()
            binding.exoPlayerView.controllerHideOnTouch = false

            val videoUrl = Uri.parse(urls.toString())
            val media = MediaItem.fromUri(videoUrl)
            exoPlayer!!.setMediaItem(media)
            exoPlayer!!.prepare()
            exoPlayer!!.play()

            exoPlayer!!.addListener(object : Player.Listener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        skipToHome()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    // Handle the error properly here
                    skipToHome()
                }
            })

        } catch (e: Exception) {
            skipToHome()
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun playTheSplashVideo() {

        binding.exoPlayerView.visibility = View.VISIBLE
        binding.splashImage.visibility = View.GONE

        val fileType = "Splash.mp4"
        loadMyVideo(fileType)

    }

    override fun onDestroy() {
        super.onDestroy()
        if (exoPlayer != null){
            exoPlayer!!.stop()
            exoPlayer!!.release()

          //  Toast.makeText(applicationContext, "released", Toast.LENGTH_SHORT).show()
        }

    }


    private fun skipToHome() {
        startActivity(Intent(this, TvActivityOrAppMode::class.java))
        finish()
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


    private fun delayedSkipToHome() {
        handler.postDelayed(Runnable {
            skipToHome()
        }, 2000)
    }

}
