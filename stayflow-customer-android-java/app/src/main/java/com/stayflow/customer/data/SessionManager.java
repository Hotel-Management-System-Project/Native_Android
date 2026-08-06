package com.stayflow.customer.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

public class SessionManager {
    private final SharedPreferences prefs;
    public SessionManager(Context c){prefs=c.getSharedPreferences("stayflow_customer",Context.MODE_PRIVATE);}
    public void save(String token,String email){prefs.edit().putString("token",token).putString("email",email).apply();}
    public String token(){return prefs.getString("token",null);}
    public String email(){return prefs.getString("email","");}
    public boolean loggedIn(){return token()!=null;}
    /** Reads the userId claim already contained in the Spring Boot JWT. */
    public Integer userId(){
        try {
            String[] pieces=token().split("\\.");
            String json=new String(Base64.decode(pieces[1],Base64.URL_SAFE|Base64.NO_WRAP));
            JSONObject payload=new JSONObject(json);
            return payload.has("userId") ? payload.getInt("userId") : null;
        } catch(Exception ignored){ return null; }
    }
    public boolean notificationsEnabled(){return prefs.getBoolean("booking_notifications",true);}
    public void setNotificationsEnabled(boolean value){prefs.edit().putBoolean("booking_notifications",value).apply();}
    public boolean darkMode(){return prefs.getBoolean("dark_mode",false);}
    public void setDarkMode(boolean value){prefs.edit().putBoolean("dark_mode",value).apply();}
    // Sign-out removes authentication while keeping local appearance preferences.
    public void clear(){prefs.edit().remove("token").remove("email").apply();}
}
