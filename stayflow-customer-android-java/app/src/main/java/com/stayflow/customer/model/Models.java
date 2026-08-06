package com.stayflow.customer.model;

import java.util.List;

public final class Models {
    public static class Resp<T> { public String status; public T data; public String message; }
    public static class LoginRequest { public String email,password; public LoginRequest(String e,String p){email=e;password=p;} }
    public static class LoginData { public String token,email,role; }
    public static class SignupRequest { public String fullName,email,password,phone,role="CUSTOMER",emailVerificationToken; }
    public static class Hotel { public Integer hotelId,ownerId; public String hotelName,description,address,city,state,pincode,status; public double rating; }
    public static class HotelImage { public Integer imageId,hotelId; public String imageUrl; }
    public static class Room { public Integer roomId,hotelId,roomNumber,capacity; public String roomType; public double pricePerNight; public boolean availabilityStatus; }
    public static class RoomImage { public Integer imageId,roomId; public String imageUrl; }
    public static class Booking { public Integer bookingId,userId; public String checkInDate,checkOutDate,status; public double totalAmount; }
    public static class BookingRoom { public Integer bookingRoomId,bookingId,roomId; public double pricePerNight; }
    public static class Payment { public Integer paymentId,bookingId; public String method,status,currency; public double amount; }
    public static class RazorpayOrder { public String orderId,keyId,currency; public long amount; }
    public static class RazorpayVerification {
        public String razorpayOrderId,razorpayPaymentId,razorpaySignature;
        public RazorpayVerification(String orderId,String paymentId,String signature) {
            razorpayOrderId=orderId; razorpayPaymentId=paymentId; razorpaySignature=signature;
        }
    }
    public static class PasswordRequest { public String oldPassword,newPassword; public PasswordRequest(String o,String n){oldPassword=o;newPassword=n;} }
    public static class Review {
        public Integer review_id, user_id, booking_id, hotel_id, rating;
        // The review service may expose the joined customer name under any of these keys.
        public String comment, created_at, customer_name, full_name, user_name;
    }
    public static class ReviewRequest {
        public Integer user_id, booking_id, hotel_id, rating;
        public String comment;
        public ReviewRequest(Integer userId, Integer bookingId, Integer hotelId,
                             Integer stars, String reviewComment) {
            user_id=userId; booking_id=bookingId; hotel_id=hotelId;
            rating=stars; comment=reviewComment;
        }
    }
    public static class ReviewCreated { public String message; public Integer reviewId; }
}
