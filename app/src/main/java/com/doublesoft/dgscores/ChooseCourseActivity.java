package com.doublesoft.dgscores;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ChooseCourseActivity extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_course);
        context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
