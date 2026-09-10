package org.zotero.android.appupdate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.zotero.android.R

object UpdateNotifications {
    private const val CHANNEL = "app-updates"
    private const val ID = 8217

    fun available(context: Context, version: String): Boolean {
        if (Build.VERSION.SDK_INT >= 26) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL, context.getString(R.string.update_title), NotificationManager.IMPORTANCE_DEFAULT))
        }
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false
        val intent = PendingIntent.getActivity(context, 0, Intent(context, UpdateActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.getString(R.string.update_available))
            .setContentText(context.getString(R.string.mobile_update_found, version))
            .setContentIntent(intent).setAutoCancel(true).build()
        return try { manager.notify(ID, notification); true } catch (_: SecurityException) { false }
    }

    fun ready(context: Context, version: String) {
        if (Build.VERSION.SDK_INT >= 26) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL, context.getString(R.string.update_title), NotificationManager.IMPORTANCE_DEFAULT))
        }
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        val intent = PendingIntent.getActivity(context, 0, Intent(context, UpdateActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.getString(R.string.update_ready))
            .setContentText(context.getString(R.string.update_ready_version, version))
            .setContentIntent(intent).setAutoCancel(true).build()
        try { manager.notify(ID, notification) } catch (_: SecurityException) { /* Settings remains available. */ }
    }

    fun cancel(context: Context) = NotificationManagerCompat.from(context).cancel(ID)
}
