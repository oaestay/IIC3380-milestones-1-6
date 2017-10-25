package com.salatart.memeticame.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ListView;

import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.ChatInvitation;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Views.ParticipantsAdapter;

public class ParticipantsActivity extends AppCompatActivity {

    private ParticipantsAdapter mAdapter;
    private Chat mChat;

    private BroadcastReceiver mUsersKickedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Chat chat = intent.getParcelableExtra(Chat.PARCELABLE_KEY);
            User user = intent.getParcelableExtra(User.PARCELABLE_KEY);
            if (mChat.getId() == chat.getId() && mChat.onUserRemoved(ParticipantsActivity.this, user)) {
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private BroadcastReceiver mUserAcceptedInvitation = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatInvitation chatInvitation = intent.getParcelableExtra(ChatInvitation.PARCELABLE_KEY);

            if (chatInvitation.getChatId() == mChat.getId()) {
                mChat.getParticipants().add(chatInvitation.getUser());
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle data = getIntent().getExtras();
        mChat = data.getParcelable(Chat.PARCELABLE_KEY);

        setAdapter();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mUsersKickedReceiver, new IntentFilter(FilterUtils.USER_KICKED_FILTER));
        registerReceiver(mUserAcceptedInvitation, new IntentFilter(FilterUtils.CHAT_INVITATION_ACCEPTED_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsersKickedReceiver);
        unregisterReceiver(mUserAcceptedInvitation);
    }

    public void setAdapter() {
        ListView mContactsListView = (ListView) findViewById(R.id.list_view_participants);
        mAdapter = new ParticipantsAdapter(ParticipantsActivity.this, R.layout.list_item_participant, mChat);
        mContactsListView.setAdapter(mAdapter);
    }
}
