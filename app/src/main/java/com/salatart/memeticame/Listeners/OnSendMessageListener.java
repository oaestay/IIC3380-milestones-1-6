package com.salatart.memeticame.Listeners;

import com.salatart.memeticame.Models.Message;

/**
 * Created by sasalatart on 10/28/16.
 */

public interface OnSendMessageListener {
    void OnSuccess(Message message);

    void OnFailure();
}
