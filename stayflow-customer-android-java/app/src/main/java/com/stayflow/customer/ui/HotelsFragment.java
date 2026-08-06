package com.stayflow.customer.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.stayflow.customer.R;
import com.stayflow.customer.data.ApiClient;
import com.stayflow.customer.data.SessionManager;
import com.stayflow.customer.model.Models.Hotel;
import com.stayflow.customer.model.Models.HotelImage;
import com.stayflow.customer.model.Models.Resp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Customer hotel discovery UI with date, guest, search and location filters. */
public class HotelsFragment extends Fragment {
    private static final String DEFAULT_HOTEL_IMAGE =
            "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80";
    private final List<Hotel> approvedHotels = new ArrayList<>();
    private LinearLayout hotelList;
    private ProgressBar progress;
    private EditText searchInput;
    private Spinner citySpinner;
    private Spinner stateSpinner;
    private TextView resultCount;
    private TextView checkInText;
    private TextView checkOutText;
    private TextView guestText;
    private MaterialButton allButton;
    private MaterialButton topRatedButton;
    private Calendar checkIn;
    private Calendar checkOut;
    private int rooms = 1;
    private int guests = 2;
    private boolean topRatedOnly;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle state) {
        LinearLayout page = vertical();
        page.setBackgroundColor(Color.rgb(247, 247, 248));
        page.addView(createHero());
        page.addView(createBenefits());
        page.addView(createHotelSection());

        ScrollView scroll = new ScrollView(requireContext());
        scroll.setFillViewport(true);
        scroll.addView(page, new ScrollView.LayoutParams(-1, -2));
        Ui.applySavedTheme(scroll, requireContext());
        loadHotels();
        return scroll;
    }

    private View createHero() {
        LinearLayout hero = vertical();
        hero.setPadding(dp(20), dp(18), dp(20), dp(24));
        hero.setBackground(gradient(new int[]{0xFF991B1B, 0xFFEF233C, 0xFFFB7185}, 0, 0, 0, 28));

        String email = new SessionManager(requireContext()).email();
        String name = email == null || email.isBlank() ? "Guest" : email.split("@")[0];
        hero.addView(text("Welcome, " + name, 15, Color.WHITE, false));
        TextView location = text("●  Hotels near you⌄", 19, Color.WHITE, true);
        location.setPadding(0, dp(3), 0, dp(20));
        hero.addView(location);
        hero.addView(text("Find your perfect stay", 31, Color.WHITE, true));
        TextView subtitle = text("Verified hotels, comfortable rooms and easy booking.", 15, 0xFFFFE4E6, false);
        subtitle.setPadding(0, dp(5), 0, dp(17));
        hero.addView(subtitle);

        searchInput = new EditText(requireContext());
        searchInput.setHint("Search hotel, city or location");
        searchInput.setSingleLine(true);
        searchInput.setTextSize(16);
        searchInput.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_search, 0, 0, 0);
        searchInput.setCompoundDrawablePadding(dp(12));
        searchInput.setPadding(dp(18), 0, dp(18), 0);
        searchInput.setBackground(rounded(Color.WHITE, 18));
        hero.addView(searchInput, params(-1, dp(58), 0, 0, 0, 14));
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        LinearLayout stay = horizontal();
        stay.setPadding(dp(12), dp(8), dp(12), dp(8));
        stay.setBackground(rounded(Color.WHITE, 18));
        checkInText = selectorText("CHECK-IN\nChoose date");
        checkOutText = selectorText("CHECK-OUT\nChoose date");
        guestText = selectorText("GUESTS\n1 room · 2 guests");
        checkInText.setOnClickListener(v -> pickDate(true));
        checkOutText.setOnClickListener(v -> pickDate(false));
        guestText.setOnClickListener(v -> pickGuests());
        stay.addView(checkInText, weight());
        stay.addView(divider(), new LinearLayout.LayoutParams(dp(1), dp(45)));
        stay.addView(checkOutText, weight());
        stay.addView(divider(), new LinearLayout.LayoutParams(dp(1), dp(45)));
        stay.addView(guestText, weight());
        hero.addView(stay, params(-1, dp(70), 0, 0, 0, 0));
        return hero;
    }

    private View createBenefits() {
        LinearLayout row = horizontal();
        row.setPadding(dp(18), dp(18), dp(18), dp(8));

        MaterialCardView save = benefitCard("✦", "Save more", "Special prices on verified stays", 0xFFF7A3A8, 0xFF18181B);
        MaterialCardView safe = benefitCard("◇", "Book safely", "Only approved properties", 0xFF159AB8, Color.WHITE);
        row.addView(save, cardWeight(0, 8));
        row.addView(safe, cardWeight(8, 0));
        return row;
    }

    private View createHotelSection() {
        LinearLayout section = vertical();
        section.setPadding(dp(18), dp(20), dp(18), dp(28));
        section.addView(text("Recommended hotels", 27, 0xFF18181B, true));
        resultCount = text("Loading verified stays...", 14, 0xFF71717A, false);
        resultCount.setPadding(0, dp(3), 0, dp(12));
        section.addView(resultCount);

        LinearLayout quickFilters = horizontal();
        allButton = filterButton("All");
        topRatedButton = filterButton("Top rated");
        allButton.setOnClickListener(v -> { topRatedOnly = false; styleFilterButtons(); applyFilters(); });
        topRatedButton.setOnClickListener(v -> { topRatedOnly = true; styleFilterButtons(); applyFilters(); });
        quickFilters.addView(allButton, cardWeight(0, 6));
        quickFilters.addView(topRatedButton, cardWeight(6, 0));
        section.addView(quickFilters, params(-1, dp(46), 0, 0, 0, 10));
        styleFilterButtons();

        LinearLayout locations = horizontal();
        citySpinner = new Spinner(requireContext());
        stateSpinner = new Spinner(requireContext());
        locations.addView(citySpinner, cardWeight(0, 6));
        locations.addView(stateSpinner, cardWeight(6, 0));
        section.addView(locations, params(-1, dp(52), 0, 0, 0, 14));

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int position, long id) { applyFilters(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };
        citySpinner.setOnItemSelectedListener(listener);
        stateSpinner.setOnItemSelectedListener(listener);

        progress = new ProgressBar(requireContext());
        section.addView(progress, new LinearLayout.LayoutParams(-1, dp(48)));
        hotelList = vertical();
        section.addView(hotelList);
        return section;
    }

    private void loadHotels() {
        ApiClient.create(requireContext()).hotels().enqueue(new Callback<Resp<List<Hotel>>>() {
            @Override public void onResponse(@NonNull Call<Resp<List<Hotel>>> call,
                                             @NonNull Response<Resp<List<Hotel>>> response) {
                if (!isAdded()) return;
                removeProgress();
                approvedHotels.clear();
                if (!response.isSuccessful() || response.body() == null || response.body().data == null) {
                    showEmpty("Unable to load hotels");
                    return;
                }
                for (Hotel hotel : response.body().data) {
                    if ("APPROVED".equalsIgnoreCase(hotel.status)) approvedHotels.add(hotel);
                }
                configureLocationFilters();
                applyFilters();
            }

            @Override public void onFailure(@NonNull Call<Resp<List<Hotel>>> call,
                                            @NonNull Throwable throwable) {
                if (!isAdded()) return;
                removeProgress();
                showEmpty("Network error. Check your backend connection.");
            }
        });
    }

    private void configureLocationFilters() {
        Set<String> cities = new LinkedHashSet<>();
        Set<String> states = new LinkedHashSet<>();
        cities.add("All cities");
        states.add("All states");
        for (Hotel hotel : approvedHotels) {
            if (hotel.city != null && !hotel.city.isBlank()) cities.add(hotel.city.trim());
            if (hotel.state != null && !hotel.state.isBlank()) states.add(hotel.state.trim());
        }
        setSpinner(citySpinner, new ArrayList<>(cities));
        setSpinner(stateSpinner, new ArrayList<>(states));
    }

    private void applyFilters() {
        if (hotelList == null || citySpinner == null || stateSpinner == null) return;
        hotelList.removeAllViews();
        String query = searchInput == null ? "" : searchInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        String city = selected(citySpinner, "All cities");
        String state = selected(stateSpinner, "All states");
        int shown = 0;

        for (Hotel hotel : approvedHotels) {
            String searchable = safe(hotel.hotelName) + " " + safe(hotel.city) + " " + safe(hotel.state) + " " + safe(hotel.address);
            boolean matches = searchable.toLowerCase(Locale.ROOT).contains(query)
                    && ("All cities".equals(city) || city.equalsIgnoreCase(safe(hotel.city).trim()))
                    && ("All states".equals(state) || state.equalsIgnoreCase(safe(hotel.state).trim()))
                    && (!topRatedOnly || hotel.rating >= 4.0);
            if (matches) {
                hotelList.addView(hotelCard(hotel));
                shown++;
            }
        }
        resultCount.setText(shown + " verified stay" + (shown == 1 ? "" : "s") + " found");
        if (shown == 0) showEmpty("No hotels found. Try another search or filter.");
        Ui.applySavedTheme(hotelList, requireContext());
    }

    private View hotelCard(Hotel hotel) {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(dp(22));
        card.setCardElevation(dp(3));
        card.setStrokeColor(0xFFEF233C);
        card.setStrokeWidth(dp(2));

        LinearLayout content = vertical();
        FrameLayout imageArea = new FrameLayout(requireContext());
        ImageView image = new ImageView(requireContext());
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        showHotelImage(image, DEFAULT_HOTEL_IMAGE);
        imageArea.addView(image, new FrameLayout.LayoutParams(-1, dp(220)));

        TextView verified = badge("✓  STAYFLOW VERIFIED", 0xFF16A34A, Color.WHITE);
        FrameLayout.LayoutParams verifiedParams = new FrameLayout.LayoutParams(-2, dp(36), Gravity.TOP | Gravity.START);
        verifiedParams.setMargins(dp(14), dp(14), 0, 0);
        imageArea.addView(verified, verifiedParams);

        if (hotel.rating > 0) {
            TextView rating = badge("★ " + hotel.rating, 0xFF15803D, Color.WHITE);
            FrameLayout.LayoutParams ratingParams = new FrameLayout.LayoutParams(-2, dp(36), Gravity.BOTTOM | Gravity.END);
            ratingParams.setMargins(0, 0, dp(14), dp(14));
            imageArea.addView(rating, ratingParams);
        }
        content.addView(imageArea);

        LinearLayout copy = vertical();
        copy.setPadding(dp(18), dp(16), dp(18), dp(18));
        copy.addView(text(hotel.hotelName, 23, 0xFF18181B, true));
        TextView location = text("●  " + join(hotel.city, hotel.state), 14, 0xFF71717A, false);
        location.setPadding(0, dp(5), 0, 0);
        copy.addView(location);
        if (hotel.description != null && !hotel.description.isBlank()) {
            TextView description = text(hotel.description, 14, 0xFF52525B, false);
            description.setPadding(0, dp(8), 0, dp(12));
            copy.addView(description);
        }

        MaterialButton roomsButton = Ui.primaryButton(requireContext(), "View available rooms");
        roomsButton.setOnClickListener(v -> openRooms(hotel));
        copy.addView(roomsButton, new LinearLayout.LayoutParams(-1, dp(50)));

        MaterialButton reviewsButton = new MaterialButton(requireContext());
        reviewsButton.setText("★  Reviews & ratings");
        reviewsButton.setTextSize(14);
        reviewsButton.setTextColor(0xFFE11D2E);
        reviewsButton.setCornerRadius(dp(14));
        reviewsButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
        reviewsButton.setStrokeColor(android.content.res.ColorStateList.valueOf(0xFFE11D2E));
        reviewsButton.setStrokeWidth(dp(1));
        reviewsButton.setOnClickListener(v -> openReviews(hotel));
        LinearLayout.LayoutParams reviewsParams = new LinearLayout.LayoutParams(-1, dp(50));
        reviewsParams.setMargins(0, dp(10), 0, 0);
        copy.addView(reviewsButton, reviewsParams);
        content.addView(copy);
        card.addView(content);
        card.setLayoutParams(params(-1, -2, 0, 0, 0, 16));
        loadHotelImage(hotel, image);
        return card;
    }

    private void loadHotelImage(Hotel hotel, ImageView image) {
        if (hotel.hotelId == null) return;
        ApiClient.create(requireContext()).hotelImages(hotel.hotelId)
                .enqueue(new Callback<List<HotelImage>>() {
                    @Override public void onResponse(@NonNull Call<List<HotelImage>> call,
                                                     @NonNull Response<List<HotelImage>> response) {
                        if (!isAdded() || !response.isSuccessful() || response.body() == null
                                || response.body().isEmpty()) {
                            showHotelImage(image, DEFAULT_HOTEL_IMAGE);
                            return;
                        }
                        String url = response.body().get(0).imageUrl;
                        showHotelImage(image, url);
                    }
                    @Override public void onFailure(@NonNull Call<List<HotelImage>> call,
                                                    @NonNull Throwable throwable) {
                        if (isAdded()) showHotelImage(image, DEFAULT_HOTEL_IMAGE);
                    }
                });
    }

    private void showHotelImage(ImageView image, String url) {
        if (!isAdded()) return;
        String source = url == null || url.isBlank() ? DEFAULT_HOTEL_IMAGE : url;
        Glide.with(this)
                .load(source)
                .centerCrop()
                .error(Glide.with(this).load(DEFAULT_HOTEL_IMAGE).centerCrop())
                .into(image);
    }

    private void openRooms(Hotel hotel) {
        if (checkIn == null || checkOut == null) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Select stay dates")
                    .setMessage("Choose check-in and check-out dates to see accurate room availability.")
                    .setPositiveButton("Choose dates", (dialog, which) -> pickDate(true))
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }
        Intent intent = new Intent(requireContext(), RoomsActivity.class);
        intent.putExtra("hotelId", hotel.hotelId);
        intent.putExtra("hotelName", hotel.hotelName);
        intent.putExtra("checkInDate", apiDate(checkIn));
        intent.putExtra("checkOutDate", apiDate(checkOut));
        intent.putExtra("rooms", rooms);
        intent.putExtra("guests", guests);
        startActivity(intent);
    }

    private void openReviews(Hotel hotel) {
        if (hotel.hotelId == null) return;
        Intent intent = new Intent(requireContext(), HotelReviewsActivity.class);
        intent.putExtra("hotelId", hotel.hotelId);
        intent.putExtra("hotelName", hotel.hotelName);
        startActivity(intent);
    }

    private void pickDate(boolean selectingCheckIn) {
        Calendar initial = selectingCheckIn
                ? (checkIn == null ? Calendar.getInstance() : checkIn)
                : (checkOut == null ? (checkIn == null ? Calendar.getInstance() : checkIn) : checkOut);
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, day, 0, 0, 0);
            chosen.set(Calendar.MILLISECOND, 0);
            if (selectingCheckIn) {
                checkIn = chosen;
                if (checkOut != null && !checkOut.after(checkIn)) checkOut = null;
                updateDateLabels();
                pickDate(false);
            } else {
                if (checkIn == null) {
                    Ui.toast(requireContext(), "Select check-in first");
                    pickDate(true);
                    return;
                }
                if (!chosen.after(checkIn)) {
                    Ui.toast(requireContext(), "Check-out must be after check-in");
                    return;
                }
                checkOut = chosen;
                updateDateLabels();
            }
        }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(selectingCheckIn
                ? System.currentTimeMillis() - 1000
                : (checkIn == null ? System.currentTimeMillis() : checkIn.getTimeInMillis() + 86_400_000L));
        dialog.show();
    }

    private void pickGuests() {
        LinearLayout fields = horizontal();
        fields.setPadding(dp(18), 0, dp(18), 0);
        NumberPicker roomPicker = picker(1, 5, rooms);
        NumberPicker guestPicker = picker(1, 10, guests);
        LinearLayout roomBox = labelledPicker("ROOMS", roomPicker);
        LinearLayout guestBox = labelledPicker("GUESTS", guestPicker);
        fields.addView(roomBox, weight());
        fields.addView(guestBox, weight());
        new AlertDialog.Builder(requireContext())
                .setTitle("Guests and rooms")
                .setView(fields)
                .setPositiveButton("Apply", (dialog, which) -> {
                    rooms = roomPicker.getValue();
                    guests = guestPicker.getValue();
                    guestText.setText("GUESTS\n" + rooms + " room" + (rooms == 1 ? "" : "s") + " · " + guests + " guests");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateDateLabels() {
        checkInText.setText("CHECK-IN\n" + (checkIn == null ? "Choose date" : displayDate(checkIn)));
        checkOutText.setText("CHECK-OUT\n" + (checkOut == null ? "Choose date" : displayDate(checkOut)));
    }

    private MaterialCardView benefitCard(String icon, String title, String subtitle, int background, int foreground) {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setRadius(dp(20));
        card.setCardBackgroundColor(background);
        LinearLayout content = vertical();
        content.setPadding(dp(16), dp(16), dp(16), dp(16));
        content.addView(text(icon, 27, foreground, true));
        TextView titleView = text(title, 20, foreground, true);
        titleView.setPadding(0, dp(14), 0, dp(4));
        content.addView(titleView);
        content.addView(text(subtitle, 13, foreground, false));
        card.addView(content);
        return card;
    }

    private MaterialButton filterButton(String label) {
        MaterialButton button = new MaterialButton(requireContext());
        button.setText(label);
        button.setTextSize(14);
        button.setCornerRadius(dp(12));
        return button;
    }

    private void styleFilterButtons() {
        if (allButton == null) return;
        allButton.setBackgroundColor(topRatedOnly ? Color.WHITE : 0xFFEF233C);
        allButton.setTextColor(topRatedOnly ? 0xFF52525B : Color.WHITE);
        topRatedButton.setBackgroundColor(topRatedOnly ? 0xFFEF233C : Color.WHITE);
        topRatedButton.setTextColor(topRatedOnly ? Color.WHITE : 0xFF52525B);
        Ui.applySavedTheme(allButton, requireContext());
        Ui.applySavedTheme(topRatedButton, requireContext());
    }

    private TextView selectorText(String value) {
        TextView view = text(value, 12, 0xFF18181B, true);
        view.setGravity(Gravity.CENTER);
        return view;
    }

    private TextView badge(String value, int background, int foreground) {
        TextView view = text(value, 11, foreground, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(12), 0, dp(12), 0);
        view.setBackground(rounded(background, 18));
        return view;
    }

    private View divider() {
        View divider = new View(requireContext());
        divider.setBackgroundColor(0xFFE4E4E7);
        return divider;
    }

    private NumberPicker picker(int min, int max, int value) {
        NumberPicker picker = new NumberPicker(requireContext());
        picker.setMinValue(min);
        picker.setMaxValue(max);
        picker.setValue(value);
        return picker;
    }

    private LinearLayout labelledPicker(String label, NumberPicker picker) {
        LinearLayout box = vertical();
        box.setGravity(Gravity.CENTER);
        box.addView(text(label, 12, 0xFF71717A, true));
        box.addView(picker);
        return box;
    }

    private void setSpinner(Spinner spinner, List<String> values) {
        boolean dark = new com.stayflow.customer.data.SessionManager(requireContext()).darkMode();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, values) {
            @NonNull @Override public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(dark ? Color.WHITE : 0xFF18181B);
                view.setTextSize(14);
                view.setPadding(dp(12), 0, dp(8), 0);
                view.setBackgroundColor(dark ? 0xFF18181B : Color.WHITE);
                return view;
            }
            @NonNull @Override public View getDropDownView(int position, @Nullable View convertView,
                                                           @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(dark ? Color.WHITE : 0xFF18181B);
                view.setTextSize(14);
                view.setPadding(dp(16), dp(14), dp(16), dp(14));
                view.setBackgroundColor(dark ? 0xFF18181B : Color.WHITE);
                return view;
            }
        };
        spinner.setAdapter(adapter);
        spinner.setBackground(rounded(dark ? 0xFF18181B : Color.WHITE, 12));
        spinner.setPopupBackgroundDrawable(new android.graphics.drawable.ColorDrawable(
                dark ? 0xFF18181B : Color.WHITE));
    }

    private String selected(Spinner spinner, String fallback) {
        return spinner.getSelectedItem() == null ? fallback : spinner.getSelectedItem().toString();
    }

    private void removeProgress() {
        if (progress != null && progress.getParent() instanceof ViewGroup) ((ViewGroup) progress.getParent()).removeView(progress);
    }

    private void showEmpty(String value) {
        hotelList.removeAllViews();
        TextView message = text(value, 16, 0xFF71717A, true);
        message.setGravity(Gravity.CENTER);
        message.setPadding(dp(14), dp(42), dp(14), dp(42));
        hotelList.addView(message);
    }

    private LinearLayout vertical() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout horizontal() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        return layout;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(requireContext());
        view.setText(value == null ? "" : value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private GradientDrawable gradient(int[] colors, float tl, float tr, float br, float bl) {
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        drawable.setCornerRadii(new float[]{dp(tl), dp(tl), dp(tr), dp(tr), dp(br), dp(br), dp(bl), dp(bl)});
        return drawable;
    }

    private LinearLayout.LayoutParams weight() {
        return new LinearLayout.LayoutParams(0, -1, 1f);
    }

    private LinearLayout.LayoutParams cardWeight(int left, int right) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1f);
        params.setMargins(dp(left), 0, dp(right), 0);
        return params;
    }

    private LinearLayout.LayoutParams params(int width, int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private String safe(String value) { return value == null ? "" : value; }

    private String join(String first, String second) {
        if (first == null || first.isBlank()) return safe(second);
        if (second == null || second.isBlank()) return first;
        return first + ", " + second;
    }

    private String displayDate(Calendar value) {
        return new SimpleDateFormat("dd MMM", Locale.ENGLISH).format(value.getTime());
    }

    private String apiDate(Calendar value) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(value.getTime());
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
