package com.salatart.memeticame.Utils;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import com.salatart.memeticame.Activities.AudioImageViewerActivity;
import com.salatart.memeticame.Models.Attachment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by sasalatart on 9/13/16.
 */
public class FileUtils {
    public static String getMemeticameDirectory() {
        return checkAndReturnDir(Environment.getExternalStorageDirectory() + "/Memeticame");
    }

    public static String getMemeticameDownloadsDirectory() {
        return checkAndReturnDir(getMemeticameDirectory() + "/Downloads");
    }

    public static String getMemeticameMemeaudiosDirectory() {
        return checkAndReturnDir(getMemeticameDirectory() + "/Memeaudios");
    }

    public static String getMemeticameMemesDirectory() {
        return checkAndReturnDir(getMemeticameDirectory() + "/Memes");
    }

    public static String getMemeticameUnzipsDirectory() {
        return checkAndReturnDir(getMemeticameDirectory() + "/Unzips");
    }

    public static String getMemeticameTempDirectory() {
        return checkAndReturnDir(getMemeticameDirectory() + "/Temp");
    }

    public static boolean hasMediaPermissions(Context context) {
        boolean canRecordAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean canUseCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean canWriteToStorage = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return canRecordAudio && canUseCamera && canWriteToStorage;
    }

