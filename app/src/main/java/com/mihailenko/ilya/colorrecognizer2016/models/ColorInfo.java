package com.mihailenko.ilya.colorrecognizer2016.models;

/**
 * Created by Ilya on 19.02.2017.
 */

public class ColorInfo {
    String colorHex;
    String colorName;

    public ColorInfo(String colorHex, String colorName) {
        this.colorHex = colorHex;
        this.colorName = colorName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getColorName() {
        return colorName;
    }
}
