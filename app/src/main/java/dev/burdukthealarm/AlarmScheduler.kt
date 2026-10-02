package dev.burdukthealarm
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar
object AlarmScheduler {
 fun schedule(c:Context){
  val am=c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
  val pi=PendingIntent.getBroadcast(c,1001,Intent(c,AlarmReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  val cal=Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,AlarmData.hour(c));set(Calendar.MINUTE,AlarmData.minute(c));set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0);if(timeInMillis<=System.currentTimeMillis())add(Calendar.DAY_OF_YEAR,1)}
  if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S&&!am.canScheduleExactAlarms())throw SecurityException("Разрешите точные будильники в настройках Android")
  am.setAlarmClock(AlarmManager.AlarmClockInfo(cal.timeInMillis,pi),pi);AlarmData.setEnabled(c,true)
 }
 fun cancel(c:Context){val am=c.getSystemService(Context.ALARM_SERVICE) as AlarmManager;val pi=PendingIntent.getBroadcast(c,1001,Intent(c,AlarmReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);am.cancel(pi);AlarmData.setEnabled(c,false)}
}
