package com.salatart.memeticame.Models;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by sasalatart on 10/16/16.
 */

public class MessageCount extends RealmObject {

    @PrimaryKey
    private int mChatId;
    private int mMessageCount;
    private int mUnreadMessages;

    public MessageCount() {
    }

    public MessageCount(int chatId, int unreadMessages) {
        this.mChatId = chatId;
        mMessageCount = 0;
        mUnreadMessages = unreadMessages;
    }

    public static MessageCount findOne(Chat chat) {
        Realm realm = Realm.getDefaultInstance();
        MessageCount messageCount = realm.where(MessageCount.class).equalTo("mChatId", chat.getId()).findFirst();

        if (messageCount == null) {
            realm.beginTransaction();
            messageCount = new MessageCount(chat.getId(), chat.getMessages().size());
            realm.copyToRealmOrUpdate(messageCount);
            realm.commitTransaction();
        }
        return messageCount;
    }

    public static void moveOrUpdateAll(ArrayList<Chat> chats) {
        Realm realm = Realm.getDefaultInstance();
        for (Chat chat : chats) {
            MessageCount messageCount = MessageCount.findOne(chat);
            realm.beginTransaction();
            messageCount.setUnreadMessages(chat.getMessages().size() - messageCount.getMessageCount() + messageCount.getUnreadMessages());
            messageCount.setMessageCount(chat.getMessages().size());
            realm.copyToRealmOrUpdate(messageCount);
            realm.commitTransaction();
        }
    }

    public static void addOneUnreadMessage(int chatId) {
        Realm realm = Realm.getDefaultInstance();
        MessageCount messageCount = realm.where(MessageCount.class).equalTo("mChatId", chatId).findFirst();
        if (messageCount != null) {
            realm.beginTransaction();
            messageCount.setUnreadMessages(messageCount.getUnreadMessages() + 1);
            messageCount.setMessageCount(messageCount.getMessageCount() + 1);
            realm.commitTransaction();
        }
    }

    public int getMessageCount() {
        return mMessageCount;
    }

    public void setMessageCount(int messageCount) {
        mMessageCount = messageCount;
    }

    public int getUnreadMessages() {
        return mUnreadMessages;
    }

    public void setUnreadMessages(int unreadMessages) {
        mUnreadMessages = unreadMessages;
    }

    public void update(Chat chat, int numberOfMessages, int numberOfUnreadMessages) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        MessageCount messageCount = MessageCount.findOne(chat);
        messageCount.setMessageCount(numberOfMessages);
        messageCount.setUnreadMessages(numberOfUnreadMessages);
        realm.commitTransaction();
    }
}
