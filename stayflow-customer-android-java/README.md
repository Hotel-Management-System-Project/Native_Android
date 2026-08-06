# StayFlow Customer — Native Android Java

Native Android Studio application written in Java. It connects to the existing Spring Boot API and exposes customer operations only.

## Included

- Customer-only signup and JWT login
- Approved hotel browsing
- Hotel-wise available rooms
- Booking creation and booking-room association
- My Bookings and cancellation
- Profile, password change, and logout
- Retrofit/OkHttp, Gson, Material 3, SharedPreferences JWT storage

## Run

1. Open this folder in Android Studio.
2. Use JDK 17 and install Android SDK 35.
3. Let Gradle sync.
4. Start Spring Boot on port 8081.
5. Ensure phone and PC use the same Wi-Fi.
6. Run the `app` configuration on an Android phone/emulator.

The physical-phone backend URL is configured in `ApiClient.java` as `http://172.18.4.191:8081/`. For Android Emulator change it to `http://10.0.2.2:8081/`.

## Security

The app rejects every login whose backend role is not `CUSTOMER`. Authorization must still be enforced by Spring Security on the server.
