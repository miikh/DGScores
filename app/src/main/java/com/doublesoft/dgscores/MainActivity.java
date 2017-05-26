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
                            .setMessage("Unfinished course found!\nWant to continue course?")
                            .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(MainActivity.this, PlayCourseActivity.class);
                                    startActivityForResult(i, 1);
                                }
                            })
                            .setNegativeButton("Start New Game", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor spEditor = getSharedPreferences("sharedPreferences", MODE_PRIVATE).edit();
                                    spEditor.putInt("gameId", 0);
                                    spEditor.apply();
                                    Intent i = new Intent(MainActivity.this, StartCourseActivity.class);
                                    startActivityForResult(i, 1);
                                }
                            })
                            .show();
                }
                else {
                    Intent i = new Intent(MainActivity.this, StartCourseActivity.class);
                    MainActivity.this.startActivity(i);
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
        if(requestCode == 1){
            //TODO: Intent data tulee nullina
            if(resultCode == Activity.RESULT_OK) {
                gameId = data.getIntExtra("gameId", 0);
                btnPlay.setText(getResources().getString(R.string.title_activity_play_course));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
