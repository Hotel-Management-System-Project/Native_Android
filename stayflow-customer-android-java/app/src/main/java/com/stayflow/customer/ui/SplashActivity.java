package com.stayflow.customer.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.stayflow.customer.data.SessionManager;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DURATION_MS = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.rgb(9, 10, 13));

        TextView mark = new TextView(this);
        mark.setText("S");
        mark.setTextColor(Color.WHITE);
        mark.setTextSize(40);
        mark.setGravity(Gravity.CENTER);
        mark.setTypeface(null, 1);
        mark.setBackgroundResource(com.stayflow.customer.R.drawable.splash_mark);
        mark.getBackground().setTint(Color.rgb(239, 35, 60));
        root.addView(mark, new LinearLayout.LayoutParams(150, 150));

        TextView title = new TextView(this);
        title.setText("StayFlow");
        title.setTextColor(Color.WHITE);
        title.setTextSize(32);
        title.setTypeface(null, 1);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 28, 0, 8);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Your next stay starts here");
        subtitle.setTextColor(Color.rgb(174, 177, 185));
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);

        setContentView(root);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager session = new SessionManager(this);
            Intent destination = new Intent(
                    this,
                    session.loggedIn() ? MainActivity.class : LoginActivity.class
            );
            startActivity(destination);
            finish();
        }, SPLASH_DURATION_MS);
    }
}
