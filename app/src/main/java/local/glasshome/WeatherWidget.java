package local.glasshome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class WeatherWidget {
 private final Activity activity;
 private final SharedPreferences prefs;
 private final Handler ui=new Handler(Looper.getMainLooper());
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private TextView view;
 private boolean active,closed,loading,failed;
 private long lastAttempt;
 private int revision;
 private final Runnable timer=()->refresh(false);
 WeatherWidget(Activity activity){this.activity=activity;prefs=activity.getSharedPreferences("weather",0);}
 TextView createView(){view=new TextView(activity);view.setTextColor(0xffeefaff);view.setTextSize(14);view.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);view.setPadding(12,0,12,0);view.setFocusable(true);view.setClickable(true);view.setContentDescription("Local weather. Select to change location.");view.setOnClickListener(v->chooseLocation());view.setOnFocusChangeListener((v,focused)->view.setBackgroundColor(focused?0x55ffffff:0x00000000));display();return view;}
 void start(){active=true;refresh(false);}
 void stop(){active=false;ui.removeCallbacks(timer);}
 void destroy(){closed=true;stop();worker.shutdownNow();ui.removeCallbacksAndMessages(null);}
 private void display(){if(view==null)return;String town=prefs.getString("town","");if(town.isEmpty()){view.setText("Local weather · "+(loading?"Locating…":"Unavailable"));return;}String conditions=prefs.getString("conditions","");long age=System.currentTimeMillis()-prefs.getLong("updated",0);view.setText(town+(prefs.getBoolean("automatic",true)?" (approx.)":"")+"  ·  "+(conditions.isEmpty()?"Weather unavailable":conditions)+((failed||age>600000)&&!conditions.isEmpty()?" (saved)":""));}
 private void refresh(boolean force){
  ui.removeCallbacks(timer);if(!active||closed)return;display();
  if(!loading&&(force||System.currentTimeMillis()-lastAttempt>300000)){
   loading=true;lastAttempt=System.currentTimeMillis();final int requestedRevision=revision;final boolean automatic=prefs.getBoolean("automatic",true);final String savedLat=prefs.getString("lat",""),savedLon=prefs.getString("lon",""),savedTown=prefs.getString("town","");display();
   worker.execute(()->{String result=null,lat=savedLat,lon=savedLon,town=savedTown;
    try{if(automatic){JSONObject location=request("https://ipwho.is/?fields=success,city,latitude,longitude");if(!location.optBoolean("success"))throw new java.io.IOException("Location unavailable");lat=String.valueOf(location.getDouble("latitude"));lon=String.valueOf(location.getDouble("longitude"));town=location.getString("city");}
     if(lat.isEmpty()||lon.isEmpty())throw new java.io.IOException("No location");JSONObject current=request("https://api.open-meteo.com/v1/forecast?latitude="+lat+"&longitude="+lon+"&current=temperature_2m,weather_code&forecast_days=1").getJSONObject("current");result=Math.round(current.getDouble("temperature_2m"))+"°C  "+condition(current.getInt("weather_code"));
    }catch(Exception ignored){}
    final String answer=result,newLat=lat,newLon=lon,newTown=town;
    ui.post(()->{loading=false;if(closed)return;if(requestedRevision!=revision){lastAttempt=0;refresh(true);return;}failed=answer==null;if(answer!=null)prefs.edit().putString("town",newTown).putString("lat",newLat).putString("lon",newLon).putString("conditions",answer).putLong("updated",System.currentTimeMillis()).apply();display();});
   });
  }ui.postDelayed(timer,300000);
 }
 void chooseLocation(){new AlertDialog.Builder(activity).setTitle("Local weather").setItems(new String[]{"Automatic location (approximate; follows VPN)","Use postcode or town","Refresh weather"},(d,n)->{if(n==0){revision++;prefs.edit().putBoolean("automatic",true).remove("town").remove("conditions").remove("updated").apply();lastAttempt=0;refresh(true);}else if(n==1)manualLocation();else refresh(true);}).setNegativeButton("Close",null).show();}
 void manualLocation(){EditText input=new EditText(activity);input.setSingleLine(true);input.setHint("UK postcode or town");input.setText(prefs.getString("town",""));new AlertDialog.Builder(activity).setTitle("Weather location").setMessage("Enter your UK postcode for local weather, or a town. Postcode lookup uses Postcodes.io; weather uses Open-Meteo. The location is saved only on this stick.").setView(input).setPositiveButton("Search",(d,n)->search(input.getText().toString().trim())).setNeutralButton("Refresh",(d,n)->refresh(true)).setNegativeButton("Cancel",null).show();}
 private void search(String query){if(query.isEmpty())return;android.widget.Toast.makeText(activity,"Finding location…",android.widget.Toast.LENGTH_SHORT).show();worker.execute(()->{JSONArray matches=null;try{if(query.replace(" ","").matches("(?i)[A-Z]{1,2}[0-9][A-Z0-9]?[0-9][A-Z]{2}")){JSONObject place=request("https://api.postcodes.io/postcodes/"+URLEncoder.encode(query.replace(" ",""),"UTF-8")).getJSONObject("result");JSONObject item=new JSONObject();item.put("name",place.getString("postcode"));item.put("admin1",place.optString("admin_district"));item.put("country",place.optString("country"));item.put("latitude",place.getDouble("latitude"));item.put("longitude",place.getDouble("longitude"));matches=new JSONArray().put(item);}else matches=request("https://geocoding-api.open-meteo.com/v1/search?name="+URLEncoder.encode(query,"UTF-8")+"&count=5&language=en&format=json").optJSONArray("results");}catch(Exception ignored){}final JSONArray found=matches;ui.post(()->{if(closed||activity.isFinishing())return;if(found==null||found.length()==0){new AlertDialog.Builder(activity).setMessage("Location not found. Check your connection and try a town or city name.").setPositiveButton("OK",null).show();return;}String[] names=new String[found.length()];for(int i=0;i<names.length;i++){JSONObject item=found.optJSONObject(i);names[i]=item.optString("name")+", "+item.optString("admin1")+", "+item.optString("country");}new AlertDialog.Builder(activity).setTitle("Choose location").setItems(names,(d,n)->{JSONObject item=found.optJSONObject(n);revision++;prefs.edit().putBoolean("automatic",false).putString("town",item.optString("name")).putString("lat",String.valueOf(item.optDouble("latitude"))).putString("lon",String.valueOf(item.optDouble("longitude"))).remove("conditions").remove("updated").apply();lastAttempt=0;display();refresh(true);}).setNegativeButton("Cancel",null).show();});});}
 private static JSONObject request(String url) throws Exception {HttpURLConnection connection=(HttpURLConnection)new URL(url).openConnection();connection.setConnectTimeout(10000);connection.setReadTimeout(10000);try{if(connection.getResponseCode()!=200)throw new java.io.IOException("Weather service unavailable");try(InputStream in=connection.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1){out.write(buffer,0,n);if(out.size()>262144)throw new java.io.IOException("Response too large");}return new JSONObject(out.toString("UTF-8"));}}finally{connection.disconnect();}}
 static String condition(int code){if(code==0)return "Clear";if(code==1)return "Mostly clear";if(code==2)return "Partly cloudy";if(code==3)return "Overcast";if(code==45||code==48)return "Fog";if(code>=51&&code<=57)return "Drizzle";if(code>=61&&code<=67)return "Rain";if(code>=71&&code<=77)return "Snow";if(code>=80&&code<=82)return "Showers";if(code==85||code==86)return "Snow showers";if(code>=95)return "Thunderstorms";return "Conditions unavailable";}
}
