package com.salatart.memeticame.Listeners;

import com.salatart.memeticame.Models.User;

import java.util.ArrayList;

/**
 * Created by sasalatart on 10/19/16.
 */

public interface OnContactsReadListener {
    void OnRead(ArrayList<User> intersectedContacts, ArrayList<User> localContacts);

    void OnFailure(String message);
}
