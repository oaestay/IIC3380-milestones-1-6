package com.salatart.memeticame.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.salatart.memeticame.Listeners.OnContactsReadListener;
import com.salatart.memeticame.Listeners.OnRequestIndexListener;
import com.salatart.memeticame.Listeners.OnRequestListener;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.ChatInvitation;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.CallbackUtils;
import com.salatart.memeticame.Utils.ChatInvitationsUtils;
import com.salatart.memeticame.Utils.ContactsUtils;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.ContactsSelectAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;

public class AddParticipantsActivity extends AppCompatActivity {

    @BindView(R.id.list_view_users_to_add) ListView mUsersListView;
    @BindView(R.id.loading_participants) com.wang.avi.AVLoadingIndicatorView mLoading;

    private ArrayList<User> mUsers = new ArrayList<>();
    private ArrayList<User> mSelectedUsers = new ArrayList<>();
    private ArrayList<User> mInvitedUsers = new ArrayList<>();
    private ContactsSelectAdapter mAdapter;

    private Chat mChat;

    private BroadcastReceiver mUsersKickedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Chat chat = intent.getParcelableExtra(Chat.PARCELABLE_KEY);
            User user = intent.getParcelableExtra(User.PARCELABLE_KEY);
            if (mChat.getId() == chat.getId() && mChat.onUserRemoved(AddParticipantsActivity.this, user)) {
                mUsers.add(user);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private BroadcastReceiver mNewChatInvitationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<ChatInvitation> chatInvitations = intent.getParcelableArrayListExtra(ChatInvitation.PARCELABLE_KEY_ARRAY_LIST);
            for (ChatInvitation chatInvitation : chatInvitations) {
                if (chatInvitation.getChatId() == mChat.getId()) {
                    mInvitedUsers.add(chatInvitation.getUser());
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    };

    private BroadcastReceiver mChatInvitationAcceptedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatInvitation chatInvitation = intent.getParcelableExtra(ChatInvitation.PARCELABLE_KEY);
            if (chatInvitation.getChatId() == mChat.getId()) {
                mChat.getParticipants().add(chatInvitation.getUser());
                User.removeFromList(mUsers, chatInvitation.getUser());
                User.removeFromList(mSelectedUsers, chatInvitation.getUser());
                User.removeFromList(mInvitedUsers, chatInvitation.getUser());
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private BroadcastReceiver mChatInvitationRejectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatInvitation chatInvitation = intent.getParcelableExtra(ChatInvitation.PARCELABLE_KEY);
            if (chatInvitation.getChatId() == mChat.getId()) {
                User.removeFromList(mInvitedUsers, chatInvitation.getUser());
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_participants);

        ButterKnife.bind(this);

        Bundle data = getIntent().getExtras();
        mChat = data.getParcelable(Chat.PARCELABLE_KEY);

        setParticipants();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("Add users to group");
    }

    @Override
    public void onResume() {
        super.onResume();
        AddParticipantsActivity.this.registerReceiver(mUsersKickedReceiver, new IntentFilter(FilterUtils.USER_KICKED_FILTER));
        AddParticipantsActivity.this.registerReceiver(mNewChatInvitationsReceiver, new IntentFilter(FilterUtils.NEW_CHAT_INVITATION_FILTER));
        AddParticipantsActivity.this.registerReceiver(mChatInvitationAcceptedReceiver, new IntentFilter(FilterUtils.CHAT_INVITATION_ACCEPTED_FILTER));
        AddParticipantsActivity.this.registerReceiver(mChatInvitationRejectedReceiver, new IntentFilter(FilterUtils.CHAT_INVITATION_REJECTED_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        AddParticipantsActivity.this.unregisterReceiver(mUsersKickedReceiver);
        AddParticipantsActivity.this.unregisterReceiver(mNewChatInvitationsReceiver);
        AddParticipantsActivity.this.unregisterReceiver(mChatInvitationAcceptedReceiver);
        AddParticipantsActivity.this.unregisterReceiver(mChatInvitationRejectedReceiver);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(ChatActivity.getIntent(AddParticipantsActivity.this, mChat));
        finish();
        return true;
    }

    public void getInvitations() {
        Request request = Routes.chatInvitationsFromChat(AddParticipantsActivity.this, mChat);
        ChatInvitationsUtils.indexRequest(request, new OnRequestIndexListener<ChatInvitation>() {
            @Override
            public void OnSuccess(ArrayList<ChatInvitation> chatInvitations) {
                for (ChatInvitation chatInvitation : chatInvitations) {
                    mInvitedUsers.add(chatInvitation.getUser());
                }
                setAdapter();
            }

            @Override
            public void OnFailure(String message) {
                CallbackUtils.onUnsuccessfulRequest(AddParticipantsActivity.this, message);
            }
        });
    }

    public void setParticipants() {
        mLoading.show();
        ContactsUtils.retrieveContacts(AddParticipantsActivity.this, new OnContactsReadListener() {
            @Override
            public void OnRead(ArrayList<User> intersectedContacts, ArrayList<User> localContacts) {
                mUsers = User.difference(intersectedContacts, mChat.getParticipants());
                getInvitations();
            }

            @Override
            public void OnFailure(String message) {
                CallbackUtils.onUnsuccessfulRequestWithSpinner(AddParticipantsActivity.this, message, mLoading);
            }
        });
    }

    public void addParticipants(final View submitButton) {
        if (mSelectedUsers.size() == 0) {
            Toast.makeText(AddParticipantsActivity.this, "You must select at least one participant.", Toast.LENGTH_LONG).show();
            return;
        }

        submitButton.setEnabled(false);
        Request request = Routes.inviteUsers(AddParticipantsActivity.this, mChat, mSelectedUsers);
        ChatInvitationsUtils.addParticipantsRequest(request, new OnRequestListener() {
            @Override
            public void OnSuccess() {
                AddParticipantsActivity.this.startActivity(ChatActivity.getIntent(AddParticipantsActivity.this, mChat));
                AddParticipantsActivity.this.finish();
            }

            @Override
            public void OnFailure(String message) {
                CallbackUtils.onUnsuccessfulSubmit(AddParticipantsActivity.this, message, submitButton);
            }
        });
    }

    public void setAdapter() {
        mAdapter = new ContactsSelectAdapter(AddParticipantsActivity.this, R.layout.list_item_contact, mUsers, mSelectedUsers, mInvitedUsers);
        AddParticipantsActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                mUsersListView.setAdapter(mAdapter);
                mUsersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        User selectedContact = mUsers.get(position);
                        if (mSelectedUsers.contains(selectedContact)) {
                            mSelectedUsers.remove(selectedContact);
                        } else {
                            mSelectedUsers.add(selectedContact);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });
                mLoading.hide();
            }
        });
    }
}
