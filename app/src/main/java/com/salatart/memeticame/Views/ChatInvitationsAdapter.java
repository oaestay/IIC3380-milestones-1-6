package com.salatart.memeticame.Views;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.salatart.memeticame.Models.ChatInvitation;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.Routes;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 10/15/16.
 */

public class ChatInvitationsAdapter extends ArrayAdapter<ChatInvitation> {

    private ArrayList<ChatInvitation> mChatInvitations;
    private LayoutInflater mLayoutInflater;

    public ChatInvitationsAdapter(Context context, int resource, ArrayList<ChatInvitation> chatInvitations) {
        super(context, resource, chatInvitations);
        mChatInvitations = chatInvitations;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ChatInvitation chatInvitation = mChatInvitations.get(position);

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_item_chat_invitation, parent, false);
        }

        TextView titleView = (TextView) convertView.findViewById(R.id.label_title);
        titleView.setText(chatInvitation.getChatTitle());

        final Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    mChatInvitations.remove(chatInvitation);
                    getContext().sendBroadcast(new Intent(FilterUtils.CHAT_INVITATIONS_ADAPTER_FILTER));
                } else {
                    Log.e("ERROR", "Could not edit this invitation");
                }
                response.body().close();
            }
        };

        ImageButton rejectButton = (ImageButton) convertView.findViewById(R.id.button_reject_chat_invitation);
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View rejectButton) {
                rejectButton.setEnabled(false);
                Request request = Routes.rejectChatInvitation(getContext(), chatInvitation);
                HttpClient.getInstance().newCall(request).enqueue(callback);
            }
        });

        ImageButton acceptButton = (ImageButton) convertView.findViewById(R.id.button_accept_chat_invitation);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View acceptButton) {
                acceptButton.setEnabled(false);
                Request request = Routes.acceptChatInvitation(getContext(), chatInvitation);
                HttpClient.getInstance().newCall(request).enqueue(callback);
            }
        });

        return convertView;
    }
}
