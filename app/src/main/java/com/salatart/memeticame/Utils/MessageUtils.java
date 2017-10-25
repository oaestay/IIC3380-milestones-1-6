package com.salatart.memeticame.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.salatart.memeticame.Listeners.OnSendMessageListener;
import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 10/17/16.
 */

public class MessageUtils {
    private static String PREFERENCES = "MESSAGE";
    private static String MESSAGE_CONTENT = "messageContent";
    private static String MESSAGE_ATTACHMENT_URI = "messageAttachmentUri";
    private static String MESSAGE_ATTACHMENT_IS_AUDIO_IMAGE = "messageAttachmentIsAudioImage";

    public static void sendMessage(Request request, final OnSendMessageListener listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        Message responseMessage = ParserUtils.messageFromJson(new JSONObject(response.body().string()));
                        listener.OnSuccess(responseMessage);
                    } catch (IOException | JSONException e) {
                        listener.OnFailure();
                    }
                } else {
                    listener.OnFailure();
                }
                response.body().close();
            }
        });
    }

    public static void copyMessage(Context context, Message message) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES, 0).edit();
        editor.putString(MESSAGE_CONTENT, message.getContent());

        Attachment attachment = message.getAttachment();
        if (attachment != null && attachment.exists(context)) {
            String uriString = FileUtils.getUriFromFileName(context, attachment.getName()).toString();
            editor.putString(MESSAGE_ATTACHMENT_URI, uriString);
            if (attachment.isAudioImage()) {
                editor.putString(MESSAGE_ATTACHMENT_IS_AUDIO_IMAGE, "TRUE");
            } else {
                editor.putString(MESSAGE_ATTACHMENT_IS_AUDIO_IMAGE, null);
            }

        } else {
            editor.putString(MESSAGE_ATTACHMENT_URI, null);
            editor.putString(MESSAGE_ATTACHMENT_IS_AUDIO_IMAGE, null);
        }

        editor.commit();
    }

    public static String[] retrieveMessage(Context context) {
        String messageContent = context.getSharedPreferences(PREFERENCES, 0).getString(MESSAGE_CONTENT, null);
        String messageAttachmentUri = context.getSharedPreferences(PREFERENCES, 0).getString(MESSAGE_ATTACHMENT_URI, null);
        String messageAttachmentIsAudioImage = context.getSharedPreferences(PREFERENCES, 0).getString(MESSAGE_ATTACHMENT_IS_AUDIO_IMAGE, null);

        String[] messageData = {messageContent, messageAttachmentUri, messageAttachmentIsAudioImage};

        if (messageContent == null && messageAttachmentUri == null && messageAttachmentIsAudioImage == null) {
            return null;
        } else {
            return messageData;
        }
    }
}
