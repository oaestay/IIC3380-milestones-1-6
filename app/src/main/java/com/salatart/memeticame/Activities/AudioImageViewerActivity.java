package com.salatart.memeticame.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.salatart.memeticame.Managers.MediaPlayerManager;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FileUtils;

import butterknife.BindView;

public class AudioImageViewerActivity extends AppCompatActivity {

    private String mImageUri;
    private String mAudioUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_image_viewer);

        Bundle b = getIntent().getExtras();
        mAudioUri = b.getString("audioUri");
        mImageUri= b.getString("imageUri");

        ImageButton mImagePreview = (ImageButton) findViewById(R.id.image_preview);

        Glide.with(this)
                .load(mImageUri)
                .placeholder(R.drawable.ic_image_black_24dp)
                .crossFade()
                .into(mImagePreview);

        setMediaPlayer(Uri.parse(mAudioUri));
    }

    public void openImage(View view){
        Uri uri = Uri.parse(mImageUri);
        Intent i = FileUtils.getOpenFileIntent(uri, FileUtils.getMimeType(this, uri));
        startActivity(i);
    }

    private void setMediaPlayer(final Uri audioUri) {
        final ImageButton playButton = ((ImageButton) findViewById(R.id.button_play));
        final ImageButton pauseButton = ((ImageButton) findViewById(R.id.button_pause));
        final ImageButton stopButton = ((ImageButton) findViewById(R.id.button_stop));

        final MediaPlayerManager mediaPlayerManager = new MediaPlayerManager(this, audioUri, playButton, pauseButton, stopButton);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.onPlay();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.onPause();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.onStop();
            }
        });
    }

}
