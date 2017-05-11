package com.doublesoft.dgscores;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class PlayCourse extends AppCompatActivity {

    static Context context;
    static DatabaseAdapter db;
    static Cursor course;
    static Cursor players;
    static Cursor holes;
    static int holeCount;
    static ArrayList<String> playerList;
    static HashMap<String,Scorecards> scorecards;
    static HashMap<String, String> scores;
    static HashMap<String, TextView> scoreViews;
    static ViewPager viewPager;
    static boolean instantiated;
    CustomPagerAdapter pagerAdapter;
    Button next;
    Button prev;

    //DEBUG
    Button debug_insert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_course);
        getSupportActionBar().hide();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;

        Intent intent = this.getIntent();
        String courseName = intent.getStringExtra("courseName");
        playerList = intent.getStringArrayListExtra("players");
        scores = new HashMap<>();
        scoreViews = new HashMap<>();
        instantiated = false;
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

        String[] a = new String[playerList.size()];
        //players = db.getCoursePlayers(playersList.toArray(a));

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if(position == 0) prev.setEnabled(false);
                else prev.setEnabled(true);
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
                viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
            }
        });

       /* debug_insert = (Button) findViewById(R.id.debug_insertScorecard);
       debug_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.insertScorecard(debugCard);
            }
        });*/

        holes.moveToFirst();
        players.moveToFirst();

        Random rand = new Random(System.currentTimeMillis());
        int gameId = rand.nextInt(Integer.SIZE - 1);
        scorecards = new HashMap<String, Scorecards>();
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

        Log.i("Scorecards row count", Integer.toString(scorecards.size()));


    }

    @Override
    protected void onResume() {
        super.onResume();
        //instantiated = true;
    }

    public static class HoleFragment extends Fragment {

        View rootView;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_play_course, container, false);

            Bundle b = getArguments();
            int hole = b.getInt("hole");
            int par = b.getInt("par");
            String holeId = b.getString("holeId");
            String[] playerId = b.getStringArray("playerId");
            TextView holeView = (TextView) rootView.findViewById(R.id.textView2);

            holeView.setText("Hole " + (hole+1));

            players.moveToFirst();
            for(int i=0;i<playerId.length;i++){
                if(playerId[i].equals(players.getString(0)))
                    addRow(playerList.get(i), par, holeId, playerId[i]);
                players.moveToNext();
            }

            return rootView;
        }

        public static HoleFragment newInstance(int position, Cursor hole){

            HoleFragment fragment = new HoleFragment();
            int par = Integer.parseInt(hole.getString(hole.getColumnIndex("PAR")));
            String holeId = hole.getString(0);
            players.moveToFirst();
            String playerId[] = new String[players.getCount()];
            for(int i=0;i<playerId.length;i++){
                playerId[i] = players.getString(0);
                players.moveToNext();
            }
            Bundle b = new Bundle();
            b.putInt("hole", position);
            b.putInt("par", par);
            b.putString("holeId", holeId);
            b.putStringArray("playerId", playerId);
            fragment.setArguments(b);
            return fragment;
        }

        private void addRow(final String player, final int par, final String holeId, final String playerId){
            LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.fragment_layout);
            LayoutInflater inflater = LayoutInflater.from(context);
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
                    scores.put(player, sV);
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
                    scores.put(player, sV);
                    if(score.getText().equals("2")){
                        subtract.setClickable(true);
                    }
                }
            });

            layout.addView(row);

        }

        private void changeScore(String player){
            TextView t = scoreViews.get(player);
            t.setText("Score: " + scores.get(player));
            scoreViews.put(player, t);
        }

    }

    private class CustomPagerAdapter extends FragmentStatePagerAdapter {

        private int NUM_ITEMS = holeCount;

        public CustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            Fragment fragment = (Fragment) super.instantiateItem(container, position);

            players.moveToFirst();
            if(instantiated) {
                //TODO: ei toimi
                for (int i = 0; i < players.getCount(); i++) {
                    String player = players.getString(1);
                    ((HoleFragment) fragment).changeScore(player);
                    players.moveToNext();
                }
            }
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
            return null;
        }


        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

    }

    private class Scorecards {
        String playerId;
        String fairwayId;
        int gameId;
        int ob;
        String throwCount;
        Date date;

        public Scorecards(String playerId, String fairwayId, int gameId, int ob, String throwCount){
            this.playerId = playerId;
            this.fairwayId = fairwayId;
            this.gameId = gameId;
            this.ob = ob;
            this.throwCount = throwCount;
            this.date = Calendar.getInstance().getTime();
        }

        public String getPlayerId() {
            return playerId;
        }
        public String getFairwayId() {
            return fairwayId;
        }
        public int getGameId() {
            return gameId;
        }
        public int getOb() {
            return ob;
        }
        public String getThrowCount() {
            return throwCount;
        }
        public Date getDate() {
            return date;
        }

        public void setDate() {
            this.date = Calendar.getInstance().getTime();
        }
    }


}
