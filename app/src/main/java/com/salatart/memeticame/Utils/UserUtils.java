package com.salatart.memeticame.Utils;

import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 10/19/16.
 */

public class UserUtils {
    public static void signup(Request request, final OnRequestShowListener<String> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure("Failed to signup");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(new JSONObject(response.body().string()).getString("api_key"));
                    } catch (Exception e) {
                        listener.OnFailure("Error");
                    }
                } else {
                    listener.OnSuccess(HttpClient.parseErrorMessage(response));
                }
                response.body().close();
            }
        });
    }

    public static void showRequest(Request request, final OnRequestShowListener<User> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure("Error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.userFromJson(new JSONObject(response.body().string())));
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

    public static User getUserDifference(ArrayList<User> biggerList, ArrayList<User> smallerList) {
        for (User bigUser : biggerList) {
            boolean notFound = true;
            for (User smallUser : smallerList) {
                if (bigUser.getPhoneNumber().equals(smallUser.getPhoneNumber())) {
                    notFound = false;
                }
            }

            if (notFound) {
                return bigUser;
            }
        }

        return null;
    }
}
