package com.mihailenko.ilya.colorrecognizer2016.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.mihailenko.ilya.colorrecognizer2016.R;
import com.mihailenko.ilya.colorrecognizer2016.activities.viewmodels.SurfActivityViewModel;
import com.mihailenko.ilya.colorrecognizer2016.databinding.ActivitySurfBinding;
import com.mihailenko.ilya.colorrecognizer2016.models.MyColor;
import com.mihailenko.ilya.colorrecognizer2016.utils.Constants;
import com.mihailenko.ilya.colorrecognizer2016.utils.RxCameraPreviewListener;
import com.mihailenko.ilya.colorrecognizer2016.utils.SQLHelper;

import java.io.IOException;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by ILYA on 07.08.2016.
 */
@RuntimePermissions
public class SurfActivity extends BaseActivity
        implements SurfaceHolder.Callback, SensorEventListener, SurfActivityViewModel {
    public static int PHOTO_HEIGHT_THRESHOLD = 1280;

    private SQLHelper sqlHelper;

    private SensorManager sensorMan;
    private Sensor accelerometer;

    private RxCameraPreviewListener rxCameraPreviewListener;

    private MyColor myColor;
    private SurfaceHolder holder;
    private Camera camera;

    final boolean FULL_SCREEN = true;

    final int CAMERA_ID = 0;

    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    private String currentColorHex;
    private String currentColorName;

    private ActivitySurfBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SurfActivityPermissionsDispatcher.checkPermissionWithCheck(this);

        rxCameraPreviewListener = new RxCameraPreviewListener();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_surf);
        binding.setViewModel(this);

        setupActionBar();
        sqlHelper = new SQLHelper(this);

        holder = binding.surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        setMovementRecognize();

        holder.addCallback(this);

    }

    @NeedsPermission({android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE})
    void checkPermission() {

    }

    private boolean permissionsGranted() {
        String[] permissions = {android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE};
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (permissionsGranted()) {

            camera = Camera.open(CAMERA_ID);
            setPreviewSize(FULL_SCREEN);

            sensorMan.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null)
            try {
                camera.stopPreview();
                camera.setPreviewCallback(null);
                camera.release();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        sensorMan.unregisterListener((SensorEventListener) this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (permissionsGranted()) {
            try {
                camera.setPreviewDisplay(holder);
                Camera.Parameters params = camera.getParameters();
                params.setPictureSize(getPreferredPictureSize().width, getPreferredPictureSize().height);
                camera.setParameters(params);
                camera.startPreview();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (camera == null) {
            return;
        }

        camera.stopPreview();
        setCameraDisplayOrientation(CAMERA_ID);
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            camera.setPreviewCallback(rxCameraPreviewListener);
            rxCameraPreviewListener.getCameraPreview()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(colorInfo -> {
                        currentColorName = colorInfo.getColorName();
                        currentColorHex = colorInfo.getColorHex();
                        String myColor = String.format("цвет : %s - %s", currentColorHex, currentColorName);
                        binding.colorInfo.setText(myColor);

                        int currentColor = Color.parseColor(currentColorHex);
                        binding.currentColor.setBackgroundColor(currentColor);
                    }, Throwable::printStackTrace);

//            camera.setPreviewCallback(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    void setPreviewSize(boolean fullScreen) {

        // получаем размеры экрана
        Display display = getWindowManager().getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();

        // определяем размеры превью камеры
        Camera.Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        // RectF экрана, соотвествует размерам экрана
        rectDisplay.set(0, 0, display.getWidth(), display.getHeight());

        // RectF первью
        if (widthIsMax) {
            // превью в горизонтальной ориентации
            rectPreview.set(0, 0, size.width, size.height);
        } else {
            // превью в вертикальной ориентации
            rectPreview.set(0, 0, size.height, size.width);
        }

        Matrix matrix = new Matrix();
        // подготовка матрицы преобразования
        if (!fullScreen) {
            // если превью будет "втиснут" в экран
            matrix.setRectToRect(rectPreview, rectDisplay,
                    Matrix.ScaleToFit.START);
        } else {
            // если экран будет "втиснут" в превью
            matrix.setRectToRect(rectDisplay, rectPreview,
                    Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }
        // преобразование
        matrix.mapRect(rectPreview);

        // установка размеров surface из получившегося преобразования
        binding.surfaceView.getLayoutParams().height = (int) (rectPreview.bottom);
        binding.surfaceView.getLayoutParams().width = (int) (rectPreview.right);
    }

    void setCameraDisplayOrientation(int cameraId) {
        // определяем насколько повернут экран от нормального положения
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        // получаем инфо по камере cameraId
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // задняя камера
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else
            // передняя камера
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        result = result % 360;
        camera.setDisplayOrientation(result);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values.clone();
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) (Math.sqrt(x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            if (Math.abs(mAccel) < 0.3) {
                binding.frame.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);

            } else {
                binding.frame.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // required method
    }

    private void setMovementRecognize() {
        sensorMan = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    private Camera.Size getPreferredPictureSize() {
        Camera.Size res = null;
        Camera.Parameters params = camera.getParameters();
        float defaultCameraRatio = (float) params.getPictureSize().width / (float) params.getPictureSize().height;

        List<Camera.Size> sizes = camera.getParameters().getSupportedPictureSizes();

        for (Camera.Size s : sizes) {
            float ratio = (float) s.width / (float) s.height;
            if (ratio == defaultCameraRatio && s.height <= PHOTO_HEIGHT_THRESHOLD) {
                res = s;
                break;
            }
        }
        return res;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.action_history: {
                startActivity(new Intent(this, ColorHistoryActivity.class));
                break;
            }
            case R.id.action_default_mode: {
                item.setChecked(true);
                rxCameraPreviewListener.setColorMode(Constants.DEFAULT_TYPE);
                break;
            }
            case R.id.action_extra_mode: {
                item.setChecked(true);
                rxCameraPreviewListener.setColorMode(Constants.EXTRA_TYPE);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu_surf, menu);
        return true;
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


    private void setMyColorInfo() {
        myColor = new MyColor();
        myColor.setColorName(currentColorName);
        myColor.setColorHEX(currentColorHex);
    }

    @Override
    public void onAddPhotoClick(View view) {
        setMyColorInfo();
        sqlHelper.addColor(myColor);
        Toast.makeText(SurfActivity.this, R.string.color_to_history, Toast.LENGTH_SHORT).show();
    }
}
