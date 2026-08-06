package com.stayflow.customer.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.stayflow.customer.data.ReviewApiClient;
import com.stayflow.customer.model.Models.Review;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Read-only page containing customer ratings and comments for one hotel. */
public class HotelReviewsActivity extends AppCompatActivity {
    private LinearLayout list;
    private TextView summary;
    private int hotelId;
    private String hotelName;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Ui.applyHeaderStatusBar(this);
        hotelId = getIntent().getIntExtra("hotelId", 0);
        hotelName = getIntent().getStringExtra("hotelName");
        if (hotelName == null || hotelName.isBlank()) hotelName = "Selected hotel";

        LinearLayout page = vertical();
        page.setBackgroundColor(0xFFF7F7F8);
        page.addView(Ui.gradientStatusBarSpacer(this));
        page.addView(header());

        LinearLayout body = vertical();
        body.setPadding(dp(18), dp(20), dp(18), dp(32));
        summary = text("Loading customer ratings...", 15, 0xFF71717A, false);
        body.addView(summary, bottom(16));
        list = vertical();
        ProgressBar progress = new ProgressBar(this);
        list.addView(progress, new LinearLayout.LayoutParams(-1, dp(52)));
        body.addView(list);
        page.addView(body);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(page);
        setContentView(scroll);
        Ui.applySavedTheme(scroll, this);
        loadReviews();
    }

    private View header() {
        LinearLayout hero = vertical();
        hero.setPadding(dp(18), dp(13), dp(18), dp(24));
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF991B1B, 0xFFEF233C, 0xFFFB7185});
        gradient.setCornerRadii(new float[]{0,0,0,0,dp(28),dp(28),dp(28),dp(28)});
        hero.setBackground(gradient);

        LinearLayout toolbar = horizontal();
        MaterialButton back = new MaterialButton(this);
        back.setText("‹"); back.setTextSize(29); back.setTextColor(Color.WHITE);
        back.setCornerRadius(dp(25));
        back.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0x33FFFFFF));
        back.setOnClickListener(v -> finish());
        toolbar.addView(back, new LinearLayout.LayoutParams(dp(52), dp(52)));
        TextView title = text("Reviews & ratings", 19, Color.WHITE, true);
        title.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, dp(52), 1f);
        titleParams.setMargins(dp(13), 0, 0, 0);
        toolbar.addView(title, titleParams);
        hero.addView(toolbar);
        TextView hotel = text(hotelName, 29, Color.WHITE, true);
        hotel.setPadding(0, dp(15), 0, dp(3));
        hero.addView(hotel);
        hero.addView(text("Ratings shared by verified StayFlow customers", 14, 0xFFFFE4E6, false));
        return hero;
    }

    private void loadReviews() {
        ReviewApiClient.create().reviewsByHotel(hotelId).enqueue(new Callback<List<Review>>() {
            @Override public void onResponse(@NonNull Call<List<Review>> call,
                                             @NonNull Response<List<Review>> response) {
                list.removeAllViews();
                List<Review> reviews = response.body();
                if (!response.isSuccessful() || reviews == null || reviews.isEmpty()) {
                    summary.setText("No customer ratings yet");
                    showEmpty();
                    return;
                }
                double total = 0;
                for (Review review : reviews) total += review.rating == null ? 0 : review.rating;
                double average = total / reviews.size();
                summary.setText(String.format(Locale.ENGLISH,
                        "★ %.1f out of 5  ·  %d customer review%s",
                        average, reviews.size(), reviews.size() == 1 ? "" : "s"));
                summary.setTextColor(0xFFE11D2E);
                summary.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                for (Review review : reviews) list.addView(reviewCard(review));
                Ui.applySavedTheme(list, HotelReviewsActivity.this);
            }

            @Override public void onFailure(@NonNull Call<List<Review>> call,
                                            @NonNull Throwable error) {
                list.removeAllViews();
                summary.setText("Unable to load ratings");
                TextView message = text("Start the Express review server on port 4000 and try again.",
                        14, 0xFFB91C1C, false);
                list.addView(message);
                Ui.applySavedTheme(list, HotelReviewsActivity.this);
            }
        });
    }

    private View reviewCard(Review review) {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(dp(19)); card.setCardElevation(dp(1));
        card.setCardBackgroundColor(Color.WHITE); card.setStrokeColor(0xFFE4E4E7); card.setStrokeWidth(1);
        LinearLayout box = vertical();
        box.setPadding(dp(16), dp(15), dp(16), dp(15));
        int rating = review.rating == null ? 0 : review.rating;
        LinearLayout top = horizontal();
        TextView customer = text(customerName(review), 15, 0xFF18181B, true);
        top.addView(customer, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView score = text("★ " + rating + "/5", 14, 0xFFB45309, true);
        score.setPadding(dp(10), dp(6), dp(10), dp(6));
        score.setBackground(rounded(0xFFFEF3C7, 12));
        top.addView(score);
        box.addView(top, bottom(10));
        box.addView(text(stars(rating), 20, 0xFFFFA000, true), bottom(7));
        box.addView(text(review.comment == null || review.comment.isBlank()
                ? "No written comment" : review.comment, 14, 0xFF52525B, false));
        box.addView(text("Verified booking #" + review.booking_id, 11, 0xFF71717A, false));
        card.addView(box);
        card.setLayoutParams(bottom(12));
        return card;
    }

    /** Uses the customer name joined by the review API and supports older response formats. */
    private String customerName(Review review) {
        if (review.customer_name != null && !review.customer_name.isBlank()) {
            return review.customer_name;
        }
        if (review.full_name != null && !review.full_name.isBlank()) {
            return review.full_name;
        }
        if (review.user_name != null && !review.user_name.isBlank()) {
            return review.user_name;
        }
        return "Customer #" + review.user_id;
    }

    private void showEmpty() {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(dp(20)); card.setCardElevation(0); card.setCardBackgroundColor(Color.WHITE);
        LinearLayout box = vertical(); box.setGravity(Gravity.CENTER);
        box.setPadding(dp(20), dp(48), dp(20), dp(48));
        box.addView(text("☆", 40, 0xFFE11D2E, true));
        box.addView(text("Be the first to review this hotel", 18, 0xFF18181B, true));
        card.addView(box); list.addView(card);
        Ui.applySavedTheme(list, this);
    }

    private String stars(int count) { StringBuilder value=new StringBuilder(); for(int i=1;i<=5;i++) value.append(i<=count?'★':'☆'); return value.toString(); }
    private GradientDrawable rounded(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private TextView text(String value,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setPadding(0,dp(3),0,dp(3));return t;}
    private LinearLayout.LayoutParams bottom(int margin){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(margin));return p;}
    private int dp(float value){return Math.round(value*getResources().getDisplayMetrics().density);}
}
