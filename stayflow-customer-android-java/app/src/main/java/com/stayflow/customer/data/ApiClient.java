package com.stayflow.customer.data;

import android.content.Context;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    // Physical phone and computer must be on the same Wi-Fi.
    public static final String BASE_URL="http://192.168.1.6:8081/";
    public static ApiService create(Context context){
        SessionManager session=new SessionManager(context);
        HttpLoggingInterceptor logging=new HttpLoggingInterceptor(); logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient http=new OkHttpClient.Builder().addInterceptor(chain->{
            okhttp3.Request original=chain.request();
            if(original.url().encodedPath().endsWith("/api/auth/login")||original.url().encodedPath().endsWith("/api/auth/signup")) return chain.proceed(original);
            String token=session.token();
            return chain.proceed(token==null?original:original.newBuilder().header("Authorization","Bearer "+token).build());
        }).addInterceptor(logging).build();
        return new Retrofit.Builder().baseUrl(BASE_URL).client(http).addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create())).build().create(ApiService.class);
    }
}
