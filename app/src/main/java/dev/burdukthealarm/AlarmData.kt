package dev.burdukthealarm

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class AudioSource(val id:String,val title:String,val uri:String,val tags:List<String>,val isRadio:Boolean=false)

object AlarmData {
 private const val P="not_the_alarm"
 private fun p(c:Context)=c.getSharedPreferences(P,Context.MODE_PRIVATE)
 fun enabled(c:Context)=p(c).getBoolean("enabled",false)
 fun setEnabled(c:Context,v:Boolean){p(c).edit().putBoolean("enabled",v).apply()}
 fun hour(c:Context)=p(c).getInt("hour",7)
 fun minute(c:Context)=p(c).getInt("minute",0)
 fun setTime(c:Context,h:Int,m:Int){p(c).edit().putInt("hour",h).putInt("minute",m).apply()}
 fun selectedTags(c:Context)=p(c).getStringSet("tags",setOf("energetic","upbeat"))?.toSet()?:setOf("energetic","upbeat")
 fun setSelectedTags(c:Context,t:Set<String>){p(c).edit().putStringSet("tags",t).apply()}
 private fun read(c:Context,key:String,radio:Boolean):List<AudioSource>{
  val a=JSONArray(p(c).getString(key,"[]"))
  return (0 until a.length()).mapNotNull { i -> runCatching { val o=a.getJSONObject(i); AudioSource(o.getString("id"),o.getString("title"),o.getString("uri"),o.optString("tags","any").split(",").map{it.trim().lowercase()},radio) }.getOrNull() }
 }
 private fun write(c:Context,key:String,s:List<AudioSource>){
  val a=JSONArray(); s.forEach { a.put(JSONObject().put("id",it.id).put("title",it.title).put("uri",it.uri).put("tags",it.tags.joinToString(","))) }
  p(c).edit().putString(key,a.toString()).apply()
 }
 fun tracks(c:Context)=read(c,"tracks",false)
 fun stations(c:Context)=read(c,"stations",true)
 fun addTrack(c:Context,title:String,uri:String){write(c,"tracks",tracks(c)+AudioSource(UUID.randomUUID().toString(),title,uri,listOf("any")))}
 fun addStation(c:Context,title:String,uri:String,tags:List<String>){write(c,"stations",stations(c)+AudioSource(UUID.randomUUID().toString(),title,uri,tags,true))}
 fun updateTags(c:Context,s:AudioSource,tags:List<String>){val key=if(s.isRadio)"stations" else "tracks";write(c,key,read(c,key,s.isRadio).map{if(it.id==s.id)it.copy(tags=tags)else it})}
 fun choose(c:Context):AudioSource?{
  val all=tracks(c)+stations(c); if(all.isEmpty())return null
  val wanted=selectedTags(c)
  return all.filter{it.tags.contains("any")||wanted.isEmpty()||it.tags.any(wanted::contains)}.ifEmpty{tracks(c).ifEmpty{all}}.randomOrNull()
 }
}
