package com.salatart.memeticame.Utils;

import com.salatart.memeticame.Listeners.OnRequestIndexListener;
import com.salatart.memeticame.Listeners.OnRequestListener;
import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Models.Chat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 10/19/16.
 */

public class ChatUtils {
    public static void indexRequest(Request request, final OnRequestIndexListener<Chat> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.chatsFromJsonArray(new JSONArray(response.body().string())));
                    } catch (JSONException e) {
                        listener.OnFailure(e.toString());
                    }
                } else {
                    listener.OnFailure(HttpClient.parseErrorMessage(response));
                }
            }
        });
    }

    public static void showRequest(Request request, final OnRequestShowListener listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure("Error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.chatFromJson(new JSONObject(response.body().string())));
                    } catch (JSONException e) {
                        listener.OnFailure("Error");
                    }
                } else {
                    listener.OnFailure("Could not retrieve chat");
                }
                response.body().close();
            }
        });
    }

    public static void createRequest(Request request, final OnRequestShowListener listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure("Error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.chatFromJson(new JSONObject(response.body().string())));
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

    public static void leaveRequest(Request request, final OnRequestListener listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure("Error");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    listener.OnSuccess();
                } else {
                    listener.OnFailure(HttpClient.parseErrorMessage(response));
                }
                response.body().close();
            }
        });
    }

    public static void kickRequest(Request request, final OnRequestListener listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure("Error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    listener.OnSuccess();
                } else {
                    listener.OnFailure(HttpClient.parseErrorMessage(response));
                }

                response.body().close();
            }
        });
    }
}
