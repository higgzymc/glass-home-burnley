package local.glasshome;
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.content.Intent;
public class HomeService extends AccessibilityService {
 private long last;
 public void onAccessibilityEvent(AccessibilityEvent e) {
  String screen=e.getClassName()==null?"":e.getClassName().toString();
  if(e.getPackageName()!=null && "com.amazon.tv.launcher".contentEquals(e.getPackageName()) && screen.contains("HomeActivity") && android.os.SystemClock.elapsedRealtime()-last>800) {
   last=android.os.SystemClock.elapsedRealtime();
   startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
  }
 }
 public void onInterrupt() {}
}
