package com.aitgamesstudio.myapplication

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.airbnb.lottie.LottieAnimationView
import kotlinx.android.synthetic.main.activity_fullscreen.*
import org.json.JSONObject


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<String>,
    ConnectivityReceiver.ConnectivityReceiverListener {
    private lateinit var fullscreenContent: LottieAnimationView
    private lateinit var mPreferences: SharedPreferences

    val LOADER_ID = 20
    private var link_url: String? = null
    private var home_url: String? = null
    private var first: Boolean = true
    private val sharedPrefFile = "com.aitgamesstudio.myapplication"

    // Key for link url
    private val LINK = "link"

    // Key for home url
    private val HOME = "home"

    // Saved instance state key.
    private val FIRST = "first"
    private var repeatAnimCount = 2
    private val hideHandler = Handler()

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = findViewById(R.id.fullscreen_content_)
        fullscreen_content_.visibility = View.VISIBLE
        fullscreen_content_.repeatCount = repeatAnimCount

        registerReceiver(
            ConnectivityReceiver(),
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        if (mPreferences != null) {
            link_url = mPreferences.getString(LINK, "")
            home_url = mPreferences.getString(HOME, "")
            first = mPreferences.getBoolean(FIRST, true)
        }
        if ((link_url.isNullOrEmpty() || home_url.isNullOrEmpty()) && checkNetworkState()) {
            supportLoaderManager.initLoader(LOADER_ID, null, this)
            makeRequest()
        } else if (!checkNetworkState()) {
            showToast("Check internet connection!")
        } else {
            showToast(link_url + "\n" + home_url)
        }

        fullscreen_content_.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
//                while (link_url.isNullOrEmpty() || home_url.isNullOrEmpty() || !checkNetworkState()) {
//                    fullscreen_content_.playAnimation()
//                }
//                fullscreen_content_.cancelAnimation()
//                fullscreen_content_.visibility = View.INVISIBLE
//                startWebActivity()
                if (!link_url.isNullOrEmpty() && !home_url.isNullOrEmpty()) {
                    fullscreen_content_.visibility = View.INVISIBLE

                    startWebActivity()
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })

    }


    override fun onPause() {
        super.onPause()
        savePref()
    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
        hide()
    }


    private fun savePref() {
        val preferencesEditor = mPreferences!!.edit()
        preferencesEditor.putString(LINK, link_url)
        preferencesEditor.putString(HOME, home_url)
        preferencesEditor.apply()
    }

    private fun reset() {
        // Reset
        link_url = null
        home_url = null

        // Clear preferences
        val preferencesEditor = mPreferences!!.edit()
        preferencesEditor.clear()
        preferencesEditor.apply()
    }

    private fun makeRequest() {

        // this will try to fetch a Loader with ID = LOADER_ID
        val loader: Loader<Long>? = supportLoaderManager.getLoader(LOADER_ID)
        if (loader == null) {
            /* if the Loader with the loaderID not found,
            * Initialize a New Loader with ID = LOADER_ID
            * Also pass the necessary callback which is 'this' because we've implemented it on our activity
            */
            supportLoaderManager.initLoader(LOADER_ID, Bundle.EMPTY, this)
        } else {
            /* If the Loader was found with ID = LOADER_ID,
            * Stop whatever it may be doing
            * Restart it
            * Also pass the necessary callback which is 'this' because we've implemented it on our activity
            */
            supportLoaderManager.restartLoader(LOADER_ID, Bundle.EMPTY, this)
        }
    }

    private fun showToast(s: String) {
        Toast.makeText(baseContext, s, Toast.LENGTH_SHORT).show()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun startWebActivity() {
        val intent = Intent(baseContext, WebViewActivity::class.java)
        startActivity(intent)
    }

    private fun checkNetworkState(): Boolean {
        val conMgr =
            baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (conMgr!!.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .state == NetworkInfo.State.CONNECTED
            || conMgr!!.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .state == NetworkInfo.State.CONNECTED
        ) {

            return true
        } else if (conMgr!!.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .state == NetworkInfo.State.DISCONNECTED
            || conMgr!!.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .state == NetworkInfo.State.DISCONNECTED
        ) {
            return false
        }
        return true
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        isFullscreen = false
        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }


    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }


    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 300

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    override fun onLoadFinished(loader: Loader<String>, data: String?) {
        try {
            val jsonObject = JSONObject(data)
            //Toast.makeText(this, "sd"+jsonObject.toString(), Toast.LENGTH_SHORT).show();
            link_url = jsonObject.getString("link")
            home_url = jsonObject.getString("home")
            savePref()

            Toast.makeText(this, link_url, Toast.LENGTH_SHORT).show()


        } catch (e: Exception) {
            // If onPostExecute does not receive a proper JSON string,
            // update the UI to show failed results.
            Toast.makeText(
                this,
                "No url is not specified. Check internet connection",
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): BookLoader {
        return BookLoader(this)
    }

    override fun onLoaderReset(loader: Loader<String>) {
        TODO("Not yet implemented")
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        //showToast("Network state:$isConnected")
        if (isConnected) {
            if (link_url.isNullOrEmpty() || home_url.isNullOrEmpty()) {
                makeRequest()
                //fullscreen_content_.repeatCount = 1
                fullscreen_content_.playAnimation()
            }
        }
    }


}