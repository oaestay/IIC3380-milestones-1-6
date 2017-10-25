package com.salatart.memeticame.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.Models.MessageCount;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.TimeUtils;

import java.util.ArrayList;

/**
 * Created by sasalatart on 8/29/16.
 */
public class ChatsAdapter extends ArrayAdapter<Chat> {
    private LayoutInflater mLayoutInflater;

    public ChatsAdapter(Context context, int resource, ArrayList<Chat> chats) {
        super(context, resource, chats);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Chat chat = getItem(position);

        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.list_item_chat, parent, false);
        }

        setTextViews(view, chat);

        return view;
    }

    private void setTextViews(View view, Chat chat) {
        if (chat.isGroup()) {
            ImageView iconView = (ImageView) view.findViewById(R.id.chat_icon);
            iconView.setImageResource(R.drawable.ic_people_black_24dp);
        }

        TextView titleView = (TextView) view.findViewById(R.id.label_title);
        titleView.setText(chat.getTitle());

        TextView adminView = (TextView) view.findViewById(R.id.label_admin);
        adminView.setText(chat.getParticipantsHash().get(chat.getAdmin()));

        TextView createdAtView = (TextView) view.findViewById(R.id.label_created_at);
        createdAtView.setText(TimeUtils.parseISODate(chat.getCreatedAt()));

        TextView lastMessageView = (TextView) view.findViewById(R.id.label_last_message);
        String lastMessageText;
        if (chat.getMessages().size() == 0) {
            lastMessageText = "No messages yet";
        } else {
            Message lastMessage = chat.getMessages().get(chat.getMessages().size() - 1);
            String lastUserName = chat.getParticipantsHash().get(lastMessage.getSenderPhone());
            lastMessageText = lastUserName + ": " + lastMessage.getContent();
            lastMessageView.setText(lastMessageText);
        }
        lastMessageView.setText(lastMessageText);

        String unreadMessages = MessageCount.findOne(chat).getUnreadMessages() + "";
        TextView unreadCountView = (TextView) view.findViewById(R.id.label_unread_count);
        unreadCountView.setText(unreadMessages);
    }
}
