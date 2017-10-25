package com.salatart.memeticame.Listeners;

/**
 * Created by sasalatart on 10/19/16.
 */

public interface OnRequestShowListener<T> {
    void OnSuccess(T object);

    void OnFailure(String message);
}
