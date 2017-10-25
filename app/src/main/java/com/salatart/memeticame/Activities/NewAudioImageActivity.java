package com.salatart.memeticame.Activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.FilterUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewAudioImageActivity extends AppCompatActivity {
    public static final String AUDIO_STATE = "audioState";
    public static final String IMAGE_STATE = "imageState";


    @BindView(R.id.audio_image_name) EditText mAudioImageName;
    @BindView(R.id.image_picker) Button mImagePicker;
    @BindView(R.id.audio_picker) Button mAudioPicker;
    @BindView(R.id.submit_button) Button mSubmitButton;

    private Uri mCurrentImageUri;
    private Uri mCurrentAudioUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_audio_image);


        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            mCurrentAudioUri = savedInstanceState.getParcelable(AUDIO_STATE);
            mCurrentImageUri= savedInstanceState.getParcelable(IMAGE_STATE);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(AUDIO_STATE, mCurrentAudioUri);
        savedInstanceState.putParcelable(IMAGE_STATE, mCurrentImageUri);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void selectAudioFromDevice(View view) {
        startActivityForResult(FileUtils.getSelectFileIntent("audio/*"), FilterUtils.REQUEST_PICK_AUDIO);
    }

    public void selectImageFromDevice(View view) {
        startActivityForResult(FileUtils.getSelectFileIntent("image/*"), FilterUtils.REQUEST_PICK_IMAGE);
    }


    public void submitAudioImage(View view) {
        final String name = mAudioImageName.getText().toString();
        Uri zipFile = FileUtils.zipAudioImage(getApplicationContext(), mCurrentAudioUri, mCurrentImageUri, name);

        Intent returnIntent = new Intent();

        if (zipFile == Uri.EMPTY) {
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
            return;
        }

        returnIntent.putExtra("zipFile", zipFile.toString());
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FilterUtils.REQUEST_PICK_AUDIO && resultCode == RESULT_OK && data != null) {
            mCurrentAudioUri = data.getData();
        } else if (requestCode == FilterUtils.REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            mCurrentImageUri = data.getData();
        }
    }
}
