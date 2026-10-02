package dev.burdukthealarm
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
class AlarmReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent?){ContextCompat.startForegroundService(c,Intent(c,AlarmPlaybackService::class.java))}}
class BootReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent?){if(AlarmData.enabled(c))runCatching{AlarmScheduler.schedule(c)}}}
