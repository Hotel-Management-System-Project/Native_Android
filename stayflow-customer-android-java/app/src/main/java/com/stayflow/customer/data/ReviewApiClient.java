package com.stayflow.customer.data;

import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Connects Android to the Express review module running on the same computer. */
public final class ReviewApiClient {
    // Keep this host equal to ApiClient.BASE_URL; Express review server uses port 4000.
    public static final String BASE_URL = "http://192.168.1.6:4000/";

    private ReviewApiClient() { }

    public static ReviewApiService create() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                .build()
                .create(ReviewApiService.class);
    }
}
