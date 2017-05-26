package com.doublesoft.dgscores;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by yone on 14.10.2016.
 */

class DatabaseAdapter {

    private SQLiteDatabase db;
    private DBOpenHelper dbHelper;
    private static final String DBNAME = "database.db";
    private static final String TABLE_PLAYERS = "players";
    private static final String TABLE_COURSES = "courses";
    private static final String TABLE_FAIRWAY = "fairway";
    private static final String TABLE_SCORECARDS = "scorecards";

    DatabaseAdapter(Context context){
        dbHelper = new DBOpenHelper(context, DBNAME, null, 1);
    }

    void open(){
        try{
            db = dbHelper.getWritableDatabase();
        }catch (SQLiteException e){
            db = dbHelper.getReadableDatabase();
        }
    }

    void close(){
        dbHelper.close();
    }

    void insertPlayers(ContentValues name){
        db.insert(TABLE_PLAYERS, null, name);
    }

    void insertCourse(ContentValues course, ContentValues[] fairways){
        long id = db.insert(TABLE_COURSES, null, course);
        for(ContentValues cv : fairways) {
            cv.put("course_id", id);
            db.insert(TABLE_FAIRWAY, null, cv);
        }
    }

    void insertScorecard(ContentValues[] scorecards){
        for(ContentValues scorecard : scorecards){
            db.insert(TABLE_SCORECARDS, null, scorecard);
        }
    }

    void updateScorecards(ContentValues[] scorecards){
        for(ContentValues scorecard : scorecards){
            db.update(TABLE_SCORECARDS, scorecard, "PLAYER_ID = ? AND FAIRWAY_ID = ? AND GAME_ID = ?",
                    new String[] {String.valueOf(scorecard.get("PLAYER_ID")), String.valueOf(scorecard.get("FAIRWAY_ID")), String.valueOf(scorecard.get("GAME_ID"))});
        }
    }

    public Cursor getPlayers(){
        return db.rawQuery("SELECT * FROM PLAYERS ORDER BY NAME ASC", null);
    }

    Cursor getPlayer(String name) { return db.rawQuery("SELECT * FROM PLAYERS WHERE NAME = ?", new String[] {name}); }

    Cursor getPlayer(long id) { return db.rawQuery("SELECT * FROM PLAYERS WHERE _id = ?", new String[] {Long.toString(id)}); }

    Cursor getCoursePlayers(ArrayList<String> players) {
        MatrixCursor cursor = new MatrixCursor(new String[] {"_id", "NAME"}, players.size());
        for(String p : players) {
            Cursor c = getPlayer(p);
            c.moveToFirst();
            cursor.addRow(new Object[] {c.getString(0), c.getString(1)});
        }
        return cursor;
    }

    Cursor getCoursePlayersByGameId(long gameId) {
        Cursor playerIds = db.rawQuery("SELECT PLAYER_ID FROM SCORECARDS WHERE GAME_ID = ? GROUP BY PLAYER_ID", new String[]{Long.toString(gameId)});
        MatrixCursor players = new MatrixCursor(new String[]{"_id", "NAME"}, playerIds.getCount());
        playerIds.moveToFirst();
        while(!playerIds.isAfterLast()){
            Cursor c = getPlayer(playerIds.getLong(playerIds.getColumnIndex("PLAYER_ID")));
            c.moveToFirst();
            players.addRow(new Object[] {c.getString(0), c.getString(1)});
            playerIds.moveToNext();
        }
        playerIds.close();
        return players;
    }

    Cursor getFairways(String courseName){
        Cursor course = getCourse(courseName);
        course.moveToFirst();
        long id = course.getLong(0);
        return db.rawQuery("SELECT * FROM FAIRWAY WHERE COURSE_ID = ? ORDER BY _id ASC", new String[]{Long.toString(id)});
    }

    Cursor getFairways(long id){
        return db.rawQuery("SELECT * FROM FAIRWAY WHERE COURSE_ID = ?", new String[]{Long.toString(id)});
    }

