package com.doublesoft.dgscores;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

/**
 * Sisältää kierroksen pelaamisen toiminnallisuuden
 */
public class PlayCourseActivity extends AppCompatActivity {

    Context context;
    DatabaseAdapter db;
    static Cursor course;
    static Cursor players;
    static Cursor holes;
    static int holeCount;
    static ArrayList<String> playerList;
    static HashMap<String,Scorecards> scorecards; // key == playerId + holeId
    static HashMap<String, String> scores;
    static HashMap<String, TextView> scoreViews;
    ViewPager viewPager;
    CustomPagerAdapter pagerAdapter;
    static private SparseArray<Fragment> fragments;
    static private ScoreFragment scoreFragment;
    static int gameId;
    Button next;
    Button prev;
    boolean newGame;
    boolean finished;
    boolean onBackPressed;

    Button finishButton;
    LinearLayout finishLayout;

    /**
     * Alustaa kierroksen pelaamisnäkymän. Jos jatketaan vanhaa peliä, hakee tietokannasta
     * kesken jääneen kierroksen tiedot.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_course);
        getSupportActionBar().hide();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;
        finished = false;
        onBackPressed = false;
        /*
        if (savedInstanceState != null) {
            newGame = false;
            gameId = savedInstanceState.getInt("gameId");
            final AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                                       .setTitle("Jatka peliä?")
                                        .setMessage("Haluatko jatkaa peliä?");
                       dialog.show();
        }
        */

        // Jos vanha peli löytyy
        SharedPreferences sp = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        int sharedPreference = sp.getInt("gameId", 0);
        if(sharedPreference > 0){
            gameId = sharedPreference;
            newGame = false;
            Log.i("sharedPreference", "gameId:" + String.valueOf(gameId));
            db = new DatabaseAdapter(context);
            db.open();
            players = db.getCoursePlayersByGameId(gameId);
            players.moveToFirst();
            course = db.getCourseByGameId(gameId);
            course.moveToFirst();
            playerList = new ArrayList<>();
            scores = new HashMap<>();
            scoreViews = new HashMap<>();
            while(!players.isAfterLast()){
                playerList.add(players.getString(1));
                Cursor c = db.getScorecardsByGameIdAndPlayerId(gameId, players.getLong(0));
                c.moveToFirst();
                int throwCount = 0;
                while(!c.isAfterLast()){
                    throwCount += c.getInt(c.getColumnIndex("THROW_COUNT"));
                    c.moveToNext();
                }
                throwCount -= course.getInt(course.getColumnIndex("PAR"));
                scores.put(players.getString(1), String.valueOf(throwCount));
                players.moveToNext();
            }


            holeCount = Integer.parseInt(course.getString(course.getColumnIndex("HOLE_COUNT")));
            ((TextView) findViewById(R.id.textView10)).setText(course.getString(course.getColumnIndex("NAME")));

            holes = db.getHoles(course.getString(course.getColumnIndex("NAME")));
            holes.moveToFirst();
            players.moveToFirst();
            scorecards = new HashMap<>();

            while (!holes.isAfterLast()){
                while (!players.isAfterLast()){
                    String pId = players.getString(0);
                    String hId = holes.getString(0);
                    String tC = String.valueOf(db.getThrowCount(pId, hId, String.valueOf(gameId)));
                    scorecards.put((pId+hId), new Scorecards(pId, hId, gameId, 0, tC));
                    players.moveToNext();
                }
                players.moveToFirst();
                holes.moveToNext();
            }
        }
        else { // Jos pelataan uusi peli
            newGame = true;
            Intent intent = this.getIntent();
            String courseName = intent.getStringExtra("courseName");
            playerList = intent.getStringArrayListExtra("players");
            scores = new HashMap<>();
            scoreViews = new HashMap<>();
            for (String p : playerList) {
                scores.put(p, "0");
            }

            db = new DatabaseAdapter(context);
            db.open();
            course = db.getCourse(courseName);
            course.moveToFirst();
            players = db.getCoursePlayers(playerList);
            holes = db.getHoles(courseName);

            holeCount = Integer.parseInt(course.getString(course.getColumnIndex("HOLE_COUNT")));

            ((TextView) findViewById(R.id.textView10)).setText(courseName);

            holes.moveToFirst();
            players.moveToFirst();

            Random rand = new Random(System.currentTimeMillis());
            gameId = rand.nextInt(Integer.MAX_VALUE) + 1;
            scorecards = new HashMap<>();
            while (!holes.isAfterLast()){
                while (!players.isAfterLast()){
                    String pId = players.getString(0);
                    String hId = holes.getString(0);
                    String tC = holes.getString(holes.getColumnIndex("PAR"));
                    scorecards.put((pId+hId), new Scorecards(pId, hId, gameId, 0, tC));
                    players.moveToNext();
                }
                players.moveToFirst();
                holes.moveToNext();
            }
        }

