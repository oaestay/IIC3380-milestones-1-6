package com.salatart.memeticame.Activities;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Listeners.OnSendMessageListener;
import com.salatart.memeticame.Managers.AudioRecorderManager;
import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.ChatInvitation;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.Models.MessageCount;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.CallbackUtils;
import com.salatart.memeticame.Utils.ChatUtils;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Utils.MessageUtils;
import com.salatart.memeticame.Utils.ParserUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;
import com.salatart.memeticame.Views.MessagesAdapter;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;

public class ChatActivity extends AppCompatActivity {
    public static final String PICTURE_STATE = "pictureState";
    public static final String VIDEO_STATE = "videoState";

    public static boolean sIsActive = false;

    @BindView(R.id.input_message) EmojiEditText mMessageInput;
    @BindView(R.id.toggle_emojis_button) ImageButton mToggleEmojisButton;
    @BindView(R.id.attachment) ImageView mAttachmentImageView;
    @BindView(R.id.button_cancel_attachment) ImageButton mCancelButton;
    @BindView(R.id.take_audio) ImageButton mRecordButton;
    @BindView(R.id.list_view_messages) ListView mMessagesListView;
    @BindView(R.id.meme_options) ImageButton mMemeOptionsButton;

    private EmojiPopup mEmojiPopup;

    private int mRetryMultiplier = 5;
    private int mMaximumTries = 5;

    private Chat mChat;
    private MessageCount mMessageCount;
    private MessagesAdapter mAdapter;
    private UpdaterAsyncTask mUpdater;
    private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

    private boolean mCurrentlyRecording;
    private AudioRecorderManager mAudioRecorderManager;

    private Attachment mCurrentAttachment;
    private Uri mCurrentImageUri;
    private Uri mCurrentVideoUri;

