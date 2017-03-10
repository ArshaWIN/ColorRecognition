package com.mihailenko.ilya.colorrecognizer2016.models;

import android.hardware.Camera;

/**
 * Created by Ilya on 19.02.2017.
 */

public class PreviewFrame {
    byte[] data;
    Camera camera;

    public PreviewFrame(byte[] data, Camera camera) {
        this.data = data;
        this.camera = camera;
    }

    public byte[] getData() {
        return data;
    }

    public Camera getCamera() {
        return camera;
    }
}
