package com.doublesoft.dgscores;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import static android.R.color.holo_green_dark;

public class PlayCourse extends AppCompatActivity {

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

    //DEBUG
    Button debug_insert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            gameId = savedInstanceState.getInt("gameId");
            final AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                                       .setTitle("Jatka peliä?")
                                        .setMessage("Haluatko jatkaa peliä?");
                       dialog.show();
        }

        setContentView(R.layout.activity_play_course);
        getSupportActionBar().hide();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;

        Intent intent = this.getIntent();
        String courseName = intent.getStringExtra("courseName");
        playerList = intent.getStringArrayListExtra("players");
        scores = new HashMap<>();
        scoreViews = new HashMap<>();
        for(String p : playerList){
            scores.put(p, "0");
        }

        db = new DatabaseAdapter(context);
        db.open();
        course = db.getCourse(courseName);
        course.moveToFirst();
        players = db.getCoursePlayers(playerList);
        holes = db.getFairways(courseName);

        holeCount = Integer.parseInt(course.getString(course.getColumnIndex("HOLE_COUNT")));

        ((TextView) findViewById(R.id.textView10)).setText(courseName);

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if(position == 0) prev.setEnabled(false);
                else prev.setEnabled(true);
                if(position == holeCount) {
                    next.setText("FINISH");
                    next.setBackgroundColor(getResources().getColor(holo_green_dark));
                }
                else next.setText("NEXT");
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
                    insertScorecards();
                    onBackPressed();
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

       /*debug_insert = (Button) findViewById(R.id.debug_insertScorecard);
       debug_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues[] values = new ContentValues[scorecards.size()];
                int i=0;
                for(HashMap.Entry e : scorecards.entrySet()){
                    Scorecards sc = (Scorecards) e.getValue();
                    values[i] = new ContentValues();
                    values[i].put("PLAYER_ID", Integer.parseInt(sc.playerId));
                    values[i].put("FAIRWAY_ID", Integer.parseInt(sc.fairwayId));
                    values[i].put("GAME_ID", sc.gameId);
                    values[i].put("OB", sc.ob);
                    values[i].put("THROW_COUNT", Integer.parseInt(sc.throwCount));
                    values[i].put("DATE", sc.date.toString());
                    i++;
                }
                db.insertScorecard(values);
                Toast.makeText(context, "Scorecards inserted!", Toast.LENGTH_SHORT).show();
            }
        });*/

        holes.moveToFirst();
        players.moveToFirst();

        Random rand = new Random(System.currentTimeMillis());
        gameId = rand.nextInt(Integer.SIZE - 1);
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

        //Log.i("Scorecards row count", Integer.toString(scorecards.size()));

    }

    void insertScorecards(){
        ContentValues[] values = new ContentValues[scorecards.size()];
        int i=0;
        for(HashMap.Entry e : scorecards.entrySet()){
            Scorecards sc = (Scorecards) e.getValue();
            values[i] = new ContentValues();
            values[i].put("PLAYER_ID", Integer.parseInt(sc.playerId));
            values[i].put("FAIRWAY_ID", Integer.parseInt(sc.fairwayId));
            values[i].put("GAME_ID", sc.gameId);
            values[i].put("OB", sc.ob);
            values[i].put("THROW_COUNT", Integer.parseInt(sc.throwCount));
            values[i].put("DATE", sc.date.toString());
            i++;
        }
        db.insertScorecard(values);
        Toast.makeText(context, "Scorecards inserted!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putInt("gameId", gameId);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //TODO: shared preferences gameId ja scorecards insert tännne
    }

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
                    addRow(playerList.get(i), holeId, playerId[i]);
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

        private void addRow(final String player, final String holeId, final String playerId){
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
                        updateOverallScores(fragments.get(key), player, sV);
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
                        updateOverallScores(fragments.get(key), player, sV);
                    }
                    if(score.getText().equals("2")){
                        subtract.setClickable(true);
                    }
                }
            });

            layout.addView(row);

        }

        private void updateOverallScores(Fragment fragment, String player, String score){
            LinearLayout layout = (LinearLayout)((HoleFragment)fragment).rootView.findViewById(R.id.fragment_layout);
            for(int i=0;i<layout.getChildCount();i++){
                RelativeLayout row = (RelativeLayout) layout.getChildAt(i);
                TextView scoreView = (TextView) row.findViewById(R.id.row_scoreview);
                TextView playerView = (TextView) row.findViewById(R.id.row_name);
                if(playerView.getText().toString().equals(player)) {
                    scoreView.setText("Score: " + score);
                    return;
                }
            }
            if(scoreFragment != null){
                //TODO: päivitä tulos
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
            t.setText("Overall scores");
            setHeader();
            setScores();
            return rootView;
        }

        public static ScoreFragment newInstance(int position){

            ScoreFragment fragment = new ScoreFragment();

            return fragment;
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

        void setScores(){
            holes.moveToFirst();
            TableLayout layout = (TableLayout) rootView.findViewById(R.id.scores_table);
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(5,0,0,0);
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
                params.setMargins(0,0,5,0);
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
            if(NUM_ITEMS == position) return ScoreFragment.newInstance(position);
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
        String fairwayId;
        int gameId;
        int ob;
        String throwCount;
        Date date;

        Scorecards(String playerId, String fairwayId, int gameId, int ob, String throwCount){
            this.playerId = playerId;
            this.fairwayId = fairwayId;
            this.gameId = gameId;
            this.ob = ob;
            this.throwCount = throwCount;
            this.date = Calendar.getInstance().getTime();
        }
    }

}
