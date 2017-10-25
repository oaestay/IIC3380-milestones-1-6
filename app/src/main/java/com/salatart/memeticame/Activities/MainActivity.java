package com.salatart.memeticame.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.salatart.memeticame.Fragments.ChatInvitationsFragment;
import com.salatart.memeticame.Fragments.ChatsFragment;
import com.salatart.memeticame.Fragments.ContactsFragment;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.ChatInvitation;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.ContactsUtils;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.SessionUtils;
import com.salatart.memeticame.Views.ViewPagerAdapter;

import java.util.ArrayList;

import static com.salatart.memeticame.Utils.FilterUtils.REQUEST_NEW_CONTACT;

public class MainActivity extends AppCompatActivity implements ChatsFragment.OnChatSelected, ContactsFragment.OnContactSelected {
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 101;
    public static final int PERMISSIONS_REQUEST_MEDIA = 200;

    private ChatsFragment mChatsFragment;
    private ContactsFragment mContactsFragment;
    private ChatInvitationsFragment mChatInvitationsFragment;

    private String[] mMediaPermissions = {"android.permission.RECORD_AUDIO", "android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private String[] mContactsPermissions = {Manifest.permission.READ_CONTACTS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !FileUtils.hasMediaPermissions(MainActivity.this)) {
            requestPermissions(mMediaPermissions, PERMISSIONS_REQUEST_MEDIA);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !ContactsUtils.hasContactsPermissions(MainActivity.this)) {
            requestPermissions(mContactsPermissions, PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        Bundle data = getIntent().getExtras();
        setupViewPager(data);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.basic_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            SessionUtils.logout(MainActivity.this);
        } else if (id == R.id.action_add_contact) {
            startActivityForResult(new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI), REQUEST_NEW_CONTACT);
        } else if (id == R.id.action_create_group_chat) {
            startActivity(NewChatGroupActivity.getIntent(getApplicationContext()));
        } else if (id == R.id.action_see_downloads_gallery) {
            startActivity(new Intent(MainActivity.this, DownloadsGalleryActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!SessionUtils.loggedIn(MainActivity.this)) {
            startActivity(new Intent(this, LoginActivity.class));
            MainActivity.this.finish();
        }
    }

    private void setupViewPager(Bundle data) {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        this.mChatsFragment = new ChatsFragment();
        this.mContactsFragment = new ContactsFragment();
        this.mChatInvitationsFragment = new ChatInvitationsFragment();

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(this.mChatsFragment, "Chats");
        adapter.addFragment(this.mContactsFragment, "Contacts");
        adapter.addFragment(this.mChatInvitationsFragment, "Chat Invitations");

        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        if (data != null && data.containsKey(ChatInvitation.NOTIFICATION_CLICKED)) {
            viewPager.setCurrentItem(2);
        }
    }

    @Override
    public void onContactSelected(User user) {
        startActivity(NewChatActivity.getIntent(MainActivity.this, user, user.findChats(mChatsFragment.getChats())));
    }

    @Override
    public void OnChatSelected(Chat chat) {
        if (FileUtils.hasMediaPermissions(MainActivity.this)) {
            startActivity(ChatActivity.getIntent(getApplicationContext(), chat));
        } else {
            Toast.makeText(MainActivity.this, "You do not have media permissions.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_NEW_CONTACT) {
            ContactsUtils.getContacts(MainActivity.this, new ContactsUtils.ContactsProviderListener() {
                @Override
                public void OnContactsReady(ArrayList<User> localContacts) {
                    ((MemeticameApplication) getApplication()).onContactsAdded(localContacts);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mContactsFragment.setContacts();
                }
            }
        }
    }
}
