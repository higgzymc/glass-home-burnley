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
 private TextView selectedApp;
 private Bitmap teamPhoto;
 private boolean library=false;
 private String lastPackage="";
 private static final int WHITE=0xffeefaff, MUTED=0xffabc2ca;
 private final Runnable tick=new Runnable(){ public void run(){ if(clock!=null) clock.setText(new SimpleDateFormat("HH:mm",Locale.UK).format(new Date())); handler.postDelayed(this,15000); }};
 static class App { String pkg,label; Intent intent; Drawable icon; }
 @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().getDecorView().setSystemUiVisibility(5894); BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=2;teamPhoto=BitmapFactory.decodeResource(getResources(),R.drawable.burnley_team,o);handler.post(tick);}
 @Override public void onResume(){super.onResume();loadApps();render();}
 @Override public void onNewIntent(Intent i){super.onNewIntent(i);library=false;lastPackage="";}
 @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}
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
 private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setFontFeatureSettings("kern");return t;}
 private void render(){
  FrameLayout root=new FrameLayout(this);root.addView(new Backdrop(this),new FrameLayout.LayoutParams(-1,-1));
  page=new LinearLayout(this);page.setOrientation(1);page.setPadding(dp(48),dp(28),dp(48),dp(22));root.addView(page,new FrameLayout.LayoutParams(-1,-1));setContentView(root);
  LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
  TextView brand=text("BURNLEY  /  HOME",13,WHITE);brand.setLetterSpacing(.16f);header.addView(brand,new LinearLayout.LayoutParams(0,dp(36),1));brand.setGravity(Gravity.CENTER_VERTICAL);
  TextView nav=button("‹  Home",()->{library=false;render();});
  if(library){header.addView(nav,new LinearLayout.LayoutParams(dp(120),dp(36)));
   TextView settings=button("Settings",this::openSettings);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(100),dp(36));sp.leftMargin=dp(10);header.addView(settings,sp);}
  page.addView(header);
  if(library)renderLibrary(nav);else renderHome(nav);
 }
 private TextView button(String label,Runnable action){
  TextView t=text(label,14,WHITE);t.setGravity(Gravity.CENTER);t.setFocusable(true);t.setClickable(true);t.setBackground(surface(0x22ffffff,20,0x25ffffff));
  t.setOnFocusChangeListener((v,f)->{t.setTextColor(f?0xff15223d:WHITE);t.setBackground(surface(f?0xfff5f7ff:0x22ffffff,20,f?WHITE:0x25ffffff));});t.setOnClickListener(v->action.run());return t;
 }
 private void renderHome(View nav){
  LinearLayout hero=new LinearLayout(this);hero.setOrientation(1);hero.setGravity(Gravity.CENTER);page.addView(hero,new LinearLayout.LayoutParams(-1,0,1));
  TextView date=text(new SimpleDateFormat("EEEE, d MMMM",Locale.UK).format(new Date()),17,WHITE);date.setTypeface(android.graphics.Typeface.create("sans-serif-medium",0));date.setGravity(Gravity.CENTER);hero.addView(date,new LinearLayout.LayoutParams(-1,-2));
  clock=text(new SimpleDateFormat("HH:mm",Locale.UK).format(new Date()),126,WHITE);clock.setTypeface(android.graphics.Typeface.create("sans-serif-condensed-medium",0));clock.setLetterSpacing(-.035f);clock.setGravity(Gravity.CENTER);clock.setIncludeFontPadding(false);clock.setShadowLayer(dp(18),0,dp(4),0x99000000);hero.addView(clock,new LinearLayout.LayoutParams(-1,dp(150)));
  TextView sub=text("UP THE CLARETS",12,0xffd3e6ed);sub.setLetterSpacing(.28f);sub.setGravity(Gravity.CENTER);LinearLayout.LayoutParams su=new LinearLayout.LayoutParams(-1,-2);su.topMargin=dp(8);hero.addView(sub,su);
  ArrayList<App> recentApps=new ArrayList<>();
  for(String wanted:new String[]{"disney","tivimate","youtube","luna","xbox"}){
   for(App a:apps){String name=a.label.toLowerCase(Locale.ROOT);if(name.contains(wanted)||(wanted.equals("tivimate")&&a.pkg.equals("ar.tvplayer.tv"))||(wanted.equals("xbox")&&a.pkg.equals("gamepass.fire.tv"))){recentApps.add(a);break;}}
  }
  int count=recentApps.size();
  LinearLayout dock=new LinearLayout(this);dock.setGravity(Gravity.CENTER_VERTICAL);dock.setPadding(dp(10),dp(9),dp(10),dp(9));dock.setBackground(surface(0x7a425b61,24,0x99b4e3e8));
  LinearLayout.LayoutParams dockp=new LinearLayout.LayoutParams(-1,dp(134));dockp.topMargin=dp(8);page.addView(dock,dockp);
  View first=null,remember=null;
  for(int i=0;i<count;i++){App a=recentApps.get(i);View card=dockCard(a);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-1,1);cp.leftMargin=dp(4);cp.rightMargin=dp(4);dock.addView(card,cp);if(first==null)first=card;if(a.pkg.equals(lastPackage))remember=card;}
  if(count==0){TextView empty=text("Open All Apps to get started.",18,WHITE);dock.addView(empty);}
  TextView allTile=dockAction("▦", "All Apps",()->{library=true;render();});LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(112),-1);ap.leftMargin=dp(5);dock.addView(allTile,ap);
  TextView settingsTile=dockAction("⚙", "Settings",this::openSettings);LinearLayout.LayoutParams stp=new LinearLayout.LayoutParams(dp(112),-1);stp.leftMargin=dp(8);dock.addView(settingsTile,stp);
  selectedApp=text("",14,WHITE);selectedApp.setGravity(Gravity.CENTER);LinearLayout.LayoutParams selp=new LinearLayout.LayoutParams(-1,dp(26));selp.topMargin=dp(7);page.addView(selectedApp,selp);
  TextView credit=text("BURNLEY FOOTBALL CLUB",10,MUTED);credit.setGravity(Gravity.CENTER);credit.setLetterSpacing(.13f);page.addView(credit,new LinearLayout.LayoutParams(-1,dp(18)));
  View target=remember!=null?remember:first!=null?first:nav;page.post(()->target.requestFocus());
 }
 private View dockCard(App a){
  FrameLayout tile=new FrameLayout(this);tile.setFocusable(true);tile.setClickable(true);tile.setContentDescription(a.label);
  tile.setBackground(surface(0x6c091018,17,0x5596bec6));
  ImageView icon=new ImageView(this);icon.setImageDrawable(a.icon);icon.setScaleType(ImageView.ScaleType.FIT_CENTER);FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(dp(72),dp(72),Gravity.CENTER);tile.addView(icon,ip);
  tile.setOnFocusChangeListener((v,f)->{tile.setBackground(surface(f?0xffd8f6f8:0x6c091018,17,f?WHITE:0x5596bec6));tile.animate().scaleX(f?1.035f:1).scaleY(f?1.035f:1).setDuration(120).start();if(f&&selectedApp!=null)selectedApp.setText(a.label);});
  tile.setOnClickListener(v->{try{lastPackage=a.pkg;startActivity(a.intent);}catch(Exception e){Toast.makeText(this,"This app could not be opened.",Toast.LENGTH_SHORT).show();}});return tile;
 }
 private TextView dockAction(String glyph,String label,Runnable action){
  TextView tile=text(glyph,42,WHITE);tile.setGravity(Gravity.CENTER);tile.setFocusable(true);tile.setClickable(true);tile.setContentDescription(label);tile.setBackground(surface(0x5a25404a,17,0x5596bec6));
  tile.setOnFocusChangeListener((v,f)->{tile.setTextColor(f?0xff182833:WHITE);tile.setBackground(surface(f?0xffd8f6f8:0x5a25404a,17,f?WHITE:0x5596bec6));if(f&&selectedApp!=null)selectedApp.setText(label);});tile.setOnClickListener(v->action.run());return tile;
 }
 private View card(App a){
  LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setGravity(Gravity.CENTER);card.setPadding(dp(7),dp(7),dp(7),dp(5));card.setFocusable(true);card.setClickable(true);card.setContentDescription(a.label);
  card.setBackground(surface(0x09ffffff,19,0x00ffffff));
  ImageView icon=new ImageView(this);icon.setImageDrawable(a.icon);icon.setScaleType(ImageView.ScaleType.FIT_CENTER);icon.setPadding(dp(5),dp(5),dp(5),dp(5));icon.setBackground(surface(0x30ffffff,19,0x20ffffff));icon.setClipToOutline(true);card.addView(icon,new LinearLayout.LayoutParams(dp(78),dp(78)));
  TextView label=text(a.label,13,WHITE);label.setGravity(Gravity.CENTER);label.setMaxLines(1);label.setEllipsize(android.text.TextUtils.TruncateAt.END);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(26));lp.topMargin=dp(6);card.addView(label,lp);
  card.setOnFocusChangeListener((v,f)->{card.setBackground(surface(f?0x36ffffff:0x09ffffff,19,f?0xddffffff:0x00ffffff));card.animate().scaleX(f?1.035f:1).scaleY(f?1.035f:1).setDuration(120).start();});
  card.setOnClickListener(v->{try{lastPackage=a.pkg;startActivity(a.intent);}catch(Exception e){Toast.makeText(this,"This app could not be opened.",Toast.LENGTH_SHORT).show();}});return card;
 }
 private void renderLibrary(View nav){
  TextView title=text("All Apps",32,WHITE);title.setTypeface(android.graphics.Typeface.create("sans-serif-light",0));LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.topMargin=dp(22);page.addView(title,tp);
  TextView subtitle=text(apps.size()+" apps  ·  A–Z",13,MUTED);page.addView(subtitle);
  ScrollView scroll=new ScrollView(this);scroll.setClipToPadding(false);scroll.setPadding(0,dp(12),0,dp(6));LinearLayout.LayoutParams scp=new LinearLayout.LayoutParams(-1,0,1);scp.topMargin=dp(8);page.addView(scroll,scp);
  LinearLayout rows=new LinearLayout(this);rows.setOrientation(1);scroll.addView(rows);View first=null,remember=null;
  for(int i=0;i<apps.size();i+=6){LinearLayout row=new LinearLayout(this);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(130));rp.bottomMargin=dp(10);rows.addView(row,rp);
   for(int j=0;j<6;j++){LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-1,1);cp.setMargins(dp(5),dp(3),dp(5),dp(3));if(i+j<apps.size()){App a=apps.get(i+j);View c=card(a);row.addView(c,cp);if(first==null)first=c;if(a.pkg.equals(lastPackage))remember=c;}else row.addView(new View(this),cp);}
  }
  View target=remember!=null?remember:first!=null?first:nav;page.post(()->target.requestFocus());
 }
 private void openSettings(){new android.app.AlertDialog.Builder(this).setTitle("Glass Home").setItems(new String[]{"Fire TV settings","Wallpaper information"},(d,n)->{if(n==0){try{startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));}catch(Exception e){Toast.makeText(this,"Settings is available in All Apps.",Toast.LENGTH_SHORT).show();}}else new android.app.AlertDialog.Builder(this).setTitle("Burnley wallpaper").setMessage("Team photo supplied by the device owner. Displayed with a dark overlay for clock and app readability. Independent personal fan theme; not affiliated with Burnley FC or Apple.").setPositiveButton("Done",null).show();}).show();}
 private class Backdrop extends View{
  Paint p=new Paint(3);Backdrop(Context c){super(c);}
  @Override protected void onDraw(Canvas c){float w=getWidth(),h=getHeight();p.setColor(0xff080c10);c.drawRect(0,0,w,h,p);
   if(teamPhoto!=null){float scale=Math.max(w/teamPhoto.getWidth(),h/teamPhoto.getHeight());float imageW=teamPhoto.getWidth()*scale,imageH=teamPhoto.getHeight()*scale;c.drawBitmap(teamPhoto,null,new RectF((w-imageW)/2,(h-imageH)/2,(w+imageW)/2,(h+imageH)/2),p);}
   p.setShader(new LinearGradient(0,0,0,h,new int[]{0xbd080a10,0x82070b11,0x790b1119,0x9c080b10},new float[]{0,.34f,.62f,1},Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);
   p.setShader(new LinearGradient(0,0,w,0,new int[]{0x303f102b,0x00000000,0x20204f67},null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
  }
 }
}
