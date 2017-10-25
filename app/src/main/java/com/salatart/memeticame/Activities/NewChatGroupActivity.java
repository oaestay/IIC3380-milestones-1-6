package com.salatart.memeticame.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.salatart.memeticame.Listeners.OnContactsReadListener;
import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.CallbackUtils;
import com.salatart.memeticame.Utils.ChatUtils;
import com.salatart.memeticame.Utils.ContactsUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.ContactsSelectAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;

public class NewChatGroupActivity extends AppCompatActivity {

    @BindView(R.id.group_name_input) EditText mGroupNameInput;
    @BindView(R.id.contacts_list_view) ListView mContactsListView;
    @BindView(R.id.loading_contacts) com.wang.avi.AVLoadingIndicatorView mLoading;

    private ArrayList<User> mContacts = new ArrayList<>();
    private ArrayList<User> mSelectedContacts = new ArrayList<>();
    private ContactsSelectAdapter mAdapter;

    public static Intent getIntent(Context context) {
        return new Intent(context, NewChatGroupActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat_group);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("New Group Chat");

        setContacts();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        return true;
    }

    public void createGroup(final View submitButton) {
        String title = mGroupNameInput.getText().toString();
        if (title.isEmpty()) {
            Toast.makeText(NewChatGroupActivity.this, "You must choose a name before creating a group.", Toast.LENGTH_LONG).show();
            return;
        }

        submitButton.setEnabled(false);
        Request request = Routes.chatsCreate(getApplicationContext(), title, mSelectedContacts, true);
        ChatUtils.createRequest(request, new OnRequestShowListener<Chat>() {
            @Override
            public void OnSuccess(Chat chat) {
                startActivity(ChatActivity.getIntent(NewChatGroupActivity.this, chat));
                finish();
            }

            @Override
            public void OnFailure(String message) {
                CallbackUtils.onUnsuccessfulSubmit(NewChatGroupActivity.this, message, submitButton);
            }
        });
    }

    public void setContacts() {
        mContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedContact = mContacts.get(position);
                if (User.isPresent(mSelectedContacts, selectedContact.getPhoneNumber())) {
                    User.removeFromList(mSelectedContacts, selectedContact);
                } else {
                    mSelectedContacts.add(selectedContact);
                }
                mAdapter.notifyDataSetChanged();
            }
        });

        mLoading.show();
        ContactsUtils.retrieveContacts(NewChatGroupActivity.this, new OnContactsReadListener() {
            @Override
            public void OnRead(ArrayList<User> intersectedContacts, ArrayList<User> localContacts) {
                mContacts = intersectedContacts;
                setAdapter();
            }

            @Override
            public void OnFailure(String message) {
                CallbackUtils.onUnsuccessfulRequestWithSpinner(NewChatGroupActivity.this, message, mLoading);
            }
        });
    }

    public void setAdapter() {
        mAdapter = new ContactsSelectAdapter(NewChatGroupActivity.this, R.layout.list_item_contact, mContacts, mSelectedContacts, new ArrayList<User>());
        NewChatGroupActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                mContactsListView.setAdapter(mAdapter);
                mLoading.hide();
            }
        });
    }
}
