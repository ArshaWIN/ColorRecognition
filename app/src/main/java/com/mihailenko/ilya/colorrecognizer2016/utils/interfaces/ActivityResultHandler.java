package com.mihailenko.ilya.colorrecognizer2016.utils.interfaces;

import android.content.Intent;

public interface ActivityResultHandler {
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
