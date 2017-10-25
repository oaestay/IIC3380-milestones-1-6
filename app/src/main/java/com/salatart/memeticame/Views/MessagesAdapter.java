package com.salatart.memeticame.Views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.salatart.memeticame.Managers.MediaPlayerManager;
import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FileUtils;
import com.salatart.memeticame.Utils.MessageUtils;
import com.vanniktech.emoji.EmojiTextView;

/**
 * Created by sasalatart on 9/4/16.
 */
public class MessagesAdapter extends ArrayAdapter<Message> {
    private Chat mParentChat;
    private LayoutInflater mLayoutInflater;

    public MessagesAdapter(Context context, int resource, Chat parentChat) {
        super(context, resource, parentChat.getMessages());
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mParentChat = parentChat;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Message message = mParentChat.getMessages().get(position);

        if (view == null) {
            if (message.isMine(getContext()) && message.getAttachment() == null) {
                view = mLayoutInflater.inflate(R.layout.list_item_message_out, parent, false);
            } else if (message.isMine(getContext())) {
                view = mLayoutInflater.inflate(R.layout.list_item_attachment_out, parent, false);
            } else if (!message.isMine(getContext()) && message.getAttachment() == null) {
                view = mLayoutInflater.inflate(R.layout.list_item_message_in, parent, false);
            } else if (!message.isMine(getContext())) {
                view = mLayoutInflater.inflate(R.layout.list_item_attachment_in, parent, false);
            }
        }

        setTextViews(view, message);
        setAttachment(view, message);

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);

