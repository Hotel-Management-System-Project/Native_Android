package com.stayflow.customer.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.stayflow.customer.data.ApiClient;
import com.stayflow.customer.data.SessionManager;
import com.stayflow.customer.model.Models.PasswordRequest;
import com.stayflow.customer.model.Models.Resp;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Customer account page styled consistently with the React Native profile screen. */
public class ProfileFragment extends Fragment {
    private SessionManager session;
    private boolean dark;
    private int pageColor;
    private int cardColor;
    private int primaryText;
    private int secondaryText;
    private TextInputEditText currentPassword;
    private TextInputEditText newPassword;
    private TextInputEditText confirmPassword;
    private MaterialButton updatePassword;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent,
                             @Nullable Bundle state) {
        session = new SessionManager(requireContext());
        dark = session.darkMode();
        applyPalette();

        ScrollView scroll = new ScrollView(requireContext());
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(pageColor);
        LinearLayout page = vertical();
        page.setBackgroundColor(pageColor);
        page.addView(hero());

        LinearLayout content = vertical();
        content.setPadding(dp(20), dp(22), dp(20), dp(30));
        content.addView(sectionHeading("Account details", "Your StayFlow account information"), bottom(12));
        content.addView(accountDetails(), bottom(14));
        content.addView(notificationCard(), bottom(14));
        content.addView(appearanceCard(), bottom(24));
        content.addView(sectionHeading("Change password", "Keep your account secure"), bottom(12));
        content.addView(passwordCard(), bottom(18));
        content.addView(signOutButton(), bottom(18));
        TextView version = text("StayFlow Customer  \u00B7  Version 1.0.0", 11, secondaryText, false);
        version.setGravity(Gravity.CENTER);
        content.addView(version);
        page.addView(content);
        scroll.addView(page);
        Ui.applySavedTheme(scroll, requireContext());
        return scroll;
    }

    private View hero() {
        LinearLayout hero = vertical();
        hero.setPadding(dp(20), dp(18), dp(20), dp(22));
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF991B1B, 0xFFEF233C, 0xFFFB7185});
        gradient.setCornerRadii(new float[]{0,0,0,0,dp(28),dp(28),dp(28),dp(28)});
        hero.setBackground(gradient);

        LinearLayout heading = horizontal();
        LinearLayout titleCopy = vertical();
        titleCopy.addView(text("YOUR ACCOUNT", 10, 0xFFFECACA, true));
        titleCopy.addView(text("Profile", 30, Color.WHITE, true));
        heading.addView(titleCopy, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView settings = text("\u2699", 26, Color.WHITE, false);
        settings.setGravity(Gravity.CENTER);
        settings.setBackground(rounded(0x2FFFFFFF, 99));
        heading.addView(settings, new LinearLayout.LayoutParams(dp(50), dp(50)));
        hero.addView(heading);
        hero.addView(identityCard(), top(16));
        return hero;
    }

    private View identityCard() {
        MaterialCardView card = plainCard();
        LinearLayout row = horizontal();
        row.setPadding(dp(16), dp(16), dp(16), dp(16));
        String email = session.email().isBlank() ? "customer@stayflow.com" : session.email();
        String displayName = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String initials = displayName.substring(0, Math.min(2, displayName.length())).toUpperCase(Locale.ENGLISH);

        TextView avatar = text(initials, 24, Color.WHITE, true);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(rounded(0xFF18181B, 99));
        row.addView(avatar, new LinearLayout.LayoutParams(dp(70), dp(70)));

        LinearLayout copy = vertical();
        copy.setGravity(Gravity.CENTER_VERTICAL);
        TextView nameView = text(displayName, 21, primaryText, true);
        nameView.setSingleLine(true);
        copy.addView(nameView);
        TextView emailView = text(email, 12, secondaryText, false);
        emailView.setSingleLine(true);
        emailView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        copy.addView(emailView);

        // Use an explicit minimum height so large system fonts cannot clip this badge.
        TextView verified = text("\u2713   VERIFIED CUSTOMER", 11, 0xFF9F1239, true);
        verified.setSingleLine(true);
        verified.setGravity(Gravity.CENTER_VERTICAL);
        verified.setMinHeight(dp(30));
        verified.setPadding(dp(11), 0, dp(11), 0);
        verified.setBackground(rounded(0xFFFFE4E6, 12));
        LinearLayout.LayoutParams verifiedParams = new LinearLayout.LayoutParams(-2, dp(30));
        verifiedParams.setMargins(0, dp(9), 0, 0);
        copy.addView(verified, verifiedParams);
        // WRAP_CONTENT prevents the name, email and verification badge overlapping.
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(0, -2, 1f);
        copyParams.setMargins(dp(14), 0, 0, 0);
        row.addView(copy, copyParams);
        card.addView(row);
        return card;
    }

    private View accountDetails() {
        MaterialCardView card = card();
        LinearLayout rows = vertical();
        rows.setPadding(dp(16), 0, dp(16), 0);
        rows.addView(accountRow("\u2709", "Email address", session.email(), true));
        rows.addView(accountRow("\u263A", "Account type", "Customer", true));
        rows.addView(accountRow("\u2713", "Account status", "Active and verified", false));
        card.addView(rows);
        return card;
    }

    private View accountRow(String iconValue, String title, String value, boolean divider) {
        LinearLayout row = horizontal();
        row.setPadding(0, dp(12), 0, dp(12));
        if (divider) {
            GradientDrawable border = rounded(Color.TRANSPARENT, 0);
            border.setStroke(0, Color.TRANSPARENT);
            row.setBackground(border);
        }
        TextView icon = text(iconValue, 21, 0xFFE11D2E, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(rounded(0xFFFFF1F2, 12));
        row.addView(icon, new LinearLayout.LayoutParams(dp(46), dp(46)));
        LinearLayout copy = vertical();
        copy.setGravity(Gravity.CENTER_VERTICAL);
        copy.addView(text(title.toUpperCase(Locale.ENGLISH), 10, 0xFFA1A1AA, true));
        copy.addView(text(value, 14, primaryText, true));
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(0, dp(46), 1f);
        copyParams.setMargins(dp(12), 0, 0, 0);
        row.addView(copy, copyParams);
        return row;
    }

    private View notificationCard() {
        MaterialCardView card = card();
        LinearLayout row = horizontal();
        row.setPadding(dp(15), dp(14), dp(15), dp(14));
        TextView icon = text("\u2662", 24, 0xFFE11D2E, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(rounded(0xFFFFF1F2, 12));
        row.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout copy = vertical();
        copy.setGravity(Gravity.CENTER_VERTICAL);
        copy.addView(text("Booking notifications", 15, primaryText, true));
        copy.addView(text("Receive trip and reservation updates", 11, secondaryText, false));
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        copyParams.setMargins(dp(12), 0, dp(6), 0);
        row.addView(copy, copyParams);
        SwitchMaterial toggle = new SwitchMaterial(requireContext());
        toggle.setChecked(session.notificationsEnabled());
        toggle.setButtonTintList(ColorStateList.valueOf(0xFFE11D2E));
        toggle.setOnCheckedChangeListener((button, checked) -> session.setNotificationsEnabled(checked));
        row.addView(toggle);
        card.addView(row);
        return card;
    }

    private View appearanceCard() {
        MaterialCardView card = card();
        LinearLayout content = vertical();
        content.setPadding(dp(15), dp(14), dp(15), dp(15));
        LinearLayout heading = horizontal();
        TextView icon = text(dark ? "\u263E" : "\u2600", 22, 0xFFE11D2E, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(rounded(0xFFFFF1F2, 12));
        heading.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout copy = vertical();
        copy.setGravity(Gravity.CENTER_VERTICAL);
        copy.addView(text("App appearance", 15, primaryText, true));
        copy.addView(text("Choose your preferred theme", 11, secondaryText, false));
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        copyParams.setMargins(dp(12), 0, 0, 0);
        heading.addView(copy, copyParams);
        content.addView(heading, bottom(13));

        LinearLayout options = horizontal();
        MaterialButton light = themeButton("\u2600  Light", !dark);
        MaterialButton night = themeButton("\u263E  Dark", dark);
        light.setOnClickListener(v -> changeTheme(false));
        night.setOnClickListener(v -> changeTheme(true));
        options.addView(light, new LinearLayout.LayoutParams(0, dp(46), 1f));
        options.addView(night, new LinearLayout.LayoutParams(0, dp(46), 1f));
        content.addView(options);
        card.addView(content);
        return card;
    }

    private MaterialButton themeButton(String title, boolean selected) {
        MaterialButton button = new MaterialButton(requireContext());
        button.setText(title);
        button.setTextColor(selected ? 0xFF881337 : secondaryText);
        button.setBackgroundTintList(ColorStateList.valueOf(selected ? 0xFFFECDD3 : cardColor));
        button.setStrokeColor(ColorStateList.valueOf(dark ? 0xFF52525B : 0xFFD4D4D8));
        button.setStrokeWidth(dp(1));
        button.setCornerRadius(dp(14));
        return button;
    }

    private void changeTheme(boolean value) {
        if (dark == value) return;
        session.setDarkMode(value);
        requireActivity().recreate();
    }

    private View passwordCard() {
        MaterialCardView card = card();
        LinearLayout content = vertical();
        content.setPadding(dp(15), dp(15), dp(15), dp(15));
        currentPassword = passwordField(content, "Current password");
        newPassword = passwordField(content, "New password");
        confirmPassword = passwordField(content, "Confirm new password");
        updatePassword = new MaterialButton(requireContext());
        updatePassword.setText("Update password");
        updatePassword.setTextColor(Color.WHITE);
        updatePassword.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        updatePassword.setBackgroundTintList(ColorStateList.valueOf(0xFFE11D2E));
        updatePassword.setCornerRadius(dp(14));
        updatePassword.setOnClickListener(v -> changePassword());
        content.addView(updatePassword, new LinearLayout.LayoutParams(-1, dp(52)));
        card.addView(content);
        return card;
    }

    private TextInputEditText passwordField(LinearLayout parent, String hint) {
        TextInputLayout layout = new TextInputLayout(requireContext());
        layout.setHint(hint);
        layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layout.setBoxBackgroundColor(cardColor);
        layout.setBoxStrokeColor(0xFFE11D2E);
        layout.setBoxCornerRadii(dp(12), dp(12), dp(12), dp(12));
        layout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        TextInputEditText input = new TextInputEditText(requireContext());
        input.setTextColor(primaryText);
        input.setHintTextColor(secondaryText);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(input);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(12));
        parent.addView(layout, params);
        return input;
    }

    private void changePassword() {
        String oldValue = value(currentPassword);
        String newValue = value(newPassword);
        String confirmValue = value(confirmPassword);
        if (oldValue.isBlank() || newValue.isBlank() || confirmValue.isBlank()) {
            Ui.toast(requireContext(), "Please complete all password fields"); return;
        }
        if (newValue.length() < 6) {
            Ui.toast(requireContext(), "New password must contain at least 6 characters"); return;
        }
        if (!newValue.equals(confirmValue)) {
            Ui.toast(requireContext(), "New password and confirmation do not match"); return;
        }
        setBusy(true);
        ApiClient.create(requireContext()).changePassword(new PasswordRequest(oldValue, newValue))
                .enqueue(new Callback<Resp<Object>>() {
                    @Override public void onResponse(@NonNull Call<Resp<Object>> call,
                                                     @NonNull Response<Resp<Object>> response) {
                        if (!isAdded()) return;
                        setBusy(false);
                        boolean success = response.isSuccessful() && response.body() != null
                                && "success".equalsIgnoreCase(response.body().status);
                        Ui.toast(requireContext(), success ? "Password changed successfully"
                                : response.body() != null && response.body().message != null
                                ? response.body().message : "Unable to change password");
                        if (success) {
                            currentPassword.setText(""); newPassword.setText(""); confirmPassword.setText("");
                        }
                    }
                    @Override public void onFailure(@NonNull Call<Resp<Object>> call,
                                                    @NonNull Throwable error) {
                        if (!isAdded()) return;
                        setBusy(false); Ui.toast(requireContext(), "Network error");
                    }
                });
    }

    private View signOutButton() {
        MaterialButton button = new MaterialButton(requireContext());
        button.setText("Sign out");
        button.setTextColor(0xFFDC2626);
        button.setBackgroundTintList(ColorStateList.valueOf(pageColor));
        button.setStrokeColor(ColorStateList.valueOf(0xFFFCA5A5));
        button.setStrokeWidth(dp(1));
        button.setCornerRadius(dp(16));
        button.setOnClickListener(v -> confirmSignOut());
        button.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(54)));
        return button;
    }

    private void confirmSignOut() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sign out?")
                .setMessage("You will need to enter your email and password to access StayFlow again.")
                .setNegativeButton("Stay signed in", null)
                .setPositiveButton("Sign out", (dialog, which) -> signOut())
                .show();
    }

    private void signOut() {
        session.clear();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private View sectionHeading(String title, String subtitle) {
        LinearLayout row = horizontal();
        LinearLayout copy = vertical();
        copy.addView(text(title, 22, primaryText, true));
        copy.addView(text(subtitle, 12, secondaryText, false));
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView icon = text("\u25CE", 24, 0xFFE11D2E, true);
        icon.setGravity(Gravity.CENTER);
        row.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));
        return row;
    }

    private MaterialCardView card() {
        MaterialCardView card = plainCard();
        card.setCardElevation(dp(2));
        card.setStrokeColor(dark ? 0xFF3F3F46 : 0xFFE4E4E7);
        card.setStrokeWidth(dp(1));
        return card;
    }

    private MaterialCardView plainCard() {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setRadius(dp(20));
        card.setCardBackgroundColor(cardColor);
        return card;
    }

    private void applyPalette() {
        pageColor = dark ? 0xFF09090B : 0xFFF5F5F5;
        cardColor = dark ? 0xFF18181B : Color.WHITE;
        primaryText = dark ? 0xFFF4F4F5 : 0xFF18181B;
        secondaryText = dark ? 0xFFA1A1AA : 0xFF71717A;
    }

    private void setBusy(boolean busy) {
        updatePassword.setEnabled(!busy);
        updatePassword.setText(busy ? "Updating password..." : "Update password");
    }

    private String value(TextInputEditText input) { return input.getText() == null ? "" : input.getText().toString(); }
    private GradientDrawable rounded(int color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private LinearLayout vertical() { LinearLayout l = new LinearLayout(requireContext()); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout horizontal() { LinearLayout l = new LinearLayout(requireContext()); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    private TextView text(String value, int size, int color, boolean bold) { TextView t = new TextView(requireContext()); t.setText(value); t.setTextSize(size); t.setTextColor(color); if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return t; }
    private LinearLayout.LayoutParams bottom(int margin) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.setMargins(0, 0, 0, dp(margin)); return p; }
    private LinearLayout.LayoutParams top(int margin) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.setMargins(0, dp(margin), 0, 0); return p; }
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