    private static String checkAndReturnDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return path;
    }

    public static Intent getOpenFileIntent(Uri uri, String mimeType) {
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        openIntent.setDataAndType(uri, mimeType);
        return openIntent;
    }

    public static Intent getSelectFileIntent(String type) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(type);
        return intent;
    }

    public static boolean openFile(Context context, Attachment attachment) {
        boolean fileExists = attachment.exists(context);

        if (!fileExists) {
            downloadAttachment(context, attachment);
        } else {
            if (attachment.isAudioImage()){
                if (!attachment.existsFiles(context)) {
                    new UnzipAsyncTask(context, attachment.getName()).execute("");
                } else {
                    Intent intent = new Intent(context, AudioImageViewerActivity.class);
                    Bundle b = new Bundle();
                    b.putString("audioUri", getUriFromFileName(context, attachment.getAudioName(context)).toString());
                    b.putString("imageUri", getUriFromFileName(context, attachment.getImageName(context)).toString());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
                return true;
            }

            try {
                context.startActivity(getOpenFileIntent(Uri.parse(attachment.getStringUri()), attachment.getMimeType()));
            } catch (ActivityNotFoundException e) {
                return false;
            }
        }

        return true;
    }

    public static String getName(Context context, Uri uri) {
        String result = null;

        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }

        return result;
    }

    public static String getMimeType(Context context, Uri uri) {
        String type = null;

        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        if (type == null) {
            ContentResolver cR = context.getContentResolver();
            type = cR.getType(uri);
        }

        if (type != null && type.contains("video")) {
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(context, uri);
            if (metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) == null) {
                type = type.replace("video", "audio");
            }
        }

        return type;
    }

    public static String encodeToBase64FromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        byte[] file = getBytes(inputStream);
        return Base64.encodeToString(file, Base64.NO_WRAP);
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        return byteBuffer.toByteArray();
    }

    public static File createMediaFile(Context context, String extension, String directory) {
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = context.getExternalFilesDir(directory);

        File file = null;
        try {
            file = File.createTempFile(
                    fileName,           /* prefix */
                    "." + extension,    /* suffix */
                    storageDir          /* directory */
            );
        } catch (IOException e) {
            Log.e("ERROR", e.toString());
        }

        return file;
    }

    public static Uri zipAudioImage(Context context, Uri audioFile, Uri imageFile, String zipFileName) {
        try {
            final String audioFilePath = getPath(context, audioFile);
            final String imageFilePath = getPath(context, imageFile);

            BufferedInputStream origin = null;
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(getMemeticameMemeaudiosDirectory() + "/" +  zipFileName + ".zip")));
            try {
                byte data[] = new byte[256];

                Log.v("Adding File", audioFilePath);
                FileInputStream fi = new FileInputStream(audioFilePath);
                origin = new BufferedInputStream(fi, 256);
                try {
                    ZipEntry entry = new ZipEntry("audio" + audioFilePath.substring(audioFilePath.lastIndexOf(".")));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, 256)) != -1) {
                        out.write(data, 0, count);
                    }
                }
                finally {
                    origin.close();
                }

                Log.v("Adding File", imageFilePath);
                fi = new FileInputStream(imageFilePath);
                origin = new BufferedInputStream(fi, 256);
                try {
                    ZipEntry entry = new ZipEntry("image" + imageFilePath.substring(imageFilePath.lastIndexOf(".")));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, 256)) != -1) {
                        out.write(data, 0, count);
                    }
                }
                finally {
                    origin.close();
                }
            }
            finally {
                out.close();

                Log.v("zipPath", getMemeticameMemeaudiosDirectory() + "/" + zipFileName + ".zip");
                Uri zipFile = getUriFromFileName(context, zipFileName + ".zip");
                Log.v("Compress", zipFile.toString());

                return zipFile;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Uri.EMPTY;
    }

    public static void unzipAudioImage(Context context, String _zipFile) {

        try {
            String zipPath = getPathFromFileName(context, _zipFile) + "/" + _zipFile;
            Log.v("Unzipping", zipPath);
            String zipName = _zipFile.substring(0, _zipFile.lastIndexOf("."));
            Log.v("zipName", zipName);
            ArrayList<String> paths = new ArrayList<String>();
            FileInputStream fin = new FileInputStream(zipPath);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                String nextPath = getMemeticameUnzipsDirectory() + "/" + zipName + "-" + ze.getName();
                paths.add(nextPath);
                FileOutputStream fout = new FileOutputStream(nextPath);
                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }

                zin.closeEntry();
                fout.close();

            }
            zin.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static boolean checkFileExistence(Context context, String name) {
        File file1 = new File(getMemeticameDirectory() + "/" + name);
        File file2 = new File(getMemeticameDownloadsDirectory() + "/" + name);
        File file3 = new File(getMemeticameMemeaudiosDirectory() + "/" + name);
        File file4 = new File(getMemeticameMemesDirectory() + "/" + name);
        File file5 = new File(getMemeticameUnzipsDirectory() + "/" + name);
        File file6 = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + name);
        File file7 = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + name);

        return file1.exists() || file2.exists() || file3.exists() || file4.exists() || file5.exists() || file6.exists() || file7.exists();
    }

    public static Uri getUriFromFileName(Context context, String name) {
        File file1 = new File(getMemeticameDirectory() + "/" + name);
        File file2 = new File(getMemeticameDownloadsDirectory() + "/" + name);
        File file3 = new File(getMemeticameMemeaudiosDirectory() + "/" + name);
        File file4 = new File(getMemeticameMemesDirectory() + "/" + name);
        File file5 = new File(getMemeticameUnzipsDirectory() + "/" + name);
        File file6 = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + name);
        File file7 = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + name);

        if (file1.exists()) {
            return Uri.fromFile(file1);
        } else if (file2.exists()) {
            return Uri.fromFile(file2);
        } else if (file3.exists()) {
            return Uri.fromFile(file3);
        } else if (file4.exists()) {
            return Uri.fromFile(file4);
        } else if (file5.exists()) {
            return Uri.fromFile(file5);
        } else if (file6.exists()) {
            return Uri.fromFile(file6);
        } else if (file7.exists()) {
            return Uri.fromFile(file7);
        } else {
            return null;
        }
    }

    public static String getPathFromFileName(Context context, String name) {
        File file1 = new File(getMemeticameDirectory() + "/" + name);
        File file2 = new File(getMemeticameDownloadsDirectory() + "/" + name);
        File file3 = new File(getMemeticameMemeaudiosDirectory() + "/" + name);
        File file4 = new File(getMemeticameMemesDirectory() + "/" + name);
        File file5 = new File(getMemeticameUnzipsDirectory() + "/" + name);
        File file6 = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + name);
        File file7 = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + name);

        if (file1.exists()) {
            return getMemeticameDirectory();
        } else if (file2.exists()) {
            return getMemeticameDownloadsDirectory();
        } else if (file3.exists()) {
            return getMemeticameMemeaudiosDirectory();
        } else if (file4.exists()) {
            return getMemeticameMemesDirectory();
        } else if (file5.exists()) {
            return getMemeticameUnzipsDirectory();
        } else if (file6.exists()) {
            return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        } else if (file7.exists()) {
            return context.getExternalFilesDir(Environment.DIRECTORY_MUSIC).toString();
        } else {
            return null;
        }

    }

    public static long downloadFile(Context context, Uri downloadUri, String name) {
        File dir = new File(getMemeticameDownloadsDirectory());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(name)
                .setDescription("Downloaded with Memeticame")
                .setDestinationInExternalPublicDir("/Memeticame/Downloads", name);

        return downloadManager.enqueue(request);
    }

    public static void downloadAttachment(final Context context, final Attachment attachment) {
        if (attachment == null || !URLUtil.isValidUrl(attachment.getStringUri())) {
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Download file")
                .setMessage("Do you really want to download this file (" + attachment.getName() + ")?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        attachment.setDownloadId(downloadFile(context, Uri.parse(attachment.getStringUri()), attachment.getName()));
                        attachment.setProgress(0);
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    public static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    public static void deleteFile(String path) {
        File fileToDelete = new File(path);

        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }

    public static boolean saveBitmapToFile(Bitmap bitmap, File outputFile) {
        boolean success = false;
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return success;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
