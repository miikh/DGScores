package com.doublesoft.dgscores;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPlay = (Button) findViewById(R.id.button_play);
        Button btnEditCourses = (Button) findViewById(R.id.button_edit_courses_);
        Button btnSettings = (Button) findViewById(R.id.button_settings);
        Button btnEditPlayers = (Button) findViewById(R.id.button_edit_players);
        Button btnScorecards = (Button) findViewById(R.id.button_scorecards);
        Button btnStatistics = (Button) findViewById(R.id.button_statistics);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Play_course.class);
                MainActivity.this.startActivity(i);
            }
        });

        btnEditCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Courses.class);
                MainActivity.this.startActivity(i);
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Settings.class);
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
                Intent i = new Intent(MainActivity.this, Scorecards.class);
                MainActivity.this.startActivity(i);
            }
        });

        btnStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Statistics.class);
                MainActivity.this.startActivity(i);
            }
        });
    }


}
