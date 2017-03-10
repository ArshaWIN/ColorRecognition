package com.mihailenko.ilya.colorrecognizer2016.utils.interfaces;

import java.util.ArrayList;

public interface PhotoReceiver {
    void onPhotoReceive(String photoPath);

    void onMultiplePhotoReceive(ArrayList<String> photoList);
}
