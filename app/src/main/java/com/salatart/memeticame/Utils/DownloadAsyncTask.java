package com.salatart.memeticame.Utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.salatart.memeticame.Listeners.OnRequestListener;

/**
 * Created by sasalatart on 11/16/16.
 */

public class DownloadAsyncTask extends AsyncTask {

    private Activity mActivity;
    private ProgressDialog mProgressDialog;
    private long mDownloadId;
    private OnRequestListener mListener;

    public DownloadAsyncTask(Activity activity, ProgressDialog progressDialog, long downloadId, OnRequestListener listener) {
        mActivity = activity;
        mProgressDialog = progressDialog;
        mDownloadId = downloadId;
        mListener = listener;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        while (updateProgressBar()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public boolean updateProgressBar() {
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(mDownloadId);
        try {
            DownloadManager downloadManager = (DownloadManager) mActivity.getSystemService(Context.DOWNLOAD_SERVICE);
            Cursor cursor = downloadManager.query(q);
            cursor.moveToFirst();

            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            int downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            int total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            float progress = (status == DownloadManager.STATUS_SUCCESSFUL ? 1 : (float) downloaded / (float) total);

            cursor.close();

            if (status == DownloadManager.STATUS_FAILED || status == DownloadManager.STATUS_PAUSED) {
                mProgressDialog.dismiss();
                mListener.OnFailure("Failed to download file. Try again.");
            } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                mProgressDialog.dismiss();
                mListener.OnSuccess();
            } else {
                mProgressDialog.setProgress((int) progress * 100);
            }

            return status != DownloadManager.STATUS_FAILED && status != DownloadManager.STATUS_PAUSED && status != DownloadManager.STATUS_SUCCESSFUL;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
