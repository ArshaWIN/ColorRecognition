package com.mihailenko.ilya.colorrecognizer2016.models;

/**
 * Created by ILYA on 06.08.2016.
 */

public class MyColor {
    private String colorName;
    private String colorHEX;
    private long id;

    public MyColor() {
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getColorHEX() {
        return colorHEX;
    }

    public void setColorHEX(String colorHEX) {
        this.colorHEX = colorHEX;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
