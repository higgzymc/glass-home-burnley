package local.glasshome;

import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
 private final ArrayList<App> apps=new ArrayList<>();
 private final Handler handler=new Handler();
 private LinearLayout page;
 private TextView clock;
 private TextView date;
 private WeatherWidget weather;
 private TextView selectedApp;
 private Bitmap wallpaper;
 private boolean customWallpaper;
 private final ArrayList<String> favourites=new ArrayList<>();
 private static final String DEFAULT_APPS="com.disney.disneyplus,com.amazon.firebat,com.amazon.firetv.youtube,com.mobileiq.demand5,uk.co.bbc.iplayer,ar.tvplayer.tv,com.channel4.ondemand,com.discovery.discoplus,air.ITVMobilePlayer";
 private void loadFavourites(){favourites.clear();String saved=getPreferences(0).getString("favourites",DEFAULT_APPS);if(!saved.isEmpty())favourites.addAll(Arrays.asList(saved.split(",")));}
 private final IdentityHashMap<View,App> favouriteViews=new IdentityHashMap<>();
 private App heldApp;private View heldView;private long selectDown;private boolean held;
 private void favouriteKeys(View tile,App app){favouriteViews.put(tile,app);}
 @Override public boolean dispatchKeyEvent(KeyEvent event){int key=event.getKeyCode();if(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER){if(event.getAction()==KeyEvent.ACTION_DOWN){App app=favouriteViews.get(getCurrentFocus());if(app!=null){if(event.getRepeatCount()==0){heldApp=app;heldView=getCurrentFocus();selectDown=event.getEventTime();held=false;}else held=true;return true;}}else if(event.getAction()==KeyEvent.ACTION_UP&&heldApp!=null){App app=heldApp;View tile=heldView;boolean longPress=held||event.getEventTime()-selectDown>=500;heldApp=null;heldView=null;if(!event.isCanceled()){if(longPress)editFavourite(app);else tile.performClick();}return true;}}return super.dispatchKeyEvent(event);}
 private void editFavourite(App app){boolean pinned=favourites.contains(app.pkg);new android.app.AlertDialog.Builder(this).setTitle(app.label).setItems(new String[]{pinned?"Remove from Home":"Add to Home"},(dialog,which)->{if(pinned)favourites.remove(app.pkg);else favourites.add(app.pkg);getPreferences(0).edit().putString("favourites",android.text.TextUtils.join(",",favourites)).apply();lastPackage=app.pkg;render();Toast.makeText(this,pinned?"Removed from Home":"Added to Home",Toast.LENGTH_SHORT).show();}).setNegativeButton("Cancel",null).show();}

 private boolean library=false;
 private String lastPackage="";
 private static final int WHITE=0xffeefaff, MUTED=0xffabc2ca;
 private boolean resumed;
 private final Runnable tick=new Runnable(){public void run(){updateClock();if(resumed)handler.postDelayed(this,60000-System.currentTimeMillis()%60000+50);}};
 private void updateClock(){Date now=new Date();if(clock!=null)clock.setText(new SimpleDateFormat("H:mm",Locale.UK).format(now));if(date!=null)date.setText(new SimpleDateFormat("EEEE d MMMM",Locale.UK).format(now));}
 private void restartClock(){handler.removeCallbacks(tick);if(resumed)tick.run();}
 private final BroadcastReceiver timeChanged=new BroadcastReceiver(){@Override public void onReceive(Context context,Intent intent){restartClock();}};
 @Override public void onStart(){super.onStart();IntentFilter filter=new IntentFilter();filter.addAction(Intent.ACTION_TIME_CHANGED);filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);filter.addAction(Intent.ACTION_DATE_CHANGED);filter.addAction(Intent.ACTION_SCREEN_ON);registerReceiver(timeChanged,filter);}
 @Override public void onStop(){unregisterReceiver(timeChanged);super.onStop();}
 @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)restartClock();}
 static class App { String pkg,label; Intent intent; Drawable icon; }
 @Override public void onCreate(Bundle b){super.onCreate(b);weather=new WeatherWidget(this); getWindow().getDecorView().setSystemUiVisibility(5894); BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=1;java.io.File custom=new java.io.File(getFilesDir(),"wallpaper.png");if(custom.isFile()){wallpaper=BitmapFactory.decodeFile(custom.getAbsolutePath(),o);customWallpaper=wallpaper!=null;}if(wallpaper==null)wallpaper=BitmapFactory.decodeResource(getResources(),R.drawable.alpine_dusk,o);}
 @Override public void onResume(){super.onResume();loadFavourites();loadApps();render();weather.start();resumed=true;restartClock();}
 @Override public void onPause(){resumed=false;heldApp=null;heldView=null;weather.stop();handler.removeCallbacks(tick);super.onPause();}
 @Override public void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);library=false;lastPackage="";if(resumed){render();restartClock();}}
 @Override public void onDestroy(){weather.destroy();handler.removeCallbacksAndMessages(null);super.onDestroy();}
 @Override public void onBackPressed(){if(library){library=false;render();}}
 @Override public boolean onKeyDown(int key,KeyEvent event){if(key==KeyEvent.KEYCODE_MENU){library=!library;render();return true;}return super.onKeyDown(key,event);}
 private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
 private void loadApps(){
  apps.clear(); PackageManager pm=getPackageManager(); LinkedHashMap<String,ResolveInfo> found=new LinkedHashMap<>();
  for(String cat:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER}){
   for(ResolveInfo r:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(cat),0)) found.putIfAbsent(r.activityInfo.packageName,r);
  }
  for(Map.Entry<String,ResolveInfo> entry:found.entrySet()){
   String pkg=entry.getKey(); if(pkg.equals(getPackageName())||pkg.equals("com.spocky.projengmenu")||pkg.equals("com.amazon.tv.launcher"))continue;
   ResolveInfo r=entry.getValue(); App a=new App();a.pkg=pkg;a.label=r.loadLabel(pm).toString();a.icon=r.loadIcon(pm);
   a.intent=new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(pkg,r.activityInfo.name)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
   apps.add(a);
  }
  apps.sort((a,b)->a.label.compareToIgnoreCase(b.label));
 }
 private GradientDrawable surface(int color,float radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));d.setStroke(dp(1),stroke);return d;}
 private TextView text(String s,int size,int color){TextView t=size==126&&!customWallpaper?new DepthClock(this):new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setFontFeatureSettings("kern");return t;}
 private void render(){
  favouriteViews.clear();
  clock=null;date=null;selectedApp=null;
  FrameLayout root=new FrameLayout(this);root.addView(new Backdrop(this),new FrameLayout.LayoutParams(-1,-1));
  page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(48),dp(28),dp(48),dp(22));root.addView(page,new FrameLayout.LayoutParams(-1,-1));setContentView(root);
  LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
  TextView brand=text("Home",13,WHITE);brand.setLetterSpacing(.04f);header.addView(brand,new LinearLayout.LayoutParams(0,dp(36),1));brand.setGravity(Gravity.CENTER_VERTICAL);
  TextView nav=button("‹  Home",()->{library=false;render();});
  if(library){header.addView(nav,new LinearLayout.LayoutParams(dp(120),dp(36)));
   TextView settings=button("Settings",this::openSettings);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(100),dp(36));sp.leftMargin=dp(10);header.addView(settings,sp);}
  if(!library)header.addView(weather.createView(),new LinearLayout.LayoutParams(-2,dp(36)));
  page.addView(header);
  if(library)renderLibrary(nav);else {renderHome(nav);}
 }
 private TextView button(String label,Runnable action){
  TextView t=text(label,14,WHITE);t.setGravity(Gravity.CENTER);t.setFocusable(true);t.setClickable(true);t.setBackground(surface(0x22ffffff,20,0x25ffffff));
  t.setOnFocusChangeListener((v,f)->{t.setTextColor(f?0xff15223d:WHITE);t.setBackground(surface(f?0xfff5f7ff:0x22ffffff,20,f?WHITE:0x25ffffff));});t.setOnClickListener(v->action.run());return t;
 }
 private void renderHome(View nav){
  LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setGravity(Gravity.CENTER);page.addView(hero,new LinearLayout.LayoutParams(-1,0,1));
  date=text(new SimpleDateFormat("EEEE d MMMM",Locale.UK).format(new Date()),17,WHITE);date.setTypeface(android.graphics.Typeface.create("sans-serif-medium",Typeface.NORMAL));date.setGravity(Gravity.CENTER);hero.addView(date,new LinearLayout.LayoutParams(-1,-2));
  clock=text(new SimpleDateFormat("H:mm",Locale.UK).format(new Date()),126,0xffefc7df);clock.setTypeface(android.graphics.Typeface.create("sans-serif-thin",Typeface.NORMAL));clock.setLetterSpacing(-.035f);clock.setGravity(Gravity.CENTER);clock.setIncludeFontPadding(false);clock.setTranslationY(dp(10));clock.setShadowLayer(dp(8),0,dp(2),0x44000000);hero.addView(clock,new LinearLayout.LayoutParams(-1,dp(150)));
  ArrayList<App> recentApps=new ArrayList<>();
  for(String wanted:favourites){
   for(App a:apps){if(a.pkg.equals(wanted)){recentApps.add(a);break;}}
  }
  int count=recentApps.size();
  LinearLayout dock=new LinearLayout(this);dock.setGravity(Gravity.CENTER_VERTICAL);dock.setPadding(dp(10),dp(9),dp(10),dp(9));dock.setBackground(surface(0x66303848,24,0x40ffffff));
  LinearLayout.LayoutParams dockp=new LinearLayout.LayoutParams(-2,dp(96));dockp.gravity=Gravity.CENTER_HORIZONTAL;dockp.topMargin=dp(8);HorizontalScrollView dockScroll=new HorizontalScrollView(this);dockScroll.setHorizontalScrollBarEnabled(false);dockScroll.setClipToPadding(false);dockScroll.addView(dock,new HorizontalScrollView.LayoutParams(-2,-1));page.addView(dockScroll,dockp);
  View first=null,remember=null;
  for(int i=0;i<count;i++){App a=recentApps.get(i);View card=dockCard(a);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(count>7?62:78),-1);cp.leftMargin=dp(4);cp.rightMargin=dp(4);dock.addView(card,cp);if(first==null)first=card;if(a.pkg.equals(lastPackage))remember=card;}
  if(count==0){TextView empty=text("Open All Apps to get started.",18,WHITE);dock.addView(empty);}
  TextView allTile=dockAction("▦", "All Apps",()->{library=true;render();});LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(72),-1);ap.leftMargin=dp(5);dock.addView(allTile,ap);
  TextView settingsTile=dockAction("⚙", "Settings",this::openSettings);LinearLayout.LayoutParams stp=new LinearLayout.LayoutParams(dp(72),-1);stp.leftMargin=dp(8);dock.addView(settingsTile,stp);
  selectedApp=text("",14,WHITE);selectedApp.setGravity(Gravity.CENTER);LinearLayout.LayoutParams selp=new LinearLayout.LayoutParams(-1,dp(26));selp.topMargin=dp(7);page.addView(selectedApp,selp);
  View target=remember!=null?remember:first!=null?first:allTile;page.post(()->target.requestFocus());
 }
 private View dockCard(App a){
  FrameLayout tile=new FrameLayout(this);tile.setFocusable(true);tile.setClickable(true);tile.setContentDescription(a.label+". Hold Select to change Home favourites.");tile.setOnLongClickListener(v->{editFavourite(a);return true;});favouriteKeys(tile,a);
  tile.setBackground(surface(0x25101825,17,0x20ffffff));
  ImageView icon=new ImageView(this);icon.setImageDrawable(a.icon);icon.setScaleType(ImageView.ScaleType.FIT_CENTER);FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(dp(50),dp(50),Gravity.CENTER);tile.addView(icon,ip);
  tile.setOnFocusChangeListener((v,f)->{tile.setBackground(surface(f?0x66ffffff:0x25101825,17,f?WHITE:0x20ffffff));tile.animate().scaleX(f?1.08f:1).scaleY(f?1.08f:1).setDuration(120).start();if(f&&selectedApp!=null)selectedApp.setText(a.label);});
  tile.setOnClickListener(v->{try{lastPackage=a.pkg;startActivity(a.intent);}catch(Exception e){Toast.makeText(this,"This app could not be opened.",Toast.LENGTH_SHORT).show();}});return tile;
 }
 private TextView dockAction(String glyph,String label,Runnable action){
  TextView tile=text(glyph,30,WHITE);tile.setGravity(Gravity.CENTER);tile.setFocusable(true);tile.setClickable(true);tile.setContentDescription(label);tile.setBackground(surface(0x25ffffff,17,0x20ffffff));
  tile.setOnFocusChangeListener((v,f)->{tile.setTextColor(f?0xff182833:WHITE);tile.setBackground(surface(f?0xfff3e8f2:0x25ffffff,17,f?WHITE:0x20ffffff));if(f&&selectedApp!=null)selectedApp.setText(label);});tile.setOnClickListener(v->action.run());return tile;
 }
 private View card(App a){
  LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(7),dp(7),dp(7),dp(5));card.setFocusable(true);card.setClickable(true);card.setContentDescription(a.label+". Hold Select to add or remove from Home.");card.setOnLongClickListener(v->{editFavourite(a);return true;});favouriteKeys(card,a);
  card.setBackground(surface(0x09ffffff,19,0x00ffffff));
  ImageView icon=new ImageView(this);icon.setImageDrawable(a.icon);icon.setScaleType(ImageView.ScaleType.FIT_CENTER);icon.setPadding(dp(5),dp(5),dp(5),dp(5));icon.setBackground(surface(0x30ffffff,19,0x20ffffff));icon.setClipToOutline(true);card.addView(icon,new LinearLayout.LayoutParams(dp(78),dp(78)));
  TextView label=text(a.label,13,WHITE);label.setGravity(Gravity.CENTER);label.setMaxLines(1);label.setEllipsize(android.text.TextUtils.TruncateAt.END);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(26));lp.topMargin=dp(6);card.addView(label,lp);
  card.setOnFocusChangeListener((v,f)->{card.setBackground(surface(f?0x36ffffff:0x09ffffff,19,f?0xddffffff:0x00ffffff));card.animate().scaleX(f?1.08f:1).scaleY(f?1.08f:1).setDuration(120).start();});
  card.setOnClickListener(v->{try{lastPackage=a.pkg;startActivity(a.intent);}catch(Exception e){Toast.makeText(this,"This app could not be opened.",Toast.LENGTH_SHORT).show();}});return card;
 }
 private void renderLibrary(View nav){
  TextView title=text("All Apps",32,WHITE);title.setTypeface(android.graphics.Typeface.create("sans-serif-light",Typeface.NORMAL));LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.topMargin=dp(22);page.addView(title,tp);
  TextView subtitle=text(apps.size()+" apps  ·  Hold Select to add or remove from Home",13,MUTED);page.addView(subtitle);
  ScrollView scroll=new ScrollView(this);scroll.setClipToPadding(false);scroll.setPadding(0,dp(12),0,dp(6));LinearLayout.LayoutParams scp=new LinearLayout.LayoutParams(-1,0,1);scp.topMargin=dp(8);page.addView(scroll,scp);
  LinearLayout rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);scroll.addView(rows);View first=null,remember=null;
  for(int i=0;i<apps.size();i+=6){LinearLayout row=new LinearLayout(this);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(130));rp.bottomMargin=dp(10);rows.addView(row,rp);
   for(int j=0;j<6;j++){LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-1,1);cp.setMargins(dp(5),dp(3),dp(5),dp(3));if(i+j<apps.size()){App a=apps.get(i+j);View c=card(a);row.addView(c,cp);if(first==null)first=c;if(a.pkg.equals(lastPackage))remember=c;}else row.addView(new View(this),cp);}
  }
  View target=remember!=null?remember:first!=null?first:nav;page.post(()->target.requestFocus());
 }
 private void openSettings(){String[] labels={"My Fire TV","Network","Display & Sounds","Controllers & Bluetooth","Applications","Preferences","Accessibility","Account","Weather location"};String[] pages={"device.DeviceActivity","network.NetworkActivity","display_sounds.DisplayAndSoundsActivity","controllers_bluetooth_devices.ControllersAndBluetoothActivity","applications.ApplicationsActivity","preferences.PreferencesActivity","accessibility.AccessibilityActivity","my_account.MyAccountActivity"};new android.app.AlertDialog.Builder(this).setTitle("Fire TV settings").setItems(labels,(dialog,index)->{if(index==8){weather.chooseLocation();return;}try{startActivity(new Intent().setComponent(new ComponentName("com.amazon.tv.settings.v2","com.amazon.tv.settings.v2.tv."+pages[index])));}catch(ActivityNotFoundException|SecurityException e){try{startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));}catch(ActivityNotFoundException|SecurityException ignored){Toast.makeText(this,"This settings page is unavailable on this Fire TV.",Toast.LENGTH_LONG).show();}}}).setNegativeButton("Close",null).show();}
 // Clip clock glyphs against the dark foreground ridge in the bundled dusk photo.
 private class DepthClock extends TextView {
  private final Path mountain=new Path();private final int[] position=new int[2],rootPosition=new int[2];private final float[] ridge;
  DepthClock(Context c){super(c);ridge=new float[wallpaper.getWidth()];for(int x=0;x<ridge.length;x++){ridge[x]=wallpaper.getHeight();for(int y=(int)(wallpaper.getHeight()*.46f);y<wallpaper.getHeight()*.76f;y++){int pixel=wallpaper.getPixel(x,y);int r=Color.red(pixel),g=Color.green(pixel),b=Color.blue(pixel);if(r<85&&g<95&&b>r*1.08f){ridge[x]=y;break;}}}}
  @Override protected void onDraw(Canvas canvas){View root=(View)getParent().getParent().getParent();float w=root.getWidth(),h=root.getHeight();float scale=Math.max(w/wallpaper.getWidth(),h/wallpaper.getHeight());getLocationOnScreen(position);root.getLocationOnScreen(rootPosition);float ox=(w-wallpaper.getWidth()*scale)/2+rootPosition[0]-position[0],oy=(h-wallpaper.getHeight()*scale)/2+rootPosition[1]-position[1];mountain.reset();for(int x=0;x<ridge.length;x++){if(x==0)mountain.moveTo(ox,oy+ridge[x]*scale);else mountain.lineTo(ox+x*scale,oy+ridge[x]*scale);}mountain.lineTo(ox+w,oy+h);mountain.lineTo(ox,oy+h);mountain.close();int save=canvas.save();canvas.clipOutPath(mountain);super.onDraw(canvas);canvas.restoreToCount(save);}
 }
 private class Backdrop extends View{
  Paint p=new Paint(3);Backdrop(Context c){super(c);}
  @Override protected void onDraw(Canvas c){float w=getWidth(),h=getHeight();p.setColor(0xff080c10);c.drawRect(0,0,w,h,p);
   if(wallpaper!=null){float scale=Math.max(w/wallpaper.getWidth(),h/wallpaper.getHeight());float imageW=wallpaper.getWidth()*scale,imageH=wallpaper.getHeight()*scale;c.drawBitmap(wallpaper,null,new RectF((w-imageW)/2,(h-imageH)/2,(w+imageW)/2,(h+imageH)/2),p);}
   p.setShader(new LinearGradient(0,0,0,h,new int[]{0x33080a10,0x10070b11,0x190b1119,0x66080b10},new float[]{0,.34f,.62f,1},Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);
   p.setShader(new LinearGradient(0,0,w,0,new int[]{0x303f102b,0x00000000,0x20204f67},null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
  }
 }
}
