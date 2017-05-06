package com.doublesoft.dgscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class PlayCourse extends AppCompatActivity {

    Context context;
    DatabaseAdapter db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_course);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;

        Intent intent = this.getIntent();
        String courseName = intent.getStringExtra("courseName");
        ArrayList<String> players = intent.getStringArrayListExtra("players");

        db = new DatabaseAdapter(context);
        db.open();
        Cursor course = db.getCourse(courseName);



    }

}
