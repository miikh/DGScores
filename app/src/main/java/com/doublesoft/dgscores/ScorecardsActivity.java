package com.doublesoft.dgscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
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

        LinearLayout gameTable = (LinearLayout) findViewById(R.id.scorecard_layout);

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

        // käydään jokainen yksittäinen peli läpi ja "tulostetaan" tiedot testViewiin
        int i=0;
        for(Integer gameId : gameIds){
            String _players = "";
            String _course = "";

            Cursor[] scores = gameScoresByPlayers.get(gameId);
            scores[i].moveToFirst();
            long holeId = scores[i].getLong(scores[i].getColumnIndex("FAIRWAY_ID"));
            Cursor course = db.getCourse(holeId);
            course.moveToFirst();

            // Set course name
            _course = course.getString(course.getColumnIndex("NAME")) + "\n";

            int throwCount = 0;
            for(Cursor c : scores){
                c.moveToFirst();
                Cursor player = db.getPlayer(c.getLong(c.getColumnIndex("PLAYER_ID")));
                player.moveToFirst();

                // Set player names
                _players += player.getString(1) + ",";

                while(!c.isAfterLast()) {
                    throwCount += c.getInt(c.getColumnIndex("THROW_COUNT"));
                    c.moveToNext();
                }
                //testView.append("throws: " + Integer.toString(throwCount) + "\n");
                //testView.append("score: " + Integer.toString(throwCount - course.getInt(course.getColumnIndex("PAR")))+ "\n");
                throwCount = 0;
                View row = createGameRow(gameId, _players, _course);
                gameTable.addView(row);

                //TODO: peliriveille omat clickHandlerit, joilla saatas gameId passattua ViewScorecardiin
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(ScorecardsActivity.this, ViewScorecardsActivity.class);
                        ScorecardsActivity.this.startActivity(i);
                    }
                });

            }
            i++;
        }

    }

    public View createGameRow(int gameId, String players, String course) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View row = inflater.inflate(R.layout.scorecards_game, null, false);
        TextView playersView = (TextView) row.findViewById(R.id.textView_players);
        TextView courseView = (TextView) row.findViewById(R.id.textView_course);

        playersView.append(players);
        courseView.append(course);

        return row;
    }


}
