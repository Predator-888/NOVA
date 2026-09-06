package com.example.nova.blocker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AppBlockerService : Service() {

    private var warningTextView: TextView? = null
    private var windowManager: WindowManager? = null
    private var overlayView: LinearLayout? = null
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // The list of apps you want to block
    private val blockedApps = listOf("com.instagram.android", "com.zhiliaoapp.musically")

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setupOverlayView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isNightMode = intent?.getStringExtra("MODE") == "NIGHT_MODE"

        // Dynamically change the text based on the time/mode!
        warningTextView?.text = if (isNightMode) {
            "Wind Down Mode Active.\nDisconnect and get to sleep."
        } else {
            "Get back to studying."
        }

        startMonitoring()
        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        val channelId = "BlockerServiceChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "App Blocker Active",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Nova Focus Mode")
            .setContentText("Guarding your time...")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback icon
            .build()

        startForeground(1, notification)
    }

    private fun setupOverlayView() {
        // We build a programmatic UI here because wrapping Jetpack Compose
        // inside a system-level window manager requires massive boilerplate.
        overlayView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212")) // Dark theme background
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)

            warningTextView = TextView(this@AppBlockerService).apply {
                text = "Get back to studying."
                setTextColor(Color.parseColor("#00C853")) // Nova Green
                textSize = 32f
                gravity = Gravity.CENTER
            }

            val homeButton = Button(this@AppBlockerService).apply {
                text = "Exit App"
                setBackgroundColor(Color.parseColor("#00C853"))
                setTextColor(Color.WHITE)
                setOnClickListener {
                    // This simulates pressing the home button!
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(homeIntent)
                    removeOverlay()
                }
            }

            addView(warningTextView)
            addView(homeButton)
        }
    }

    private fun startMonitoring() {
        serviceScope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            var isOverlayShowing = false

            // ---> MOVED THIS OUTSIDE THE LOOP <---
            // Now it REMEMBERS the last app you were looking at
            var currentForegroundApp = ""

            while (isActive) {
                val currentTime = System.currentTimeMillis()
                // Look at the last 10 seconds of app usage events
                val usageEvents = usageStatsManager.queryEvents(currentTime - 10000, currentTime)
                val event = UsageEvents.Event()

                while (usageEvents.hasNextEvent()) {
                    usageEvents.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        currentForegroundApp = event.packageName
                    }
                }

                if (blockedApps.contains(currentForegroundApp)) {
                    if (!isOverlayShowing) {
                        showOverlay()
                        isOverlayShowing = true
                    }
                } else {
                    if (isOverlayShowing) {
                        removeOverlay()
                        isOverlayShowing = false
                    }
                }

                delay(1500) // Check every 1.5 seconds
            }
        }
    }

    private fun showOverlay() {
        launchOnMainThread {
            if (overlayView?.windowToken == null) {
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    else WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT
                )
                windowManager?.addView(overlayView, params)
            }
        }
    }

    private fun removeOverlay() {
        launchOnMainThread {
            if (overlayView?.windowToken != null) {
                windowManager?.removeView(overlayView)
            }
        }
    }

    private fun launchOnMainThread(block: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch { block() }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        removeOverlay()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}