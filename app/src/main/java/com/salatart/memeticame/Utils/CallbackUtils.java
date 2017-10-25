package com.salatart.memeticame.Utils;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by sasalatart on 10/22/16.
 */

public class CallbackUtils {
    public static void onUnsuccessfulRequestWithSpinner(final Activity activity, final String message, final com.wang.avi.AVLoadingIndicatorView loading) {
        if (activity == null) return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                loading.hide();
            }
        });
    }

    public static void onUnsuccessfulRequest(final Activity activity, final String message) {
        if (activity == null) return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void onUnsuccessfulSubmit(final Activity activity, final String message, final View submitButton) {
        if (activity == null) return;

        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                submitButton.setEnabled(true);
            }
        });
    }

    public static void onUnsuccessfulSubmitWithSpinner(final Activity activity, final String message, final View submitButton, final com.wang.avi.AVLoadingIndicatorView loading) {
        if (activity == null) return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                submitButton.setEnabled(true);
                loading.hide();
            }
        });
    }
}
