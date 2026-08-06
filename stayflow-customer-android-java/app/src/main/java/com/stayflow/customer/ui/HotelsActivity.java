package com.stayflow.customer.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.stayflow.customer.data.ApiClient;
import com.stayflow.customer.model.Models.Hotel;
import com.stayflow.customer.model.Models.Resp;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HotelsActivity extends AppCompatActivity {
    private LinearLayout hotelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout page = Ui.page(this, "Approved Hotels");

        LinearLayout navigation = new LinearLayout(this);
        navigation.setOrientation(LinearLayout.HORIZONTAL);
        navigation.setPadding(0, 12, 0, 26);

        MaterialButton bookings = new MaterialButton(this);
        bookings.setText("My Bookings");
        bookings.setOnClickListener(v -> startActivity(new Intent(this, BookingsActivity.class)));

        MaterialButton profile = new MaterialButton(this);
        profile.setText("Profile");
        profile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        LinearLayout.LayoutParams navButton = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        navButton.setMargins(0, 0, 10, 0);
        navigation.addView(bookings, navButton);
        LinearLayout.LayoutParams profileParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        profileParams.setMargins(10, 0, 0, 0);
        navigation.addView(profile, profileParams);
        page.addView(navigation);

        TextView helper = Ui.text(this, page, "Choose an approved hotel to see its available rooms.");
        helper.setTextColor(Color.rgb(102, 112, 133));

        hotelList = new LinearLayout(this);
        hotelList.setOrientation(LinearLayout.VERTICAL);
        page.addView(hotelList);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(page);
        setContentView(scroll);
        loadHotels();
    }

    private void loadHotels() {
        ApiClient.create(this).hotels().enqueue(new Callback<Resp<List<Hotel>>>() {
            @Override
            public void onResponse(Call<Resp<List<Hotel>>> call, Response<Resp<List<Hotel>>> response) {
                if (response.body() == null || response.body().data == null) return;
                for (Hotel hotel : response.body().data) {
                    if ("APPROVED".equalsIgnoreCase(hotel.status)) addHotelCard(hotel);
                }
            }

            @Override
            public void onFailure(Call<Resp<List<Hotel>>> call, Throwable error) {
                Ui.toast(HotelsActivity.this, error.getMessage());
            }
        });
    }

    private void addHotelCard(Hotel hotel) {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(28);
        card.setCardElevation(5);
        card.setCardBackgroundColor(Color.WHITE);
        card.setStrokeColor(Color.rgb(234, 236, 240));
        card.setStrokeWidth(1);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(30, 28, 30, 24);

        TextView title = new TextView(this);
        title.setText(hotel.hotelName);
        title.setTextSize(24);
        title.setTextColor(Color.rgb(16, 24, 40));
        title.setTypeface(null, 1);
        content.addView(title);

        TextView details = new TextView(this);
        details.setText(hotel.city + ", " + hotel.state + "\n" + hotel.description + "\n★ " + hotel.rating + "/5");
        details.setTextSize(16);
        details.setTextColor(Color.rgb(71, 84, 103));
        details.setLineSpacing(6, 1f);
        details.setPadding(0, 12, 0, 18);
        content.addView(details);

        MaterialButton rooms = new MaterialButton(this);
        rooms.setText("View available rooms");
        rooms.setOnClickListener(v -> {
            Intent intent = new Intent(this, RoomsActivity.class);
            intent.putExtra("hotelId", hotel.hotelId);
            intent.putExtra("hotelName", hotel.hotelName);
            startActivity(intent);
        });
        content.addView(rooms);
        card.addView(content);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 16);
        hotelList.addView(card, params);
    }
}
