package com.stayflow.customer.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.stayflow.customer.data.ApiClient;
import com.stayflow.customer.model.Models.Resp;
import com.stayflow.customer.model.Models.Room;
import com.stayflow.customer.model.Models.RoomImage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Displays room inventory for one selected hotel using card and image-slider UI. */
public class RoomsActivity extends AppCompatActivity {
    private static final String FALLBACK_ROOM_IMAGE =
            "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1200&q=80";

    private final List<Room> hotelRooms = new ArrayList<>();
    private LinearLayout roomList;
    private LinearLayout filterRow;
    private ProgressBar progress;
    private int hotelId;
    private String hotelName;
    private String checkInDate;
    private String checkOutDate;
    private String selectedType = "All";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Ui.applyHeaderStatusBar(this);
        hotelId = getIntent().getIntExtra("hotelId", 0);
        hotelName = getIntent().getStringExtra("hotelName");
        checkInDate = getIntent().getStringExtra("checkInDate");
        checkOutDate = getIntent().getStringExtra("checkOutDate");

        LinearLayout page = vertical();
        page.setBackgroundColor(0xFFF7F7F8);
        page.addView(Ui.gradientStatusBarSpacer(this));
        page.addView(header());
        page.addView(intro());

        filterRow = horizontal();
        filterRow.setPadding(dp(18), 0, dp(18), dp(14));
        HorizontalScrollView filters = new HorizontalScrollView(this);
        filters.setHorizontalScrollBarEnabled(false);
        filters.addView(filterRow);
        page.addView(filters, new LinearLayout.LayoutParams(-1, -2));

