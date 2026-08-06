package com.stayflow.customer.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.stayflow.customer.data.ApiClient;
import com.stayflow.customer.data.ReviewApiClient;
import com.stayflow.customer.data.SessionManager;
import com.stayflow.customer.model.Models.Booking;
import com.stayflow.customer.model.Models.BookingRoom;
import com.stayflow.customer.model.Models.Hotel;
import com.stayflow.customer.model.Models.Resp;
import com.stayflow.customer.model.Models.Review;
import com.stayflow.customer.model.Models.ReviewCreated;
import com.stayflow.customer.model.Models.ReviewRequest;
import com.stayflow.customer.model.Models.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Lets customers rate hotels from their real booking history and read hotel reviews. */
public class ReviewsFragment extends Fragment {
    private final List<BookingChoice> choices = new ArrayList<>();
    private final Map<Integer, Room> rooms = new HashMap<>();
    private final Map<Integer, Hotel> hotels = new HashMap<>();
    private Spinner bookingSpinner;
    private RatingBar ratingBar;
    private EditText comment;
    private LinearLayout reviewList;
    private MaterialButton submit;
    private int pendingBookingRooms;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle state) {
        ScrollView scroll = new ScrollView(requireContext());
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(new SessionManager(requireContext()).darkMode()
                ? 0xFF09090B : 0xFFF7F7F8);
        LinearLayout page = vertical();
        page.addView(hero());

        LinearLayout body = vertical();
        body.setPadding(dp(18), dp(20), dp(18), dp(32));
        body.addView(reviewForm(), bottom(22));
        body.addView(text("Your reviews", 23, 0xFF18181B, true), bottom(12));
        reviewList = vertical();
        body.addView(reviewList);
        page.addView(body);
        scroll.addView(page);
        loadData();
        Ui.applySavedTheme(page, requireContext());
        return scroll;
    }

    private View hero() {
        LinearLayout hero = vertical();
        hero.setPadding(dp(20), dp(24), dp(20), dp(26));
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF991B1B, 0xFFEF233C, 0xFFFB7185});
        gradient.setCornerRadii(new float[]{0,0,0,0,dp(28),dp(28),dp(28),dp(28)});
        hero.setBackground(gradient);
        hero.addView(text("YOUR EXPERIENCE", 12, 0xFFFFE4E6, true));
        hero.addView(text("Reviews & ratings", 30, Color.WHITE, true));
        hero.addView(text("Share an honest review after your hotel stay.", 14, 0xFFFFE4E6, false));
        return hero;
    }

    private View reviewForm() {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setRadius(dp(22)); card.setCardElevation(dp(2));
        card.setCardBackgroundColor(Color.WHITE); card.setStrokeColor(0xFFE4E4E7); card.setStrokeWidth(1);
        LinearLayout content = vertical();
        content.setPadding(dp(18), dp(18), dp(18), dp(18));
        content.addView(text("Rate your booking", 21, 0xFF18181B, true));
        content.addView(text("Select a booking so the review is linked to the correct hotel.",
                13, 0xFF71717A, false), bottom(14));

        bookingSpinner = new Spinner(requireContext());
        bookingSpinner.setBackground(rounded(0xFFF4F4F5, 13));
        bookingSpinner.setPadding(dp(12), 0, dp(12), 0);
        content.addView(bookingSpinner, fixedHeight(54, 12));

        ratingBar = new RatingBar(requireContext(), null,
                android.R.attr.ratingBarStyleIndicator);
        ratingBar.setIsIndicator(false); ratingBar.setNumStars(5); ratingBar.setStepSize(1f);
        ratingBar.setRating(5f);
        ratingBar.setProgressTintList(ColorStateList.valueOf(0xFFFFB000));
        ratingBar.setSecondaryProgressTintList(ColorStateList.valueOf(0xFFE4E4E7));
        content.addView(ratingBar, bottom(12));

        comment = new EditText(requireContext());
        comment.setHint("Tell other guests about cleanliness, service, and comfort...");
        comment.setTextColor(0xFF18181B); comment.setHintTextColor(0xFFA1A1AA);
        comment.setGravity(Gravity.TOP); comment.setMinLines(4); comment.setPadding(dp(14),dp(13),dp(14),dp(13));
        comment.setBackground(rounded(0xFFF4F4F5, 13));
        content.addView(comment, bottom(14));

        submit = new MaterialButton(requireContext());
        submit.setText("Submit review"); submit.setTextColor(Color.WHITE);
        submit.setTextSize(15); submit.setCornerRadius(dp(14));
        submit.setBackgroundTintList(ColorStateList.valueOf(0xFFE11D2E));
        submit.setOnClickListener(v -> submitReview());
        content.addView(submit, new LinearLayout.LayoutParams(-1, dp(52)));
        card.addView(content);
        return card;
    }

    private void loadData() {
        submit.setEnabled(false);
        ApiClient.create(requireContext()).rooms().enqueue(listCallback(data -> {
            for (Room room : data) if (room.roomId != null) rooms.put(room.roomId, room);
            loadHotels();
        }));
    }

    private void loadHotels() {
        ApiClient.create(requireContext()).hotels().enqueue(listCallback(data -> {
            for (Hotel hotel : data) if (hotel.hotelId != null) hotels.put(hotel.hotelId, hotel);
            loadBookings();
        }));
    }

    private void loadBookings() {
        ApiClient.create(requireContext()).myBookings().enqueue(listCallback(data -> {
            choices.clear(); pendingBookingRooms = data.size();
            if (data.isEmpty()) { showChoices(); return; }
            for (Booking booking : data) resolveBooking(booking);
        }));
    }

    private void resolveBooking(Booking booking) {
        ApiClient.create(requireContext()).bookingRooms(booking.bookingId)
                .enqueue(new Callback<Resp<List<BookingRoom>>>() {
                    @Override public void onResponse(@NonNull Call<Resp<List<BookingRoom>>> call,
                                                     @NonNull Response<Resp<List<BookingRoom>>> response) {
                        if (response.body()!=null && response.body().data!=null && !response.body().data.isEmpty()) {
                            Room room=rooms.get(response.body().data.get(0).roomId);
                            if (room!=null && room.hotelId!=null) {
                                Hotel hotel=hotels.get(room.hotelId);
                                choices.add(new BookingChoice(booking.bookingId, room.hotelId,
                                        hotel==null ? "Hotel #"+room.hotelId : hotel.hotelName,
                                        room.roomNumber));
                            }
                        }
                        resolvedOne();
                    }
                    @Override public void onFailure(@NonNull Call<Resp<List<BookingRoom>>> call,
                                                    @NonNull Throwable error) { resolvedOne(); }
                });
    }

    private void resolvedOne() { if (--pendingBookingRooms == 0) showChoices(); }

    private void showChoices() {
        if (!isAdded()) return;
        List<String> labels = new ArrayList<>();
        for (BookingChoice choice : choices) labels.add(choice.toString());
        if (labels.isEmpty()) labels.add("No eligible bookings found");
        ArrayAdapter<String> adapter = bookingAdapter(labels);
        bookingSpinner.setAdapter(adapter);
        submit.setEnabled(!choices.isEmpty());
        Ui.applySavedTheme(bookingSpinner, requireContext());
        if (!choices.isEmpty()) loadReviews(choices.get(0).hotelId);
        bookingSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int position, long id) {
                if (position < choices.size()) loadReviews(choices.get(position).hotelId);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) { }
        });
    }

    /** Makes the booking spinner readable and properly spaced in both app themes. */
    private ArrayAdapter<String> bookingAdapter(List<String> labels) {
        final boolean dark = new SessionManager(requireContext()).darkMode();
        final int textColor = dark ? Color.WHITE : 0xFF18181B;
        final int rowColor = dark ? 0xFF27272A : Color.WHITE;

        return new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, labels) {
            @NonNull @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView row = spinnerRow(getItem(position), textColor,
                        dark ? 0xFF18181B : 0xFFF4F4F5);
                row.setSingleLine(true);
                row.setEllipsize(android.text.TextUtils.TruncateAt.END);
                return row;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView,
                                        @NonNull ViewGroup parent) {
                TextView row = spinnerRow(getItem(position), textColor, rowColor);
                row.setMinHeight(dp(58));
                return row;
            }
        };
    }

    private TextView spinnerRow(String value, int textColor, int backgroundColor) {
        TextView row = text(value == null ? "" : value, 15, textColor, true);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(12), dp(16), dp(12));
        row.setBackgroundColor(backgroundColor);
        return row;
    }

    private void submitReview() {
        int position=bookingSpinner.getSelectedItemPosition();
        String message=comment.getText().toString().trim();
        int stars=Math.round(ratingBar.getRating());
        Integer userId=new SessionManager(requireContext()).userId();
        if (position<0 || position>=choices.size()) { Ui.toast(requireContext(),"Select a booking"); return; }
        if (userId==null) { Ui.toast(requireContext(),"Please sign in again"); return; }
        if (stars<1) { Ui.toast(requireContext(),"Select at least one star"); return; }
        if (message.length()<3) { Ui.toast(requireContext(),"Please enter your review"); return; }
        BookingChoice choice=choices.get(position);
        submit.setEnabled(false); submit.setText("Submitting...");
        ReviewApiClient.create().createReview(new ReviewRequest(userId,choice.bookingId,
                choice.hotelId,stars,message)).enqueue(new Callback<ReviewCreated>() {
            @Override public void onResponse(@NonNull Call<ReviewCreated> call,
                                             @NonNull Response<ReviewCreated> response) {
                if (!isAdded()) return;
                submit.setEnabled(true); submit.setText("Submit review");
                if (response.isSuccessful()) {
                    comment.setText(""); ratingBar.setRating(5f);
                    Ui.toast(requireContext(),"Review submitted successfully");
                    loadReviews(choice.hotelId);
                } else Ui.toast(requireContext(),"Unable to submit review");
            }
            @Override public void onFailure(@NonNull Call<ReviewCreated> call,@NonNull Throwable error) {
                if (!isAdded()) return;
                submit.setEnabled(true); submit.setText("Submit review");
                Ui.toast(requireContext(),"Review server unavailable. Start Express on port 4000.");
            }
        });
    }

    private void loadReviews(int hotelId) {
        reviewList.removeAllViews();
        ProgressBar progress=new ProgressBar(requireContext());
        reviewList.addView(progress,new LinearLayout.LayoutParams(-1,dp(48)));
        ReviewApiClient.create().reviewsByHotel(hotelId).enqueue(new Callback<List<Review>>() {
            @Override public void onResponse(@NonNull Call<List<Review>> call,
                                             @NonNull Response<List<Review>> response) {
                if (!isAdded()) return;
                reviewList.removeAllViews();
                List<Review> data=response.body();
                Integer currentUserId = new SessionManager(requireContext()).userId();
                int displayedReviews = 0;

                if (response.isSuccessful() && data != null && currentUserId != null) {
                    for (Review review : data) {
                        // This tab is private: only display reviews created by the signed-in customer.
                        if (review.user_id != null && review.user_id.equals(currentUserId)) {
                            reviewList.addView(reviewCard(review));
                            displayedReviews++;
                        }
                    }
                }

                if (displayedReviews == 0) {
                    reviewList.addView(text("You have not reviewed this hotel yet.",
                            14,0xFF71717A,false));
                    Ui.applySavedTheme(reviewList, requireContext());
                    return;
                }
                Ui.applySavedTheme(reviewList, requireContext());
            }
            @Override public void onFailure(@NonNull Call<List<Review>> call,@NonNull Throwable error) {
                if (!isAdded()) return;
                reviewList.removeAllViews();
                reviewList.addView(text("Could not load reviews. Start the Express server on port 4000.",
                        14,0xFFB91C1C,false));
                Ui.applySavedTheme(reviewList, requireContext());
            }
        });
    }

    private View reviewCard(Review review) {
        MaterialCardView card=new MaterialCardView(requireContext());
        card.setRadius(dp(18)); card.setCardElevation(0); card.setCardBackgroundColor(Color.WHITE);
        card.setStrokeColor(0xFFE4E4E7); card.setStrokeWidth(1);
        LinearLayout box=vertical(); box.setPadding(dp(16),dp(15),dp(16),dp(15));
        int stars=review.rating==null?0:review.rating;
        box.addView(text(String.format(Locale.ENGLISH,"%s  %d/5",starText(stars),stars),17,0xFFFFA000,true));
        box.addView(text(review.comment==null?"No comment":review.comment,14,0xFF3F3F46,false));
        box.addView(text("Verified customer booking #"+review.booking_id,11,0xFF71717A,false));
        card.addView(box);
        card.setLayoutParams(bottom(10)); return card;
    }

    private String starText(int rating) { StringBuilder s=new StringBuilder(); for(int i=1;i<=5;i++) s.append(i<=rating?'★':'☆'); return s.toString(); }
    private <T> Callback<Resp<List<T>>> listCallback(DataConsumer<T> consumer) { return new Callback<>() {
        @Override public void onResponse(@NonNull Call<Resp<List<T>>> call,@NonNull Response<Resp<List<T>>> response) {
            if (!isAdded()) return;
            if (response.isSuccessful() && response.body()!=null && response.body().data!=null) consumer.accept(response.body().data);
            else Ui.toast(requireContext(),"Unable to load booking information");
        }
        @Override public void onFailure(@NonNull Call<Resp<List<T>>> call,@NonNull Throwable error) {
            if (isAdded()) Ui.toast(requireContext(),"Network error");
        }
    }; }
    private static class BookingChoice {
        final int bookingId,hotelId; final String hotelName; final Integer roomNumber;
        BookingChoice(int b,int h,String name,Integer room){bookingId=b;hotelId=h;hotelName=name;roomNumber=room;}
        @NonNull @Override public String toString(){return hotelName+" · Room #"+roomNumber+" · Booking #"+bookingId;}
    }
    private interface DataConsumer<T>{void accept(List<T> data);}
    private GradientDrawable rounded(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private LinearLayout vertical(){LinearLayout l=new LinearLayout(requireContext());l.setOrientation(LinearLayout.VERTICAL);return l;}
    private TextView text(String value,int size,int color,boolean bold){TextView t=new TextView(requireContext());t.setText(value);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setPadding(0,dp(3),0,dp(3));return t;}
    private LinearLayout.LayoutParams bottom(int margin){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(margin));return p;}
    private LinearLayout.LayoutParams fixedHeight(int height,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(height));p.setMargins(0,0,0,dp(bottom));return p;}
    private int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
