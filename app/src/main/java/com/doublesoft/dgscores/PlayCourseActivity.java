package com.doublesoft.dgscores;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PlayCourseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_course);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}