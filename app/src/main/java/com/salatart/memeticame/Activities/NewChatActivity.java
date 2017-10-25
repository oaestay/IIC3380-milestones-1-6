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
import android.widget.TextView;
import android.widget.Toast;

import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.CallbackUtils;
import com.salatart.memeticame.Utils.ChatUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;
import com.salatart.memeticame.Views.ChatsAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;

public class NewChatActivity extends AppCompatActivity {
    @BindView(R.id.input_chat_name) EditText mChatNameInput;
    @BindView(R.id.label_existing_chats) TextView mLabelExistingChats;

    private User mUser;

    public static Intent getIntent(Context context, User user, ArrayList<Chat> currentChats) {
        Intent intent = new Intent(context, NewChatActivity.class);
        intent.putExtra(User.PARCELABLE_KEY, user);
        intent.putParcelableArrayListExtra(Chat.PARCELABLE_ARRAY_KEY, currentChats);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("New chat");

        Bundle data = getIntent().getExtras();
        mUser = data.getParcelable(User.PARCELABLE_KEY);
        final ArrayList<Chat> currentChats = data.getParcelableArrayList(Chat.PARCELABLE_ARRAY_KEY);
        setCurrentChats(currentChats);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!SessionUtils.loggedIn(getApplicationContext())) {
            startActivity(new Intent(this, LoginActivity.class));
            NewChatActivity.this.finish();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        return true;
    }

    public void createChat(final View submitButton) {
        String title = this.mChatNameInput.getText().toString();
        if (title.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Title can't be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        submitButton.setEnabled(false);
        Request request = Routes.chatsCreate(getApplicationContext(), title, new ArrayList<>(Arrays.asList(mUser)), false);
        ChatUtils.createRequest(request, new OnRequestShowListener<Chat>() {
            @Override
            public void OnSuccess(Chat chat) {
                startActivity(ChatActivity.getIntent(NewChatActivity.this, chat));
                finish();
            }

            @Override
            public void OnFailure(String message) {
                CallbackUtils.onUnsuccessfulSubmit(NewChatActivity.this, message, submitButton);
            }
        });
    }

    public void setCurrentChats(final ArrayList<Chat> currentChats) {
        ListView currentChatsListView = (ListView) findViewById(R.id.list_view_existing_chats);
        if (currentChats != null && currentChats.size() > 0) {
            ChatsAdapter mAdapter = new ChatsAdapter(getApplicationContext(), R.layout.list_item_chat, currentChats);
            currentChatsListView.setAdapter(mAdapter);
            currentChatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startActivity(ChatActivity.getIntent(getApplicationContext(), currentChats.get(position)));
                }
            });
        } else {
            mLabelExistingChats.setVisibility(View.GONE);
        }
    }
}
