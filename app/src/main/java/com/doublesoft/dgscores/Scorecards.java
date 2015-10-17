package com.doublesoft.dgscores;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class Scorecards extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecards);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
