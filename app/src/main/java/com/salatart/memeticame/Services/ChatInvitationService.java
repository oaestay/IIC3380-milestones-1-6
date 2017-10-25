package com.salatart.memeticame.Services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import com.salatart.memeticame.Models.ChatInvitation;
import com.salatart.memeticame.Utils.ChatInvitationsUtils;
import com.salatart.memeticame.Utils.Routes;

import okhttp3.Request;

public class ChatInvitationService extends IntentService {
    private static final String ACTION_ACCEPT_OR_REJECT = "com.salatart.memeticame.Services.action.ACCEPT";

    private static final String CHAT_INVITATION_ACCEPTED = "chatInvitationAccepted";

    public ChatInvitationService() {
        super("ChatInvitationService");
    }

    public static Intent getActionAcceptOrReject(Context context, ChatInvitation chatInvitation, boolean accepted) {
        Intent intent = new Intent(context, ChatInvitationService.class);
        intent.setAction(ACTION_ACCEPT_OR_REJECT);
        intent.putExtra(ChatInvitation.PARCELABLE_KEY, chatInvitation);
        intent.putExtra(CHAT_INVITATION_ACCEPTED, accepted);
        return intent;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ACCEPT_OR_REJECT.equals(action)) {
                final ChatInvitation chatInvitation = intent.getParcelableExtra(ChatInvitation.PARCELABLE_KEY);
                final boolean accepted = intent.getBooleanExtra(CHAT_INVITATION_ACCEPTED, true);
                handleActionAcceptOrReject(chatInvitation, accepted);
            }
        }
    }

    private void handleActionAcceptOrReject(ChatInvitation chatInvitation, boolean accepted) {
        Request request;
        if (accepted) {
            request = Routes.acceptChatInvitation(getApplicationContext(), chatInvitation);
        } else {
            request = Routes.rejectChatInvitation(getApplicationContext(), chatInvitation);
        }

        ChatInvitationsUtils.acceptOrRejectRequest(request);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(chatInvitation.getId());
    }
}
