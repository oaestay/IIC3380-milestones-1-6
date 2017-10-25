package com.salatart.memeticame.Models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.salatart.memeticame.Utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by sasalatart on 9/14/16.
 */
public class Attachment implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Attachment createFromParcel(Parcel in) {
            return new Attachment(in);
        }

        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };

    public static String PARCELABLE_KEY = "com.salatart.memeticamea.Models.Attachment";
    public static String IMAGE_URI_KEY = "imageUriKey";
    public static String AUDIO_URI_KEY = "audioUriKey";

    public static int IMAGE_SIZE = 480;
    public static int IMAGE_THUMB_SIZE = 48;

    private String mName;
    private String mMimeType;
    private String mBase64Content;
    private String mUri;
    private long mSize;
    private long mDownloadId;
    private float mProgress;
    private boolean mDirty;

    public Attachment(String name, String mimeType, String base64Content, String uri, long size, float progress, boolean dirty) {
        this.mName = name;
        this.mMimeType = mimeType;
        this.mBase64Content = base64Content;
        this.mUri = uri;
        this.mSize = size;
        this.mProgress = progress;
        this.mDownloadId = 0;
        this.mDirty = dirty;
    }

    public Attachment(Parcel in) {
        this.mName = in.readString();
        this.mMimeType = in.readString();
        this.mBase64Content = in.readString();
        this.mUri = in.readString();
        this.mSize = in.readLong();
        this.mProgress = in.readFloat();
        this.mDownloadId = 0;
        this.mDirty = in.readByte() != 0;
    }

    public Attachment clone() {
        return new Attachment(mName, mMimeType, mBase64Content, mUri, mSize, mProgress, mDirty);
    }

    public String getName() {
        return mName;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public String getBase64Content() {
        return mBase64Content;
    }

    public String getStringUri() {
        return mUri;
    }

    public long getSize() {
        return mSize;
    }

    public int getProgress() {
        return (int) (mProgress * 100);
    }

    public void setProgress(float progress) {
        mDirty = true;
        mProgress = progress;
    }

    public long getDownloadId() {
        return mDownloadId;
    }

    public void setDownloadId(long downloadId) {
        mDownloadId = downloadId;
    }

    public boolean isDirty() {
        return mDirty;
    }

    // Code from: http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    public String getHumanReadableByteCount(boolean si) {
        int unit = si ? 1000 : 1024;
        if (mSize < unit) return mSize + " B";
        int exp = (int) (Math.log(mSize) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", mSize / Math.pow(unit, exp), pre);
    }

    public void setUri(String uri) {
        mUri = uri;
    }

    public String getShowableStringUri(Context context) {
        return getStringUri();
    }

    public boolean isAudio() {
        return mMimeType.contains("audio") && !mMimeType.contains("meme");
    }

    public boolean isVideo() {
        return mMimeType.contains("video");
    }

    public boolean isImage() {
        return mMimeType.contains("image") && !mMimeType.contains("meme");
    }

    public boolean isAudioImage() {
        return mMimeType.contains("memeaudio");
    }

    public boolean exists(Context context) {
        return FileUtils.checkFileExistence(context, mName) && (mProgress == -1 || mProgress == 1);
    }

    public boolean existsFiles(Context context) {
        if (!exists(context) || !isAudioImage()) {
            return false;
        }
        Log.v("existsFiles","Checking if files of " + mName + " exist");
        String zipPath = FileUtils.getPathFromFileName(context, mName) + "/" + mName;
        Log.v("zipPath", zipPath);
        String zipName = mName.substring(0, mName.lastIndexOf("."));
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zipPath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry ze = entries.nextElement();
                String nextName = FileUtils.getMemeticameUnzipsDirectory() + "/" + zipName + "-" + ze.getName();
                Log.v("existsFiles","Checking if file " + nextName + " exists");
                File file = new File(nextName);
                if (!file.exists()) {
                    Log.v("missing",nextName + " doesn't exist");
                    return false;
                }
            }
            return true;

        } catch (IOException e) {
            return false;
        }
    }

    public String getImageName(Context context) {
        final String[] okFileExtensions =  new String[] {"jpg", "png", "jpeg"};
        String zipPath = FileUtils.getPathFromFileName(context, mName) + "/" + mName;
        String zipName = mName.substring(0, mName.lastIndexOf("."));
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zipPath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry ze = entries.nextElement();
                Log.v("Check if image", ze.getName());
                for (String extension : okFileExtensions) {
                    if (ze.getName().endsWith(extension)) {
                        return zipName + "-" + ze.getName();
                    }
                }
            }
            return "";
        } catch (IOException e) {
            return "";
        }
    }

    public String getAudioName(Context context) {
        String zipPath = FileUtils.getPathFromFileName(context, mName) + "/" + mName;
        String zipName = mName.substring(0, mName.lastIndexOf("."));
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zipPath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry ze = null;
            while ((ze = entries.nextElement()) != null) {
                if (ze.getName().endsWith("mp3")) {
                    return zipName + "-" + ze.getName();
                }
            }
            return "";
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mMimeType);
        dest.writeString(mBase64Content);
        dest.writeString(mUri);
        dest.writeLong(mSize);
        dest.writeFloat(mProgress);
        dest.writeByte((byte) (mDirty ? 1 : 0));
    }
}