        if (message.isMine(getContext()) && message.getAttachment() == null) {
            return 0;
        } else if (message.isMine(getContext())) {
            return 1;
        } else if (!message.isMine(getContext()) && message.getAttachment() == null) {
            return 2;
        } else {
            return 3;
        }
    }

    private void setTextViews(View view, Message message) {
        TextView senderLabel = (TextView) view.findViewById(R.id.label_sender);

        ((TextView) view.findViewById(R.id.label_timestamp)).setText(message.getCreatedAt().split(" ")[0]);

        EmojiTextView messageEmojiView = (EmojiTextView) view.findViewById(R.id.message);
        messageEmojiView.setText(message.getContent());

        if (message.isMine(getContext())) {
            senderLabel.setText(R.string.me);

            ImageView statusImageView = (ImageView) view.findViewById(R.id.message_status_check);
            if (message.getId() == -1) {
                statusImageView.setImageResource(R.drawable.ic_access_time_black_24dp);
            } else {
                statusImageView.setImageResource(R.drawable.ic_check_black_24dp);
            }
        } else {
            String sender = mParentChat.getParticipantsHash().get(message.getSenderPhone());
            if (sender == null) {
                sender = message.getSenderPhone();
            }
            senderLabel.setText(sender);
        }
    }

    private void setAttachment(View view, final Message message) {
        final Attachment attachment = message.getAttachment();

        if (attachment == null) {
            return;
        }

        boolean fileExists = attachment.exists(getContext());
        boolean filesExists = attachment.existsFiles(getContext());
        boolean isImage = attachment.isImage();
        boolean isVideo = attachment.isVideo();
        final boolean isAudio = attachment.isAudio();
        final boolean isAudioImage = attachment.isAudioImage();

        if (fileExists) {
            attachment.setUri(FileUtils.getUriFromFileName(getContext(), attachment.getName()).toString());
        }

        ImageView attachmentType = (ImageView) view.findViewById(R.id.label_attachment_type);
        if (isImage) {
            attachmentType.setImageResource(R.drawable.ic_image_black_24dp);
        } else if (isVideo) {
            attachmentType.setImageResource(R.drawable.ic_videocam_black_24dp);
        } else if (isAudio) {
            attachmentType.setImageResource(R.drawable.ic_record_voice_over_black_24dp);
        } else if (isAudioImage) {
            attachmentType.setImageResource(R.drawable.ic_star_black_24dp);
        } else {
            attachmentType.setImageResource(R.drawable.ic_insert_drive_file_black_24dp);
        }

        ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        if (isImage || (fileExists && isVideo)) {

            String uri = attachment.getStringUri();

            Glide.with(getContext())
                    .load(uri)
                    .placeholder(R.drawable.ic_access_time_black_24dp)
                    .crossFade()
                    .override(Attachment.IMAGE_SIZE, Attachment.IMAGE_SIZE)
                    .into(thumbnail);
        } else if (fileExists && isAudioImage && filesExists) {
            String imageName = attachment.getImageName(getContext());
            Log.v("Opening image", imageName);
            String uri = FileUtils.getUriFromFileName(getContext(), imageName).toString();

            Glide.with(getContext())
                    .load(uri)
                    .placeholder(R.drawable.ic_access_time_black_24dp)
                    .crossFade()
                    .override(Attachment.IMAGE_SIZE, Attachment.IMAGE_SIZE)
                    .into(thumbnail);

        } else if (!fileExists) {
            thumbnail.setImageResource(R.drawable.ic_file_download_black_24dp);
        } else if (isAudio) {
            thumbnail.setImageResource(R.drawable.ic_audiotrack_black_24dp);
            setMediaPlayer(view, Uri.parse(attachment.getStringUri()));
        } else {
            thumbnail.setImageResource(R.drawable.ic_attach_file_black_24dp);
        }

        TextView attachmentName = (TextView) view.findViewById(R.id.label_attachment_name);
        attachmentName.setText(attachment.getName());

        TextView attachmentMimeType = (TextView) view.findViewById(R.id.label_attachment_mime_type);
        attachmentMimeType.setText(attachment.getMimeType());

        TextView attachmentSize = (TextView) view.findViewById(R.id.label_attachment_size);
        attachmentSize.setText(attachment.getHumanReadableByteCount(false));

        Button openButton = (Button) view.findViewById(R.id.button_open);
        openButton.setText(!fileExists ? "Download" : ((isAudioImage && !filesExists) ? "Unzip" : "Open"));
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!FileUtils.openFile(getContext(), attachment)) {
                    Toast.makeText(getContext(), "Can't open this file.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton copyButton = ((ImageButton) view.findViewById(R.id.button_copy));
        copyButton.setVisibility(fileExists ? View.VISIBLE : View.GONE);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageUtils.copyMessage(getContext(), message);
                Toast.makeText(getContext(), "Message copied", Toast.LENGTH_LONG).show();
            }
        });

        LinearLayout groupAudioButtons = (LinearLayout) view.findViewById(R.id.group_audio_buttons);
        groupAudioButtons.setVisibility((isAudio && fileExists) ? View.VISIBLE : View.GONE);
        openButton.setVisibility((isAudio && fileExists) ? View.GONE : View.VISIBLE);

        LinearLayout groupProgress = (LinearLayout) view.findViewById(R.id.group_progress);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        TextView progressLabel = (TextView) view.findViewById(R.id.label_progress);
        if (attachment.getProgress() >= 0 && attachment.getProgress() <= 100) {
            groupProgress.setVisibility(View.VISIBLE);
            progressBar.setProgress(attachment.getProgress());
            String progress = attachment.getProgress() + "%";
            progressLabel.setText(progress);
        } else {
            groupProgress.setVisibility(View.GONE);
        }

        if (attachment.getProgress() >= 0 && attachment.getProgress() < 100) {
            openButton.setEnabled(false);
        } else {
            openButton.setEnabled(true);
        }
    }

    private void setMediaPlayer(final View view, final Uri audioUri) {
        final ImageButton playButton = ((ImageButton) view.findViewById(R.id.button_play));
        final ImageButton pauseButton = ((ImageButton) view.findViewById(R.id.button_pause));
        final ImageButton stopButton = ((ImageButton) view.findViewById(R.id.button_stop));

        final MediaPlayerManager mediaPlayerManager = new MediaPlayerManager(getContext(), audioUri, playButton, pauseButton, stopButton);

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
