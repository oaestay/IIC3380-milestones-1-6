package com.salatart.memeticame.Models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.salatart.memeticame.BR;

/**
 * Created by sasalatart on 8/27/16.
 */
public class LoginForm extends BaseObservable {
    private String mPhoneNumber;
    private String mPassword;

    public LoginForm(String phoneNumber, String password) {
        this.mPhoneNumber = phoneNumber;
        this.mPassword = password;
    }

    @Bindable
    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.mPhoneNumber = phoneNumber;
        notifyPropertyChanged(BR.phoneNumber);
    }

    @Bindable
    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        this.mPassword = password;
        notifyPropertyChanged(BR.password);
    }
}
