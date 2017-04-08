package com.doublesoft.dgscores;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private DatabaseAdapter db = null;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        db = new DatabaseAdapter(context);
        db.open();

        Button btnPlay = (Button) findViewById(R.id.button_play);
        Button btnEditCourses = (Button) findViewById(R.id.button_edit_courses_);
        Button btnSettings = (Button) findViewById(R.id.button_settings);
        Button btnEditPlayers = (Button) findViewById(R.id.button_edit_players);
        Button btnScorecards = (Button) findViewById(R.id.button_scorecards);
        Button btnStatistics = (Button) findViewById(R.id.button_statistics);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PlayCourseActivity.class);
                MainActivity.this.startActivity(i);
            }
        });

        btnEditCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, CoursesActivity.class);
                MainActivity.this.startActivity(i);
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivity(i);
            }
        });

        btnEditPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Players.class);
                MainActivity.this.startActivity(i);
            }
        });

        btnScorecards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ScorecardsActivity.class);
                MainActivity.this.startActivity(i);
            }
        });

        btnStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, StatisticsActivity.class);
                MainActivity.this.startActivity(i);
                ;
            }
        });
    }


}
