package com.salatart.memeticame.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.salatart.memeticame.Activities.LoginActivity;
import com.salatart.memeticame.Listeners.OnRequestShowListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 8/27/16.
 */
public class SessionUtils {

    private static String PREFERENCES = "SESSION";

    public static boolean loggedIn(Context context) {
        return context != null && !context.getSharedPreferences(PREFERENCES, 0).getString("Token", "").isEmpty();
    }

    public static String getToken(Context context) {
        if (context == null) return "";

        return context.getSharedPreferences(PREFERENCES, 0).getString("Token", "");
    }

    public static String getPhoneNumber(Context context) {
        return context.getSharedPreferences(PREFERENCES, 0).getString("PhoneNumber", "");
    }

    public static String getFCMToken(Context context) {
        return context.getSharedPreferences(PREFERENCES, 0).getString("FCMToken", "");
    }

    public static void saveToken(Context context, String token) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES, 0).edit();
        editor.putString("Token", token);
        editor.commit();
    }

    public static void savePhoneNumber(Context context, String phoneNumber) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES, 0).edit();
        editor.putString("PhoneNumber", phoneNumber);
        editor.commit();
    }

    public static void saveFCMToken(Context context, String fcmToken) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES, 0).edit();
        editor.putString("FCMToken", fcmToken);
        editor.commit();
    }

    public static void registerFCMToken(Context context) {
        Request request = Routes.fcmRegister(context, getFCMToken(context));
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.body().close();
            }
        });
    }

    public static void login(Request request, final OnRequestShowListener<String> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure("Failed to login");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(new JSONObject(response.body().string()).getString("api_key"));
                    } catch (JSONException e) {
                        listener.OnFailure("Error");
                    }
                } else {
                    listener.OnFailure(HttpClient.parseErrorMessage(response));
                }

                response.body().close();
            }
        });
    }

    public static void logout(final Activity activity) {
        Request request = Routes.logout(activity);
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (activity == null) return;

                SharedPreferences.Editor editor = activity.getSharedPreferences(PREFERENCES, 0).edit();
                editor.remove("Token");
                editor.remove("PhoneNumber");
                editor.commit();

                activity.startActivity(new Intent(activity, LoginActivity.class));
                activity.finish();
                response.body().close();
            }
        });
    }
}
