package com.stayflow.customer.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.stayflow.customer.data.ApiClient;
import com.stayflow.customer.model.Models.Resp;
import com.stayflow.customer.model.Models.SignupRequest;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Customer signup with mandatory email OTP verification before account creation. */
public class SignupActivity extends AppCompatActivity {
    private TextInputLayout nameLayout,emailLayout,phoneLayout,passwordLayout,confirmLayout,otpLayout;
    private TextInputEditText nameField,emailField,phoneField,passwordField,confirmField,otpField;
    private MaterialButton otpButton,verifyButton,signupButton;
    private ProgressBar progress;
    private LinearLayout otpArea;
    private TextView verifiedText;
    private boolean otpSent;
    private boolean otpVerified;
    private boolean otpBusy;
    private String verificationToken="";

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout page=vertical();
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(22),dp(34),dp(22),dp(30));
        page.setBackgroundColor(0xFF090A0D);
        View hero=createHero();
        hero.setAlpha(0f); hero.setTranslationY(-dp(35));
        page.addView(hero);

        LinearLayout form=createForm();
        form.setAlpha(0f); form.setTranslationY(dp(55));
        LinearLayout.LayoutParams formParams=new LinearLayout.LayoutParams(-1,-2);
        page.addView(form,formParams);
        TextView security=text("Customer registration only  \u2022  Secure verification",13,0xFF9497A0,false);
        security.setPadding(0,dp(22),0,0);
        page.addView(security);

        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xFF090A0D);
        scroll.addView(page);
        setContentView(scroll);

        hero.animate().alpha(1f).translationY(0).setDuration(450).withEndAction(() ->
                form.animate().alpha(1f).translationY(0).setDuration(420).start()).start();
    }

    private View createHero() {
        LinearLayout hero=vertical();
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setPadding(0,dp(8),0,dp(26));
        TextView logo=text("S",30,Color.WHITE,true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackgroundResource(com.stayflow.customer.R.drawable.splash_mark);
        logo.getBackground().setTint(0xFFEF233C);
        hero.addView(logo,new LinearLayout.LayoutParams(dp(72),dp(72)));
        TextView brand=text("StayFlow",30,0xFFF8FAFC,true);
        brand.setPadding(0,dp(12),0,0);
        hero.addView(brand);
        TextView tagline=text("Customer mobile portal",15,0xFF9497A0,false);
        tagline.setPadding(0,dp(3),0,0);
        hero.addView(tagline);
        return hero;
    }

    private LinearLayout createForm() {
        LinearLayout form=vertical();
        form.setPadding(dp(22),dp(24),dp(22),dp(24));
        GradientDrawable background=rounded(0xFF15171C,22);
        background.setStroke(dp(1),0xFF30333B);
        form.setBackground(background);
        form.setElevation(dp(3));
        form.addView(text("Create customer account",25,0xFFF8FAFC,true));
        TextView subtitle=text("Register to find rooms and manage your bookings.",15,0xFFAEB1B9,false);
        subtitle.setPadding(0,dp(5),0,dp(20));
        form.addView(subtitle);

        nameLayout=inputLayout("Full name");
        nameField=input(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS,EditorInfo.IME_ACTION_NEXT);
        nameLayout.addView(nameField); form.addView(nameLayout,fieldParams());

        emailLayout=inputLayout("Email address");
        emailField=input(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,EditorInfo.IME_ACTION_NEXT);
        emailLayout.addView(emailField); form.addView(emailLayout,fieldParams());

        otpButton=outlinedButton("Send verification code");
        otpButton.setOnClickListener(v -> {if(otpSent)changeEmail();else sendOtp();});
        form.addView(otpButton,bottomButton());

        otpArea=vertical();
        otpArea.setVisibility(View.GONE);
        otpLayout=inputLayout("Six-digit verification code");
        otpField=input(InputType.TYPE_CLASS_NUMBER,EditorInfo.IME_ACTION_NEXT);
        otpField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        otpLayout.addView(otpField); otpArea.addView(otpLayout,fieldParams());
        verifyButton=outlinedButton("Verify email");
        verifyButton.setBackgroundTintList(ColorStateList.valueOf(0xFFFECACA));
        verifyButton.setTextColor(0xFF7F1D1D);
        verifyButton.setOnClickListener(v -> verifyOtp());
        otpArea.addView(verifyButton,bottomButton());
        form.addView(otpArea);

        verifiedText=text("\u2713  Email address verified",14,0xFF4ADE80,true);
        verifiedText.setGravity(Gravity.CENTER);
        verifiedText.setPadding(0,dp(4),0,dp(14));
        verifiedText.setVisibility(View.GONE);
        form.addView(verifiedText);

        phoneLayout=inputLayout("Phone number");
        phoneField=input(InputType.TYPE_CLASS_PHONE,EditorInfo.IME_ACTION_NEXT);
        phoneLayout.addView(phoneField); form.addView(phoneLayout,fieldParams());

        passwordLayout=inputLayout("Password");
        passwordLayout.setHelperText("Use at least 6 characters");
        passwordLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        passwordField=input(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD,EditorInfo.IME_ACTION_NEXT);
        passwordLayout.addView(passwordField); form.addView(passwordLayout,fieldParams());

        confirmLayout=inputLayout("Confirm password");
        confirmLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        confirmField=input(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD,EditorInfo.IME_ACTION_DONE);
        confirmField.setOnEditorActionListener((v,id,event)->{if(id==EditorInfo.IME_ACTION_DONE){signup();return true;}return false;});
        confirmLayout.addView(confirmField); form.addView(confirmLayout,fieldParams());

        LinearLayout action=horizontal();
        signupButton=new MaterialButton(this);
        signupButton.setText("Create customer account");
        signupButton.setTextColor(Color.WHITE);
        signupButton.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        signupButton.setCornerRadius(dp(14));
        signupButton.setBackgroundTintList(ColorStateList.valueOf(0xFFDC2626));
        signupButton.setEnabled(false);
        signupButton.setOnClickListener(v -> signup());
        action.addView(signupButton,new LinearLayout.LayoutParams(0,dp(54),1f));
        progress=new ProgressBar(this);
        progress.setVisibility(View.GONE);
        LinearLayout.LayoutParams progressParams=new LinearLayout.LayoutParams(dp(34),dp(34));
        progressParams.setMargins(dp(10),0,0,0);
        action.addView(progress,progressParams);
        form.addView(action,bottom(12));

        MaterialButton signIn=new MaterialButton(this);
        TextView accountHint=text("Already registered with StayFlow?",14,0xFFAEB1B9,false);
        accountHint.setGravity(Gravity.CENTER);
        accountHint.setPadding(0,dp(18),0,dp(4));
        form.addView(accountHint);
        signIn.setText("Sign in to your account");
        signIn.setTextColor(0xFFFCA5A5);
        signIn.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        signIn.setOnClickListener(v -> openLogin());
        form.addView(signIn,new LinearLayout.LayoutParams(-1,dp(48)));
        return form;
    }

    private void sendOtp() {
        clearErrors();
        String email=email();
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailLayout.setError("Enter a valid email address first"); emailField.requestFocus(); return;
        }
        setOtpBusy(true);
        Map<String,String> body=new HashMap<>(); body.put("email",email);
        ApiClient.create(this).sendSignupOtp(body).enqueue(new Callback<Resp<Object>>() {
            @Override public void onResponse(@NonNull Call<Resp<Object>> call,@NonNull Response<Resp<Object>> response){
                setOtpBusy(false);
                if(success(response.body())){
                    otpSent=true; otpVerified=false; verificationToken=""; otpField.setText("");
                    emailField.setEnabled(false); otpArea.setVisibility(View.VISIBLE);
                    otpButton.setText("Change email"); signupButton.setEnabled(false);
                    Ui.toast(SignupActivity.this,"Verification code sent. Check your email.");
                }else Ui.toast(SignupActivity.this,message(response.body(),"Verification code could not be sent"));
            }
            @Override public void onFailure(@NonNull Call<Resp<Object>> call,@NonNull Throwable error){setOtpBusy(false);Ui.toast(SignupActivity.this,"Cannot connect to server");}
        });
    }

    private void verifyOtp() {
        String code=value(otpField).trim();
        if(!code.matches("[0-9]{6}")){otpLayout.setError("Enter the six-digit verification code");return;}
        otpLayout.setError(null); setOtpBusy(true);
        Map<String,String> body=new HashMap<>(); body.put("email",email()); body.put("code",code);
        ApiClient.create(this).verifySignupOtp(body).enqueue(new Callback<Resp<Map<String,String>>>() {
            @Override public void onResponse(@NonNull Call<Resp<Map<String,String>>> call,@NonNull Response<Resp<Map<String,String>>> response){
                setOtpBusy(false); Resp<Map<String,String>> result=response.body();
                String token=result==null||result.data==null?null:result.data.get("verificationToken");
                if(success(result)&&token!=null&&!token.isBlank()){
                    verificationToken=token; otpVerified=true; otpArea.setVisibility(View.GONE);
                    verifiedText.setVisibility(View.VISIBLE); otpButton.setEnabled(false); signupButton.setEnabled(true);
                    Ui.toast(SignupActivity.this,"Email verified successfully");
                }else Ui.toast(SignupActivity.this,message(result,"Incorrect verification code"));
            }
            @Override public void onFailure(@NonNull Call<Resp<Map<String,String>>> call,@NonNull Throwable error){setOtpBusy(false);Ui.toast(SignupActivity.this,"Cannot connect to server");}
        });
    }

    private void changeEmail() {
        otpSent=false; otpVerified=false; verificationToken=""; otpField.setText("");
        otpArea.setVisibility(View.GONE); verifiedText.setVisibility(View.GONE);
        emailField.setEnabled(true); emailField.requestFocus(); otpButton.setText("Send verification code");
        signupButton.setEnabled(false);
    }

    private void signup() {
        clearErrors();
        if(!otpVerified||verificationToken.isBlank()){Ui.toast(this,"Verify your email before creating the account");return;}
        String name=value(nameField).trim(),phone=value(phoneField).replace(" ",""),password=value(passwordField),confirm=value(confirmField);
        if(name.length()<2){nameLayout.setError("Enter your full name");return;}
        if(!phone.matches("[0-9]{10}")){phoneLayout.setError("Enter a valid 10-digit mobile number");return;}
        if(password.length()<6){passwordLayout.setError("Password must contain at least 6 characters");return;}
        if(!password.equals(confirm)){confirmLayout.setError("Passwords do not match");return;}

        SignupRequest request=new SignupRequest();
        request.fullName=name; request.email=email(); request.phone=phone; request.password=password;
        request.role="CUSTOMER"; request.emailVerificationToken=verificationToken;
        setLoading(true);
        ApiClient.create(this).signup(request).enqueue(new Callback<Resp<Object>>() {
            @Override public void onResponse(@NonNull Call<Resp<Object>> call,@NonNull Response<Resp<Object>> response){
                setLoading(false); Resp<Object> result=response.body();
                if(success(result)){
                    Ui.toast(SignupActivity.this,"Customer account created successfully. Please sign in.");
                    new Handler(Looper.getMainLooper()).postDelayed(SignupActivity.this::openLogin,900);
                }else Ui.toast(SignupActivity.this,message(result,"Unable to create account"));
            }
            @Override public void onFailure(@NonNull Call<Resp<Object>> call,@NonNull Throwable error){setLoading(false);Ui.toast(SignupActivity.this,"Cannot connect to server. Check Wi-Fi and backend URL.");}
        });
    }

    private void openLogin(){Intent intent=new Intent(this,LoginActivity.class);intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);startActivity(intent);finish();}
    private void setOtpBusy(boolean busy){otpBusy=busy;otpButton.setEnabled(!busy&&!otpVerified);verifyButton.setEnabled(!busy);otpButton.setText(busy?"Please wait...":otpSent?"Change email":"Send verification code");verifyButton.setText(busy?"Verifying...":"Verify email");}
    private void setLoading(boolean busy){signupButton.setEnabled(!busy&&otpVerified);signupButton.setText(busy?"Creating account...":"Create customer account");progress.setVisibility(busy?View.VISIBLE:View.GONE);}
    private void clearErrors(){nameLayout.setError(null);emailLayout.setError(null);phoneLayout.setError(null);passwordLayout.setError(null);confirmLayout.setError(null);if(otpLayout!=null)otpLayout.setError(null);}
    private boolean success(Resp<?> response){return response!=null&&"success".equalsIgnoreCase(response.status);}
    private String message(Resp<?> response,String fallback){return response!=null&&response.message!=null&&!response.message.isBlank()?response.message:fallback;}
    private String email(){return value(emailField).trim().toLowerCase(Locale.ENGLISH);}
    private TextInputLayout inputLayout(String hint){TextInputLayout l=new TextInputLayout(this);l.setHint(hint);l.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);l.setBoxBackgroundColor(0xFF18181B);l.setBoxStrokeColor(0xFFEF4444);l.setHintTextColor(ColorStateList.valueOf(0xFFD4D4D8));l.setBoxCornerRadii(dp(12),dp(12),dp(12),dp(12));return l;}
    private TextInputEditText input(int type,int action){TextInputEditText f=new TextInputEditText(this);f.setSingleLine(true);f.setTextSize(15);f.setTextColor(Color.WHITE);f.setHintTextColor(0xFFA1A1AA);f.setInputType(type);f.setImeOptions(action);return f;}
    private MaterialButton outlinedButton(String title){MaterialButton b=new MaterialButton(this);b.setText(title);b.setTextColor(0xFFFCA5A5);b.setCornerRadius(dp(13));b.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));b.setStrokeColor(ColorStateList.valueOf(0xFFF87171));b.setStrokeWidth(dp(1));return b;}
    private LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private TextView text(String value,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private GradientDrawable rounded(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private LinearLayout.LayoutParams fieldParams(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(13));return p;}
    private LinearLayout.LayoutParams bottomButton(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(50));p.setMargins(0,0,0,dp(13));return p;}
    private LinearLayout.LayoutParams bottom(int margin){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(margin));return p;}
    private String value(TextInputEditText field){return field.getText()==null?"":field.getText().toString();}
    private int dp(float value){return Math.round(value*getResources().getDisplayMetrics().density);}
}
