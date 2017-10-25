package com.salatart.memeticame.Views;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;

import java.util.ArrayList;

/**
 * Created by sasalatart on 10/7/16.
 */

public class ContactsSelectAdapter extends ArrayAdapter<User> {
    private ArrayList<User> mContacts;
    private ArrayList<User> mSelectedContacts;
    private ArrayList<User> mDisabledContacts;
    private LayoutInflater mLayoutInflater;

    public ContactsSelectAdapter(Context context, int resource, ArrayList<User> contacts, ArrayList<User> selectedContacts, ArrayList<User> disabledContacts) {
        super(context, resource, contacts);
        mContacts = contacts;
        mSelectedContacts = selectedContacts;
        mDisabledContacts = disabledContacts;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.list_item_contact, parent, false);
        }

        User user = mContacts.get(position);

        TextView nameView = (TextView) view.findViewById(R.id.contact_name);
        nameView.setText(user.getName());

        TextView phoneView = (TextView) view.findViewById(R.id.contact_phone_number);
        phoneView.setText(user.getPhoneNumber());

        String identificationString = user.getName().charAt(0) + "";
        int identificationColor = ColorGenerator.MATERIAL.getColor(user.getName());
        if (User.isPresent(mSelectedContacts, user.getPhoneNumber())) {
            identificationString = "\u2713";
            identificationColor = Color.GRAY;
        }

        if (User.isPresent(mDisabledContacts, user.getPhoneNumber())) {
            nameView.setTextColor(Color.GRAY);
            phoneView.setTextColor(Color.GRAY);
            identificationColor = Color.GRAY;
        } else {
            nameView.setTextColor(Color.BLACK);
            phoneView.setTextColor(Color.BLACK);
        }

        ImageView identificationView = (ImageView) view.findViewById(R.id.label_user_identification);
        TextDrawable userIdentification = TextDrawable.builder()
                .beginConfig()
                .withBorder(5)
                .toUpperCase()
                .endConfig()
                .buildRoundRect(identificationString, identificationColor, 10);
        identificationView.setImageDrawable(userIdentification);

        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        User user = mContacts.get(position);
        return !User.isPresent(mDisabledContacts, user.getPhoneNumber());
    }
}
