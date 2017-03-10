package com.mihailenko.ilya.colorrecognizer2016.utils;

import android.Manifest;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.annotation.StringRes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileStorage {
    private static final String NO_MEDIA_FILE = ".nomedia";
    private static final String FILE_PREFIX = "file://";
    private final int appNameRes;
    private Context context;

    public FileStorage(@NonNull Context context, @StringRes int appNameRes) {
        this.context = context;
        this.appNameRes = appNameRes;
    }

    public FileStorage(@NonNull Context context) {
        this.context = context;
        this.appNameRes = context.getApplicationContext().getApplicationInfo().labelRes;
    }

    @NonNull
    private static String getFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        return "IMG_" + timeStamp + ".jpg";
    }

    /**
     * @param dir directory file to create
     * @return true - if directory didn't exist and was created, otherwise - false
     * @throws IOException - if some error occurred while dir create
     */
    private static boolean createDir(File dir) throws IOException {
        if (!dir.exists()) {
            if (!dir.mkdirs() && !dir.exists()) {
                throw new IOException("Dir \"" + dir + "\" create error");
            }

            return true;
        }
        return false;
    }

    /**
     * Creates file ".nomedia" in dir
     *
     * @param dir
     * @throws IOException
     */
    private static void createNoMediaFile(File dir) throws IOException {
        File noMediaFile = new File(dir, NO_MEDIA_FILE);

        FileOutputStream outputStream = new FileOutputStream(noMediaFile);
        outputStream.close();
    }

    public static String addFilePrefix(@NonNull final String path) {
        return path.startsWith(FILE_PREFIX) ? path : FILE_PREFIX + path;
    }

    @NonNull
    public static String getFileExtension(@NonNull File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");

        return lastIndexOf == -1 ? "" : name.substring(lastIndexOf + 1, name.length());
    }

    @RequiresPermission(allOf = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public File getNewOutputImageFile(String subdir) throws IOException {
        File dir = new File(getAppDir(), subdir);

        createDir(dir);

        // Create a media file name
        return new File(dir + File.separator + getFileName());
    }

    @RequiresPermission(allOf = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public File getNewOutputImageFileInPictureDir() throws IOException {
        String appName = context.getResources().getString(appNameRes);
        File publicPictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File dir = new File(publicPictureDir, appName);

        createDir(dir);

        return new File(dir + File.separator + getFileName());
    }

    private File getAppDir() throws IOException {
        File appDir = new File(
                Environment.getExternalStorageDirectory(),
                context.getResources().getString(appNameRes));

        if (createDir(appDir)) {
            createNoMediaFile(appDir);
        }
        return appDir;
    }

    public String getRealPathFromUri(String uri) {
        return getRealPathFromUri(Uri.parse(uri));
    }

    public String getRealPathFromUri(Uri contentUri) {
        return FileUtils.getPath(context, contentUri);
    }
}
