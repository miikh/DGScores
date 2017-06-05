package com.doublesoft.dgscores;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Sisältää sovelluksen tietokantataulujen määrittelyt ja tietokantametodit.
 */
class DatabaseAdapter {

    private SQLiteDatabase db;
    private DBOpenHelper dbHelper;
    private static final String DB_NAME = "database.sqlite";
    private static final String TABLE_PLAYERS = "players";
    private static final String TABLE_COURSES = "courses";
    private static final String TABLE_HOLES = "holes";
    private static final String TABLE_SCORECARDS = "scorecards";

    DatabaseAdapter(Context context){
        dbHelper = new DBOpenHelper(context, DB_NAME, null, 1);
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

    /**
     * Lisää pelaajan tietokantaan
     * @param name pelaajan nimi
     * @return bool, onnistuiko lisääminen
     */
    boolean insertPlayers(ContentValues name){
        if(!checkPlayersDuplicate(name.get("NAME").toString())) {
            db.insert(TABLE_PLAYERS, null, name);
            return true;
        }
        else return false;
    }

    /**
     * Etsii tietokannasta duplikaattipelaajat
     * @param name pelaajan nimi
     * @return bool, löytyikö duplikaatteja
     */
    private boolean checkPlayersDuplicate(String name){
        Cursor c = db.rawQuery("SELECT * FROM PLAYERS WHERE NAME = ? AND DELETED = 0", new String[]{name});
        if(c != null && c.getCount() > 0) return true; // duplicate found
        else return false;
    }

    /**
     * Lisää radan tietokantaan
     * @param course radan tiedot
     * @param holes väylien tiedot
     * @return radan id. -1 jos samanniminen rata on jo olemassa
     */
    long insertCourse(ContentValues course, ContentValues[] holes){
        if(!checkCourseDuplicate(course.get("NAME").toString())) {
            long id = db.insert(TABLE_COURSES, null, course);
            for (ContentValues cv : holes) {
                cv.put("COURSE_ID", id);
                db.insert(TABLE_HOLES, null, cv);
            }
            return id;
        }
        else return -1;
    }

    /**
     * Etsii tietokannasta duplikaattiradat
     * @param name radan nimi
     * @return bool, löytyikö duplikaatteja
     */
    private boolean checkCourseDuplicate(String name){
        Cursor c = db.rawQuery("SELECT * FROM COURSES WHERE NAME = ? AND DELETED = 0", new String[]{name});
        if(c != null && c.getCount() > 0) return true; // duplicate found
        else return false;
    }

    /**
     * Lisää tuloskortin tietokantaan
     * @param scorecards tuloskortin tiedot
     */
    void insertScorecard(ContentValues[] scorecards){
        for(ContentValues scorecard : scorecards){
            db.insert(TABLE_SCORECARDS, null, scorecard);
        }
    }

    /**
     * Päivittää tietokannassa olevan tuloskortin
     * @param scorecards tuloskortin tiedot
     */
    void updateScorecards(ContentValues[] scorecards){
        for(ContentValues scorecard : scorecards){
            db.update(TABLE_SCORECARDS, scorecard, "PLAYER_ID = ? AND HOLE_ID = ? AND GAME_ID = ?",
                    new String[] {String.valueOf(scorecard.get("PLAYER_ID")), String.valueOf(scorecard.get("HOLE_ID")), String.valueOf(scorecard.get("GAME_ID"))});
        }
    }

    /**
     * Palauttaa tietokantaan lisätyt, poistamattomat pelaajat (Deleted = 0)
     * nimen mukaan nousevassa aakkosjärjestyksessä
     * @return pelaajat
     */
    public Cursor getPlayers(){
        return db.rawQuery("SELECT * FROM PLAYERS WHERE DELETED = 0 ORDER BY NAME ASC", null);
    }

    /**
     * Palauttaa tietokantaan lisätyn, poistamattoman pelaajan (Deleted = 0) pelaajan
     * @param name pelaajan nimi
     * @return pelaaja
     */
    Cursor getPlayer(String name) { return db.rawQuery("SELECT * FROM PLAYERS WHERE NAME = ? AND DELETED = 0", new String[] {name}); }

    /**
     * Palauttaa tietokantaan lisätyn, poistamattoman (Deleted = 0) pelaajan
     * @param id pelaajan id
     * @return pelaaja
     */
    Cursor getPlayer(long id) { return db.rawQuery("SELECT * FROM PLAYERS WHERE _id = ?", new String[] {Long.toString(id)}); }

    /**
     * Palauttaa tietokantaan lisätyn radan
     * @param id
     * @return rata
     */
    Cursor getCourse(long id){
        return db.rawQuery("SELECT * FROM COURSES WHERE _id = ?", new String[]{String.valueOf(id)});
    }

    /**
     *
     * @param players
     * @return
     */
    Cursor getCoursePlayers(ArrayList<String> players) {
        MatrixCursor cursor = new MatrixCursor(new String[] {"_id", "NAME"}, players.size());
        for(String p : players) {
            Cursor c = getPlayer(p);
            c.moveToFirst();
            cursor.addRow(new Object[] {c.getString(0), c.getString(1)});
        }
        return cursor;
    }

    /**
     * Palauttaa samalla kierroksella pelanneet pelaajat
     * @param gameId kierroksen yksilöivä tunniste
     * @return pelaajat
     */
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

    /**
     * Palauttaa radan väylät
     * @param courseName radan nimi
     * @return rata
     */
    Cursor getHoles(String courseName){
        Cursor course = getCourse(courseName);
        course.moveToFirst();
        long id = course.getLong(0);

        return db.rawQuery("SELECT * FROM HOLES WHERE COURSE_ID = ? ORDER BY _id ASC", new String[]{Long.toString(id)});
    }

    /**
     * Palauttaa radan väylät
     * @param id radan id
     * @return rata
     */
    Cursor getHoles(long id){
        return db.rawQuery("SELECT * FROM HOLES WHERE COURSE_ID = ?", new String[]{Long.toString(id)});
    }

    /**
     * Palauttaa kaikki poistamattomat (Deleted = 0) radat
     * @return rata
     */
    Cursor getCourses() {return db.rawQuery("SELECT * FROM COURSES WHERE DELETED = 0 ORDER BY NAME ASC", null); }

    /**
     * Palauttaa kaikki poistamattomat (Deleted = 0) radat
     * @param name radan nimi
     * @return rata
     */
    Cursor getCourse(String name) {return db.rawQuery("SELECT * FROM COURSES WHERE NAME = ? AND DELETED = 0", new String[]{name});}

    /**
     * Palauttaa radan, jolla tunnisteen mukainen kierros on pelattu
     * @param gameId kierroksen yksilöivä tunniste
     * @return rata
     */
    Cursor getCourseByGameId(int gameId){
        Cursor c = db.rawQuery("SELECT HOLE_ID FROM SCORECARDS WHERE GAME_ID = ?", new String[] {String.valueOf(gameId)});
        c.moveToFirst();
        Cursor c1 = getCourseByHoleId(c.getLong(0));
        c1.moveToFirst();
        String id = c1.getString(0);
        c.close();
        c1.close();
        return db.rawQuery("SELECT * FROM COURSES WHERE _id = ?", new String[]{id});
    }

    /**
     * Palauttaa radan, jolla id:n mukainen väylä on
     * @param holeId väylän id
     * @return rata
     */
    Cursor getCourseByHoleId(long holeId){
        Cursor c = db.rawQuery("SELECT COURSE_ID FROM HOLES WHERE _id = ? GROUP BY COURSE_ID", new String[]{Long.toString(holeId)});
        c.moveToFirst();
        String id = c.getString(c.getColumnIndex("COURSE_ID"));
        c.close();
        return db.rawQuery("SELECT * FROM COURSES WHERE _id = ?", new String[]{id});
    }

    /**
     * Palauttaa kaikki tuloskortit
     * @return tuloskortti
     */
    Cursor getScorecards() { return db.rawQuery("SELECT * FROM SCORECARDS ORDER BY GAME_ID", null); }

    /**
     * Palauttaa saman kierroksen tuloskorttirivit
     * @param gameId kierroksen yksilöivä tunniste
     * @return tuloskortti
     */
    Cursor getScorecardByGameId(long gameId) { return db.rawQuery("SELECT * FROM SCORECARDS WHERE GAME_ID = ?", new String[]{Long.toString(gameId)}); }

    /**
     * Palauttaa yhden pelaajan kierroksen tuloskorttirivit
     * @param gameId kierroksen yksilöivä tunniste
     * @param playerId pelaajan id
     * @return tuloskortti
     */
    Cursor getScorecardsByGameIdAndPlayerId(long gameId, long playerId){
        return db.rawQuery("SELECT * FROM SCORECARDS WHERE GAME_ID = ? AND PLAYER_ID = ? ORDER BY HOLE_ID ASC", new String[]{Long.toString(gameId), Long.toString(playerId)});
    }

    /**
     * Palauttaa kierroksen tuloskortin luettavassa muodossa
     * @param gameId kierroksen yksilöivä tunniste
     * @param playerId pelaajan id
     * @return tuloskortti
     */
    Cursor getScorecardsReadable(long gameId, long playerId){
        return db.rawQuery("SELECT Players.Name, Fairway.Name, Throw_Count" +
                " FROM SCORECARDS" +
                " LEFT JOIN Players ON Player_ID = Players.ID" +
                " LEFT JOIN Fairways ON Fairway_ID = Fairways.ID" +
                " WHERE GAME_ID = ? AND PLAYER_ID = ?" +
                " GROUP BY Player_ID", new String[]{Long.toString(gameId), Long.toString(playerId)});
    }

    /**
     * Palauttaa yksittäiset kierrokset tuloskortti-taulusta
     * @return kierrosID:t
     */
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

    /**
     * Palauttaa yksittäiset kierrokset tuloskortti-taulusta
     * @return kierrosID:t
     */
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

    /**
     * Palauttaa pelaajan heittolukeman tietyltä kierrokselta
     * @param playerId pelaajan ID
     * @param holeId väylän ID
     * @param gameId kierroksen ID
     * @return heittolukema
     */
    int getThrowCount(String playerId, String holeId, String gameId){
        Cursor c = db.rawQuery("SELECT THROW_COUNT FROM SCORECARDS WHERE PLAYER_ID = ? AND HOLE_ID = ? AND GAME_ID = ?", new String[] {playerId, holeId, gameId});
        c.moveToFirst();
        int throwCount = c.getInt(0);
        c.close();
        return throwCount;
    }

    /**
     * Poistaa peli-id:n osoittaman tuloskortin tietokannasta
     * @param gameId
     */
    void deleteByGameId(int gameId){
        db.delete("SCORECARDS", "GAME_ID = ?", new String[] {String.valueOf(gameId)} );
    }

    /**
     * Poistaa id:n osoittaman radan tietokannasta
     * @param id radan id
     */
    void deleteCourse(long id){
        Cursor course = db.rawQuery("SELECT * FROM COURSES WHERE _id = ?", new String[]{String.valueOf(id)});
        course.moveToFirst();
        ContentValues values = new ContentValues();
        values.put("_id", course.getLong(0));
        values.put("NAME", course.getString(course.getColumnIndex("NAME")));
        values.put("HOLE_COUNT", course.getInt(course.getColumnIndex("HOLE_COUNT")));
        values.put("PAR", course.getInt(course.getColumnIndex("PAR")));
        if(course.getInt(course.getColumnIndex("DISTANCE")) != -1) values.put("DISTANCE", course.getInt(course.getColumnIndex("DISTANCE")));
        values.put("DELETED", 1);
        course.close();
        db.update(TABLE_COURSES, values, "_id = ?", new String[]{String.valueOf(id)});
    }

    /**
     * Poistaa id:n osoittaman pelaajan tietokannasta
     * @param id pelaajan id
     */
    void deletePlayer(long id){
        Cursor player = db.rawQuery("SELECT * FROM PLAYERS WHERE _id = ?", new String[]{String.valueOf(id)});
        player.moveToFirst();
        ContentValues values = new ContentValues();
        values.put("_id", player.getLong(0));
        values.put("NAME", player.getString(player.getColumnIndex("NAME")));
        values.put("DELETED", 1);
        player.close();
        db.update(TABLE_PLAYERS, values, "_id=?", new String[]{String.valueOf(id)});
    }

    /**
     * Luo tietokantataulut
     */
    private static class DBOpenHelper extends SQLiteOpenHelper{

        private static final String CREATETABLE_COURSES = "CREATE TABLE IF NOT EXISTS COURSES (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "NAME TEXT NOT NULL, HOLE_COUNT INT NOT NULL, PAR INT NOT NULL, DISTANCE INT, DELETED INT NOT NULL);";
        private static final String CREATETABLE_HOLES = "CREATE TABLE IF NOT EXISTS HOLES (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "COURSE_ID INT, PAR INT NOT NULL, DISTANCE INT, NAME TEXT, " +
                "FOREIGN KEY (COURSE_ID) REFERENCES COURSES(_id));";
        private static final String CREATETABLE_PLAYERS = "CREATE TABLE IF NOT EXISTS PLAYERS (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "NAME TEXT NOT NULL, DELETED INT NOT NULL);";
        private static final String CREATETABLE_SCORECARDS = "CREATE TABLE IF NOT EXISTS SCORECARDS (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "PLAYER_ID INT, HOLE_ID INT, GAME_ID INT NOT NULL, OB INT, THROW_COUNT INT NOT NULL, DATE TEXT NOT NULL, " +
                "FOREIGN KEY (PLAYER_ID) REFERENCES PLAYERS(_id), " +
                "FOREIGN KEY (HOLE_ID) REFERENCES HOLES(_id));";

        DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATETABLE_COURSES);
            db.execSQL(CREATETABLE_HOLES);
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
