package com.salatart.memeticame.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.salatart.memeticame.Models.Attachment;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by sasalatart on 10/31/16.
 */

public class AttachmentUtils {
    public static ArrayList<Attachment> attachmentsFromDir(Context context, File parentDir) {
        ArrayList<Attachment> attachments = new ArrayList<>();

        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        File[] files = parentDir.listFiles();
        if (files != null) {
            for (File file : files) {
                attachments.add(ParserUtils.attachmentFromUri(context, Uri.fromFile(file)));
            }
        }

        return attachments;
    }
}