        progress = new ProgressBar(this);
        page.addView(progress, new LinearLayout.LayoutParams(-1, dp(52)));
        roomList = vertical();
        roomList.setPadding(dp(18), 0, dp(18), dp(28));
        page.addView(roomList);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(page);
        setContentView(scroll);
        Ui.applySavedTheme(scroll, this);
        loadRooms();
    }

    private View header() {
        LinearLayout header = horizontal();
        header.setPadding(dp(14), dp(14), dp(18), dp(14));
        // Match Hotels, Bookings, Reviews, Profile and the system status bar.
        GradientDrawable headerGradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF991B1B, 0xFFEF233C, 0xFFFB7185});
        header.setBackground(headerGradient);
        header.setElevation(dp(2));

        MaterialButton back = new MaterialButton(this);
        back.setText("‹");
        back.setTextSize(28);
        back.setTextColor(Color.WHITE);
        back.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        back.setOnClickListener(v -> finish());
        header.addView(back, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout copy = vertical();
        copy.setGravity(Gravity.CENTER_VERTICAL);
        copy.addView(text(hotelName == null ? "Hotel rooms" : hotelName, 21, Color.WHITE, true));
        copy.addView(text("Available room collection", 12, 0xFFFFE4E6, false));
        header.addView(copy, new LinearLayout.LayoutParams(0, dp(58), 1f));
        return header;
    }

    private View intro() {
        LinearLayout intro = vertical();
        intro.setPadding(dp(18), dp(22), dp(18), dp(16));
        intro.addView(text("ROOM COLLECTION", 12, 0xFFE11D2E, true));
        TextView title = text("Choose your room", 29, 0xFF18181B, true);
        title.setPadding(0, dp(4), 0, dp(4));
        intro.addView(title);
        String period = hasDates() ? checkInDate + "  →  " + checkOutDate
                : "Select a comfortable room for your stay.";
        intro.addView(text(period, 14, 0xFF71717A, false));
        return intro;
    }

    private void loadRooms() {
        ApiClient.create(this).rooms().enqueue(new Callback<Resp<List<Room>>>() {
            @Override public void onResponse(@NonNull Call<Resp<List<Room>>> call,
                                             @NonNull Response<Resp<List<Room>>> response) {
                removeProgress();
                hotelRooms.clear();
                if (!response.isSuccessful() || response.body() == null || response.body().data == null) {
                    showEmpty("Unable to load rooms");
                    return;
                }
                for (Room room : response.body().data) {
                    if (room.hotelId != null && room.hotelId == hotelId) hotelRooms.add(room);
                }
                createFilters();
                displayRooms();
            }
            @Override public void onFailure(@NonNull Call<Resp<List<Room>>> call,
                                            @NonNull Throwable throwable) {
                removeProgress();
                showEmpty("Network error. Check your backend connection.");
            }
        });
    }

    private void createFilters() {
        filterRow.removeAllViews();
        Set<String> types = new LinkedHashSet<>();
        types.add("All");
        for (Room room : hotelRooms) if (room.roomType != null && !room.roomType.isBlank()) types.add(room.roomType);
        for (String type : types) {
            MaterialButton chip = new MaterialButton(this);
            chip.setText(type);
            chip.setTextSize(13);
            chip.setCornerRadius(dp(18));
            styleChip(chip, type.equals(selectedType));
            chip.setOnClickListener(v -> { selectedType = type; createFilters(); displayRooms(); });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(42));
            params.setMargins(0, 0, dp(8), 0);
            filterRow.addView(chip, params);
        }
        Ui.applySavedTheme(filterRow, this);
    }

    private void styleChip(MaterialButton chip, boolean selected) {
        chip.setTextColor(selected ? Color.WHITE : 0xFF52525B);
        chip.setBackgroundTintList(ColorStateList.valueOf(selected ? 0xFFE11D2E : Color.WHITE));
        chip.setStrokeColor(ColorStateList.valueOf(selected ? 0xFFE11D2E : 0xFFE4E4E7));
        chip.setStrokeWidth(dp(1));
    }

    private void displayRooms() {
        roomList.removeAllViews();
        int shown = 0;
        for (Room room : hotelRooms) {
            if ("All".equals(selectedType) || selectedType.equals(room.roomType)) {
                roomList.addView(roomCard(room));
                shown++;
            }
        }
        if (shown == 0) showEmpty("No rooms found for this hotel.");
        Ui.applySavedTheme(roomList, this);
    }

    private View roomCard(Room room) {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(dp(22));
        card.setCardElevation(dp(3));
        card.setCardBackgroundColor(Color.WHITE);
        card.setStrokeColor(0xFFE4E4E7);
        card.setStrokeWidth(dp(1));

        LinearLayout content = vertical();
        FrameLayout gallery = new FrameLayout(this);
        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        showImage(image, FALLBACK_ROOM_IMAGE);
        gallery.addView(image, new FrameLayout.LayoutParams(-1, dp(230)));

        TextView typeBadge = badge(safe(room.roomType).toUpperCase(Locale.ROOT), 0xDD18181B, Color.WHITE);
        FrameLayout.LayoutParams typeParams = new FrameLayout.LayoutParams(-2, dp(38), Gravity.BOTTOM | Gravity.START);
        typeParams.setMargins(dp(14), 0, 0, dp(14));
        gallery.addView(typeBadge, typeParams);

        TextView availabilityBadge = badge(room.availabilityStatus ? "AVAILABLE" : "UNAVAILABLE",
                room.availabilityStatus ? 0xFF22C55E : 0xFFEF4444, Color.WHITE);
        FrameLayout.LayoutParams availabilityParams = new FrameLayout.LayoutParams(-2, dp(36), Gravity.TOP | Gravity.END);
        availabilityParams.setMargins(0, dp(14), dp(14), 0);
        gallery.addView(availabilityBadge, availabilityParams);

        TextView counter = badge("1 / 1", Color.WHITE, 0xFF18181B);
        FrameLayout.LayoutParams counterParams = new FrameLayout.LayoutParams(-2, dp(36), Gravity.BOTTOM | Gravity.END);
        counterParams.setMargins(0, 0, dp(14), dp(14));
        gallery.addView(counter, counterParams);

        ImageButton previous = sliderButton(true);
        ImageButton next = sliderButton(false);
        FrameLayout.LayoutParams previousParams = new FrameLayout.LayoutParams(dp(48), dp(48), Gravity.CENTER_VERTICAL | Gravity.START);
        previousParams.setMargins(dp(10), 0, 0, 0);
        FrameLayout.LayoutParams nextParams = new FrameLayout.LayoutParams(dp(48), dp(48), Gravity.CENTER_VERTICAL | Gravity.END);
        nextParams.setMargins(0, 0, dp(10), 0);
        gallery.addView(previous, previousParams);
        gallery.addView(next, nextParams);
        content.addView(gallery);

        LinearLayout details = vertical();
        details.setPadding(dp(18), dp(17), dp(18), dp(18));
        details.addView(text("Room #" + room.roomNumber, 23, 0xFF18181B, true));
        TextView hotel = text("▣  " + hotelName, 14, 0xFF71717A, false);
        hotel.setPadding(0, dp(4), 0, dp(16));
        details.addView(hotel);

        LinearLayout priceRow = horizontal();
        LinearLayout priceCopy = vertical();
        priceCopy.addView(text("Price per night", 12, 0xFF71717A, false));
        priceCopy.addView(text("₹" + money(room.pricePerNight), 25, 0xFFE11D2E, true));
        priceRow.addView(priceCopy, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView guests = text("Guests: " + room.capacity, 14, 0xFF71717A, false);
        guests.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        priceRow.addView(guests, new LinearLayout.LayoutParams(-2, dp(56)));
        details.addView(priceRow);

        MaterialButton book = Ui.primaryButton(this, room.availabilityStatus ? "Book this room" : "Unavailable");
        book.setEnabled(room.availabilityStatus);
        book.setOnClickListener(v -> openBooking(room));
        LinearLayout.LayoutParams bookParams = new LinearLayout.LayoutParams(-1, dp(50));
        bookParams.setMargins(0, dp(12), 0, 0);
        details.addView(book, bookParams);
        content.addView(details);
        card.addView(content);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.setMargins(0, 0, 0, dp(16));
        card.setLayoutParams(cardParams);
        loadImages(room, image, counter, previous, next);
        checkAvailability(room, availabilityBadge, book);
        return card;
    }

    private void loadImages(Room room, ImageView image, TextView counter,
                            ImageButton previous, ImageButton next) {
        if (room.roomId == null) return;
        ApiClient.create(this).roomImages(room.roomId).enqueue(new Callback<Resp<List<RoomImage>>>() {
            @Override public void onResponse(@NonNull Call<Resp<List<RoomImage>>> call,
                                             @NonNull Response<Resp<List<RoomImage>>> response) {
                List<String> urls = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    for (RoomImage item : response.body().data) if (item.imageUrl != null && !item.imageUrl.isBlank()) urls.add(item.imageUrl);
                }
                if (urls.isEmpty()) urls.add(FALLBACK_ROOM_IMAGE);
                int[] index = {0};
                Runnable render = () -> {
                    showImage(image, urls.get(index[0]));
                    counter.setText((index[0] + 1) + " / " + urls.size());
                    previous.setVisibility(urls.size() > 1 ? View.VISIBLE : View.GONE);
                    next.setVisibility(urls.size() > 1 ? View.VISIBLE : View.GONE);
                };
                previous.setOnClickListener(v -> { index[0] = (index[0] - 1 + urls.size()) % urls.size(); render.run(); });
                next.setOnClickListener(v -> { index[0] = (index[0] + 1) % urls.size(); render.run(); });
                render.run();
            }
            @Override public void onFailure(@NonNull Call<Resp<List<RoomImage>>> call,
                                            @NonNull Throwable throwable) {
                showImage(image, FALLBACK_ROOM_IMAGE);
                previous.setVisibility(View.GONE);
                next.setVisibility(View.GONE);
            }
        });
    }

    private void checkAvailability(Room room, TextView badge, MaterialButton button) {
        if (!hasDates() || room.roomId == null || !room.availabilityStatus) return;
        ApiClient.create(this).roomAvailability(room.roomId, checkInDate, checkOutDate)
                .enqueue(new Callback<Resp<Boolean>>() {
                    @Override public void onResponse(@NonNull Call<Resp<Boolean>> call,
                                                     @NonNull Response<Resp<Boolean>> response) {
                        boolean available = response.isSuccessful() && response.body() != null
                                && Boolean.TRUE.equals(response.body().data);
                        badge.setText(available ? "AVAILABLE" : "UNAVAILABLE");
                        badge.setBackground(rounded(available ? 0xFF22C55E : 0xFFEF4444, 18));
                        button.setEnabled(available);
                        button.setText(available ? "Book this room" : "Unavailable for dates");
                    }
                    @Override public void onFailure(@NonNull Call<Resp<Boolean>> call,
                                                    @NonNull Throwable throwable) {
                        badge.setText("CHECK FAILED");
                        badge.setBackground(rounded(0xFFF59E0B, 18));
                        button.setEnabled(false);
                    }
                });
    }

    private void openBooking(Room room) {
        Intent intent = new Intent(this, BookRoomActivity.class);
        intent.putExtra("roomId", room.roomId);
        intent.putExtra("price", room.pricePerNight);
        intent.putExtra("roomNumber", room.roomNumber);
        intent.putExtra("roomType", room.roomType);
        intent.putExtra("hotelName", hotelName);
        intent.putExtra("checkInDate", checkInDate);
        intent.putExtra("checkOutDate", checkOutDate);
        startActivity(intent);
    }

    private ImageButton sliderButton(boolean previous) {
        ImageButton button = new ImageButton(this);
        button.setContentDescription(previous ? "Previous image" : "Next image");
        button.setImageResource(previous ? android.R.drawable.ic_media_previous : android.R.drawable.ic_media_next);
        button.setColorFilter(Color.WHITE);
        button.setBackground(rounded(0xBB18181B, 24));
        return button;
    }

    private void showImage(ImageView image, String url) {
        Glide.with(this).load(url == null || url.isBlank() ? FALLBACK_ROOM_IMAGE : url)
                .centerCrop().error(Glide.with(this).load(FALLBACK_ROOM_IMAGE).centerCrop()).into(image);
    }

    private void showEmpty(String value) {
        roomList.removeAllViews();
        TextView empty = text(value, 17, 0xFF71717A, true);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(dp(16), dp(60), dp(16), dp(60));
        roomList.addView(empty);
    }

    private TextView badge(String value, int background, int foreground) {
        TextView view = text(value, 11, foreground, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(12), 0, dp(12), 0);
        view.setBackground(rounded(background, 18));
        return view;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private LinearLayout vertical() { LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL); return layout; }
    private LinearLayout horizontal() { LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.HORIZONTAL); layout.setGravity(Gravity.CENTER_VERTICAL); return layout; }
    private TextView text(String value, int size, int color, boolean bold) { TextView view = new TextView(this); view.setText(value == null ? "" : value); view.setTextSize(size); view.setTextColor(color); if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return view; }
    private boolean hasDates() { return checkInDate != null && !checkInDate.isBlank() && checkOutDate != null && !checkOutDate.isBlank(); }
    private void removeProgress() { if (progress.getParent() instanceof ViewGroup) ((ViewGroup) progress.getParent()).removeView(progress); }
    private String money(double value) { return String.format(new Locale("en", "IN"), "%,.0f", value); }
    private String safe(String value) { return value == null ? "Room" : value; }
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
