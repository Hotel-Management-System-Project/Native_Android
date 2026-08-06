package com.stayflow.customer.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.stayflow.customer.data.ApiClient;
import com.stayflow.customer.data.SessionManager;
import com.stayflow.customer.model.Models.LoginData;
import com.stayflow.customer.model.Models.LoginRequest;
import com.stayflow.customer.model.Models.Resp;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailField;
    private TextInputEditText passwordField;
    private MaterialButton loginButton;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        SessionManager session = new SessionManager(this);
        if (session.loggedIn()) {
            openDashboard();
            return;
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(9, 10, 13));

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(22), dp(42), dp(22), dp(30));
        scroll.addView(page, new ScrollView.LayoutParams(-1, -1));

        TextView logo = new TextView(this);
        logo.setText("S");
        logo.setGravity(Gravity.CENTER);
        logo.setTextColor(Color.WHITE);
        logo.setTextSize(30);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        logo.setBackgroundResource(com.stayflow.customer.R.drawable.splash_mark);
        logo.getBackground().setTint(Color.rgb(239, 35, 60));
        page.addView(logo, new LinearLayout.LayoutParams(dp(72), dp(72)));

        TextView brand = label("StayFlow", 30, Color.rgb(248, 250, 252), true);
        brand.setPadding(0, dp(12), 0, 0);
        page.addView(brand);
        TextView tagline = label("Customer mobile portal", 15, Color.rgb(148, 151, 160), false);
        tagline.setPadding(0, dp(3), 0, dp(26));
        page.addView(tagline);

        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(Color.rgb(21, 23, 28));
        card.setRadius(dp(22));
        card.setCardElevation(dp(3));
        card.setStrokeColor(Color.rgb(48, 51, 59));
        card.setStrokeWidth(dp(1));

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(22), dp(24), dp(22), dp(24));
        card.addView(form);

        form.addView(label("Welcome back", 25, Color.rgb(248, 250, 252), true));
        TextView instruction = label("Sign in to find rooms and manage your bookings.", 15,
                Color.rgb(174, 177, 185), false);
        instruction.setPadding(0, dp(5), 0, dp(20));
        form.addView(instruction);

        emailLayout = inputLayout("Email address");
        emailField = new TextInputEditText(this);
        emailField.setSingleLine(true);
        emailField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailField.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        emailField.setTextSize(16);
        emailLayout.addView(emailField);
        form.addView(emailLayout, fieldParams());

        passwordLayout = inputLayout("Password");
        passwordLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        passwordField = new TextInputEditText(this);
        passwordField.setSingleLine(true);
        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordField.setTextSize(16);
        passwordField.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login(session);
                return true;
            }
            return false;
        });
        passwordLayout.addView(passwordField);
        form.addView(passwordLayout, fieldParams());

        MaterialButton forgotButton = new MaterialButton(this);
        forgotButton.setText("Forgot password?");
        forgotButton.setTextSize(14);
        forgotButton.setTextColor(Color.rgb(255, 113, 128));
        forgotButton.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        forgotButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
        forgotButton.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
        LinearLayout.LayoutParams forgotParams = new LinearLayout.LayoutParams(-1, dp(44));
        forgotParams.setMargins(0, dp(-8), 0, dp(10));
        form.addView(forgotButton, forgotParams);

        LinearLayout action = new LinearLayout(this);
        action.setGravity(Gravity.CENTER);
        loginButton = Ui.primaryButton(this, "Sign in securely");
        action.addView(loginButton, new LinearLayout.LayoutParams(0, dp(54), 1));
        progress = new ProgressBar(this);
        progress.setVisibility(View.GONE);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(dp(32), dp(32));
        progressParams.setMargins(dp(12), 0, 0, 0);
        action.addView(progress, progressParams);
        form.addView(action, new LinearLayout.LayoutParams(-1, dp(56)));
        loginButton.setOnClickListener(v -> login(session));

        TextView accountHint = label("New to StayFlow?", 14, Color.rgb(174, 177, 185), false);
        accountHint.setGravity(Gravity.CENTER);
        accountHint.setPadding(0, dp(20), 0, dp(4));
        form.addView(accountHint);

        MaterialButton signupButton = new MaterialButton(this);
        signupButton.setText("Create customer account");
        signupButton.setTextSize(15);
        signupButton.setTextColor(Color.rgb(255, 77, 95));
        signupButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
        signupButton.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.rgb(239, 35, 60)));
        signupButton.setStrokeWidth(dp(1));
        signupButton.setCornerRadius(dp(16));
        signupButton.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        form.addView(signupButton, new LinearLayout.LayoutParams(-1, dp(52)));

        page.addView(card, new LinearLayout.LayoutParams(-1, -2));
        TextView security = label("Customer access only  •  Secure session", 13,
                Color.rgb(148, 151, 160), false);
        security.setPadding(0, dp(22), 0, 0);
        page.addView(security);
        setContentView(scroll);
    }

    private void login(SessionManager session) {
        emailLayout.setError(null);
        passwordLayout.setError(null);
        String email = String.valueOf(emailField.getText()).trim().toLowerCase();
        String password = String.valueOf(passwordField.getText());

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            emailField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            passwordField.requestFocus();
            return;
        }

        setLoading(true);
        ApiClient.create(this).login(new LoginRequest(email, password))
                .enqueue(new Callback<Resp<LoginData>>() {
                    @Override
                    public void onResponse(@NonNull Call<Resp<LoginData>> call,
                                           @NonNull Response<Resp<LoginData>> response) {
                        setLoading(false);
                        Resp<LoginData> result = response.body();
                        if (response.isSuccessful() && result != null
                                && "success".equalsIgnoreCase(result.status)
                                && result.data != null) {
                            if (!"CUSTOMER".equalsIgnoreCase(result.data.role)) {
                                Ui.toast(LoginActivity.this, "Please use a customer account");
                                return;
                            }
                            session.save(result.data.token, result.data.email);
                            openDashboard();
                            return;
                        }
                        Ui.toast(LoginActivity.this,
                                result != null && result.message != null
                                        ? result.message : "Invalid email or password");
                    }

                    @Override
                    public void onFailure(@NonNull Call<Resp<LoginData>> call,
                                          @NonNull Throwable error) {
                        setLoading(false);
                        Ui.toast(LoginActivity.this,
                                "Cannot connect to server. Check Wi-Fi and backend URL.");
                    }
                });
    }

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        loginButton.setText(loading ? "Signing in…" : "Sign in securely");
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void openDashboard() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private TextInputLayout inputLayout(String hint) {
        TextInputLayout layout = new TextInputLayout(this);
        layout.setHint(hint);
        layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layout.setBoxCornerRadii(dp(14), dp(14), dp(14), dp(14));
        layout.setBoxStrokeColor(Color.rgb(239, 35, 60));
        return layout;
    }

    private LinearLayout.LayoutParams fieldParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(14));
        return params;
    }

    private TextView label(String value, int size, int color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        if (bold) text.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return text;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
