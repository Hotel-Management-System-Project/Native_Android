package com.stayflow.customer.ui;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.stayflow.customer.data.ApiClient;
import com.stayflow.customer.model.Models.Booking;
import com.stayflow.customer.model.Models.BookingRoom;
import com.stayflow.customer.model.Models.Hotel;
import com.stayflow.customer.model.Models.Payment;
import com.stayflow.customer.model.Models.Resp;
import com.stayflow.customer.model.Models.Room;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Customer booking history with hotel, room, payment, status, and cancellation details. */
public class BookingsFragment extends Fragment {
    private LinearLayout page;
    private LinearLayout cards;
    private LinearLayout filters;
    private final List<Booking> bookings = new ArrayList<>();
    private final Map<Integer, Room> rooms = new HashMap<>();
    private final Map<Integer, Hotel> hotels = new HashMap<>();
    private String activeFilter = "ALL";
    private int loadedSources;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent,
                             @Nullable Bundle state) {
        ScrollView scroll = new ScrollView(requireContext());
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(new com.stayflow.customer.data.SessionManager(requireContext()).darkMode()
                ? 0xFF09090B : 0xFFF7F7F8);
        page = vertical();
        page.addView(hero());

        LinearLayout body = vertical();
        body.setPadding(dp(18), dp(20), dp(18), dp(32));
        filters = horizontal();
        body.addView(filters, bottom(18));
        cards = vertical();
        body.addView(cards);
        page.addView(body);
        scroll.addView(page);
        return scroll;
    }

    @Override public void onResume() {
        super.onResume();
        if (cards != null) load();
    }

    private View hero() {
        LinearLayout hero = vertical();
        hero.setPadding(dp(20), dp(22), dp(20), dp(24));
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF991B1B, 0xFFEF233C, 0xFFFB7185});
        gradient.setCornerRadii(new float[]{0,0,0,0,dp(28),dp(28),dp(28),dp(28)});
        hero.setBackground(gradient);
        hero.addView(label("MY RESERVATIONS", 12, 0xFFFFE4E6, true));
        TextView title = label("Your bookings", 30, Color.WHITE, true);
        title.setPadding(0, dp(5), 0, dp(4));
        hero.addView(title);
        hero.addView(label("Review your stays, payment details, and booking status.",
                14, 0xFFFFE4E6, false));
        return hero;
    }

    private void load() {
        if (!isAdded()) return;
        loadedSources = 0;
        bookings.clear(); rooms.clear(); hotels.clear();
        cards.removeAllViews();
        ProgressBar progress = new ProgressBar(requireContext());
        cards.addView(progress, new LinearLayout.LayoutParams(-1, dp(54)));

        ApiClient.create(requireContext()).myBookings().enqueue(listCallback(data -> {
            bookings.addAll(data);
        }));
        ApiClient.create(requireContext()).rooms().enqueue(listCallback(data -> {
            for (Room room : data) if (room.roomId != null) rooms.put(room.roomId, room);
        }));
        ApiClient.create(requireContext()).hotels().enqueue(listCallback(data -> {
            for (Hotel hotel : data) if (hotel.hotelId != null) hotels.put(hotel.hotelId, hotel);
        }));
    }

    private <T> Callback<Resp<List<T>>> listCallback(DataConsumer<T> consumer) {
        return new Callback<>() {
            @Override public void onResponse(@NonNull Call<Resp<List<T>>> call,
                                             @NonNull Response<Resp<List<T>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    consumer.accept(response.body().data);
                }
                sourceLoaded();
            }
            @Override public void onFailure(@NonNull Call<Resp<List<T>>> call,
                                            @NonNull Throwable error) {
                if (isAdded()) sourceLoaded();
            }
        };
    }

    private void sourceLoaded() {
        loadedSources++;
        if (loadedSources == 3) render();
    }

    private void render() {
        renderFilters();
        cards.removeAllViews();
        int shown = 0;
        for (Booking booking : bookings) {
            String status = status(booking);
            if (!"ALL".equals(activeFilter) && !activeFilter.equals(status)) continue;
            cards.addView(bookingCard(booking));
            shown++;
        }
        if (shown == 0) emptyState();
        Ui.applySavedTheme(page, requireContext());
    }

    private void renderFilters() {
        filters.removeAllViews();
        addFilter("ALL", "All", bookings.size());
        addFilter("BOOKED", "Upcoming", count("BOOKED"));
        addFilter("CANCELLED", "Cancelled", count("CANCELLED"));
    }

    private void addFilter(String value, String title, int count) {
        MaterialButton button = new MaterialButton(requireContext());
        boolean selected = value.equals(activeFilter);
        button.setText(title + "  " + count);
        button.setTextSize(12);
        button.setTextColor(selected ? Color.WHITE : 0xFF52525B);
        button.setCornerRadius(dp(18));
        button.setBackgroundTintList(ColorStateList.valueOf(selected ? 0xFFE11D2E : Color.WHITE));
        button.setStrokeColor(ColorStateList.valueOf(selected ? 0xFFE11D2E : 0xFFE4E4E7));
        button.setStrokeWidth(dp(1));
        button.setOnClickListener(v -> { activeFilter = value; render(); });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(44), 1f);
        params.setMargins(0, 0, value.equals("CANCELLED") ? 0 : dp(8), 0);
        filters.addView(button, params);
    }

    private View bookingCard(Booking booking) {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setRadius(dp(20));
        card.setCardElevation(dp(2));
        card.setCardBackgroundColor(Color.WHITE);
        card.setStrokeColor(0xFFE4E4E7);
        card.setStrokeWidth(dp(1));

        LinearLayout box = vertical();
        box.setPadding(dp(17), dp(17), dp(17), dp(17));
        LinearLayout top = horizontal();
        TextView bookingId = label("BOOKING ID\n#" + booking.bookingId, 15, 0xFF18181B, true);
        top.addView(bookingId, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView status = statusBadge(status(booking));
        top.addView(status);
        box.addView(top, bottom(14));

        LinearLayout hotelPanel = vertical();
        hotelPanel.setPadding(dp(14), dp(12), dp(14), dp(12));
        hotelPanel.setBackground(rounded(0xFFF4F4F5, 14));
        TextView hotelName = label("BOOKED HOTEL\nLoading hotel details...", 14, 0xFF18181B, true);
        hotelPanel.addView(hotelName);
        TextView roomBadge = label("", 12, Color.WHITE, true);
        roomBadge.setVisibility(View.GONE);
        roomBadge.setPadding(dp(10), dp(6), dp(10), dp(6));
        roomBadge.setBackground(rounded(0xFFE11D2E, 12));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(-2, -2);
        badgeParams.setMargins(0, dp(9), 0, 0);
        hotelPanel.addView(roomBadge, badgeParams);
        box.addView(hotelPanel, bottom(14));

        box.addView(datePanel(booking), bottom(14));

        LinearLayout amountRow = horizontal();
        LinearLayout amount = vertical();
        amount.addView(label("TOTAL BOOKING VALUE", 10, 0xFF71717A, true));
        amount.addView(label("\u20B9" + money(booking.totalAmount), 23, 0xFF18181B, true));
        amountRow.addView(amount, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView payment = label("PAYMENT\nLoading...", 11, 0xFFA16207, true);
        payment.setGravity(Gravity.CENTER);
        payment.setPadding(dp(11), dp(7), dp(11), dp(7));
        payment.setBackground(rounded(0xFFFEF3C7, 12));
        amountRow.addView(payment);
        box.addView(amountRow);

        if (!"CANCELLED".equals(status(booking)) && !"COMPLETED".equals(status(booking))) {
            MaterialButton cancel = new MaterialButton(requireContext());
            cancel.setText("Cancel booking");
            cancel.setTextColor(0xFFDC2626);
            cancel.setCornerRadius(dp(14));
            cancel.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            cancel.setStrokeColor(ColorStateList.valueOf(0xFFFCA5A5));
            cancel.setStrokeWidth(dp(1));
            cancel.setOnClickListener(v -> confirmCancel(booking.bookingId));
            LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(-1, dp(50));
            cancelParams.setMargins(0, dp(15), 0, 0);
            box.addView(cancel, cancelParams);
        }

        card.addView(box);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(params);
        loadDetails(booking, hotelName, roomBadge, payment);
        return card;
    }

    private View datePanel(Booking booking) {
        LinearLayout panel = horizontal();
        panel.setPadding(dp(14), dp(12), dp(14), dp(12));
        panel.setBackground(rounded(0xFFFFF1F2, 14));
        LinearLayout in = vertical();
        in.addView(label("CHECK-IN", 10, 0xFFE11D2E, true));
        in.addView(label(formatDate(booking.checkInDate), 14, 0xFF18181B, true));
        in.addView(label("After 12:00 PM", 10, 0xFF71717A, false));
        panel.addView(in, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView arrow = label("\u2192", 24, 0xFFE11D2E, true);
        arrow.setGravity(Gravity.CENTER);
        panel.addView(arrow, new LinearLayout.LayoutParams(dp(42), -1));
        LinearLayout out = vertical();
        out.setGravity(Gravity.END);
        out.addView(label("CHECK-OUT", 10, 0xFFE11D2E, true));
        out.addView(label(formatDate(booking.checkOutDate), 14, 0xFF18181B, true));
        out.addView(label("Before 11:00 AM", 10, 0xFF71717A, false));
        panel.addView(out, new LinearLayout.LayoutParams(0, -2, 1f));
        return panel;
    }

    private void loadDetails(Booking booking, TextView hotelName, TextView roomBadge,
                             TextView paymentView) {
        ApiClient.create(requireContext()).bookingRooms(booking.bookingId)
                .enqueue(new Callback<Resp<List<BookingRoom>>>() {
                    @Override public void onResponse(@NonNull Call<Resp<List<BookingRoom>>> call,
                                                     @NonNull Response<Resp<List<BookingRoom>>> response) {
                        if (!isAdded() || response.body() == null || response.body().data == null
                                || response.body().data.isEmpty()) return;
                        BookingRoom row = response.body().data.get(0);
                        Room room = rooms.get(row.roomId);
                        if (room == null) return;
                        Hotel hotel = hotels.get(room.hotelId);
                        hotelName.setText("BOOKED HOTEL\n" + (hotel == null ? "Hotel #" + room.hotelId : hotel.hotelName));
                        roomBadge.setText("ROOM #" + room.roomNumber + "  \u00B7  " + room.roomType);
                        roomBadge.setVisibility(View.VISIBLE);
                    }
                    @Override public void onFailure(@NonNull Call<Resp<List<BookingRoom>>> call,
                                                    @NonNull Throwable error) { }
                });

        ApiClient.create(requireContext()).bookingPayment(booking.bookingId)
                .enqueue(new Callback<Resp<Payment>>() {
                    @Override public void onResponse(@NonNull Call<Resp<Payment>> call,
                                                     @NonNull Response<Resp<Payment>> response) {
                        if (!isAdded() || response.body() == null || response.body().data == null) return;
                        Payment payment = response.body().data;
                        String method = "RAZORPAY".equalsIgnoreCase(payment.method) ? "ONLINE" :
                                (payment.method == null ? "NO PAYMENT" : payment.method.toUpperCase(Locale.ENGLISH));
                        String paymentStatus = payment.status == null ? "NOT RECORDED" : payment.status.toUpperCase(Locale.ENGLISH);
                        paymentView.setText(method + "\n" + paymentStatus);
                        boolean paid = "PAID".equals(paymentStatus);
                        boolean failed = "FAILED".equals(paymentStatus);
                        paymentView.setTextColor(paid ? 0xFF15803D : failed ? 0xFFB91C1C : 0xFFA16207);
                        paymentView.setBackground(rounded(paid ? 0xFFDCFCE7 : failed ? 0xFFFEE2E2 : 0xFFFEF3C7, 12));
                    }
                    @Override public void onFailure(@NonNull Call<Resp<Payment>> call,
                                                    @NonNull Throwable error) { }
                });
    }

    private void confirmCancel(Integer id) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel booking?")
                .setMessage("This reservation will be cancelled. This action cannot be undone.")
                .setNegativeButton("Keep booking", null)
                .setPositiveButton("Cancel booking", (dialog, which) -> cancel(id))
                .show();
    }

    private void cancel(Integer id) {
        ApiClient.create(requireContext()).cancel(id).enqueue(new Callback<Resp<Object>>() {
            @Override public void onResponse(@NonNull Call<Resp<Object>> call,
                                             @NonNull Response<Resp<Object>> response) {
                if (!isAdded()) return;
                Ui.toast(requireContext(), response.isSuccessful()
                        ? "Booking cancelled successfully" : "Unable to cancel booking");
                if (response.isSuccessful()) load();
            }
            @Override public void onFailure(@NonNull Call<Resp<Object>> call,
                                            @NonNull Throwable error) {
                if (isAdded()) Ui.toast(requireContext(), "Network error");
            }
        });
    }

    private void emptyState() {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setRadius(dp(20)); card.setCardBackgroundColor(Color.WHITE); card.setCardElevation(0);
        LinearLayout content = vertical();
        content.setGravity(Gravity.CENTER);
        content.setPadding(dp(20), dp(55), dp(20), dp(55));
        content.addView(label("No bookings found", 22, 0xFF18181B, true));
        content.addView(label("Book an approved hotel and your reservation will appear here.",
                13, 0xFF71717A, false));
        card.addView(content); cards.addView(card);
    }

    private TextView statusBadge(String status) {
        int background = "CANCELLED".equals(status) ? 0xFFFEE2E2 :
                ("COMPLETED".equals(status) ? 0xFFE0E7FF : 0xFFDCFCE7);
        int foreground = "CANCELLED".equals(status) ? 0xFFB91C1C :
                ("COMPLETED".equals(status) ? 0xFF3730A3 : 0xFF166534);
        TextView badge = label("\u25CF  " + status, 11, foreground, true);
        badge.setPadding(dp(11), dp(7), dp(11), dp(7));
        badge.setBackground(rounded(background, 14));
        return badge;
    }

    private int count(String wanted) {
        int count = 0;
        for (Booking booking : bookings) if (wanted.equals(status(booking))) count++;
        return count;
    }

    private String status(Booking booking) {
        return booking.status == null ? "BOOKED" : booking.status.toUpperCase(Locale.ENGLISH);
    }

    private String formatDate(String value) {
        try {
            return LocalDate.parse(value).format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
        } catch (Exception ignored) { return value == null ? "Not provided" : value; }
    }

    private String money(double value) {
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        format.setMaximumFractionDigits(0);
        return format.format(value);
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color); drawable.setCornerRadius(dp(radius)); return drawable;
    }

    private LinearLayout vertical() { LinearLayout layout = new LinearLayout(requireContext()); layout.setOrientation(LinearLayout.VERTICAL); return layout; }
    private LinearLayout horizontal() { LinearLayout layout = new LinearLayout(requireContext()); layout.setOrientation(LinearLayout.HORIZONTAL); layout.setGravity(Gravity.CENTER_VERTICAL); return layout; }
    private LinearLayout.LayoutParams bottom(int margin) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.setMargins(0, 0, 0, dp(margin)); return p; }
    private TextView label(String value, int size, int color, boolean bold) { TextView t = new TextView(requireContext()); t.setText(value); t.setTextSize(size); t.setTextColor(color); if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return t; }
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private interface DataConsumer<T> { void accept(List<T> data); }
}
