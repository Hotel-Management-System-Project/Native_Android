package com.stayflow.customer.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
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
import com.stayflow.customer.model.Models.Resp;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Resets a customer password after verifying the OTP sent to the account email. */
public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputLayout emailLayout, otpLayout, passwordLayout, confirmLayout;
    private TextInputEditText emailField, otpField, passwordField, confirmField;
    private LinearLayout resetFields;
    private MaterialButton actionButton;
    private ProgressBar progress;
    private boolean otpSent;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xFF090A0D);

        LinearLayout page = vertical();
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(22), dp(38), dp(22), dp(30));

        TextView logo = text("S", 30, Color.WHITE, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackgroundResource(com.stayflow.customer.R.drawable.splash_mark);
        logo.getBackground().setTint(0xFFEF233C);
        page.addView(logo, new LinearLayout.LayoutParams(dp(72), dp(72)));
        TextView brand = text("StayFlow", 30, 0xFFF8FAFC, true);
        brand.setPadding(0, dp(12), 0, dp(25));
        page.addView(brand);

        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(dp(22)); card.setCardElevation(dp(3));
        card.setCardBackgroundColor(0xFF15171C);
        card.setStrokeColor(0xFF30333B);
        card.setStrokeWidth(dp(1));
        LinearLayout form = vertical();
        form.setPadding(dp(22), dp(24), dp(22), dp(24));
        form.addView(text("Reset password", 25, 0xFFF8FAFC, true));
        TextView subtitle = text("We will send a verification code to your registered email.",
                14, 0xFFAEB1B9, false);
        subtitle.setPadding(0, dp(5), 0, dp(20));
        form.addView(subtitle);

        emailLayout = inputLayout("Email address");
        emailField = input(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailLayout.addView(emailField); form.addView(emailLayout, fieldParams());

        resetFields = vertical();
        otpLayout = inputLayout("Verification code");
        otpField = input(InputType.TYPE_CLASS_NUMBER);
        otpLayout.addView(otpField); resetFields.addView(otpLayout, fieldParams());
        passwordLayout = inputLayout("New password");
        passwordLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        passwordField = input(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordLayout.addView(passwordField); resetFields.addView(passwordLayout, fieldParams());
        confirmLayout = inputLayout("Confirm new password");
        confirmLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        confirmField = input(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmLayout.addView(confirmField); resetFields.addView(confirmLayout, fieldParams());
        resetFields.setVisibility(View.GONE);
        form.addView(resetFields);

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actionButton = Ui.primaryButton(this, "Send verification code");
        actionButton.setOnClickListener(v -> { if (otpSent) resetPassword(); else sendOtp(); });
        actions.addView(actionButton, new LinearLayout.LayoutParams(0, dp(54), 1f));
        progress = new ProgressBar(this); progress.setVisibility(View.GONE);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(dp(30), dp(30));
        pp.setMargins(dp(12), 0, 0, 0); actions.addView(progress, pp);
        form.addView(actions);

        MaterialButton back = new MaterialButton(this);
        back.setText("Back to sign in"); back.setTextColor(0xFFFF7180);
        back.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        back.setOnClickListener(v -> finish());
        form.addView(back, new LinearLayout.LayoutParams(-1, dp(48)));
        card.addView(form); page.addView(card, new LinearLayout.LayoutParams(-1, -2));
        scroll.addView(page); setContentView(scroll);
    }

    private void sendOtp() {
        emailLayout.setError(null);
        String email = value(emailField).trim().toLowerCase();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid registered email"); return;
        }
        setLoading(true, "Sending code...");
        Map<String,String> body = new HashMap<>(); body.put("email", email);
        ApiClient.create(this).sendPasswordResetOtp(body).enqueue(callback(() -> {
            otpSent = true; emailField.setEnabled(false); resetFields.setVisibility(View.VISIBLE);
            actionButton.setText("Reset password");
            Ui.toast(this, "Verification code sent to your email");
        }));
    }

    private void resetPassword() {
        otpLayout.setError(null); passwordLayout.setError(null); confirmLayout.setError(null);
        String otp=value(otpField).trim(), password=value(passwordField), confirm=value(confirmField);
        if (otp.length() < 4) { otpLayout.setError("Enter the verification code"); return; }
        if (password.length() < 6) { passwordLayout.setError("Use at least 6 characters"); return; }
        if (!password.equals(confirm)) { confirmLayout.setError("Passwords do not match"); return; }
        setLoading(true, "Resetting...");
        Map<String,String> body=new HashMap<>();
        body.put("email", value(emailField).trim().toLowerCase());
        body.put("code", otp); body.put("newPassword", password);
        ApiClient.create(this).resetPassword(body).enqueue(callback(() -> {
            Ui.toast(this, "Password reset successfully. Please sign in."); finish();
        }));
    }

    private Callback<Resp<Object>> callback(Runnable success) {
        return new Callback<>() {
            @Override public void onResponse(@NonNull Call<Resp<Object>> call,
                                             @NonNull Response<Resp<Object>> response) {
                setLoading(false, otpSent ? "Reset password" : "Send verification code");
                Resp<Object> result=response.body();
                if (response.isSuccessful() && result!=null && "success".equalsIgnoreCase(result.status)) {
                    success.run(); return;
                }
                Ui.toast(ForgotPasswordActivity.this, result!=null && result.message!=null
                        ? result.message : "Unable to complete request");
            }
            @Override public void onFailure(@NonNull Call<Resp<Object>> call,@NonNull Throwable error) {
                setLoading(false, otpSent ? "Reset password" : "Send verification code");
                Ui.toast(ForgotPasswordActivity.this,"Cannot connect to the backend");
            }
        };
    }

    private void setLoading(boolean loading,String label) {
        actionButton.setEnabled(!loading); actionButton.setText(label);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
    private TextInputLayout inputLayout(String hint){TextInputLayout l=new TextInputLayout(this);l.setHint(hint);l.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);l.setBoxCornerRadii(dp(14),dp(14),dp(14),dp(14));l.setBoxStrokeColor(0xFFEF233C);return l;}
    private TextInputEditText input(int type){TextInputEditText e=new TextInputEditText(this);e.setSingleLine(true);e.setInputType(type);e.setTextColor(0xFFF8FAFC);e.setTextSize(16);return e;}
    private String value(TextInputEditText field){return String.valueOf(field.getText());}
    private LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private TextView text(String value,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private LinearLayout.LayoutParams fieldParams(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(14));return p;}
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
}
