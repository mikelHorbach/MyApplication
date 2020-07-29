package com.aitgamesstudio.myapplication

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class WebViewActivity : AppCompatActivity() {
    private lateinit var mPreferences: SharedPreferences
    private val simpleFragment: FullscreenFragment = FullscreenFragment()

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
    private val hideHandler = Handler()


    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        simpleFragment.getWebView().systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        // supportActionBar?.show()
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_web_view)
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE)
        if (mPreferences != null) {
            link_url = mPreferences.getString(LINK, "")
            home_url = mPreferences.getString(HOME, "")
            first = mPreferences.getBoolean(FIRST, true)
        }

        displayFragment()
        //simpleFragment.getWebView().setOnTouchListener(delayHideTouchListener)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }


    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }


    override fun onBackPressed() {
        hide()
        if (simpleFragment.getWebView().canGoBack()) {
            simpleFragment.getWebView().goBack()
        }
    }

    private fun displayFragment() {

        val bundle = Bundle()
        if (first && !link_url.isNullOrEmpty()) {
            bundle.putString(URL, link_url)
            simpleFragment.arguments = bundle
            startTrans()
        } else if (!home_url.isNullOrEmpty()) {
            bundle.putString(URL, home_url)
            simpleFragment.arguments = bundle
            startTrans()
        } else {
            Toast.makeText(this, "Url for webview is not specified", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        val preferencesEditor = mPreferences!!.edit()
        preferencesEditor.putBoolean(FIRST, false)
        preferencesEditor.apply()
    }

    override fun onResume() {
        super.onResume()
        hide()
    }

    private fun startTrans() {
        // Get the FragmentManager and start a transaction.
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager
            .beginTransaction()

        // Add the SimpleFragment.
        if (simpleFragment != null) {
            fragmentTransaction.add(
                R.id.fragment_container, simpleFragment
            ).addToBackStack(null).commit()
        }
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
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        const val URL = "url"


        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}