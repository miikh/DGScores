package com.doublesoft.dgscores;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

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
    static final String TABLE_FAIRWAY = "fairway";
    static final String TABLE_SCORECARDS = "scorecards";

    public DatabaseAdapter(Context context){
        this.context = context;
        dbHelper = new DBOpenHelper(context, DBNAME, null, 1);
    }

    public void open(){
        try{
            db = dbHelper.getWritableDatabase();
        }catch (SQLiteException e){
            db = dbHelper.getReadableDatabase();
        }
    }

    public void close(){
        dbHelper.close();
    }

    public void insertPlayers(ContentValues name){
        db.insert(TABLE_PLAYERS, null, name);
    }

    public void insertCourse(ContentValues course, ContentValues[] fairways){
        long id = db.insert(TABLE_COURSES, null, course);
        for(int i=0;i<fairways.length;i++){
            fairways[i].put("course_id", id);
            db.insert(TABLE_FAIRWAY, null, fairways[i]);
        }
    }

    public void insertScrorecards(){

    }

    public Cursor getPlayers(){
        return db.rawQuery("SELECT * FROM PLAYERS", null);
    }

    public Cursor getPlayer(String name) { return db.rawQuery("SELECT * FROM PLAYERS WHERE NAME = ?", new String[] {name}); }

    public Cursor getCoursePlayers(ArrayList<String> players) {
        MatrixCursor cursor = new MatrixCursor(new String[] {"_id", "NAME"}, players.size());
        for(String p : players) {
            Cursor c = getPlayer(p);
            c.moveToFirst();
            cursor.addRow(new Object[] {c.getString(0), c.getString(1)});
        }
        return cursor;
    }

    public Cursor getFairways(String courseName){

        Cursor course = getCourse(courseName);
        course.moveToFirst();
        long id = course.getLong(0);

        return db.rawQuery("SELECT * FROM FAIRWAY WHERE COURSE_ID =?", new String[]{Long.toString(id)});
    }

    public Cursor getCourses() { return db.rawQuery("SELECT * FROM COURSES ORDER BY NAME ASC", null); }

    public Cursor getCourse(String name) { return db.rawQuery("SELECT * FROM COURSES WHERE NAME = ?", new String[]{name}); }

    public Cursor getScorecards() { return db.rawQuery("SELECT * FROM SCORECARDS", null); }

    static class DBOpenHelper extends SQLiteOpenHelper{

        private static final String CREATETABLE_COURSES = "CREATE TABLE IF NOT EXISTS COURSES (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "NAME TEXT NOT NULL, HOLE_COUNT INT NOT NULL, PAR INT NOT NULL, DISTANCE INT);";
        private static final String CREATETABLE_FAIRWAY = "CREATE TABLE IF NOT EXISTS FAIRWAY (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "COURSE_ID INT, PAR INT NOT NULL, DISTANCE INT, NAME TEXT, " +
                "FOREIGN KEY (COURSE_ID) REFERENCES COURSES(_id));";
        private static final String CREATETABLE_PLAYERS = "CREATE TABLE IF NOT EXISTS PLAYERS (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "NAME TEXT NOT NULL);";
        private static final String CREATETABLE_SCORECARDS = "CREATE TABLE IF NOT EXISTS SCORECARDS (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "PLAYER_ID INT, FAIRWAY_ID INT, GAME_ID INT NOT NULL, OB INT, THROW_COUNT INT NOT NULL, DATE TEXT NOT NULL, " +
                "FOREIGN KEY (PLAYER_ID) REFERENCES PLAYERS(_id), " +
                "FOREIGN KEY (FAIRWAY_ID) REFERENCES FAIRWAY(_id));";

        public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATETABLE_COURSES);
            db.execSQL(CREATETABLE_FAIRWAY);
            db.execSQL(CREATETABLE_PLAYERS);
            db.execSQL(CREATETABLE_SCORECARDS);
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
