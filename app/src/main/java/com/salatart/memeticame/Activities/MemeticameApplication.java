package com.salatart.memeticame.Activities;

import android.content.Intent;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.UserUtils;

import java.util.ArrayList;

import io.realm.Realm;
import okhttp3.Request;

/**
 * Created by sasalatart on 11/2/16.
 */

public class MemeticameApplication extends MultiDexApplication {

    private ArrayList<User> mUsers;
    private ArrayList<User> mLocalContacts;

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        Stetho.initializeWithDefaults(this);
    }

    public void setUsers(ArrayList<User> users, ArrayList<User> localContacts) {
        mUsers = users;
        mLocalContacts = localContacts;
    }

    public ArrayList<User> getUsers() {
        return mUsers;
    }

    public ArrayList<User> getLocalContacts() {
        return mLocalContacts;
    }

    public void onContactsAdded(final ArrayList<User> localContacts) {
        if (mUsers != null && mLocalContacts != null) {
            User newUser = UserUtils.getUserDifference(localContacts, mLocalContacts);
            if (newUser == null) return;

            Request request = Routes.usersShow(this, newUser);
            UserUtils.showRequest(request, new OnRequestShowListener<User>() {
                @Override
                public void OnSuccess(User user) {
                    mUsers.add(user);
                    mLocalContacts.clear();
                    mLocalContacts.addAll(localContacts);
                    getApplicationContext().sendBroadcast(new Intent(FilterUtils.NEW_USER_FILTER));
                }

                @Override
                public void OnFailure(String message) {
                    Log.e("Error", message);
                }
            });
        }
    }

    public void onAccountCreated(User user) {
        if (mUsers != null && mLocalContacts != null && User.isPresent(mLocalContacts, user.getPhoneNumber())) {
            mUsers.add(user);
            getApplicationContext().sendBroadcast(new Intent(FilterUtils.NEW_USER_FILTER));
        }
    }
}
