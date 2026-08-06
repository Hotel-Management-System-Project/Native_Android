package com.stayflow.customer.data;

import com.stayflow.customer.model.Models.Review;
import com.stayflow.customer.model.Models.ReviewCreated;
import com.stayflow.customer.model.Models.ReviewRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/** Retrofit contract for the separate Express.js review service. */
public interface ReviewApiService {
    @POST("review/") Call<ReviewCreated> createReview(@Body ReviewRequest body);
    @GET("review/hotel/{hotelId}") Call<List<Review>> reviewsByHotel(@Path("hotelId") int hotelId);
}
