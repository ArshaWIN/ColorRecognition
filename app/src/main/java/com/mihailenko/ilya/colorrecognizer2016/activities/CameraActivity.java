package com.mihailenko.ilya.colorrecognizer2016.activities;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mihailenko.ilya.colorrecognizer2016.R;
import com.mihailenko.ilya.colorrecognizer2016.activities.viewmodels.CameraActivityViewModel;
import com.mihailenko.ilya.colorrecognizer2016.databinding.ActivityCameraBinding;
import com.mihailenko.ilya.colorrecognizer2016.models.MyColor;
import com.mihailenko.ilya.colorrecognizer2016.utils.AddPhotoHelper;
import com.mihailenko.ilya.colorrecognizer2016.utils.ColorUtils;
import com.mihailenko.ilya.colorrecognizer2016.utils.Constants;
import com.mihailenko.ilya.colorrecognizer2016.utils.SQLHelper;
import com.mihailenko.ilya.colorrecognizer2016.utils.interfaces.ActivityForResultStarter;
import com.mihailenko.ilya.colorrecognizer2016.utils.interfaces.PhotoReceiver;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;


/**
 * Created by ILYA on 07.08.2016.
 */
@RuntimePermissions
public class CameraActivity extends BaseActivity implements CameraActivityViewModel, ActivityForResultStarter, PhotoReceiver {


    private ActivityCameraBinding binding;

    private AddPhotoHelper photoHelper;
    private Bitmap photoBitmap;

    private SQLHelper sqlHelper;

    private MaterialDialog agreeDialog;

    private int redValue = 0;
    private int blueValue = 0;
    private int greenValue = 0;

    private String currentColorHex;
    private String currentColorName;
    private MyColor myColor;

    private String COLOR_MODE = Constants.DEFAULT_TYPE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera);
        binding.setViewModel(this);

        binding.fab.setEnabled(false);

        setupActionBar();
        createPhotoHelper();

        sqlHelper = new SQLHelper(this);

        CameraActivityPermissionsDispatcher.showPhotoDialogWithCheck(this);

        binding.photoImage.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                movePointer(motionEvent.getX(), motionEvent.getY());
                getAverageColor(photoBitmap, Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));

                currentColorName = ColorUtils.getInstanse().getColorNameFromRgb(redValue, greenValue, blueValue, COLOR_MODE);

                currentColorHex = ColorUtils.getInstanse().getColorHexFromRGB(redValue, greenValue, blueValue);
                String colorInfo = String.format("цвет : %s - %s", currentColorHex, currentColorName);
                binding.colorInfo.setText(colorInfo);

                int currentColor = Color.parseColor(currentColorHex);
                binding.currentColor.setBackgroundColor(currentColor);
            }
            return true;
        });

    }

    private void setupActionBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(R.string.take_color);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        }
    }

    private void createDialog() {
        if (agreeDialog == null) {
            agreeDialog = new MaterialDialog.Builder(this)
                    .title(R.string.dialog_tittle)
                    .content(R.string.dialog_content)
                    .positiveText(R.string.ok)
                    .negativeText(R.string.cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            sqlHelper.addColor(myColor);
                            Toast.makeText(CameraActivity.this, R.string.color_to_history, Toast.LENGTH_SHORT).show();
                        }
                    }).build();
        }
    }


    private void createPhotoHelper() {
        photoHelper = new AddPhotoHelper.Builder(this, this)
                .setTitle(R.string.upload_logo)
                .setPhotoReceiver(this)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (photoHelper != null) {
            photoHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onAddPhotoClick(View view) {
        setMyColorInfo();
        createDialog();
        agreeDialog.show();
    }

    private void setMyColorInfo() {
        myColor = new MyColor();
        myColor.setColorName(currentColorName);
        myColor.setColorHEX(currentColorHex);
    }

    @Override
    public void onPhotoReceive(final String photoPath) {

        binding.photoImage.setVisibility(View.VISIBLE);
        binding.fab.setEnabled(true);

        Picasso.with(this)
                .load(photoPath)
                .resize(binding.photoImage.getWidth()
                        , binding.photoImage.getHeight())
                .centerCrop()
                .into(binding.photoImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        photoBitmap = ((BitmapDrawable) binding.photoImage.getDrawable()).getBitmap();
                        binding.photoImage.setImageBitmap(photoBitmap);
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    @Override
    public void onMultiplePhotoReceive(ArrayList<String> photoList) {

    }

    private void getAverageColor(Bitmap bitmap, int x, int y) {
        int redBucket = 0;
        int greenBucket = 0;
        int blueBucket = 0;
        int pixelCount = 0;

        if ((x > 2) && (y > 2)) {
            for (int m = x - 1; m < x + 1; m++) {
                for (int m1 = y - 1; m1 < y + 1; m1++) {
                    int c = bitmap.getPixel(m, m1);
                    pixelCount++;
                    redBucket += Color.red(c);
                    greenBucket += Color.green(c);
                    blueBucket += Color.blue(c);
                }
            }
            redValue = redBucket / pixelCount;
            greenValue = greenBucket / pixelCount;
            blueValue = blueBucket / pixelCount;
        }

    }

    private void movePointer(float x, float y) {
        binding.myPointer.setY(Math.round(y) - binding.myPointer.getHeight());
        binding.myPointer.setX(Math.round(x) - binding.myPointer.getWidth() / 2);
        binding.myPointer.setVisibility(View.VISIBLE);
    }

    @NeedsPermission({android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE})
    void showPhotoDialog() {
        photoHelper.showAddDialog();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.action_camera: {
                CameraActivityPermissionsDispatcher.showPhotoDialogWithCheck(this);
                break;
            }
            case R.id.action_history: {
                startActivity(new Intent(this, ColorHistoryActivity.class));
                break;
            }
            case R.id.action_default_mode: {
                item.setChecked(true);
                COLOR_MODE = Constants.DEFAULT_TYPE;
                break;
            }
            case R.id.action_extra_mode: {
                item.setChecked(true);
                COLOR_MODE = Constants.EXTRA_TYPE;
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu_camera, menu);
        return true;
    }
}
