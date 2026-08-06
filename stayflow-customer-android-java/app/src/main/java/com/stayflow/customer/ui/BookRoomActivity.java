package com.stayflow.customer.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.stayflow.customer.data.ApiClient;
import com.stayflow.customer.data.SessionManager;
import com.stayflow.customer.model.Models.Booking;
import com.stayflow.customer.model.Models.BookingRoom;
import com.stayflow.customer.model.Models.Resp;
import com.stayflow.customer.model.Models.RazorpayOrder;
import com.stayflow.customer.model.Models.RazorpayVerification;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Complete-booking page with calendar inputs and live price calculation. */
public class BookRoomActivity extends AppCompatActivity {
    private int roomId;
    private int roomNumber;
    private double price;
    private String hotelName;
    private String roomType;
    private Calendar checkIn;
    private Calendar checkOut;
    private TextView checkInValue;
    private TextView checkOutValue;
    private TextView totalValue;
    private TextView nightsValue;
    private MaterialButton confirmButton;
    private ProgressBar progress;
    private String paymentMethod = "CASH";
    private MaterialCardView cashOption;
    private MaterialCardView onlineOption;
    private TextView cashRadio;
    private TextView onlineRadio;
    private Integer pendingBookingId;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Ui.applyHeaderStatusBar(this);
        roomId = getIntent().getIntExtra("roomId", 0);
        roomNumber = getIntent().getIntExtra("roomNumber", 0);
        price = getIntent().getDoubleExtra("price", 0);
        hotelName = value(getIntent().getStringExtra("hotelName"), "Selected hotel");
        roomType = value(getIntent().getStringExtra("roomType"), "Hotel room");
        checkIn = parseDate(getIntent().getStringExtra("checkInDate"));
        checkOut = parseDate(getIntent().getStringExtra("checkOutDate"));

        LinearLayout page = vertical();
        page.setBackgroundColor(0xFFF7F7F8);
        page.addView(Ui.gradientStatusBarSpacer(this));
        page.addView(hero());

        LinearLayout body = vertical();
        body.setPadding(dp(18), dp(20), dp(18), dp(32));
        body.addView(roomSummary(), bottom(16));
        body.addView(text("Select dates", 25, 0xFF18181B, true), bottom(12));
        body.addView(dateCard(), bottom(16));
        body.addView(totalCard(), bottom(18));
        body.addView(paymentCard(), bottom(18));

        confirmButton = new MaterialButton(this);
        confirmButton.setText("Confirm cash booking");
        confirmButton.setTextSize(16);
        confirmButton.setTextColor(Color.WHITE);
        confirmButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        confirmButton.setCornerRadius(dp(18));
        confirmButton.setBackgroundTintList(ColorStateList.valueOf(0xFFE11D2E));
        confirmButton.setOnClickListener(v -> submit());
        body.addView(confirmButton, new LinearLayout.LayoutParams(-1, dp(58)));

