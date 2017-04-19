package com.mihailenko.ilya.colorrecognizer2016.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import com.mihailenko.ilya.colorrecognizer2016.models.ColorInfo;
import com.mihailenko.ilya.colorrecognizer2016.models.PreviewFrame;

import java.io.ByteArrayOutputStream;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Ilya on 19.02.2017.
 */

public class RxCameraPreviewListener implements Camera.PreviewCallback {

    private PublishSubject<PreviewFrame> subject = PublishSubject.create();

    private String COLOR_MODE = Constants.DEFAULT_TYPE;

    private int redValue = 0;
    private int blueValue = 0;
    private int greenValue = 0;

    private long lastTime = System.currentTimeMillis();


    public RxCameraPreviewListener() {

    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        long time = System.currentTimeMillis();
        if (data != null && time - lastTime > 500) {
            Log.d("TAG", "CHECK");
            lastTime = System.currentTimeMillis();
            subject.onNext(new PreviewFrame(data, camera));
        }
    }

    private ColorInfo count(byte[] data, Camera camera) {

        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;
        YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

        byte[] bytes = out.toByteArray();
        Bitmap bp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        bp = Bitmap.createBitmap(bp, bp.getWidth() / 2 - 10, bp.getHeight() / 2 - 10, 20, 20);
        getAverageColor(bp);

        String currentColorName = ColorUtils.getInstanse().getColorNameFromRgb(redValue, greenValue, blueValue, COLOR_MODE);
        String currentColorHex = ColorUtils.getInstanse().getColorHexFromRGB(redValue, greenValue, blueValue);

        return new ColorInfo(currentColorHex, currentColorName);
    }

    public Observable<ColorInfo> getCameraPreview() {
        return subject
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .map(previewFrame -> count(previewFrame.getData(), previewFrame.getCamera()))
                .asObservable();
    }

    private void getAverageColor(Bitmap bitmap) {
        int redBucket = 0;
        int greenBucket = 0;
        int blueBucket = 0;
        int pixelCount = 0;


        for (int m = 0; m < 20; m++) {
            for (int m1 = 0; m1 < 20; m1++) {
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

    public void setColorMode(String colorMode) {
        this.COLOR_MODE = colorMode;
    }
}
