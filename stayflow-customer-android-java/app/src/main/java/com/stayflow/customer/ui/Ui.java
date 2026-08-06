package com.stayflow.customer.ui;
import android.app.Activity;import android.content.*;import android.content.res.ColorStateList;import android.graphics.Color;import android.graphics.drawable.ColorDrawable;import android.graphics.drawable.GradientDrawable;import android.view.*;import android.widget.*;import androidx.appcompat.app.AppCompatActivity;import com.google.android.material.button.MaterialButton;import com.google.android.material.card.MaterialCardView;import com.google.android.material.textfield.TextInputEditText;import com.google.android.material.textfield.TextInputLayout;import com.stayflow.customer.data.SessionManager;
public final class Ui {
 public static LinearLayout page(AppCompatActivity a,String title){LinearLayout p=new LinearLayout(a);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(36,40,36,30);p.setBackgroundColor(Color.rgb(9,10,13));TextView h=new TextView(a);h.setText(title);h.setTextSize(28);h.setTextColor(Color.rgb(248,250,252));h.setTypeface(null,1);p.addView(h,new LinearLayout.LayoutParams(-1,-2));return p;}
 public static TextInputEditText input(AppCompatActivity a,LinearLayout p,String hint){TextInputLayout box=new TextInputLayout(a);TextInputEditText e=new TextInputEditText(a);e.setHint(hint);box.addView(e);p.addView(box,new LinearLayout.LayoutParams(-1,-2));return e;}
 public static MaterialButton button(AppCompatActivity a,LinearLayout p,String text){MaterialButton b=new MaterialButton(a);b.setText(text);p.addView(b,new LinearLayout.LayoutParams(-1,-2));return b;}
 public static TextView text(AppCompatActivity a,LinearLayout p,String value){TextView t=new TextView(a);t.setText(value);t.setTextSize(17);t.setPadding(8,16,8,16);p.addView(t);return t;}
 public static void toast(Context c,String s){Toast.makeText(c,s,Toast.LENGTH_LONG).show();}
 public static MaterialButton primaryButton(Context c,String text){MaterialButton b=new MaterialButton(c);b.setText(text);b.setTextSize(15);b.setTextColor(Color.WHITE);b.setCornerRadius(24);b.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(239,35,60)));return b;}
 public static ColorStateList navigationColors(){return new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{Color.rgb(225,29,46),Color.rgb(113,113,122)});}
 /** Allows a custom gradient view to be drawn behind the battery/time area. */
 public static void applyHeaderStatusBar(Activity activity){
  Window window=activity.getWindow();
  window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
  window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
  window.setStatusBarColor(Color.TRANSPARENT);
  View decor=window.getDecorView();
  int flags=decor.getSystemUiVisibility();
  flags|=View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
  flags&=~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
  decor.setSystemUiVisibility(flags);
 }
 /** Occupies the system-bar inset and paints the same gradient as page headers. */
 public static View gradientStatusBarSpacer(Context context){
  View spacer=new View(context);
  GradientDrawable gradient=new GradientDrawable(
    GradientDrawable.Orientation.LEFT_RIGHT,
    new int[]{Color.rgb(153,27,27),Color.rgb(239,35,60),Color.rgb(251,113,133)});
  spacer.setBackground(gradient);
  int id=context.getResources().getIdentifier("status_bar_height","dimen","android");
  int height=id>0?context.getResources().getDimensionPixelSize(id):Math.round(24*context.getResources().getDisplayMetrics().density);
  spacer.setLayoutParams(new LinearLayout.LayoutParams(-1,height));
  return spacer;
 }
 public static void applySavedTheme(View view,Context context){
  if(view==null||!new SessionManager(context).darkMode())return;

  // Change white cards to a dark surface and retain their visible border.
  if(view instanceof MaterialCardView){
   MaterialCardView card=(MaterialCardView)view;
   int color=card.getCardBackgroundColor().getDefaultColor();
   if(isLightNeutral(color)){
    card.setCardBackgroundColor(Color.rgb(24,24,27));
    card.setStrokeColor(Color.rgb(63,63,70));
   }
  }else if(view.getBackground() instanceof ColorDrawable){
   int color=((ColorDrawable)view.getBackground()).getColor();
   if(isLightNeutral(color))view.setBackgroundColor(Color.rgb(9,9,11));
  }else if(view.getBackground() instanceof GradientDrawable){
   // Inputs, badges and panels use rounded GradientDrawable backgrounds.
   GradientDrawable drawable=(GradientDrawable)view.getBackground();
   ColorStateList colors=drawable.getColor();
   if(colors!=null&&isLightNeutral(colors.getDefaultColor())){
    drawable.setColor(Color.rgb(39,39,42));
    drawable.setStroke(1,Color.rgb(63,63,70));
   }
  }

  // Material buttons use backgroundTint instead of a normal background color.
  if(view instanceof MaterialButton){
   MaterialButton button=(MaterialButton)view;
   ColorStateList tint=button.getBackgroundTintList();
   if(tint!=null&&isLightNeutral(tint.getDefaultColor())){
    button.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(39,39,42)));
    button.setStrokeColor(ColorStateList.valueOf(Color.rgb(63,63,70)));
   }
  }

  if(view instanceof Spinner){
   ((Spinner)view).setPopupBackgroundDrawable(new ColorDrawable(Color.rgb(39,39,42)));
  }

  if(view instanceof TextView){
   TextView text=(TextView)view;
   int color=text.getCurrentTextColor();
   if(isDarkNeutral(color))text.setTextColor(Color.rgb(244,244,245));
   else if(isMediumNeutral(color))text.setTextColor(Color.rgb(161,161,170));
   if(text instanceof EditText)((EditText)text).setHintTextColor(Color.rgb(161,161,170));
  }

  if(view instanceof ViewGroup){
   ViewGroup group=(ViewGroup)view;
   for(int i=0;i<group.getChildCount();i++)applySavedTheme(group.getChildAt(i),context);
  }
 }
 private static boolean isLightNeutral(int color){float[] hsv=new float[3];Color.colorToHSV(color,hsv);return hsv[1]<0.15f&&hsv[2]>0.82f;}
 private static boolean isDarkNeutral(int color){float[] hsv=new float[3];Color.colorToHSV(color,hsv);return hsv[1]<0.18f&&hsv[2]<0.38f;}
 private static boolean isMediumNeutral(int color){float[] hsv=new float[3];Color.colorToHSV(color,hsv);return hsv[1]<0.18f&&hsv[2]>=0.38f&&hsv[2]<0.72f;}
}
