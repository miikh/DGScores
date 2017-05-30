package com.doublesoft.dgscores;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Miika on 16.5.2017.
 */

public class ViewScorecardsActivity extends ScorecardsActivity {

    ArrayList<String> playersList;
    DatabaseAdapter db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_scorecard);
        context = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DatabaseAdapter(context);
        db.open();

        Bundle b = getIntent().getExtras();
        long courseId = b.getLong("courseId");
        String courseName = b.getString("courseName");
        int gameId = b.getInt("gameId");
        playersList = b.getStringArrayList("players");

        TextView courseNameView = (TextView) findViewById(R.id.textView3);
        courseNameView.setText(courseName);

        setHeader();
        setScores(courseId, gameId);

        db.close();

    }

    void setHeader(){
        TableLayout layout = (TableLayout) findViewById(R.id.scores_table);
        TableRow header = (TableRow) layout.getChildAt(0);
        LinearLayout.LayoutParams params = new TableRow.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(5,0,5,0);

        assert playersList != null;
        for(String s : playersList){
            TextView name = new TextView(context);
            name.setText(s);
            name.setLayoutParams(params);
            header.addView(name);
        }
    }

    void setScores(long courseId, int gameId){
        Cursor holes = db.getFairways(courseId);
        holes.moveToFirst();
        TableLayout layout = (TableLayout) findViewById(R.id.scores_table);
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(5,0,0,0);
        for(int i=0;i<holes.getCount();i++){
            TableRow row = new TableRow(context);
            TextView idView = new TextView(context);
            String id = Integer.toString(i+1);
            idView.setText(id);
            idView.setLayoutParams(params);
            idView.setGravity(Gravity.CENTER);
            row.addView(idView);
            TextView parView = new TextView(context);
            parView.setText(holes.getString(holes.getColumnIndex("PAR")));
            parView.setGravity(Gravity.CENTER);
            params.gravity = Gravity.NO_GRAVITY;
            params.setMargins(0,0,5,0);
            parView.setLayoutParams(params);
            row.addView(parView);
            for(int j=0;j<playersList.size();j++){
                TextView t = new TextView(context);
                t.setGravity(Gravity.CENTER);
                row.addView(t);
            }
            layout.addView(row);
            holes.moveToNext();
        }

        Cursor course = db.getCourse(courseId);
        TableRow lastRow = new TableRow(context);
        lastRow.addView(new TextView(context));
        TextView par = new TextView(context);
        course.moveToFirst();
        par.setText(course.getString(course.getColumnIndex("PAR")));
        par.setGravity(Gravity.CENTER);
        lastRow.addView(par);
        layout.addView(lastRow);
        for(int i=0;i<playersList.size();i++){
            TextView t = new TextView(context);
            t.setText("0");
            t.setGravity(Gravity.CENTER);
            lastRow.addView(t);
        }

        int i=2;
        for(String playerName : playersList){
            Cursor c = db.getPlayer(playerName);
            c.moveToFirst();
            Cursor scores = db.getScorecardsByGameIdAndPlayerId(gameId, c.getInt(0));
            int j=1;
            while(scores.moveToNext()){
                TableRow row = (TableRow) layout.getChildAt(j);
                TextView score = (TextView) row.getChildAt(i);
                int throwCount = scores.getInt(scores.getColumnIndex("THROW_COUNT"));
                score.setText(String.valueOf(throwCount));
                TextView overallScoreView = (TextView) lastRow.getChildAt(i);
                int overallScore = Integer.parseInt(overallScoreView.getText().toString());
                overallScoreView.setText(String.valueOf(throwCount+overallScore));
                j++;
            }
            i++;
        }

        i = 2;
        for(int j=0;j<playersList.size();j++){
            TextView scoreView = (TextView) lastRow.getChildAt(i);
            int score = Integer.parseInt(scoreView.getText().toString()) - Integer.parseInt(par.getText().toString());
            scoreView.append("(" + String.valueOf(score) + ")");
            i++;
        }

        holes.close();
        course.close();

    }

}
