package com.doublesoft.dgscores;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class CoursesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton btnAddCourse = (FloatingActionButton) findViewById(R.id.btnAddCourse);

        btnAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CoursesActivity.this, AddCourseActivity.class);
                CoursesActivity.this.startActivity(i);
            }
        });
    }


}
