package com.salatart.memeticame.Services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.salatart.memeticame.Utils.SessionUtils;

/**
 * Created by sasalatart on 9/5/16.
 */
public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.i("INFO", "Refreshed token: " + refreshedToken);
        SessionUtils.saveFCMToken(getApplicationContext(), refreshedToken);

        if (SessionUtils.loggedIn(getApplicationContext())) {
            SessionUtils.registerFCMToken(getApplicationContext());
        }
    }
}