        finishButton = (Button) findViewById(R.id.btnFinish);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finished = true;
                //finish();
                showFinishDialog();
            }
        });

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        /**
         * ViewPager-käyttöliittymäelementin tapahtumakuuntelija
         */
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            /**
             * Muuttaa edellinen- ja seuraava-painikkeiden näkyvyyttä. Asettaa lopeta-painikkeen
             * näkyväksi jos ollaan radan lopussa.
             * @param position sijainti
             */
            @Override
            public void onPageSelected(int position) {
                if(position == 0) prev.setEnabled(false);
                else prev.setEnabled(true);
                if(position == holeCount) {
                    finishLayout.setVisibility(View.VISIBLE);
                    next.setEnabled(false);
                }
                else {
                    finishLayout.setVisibility(View.INVISIBLE);
                    next.setEnabled(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        //viewPager.setOffscreenPageLimit(3);

        pagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(pagerAdapter);

        next = (Button) findViewById(R.id.button2);
        prev = (Button) findViewById(R.id.button);
        prev.setEnabled(false);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(next.getText().equals("FINISH")){
                    showFinishDialog();
                }
                viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
            }
        });

    }

    /**
     * Vie kierroksen tuloskortin tietokantaan
     */
    void insertOrUpdateScorecards(){
        ContentValues[] values = new ContentValues[scorecards.size()];
        int i=0;
        for(HashMap.Entry e : scorecards.entrySet()){
            Scorecards sc = (Scorecards) e.getValue();
            values[i] = new ContentValues();
            values[i].put("PLAYER_ID", Integer.parseInt(sc.playerId));
            values[i].put("HOLE_ID", Integer.parseInt(sc.holeId));
            values[i].put("GAME_ID", sc.gameId);
            values[i].put("OB", sc.ob);
            values[i].put("THROW_COUNT", Integer.parseInt(sc.throwCount));
            values[i].put("DATE", sc.date.toString());
            i++;
        }
        if(newGame) {
            db.insertScorecard(values);
            newGame = false;
        }
        else db.updateScorecards(values);
        //Toast.makeText(context, "Scorecards inserted!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putInt("gameId", gameId);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Tallentaa kierroksen tiedot sovelluksen sulkeutuessa.
     */
    @Override
    protected void onStop() {
        SharedPreferences.Editor spEditor = getSharedPreferences("sharedPreferences", MODE_PRIVATE).edit();
        Intent i = new Intent(PlayCourseActivity.this, MainActivity.class);
        if(finished) {
            /*
            spEditor.putInt("gameId", 0);
            i.putExtra("gameId", 0);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            setResult(Activity.RESULT_OK, i);
            */
        }
        else if(onBackPressed){
            /*
            spEditor.putInt("gameId", gameId);
            i.putExtra("gameId", gameId);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            setResult(Activity.RESULT_CANCELED, i);
            */
        }
        //else spEditor.putInt("gameId", gameId);
        if(!finished && !onBackPressed) spEditor.putInt("gameId", gameId);
        spEditor.apply();
        insertOrUpdateScorecards();
        //if(finished || onBackPressed) startActivity(i);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        onBackPressed = true;
        Intent i = setActivityResults(false);
        super.onBackPressed();
        startActivity(i);
    }

    /**
     *
     */
    public static class HoleFragment extends Fragment {

        View rootView;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_play_course, container, false);

            Bundle b = getArguments();
            int hole = b.getInt("hole");
            String holeId = b.getString("holeId");
            String[] playerId = b.getStringArray("playerId");
            TextView holeView = (TextView) rootView.findViewById(R.id.textView2);

            holeView.setText("Hole " + (hole+1));

            players.moveToFirst();
            for(int i=0;i<playerId.length;i++){
                if(playerId[i].equals(players.getString(0)))
                    addRow(playerList.get(i), holeId, playerId[i], hole);
                players.moveToNext();
            }
            return rootView;
        }

        public static HoleFragment newInstance(int position, Cursor hole){

            HoleFragment fragment = new HoleFragment();
            String holeId = hole.getString(0);
            players.moveToFirst();
            String playerId[] = new String[players.getCount()];
            for(int i=0;i<playerId.length;i++){
                playerId[i] = players.getString(0);
                players.moveToNext();
            }
            Bundle b = new Bundle();
            b.putInt("hole", position);
            b.putString("holeId", holeId);
            b.putStringArray("playerId", playerId);
            fragment.setArguments(b);
            return fragment;
        }

        private void addRow(final String player, final String holeId, final String playerId, final int hole){
            LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.fragment_layout);
            LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());
            RelativeLayout row = (RelativeLayout) inflater.inflate(R.layout.play_course_row, null, false);

            TextView name = (TextView) row.findViewById(R.id.row_name);
            final TextView score = (TextView) row.findViewById(R.id.row_score);
            final TextView scoreView = (TextView) row.findViewById(R.id.row_scoreview);
            final Button subtract = (Button) row.findViewById(R.id.row_subtract);
            Button add = (Button) row.findViewById(R.id.row_add);
            score.setText(scorecards.get(playerId+holeId).throwCount);
            scoreView.setText("Score: " + scores.get(player));
            name.setText(player);
            scoreViews.put(player, scoreView);

            subtract.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String throwCount = Integer.toString(Integer.parseInt(score.getText().toString()) - 1);
                    String sV = Integer.toString(Integer.parseInt(scores.get(player)) - 1);
                    score.setText(throwCount);
                    scoreView.setText("Score: " + sV);
                    scorecards.get(playerId + holeId).throwCount = throwCount;
                    scorecards.get(playerId + holeId).date = Calendar.getInstance().getTime();
                    scores.put(player, sV);
                    for(int i=0;i<fragments.size();i++){
                        int key = fragments.keyAt(i);
                        updateOverallScores(fragments.get(key), player, sV, hole, throwCount);
                    }
                    if(score.getText().equals("1")){
                        subtract.setClickable(false);
                    }
                }
            });

            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String throwCount = Integer.toString(Integer.parseInt(score.getText().toString()) + 1);
                    String sV = Integer.toString(Integer.parseInt(scores.get(player)) + 1);
                    score.setText(throwCount);
                    scoreView.setText("Score: " + sV);
                    scorecards.get(playerId + holeId).throwCount = throwCount;
                    scorecards.get(playerId + holeId).date = Calendar.getInstance().getTime();
                    scores.put(player, sV);
                    for(int i=0;i<fragments.size();i++){
                        int key = fragments.keyAt(i);
                        updateOverallScores(fragments.get(key), player, sV, hole, throwCount);
                    }
                    if(score.getText().equals("2")){
                        subtract.setClickable(true);
                    }
                }
            });

            layout.addView(row);

        }

        private void updateOverallScores(Fragment fragment, String player, String score, int hole, String throwCount){
            LinearLayout layout = (LinearLayout)((HoleFragment)fragment).rootView.findViewById(R.id.fragment_layout);
            for(int i=0;i<layout.getChildCount();i++){
                RelativeLayout row = (RelativeLayout) layout.getChildAt(i);
                TextView scoreView = (TextView) row.findViewById(R.id.row_scoreview);
                TextView playerView = (TextView) row.findViewById(R.id.row_name);
                if(playerView.getText().toString().equals(player)) {
                    scoreView.setText("Score: " + score);
                    break;
                }
            }
            //TODO: jostain syystä ehtoon mennään välillä vaikka scorefragmenttiä ei pitäisi olla
            if(scoreFragment != null){
                TableRow firstRow = (TableRow)((TableLayout)scoreFragment.rootView.findViewById(R.id.scores_table)).getChildAt(0);
                int oldThrowCount;
                for(int i=0;i<playerList.size();i++){
                    TextView t = (TextView) firstRow.getChildAt(i+2);
                    if(t.getText().equals(player)){
                        TableRow row = (TableRow)((TableLayout)scoreFragment.rootView.findViewById(R.id.scores_table)).getChildAt(hole+1);
                        TextView scoreView = (TextView) row.getChildAt(i+2);
                        oldThrowCount = Integer.parseInt(scoreView.getText().toString());
                        scoreView.setText(throwCount);
                        TableRow lastRow = (TableRow)((TableLayout)scoreFragment.rootView.findViewById(R.id.scores_table)).getChildAt(holeCount+1);
                        TextView overallScore = (TextView) lastRow.getChildAt(i+2);
                        String s = overallScore.getText().toString();
                        StringBuilder b = new StringBuilder();
                        i = 0;
                        while(s.charAt(i) != '('){
                            b.append(s.charAt(i));
                            i++;
                        }
                        int oldScore = Integer.parseInt(b.toString());
                        if(oldThrowCount < Integer.parseInt(throwCount)) overallScore.setText(String.valueOf(++oldScore) + "(" + score + ")");
                        else overallScore.setText(String.valueOf(--oldScore) + "(" + score + ")");
                        break;
                    }
                }
            }
        }

    }

    public static class ScoreFragment extends Fragment{
        View rootView;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.view_scorecard, container, false);
            TextView t = (TextView) rootView.findViewById(R.id.textView3);
            t.setText("Final scores");
            setHeader();
            setScores();
            return rootView;
        }

        public static ScoreFragment newInstance(){
            return new ScoreFragment();
        }

        void setHeader(){
            TableLayout layout = (TableLayout) rootView.findViewById(R.id.scores_table);
            TableRow header = (TableRow) layout.getChildAt(0);
            LinearLayout.LayoutParams params = new TableRow.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(5,0,5,0);

            for(String s : playerList){
                TextView name = new TextView(getContext());
                name.setText(s);
                name.setLayoutParams(params);
                header.addView(name);
            }
        }

        private void setScores(){
            holes.moveToFirst();

            TableLayout layout = (TableLayout) rootView.findViewById(R.id.scores_table);
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(5,0,5,0);
            for(int i=0;i<holes.getCount();i++){
                TableRow row = new TableRow(getContext());
                TextView idView = new TextView(getContext());
                String id = Integer.toString(i+1);
                idView.setText(id);
                idView.setLayoutParams(params);
                idView.setGravity(Gravity.CENTER);
                row.addView(idView);
                TextView parView = new TextView(getContext());
                parView.setText(holes.getString(holes.getColumnIndex("PAR")));
                parView.setGravity(Gravity.CENTER);
                params.gravity = Gravity.NO_GRAVITY;
                params.setMargins(5,0,5,0);
                parView.setLayoutParams(params);
                row.addView(parView);
                for(int j=0;j<playerList.size();j++){
                    TextView t = new TextView(getContext());
                    t.setText(parView.getText());
                    t.setGravity(Gravity.CENTER);
                    row.addView(t);
                }
                layout.addView(row);
                holes.moveToNext();
            }

            TableRow lastRow = new TableRow(getContext());
            lastRow.addView(new TextView(getContext()));
            TextView par = new TextView(getContext());
            course.moveToFirst();
            par.setText(course.getString(course.getColumnIndex("PAR")));
            par.setGravity(Gravity.CENTER);
            lastRow.addView(par);
            layout.addView(lastRow);
            for(int i=0;i<playerList.size();i++){
                TextView t = new TextView(getContext());
                t.setText(par.getText());
                t.setGravity(Gravity.CENTER);
                lastRow.addView(t);
            }

            int i=2;
            players.moveToFirst();
            while(!players.isAfterLast()){
                String playerId = players.getString(0);
                holes.moveToFirst();
                int j = 1;
                while(!holes.isAfterLast()){
                    String holeId = holes.getString(0);
                    TableRow row = (TableRow)layout.getChildAt(j);
                    TextView score = (TextView) row.getChildAt(i);
                    score.setText(scorecards.get(playerId+holeId).throwCount);
                    holes.moveToNext();
                    j++;
                }
                players.moveToNext();
                i++;
            }

            i=2;
            for(int k=0;k<playerList.size();k++) {
                int throwCount = 0;
                for (int j = 0; j < holeCount; j++) {
                    TableRow row = (TableRow) layout.getChildAt(j + 1);
                    TextView scoreView = (TextView) row.getChildAt(i);
                    throwCount += Integer.parseInt(scoreView.getText().toString());
                }
                int score = throwCount - Integer.parseInt(par.getText().toString());
                ((TextView)lastRow.getChildAt(i)).setText(String.valueOf(throwCount) + "(" + String.valueOf(score) + ")");
                i++;
            }
            //TODO winnerText.setText(playerName);
        }
    }

    private class CustomPagerAdapter extends FragmentStatePagerAdapter {

        private int NUM_ITEMS = holeCount;

        CustomPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new SparseArray<>();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            if (position < NUM_ITEMS) fragments.put(position, fragment);
            else if(position == NUM_ITEMS) scoreFragment = (ScoreFragment) fragment;
            return fragment;
        }

        @Override
        public Fragment getItem(int position) {
            holes.moveToFirst();
            for(int i=0;i<NUM_ITEMS;i++){
                if(i == position) {
                    return HoleFragment.newInstance(position, holes);
                }
                holes.moveToNext();
            }
            if(NUM_ITEMS == position) return ScoreFragment.newInstance();
            return null;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if(position < NUM_ITEMS) fragments.remove(position);
            else if(position == NUM_ITEMS) scoreFragment = null;
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS+1;
        }

    }

    private class Scorecards {
        String playerId;
        String holeId;
        int gameId;
        int ob;
        String throwCount;
        Date date;

        Scorecards(String playerId, String holeId, int gameId, int ob, String throwCount){
            this.playerId = playerId;
            this.holeId = holeId;
            this.gameId = gameId;
            this.ob = ob;
            this.throwCount = throwCount;
            this.date = Calendar.getInstance().getTime();
        }
    }

    void showFinishDialog(){
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("Finish course")
                .setCancelable(false)
                .setMessage("Are you sure you want to finish course?")
                .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finished = true;
                        Intent i = setActivityResults(finished);
                        finish();
                        startActivity(i);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .show();
    }

    Intent setActivityResults(boolean finished){
        SharedPreferences.Editor spEditor = getSharedPreferences("sharedPreferences", MODE_PRIVATE).edit();
        Intent i = new Intent(PlayCourseActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if(finished) {
            spEditor.putInt("gameId", 0);
            i.putExtra("gameId", 0);
            setResult(Activity.RESULT_OK, i);
        }
        else{
            spEditor.putInt("gameId", gameId);
            i.putExtra("gameId", gameId);
            setResult(Activity.RESULT_CANCELED, i);
        }
        spEditor.apply();

        return i;
    }

}
