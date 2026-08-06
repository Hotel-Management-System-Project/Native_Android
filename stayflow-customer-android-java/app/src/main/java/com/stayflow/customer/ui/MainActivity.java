package com.stayflow.customer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.stayflow.customer.data.SessionManager;

public class MainActivity extends AppCompatActivity {
    private static final int HOTELS = 101;
    private static final int BOOKINGS = 102;
    private static final int PROFILE = 103;
    private static final int REVIEWS = 104;
    private static final int FRAGMENT_CONTAINER_ID = 0x00A11CE;
    private static final String SELECTED_TAB_KEY = "selected_tab";

    private int fragmentContainerId;
    private TextView screenTitle;
    private LinearLayout header;
    private int selectedTab = HOTELS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Ui.applyHeaderStatusBar(this);

        SessionManager session = new SessionManager(this);
        if (!session.loggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(session.darkMode() ? 0xFF09090B : 0xFFF7F7F8);
        root.addView(Ui.gradientStatusBarSpacer(this));

        header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(22), dp(15), dp(22), dp(14));
        // Use the same deep red as every page hero and the Android status bar.
        header.setBackgroundColor(0xFF991B1B);

        TextView brand = new TextView(this);
        brand.setText("STAYFLOW");
        brand.setTextColor(0xFFEF233C);
        brand.setTextSize(12);
        brand.setLetterSpacing(0.14f);
        brand.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(brand);

        screenTitle = new TextView(this);
        screenTitle.setTextColor(0xFFF8FAFC);
        screenTitle.setTextSize(23);
        screenTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(screenTitle);
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(78)));

        FrameLayout fragmentContainer = new FrameLayout(this);
        // A stable ID allows FragmentManager to restore content after theme recreation.
        fragmentContainerId = FRAGMENT_CONTAINER_ID;
        fragmentContainer.setId(fragmentContainerId);
        root.addView(fragmentContainer, new LinearLayout.LayoutParams(-1, 0, 1));

        BottomNavigationView navigation = new BottomNavigationView(this);
        // Keep every fragment title visible instead of showing only the selected title.
        navigation.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);
        navigation.setBackgroundColor(session.darkMode() ? 0xFF18181B : 0xFFFFFFFF);
        navigation.setItemIconTintList(Ui.navigationColors());
        navigation.setItemTextColor(Ui.navigationColors());
        navigation.setElevation(dp(16));
        navigation.getMenu().add(0, HOTELS, 0, "Hotels")
                .setIcon(android.R.drawable.ic_menu_compass);
        navigation.getMenu().add(0, BOOKINGS, 1, "Bookings")
                .setIcon(android.R.drawable.ic_menu_my_calendar);
        navigation.getMenu().add(0, REVIEWS, 2, "Reviews")
                .setIcon(android.R.drawable.btn_star_big_on);
        navigation.getMenu().add(0, PROFILE, 3, "Profile")
                .setIcon(android.R.drawable.ic_menu_manage);
        navigation.setOnItemSelectedListener(item -> {
            selectedTab = item.getItemId();
            openTab(selectedTab);
            return true;
        });
        root.addView(navigation, new LinearLayout.LayoutParams(-1, dp(72)));

        setContentView(root);
        selectedTab = savedInstanceState == null
                ? HOTELS : savedInstanceState.getInt(SELECTED_TAB_KEY, HOTELS);
        navigation.setSelectedItemId(selectedTab);
        // Some BottomNavigationView versions do not invoke the listener for an
        // item that is already checked, so open it explicitly when required.
        if (getSupportFragmentManager().findFragmentById(fragmentContainerId) == null) {
            openTab(selectedTab);
        }
    }

    private void openTab(int tabId) {
        header.setVisibility(View.GONE);
        if (tabId == BOOKINGS) {
            show("My Bookings", new BookingsFragment());
        } else if (tabId == REVIEWS) {
            show("Reviews", new ReviewsFragment());
        } else if (tabId == PROFILE) {
            show("My Profile", new ProfileFragment());
        } else {
            show("Find your stay", new HotelsFragment());
        }
    }

    @Override
    protected void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        outState.putInt(SELECTED_TAB_KEY, selectedTab);
        super.onSaveInstanceState(outState);
    }

    private void show(String title, Fragment fragment) {
        screenTitle.setText(title);
        getSupportFragmentManager().beginTransaction()
                .replace(fragmentContainerId, fragment)
                .commit();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
