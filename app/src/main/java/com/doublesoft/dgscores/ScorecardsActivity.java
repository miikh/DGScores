package com.doublesoft.dgscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Sisältää tuloskortin tarkastelun toiminnallisuuden
 */
public class ScorecardsActivity extends AppCompatActivity {

    Context context;
    Cursor[] scorecards;
    Cursor[] holes;
    Cursor[] players;
    LongSparseArray<Cursor[]> gameScoresByPlayers; // key: gameId, value: yhden pelin scorecardit pelaajittain
    DatabaseAdapter db;
    ListView listView;
    ScorecardAdapter adapter;
    int[] gameIds;

    /**
     * Alustaa tuloskorttilistauksen
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecards);
        context = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        listView = (ListView)findViewById(R.id.scorecard_listview);
        adapter = new ScorecardAdapter(context, R.layout.scorecards_game, getScorecardData());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int gameId = Integer.parseInt(view.getTag().toString());
                Cursor[] scoresByPlayers = gameScoresByPlayers.get(gameId);
                scoresByPlayers[0].moveToFirst();
                long holeId = scoresByPlayers[0].getInt(scoresByPlayers[0].getColumnIndex("HOLE_ID"));
                Cursor course = db.getCourseByHoleId(holeId);
                course.moveToFirst();
                String courseName = course.getString(course.getColumnIndex("NAME"));
                ArrayList<String> playerList = new ArrayList<>();
                Cursor c = players[position];
                c.moveToFirst();
                while(!c.isAfterLast()){
                    playerList.add(c.getString(1));
                    c.moveToNext();
                }

                String msg = String.valueOf(gameId) + ":" + course.getString(0) + ":" + course.getInt(course.getColumnIndex("PAR"));
                Log.i("gameId:courseId:par", msg);

                Bundle b = new Bundle();
                b.putLong("courseId", course.getLong(0));
                b.putInt("gameId", gameId);
                b.putString("courseName", courseName);
                b.putStringArrayList("players", playerList);
                Intent intent = new Intent(ScorecardsActivity.this, ViewScorecardsActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        registerForContextMenu(listView);
    }

    /**
     * Avaa pitkän painalluksen konteksti-ikkunan tuloskortin poistamiseen
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_delete, menu);
    }

    /**
     * Tuloskortin poistaminen
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if(item.getItemId() ==  R.id.delete)
        {
            int id = Integer.parseInt(menuInfo.targetView.getTag().toString());
            db.deleteByGameId(id);
            adapter.remove(adapter.getItem(menuInfo.position));
            adapter.notifyDataSetChanged();
        }
        else return false;
        return true;
    }

    /**
     * Tuloskorttirivin ulkoasu ja tiedot
     */
    private class ScorecardAdapter extends ArrayAdapter<String[]>{
        LayoutInflater inflater;
        ArrayList<String[]> scorecardData;

        ScorecardAdapter(Context context,int resource, ArrayList<String[]> objects) {
            super(context, resource, objects);
            scorecardData = objects;
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View view = convertView;
            if(view == null){
                view = inflater.inflate(R.layout.scorecards_game, null);
            }

            String[] row = getItem(position);

            if(row != null){

                view.setTag(row[0]);

                TextView name = (TextView) view.findViewById(R.id.textView_course);
                TextView holes = (TextView) view.findViewById(R.id.textView_players);
                TextView par = (TextView) view.findViewById(R.id.textView_date);

                name.setText(row[1]);
                holes.setText(row[2]);
                par.setText(row[3]);

            }

            return view;

        }

    }

    /**
     * Hakee tietokannasta tuloskorttirivit
     * @return tuloskortti
     */
    private ArrayList<String[]> getScorecardData(){

        db = new DatabaseAdapter(context);
        db.open();

        gameScoresByPlayers = new LongSparseArray<>(); // HashMapin ja ArrayListin yhdistelmä
        gameIds = db.getScorecardGameIds();
        int[] courseIds = new int[gameIds.length];
        scorecards = new Cursor[gameIds.length];
        holes = new Cursor[gameIds.length];
        players = new Cursor[gameIds.length];

        // kasataan tietokannasta yksittäiset pelit gameScoreByPlayers "listaan"
        for(int i=0;i<gameIds.length;i++){
            scorecards[i] = db.getScorecardByGameId(gameIds[i]);
            scorecards[i].moveToFirst();
            long holeId = scorecards[i].getInt(scorecards[i].getColumnIndex("HOLE_ID"));
            Cursor c = db.getCourseByHoleId(holeId);
            c.moveToFirst();
            courseIds[i] = c.getInt(0);
            holes[i] = db.getHoles(courseIds[i]);
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

        ArrayList<String[]> scorecardData = new ArrayList<>();
        for(int i=0;i<gameScoresByPlayers.size();i++){
            int gameId = (int) gameScoresByPlayers.keyAt(i);
            String courseName;
            StringBuilder players = new StringBuilder();
            String date = "";

            // set course name
            Cursor[] scoresByPlayers = gameScoresByPlayers.get(gameId);
            scoresByPlayers[0].moveToFirst();
            long holeId = scoresByPlayers[0].getLong(scoresByPlayers[0].getColumnIndex("HOLE_ID"));
            Cursor course = db.getCourseByHoleId(holeId);
            course.moveToFirst();
            courseName = "Course: " + course.getString(course.getColumnIndex("NAME"));

            // set players and scores
            players.append("Players: ");
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

                players.append(playerName + "(" + String.valueOf(overallScore) + ") ");

                throwCount = 0;
            }

            // set date
            try {
                SimpleDateFormat dbDate = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");
                scoresByPlayers[0].moveToFirst();
                SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
                Date dateRaw = dbDate.parse(scoresByPlayers[0].getString(scoresByPlayers[0].getColumnIndex("DATE")));
                date = "Date: " + format.format(dateRaw);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // lisätään "rivin" tiedot arraylistiin
            scorecardData.add(new String[]{String.valueOf(gameId), courseName, players.toString(), date});
        }

        return scorecardData;
    }
}
