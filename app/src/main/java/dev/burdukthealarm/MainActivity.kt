package dev.burdukthealarm

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { AlarmHome() } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmHome() {
 val context= LocalContext.current
 var refresh by remember { mutableIntStateOf(0) }
 val currentRevision = refresh
 var enabled by remember { mutableStateOf(AlarmData.enabled(context)) }
 var hour by remember { mutableIntStateOf(AlarmData.hour(context)) }
 var minute by remember { mutableIntStateOf(AlarmData.minute(context)) }
 var tags by remember { mutableStateOf(AlarmData.selectedTags(context)) }
 var status by remember { mutableStateOf("Выбери настроение и добавь музыку или радиостанции.") }
 var showRadio by remember { mutableStateOf(false) }
 var radioName by remember { mutableStateOf("") }
 var radioUrl by remember { mutableStateOf("") }
 var radioTags by remember { mutableStateOf("energetic, upbeat") }
 val picker= rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
  if(uri!=null) runCatching {
   context.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)
   val title=context.contentResolver.query(uri,null,null,null,null)?.use { c ->
    val idx=c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if(c.moveToFirst()&&idx>=0)c.getString(idx) else null
   } ?: uri.lastPathSegment ?: "Моя музыка"
   AlarmData.addTrack(context,title,uri.toString());refresh++;status="Добавлено: "+title
  }.onFailure { status="Не удалось добавить файл: "+(it.localizedMessage ?: "ошибка") }
 }
 MaterialTheme(colorScheme=darkColorScheme(primary=androidx.compose.ui.graphics.Color(0xFFB7F397),background=androidx.compose.ui.graphics.Color(0xFF10120F),surface=androidx.compose.ui.graphics.Color(0xFF1A1D18))) {
  Scaffold(topBar={TopAppBar(title={Text("NotTheAlarm",fontWeight=FontWeight.Bold)},colors=TopAppBarDefaults.topAppBarColors(containerColor=MaterialTheme.colorScheme.background))}) { inset ->
   LazyColumn(Modifier.fillMaxSize().padding(inset).padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(14.dp),contentPadding=PaddingValues(top=8.dp,bottom=32.dp)) {
    item {
     Card {
      Column(Modifier.fillMaxWidth().padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
       Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){Icon(Icons.Default.Alarm,null);Text("Следующий будильник")}
       Text(String.format(Locale.getDefault(),"%02d:%02d",hour,minute),fontSize=52.sp,fontWeight=FontWeight.Light)
       Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Button(onClick={TimePickerDialog(context,{_,h,m->hour=h;minute=m;AlarmData.setTime(context,h,m);if(enabled)runCatching{AlarmScheduler.schedule(context)}.onFailure{status=it.localizedMessage?: "Проверь разрешение"}},hour,minute,true).show()}) {Text("Время")}
        Switch(checked=enabled,onCheckedChange={turnOn->
         if(turnOn) {
          runCatching {
           if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.S) {
            val am=context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            if(!am.canScheduleExactAlarms()){context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));status="Разреши точные будильники в настройках, затем включи снова.";return@runCatching}
           }
           AlarmScheduler.schedule(context);enabled=true;status="Будильник включён."
          }.onFailure{status=it.localizedMessage?: "Не удалось включить"}
         } else {AlarmScheduler.cancel(context);enabled=false;status="Будильник выключен."}
        })
       }
      }
     }
    }
    item {Text("НАСТРОЕНИЕ",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary)}
    item {
     val options=listOf("energetic" to "Энергично","upbeat" to "Бодро","calm" to "Спокойно","rock" to "Рок","electronic" to "Электроника","ambient" to "Эмбиент","any" to "Любое")
     Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
      options.chunked(2).forEach { row ->
       Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        row.forEach { (key,label)->FilterChip(selected=key in tags,onClick={tags=if(key in tags)tags-key else tags+key;AlarmData.setSelectedTags(context,tags)},label={Text(label)},modifier=Modifier.weight(1f)) }
        if(row.size==1) Spacer(Modifier.weight(1f))
       }
      }
     }
    }
    item {Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.MusicNote,null);Spacer(Modifier.width(8.dp));Text("Моя музыка (" + currentRevision + ")",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold);Spacer(Modifier.weight(1f));IconButton(onClick={picker.launch(arrayOf("audio/*"))}){Icon(Icons.Default.Add,"Добавить музыку")}}}
    if(AlarmData.tracks(context).isEmpty()) item {Text("Добавь аудиофайлы с устройства. Локальная музыка сможет играть без интернета.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
    items(AlarmData.tracks(context),key={it.id}) { track ->
     ListItem(headlineContent={Text(track.title)},supportingContent={Text(track.tags.joinToString(", "))},leadingContent={Icon(Icons.Default.MusicNote,null)},trailingContent={
      TextButton(onClick={
       val input=android.widget.EditText(context).apply{setText(track.tags.filter{it!="any"}.joinToString(", "))}
       android.app.AlertDialog.Builder(context).setTitle("Теги: "+track.title).setMessage("Введи теги через запятую").setView(input).setPositiveButton("Сохранить"){_,_->AlarmData.updateTags(context,track,input.text.toString().split(",").map{it.trim().lowercase()}.filter{it.isNotBlank()}.ifEmpty{listOf("any")});refresh++}.setNegativeButton("Отмена",null).show()
      }){Text("Теги")}
     })
     HorizontalDivider()
    }
    item {Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Radio,null);Spacer(Modifier.width(8.dp));Text("Интернет-радио (" + currentRevision + ")",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold);Spacer(Modifier.weight(1f));IconButton(onClick={showRadio=true}){Icon(Icons.Default.Add,"Добавить радио")}}}
    if(AlarmData.stations(context).isEmpty()) item {Text("Добавь прямой URL аудиопотока, а не ссылку на сайт радиостанции.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
    items(AlarmData.stations(context),key={it.id}) { station ->
     ListItem(headlineContent={Text(station.title)},supportingContent={Text(station.tags.joinToString(", ")+"\n"+station.uri)},leadingContent={Icon(Icons.Default.Radio,null)},trailingContent={
      TextButton(onClick={
       val input=android.widget.EditText(context).apply{setText(station.tags.joinToString(", "))}
       android.app.AlertDialog.Builder(context).setTitle("Теги: "+station.title).setMessage("Введи теги через запятую").setView(input).setPositiveButton("Сохранить"){_,_->AlarmData.updateTags(context,station,input.text.toString().split(",").map{it.trim().lowercase()}.filter{it.isNotBlank()}.ifEmpty{listOf("any")});refresh++}.setNegativeButton("Отмена",null).show()
      }){Text("Теги")}
     })
     HorizontalDivider()
    }
    item {Text(status,color=MaterialTheme.colorScheme.onSurfaceVariant)}
   }
  }
  if(showRadio) AlertDialog(onDismissRequest={showRadio=false},title={Text("Добавить радиостанцию")},text={
   Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
    OutlinedTextField(radioName,{radioName=it},label={Text("Название")},singleLine=true)
    OutlinedTextField(radioUrl,{radioUrl=it},label={Text("URL аудиопотока")},singleLine=true)
    OutlinedTextField(radioTags,{radioTags=it},label={Text("Теги через запятую")},singleLine=true)
   }
  },confirmButton={TextButton(onClick={
   if(radioName.isNotBlank()&&(radioUrl.startsWith("http://")||radioUrl.startsWith("https://"))){
    AlarmData.addStation(context,radioName.trim(),radioUrl.trim(),radioTags.split(",").map{it.trim().lowercase()}.filter{it.isNotBlank()}.ifEmpty{listOf("any")})
    refresh++;radioName="";radioUrl="";showRadio=false;status="Радиостанция добавлена."
   }else status="Нужно название и URL, начинающийся с http:// или https://"
  }){Text("Добавить")}},dismissButton={TextButton(onClick={showRadio=false}){Text("Отмена")}})
 }
}
