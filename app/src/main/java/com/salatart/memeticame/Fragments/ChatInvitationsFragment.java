package com.salatart.memeticame.Fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.salatart.memeticame.Listeners.OnRequestIndexListener;
import com.salatart.memeticame.Models.ChatInvitation;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.CallbackUtils;
import com.salatart.memeticame.Utils.ChatInvitationsUtils;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Utils.SessionUtils;
import com.salatart.memeticame.Views.ChatInvitationsAdapter;

import java.util.ArrayList;

import okhttp3.Request;

/**
 * Created by sasalatart on 10/15/16.
 */

public class ChatInvitationsFragment extends Fragment {

    private ArrayList<ChatInvitation> mChatInvitations;
    private ChatInvitationsAdapter mAdapter;

    private ListView mChatInvitationsListView;
    private com.wang.avi.AVLoadingIndicatorView mLoading;

    private BroadcastReceiver mChatInvitationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<ChatInvitation> chatInvitations = intent.getParcelableArrayListExtra(ChatInvitation.PARCELABLE_KEY_ARRAY_LIST);
            ChatInvitation myChatInvitation = ChatInvitation.includesUser(chatInvitations, SessionUtils.getPhoneNumber(getContext()));
            if (myChatInvitation != null) {
                mChatInvitations.add(myChatInvitation);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private BroadcastReceiver mAdapterResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.notifyDataSetChanged();
        }
    };

    public ChatInvitationsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chat_invitations, container, false);

        mChatInvitationsListView = (ListView) view.findViewById(R.id.list_view_chat_invitations);
        mLoading = (com.wang.avi.AVLoadingIndicatorView) view.findViewById(R.id.loading_chat_invitations);

        setChatInvitations();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mChatInvitationsReceiver, new IntentFilter(FilterUtils.NEW_CHAT_INVITATION_FILTER));
        getActivity().registerReceiver(mAdapterResponseReceiver, new IntentFilter(FilterUtils.CHAT_INVITATIONS_ADAPTER_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mChatInvitationsReceiver);
        getActivity().unregisterReceiver(mAdapterResponseReceiver);
    }

    public void setChatInvitations() {
        mLoading.show();
        Request request = Routes.chatInvitationsIndex(getActivity());
        ChatInvitationsUtils.indexRequest(request, new OnRequestIndexListener<ChatInvitation>() {
            @Override
            public void OnSuccess(ArrayList<ChatInvitation> chatInvitations) {
                final Activity activity = getActivity();
                if (activity == null) return;

                mChatInvitations = chatInvitations;
                mAdapter = new ChatInvitationsAdapter(activity, R.layout.list_item_chat_invitation, mChatInvitations);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChatInvitationsListView.setAdapter(mAdapter);
                        mLoading.hide();
                    }
                });
            }

            @Override
            public void OnFailure(String message) {
                CallbackUtils.onUnsuccessfulRequestWithSpinner(getActivity(), message, mLoading);
            }
        });
    }
}
