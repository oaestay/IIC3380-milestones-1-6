package com.salatart.memeticame.Models;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.salatart.memeticame.Activities.MainActivity;
import com.salatart.memeticame.Utils.SessionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by sasalatart on 8/29/16.
 */
public class Chat implements Parcelable {
    public static final Parcelable.Creator<Chat> CREATOR = new Parcelable.Creator<Chat>() {
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    public static String PARCELABLE_KEY = "com.salatart.memeticame.Models.Chat";
    public static String PARCELABLE_ARRAY_KEY = "com.salatart.memeticame.Models.ChatArrayList";

    private final int mId;
    private String mTitle;
    private boolean mIsGroup;
    private String mCreatedAt;
    private String mAdmin;
    private ArrayList<User> mParticipants;
    private ArrayList<Message> mMessages;

    public Chat(int id, String title, boolean isGroup, String createdAt, String admin, ArrayList<User> participants, ArrayList<Message> messages) {
        this.mId = id;
        this.mTitle = title;
        this.mIsGroup = isGroup;
        this.mCreatedAt = createdAt;
        this.mAdmin = admin;
        this.mParticipants = participants;
        Collections.sort(messages, new Comparator<Message>(){
            public int compare(Message o1, Message o2)
            {
                return o1.getId() - o2.getId();
            }
        });
        this.mMessages = messages;
    }

    public Chat(Parcel in) {
        this.mId = in.readInt();
        this.mTitle = in.readString();
        this.mIsGroup = in.readByte() != 0;
        this.mCreatedAt = in.readString();
        this.mAdmin = in.readString();
        this.mParticipants = new ArrayList<>();
        in.readTypedList(this.mParticipants, User.CREATOR);
        this.mMessages = new ArrayList<>();
        in.readTypedList(this.mMessages, Message.CREATOR);
    }

    public boolean onUserRemoved(final Activity activity, User user) {
        for (User localUser : mParticipants) {
            if (localUser.getId() == user.getId()) {
                mParticipants.remove(localUser);
                break;
            }
        }

        if (User.comparePhones(user.getPhoneNumber(), SessionUtils.getPhoneNumber(activity))) {
            activity.startActivity(new Intent(activity, MainActivity.class));
            activity.finish();
        }

        return true;
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isGroup() {
        return mIsGroup;
    }

    public String getAdmin() {
        return mAdmin;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public ArrayList<User> getParticipants() {
        return mParticipants;
    }

    public HashMap<String, String> getParticipantsHash() {
        HashMap<String, String> participantsHash = new HashMap<>();
        for (User user : mParticipants) {
            participantsHash.put(user.getPhoneNumber(), user.getName());
        }
        return participantsHash;
    }

    public ArrayList<Message> getMessages() {
        return mMessages;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mTitle);
        dest.writeByte((byte) (mIsGroup ? 1 : 0));
        dest.writeString(mCreatedAt);
        dest.writeString(mAdmin);
        dest.writeTypedList(mParticipants);
        dest.writeTypedList(mMessages);
    }
}
