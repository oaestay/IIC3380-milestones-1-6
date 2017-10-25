package com.salatart.memeticame.Listeners;

import java.util.ArrayList;

/**
 * Created by sasalatart on 10/19/16.
 */

public interface OnRequestIndexListener<T> {
    void OnSuccess(ArrayList<T> arrayList);

    void OnFailure(String message);
}
