package com.example.alexandre.projetct_gps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Alexandre on 11/01/2016.
 */

/**
 * It's the database manager */
public class CoordonneesDao {
    protected final static int VERSION = 2;
    // The name of the file representing the base
    protected final static String NOM = "database.db";

    protected SQLiteDatabase mDb = null;
    protected BBD_SQLite mHandler = null;
   // public static final String TABLE_CREATE = "CREATE TABLE " + BBD_SQLite.TABLE_NAME + " (" + BBD_SQLite.COLUMN_NUM_COO + " INTEGER PRIMARY KEY AUTOINCREMENT, " + INTITULE + " TEXT, " + SALAIRE + " REAL);";
    public static final String TABLE_DROP =  "DROP TABLE IF EXISTS " + BBD_SQLite.TABLE_NAME + ";";

        /**
         * Use to add
         * @param m the coordonnees which be add to the BDD
         */
        public void ajouter(Coordonnees m) {
            ContentValues value = new ContentValues();
            value.put(BBD_SQLite.COLUMN_LAT,m.getLat());
            value.put(BBD_SQLite.COLUMN_LONG,m.getLong());
            value.put(BBD_SQLite.COLUMN_PASS,m.getState());
            mDb.insert(BBD_SQLite.TABLE_NAME,null,value);
        }

        /**
         * Use to delete
         * @param id the identifiant of the object should be erase
         */
        public void supprimer(long id) {
            mDb.delete(BBD_SQLite.TABLE_NAME, BBD_SQLite.COLUMN_NUM_COO + " = ?", new String[]{String.valueOf(id)});
        }

        /**
         * @param m the coordonnees changed
         */
        public void modifier(Coordonnees m) {

        }

        /**
         * Use to get the first coordinate where Pass = 0
         * @param id the identifiant should be put
         * @return Coordonnees represent a line of the database
         */
        public Coordonnees selectionner(long id) {
            Cursor c = mDb.rawQuery("select* from " + BBD_SQLite.TABLE_NAME + " where PASS = 0 ", null);
            c.moveToFirst();
            long FirstId = c.getLong(0);
            double FirstLat = c.getDouble(1);
            int FirstState = c.getInt(2);
            double FirstLong = c.getDouble(3);
            c.close();
            Coordonnees m = new Coordonnees(FirstId,FirstLat,FirstLong,FirstState);
            return m;
        }

    /** Selections all the data base and place it in an arraylist of Coordonnees
     *
     *
     */

    public ArrayList<Coordonnees> selections(){
        ArrayList<Coordonnees> Data = new ArrayList<>();
        Cursor c = mDb.rawQuery("select* from "+ BBD_SQLite.TABLE_NAME, null);
        c.moveToFirst();
        while(c.isAfterLast()){
            long FirstId = c.getLong(0);
            double FirstLat = c.getDouble(1);
            int FirstState = c.getInt(2);
            double FirstLong = c.getDouble(3);
            Data.add(new Coordonnees(FirstId,FirstLat,FirstLong,FirstState));
            c.moveToNext();
        }
        return Data;
    }




    public CoordonneesDao(Context pContext) {
        this.mHandler = new BBD_SQLite(pContext);
    }

    public SQLiteDatabase open() {
        // Pas besoin de fermer la dernière base puisque getWritableDatabase s'en charge
        mDb = mHandler.getWritableDatabase();
        return mDb;
    }

    public void close() {
        mDb.close();
    }

    public SQLiteDatabase getDb() {
        return mDb;
    }
}
