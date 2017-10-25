package com.salatart.memeticame.Views;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.salatart.memeticame.Listeners.OnRequestListener;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.ChatUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;

import okhttp3.Request;

/**
 * Created by sasalatart on 10/9/16.
 */

public class ParticipantsAdapter extends ContactsAdapter {
    private Chat mChat;

    public ParticipantsAdapter(Context context, int resource, Chat chat) {
        super(context, resource, chat.getParticipants());
        mChat = chat;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        ImageButton removeButton = (ImageButton) view.findViewById(R.id.button_kick_from_group);

        User user = mChat.getParticipants().get(position);
        String myPhoneNumber = SessionUtils.getPhoneNumber(getContext());

        boolean myself = user.getPhoneNumber().equals(myPhoneNumber);
        boolean notAdmin = !myPhoneNumber.equals(mChat.getAdmin());

        if (myself || notAdmin) {
            removeButton.setVisibility(View.GONE);
        } else {
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final User userToRemove = mChat.getParticipants().get(position);
                    Request request = Routes.kickUser(getContext(), mChat, userToRemove);
                    ChatUtils.kickRequest(request, new OnRequestListener() {
                        @Override
                        public void OnSuccess() {
                            mChat.getParticipants().remove(userToRemove);
                        }

                        @Override
                        public void OnFailure(String message) {
                            Log.e("ERROR", message);
                        }
                    });
                }
            });
        }

        return view;
    }
}