    private HashMap<String, Integer> pendingUploads = new HashMap<>();

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message newMessage = intent.getParcelableExtra(Message.PARCELABLE_KEY);
            if (mChat.getId() == newMessage.getChatId() && !newMessage.isMine(getApplicationContext())) {
                mChat.getMessages().add(newMessage);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private BroadcastReceiver mUsersKickedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Chat chat = intent.getParcelableExtra(Chat.PARCELABLE_KEY);
            User user = intent.getParcelableExtra(User.PARCELABLE_KEY);
            if (mChat.getId() == chat.getId()) {
                mChat.onUserRemoved(ChatActivity.this, user);
            }
        }
    };

    private BroadcastReceiver mUserAcceptedInvitationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatInvitation chatInvitation = intent.getParcelableExtra(ChatInvitation.PARCELABLE_KEY);
            if (chatInvitation.getChatId() == mChat.getId()) {
                mChat.getParticipants().add(chatInvitation.getUser());
            }
        }
    };

    public static Intent getIntent(Context context, Chat chat) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Chat.PARCELABLE_KEY, chat);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            mCurrentImageUri = savedInstanceState.getParcelable(PICTURE_STATE);
            mCurrentVideoUri = savedInstanceState.getParcelable(VIDEO_STATE);
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle data = getIntent().getExtras();
        mChat = data.getParcelable(Chat.PARCELABLE_KEY);

        mMessageCount = MessageCount.findOne(mChat);
        mMessageCount.update(mChat, mChat.getMessages().size(), 0);

        setTitle(mChat.getTitle());

        setEmojiPopup();

        mAudioRecorderManager = new AudioRecorderManager();
        mCurrentlyRecording = false;
        registerForContextMenu(mMessageInput);
        registerForContextMenu(mMemeOptionsButton);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!SessionUtils.loggedIn(getApplicationContext())) {
            startActivity(new Intent(this, LoginActivity.class));
            ChatActivity.this.finish();
        }

        getChat();

        registerReceiver(mMessageReceiver, new IntentFilter(FilterUtils.NEW_MESSAGE_FILTER));
        registerReceiver(mUsersKickedReceiver, new IntentFilter(FilterUtils.USER_KICKED_FILTER));
        registerReceiver(mUserAcceptedInvitationReceiver, new IntentFilter(FilterUtils.CHAT_INVITATION_ACCEPTED_FILTER));
        sIsActive = true;
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(mMessageReceiver);
        unregisterReceiver(mUsersKickedReceiver);
        unregisterReceiver(mUserAcceptedInvitationReceiver);
        mMessageCount.update(mChat, mChat.getMessages().size(), 0);
        sIsActive = false;

        if (mUpdater != null) {
            mUpdater.stop();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(PICTURE_STATE, mCurrentImageUri);
        savedInstanceState.putParcelable(VIDEO_STATE, mCurrentVideoUri);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        MenuItem addParticipantsItem = menu.findItem(R.id.action_add_participants);
        addParticipantsItem.setVisible(mChat.isGroup());
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_see_participants) {
            Intent intent = new Intent(this, ParticipantsActivity.class);
            intent.putExtra(Chat.PARCELABLE_KEY, mChat);
            startActivity(intent);
        } else if (id == R.id.action_add_participants) {
            Intent intent = new Intent(this, AddParticipantsActivity.class);
            intent.putExtra(Chat.PARCELABLE_KEY, mChat);
            startActivity(intent);
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }

        return true;
    }

    public void getChat() {
        Request request = Routes.chatShow(ChatActivity.this, mChat);
        ChatUtils.showRequest(request, new OnRequestShowListener() {
            @Override
            public void OnSuccess(Object chat) {
                mChat = (Chat) chat;
                setAdapter();
            }

            @Override
            public void OnFailure(String message) {
                CallbackUtils.onUnsuccessfulRequest(ChatActivity.this, message);
            }
        });
    }

    public void onClickSendMessage(View view) {
        final String content = mMessageInput.getText().toString();
        if (content.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Can't send empty messages.", Toast.LENGTH_SHORT).show();
            return;
        }

        final Message message = createFakeMessage(content);
        ChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessageInput.setText("");
            }
        });

        sendMessage(message);
    }

    public void sendMessage(final Message message) {
        Request request = Routes.messagesCreate(getApplicationContext(), message);
        MessageUtils.sendMessage(request, new OnSendMessageListener() {
            @Override
            public void OnSuccess(final Message responseMessage) {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChat.getMessages().remove(message);
                        mChat.getMessages().add(responseMessage);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void OnFailure() {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pendingUploads.containsKey(message.getCreatedAt())) {
                            pendingUploads.put(message.getCreatedAt(), pendingUploads.get(message.getCreatedAt()) + 1);
                        } else {
                            pendingUploads.put(message.getCreatedAt(), 1);
                        }

                        int timesTried = pendingUploads.get(message.getCreatedAt());
                        if (timesTried > mMaximumTries) {
                            Toast.makeText(getApplicationContext(), "Failed " + timesTried + " times. Canceling upload.", Toast.LENGTH_LONG).show();
                            mChat.getMessages().remove(message);
                            mAdapter.notifyDataSetChanged();
                            return;
                        }

                        int seconds = mRetryMultiplier * timesTried;
                        Toast.makeText(getApplicationContext(), "Could not send message (Attempt: " + timesTried + "). Trying again in " + seconds + " seconds.", Toast.LENGTH_LONG).show();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendMessage(message);
                            }
                        }, seconds * 1000);
                    }
                });
            }
        });
    }

    private Message createFakeMessage(String content) {
        Message message = Message.createFake(getApplicationContext(), content, mChat.getId());
        if (mCurrentAttachment != null) {
            message.setAttachment(mCurrentAttachment.clone());
            toggleAttachmentVisibilities(false);
            mCurrentAttachment = null;
        }

        mChat.getMessages().add(message);
        mAdapter.notifyDataSetChanged();
        return message;
    }

    public void cancelAttachment(View view) {
        toggleAttachmentVisibilities(false);
    }

    public void toggleAttachmentVisibilities(boolean show) {
        mCancelButton.setVisibility(show ? View.VISIBLE : View.GONE);
        mAttachmentImageView.setVisibility(show ? View.VISIBLE : View.GONE);

        if (mAttachmentImageView.getVisibility() == View.VISIBLE) {
            if (mCurrentAttachment.isImage() || mCurrentAttachment.isVideo()) {
                Glide.with(getApplicationContext())
                        .load(mCurrentAttachment.getShowableStringUri(ChatActivity.this))
                        .override(Attachment.IMAGE_THUMB_SIZE, Attachment.IMAGE_THUMB_SIZE)
                        .into(mAttachmentImageView);
            } else if (mCurrentAttachment.isAudio()) {
                mAttachmentImageView.setImageResource(R.drawable.ic_record_voice_over_black_24dp);
            } else {
                mAttachmentImageView.setImageResource(R.drawable.ic_insert_drive_file_black_24dp);
            }
        } else {
            mCurrentAttachment = null;
        }
    }

    public void selectFileFromDevice(View view) {
        startActivityForResult(FileUtils.getSelectFileIntent("*/*"), FilterUtils.REQUEST_PICK_FILE);
    }

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = FileUtils.createMediaFile(getApplicationContext(), "jpg", Environment.DIRECTORY_PICTURES);
            if (photoFile != null) {
                mCurrentImageUri = FileProvider.getUriForFile(this, "com.salatart.memeticame.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentImageUri);
                startActivityForResult(takePictureIntent, FilterUtils.REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    public void dispatchTakeVideoIntent(View view) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            File videoFile = FileUtils.createMediaFile(getApplicationContext(), "mp4", Environment.DIRECTORY_PICTURES);
            if (videoFile != null) {
                mCurrentVideoUri = FileProvider.getUriForFile(this, "com.salatart.memeticame.fileprovider", videoFile);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentVideoUri);
                startActivityForResult(takeVideoIntent, FilterUtils.REQUEST_VIDEO_CAPTURE);
            }
        }
    }

    public void toggleRecording(View view) {
        if (mCurrentlyRecording) {
            File audioFile = mAudioRecorderManager.stopAudioRecording();
            Uri uri = mAudioRecorderManager.addRecordingToMediaLibrary(ChatActivity.this, audioFile);
            setCurrentAttachmentFromUri(uri);
            mRecordButton.setColorFilter(Color.BLACK);
        } else {
            mAudioRecorderManager.startAudioRecording(ChatActivity.this);
            mRecordButton.setColorFilter(Color.RED);
        }
        mCurrentlyRecording = !mCurrentlyRecording;
    }

    private void setCurrentAttachmentFromUri(Uri uri) {
        mCurrentAttachment = ParserUtils.attachmentFromUri(ChatActivity.this, uri);

        if (mCurrentAttachment == null) {
            return;
        }

        toggleAttachmentVisibilities(true);
    }

    private void setCurrentAttachmentFromUriWithMimeType(Uri uri, String mimeType) {
        mCurrentAttachment = ParserUtils.attachmentFromUriWithMimeType(ChatActivity.this, uri, mimeType);

        if (mCurrentAttachment == null) {
            return;
        }

        toggleAttachmentVisibilities(true);
    }

    public void setAdapter() {
        ChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter = new MessagesAdapter(ChatActivity.this, R.layout.list_item_message_in, mChat);
                mMessagesListView.setAdapter(mAdapter);
                mMessagesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        mScrollState = scrollState;
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    }
                });

                mUpdater = new UpdaterAsyncTask();
                mUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
            }
        });
    }

    public void dispatchSelectAudioImageIntent(View view) {
        Intent selectAudioImageIntent = new Intent(ChatActivity.this, NewAudioImageActivity.class);
        startActivityForResult(selectAudioImageIntent, FilterUtils.REQUEST_PICK_AUDIO_IMAGE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderIcon(R.drawable.ic_textsms_black_24dp);
        menu.setHeaderTitle("Actions");

        if (view.getId() == R.id.input_message) {
            menu.add(Menu.NONE, 0, 0, "Paste");
            menu.add(Menu.NONE, 1, 1, "Cancel");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int menuItemId = item.getItemId();
        if (menuItemId == 0) {
            String[] messageData = MessageUtils.retrieveMessage(ChatActivity.this);
            if (messageData == null) {
                return false;
            }

            mMessageInput.setText(messageData[0]);
            if (messageData[1] != null) {
                if (messageData[2] == "TRUE") {
                    mCurrentAttachment = ParserUtils.attachmentFromUriWithMimeType(ChatActivity.this, Uri.parse(messageData[1]), "zip/memeaudio");
                } else {
                    mCurrentAttachment = ParserUtils.attachmentFromUri(ChatActivity.this, Uri.parse(messageData[1]));
                }
                toggleAttachmentVisibilities(true);
            }
        }
        return true;
    }

    public void onClickToggleEmojis(View view) {
        if (mEmojiPopup.isShowing()) {
            mToggleEmojisButton.setImageResource(R.drawable.ic_sentiment_very_satisfied_black_24dp);
        } else {
            mToggleEmojisButton.setImageResource(R.drawable.ic_keyboard_black_24dp);
        }

        mEmojiPopup.toggle();
    }

    public void setEmojiPopup() {
        mEmojiPopup = EmojiPopup.Builder
                .fromRootView(findViewById(android.R.id.content))
                .setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
                    @Override
                    public void onKeyboardClose() {
                        mEmojiPopup.dismiss();
                        mToggleEmojisButton.setImageResource(R.drawable.ic_sentiment_very_satisfied_black_24dp);
                    }
                })
                .build(mMessageInput);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FilterUtils.REQUEST_PICK_FILE && resultCode == RESULT_OK && data != null) {
            setCurrentAttachmentFromUri(data.getData());
        } else if (requestCode == FilterUtils.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setCurrentAttachmentFromUri(mCurrentImageUri);
        } else if (requestCode == FilterUtils.REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            setCurrentAttachmentFromUri(mCurrentVideoUri);
        } else if (requestCode == FilterUtils.REQUEST_PICK_AUDIO_IMAGE && resultCode == RESULT_OK) {
            setCurrentAttachmentFromUriWithMimeType(Uri.parse(data.getStringExtra("zipFile")), "zip/memeaudio");
        }
    }

    private class UpdaterAsyncTask extends AsyncTask {

        boolean isRunning = true;

        public void stop() {
            isRunning = false;
        }

        protected void onProgressUpdate() {
            super.onProgressUpdate();

            // Update only when we're not scrolling, and only for visible views
            if (mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                int start = mMessagesListView.getFirstVisiblePosition();
                for (int i = start, j = mMessagesListView.getLastVisiblePosition(); i <= j; i++) {
                    final View view = mMessagesListView.getChildAt(i - start);
                    try {
                        Attachment attachment = ((Message) mMessagesListView.getItemAtPosition(i)).getAttachment();
                        if (attachment != null && attachment.isDirty()) {
                            final int finalI = i;
                            ChatActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mMessagesListView.getAdapter().getView(finalI, view, mMessagesListView); // Tell the adapter to update this view
                                }
                            });
                        }
                    } catch (IndexOutOfBoundsException e) {
                        Log.e("ERROR", e.toString());
                    }
                }
            }
        }

        @Override
        protected Object doInBackground(Object[] params) {
            while (isRunning) {
                updateCurrentAdapterContent();
                onProgressUpdate();

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        private void updateCurrentAdapterContent() {
            List<Message> messages = mChat.getMessages();
            Map<Long, Holder> map = new HashMap<>();

            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterByStatus(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_SUCCESSFUL | DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PENDING);
            try {
                DownloadManager downloadManager = (DownloadManager) ChatActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);
                Cursor cursor = downloadManager.query(q);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
                    String uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI));
                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    int downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    float progress = (status == DownloadManager.STATUS_SUCCESSFUL ? 1 : (float) downloaded / (float) total);

                    map.put(id, new Holder(progress, status));
                }

                cursor.close();

                if (map.size() == 0) {
                    return;
                }

                for (Message message : messages) {
                    final Attachment attachment = message.getAttachment();
                    if (attachment == null || !map.containsKey(attachment.getDownloadId())) {
                        continue;
                    }

                    Holder currentHolder = map.get(attachment.getDownloadId());

                    if (attachment.getProgress() != currentHolder.progress * 100) {
                        attachment.setProgress(currentHolder.progress);
                    }

                    if (currentHolder.status == DownloadManager.STATUS_SUCCESSFUL) {
                        // downloadManager.remove(attachment.getDownloadId());
                    } else if (currentHolder.status == DownloadManager.STATUS_FAILED || currentHolder.status == DownloadManager.STATUS_PAUSED) {
                        downloadManager.remove(attachment.getDownloadId());
                        attachment.setDownloadId(0);
                        attachment.setProgress(-1);

                        ChatActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String toastText = "Error trying to download " + attachment.getName() + ". Try again...";
                                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public class Holder {
            public float progress;
            public int status;

            public Holder(float progress, int status) {
                this.progress = progress;
                this.status = status;
            }
        }
    }
}
