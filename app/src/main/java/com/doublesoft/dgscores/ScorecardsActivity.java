package com.doublesoft.dgscores;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;

public class ScorecardsActivity extends AppCompatActivity {

    Context context;
    Cursor[] scorecards;
    Cursor[] holes;
    Cursor[] players;
    LongSparseArray<Cursor[]> gameScoresByPlayers; // key: gameId, value: yhen pelin scorecardit pelaajittain
    DatabaseAdapter db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecards);
        context = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DatabaseAdapter(context);
        db.open();

        TextView testView = (TextView) findViewById(R.id.scorecards_text);

        gameScoresByPlayers = new LongSparseArray<>(); // HashMapin ja ArrayListin yhdistelmä
        Cursor allScorecards = db.getScorecards();
        int[] gameIds = db.getScorecardGameIds();
        int[] courseIds = new int[gameIds.length];
        scorecards = new Cursor[gameIds.length];
        holes = new Cursor[gameIds.length];
        players = new Cursor[gameIds.length];

        // kasataan tietokannasta yksittäiset pelit gameScoreByPlayers "listaan"
        for(int i=0;i<gameIds.length;i++){
            scorecards[i] = db.getScorecardByGameId(gameIds[i]);
            scorecards[i].moveToFirst();
            long holeId = scorecards[i].getInt(scorecards[i].getColumnIndex("FAIRWAY_ID"));
            Cursor c = db.getCourse(holeId);
            c.moveToFirst();
            //testView.append("Course " + c.getString(c.getColumnIndex("NAME")) + ", " + c.getString(0) + "\n");
            courseIds[i] = c.getInt(0);
            holes[i] = db.getFairways(courseIds[i]);
            holes[i].moveToFirst();
            //testView.append(holes[i].getString(0) + ", " + holes[i].getString(holes[i].getColumnIndex("COURSE_ID")) + "\n");
            players[i] = db.getCoursePlayersByGameId(gameIds[i]);
            players[i].moveToFirst();
            //testView.append(players[i].getString(1) + "\n");
            Cursor[] temp = new Cursor[players[i].getCount()];
            for(int j=0;j<players[i].getCount();j++){
                temp[j] = db.getScorecardsByGameIdAndPlayerId(gameIds[i], players[i].getLong(0));
                players[i].moveToNext();
            }
            gameScoresByPlayers.put(gameIds[i], temp);
        }


        allScorecards.moveToFirst();

        testView.append("scorecards table row count: ");
        testView.append(Integer.toString(allScorecards.getCount()) + "\n");
        testView.append("scorecards count: " + gameIds.length + "\n");

        // käydään jokainen yksittäinen peli läpi ja "tulostetaan" tiedot testViewiin
        int i=0;
        for(Integer gameId : gameIds){
            Cursor[] scores = gameScoresByPlayers.get(gameId);
            scores[i].moveToFirst();
            long holeId = scores[i].getLong(scores[i].getColumnIndex("FAIRWAY_ID"));
            Cursor course = db.getCourse(holeId);
            course.moveToFirst();
            testView.append("Course name: " + course.getString(course.getColumnIndex("NAME")) + "\n" + "Players: " + "\n");
            int throwCount = 0;
            for(Cursor c : scores){
                c.moveToFirst();
                Cursor player = db.getPlayer(c.getLong(c.getColumnIndex("PLAYER_ID")));
                player.moveToFirst();
                testView.append(player.getString(1) + "\n");
                while(!c.isAfterLast()) {
                    throwCount += c.getInt(c.getColumnIndex("THROW_COUNT"));
                    c.moveToNext();
                }
                testView.append("throws: " + Integer.toString(throwCount) + "\n");
                testView.append("score: " + Integer.toString(throwCount - course.getInt(course.getColumnIndex("PAR")))+ "\n");
                throwCount = 0;
            }
            i++;
        }

    }
}