        progress = new ProgressBar(this);
        progress.setVisibility(android.view.View.GONE);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(-1, dp(42));
        progressParams.setMargins(0, dp(8), 0, 0);
        body.addView(progress, progressParams);
        page.addView(body);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(page);
        setContentView(scroll);
        Ui.applySavedTheme(scroll, this);
        updateDatesAndTotal();
    }

    private ViewGroup hero() {
        LinearLayout hero = vertical();
        hero.setPadding(dp(18), dp(14), dp(18), dp(24));
        GradientDrawable background = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF991B1B, 0xFFEF233C, 0xFFFB7185});
        background.setCornerRadii(new float[]{0, 0, 0, 0, dp(28), dp(28), dp(28), dp(28)});
        hero.setBackground(background);

        LinearLayout toolbar = horizontal();
        MaterialButton back = new MaterialButton(this);
        back.setText("‹");
        back.setTextSize(30);
        back.setTextColor(Color.WHITE);
        back.setBackgroundTintList(ColorStateList.valueOf(0x33FFFFFF));
        back.setCornerRadius(dp(26));
        back.setOnClickListener(v -> finish());
        toolbar.addView(back, new LinearLayout.LayoutParams(dp(54), dp(54)));

        TextView toolbarTitle = text("Complete booking", 19, Color.WHITE, true);
        toolbarTitle.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams toolbarTitleParams = new LinearLayout.LayoutParams(0, dp(54), 1f);
        toolbarTitleParams.setMargins(dp(14), 0, 0, 0);
        toolbar.addView(toolbarTitle, toolbarTitleParams);
        hero.addView(toolbar);

        TextView hotel = text(hotelName, 30, Color.WHITE, true);
        hotel.setPadding(0, dp(18), 0, dp(4));
        hero.addView(hotel);
        hero.addView(text("Room #" + roomNumber + " · " + roomType, 15, 0xFFFFE4E6, false));
        return hero;
    }

    private MaterialCardView roomSummary() {
        MaterialCardView card = card();
        LinearLayout row = horizontal();
        row.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView icon = text("▣", 28, 0xFFE11D2E, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(rounded(0xFFFFE4E6, 16));
        row.addView(icon, new LinearLayout.LayoutParams(dp(64), dp(64)));

        LinearLayout copy = vertical();
        copy.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text(roomType + " room", 19, 0xFF18181B, true);
        copy.addView(title);
        copy.addView(text("Room #" + roomNumber + " · ₹" + money(price) + " per night",
                13, 0xFF71717A, false));
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(0, dp(64), 1f);
        copyParams.setMargins(dp(14), 0, 0, 0);
        row.addView(copy, copyParams);
        card.addView(row);
        return card;
    }

    private MaterialCardView dateCard() {
        MaterialCardView card = card();
        LinearLayout content = vertical();
        content.setPadding(dp(14), dp(14), dp(14), dp(14));
        checkInValue = dateSelector("CHECK-IN");
        checkOutValue = dateSelector("CHECK-OUT");
        checkInValue.setOnClickListener(v -> pickDate(true));
        checkOutValue.setOnClickListener(v -> pickDate(false));
        content.addView(checkInValue, bottom(10));
        content.addView(checkOutValue);
        card.addView(content);
        return card;
    }

    private TextView dateSelector(String label) {
        TextView selector = text(label + "\nChoose date", 15, 0xFF18181B, true);
        selector.setGravity(Gravity.CENTER_VERTICAL);
        selector.setPadding(dp(18), dp(10), dp(18), dp(10));
        GradientDrawable border = rounded(Color.WHITE, 12);
        border.setStroke(dp(1), 0xFFA1A1AA);
        selector.setBackground(border);
        selector.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_my_calendar, 0, 0, 0);
        selector.setCompoundDrawablePadding(dp(14));
        selector.setMinHeight(dp(70));
        return selector;
    }

    private MaterialCardView totalCard() {
        MaterialCardView card = card();
        LinearLayout row = horizontal();
        row.setPadding(dp(18), dp(18), dp(18), dp(18));
        LinearLayout amount = vertical();
        amount.addView(text("TOTAL BOOKING VALUE", 11, 0xFFA1A1AA, true));
        totalValue = text("₹0", 31, 0xFF18181B, true);
        totalValue.setPadding(0, dp(3), 0, 0);
        amount.addView(totalValue);
        nightsValue = text("Select dates", 13, 0xFF71717A, false);
        amount.addView(nightsValue);
        row.addView(amount, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView secure = text("✓\nSecure", 12, 0xFF15803D, true);
        secure.setGravity(Gravity.CENTER);
        secure.setBackground(rounded(0xFFECFDF3, 14));
        row.addView(secure, new LinearLayout.LayoutParams(dp(72), dp(72)));
        card.addView(row);
        return card;
    }

    private MaterialCardView paymentCard() {
        MaterialCardView card = card();
        LinearLayout content = vertical();
        content.setPadding(dp(16), dp(16), dp(16), dp(16));
        content.addView(text("Payment method", 18, 0xFF18181B, true), bottom(12));

        cashOption = paymentOption("Cash at hotel", "Pay when you arrive at the property", true);
        cashRadio = (TextView) ((LinearLayout) cashOption.getChildAt(0)).getChildAt(2);
        cashOption.setOnClickListener(v -> selectPayment("CASH"));
        content.addView(cashOption, bottom(10));

        onlineOption = paymentOption("Pay online", "UPI, cards, netbanking and wallets via Razorpay", false);
        onlineRadio = (TextView) ((LinearLayout) onlineOption.getChildAt(0)).getChildAt(2);
        onlineOption.setOnClickListener(v -> selectPayment("RAZORPAY"));
        content.addView(onlineOption);
        card.addView(content);
        return card;
    }

    private MaterialCardView paymentOption(String title, String subtitle, boolean selected) {
        MaterialCardView option = new MaterialCardView(this);
        option.setRadius(dp(14));
        option.setCardElevation(0);
        option.setStrokeWidth(dp(selected ? 2 : 1));
        option.setStrokeColor(selected ? 0xFFE11D2E : 0xFFD4D4D8);
        option.setCardBackgroundColor(selected ? 0xFFFFF1F2 : surfaceColor());
        option.setClickable(true);

        LinearLayout row = horizontal();
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        TextView icon = text(title.startsWith("Cash") ? "\u20B9" : "\uD83D\uDD12", 21, 0xFFE11D2E, true);
        icon.setGravity(Gravity.CENTER);
        row.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout copy = vertical();
        copy.addView(text(title, 15, 0xFF18181B, true));
        copy.addView(text(subtitle, 11, 0xFF71717A, false));
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(0, -2, 1f);
        copyParams.setMargins(dp(8), 0, dp(8), 0);
        row.addView(copy, copyParams);

        TextView radio = text(selected ? "\u25C9" : "\u25CB", 25, 0xFFE11D2E, true);
        radio.setGravity(Gravity.CENTER);
        row.addView(radio, new LinearLayout.LayoutParams(dp(34), dp(42)));
        option.addView(row);
        return option;
    }

    private void selectPayment(String method) {
        paymentMethod = method;
        boolean cash = "CASH".equals(method);
        stylePaymentOption(cashOption, cashRadio, cash);
        stylePaymentOption(onlineOption, onlineRadio, !cash);
        confirmButton.setText(cash ? "Confirm cash booking" : "Pay securely online");
    }

    private void stylePaymentOption(MaterialCardView option, TextView radio, boolean selected) {
        option.setStrokeWidth(dp(selected ? 2 : 1));
        option.setStrokeColor(selected ? 0xFFE11D2E : 0xFFD4D4D8);
        option.setCardBackgroundColor(selected ? 0xFFFFF1F2 : surfaceColor());
        radio.setText(selected ? "\u25C9" : "\u25CB");
    }

    private void pickDate(boolean selectingCheckIn) {
        Calendar initial = selectingCheckIn
                ? (checkIn == null ? Calendar.getInstance() : checkIn)
                : (checkOut == null ? (checkIn == null ? Calendar.getInstance() : checkIn) : checkOut);
        DatePickerDialog dialog = new DatePickerDialog(this, (picker, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);
            if (selectingCheckIn) {
                checkIn = selected;
                if (checkOut != null && !checkOut.after(checkIn)) checkOut = null;
                updateDatesAndTotal();
                pickDate(false);
            } else {
                if (checkIn == null || !selected.after(checkIn)) {
                    Ui.toast(this, "Check-out must be after check-in");
                    return;
                }
                checkOut = selected;
                updateDatesAndTotal();
            }
        }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(selectingCheckIn
                ? System.currentTimeMillis() - 1000
                : (checkIn == null ? System.currentTimeMillis() : checkIn.getTimeInMillis() + 86_400_000L));
        dialog.show();
    }

    private void updateDatesAndTotal() {
        if (checkInValue == null) return;
        checkInValue.setText("CHECK-IN\n" + (checkIn == null ? "Choose date" : displayDate(checkIn)));
        checkOutValue.setText("CHECK-OUT\n" + (checkOut == null ? "Choose date" : displayDate(checkOut)));
        long nights = calculateNights();
        totalValue.setText("₹" + money(nights * price));
        nightsValue.setText(nights + " night" + (nights == 1 ? "" : "s"));
    }

    private void submit() {
        if (checkIn == null || checkOut == null) {
            Ui.toast(this, "Select check-in and check-out dates");
            return;
        }
        setLoading(true);
        ApiClient.create(this).roomAvailability(roomId, apiDate(checkIn), apiDate(checkOut))
                .enqueue(new Callback<Resp<Boolean>>() {
                    @Override public void onResponse(@NonNull Call<Resp<Boolean>> call,
                                                     @NonNull Response<Resp<Boolean>> response) {
                        if (!response.isSuccessful() || response.body() == null
                                || !Boolean.TRUE.equals(response.body().data)) {
                            setLoading(false);
                            Ui.toast(BookRoomActivity.this, "Room is unavailable for these dates");
                            return;
                        }
                        createBooking();
                    }
                    @Override public void onFailure(@NonNull Call<Resp<Boolean>> call,
                                                    @NonNull Throwable error) {
                        setLoading(false);
                        Ui.toast(BookRoomActivity.this, error.getMessage());
                    }
                });
    }

    private void createBooking() {
        Booking booking = new Booking();
        booking.checkInDate = apiDate(checkIn);
        booking.checkOutDate = apiDate(checkOut);
        booking.status = "BOOKED";
        booking.totalAmount = calculateNights() * price;
        ApiClient.create(this).createBooking(booking).enqueue(new Callback<Resp<Booking>>() {
            @Override public void onResponse(@NonNull Call<Resp<Booking>> call,
                                             @NonNull Response<Resp<Booking>> response) {
                if (response.body() == null || response.body().data == null) {
                    setLoading(false);
                    Ui.toast(BookRoomActivity.this, "Booking failed");
                    return;
                }
                pendingBookingId = response.body().data.bookingId;
                attachRoom(pendingBookingId);
            }
            @Override public void onFailure(@NonNull Call<Resp<Booking>> call,
                                            @NonNull Throwable error) {
                setLoading(false);
                Ui.toast(BookRoomActivity.this, error.getMessage());
            }
        });
    }

    private void attachRoom(int bookingId) {
        BookingRoom room = new BookingRoom();
        room.bookingId = bookingId;
        room.roomId = roomId;
        room.pricePerNight = price;
        ApiClient.create(this).addBookingRoom(room).enqueue(new Callback<Resp<BookingRoom>>() {
            @Override public void onResponse(@NonNull Call<Resp<BookingRoom>> call,
                                             @NonNull Response<Resp<BookingRoom>> response) {
                if (response.body() != null && response.body().data != null) {
                    createPayment(bookingId);
                } else {
                    setLoading(false);
                    Ui.toast(BookRoomActivity.this, "Room was booked by another customer");
                }
            }
            @Override public void onFailure(@NonNull Call<Resp<BookingRoom>> call,
                                            @NonNull Throwable error) {
                setLoading(false);
                Ui.toast(BookRoomActivity.this, error.getMessage());
            }
        });
    }

    private void createPayment(int bookingId) {
        if ("CASH".equals(paymentMethod)) {
            ApiClient.create(this).createCashPayment(bookingId).enqueue(new Callback<Resp<Object>>() {
                @Override public void onResponse(@NonNull Call<Resp<Object>> call,
                                                 @NonNull Response<Resp<Object>> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null
                            && "success".equalsIgnoreCase(response.body().status)) {
                        Ui.toast(BookRoomActivity.this, "Booking confirmed. Pay cash at the hotel.");
                        finish();
                    } else cancelFailedPayment("Unable to register cash payment");
                }
                @Override public void onFailure(@NonNull Call<Resp<Object>> call,
                                                @NonNull Throwable error) {
                    setLoading(false);
                    cancelFailedPayment(error.getMessage());
                }
            });
            return;
        }

        ApiClient.create(this).createRazorpayOrder(bookingId).enqueue(new Callback<Resp<RazorpayOrder>>() {
            @Override public void onResponse(@NonNull Call<Resp<RazorpayOrder>> call,
                                             @NonNull Response<Resp<RazorpayOrder>> response) {
                setLoading(false);
                RazorpayOrder order = response.body() == null ? null : response.body().data;
                if (order == null || order.orderId == null || order.keyId == null) {
                    cancelFailedPayment("Unable to start online payment");
                    return;
                }
                openRazorpay(order);
            }
            @Override public void onFailure(@NonNull Call<Resp<RazorpayOrder>> call,
                                            @NonNull Throwable error) {
                setLoading(false);
                cancelFailedPayment(error.getMessage());
            }
        });
    }

    private void openRazorpay(RazorpayOrder order) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Material_Light_NoActionBar);
        LinearLayout page = vertical();
        TextView close = text("Secure online payment     \u2715", 18, 0xFF18181B, true);
        close.setGravity(Gravity.CENTER_VERTICAL);
        close.setPadding(dp(18), dp(12), dp(18), dp(12));
        page.addView(close, new LinearLayout.LayoutParams(-1, dp(58)));

        WebView web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient());
        PaymentBridge bridge = new PaymentBridge(dialog);
        web.addJavascriptInterface(bridge, "AndroidPayment");
        page.addView(web, new LinearLayout.LayoutParams(-1, 0, 1f));
        close.setOnClickListener(v -> bridge.cancelled("Online payment was cancelled"));
        dialog.setOnCancelListener(v -> cancelPendingBooking("Online payment was cancelled"));
        dialog.setContentView(page);
        dialog.show();

        String html = "<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "</head><body><script src='https://checkout.razorpay.com/v1/checkout.js'></script><script>"
                + "var options={key:'" + js(order.keyId) + "',amount:" + order.amount
                + ",currency:'" + js(order.currency) + "',name:'StayFlow',description:'Hotel room booking',"
                + "order_id:'" + js(order.orderId) + "',theme:{color:'#E11D2E'},modal:{confirm_close:true,"
                + "ondismiss:function(){AndroidPayment.cancel('Online payment was cancelled');}},"
                + "handler:function(r){AndroidPayment.success(r.razorpay_order_id,r.razorpay_payment_id,r.razorpay_signature);}};"
                + "var checkout=new Razorpay(options);checkout.on('payment.failed',function(r){AndroidPayment.cancel(r.error.description);});checkout.open();"
                + "</script></body></html>";
        web.loadDataWithBaseURL("https://checkout.razorpay.com", html, "text/html", "UTF-8", null);
    }

    private final class PaymentBridge {
        private final Dialog dialog;
        private boolean finished;
        PaymentBridge(Dialog dialog) { this.dialog = dialog; }

        @JavascriptInterface public void success(String orderId, String paymentId, String signature) {
            runOnUiThread(() -> {
                if (finished) return;
                finished = true;
                dialog.dismiss();
                verifyPayment(orderId, paymentId, signature);
            });
        }

        @JavascriptInterface public void cancel(String message) { cancelled(message); }

        void cancelled(String message) {
            runOnUiThread(() -> {
                if (finished) return;
                finished = true;
                dialog.dismiss();
                cancelPendingBooking(message);
            });
        }
    }

    private void verifyPayment(String orderId, String paymentId, String signature) {
        setLoading(true);
        RazorpayVerification body = new RazorpayVerification(orderId, paymentId, signature);
        ApiClient.create(this).verifyRazorpayPayment(body).enqueue(new Callback<Resp<Object>>() {
            @Override public void onResponse(@NonNull Call<Resp<Object>> call,
                                             @NonNull Response<Resp<Object>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null
                        && "success".equalsIgnoreCase(response.body().status)) {
                    Ui.toast(BookRoomActivity.this, "Online payment verified. Booking confirmed.");
                    finish();
                } else cancelFailedPayment("Payment verification failed");
            }
            @Override public void onFailure(@NonNull Call<Resp<Object>> call,
                                            @NonNull Throwable error) {
                setLoading(false);
                cancelFailedPayment(error.getMessage());
            }
        });
    }

    private void cancelFailedPayment(String message) {
        cancelPendingBooking(message == null ? "Payment failed" : message);
    }

    private void cancelPendingBooking(String message) {
        if (pendingBookingId == null) {
            Ui.toast(this, message);
            return;
        }
        ApiClient.create(this).cancel(pendingBookingId).enqueue(new Callback<Resp<Object>>() {
            @Override public void onResponse(@NonNull Call<Resp<Object>> call,
                                             @NonNull Response<Resp<Object>> response) {
                pendingBookingId = null;
                Ui.toast(BookRoomActivity.this, message);
            }
            @Override public void onFailure(@NonNull Call<Resp<Object>> call,
                                            @NonNull Throwable error) {
                pendingBookingId = null;
                Ui.toast(BookRoomActivity.this, message);
            }
        });
    }

    private void setLoading(boolean loading) {
        confirmButton.setEnabled(!loading);
        confirmButton.setText(loading ? "Processing..." :
                ("CASH".equals(paymentMethod) ? "Confirm cash booking" : "Pay securely online"));
        progress.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private MaterialCardView card() {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(dp(20));
        card.setCardElevation(dp(2));
        card.setCardBackgroundColor(surfaceColor());
        card.setStrokeColor(0xFFE4E4E7);
        card.setStrokeWidth(dp(1));
        return card;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private LinearLayout vertical() { LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL); return layout; }
    private LinearLayout horizontal() { LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.HORIZONTAL); layout.setGravity(Gravity.CENTER_VERTICAL); return layout; }
    private TextView text(String value, int size, int color, boolean bold) { TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color); if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return view; }
    private LinearLayout.LayoutParams bottom(int margin) { LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2); params.setMargins(0, 0, 0, dp(margin)); return params; }
    private long calculateNights() { if (checkIn == null || checkOut == null) return 0; return Math.max(1, ChronoUnit.DAYS.between(LocalDate.parse(apiDate(checkIn)), LocalDate.parse(apiDate(checkOut)))); }
    private Calendar parseDate(String value) { try { Calendar calendar = Calendar.getInstance(); calendar.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(value)); return calendar; } catch (Exception ignored) { return null; } }
    private String displayDate(Calendar value) { return new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(value.getTime()); }
    private String apiDate(Calendar value) { return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(value.getTime()); }
    private String money(double value) { return String.format(new Locale("en", "IN"), "%,.0f", value); }
    private String value(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
    private String js(String value) { return value == null ? "" : value.replace("\\", "\\\\").replace("'", "\\'"); }
    private int surfaceColor() { return new SessionManager(this).darkMode() ? 0xFF18181B : Color.WHITE; }
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
