package com.salatart.memeticame.Managers;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.ImageButton;

/**
 * Created by sasalatart on 11/7/16.
 */

public class MediaPlayerManager {
    private Context mContext;
    private Uri mAudioUri;
    private MediaPlayer mMediaPlayer;
    private ImageButton mPlayButton;
    private ImageButton mPauseButton;
    private ImageButton mStopButton;

    public MediaPlayerManager(Context context, Uri audioUri, ImageButton playButton, ImageButton pauseButton, ImageButton stopButton) {
        mContext = context;

        mPlayButton = playButton;
        mPauseButton = pauseButton;
        mStopButton = stopButton;

        mAudioUri = audioUri;
        setMediaPlayer();
    }

    public void setMediaPlayer() {
        mMediaPlayer = MediaPlayer.create(mContext, mAudioUri);

        setEnabled(true, false, false);
        setColors(Color.BLACK, Color.BLACK, Color.BLACK);

        if (mMediaPlayer == null) return;

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                setMediaPlayer();
            }
        });
    }

    public void onPlay() {
        if (mMediaPlayer == null) {
            setMediaPlayer();
        } else {
            setEnabled(false, true, true);
            setColors(Color.RED, Color.BLACK, Color.BLACK);
            mMediaPlayer.start();
        }
    }

    public void onPause() {
        setEnabled(true, false, true);
        setColors(Color.BLACK, Color.RED, Color.BLACK);
        mMediaPlayer.pause();
    }

    public void onStop() {
        setEnabled(true, false, false);
        mMediaPlayer.stop();
        mMediaPlayer.release();
        setMediaPlayer();
    }

    public void setEnabled(boolean playButtonEnabled, boolean pauseButtonEnabled, boolean stopButtonEnabled) {
        mPlayButton.setEnabled(playButtonEnabled);
        mPauseButton.setEnabled(pauseButtonEnabled);
        mStopButton.setEnabled(stopButtonEnabled);
    }

    public void setColors(int playColor, int pauseColor, int stopColor) {
        mPlayButton.setColorFilter(playColor);
        mPauseButton.setColorFilter(pauseColor);
        mStopButton.setColorFilter(stopColor);
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }
}
