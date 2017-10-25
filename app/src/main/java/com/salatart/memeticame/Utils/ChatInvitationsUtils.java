package com.salatart.memeticame.Utils;

import android.util.Log;

import com.salatart.memeticame.Listeners.OnRequestIndexListener;
import com.salatart.memeticame.Listeners.OnRequestListener;
import com.salatart.memeticame.Models.ChatInvitation;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 10/19/16.
 */

public class ChatInvitationsUtils {
    public static void indexRequest(Request request, final OnRequestIndexListener<ChatInvitation> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure("Error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.chatInvitationsFromJsonArray(new JSONArray(response.body().string())));
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

    public static void addParticipantsRequest(Request request, final OnRequestListener listener) {
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

    public static void acceptOrRejectRequest(Request request) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i("INFO", "ChatInvitation was successfuly updated");
                } else {
                    Log.e("ERROR", HttpClient.parseErrorMessage(response));
                }
                response.body().close();
            }
        });
    }
}
