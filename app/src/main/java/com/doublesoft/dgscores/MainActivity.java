package com.doublesoft.dgscores;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Context context;
    Button btnPlay;
    int gameId;
    final int CONTINUE = 1;
    final int NEW_GAME = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        btnPlay = (Button) findViewById(R.id.button_play);
        Button btnEditCourses = (Button) findViewById(R.id.button_courses);
        Button btnEditPlayers = (Button) findViewById(R.id.button_players);
        Button btnScorecards = (Button) findViewById(R.id.button_scorecards);

        SharedPreferences sp = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        gameId = sp.getInt("gameId", 0);
        if(gameId > 0){
            StringBuilder s = new StringBuilder();
            s.append(btnPlay.getText().toString());
            s.append("/RESUME");
            btnPlay.setText(s.toString());
        }

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gameId > 0){
                    new AlertDialog.Builder(context)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setTitle("Continue course?")
                            .setMessage("Unfinished course found!\nWant to continue course?\nUnfinished course will be lost!")
                            .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(MainActivity.this, PlayCourseActivity.class);
                                    startActivityForResult(i, CONTINUE);
                                }
                            })
                            .setNegativeButton("Start New Game", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DatabaseAdapter db = new DatabaseAdapter(context);
                                    db.open();
                                    db.deleteByGameId(gameId);
                                    db.close();
                                    SharedPreferences.Editor spEditor = getSharedPreferences("sharedPreferences", MODE_PRIVATE).edit();
                                    spEditor.putInt("gameId", 0);
                                    spEditor.apply();
                                    Intent i = new Intent(MainActivity.this, StartCourseActivity.class);
                                    startActivityForResult(i, NEW_GAME);
                                    gameId = 0;
                                }
                            })
                            .show();
                }
                else {
                    Intent i = new Intent(MainActivity.this, StartCourseActivity.class);
                    MainActivity.this.startActivityForResult(i, NEW_GAME);
                }
            }
        });

        btnEditCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, CoursesActivity.class);
                MainActivity.this.startActivity(i);
            }
        });

        btnEditPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PlayersActivity.class);
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == NEW_GAME){
            if(resultCode == Activity.RESULT_OK) {
                btnPlay.setText(getResources().getString(R.string.title_activity_play_course));
                gameId = 0;
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                if(data != null) {
                    btnPlay.setText(getResources().getString(R.string.title_activity_play_course) + "/RESUME");
                    gameId = data.getIntExtra("gameId", 0);
                }
            }
        }
        else if(requestCode == CONTINUE){
            if(resultCode == Activity.RESULT_OK){
                btnPlay.setText(getResources().getString(R.string.title_activity_play_course));
                gameId = 0;
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                if(data != null) {
                    btnPlay.setText(getResources().getString(R.string.title_activity_play_course) + "/RESUME");
                    gameId = data.getIntExtra("gameId", 0);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
