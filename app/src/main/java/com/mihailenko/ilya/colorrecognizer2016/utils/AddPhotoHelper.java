package com.mihailenko.ilya.colorrecognizer2016.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mihailenko.ilya.colorrecognizer2016.R;
import com.mihailenko.ilya.colorrecognizer2016.activities.BaseActivity;
import com.mihailenko.ilya.colorrecognizer2016.utils.interfaces.ActivityForResultStarter;
import com.mihailenko.ilya.colorrecognizer2016.utils.interfaces.ActivityResultHandler;
import com.mihailenko.ilya.colorrecognizer2016.utils.interfaces.PermissionCallback;
import com.mihailenko.ilya.colorrecognizer2016.utils.interfaces.PhotoReceiver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class AddPhotoHelper implements ActivityResultHandler {
    private static final int SELECT_PHOTO_REQ_CODE = 213;
    private static final int SELECT_MULTIPLE_REQ_CODE = 123;
    private static final int TAKE_PHOTO_REQ_CODE = 546;
    //static because it needs to be preserved along the activity restart
    @Nullable private static String currentPhotoPath = null;
    @NonNull private final BaseActivity activity;
    @NonNull private final ActivityForResultStarter activityForResultStarter;
    private final FileStorage fileStorage;
    @Nullable
    private PhotoReceiver photoReceiver;
    private String title;
    private boolean hasMultipleSelection;
    private MaterialDialog dialog;

    private AddPhotoHelper(@NonNull BaseActivity activity, @NonNull ActivityForResultStarter activityForResultStarter, String title) {
        this.activity = activity;
        this.activityForResultStarter = activityForResultStarter;
        this.title = title;

        fileStorage = new FileStorage(activity);
    }

    private void setHasMultipleSelection() {
        hasMultipleSelection = true;
    }

    public void setPhotoReceiver(@Nullable PhotoReceiver photoReceiver) {
        this.photoReceiver = photoReceiver;
    }

    public void showAddDialog() {

        PermissionCallback permissionCallback = AddPhotoHelper.this::doShowDialog;

        if (!hasReadWritePermissions(permissionCallback)) return;

        doShowDialog();
    }

    private void doShowDialog() {
        getDialog().show();
    }

    private MaterialDialog getDialog() {
        if (dialog == null) {
            dialog = new MaterialDialog.Builder(activity)
                    .title(title)
                    .items(R.array.select_photo_dialog)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                            switch (which) {
                                case 0:
                                    selectFromGallery();
                                    break;
                                case 1:
                                    takePhoto();
                                    break;
                            }
                        }
                    })
                    .build();
        }
        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (photoReceiver == null || resultCode == Activity.RESULT_CANCELED) return;

        switch (requestCode) {
            case SELECT_PHOTO_REQ_CODE:
            case SELECT_MULTIPLE_REQ_CODE:
                processSelectionResult(data);
                break;

            case TAKE_PHOTO_REQ_CODE:
                if (currentPhotoPath != null) {
                    photoReceiver.onPhotoReceive(FileStorage.addFilePrefix(currentPhotoPath));
                    currentPhotoPath = null;
                }
                break;

        }
    }

    private void processSelectionResult(Intent data) {
        if (photoReceiver == null) return;

        if (data.getData() != null) {

            photoReceiver.onPhotoReceive(data.getDataString());

        } else if (data.getClipData() != null) {
            ClipData clipData = data.getClipData();
            int itemCount = clipData.getItemCount();
            ArrayList<String> photoList = new ArrayList<>(itemCount);
            for (int i = 0; i < itemCount; i++) {
                photoList.add(clipData.getItemAt(i).getUri().toString());
            }
            photoReceiver.onMultiplePhotoReceive(photoList);
        }
    }

    private void takePhoto() {

        PermissionCallback permissionCallback = new PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                AddPhotoHelper.this.doTakePhoto();
            }
        };

        if (!hasReadWritePermissions(permissionCallback)) return;

        doTakePhoto();
    }

    private void doTakePhoto() {
        File photoFile;
        try {
            //checked above
            //noinspection MissingPermission
            photoFile = fileStorage.getNewOutputImageFileInPictureDir();
        } catch (IOException e) {
            return;
        }

        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (photoFile != null) {
            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            currentPhotoPath = photoFile.getAbsolutePath();
        }

        activityForResultStarter.startActivityForResult(takePicture, TAKE_PHOTO_REQ_CODE);
    }

    private boolean hasReadWritePermissions(PermissionCallback permissionCallback) {
        return activity.checkPermissions(
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                permissionCallback,
                28);
    }

    private void selectFromGallery() {
        if (hasMultipleSelection) {
            selectMultiplePhoto();
        } else {
            selectSinglePhoto();
        }
    }

    private void selectMultiplePhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityForResultStarter.startActivityForResult(Intent.createChooser(intent, title), SELECT_MULTIPLE_REQ_CODE);
    }

    private void selectSinglePhoto() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityForResultStarter.startActivityForResult(pickPhoto, SELECT_PHOTO_REQ_CODE);
    }

    public static class Builder {
        @NonNull private BaseActivity activity;
        @NonNull private ActivityForResultStarter activityForResultStarter;
        @Nullable private PhotoReceiver photoReceiver;
        private String title;
        private boolean hasMultipleSelection;

        public Builder(@NonNull BaseActivity activity, @NonNull ActivityForResultStarter activityForResultStarter) {
            this.activity = activity;
            this.activityForResultStarter = activityForResultStarter;
            title = activity.getString(R.string.add_photo);
        }


        public Builder setPhotoReceiver(@Nullable PhotoReceiver photoReceiver) {
            this.photoReceiver = photoReceiver;
            return this;
        }

        public Builder setTitle(@StringRes int titleRes) {
            return setTitle(activity.getResources().getString(titleRes));
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder multipleSelection() {
            this.hasMultipleSelection = true;
            return this;
        }

        public AddPhotoHelper build() {
            AddPhotoHelper photoHelper = new AddPhotoHelper(activity, activityForResultStarter, title);

            photoHelper.setPhotoReceiver(photoReceiver);

            if (hasMultipleSelection) {
                photoHelper.setHasMultipleSelection();
            }

            return photoHelper;
        }
    }
}
