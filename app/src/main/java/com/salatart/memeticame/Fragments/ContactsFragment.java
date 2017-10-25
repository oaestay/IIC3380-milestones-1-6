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
import android.widget.AdapterView;
import android.widget.ListView;

import com.salatart.memeticame.Listeners.OnContactsReadListener;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.CallbackUtils;
import com.salatart.memeticame.Utils.ContactsUtils;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Views.ContactsAdapter;

import java.util.ArrayList;

public class ContactsFragment extends Fragment {
    private ArrayList<User> mContacts = new ArrayList<>();
    private ContactsAdapter mAdapter;

    private OnContactSelected mContactSelectedListener;

    private ListView mContactsListView;
    private com.wang.avi.AVLoadingIndicatorView mLoadingContacts;

    private BroadcastReceiver mUsersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.notifyDataSetChanged();
        }
    };

    public ContactsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        mContactsListView = (ListView) view.findViewById(R.id.contacts_list_view);
        mLoadingContacts = (com.wang.avi.AVLoadingIndicatorView) view.findViewById(R.id.loading_contacts);
        setContacts();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mContactSelectedListener = (OnContactSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onViewSelected");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mUsersReceiver, new IntentFilter(FilterUtils.NEW_USER_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mUsersReceiver);
    }

    public void setContacts() {
        mContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mContactSelectedListener.onContactSelected(mContacts.get(position));
            }
        });

        mLoadingContacts.show();
        ContactsUtils.retrieveContacts(getActivity(), new OnContactsReadListener() {
            @Override
            public void OnRead(ArrayList<User> intersectedContacts, ArrayList<User> localContacts) {
                mContacts = intersectedContacts;
                setAdapter();
            }

            @Override
            public void OnFailure(String message) {
                CallbackUtils.onUnsuccessfulRequestWithSpinner(getActivity(), message, mLoadingContacts);
            }
        });
    }

    public void setAdapter() {
        final Activity activity = getActivity();
        if (activity == null) return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter = new ContactsAdapter(activity, R.layout.list_item_contact, mContacts);
                mContactsListView.setAdapter(mAdapter);
                mLoadingContacts.hide();
            }
        });
    }

    public interface OnContactSelected {
        void onContactSelected(User user);
    }
}
