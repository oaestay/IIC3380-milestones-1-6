package com.salatart.memeticame.Managers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.salatart.memeticame.Utils.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by sasalatart on 9/23/16.
 */

public class AudioRecorderManager {

    private MediaRecorder mAudioRecorder;
    private File mAudioFile;

    public AudioRecorderManager() {
        mAudioRecorder = new MediaRecorder();
    }

    public void startAudioRecording(Context context) {
        mAudioFile = FileUtils.createMediaFile(context, "mp4", Environment.DIRECTORY_MUSIC);

        mAudioRecorder = new MediaRecorder();
        mAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mAudioRecorder.setOutputFile(mAudioFile.getAbsolutePath());

        try {
            mAudioRecorder.prepare();
        } catch (IOException e) {
            Log.e("ERROR", e.toString());
        }

        mAudioRecorder.start();
    }

    public File stopAudioRecording() {
        mAudioRecorder.stop();
        mAudioRecorder.release();
        return mAudioFile;
    }

    public Uri addRecordingToMediaLibrary(Context context, File audioFile) {
        ContentValues values = new ContentValues(4);
        long current = System.currentTimeMillis();
        values.put(MediaStore.Audio.Media.TITLE, "audio" + audioFile.getName());
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4");
        values.put(MediaStore.Audio.Media.DATA, audioFile.getAbsolutePath());
        ContentResolver contentResolver = context.getContentResolver();

        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        return contentResolver.insert(base, values);
    }
}
