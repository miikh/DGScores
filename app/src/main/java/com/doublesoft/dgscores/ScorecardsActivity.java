package com.doublesoft.dgscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.test.suitebuilder.TestMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ScorecardsActivity extends AppCompatActivity {

    Context context;
    Cursor[] scorecards;
    Cursor[] holes;
    Cursor[] players;
    LongSparseArray<Cursor[]> gameScoresByPlayers; // key: gameId, value: yhen pelin scorecardit pelaajittain
    DatabaseAdapter db;
    ListView listView;
    int[] gameIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecards);
        context = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DatabaseAdapter(context);
        db.open();

        //LinearLayout gameTable = (LinearLayout) findViewById(R.id.scorecard_layout);

        gameScoresByPlayers = new LongSparseArray<>(); // HashMapin ja ArrayListin yhdistelmä
        Cursor allScorecards = db.getScorecards();
        gameIds = db.getScorecardGameIds();
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
            courseIds[i] = c.getInt(0);
            holes[i] = db.getFairways(courseIds[i]);
            holes[i].moveToFirst();
            players[i] = db.getCoursePlayersByGameId(gameIds[i]);
            players[i].moveToFirst();
            Cursor[] temp = new Cursor[players[i].getCount()];
            for(int j=0;j<players[i].getCount();j++){
                temp[j] = db.getScorecardsByGameIdAndPlayerId(gameIds[i], players[i].getLong(0));
                players[i].moveToNext();
            }
            gameScoresByPlayers.put(gameIds[i], temp);
        }

        allScorecards.moveToFirst();

        listView = (ListView)findViewById(R.id.scorecard_listview);
        ArrayList<Integer> ids = db.getScorecardGameIdArrayList();
        ScorecardAdapter adapter = new ScorecardAdapter(context, R.layout.scorecards_game, ids);
        listView.setAdapter(adapter);

    }

    private class ScorecardAdapter extends ArrayAdapter<Integer>{
        LayoutInflater inflater;
        ArrayList<Integer> gameIds;

        ScorecardAdapter(Context context,int resource, ArrayList<Integer> objects) {
            super(context, resource, objects);
            gameIds = objects;
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View view = convertView;
            if(view == null){
                view = inflater.inflate(R.layout.scorecards_game, null);
            }
            TextView courseNameView = (TextView) view.findViewById(R.id.textView_course);
            TextView playersView = (TextView) view.findViewById(R.id.textView_players);
            TextView dateView = (TextView) view.findViewById(R.id.textView_date);
            int gameId = gameIds.get(position);

            // set course name
            Cursor[] scoresByPlayers = gameScoresByPlayers.get(gameId);
            scoresByPlayers[0].moveToFirst();
            int holeId = scoresByPlayers[0].getInt(scoresByPlayers[0].getColumnIndex("FAIRWAY_ID"));
            Cursor course = db.getCourse(holeId);
            course.moveToFirst();
            courseNameView.append(course.getString(course.getColumnIndex("NAME")));

            // set players and scores
            int throwCount = 0;
            for(Cursor c : scoresByPlayers){
                c.moveToFirst();
                Cursor player = db.getPlayer(c.getLong(c.getColumnIndex("PLAYER_ID")));
                player.moveToFirst();
                String playerName = player.getString(1);

                // count total throw count
                while(!c.isAfterLast()){
                    throwCount += c.getInt(c.getColumnIndex("THROW_COUNT"));
                    c.moveToNext();
                }

                int overallScore = throwCount - course.getInt(course.getColumnIndex("PAR"));

                String playerInfo = playerName + " (" + Integer.toString(overallScore) + "), ";
                playersView.append(playerInfo);

                throwCount = 0;
            }

            // set date
            try {
                SimpleDateFormat dbDate = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");
                scoresByPlayers[0].moveToFirst();
                SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
                Date date = dbDate.parse(scoresByPlayers[0].getString(scoresByPlayers[0].getColumnIndex("DATE")));
                dateView.append(format.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int gameId = gameIds.get(position);
                    Cursor[] scoresByPlayers = gameScoresByPlayers.get(gameId);
                    scoresByPlayers[0].moveToFirst();
                    int holeId = scoresByPlayers[0].getInt(scoresByPlayers[0].getColumnIndex("FAIRWAY_ID"));
                    Cursor course = db.getCourse(holeId);
                    course.moveToFirst();
                    String courseName = course.getString(course.getColumnIndex("NAME"));
                    ArrayList<String> playerList = new ArrayList<>();
                    Cursor c = players[position];
                    c.moveToFirst();
                    while(!c.isAfterLast()){
                        playerList.add(c.getString(1));
                        c.moveToNext();
                    }

                    Bundle b = new Bundle();
                    b.putInt("gameId", gameId);
                    b.putString("courseName", courseName);
                    b.putStringArrayList("players", playerList);
                    Intent intent = new Intent(ScorecardsActivity.this, ViewScorecardsActivity.class);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            });
            return view;
        }
    }


}
