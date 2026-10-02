package dev.burdukthealarm
import android.app.*
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
class AlarmPlaybackService:Service(){
 private var player:ExoPlayer?=null
 override fun onCreate(){super.onCreate();if(Build.VERSION.SDK_INT>=26)getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("alarm","Будильник",NotificationManager.IMPORTANCE_HIGH))}
 override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int{
  if(intent?.action=="STOP"){stopSelf();return START_NOT_STICKY}
  startForeground(42,notice("Подбираем музыку…"))
  val source=AlarmData.choose(this)
  if(source==null){getSystemService(NotificationManager::class.java).notify(42,notice("Добавьте музыку или радиостанцию"));return START_NOT_STICKY}
  player?.release();val p=ExoPlayer.Builder(this).build();player=p
  p.addListener(object:Player.Listener{override fun onPlayerError(error:PlaybackException){
   val fallback=if(source.isRadio)AlarmData.tracks(this@AlarmPlaybackService).randomOrNull() else null
   if(fallback!=null){p.setMediaItem(MediaItem.fromUri(Uri.parse(fallback.uri)));p.prepare();p.playWhenReady=true;getSystemService(NotificationManager::class.java).notify(42,notice("Радио недоступно — локальная музыка"))}
  }})
  p.setMediaItem(MediaItem.fromUri(Uri.parse(source.uri)));p.prepare();p.playWhenReady=true
  getSystemService(NotificationManager::class.java).notify(42,notice("Сейчас играет: "+source.title))
  return START_NOT_STICKY
 }
 private fun notice(text:String)=NotificationCompat.Builder(this,"alarm").setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("NotTheAlarm").setContentText(text).setCategory(NotificationCompat.CATEGORY_ALARM).setPriority(NotificationCompat.PRIORITY_MAX).setOngoing(true).addAction(android.R.drawable.ic_media_pause,"Остановить",PendingIntent.getService(this,2,Intent(this,AlarmPlaybackService::class.java).setAction("STOP"),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)).build()
 override fun onBind(intent:Intent?):IBinder?=null
 override fun onDestroy(){player?.release();player=null;super.onDestroy()}
}
