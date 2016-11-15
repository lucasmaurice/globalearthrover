package com.globalearthrover.djls.robotserver;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Alexandre on 11/01/2016.
 */
public  class BBD_SQLite extends SQLiteOpenHelper  {

    protected static final int DATABASE_VERSION = 2;
    protected static final String DATABASE_NAME ="Memo_route";
    public static final String TABLE_NAME = "Coordonnees";
    public static final String COLUMN_PASS = "PASS";
    public static final String COLUMN_LAT = "LAT";
    public static final String COLUMN_LONG = "LONG";
    public static final String COLUMN_NUM_COO = "Num_Coo";

    protected static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_NUM_COO
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "+COLUMN_LAT + " REAL, " + COLUMN_PASS +" INTEGER, "+ COLUMN_LONG +" REAL )";


    public BBD_SQLite(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }


    /**
     * When you add modification to the database change the number of the version to recreate the database
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(BBD_SQLite.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }
}
