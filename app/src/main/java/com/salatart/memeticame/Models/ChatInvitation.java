package com.salatart.memeticame.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.salatart.memeticame.Utils.TimeUtils;

import java.util.ArrayList;

/**
 * Created by sasalatart on 10/15/16.
 */

public class ChatInvitation implements Parcelable {

    public static final Parcelable.Creator<ChatInvitation> CREATOR = new Parcelable.Creator<ChatInvitation>() {
        public ChatInvitation createFromParcel(Parcel in) {
            return new ChatInvitation(in);
        }

        public ChatInvitation[] newArray(int size) {
            return new ChatInvitation[size];
        }
    };

    public static String PARCELABLE_KEY = "com.salatart.memeticamea.Models.ChatInvitation";
    public static String PARCELABLE_KEY_ARRAY_LIST = "com.salatart.memeticamea.Models.ChatInvitationList";
    public static String NOTIFICATION_CLICKED = "chatInvitationNotificationClicked";

    private final int mId;
    private final int mChatId;
    private User mUser;
    private String mChatTitle;
    private String mCreatedAt;

    public ChatInvitation(int id, User user, int chatId, String chatTitle, String createdAt) {
        this.mId = id;
        this.mUser = user;
        this.mChatId = chatId;
        this.mChatTitle = chatTitle;
        this.mCreatedAt = TimeUtils.parseISODate(createdAt);
    }

    public ChatInvitation(Parcel in) {
        this.mId = in.readInt();
        this.mUser = in.readParcelable(User.class.getClassLoader());
        this.mChatId = in.readInt();
        this.mChatTitle = in.readString();
        this.mCreatedAt = in.readString();
    }

    public static ChatInvitation includesUser(ArrayList<ChatInvitation> chatInvitations, String userPhone) {
        for (ChatInvitation chatInvitation : chatInvitations) {
            if (User.comparePhones(chatInvitation.getUser().getPhoneNumber(), userPhone)) {
                return chatInvitation;
            }
        }

        return null;
    }

    public int getId() {
        return mId;
    }

    public User getUser() {
        return mUser;
    }

    public int getChatId() {
        return mChatId;
    }

    public String getChatTitle() {
        return mChatTitle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeParcelable(mUser, flags);
        dest.writeInt(mChatId);
        dest.writeString(mChatTitle);
        dest.writeString(mCreatedAt);
    }
}
