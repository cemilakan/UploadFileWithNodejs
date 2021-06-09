package dev.cemil.uploadfilewithnodejs

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import net.gotev.uploadservice.BuildConfig
import net.gotev.uploadservice.UploadServiceConfig

class App : Application() {

    companion object {
        const val notificationChannelID = "File Upload Channel"
    }

    private fun createNotificationChannel() {
        Log.e("==>", notificationChannelID)
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(notificationChannelID, "Cimil", NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        UploadServiceConfig.initialize(
            context = this,
            defaultNotificationChannel = notificationChannelID,
            debug = BuildConfig.DEBUG
        )
    }
}