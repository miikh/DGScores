package com.doublesoft.dgscores;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yone on 14.10.2016.
 */

public class DatabaseAdapter {

    private SQLiteDatabase db;
    private DBOpenHelper dbHelper;
    private Context context;
    static final String DBNAME = "database.db";
    static final String ID = "_id";
    static final String TABLE_PLAYERS = "players";
    static final String TABLE_COURSES = "courses";
    static final String TABLE_HOLE = "hole";
    static final String TABLE_ROUNDS = "rounds";
    static final String TABLE_SCORECARDS = "scorecards";

    public DatabaseAdapter(Context context){
        this.context = context;
        dbHelper = new DBOpenHelper(context, DBNAME, null, 1);
    }


    static class DBOpenHelper extends SQLiteOpenHelper{

        private static final String CREATEDATABASE = "";

        public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATEDATABASE);
        }

        @Override
        public void onOpen(SQLiteDatabase db){
            super.onOpen(db);
            if (!db.isReadOnly()) {
                // Enable foreign key constraints
                db.execSQL("PRAGMA foreign_keys=ON;");
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /*
            db.execSQL("DROP TABLE IF EXISTS " + TABLE);
            onCreate(db);
            */
        }

    }
}