    Cursor getCourses() { return db.rawQuery("SELECT * FROM COURSES ORDER BY NAME ASC", null); }

    Cursor getCourse(String name) { return db.rawQuery("SELECT * FROM COURSES WHERE NAME = ?", new String[]{name}); }

    Cursor getCourseByGameId(int gameId){
        Cursor c = db.rawQuery("SELECT FAIRWAY_ID FROM SCORECARDS WHERE GAME_ID = ?", new String[] {String.valueOf(gameId)});
        c.moveToFirst();
        Cursor c1 = getCourse(c.getLong(0));
        c1.moveToFirst();
        String id = c1.getString(0);
        c.close();
        c1.close();
        return db.rawQuery("SELECT * FROM COURSES WHERE _id = ?", new String[]{id});
    }

    Cursor getCourse(long holeId){
        Cursor c = db.rawQuery("SELECT COURSE_ID FROM FAIRWAY WHERE _id = ? GROUP BY COURSE_ID", new String[]{Long.toString(holeId)});
        c.moveToFirst();
        String id = c.getString(c.getColumnIndex("COURSE_ID"));
        c.close();
        return db.rawQuery("SELECT * FROM COURSES WHERE _id = ?", new String[]{id});
    }

    Cursor getScorecards() { return db.rawQuery("SELECT * FROM SCORECARDS ORDER BY GAME_ID", null); }

    Cursor getScorecardByGameId(long gameId) { return db.rawQuery("SELECT * FROM SCORECARDS WHERE GAME_ID = ?", new String[]{Long.toString(gameId)}); }

    Cursor getScorecardsByGameIdAndPlayerId(long gameId, long playerId){
        return db.rawQuery("SELECT * FROM SCORECARDS WHERE GAME_ID = ? AND PLAYER_ID = ? ORDER BY FAIRWAY_ID ASC", new String[]{Long.toString(gameId), Long.toString(playerId)});
    }

    Cursor getScorecardsReadable(long gameId, long playerId){
        return db.rawQuery("SELECT Players.Name, Fairway.Name, Throw_Count" +
                " FROM SCORECARDS" +
                " LEFT JOIN Players ON Player_ID = Players.ID" +
                " LEFT JOIN Fairways ON Fairway_ID = Fairways.ID" +
                " WHERE GAME_ID = ? AND PLAYER_ID = ?" +
                " GROUP BY Player_ID", new String[]{Long.toString(gameId), Long.toString(playerId)});
    }

    int[] getScorecardGameIds(){
        Cursor cursor = db.rawQuery("SELECT GAME_ID FROM SCORECARDS GROUP BY GAME_ID ORDER BY datetime(DATE) DESC", null);
        cursor.moveToFirst();
        int[] ids = new int[cursor.getCount()];
        for(int i=0;i<ids.length;i++){
            ids[i] = cursor.getInt(cursor.getColumnIndex("GAME_ID"));
            cursor.moveToNext();
        }
        cursor.close();
        return ids;
    }

    ArrayList<Integer> getScorecardGameIdArrayList(){
        Cursor cursor = db.rawQuery("SELECT GAME_ID FROM SCORECARDS GROUP BY GAME_ID", null);
        cursor.moveToFirst();
        ArrayList<Integer> ids = new ArrayList<>(cursor.getCount());
        for(int i=0;i<cursor.getCount();i++){
            ids.add(cursor.getInt(cursor.getColumnIndex("GAME_ID")));
            cursor.moveToNext();
        }
        cursor.close();
        return ids;
    }

    int getThrowCount(String playerId, String holeId, String gameId){
        Cursor c = db.rawQuery("SELECT THROW_COUNT FROM SCORECARDS WHERE PLAYER_ID = ? AND FAIRWAY_ID = ? AND GAME_ID = ?", new String[] {playerId, holeId, gameId});
        c.moveToFirst();
        int throwCount = c.getInt(0);
        c.close();
        return throwCount;
    }

    private static class DBOpenHelper extends SQLiteOpenHelper{

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

        DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
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
