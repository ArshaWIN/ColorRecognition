package com.mihailenko.ilya.colorrecognizer2016.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.mihailenko.ilya.colorrecognizer2016.models.MyColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ILYA on 02.11.2015.
 */
public class SQLHelper extends SQLiteOpenHelper implements BaseColumns {

    public ArrayList<MyColor> getAllColor() {
        ArrayList<MyColor> colors = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + DATABASE_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                MyColor color = new MyColor();
                color.setId(Long.valueOf(cursor.getString(0)));
                color.setColorName(cursor.getString(1));
                color.setColorHEX(cursor.getString(2));
                colors.add(color);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return colors;
    }

    public void addColor(MyColor myColor) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_COLOR_NAME, myColor.getColorName());
        values.put(COLUMN_COLOR_HEX, myColor.getColorHEX());

        db.insert(DATABASE_TABLE, null, values);
        db.close();
    }

    public void deleteColor(MyColor color) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_TABLE, COLUMN_ID + " = ?",
                new String[]{String.valueOf(color.getId())});
        db.close();
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_TABLE, null, null);
        db.close();
    }

    private static final String DATABASE_NAME = "MyDatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_TABLE = "colors";

    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_COLOR_NAME = "COLOR_NAME";
    public static final String COLUMN_COLOR_HEX = "COLOR_HEX";
    private static final String DATABASE_CREATE_SCRIPT = "create table "
            + DATABASE_TABLE + " (" + BaseColumns._ID
            + " integer primary key autoincrement, " + COLUMN_COLOR_NAME
            + " text, " + COLUMN_COLOR_HEX + " text);";


    public SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public SQLHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public SQLHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_SCRIPT);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE);
        onCreate(db);

    }


}
