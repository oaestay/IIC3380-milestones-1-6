package com.salatart.memeticame.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.R;

import java.util.ArrayList;

/**
 * Created by sasalatart on 10/17/16.
 */

public class GalleryAdapter extends ArrayAdapter<Attachment> {
    private ArrayList<Attachment> mAttachments;
    private LayoutInflater mLayoutInflater;

    public GalleryAdapter(Context context, int resource, ArrayList<Attachment> attachments) {
        super(context, resource, attachments);
        mAttachments = attachments;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Attachment attachment = mAttachments.get(position);

        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.list_item_attachment, parent, false);
        }

        setTextView(view, attachment);

        return view;
    }

    public void setTextView(View view, Attachment attachment) {
        ImageView attachmentIcon = (ImageView) view.findViewById(R.id.attachment_icon);
        if (attachment.isImage() || attachment.isVideo()) {
            Glide.with(getContext())
                    .load(attachment.getShowableStringUri(getContext()))
                    .placeholder(R.drawable.ic_access_time_black_24dp)
                    .crossFade()
                    .into(attachmentIcon);
        } else if (attachment.isAudio()) {
            attachmentIcon.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        } else {
            attachmentIcon.setImageResource(R.drawable.ic_insert_drive_file_black_24dp);
        }

        TextView attachmentName = (TextView) view.findViewById(R.id.label_attachment_name);
        attachmentName.setText(attachment.getName());

        TextView attachmentType = (TextView) view.findViewById(R.id.label_attachment_mime_type);
        attachmentType.setText(attachment.getMimeType());

        TextView attachmentSize = (TextView) view.findViewById(R.id.label_attachment_size);
        attachmentSize.setText(attachment.getHumanReadableByteCount(false));
    }
}
