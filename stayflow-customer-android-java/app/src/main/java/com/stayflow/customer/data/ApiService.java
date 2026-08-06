package com.stayflow.customer.data;

import com.stayflow.customer.model.Models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("api/auth/login") Call<Resp<LoginData>> login(@Body LoginRequest body);
    @POST("api/auth/signup") Call<Resp<Object>> signup(@Body SignupRequest body);
    @POST("api/auth/send-signup-otp") Call<Resp<Object>> sendSignupOtp(@Body java.util.Map<String,String> body);
    @POST("api/auth/verify-signup-otp") Call<Resp<java.util.Map<String,String>>> verifySignupOtp(@Body java.util.Map<String,String> body);
    @POST("api/auth/forgot-password/send-otp") Call<Resp<Object>> sendPasswordResetOtp(@Body java.util.Map<String,String> body);
    @POST("api/auth/forgot-password/reset") Call<Resp<Object>> resetPassword(@Body java.util.Map<String,String> body);
    @PUT("api/auth/change-password") Call<Resp<Object>> changePassword(@Body PasswordRequest body);
    @GET("api/hotels") Call<Resp<List<Hotel>>> hotels();
    // This backend endpoint returns a raw JSON array, not the standard Resp wrapper.
    @GET("api/hotel-images/hotel/{hotelId}") Call<List<HotelImage>> hotelImages(@Path("hotelId") int hotelId);
    @GET("getAllRooms") Call<Resp<List<Room>>> rooms();
    @GET("api/room-images/room/{roomId}") Call<Resp<List<RoomImage>>> roomImages(@Path("roomId") int roomId);
    @GET("api/booking-rooms/availability") Call<Resp<Boolean>> roomAvailability(
            @Query("roomId") int roomId,
            @Query("checkIn") String checkIn,
            @Query("checkOut") String checkOut);
    @POST("api/bookings") Call<Resp<Booking>> createBooking(@Body Booking body);
    @POST("api/booking-rooms") Call<Resp<BookingRoom>> addBookingRoom(@Body BookingRoom body);
    @GET("api/booking-rooms/{bookingId}") Call<Resp<List<BookingRoom>>> bookingRooms(@Path("bookingId") int bookingId);
    @POST("api/payments/cash/{bookingId}") Call<Resp<Object>> createCashPayment(@Path("bookingId") int bookingId);
    @POST("api/payments/razorpay/order/{bookingId}") Call<Resp<RazorpayOrder>> createRazorpayOrder(@Path("bookingId") int bookingId);
    @POST("api/payments/razorpay/verify") Call<Resp<Object>> verifyRazorpayPayment(@Body RazorpayVerification body);
    @GET("api/payments/booking/{bookingId}") Call<Resp<Payment>> bookingPayment(@Path("bookingId") int bookingId);
    @GET("api/bookings/my") Call<Resp<List<Booking>>> myBookings();
    @PUT("api/bookings/cancel/{id}") Call<Resp<Object>> cancel(@Path("id") int id);
}
