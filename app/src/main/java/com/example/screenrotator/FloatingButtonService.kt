package com.example.screenrotator

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import kotlin.math.abs

class FloatingButtonService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var view: View
    private lateinit var params: WindowManager.LayoutParams

    private val rotations = intArrayOf(
        Surface.ROTATION_0, Surface.ROTATION_90,
        Surface.ROTATION_180, Surface.ROTATION_270
    )
    private var current = 0

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification())
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        addButton()
    }

    private fun addButton() {
        view = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_rotate)
            setBackgroundResource(R.drawable.floating_bg)
            setPadding(28, 28, 28, 28)
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 300
        }

        view.setOnTouchListener(dragClick())
        wm.addView(view, params)
    }

    private fun dragClick() = object : View.OnTouchListener {
        private var ix = 0
        private var iy = 0
        private var tx = 0f
        private var ty = 0f
        private var moved = false

        override fun onTouch(v: View, e: MotionEvent): Boolean {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    ix = params.x; iy = params.y; tx = e.rawX; ty = e.rawY; moved = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - tx).toInt()
                    val dy = (e.rawY - ty).toInt()
                    if (abs(dx) > 12 || abs(dy) > 12) moved = true
                    params.x = ix + dx
                    params.y = iy + dy
                    wm.updateViewLayout(view, params)
                }
                MotionEvent.ACTION_UP -> if (!moved) rotateNext()
            }
            return true
        }
    }

    private fun rotateNext() {
        current = (current + 1) % rotations.size
        try {
            Settings.System.putInt(
                contentResolver,
                Settings.System.ACCELEROMETER_ROTATION, 0
            )
            Settings.System.putInt(
                contentResolver,
                Settings.System.USER_ROTATION, rotations[current]
            )
        } catch (_: SecurityException) {
            // нет разрешения WRITE_SETTINGS
        }
    }

    private fun buildNotification(): Notification {
        val id = "rotator_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(
                    NotificationChannel(
                        id, "Поворот экрана",
                        NotificationManager.IMPORTANCE_LOW
                    )
                )
        }
        return Notification.Builder(this, id)
            .setContentTitle("Поворот экрана активен")
            .setContentText("Тап по кнопке — смена ориентации")
            .setSmallIcon(android.R.drawable.ic_menu_rotate)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::view.isInitialized) wm.removeView(view)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
