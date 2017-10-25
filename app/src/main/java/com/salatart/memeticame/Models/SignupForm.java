package com.salatart.memeticame.Models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.salatart.memeticame.BR;

/**
 * Created by sasalatart on 8/28/16.
 */
public class SignupForm extends BaseObservable {
    private String mName;
    private String mPhoneNumber;
    private String mPassword;
    private String mPasswordConfirmation;

    public SignupForm(String name, String phoneNumber, String password, String passwordConfirmation) {
        this.mName = name;
        this.mPhoneNumber = phoneNumber;
        this.mPassword = password;
        this.mPasswordConfirmation = passwordConfirmation;
    }

    @Bindable
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
        notifyPropertyChanged(BR.name);
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

    @Bindable
    public String getPasswordConfirmation() {
        return mPasswordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.mPasswordConfirmation = passwordConfirmation;
        notifyPropertyChanged(BR.passwordConfirmation);
    }
}
