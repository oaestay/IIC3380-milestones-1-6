package com.salatart.memeticame.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Button;

/**
 * Created by Oscarin on 18/10/17.
 */

public class UnzipAsyncTask extends AsyncTask {
    private Context mContext;
    private String mZipFile;

    public UnzipAsyncTask(Context context, String zipFile) {
        mContext = context;
        mZipFile = zipFile;
    }
    @Override
    protected Object doInBackground(Object[] objects) {
        FileUtils.unzipAudioImage(mContext, mZipFile);
        return null;
    }
}
